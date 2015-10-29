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
