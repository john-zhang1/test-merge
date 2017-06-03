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

function getTopBannerWidth(){
  var brandwidth = $('.navbar-brand-default').width();
  var brandwrapper = $('#ds-options');
  var brandpos = brandwrapper.position();
  var brandloc = brandpos.left + brandwrapper.width() + 1;
  var sidebarvisible = $('#sidebar').is(":visible");

  if(brandwidth != brandloc & sidebarvisible){
      $('.navbar-brand-default').css('width', brandloc);
      $('#topbanner-mainline').removeClass('topbanner-mainline-fixed').addClass('topbanner-mainline');
      $('#topbanner-tagline').removeClass('topbanner-tagline-fixed').addClass('topbanner-tagline');
  }
  if(!sidebarvisible){
      $('#topbanner-mainline').removeClass('topbanner-mainline').addClass('topbanner-mainline-fixed');
      $('#topbanner-tagline').removeClass('topbanner-tagline').addClass('topbanner-tagline-fixed');
      $('a.navbar-brand.navbar-brand-default').css('width',280);
  }
}

getTopBannerWidth();
showhidetaglines();

var taglineswrapper =
    '<div class="row">' +
      '<div class="navbar-brand-default">' +
        '<a href="/" class="navbar-brand navbar-brand-default">' +
          '<h5 id="topbanner-tagline" class="subtitle-italic topbanner-tagline-fixed">advancing Oklahoma scholarship, <br>research and institutional memory</h5>' +
        '</a>' +
      '</div>' +
    '</div>';

$( window ).resize(function() {
    getTopBannerWidth();
    showhidetaglines();
});

function showhidetaglines(){
  var deviceType = findBootstrapEnvironment();
  if(deviceType=='xs'){
      var row = $('#topbanner-lines > .row:eq(1)');
      row.remove();
      $('.navbar-toggle.navbar-link').css('margin-top', 20);
  }
  else{
      var rows = $('#topbanner-lines > .row');
      if(rows.length==1){
        var container = $('#topbanner-lines');
        container.append(taglineswrapper);
      }
  }
}

function findBootstrapEnvironment() {
    var envs = ['xs', 'sm', 'md', 'lg'];

    var $el = $('<div>');
    $el.appendTo($('body'));

    for (var i = envs.length - 1; i >= 0; i--) {
        var env = envs[i];

        $el.addClass('hidden-'+env);
        if ($el.is(':hidden')) {
            $el.remove();
            return env;
        }
    }
}
