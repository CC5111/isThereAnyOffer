
function trLink(url){
    window.open(
        url,
        '_blank'
    );
}


function getGames(){

    var $input = $('#typeahead');


    $.get('api/games', function(data){
        console.log(data["data"]);
        $input.typeahead({ source:data["data"] });
    },'json');

    $input.change(function() {
        var current = $input.typeahead("getActive");
        if (current) {
            // Some item from your model is active!
            if (current.name == $input.val()) {
                console.log("Match: "+ current.name);
                $('form#form').submit();

                // This means the exact match is found. Use toLowerCase() if you want case insensitive match.
            } else {
                // This means it is only a partial match, you can either add a new item
                // or take the active if you don't want new items
            }
        } else {
            // Nothing is active so it is a new value (or maybe empty value)
        }
    });
}

getGames();

var filters = {};
function filter(typeFilter, nameFilter) {
    if (filters[typeFilter] == nameFilter) delete filters[typeFilter];
    else filters[typeFilter] = nameFilter;

    var trs = $('#offers').children();
    trs.each(function(index, row) {
        var globalMatch = true;
        for (var filter in filters) {
            var datasRow = $(row).data(filter).split(',');
            var match = false;
            datasRow.forEach(function (data) {
                match = match || (data === filters[filter]);
            });
            globalMatch = globalMatch && match;
        }
        if (globalMatch) $(row).show();
        else $(row).hide();
    });
}

$('.filter-button').on('click', function (event) {
    var typeFilter = $(this).data('type');
    var nameFilter = $(this).data('name');
    filter(typeFilter, nameFilter);
});
$(document).ready(function() {
    $("#tableOffers").tablesorter( {sortList: [[0,0], [1,0]]} );
});