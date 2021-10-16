(function ($) {
  'use strict';

  // Preloader
  $(window).on('load', function () {
    $('#preloader').fadeOut('slow', function () {
      $(this).remove();
    });
  });

  
  // Instagram Feed
  if (($('#instafeed').length) !== 0) {
    var accessToken = $('#instafeed').attr('data-accessToken');
    var userFeed = new Instafeed({
      get: 'user',
      resolution: 'low_resolution',
      accessToken: accessToken,
      template: '<a href="{{link}}"><img src="{{image}}" alt="instagram-image"></a>'
    });
    userFeed.run();
  }

  setTimeout(function () {
    $('.instagram-slider').slick({
      dots: false,
      speed: 300,
      // autoplay: true,
      arrows: false,
      slidesToShow: 6,
      slidesToScroll: 1,
      responsive: [{
          breakpoint: 1024,
          settings: {
            slidesToShow: 4
          }
        },
        {
          breakpoint: 600,
          settings: {
            slidesToShow: 3
          }
        },
        {
          breakpoint: 480,
          settings: {
            slidesToShow: 2
          }
        }
      ]
    });
  }, 1500);


  // e-commerce touchspin
  $('input[name=\'product-quantity\']').TouchSpin();


  // Video Lightbox
  $(document).on('click', '[data-toggle="lightbox"]', function (event) {
    event.preventDefault();
    $(this).ekkoLightbox();
  });


  // Count Down JS
  $('#simple-timer').syotimer({
    year: 2022,
    month: 5,
    day: 9,
    hour: 20,
    minute: 30
  });

  //Hero Slider
  $('.hero-slider').slick({
    // autoplay: true,
    infinite: true,
    arrows: true,
    prevArrow: '<button type=\'button\' class=\'heroSliderArrow prevArrow tf-ion-chevron-left\'></button>',
    nextArrow: '<button type=\'button\' class=\'heroSliderArrow nextArrow tf-ion-chevron-right\'></button>',
    dots: true,
    autoplaySpeed: 7000,
    pauseOnFocus: false,
    pauseOnHover: false
  });

  $('.hero-slider').slickAnimation();
	$('#product-modal').on('hidden.bs.modal', function () {
		$("#modal-title").text('');
		$("#modal-price").text('');
		$("#modal-state").text('');
		$("#cardPreviewImage").attr('src','');
		$('#cardPreviewImage').css("display", "none");
		$('#modal-cart').css("display", "none");
		$('#modal-detail').css("display", "none");
	});
	$("#selectSort").on("change",(event)=>{
		let sort = event.target.value.trim();
		$("#sort").val(sort);
		$("#searchForm").submit();
	});
	
	$(".cateAge").on("click",(event)=>{
		let age = event.target.innerText.trim();
		$("#age").val(age);
		$("#searchForm").submit();
	});
	
	$(".catePublisher").on("click",(event)=>{
		let publisher = event.target.innerText.trim();
		$("#publisher").val(publisher);
		$("#searchForm").submit();
	});
	
	$(".cateDepartment").on("click",(event)=>{
		let department = event.target.innerText.trim();
		$("#department").val(department);
		$("#searchForm").submit();
	});
	
	
})(jQuery);


function prevAjax(book_id) {
	
	$.ajax({
		type: "GET",
		url: "/searchOne/" + book_id,
		dataType: 'json',
		// data: {"bookId" : target, "range" : range, "collection" : collection, "datatype": datatype},
		success: function(text) {
			$("#modal-title").text(text.title);
			$("#modal-price").text('₩'+addComma(text.price));
			$("#modal-state").text(text.state);
			let image = text.images.split('|')[0];
			$("#cardPreviewImage").attr('src',image);
			$('#cardPreviewImage').css("display", "block");   
			$('#modal-cart').css("display", "inline-block");
			$('#modal-detail').attr('href','/complete-works/' + text.book_id);
			$('#modal-detail').css("display", "inline-block");
			// console.dir(text.title);
		}, 
		error:function(request,status,error) {
			console.dir(request);
			console.dir(status);
			console.error(error);
		}
		
	});
}

function addComma(value){
    value = value.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");
    return value; 
}

