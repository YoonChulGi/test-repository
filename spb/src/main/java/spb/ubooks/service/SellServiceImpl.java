package spb.ubooks.service;

import java.io.File;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import spb.common.FileUtils;
import spb.ubooks.dto.ComBookIndexDto;
import spb.ubooks.entity.CheckoutEntity;
import spb.ubooks.entity.CombookEntity;
import spb.ubooks.entity.FileEntity;
import spb.ubooks.mapper.MemberMapper;
import spb.ubooks.repository.CheckoutRepository;
import spb.ubooks.repository.CombookRepository;
import spb.ubooks.repository.FileRepository;

@Slf4j
@Service
public class SellServiceImpl implements SellService{

	@Autowired
	CombookRepository combookRepository;
	
	@Autowired
	FileRepository fileRepository;
	
	@Autowired
	CheckoutRepository checkoutRepository;
	
	@Autowired
	MemberMapper memberMapper;
	
	@Autowired
	FileUtils fileUtils;
	
	@Autowired
	RestHighLevelClient client;
	
	@Autowired
	SearchService searchService;
	
	@Autowired
	CartService cartService;
	
	@Override
	public ComBookIndexDto registProduct(CombookEntity combook, MultipartHttpServletRequest multipartHttpServletRequest,int bid ) throws Exception {
		HttpSession session = multipartHttpServletRequest.getSession();
		String memberId = session.getAttribute("memberId").toString();
		String sellerName = session.getAttribute("memberName").toString();
		String sellerContact = memberMapper.selectMemberContactByMemberId(memberId);
		String yyyymmddmiss = "";
		if(bid!=0) {
			combook.setBook_id(bid);
		}
		combook.setSeller_id(memberId);
		combook.setSeller_name(sellerName);
		combook.setSeller_contact(sellerContact);
		String minAge = combook.getMin_age();
		
		ComBookIndexDto combookIndexDto = null;
		
		if("초등전학년".equals(minAge) || "초등3학년이상".equals(minAge)) {
			combook.setMax_age(combook.getMin_age());
		} else {
			String[] minMaxAges = combook.getMin_age().split("-");
			combook.setMin_age(minMaxAges[0]);
			combook.setMax_age(minMaxAges[1]);
		}
		LocalDateTime now = LocalDateTime.now();
		
		
		yyyymmddmiss += now.getYear();
		yyyymmddmiss += addZero(now.getMonthValue());
		yyyymmddmiss += addZero(now.getDayOfMonth());
		yyyymmddmiss += addZero(now.getHour());
		yyyymmddmiss += addZero(now.getMinute());
		yyyymmddmiss += addZero(now.getSecond());
		
		if(bid==0) { // create
			combook.setReg_date(yyyymmddmiss);
		} else { // update
			String regDate = combookRepository.findRegDateById(bid);
			combook.setReg_date(regDate);
			combook.setUpdated_date(yyyymmddmiss);
		}
		
		CombookEntity ce = combookRepository.save(combook);
		int bookId = ce.getBook_id();
		
		Iterable<FileEntity> it = null;
		List<FileEntity> list = fileUtils.parseFileInfo(multipartHttpServletRequest,bookId);
		if(CollectionUtils.isEmpty(list)== false) { // 파일 존재할 시
			it = new Iterable<FileEntity>() {
				@Override
				public Iterator<FileEntity> iterator() {
					return list.iterator();
				}
			};
			if(bid!=0) {
				fileRepository.deleteByBookId(bid);
			}
			fileRepository.saveAll(it);
		}
		combookIndexDto = new ComBookIndexDto();
		combookIndexDto.setCombook(ce);
		combookIndexDto.setImages(list);
		return combookIndexDto;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void indexProduct(ComBookIndexDto combookIndexDto) throws Exception {
		log.debug("addIndex");
		LocalDateTime now = LocalDateTime.now();
		String indexName = "combook";
		indexName += now.getYear();
		indexName += ".";
		indexName += addZero(now.getMonthValue());
		indexName += ".";
		indexName += addZero(now.getDayOfMonth());
		
		IndexRequest request = new IndexRequest(indexName);
		request.id();
		
		CombookEntity combook = combookIndexDto.getCombook();
		List<FileEntity> images = combookIndexDto.getImages();
		String imagesValue = "";
		
		for(int i=0; i<images.size(); i++) {
			imagesValue += images.get(i).getStoredFilePath();
			if((i+1) != images.size()) {
				imagesValue += "|";
			}
		}
		
		ObjectMapper objectMapper = new ObjectMapper();
		@SuppressWarnings("rawtypes")
		Map doc = objectMapper.convertValue(combook, Map.class);
		doc.put("images", imagesValue);
		
		request.source(doc);
		
		client.indexAsync(request, RequestOptions.DEFAULT, new ActionListener<IndexResponse>() {
			
			@Override
			public void onResponse(IndexResponse response) {
				log.debug("###index Success###");
				IndexRequest req = new IndexRequest("auto_complete");
				log.debug(response.toString());
				req.id((combook.getBook_id()+""));
				Map<String,Object> doc = new HashMap<>();
				doc.put("auto_complete", combook.getTitle());
				req.source(doc);
				client.indexAsync(req, RequestOptions.DEFAULT, new ActionListener<IndexResponse>() {

					@Override
					public void onResponse(IndexResponse response) {
						log.debug("###auto_complete index Success###");
						log.debug(response.toString());
					}

					@Override
					public void onFailure(Exception e) {
						log.debug("###auto_complete index failed###");
						e.printStackTrace();
					}
					
				});
			}

			@Override
			public void onFailure(Exception e) {
				e.printStackTrace();
			}
			
		});
		
	}
	
	

	@Override
	public Map<String, Object> dataSettingForUpdateProduct(Map<String, Object> combookMap) throws Exception {
		
		String minAge = combookMap.get("min_age").toString();
		String maxAge = combookMap.get("max_age").toString();
		if(!("초등3학년이상".equals(minAge) || "초등전학년".equals(minAge))) {
			combookMap.put("min_age", minAge + "-" + maxAge);
		}
		
		String[] images =  combookMap.get("images").toString().split("|");
		String imagesRes = "";
		for(int i=0;i<images.length;i++) {
			if(i != images.length-1) {
				imagesRes += "/dev/workspace/spb/src/main/resources/static" + images[i] + "|";
			} else {
				imagesRes += "/dev/workspace/spb/src/main/resources/static" + images[i];
			}
		}
		combookMap.put("images", imagesRes);
		
		return combookMap;
	}

	@Override
	public void updateIndexProduct(ComBookIndexDto combookIndexDto) throws Exception {
		log.debug("updateIndexProduct");
		int bid = combookIndexDto.getCombook().getBook_id();
		Map<String,String> indexNameAndIdAndImages = searchService.getIndexNameAndIdAndImagesByBookId("combook*",bid); 
		String indexName = indexNameAndIdAndImages.get("indexName");
		String _id = indexNameAndIdAndImages.get("_id");
		String oldImages = indexNameAndIdAndImages.get("images"); 
		
		ObjectMapper objectMapper = new ObjectMapper();
		@SuppressWarnings("unchecked")
		Map<String,Object> doc = objectMapper.convertValue(combookIndexDto.getCombook(), Map.class);
		List<FileEntity> images = combookIndexDto.getImages();
		String imagesRes = "";
		for(int i=0; i<images.size();i++) {
			if(i != images.size()-1) {
				imagesRes += images.get(i).getStoredFilePath() + "|";
			} else {
				imagesRes += images.get(i).getStoredFilePath();
			}
		}
		if(!"".equals(imagesRes)) {
			doc.put("images", imagesRes );
		}
		
		UpdateRequest request = new UpdateRequest(indexName,_id).doc(doc);
		try {
			client.updateAsync(request, RequestOptions.DEFAULT, new ActionListener<UpdateResponse>() {

				@Override
				public void onResponse(UpdateResponse response) { // 기존 파일 삭제 로직.
					log.debug("Update Success");
					String[] filePaths = oldImages.split("\\|");
					for(String filePath : filePaths) {
						File file = new File(filePath);
						if(file.exists()) {
							if(file.delete()) {
								log.debug("["+filePath+"] file deleted successfully");
							} else {
								log.debug("["+filePath+"] file delete fail");
							}
						} else {
							log.debug("["+filePath+"] file dosen't exist");
						}
					}
					
					// 자동완성 update
					Map<String,Object> map = new HashMap<>();
					map.put("auto_complete",doc.get("title").toString());
					UpdateRequest req = new UpdateRequest("auto_complete",bid+"").doc(map);
					try {
						client.updateAsync(req, RequestOptions.DEFAULT, new ActionListener<UpdateResponse>() {

							@Override
							public void onResponse(UpdateResponse response) {
								log.debug("update auto-complete success");
								log.debug(response.toString());
							}

							@Override
							public void onFailure(Exception e) {
								log.debug("update auto-complete failed");
								e.printStackTrace();
							}
							
						});
					}catch (Exception e) {
						e.printStackTrace();
					}
				}

				@Override
				public void onFailure(Exception e) {
					e.printStackTrace();
				}
				
			});
		}catch (ElasticsearchException e) {
			e.printStackTrace();
		}
	}
	
	

	@Override
	public boolean deleteProduct(HttpServletRequest request, int bookId) throws Exception {
		
		Optional<CombookEntity> optional = combookRepository.findById(bookId);
		
		if(optional.isPresent()) {
			HttpSession session= request.getSession();
			String memberId = session.getAttribute("memberId").toString();
			CombookEntity ce = optional.get();
			if(ce.getSeller_id().toString().equals(memberId)) {
				ce.setDelete_yn('y');
				combookRepository.save(ce);
				return true;
			} else {
				return false;
			}
		} else {
			return false; 
		}
		
	}

	@Override
	public void deleteIndexProduct(int bookId) throws Exception {
		Map<String, String> doc = searchService.getIndexNameAndIdAndImagesByBookId("combook*", bookId);
		String indexName = doc.get("indexName");
		String _id = doc.get("_id");
		
		DeleteRequest request = new DeleteRequest(indexName, _id);
		client.deleteAsync(request,RequestOptions.DEFAULT,new ActionListener<DeleteResponse>() {

			@Override
			public void onResponse(DeleteResponse response) {
				log.debug("deleted Successfully");
				DeleteRequest req = new DeleteRequest("auto_complete",bookId+"");
				client.deleteAsync(req, RequestOptions.DEFAULT, new ActionListener<DeleteResponse>() {

					@Override
					public void onResponse(DeleteResponse response) {
						log.debug("delete auto-complete success");
						log.debug(response.toString());
					}

					@Override
					public void onFailure(Exception e) {
						e.printStackTrace();
					}
					
				});
			}

			@Override
			public void onFailure(Exception e) {
				e.printStackTrace();
			}
			
		});
	}
	

	@Override
	public List<Map<String,Object>> checkoutProduct(String checkoutValues) throws Exception {
		log.debug("checkoutValues: "+checkoutValues);
		String[] bidQty = checkoutValues.split(",");
		int[] bids = new int[bidQty.length];
		int[] qtys = new int[bidQty.length];
		for(int i=0;i<bidQty.length;i++) {
			bids[i] = Integer.parseInt(bidQty[i].split("-")[0]);
			qtys[i] = Integer.parseInt(bidQty[i].split("-")[1]);
		}
		
		List<Map<String,Object>> searchResults = searchService.searchManyAsMapByIds("combook*", bids);
		List<Map<String,Object>> res = new ArrayList<>();
		for(Map<String,Object> doc : searchResults) {
			for(int i=0;i<bidQty.length;i++) {
				if(Integer.parseInt(bidQty[i].split("-")[0]) == Integer.parseInt(doc.get("book_id").toString())) {
					doc.put("qty", bidQty[i].split("-")[1]);
				}
			}
			res.add(doc);
		}
		return res;
	}

	@Override
	public Map<String, Object> calcProductsPrice(List<Map<String, Object>> products) throws Exception {
		Map<String,Object> result = new HashMap<>();
		int subtotal = 0;
		int shippingFee = 0;
		int total;
		for(Map<String,Object> item : products) {
			subtotal += Integer.parseInt(item.get("price").toString()) * Integer.parseInt(item.get("qty").toString());
			shippingFee += Integer.parseInt(item.get("shipping_fee").toString());
		}
		total = subtotal + shippingFee; 
		result.put("subtotal", subtotal);
		result.put("shippingFee", shippingFee);
		result.put("total", total);
		return result;
	}

	@Override
	public List<CheckoutEntity> orderProducts(CheckoutEntity orderInfo, HttpServletRequest request, HttpServletResponse response) throws Exception { 
		log.debug(orderInfo.toString());
		String prdIds = orderInfo.getPrdIds();
		String qtys = orderInfo.getQtys();
		int len = prdIds.split("-").length;
		List<CheckoutEntity> list = new ArrayList<>();
		for(int i=0;i<len;i++) {
			orderInfo.setIdx(Long.toString(System.nanoTime())+"-" + i);
			orderInfo.setPrdIds(prdIds.split("-")[i]);
			orderInfo.setQtys(qtys.split("-")[i]);
			CheckoutEntity ce = new CheckoutEntity();
			ce.setAddress(orderInfo.getAddress());
			ce.setCardCvc(orderInfo.getCardCvc());
			ce.setCardExpiry(orderInfo.getCardExpiry());
			ce.setCardNumber(orderInfo.getCardNumber());
			ce.setCheckoutTime(orderInfo.getCheckoutTime());
			ce.setDetailAddress(orderInfo.getDetailAddress());
			ce.setExtraAddress(orderInfo.getExtraAddress());
			ce.setFullName(orderInfo.getFullName());
			ce.setIdx(orderInfo.getIdx());
			ce.setPostcode(orderInfo.getPostcode());
			ce.setPrdIds(orderInfo.getPrdIds());
			ce.setQtys(orderInfo.getQtys());
			list.add(ce);
		}
		Iterable<CheckoutEntity> it = new Iterable<CheckoutEntity>() {

			@Override
			public Iterator<CheckoutEntity> iterator() {
				// TODO Auto-generated method stub
				return list.iterator();
			}
			
		};
		
		try {
			checkoutRepository.saveAll(it);
			String[] prdIdsArr = prdIds.split("-");
			for(String id : prdIdsArr) {
				cartService.deleteCart(id, request, response);
			}
		}catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
		return list;
	}
	

	@Override
	public void indexOrderedProducts(List<CheckoutEntity> products) throws Exception {
		String indexName = "checked_out_products";
		for(CheckoutEntity ce : products) {
			IndexRequest request = new IndexRequest(indexName);
			request.id(ce.getIdx());
			ObjectMapper objectMapper = new ObjectMapper();
			@SuppressWarnings("unchecked")
			Map<String,Object> doc = objectMapper.convertValue(ce, Map.class);
			Date date = new Date(System.currentTimeMillis());
			SimpleDateFormat sdf;
		    sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
		    sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		    String stamp = sdf.format(date);
			doc.put("@timestamp", stamp);
			request.source(doc);
			try {
				client.index(request, RequestOptions.DEFAULT);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}
	
	String addZero(int time) {
		String res = "";
		if(time<10) {
			res += "0"+time;
		} else {
			res += time;
		}
		return res;
	}

}
