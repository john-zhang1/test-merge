$('#togglemap-collection').click(function() {
    var mapwrapper = $('#citizenscimap');
    if(mapwrapper.hasClass('expanded')){
	$('#citizenscimap').height(300);
        $('#citizenscimap').removeClass('expanded');
	$('#togglemap-collection').text('Expand map');
    }
    else{
        $('#citizenscimap').height(800);
        $('#citizenscimap').addClass('expanded');
        $('#togglemap-collection').text('Collapse map');
        map._onResize();
    }
});

$("#aspect_submission_StepTransformer_field_dwc_npdg_homezip").blur(function() {
    var zipcode = $("#aspect_submission_StepTransformer_field_dwc_npdg_homezip").val();
    var value = $("#aspect_submission_StepTransformer_field_dwc_npdg_spatial").val();
    if(value.trim()=='')
        getCoordinate(zipcode);
});

function getCoordinate(zip){
  geocoder = new google.maps.Geocoder();

  geocoder.geocode({ 'address': zip }, function (results, status) {
      if (status == google.maps.GeocoderStatus.OK) {
          var geo=results[0].geometry.location;
          var loc = [];
          loc.push(results[0].formatted_address);
          var text = geo.lat() + ', ' + geo.lng()
          $("#aspect_submission_StepTransformer_field_dwc_npdg_spatial").val(text);
      }
  });
}

$('#aspect_submission_StepTransformer_field_dwc_npdg_spatial').attr('placeholder', 'latitude, longitude');
