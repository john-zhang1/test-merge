$("#aspect_submission_StepTransformer_field_dc_npdg_homezip").blur(function() {
    var zipcode = $("#aspect_submission_StepTransformer_field_dc_npdg_homezip").val();
    var value = $("#aspect_submission_StepTransformer_field_dc_coverage_spatial").val();
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
          $("#aspect_submission_StepTransformer_field_dc_coverage_spatial").val(text);
      }
  });
}

$('#aspect_submission_StepTransformer_field_dc_coverage_spatial').attr('placeholder', 'latitude, longitude');

$( window ).resize(function() {
    var brandwidth = $('.navbar-brand-default').width();
    var brandwrapper = $('#ds-options');
    var brandpos = brandwrapper.position();
    var brandloc = brandpos.left + brandwrapper.width() + 1;
    var sidebarvisible = $('#sidebar').is(":visible");

    if(brandwidth != brandloc & sidebarvisible){
        $('.navbar-brand-default').css('width', brandloc);
    }
});
