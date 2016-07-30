
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