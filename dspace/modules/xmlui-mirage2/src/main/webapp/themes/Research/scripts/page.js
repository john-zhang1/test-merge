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
  $.getJSON('https://cc.lib.ou.edu/api/data_store/data/citizen_science/zipcodes/.json?query={"filter":{"zip":\"' + zip + '\"}}', function(data) {
    $.each(data.results, function(index, element) {
       var text = element.latitude + ', ' + element.longitude;
       $("#aspect_submission_StepTransformer_field_dwc_npdg_spatial").val(text);
    });
  });
}

$('#aspect_submission_StepTransformer_field_dwc_npdg_spatial').attr('placeholder', 'latitude, longitude');
