package spb.ubooks.service;

import java.util.Map;

public interface SearchService {
	public String sendREST(String sendUrl, String jsonValue) throws IllegalStateException;

	public String sendRest(String url, String jsonValue) throws Exception ;
	
	public Map<String,Object> sendHighLevelApi(String indexName, String sort) throws Exception;
	
	public String searchOneAsJson(String indexName, int bookId) throws Exception;
	
	public Map<String,Object> searchOneAsMap(String indexName, int bookId) throws Exception;
}
