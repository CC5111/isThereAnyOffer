
function trLink(url){
    window.open(
        url,
        '_blank'
    );
}

$(document).ready(function() {
    $('.panel-collapse').on('show.bs.collapse', function () {
        console.log("Abierto");
        $(this).siblings('.panel-heading').addClass('active');
    });

    $('.panel-collapse').on('hide.bs.collapse', function () {
        console.log("Cerrado");
        $(this).siblings('.panel-heading').removeClass('active');
    });
});
