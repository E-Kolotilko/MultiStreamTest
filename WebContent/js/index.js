/*
function main(){
	var loggerDiv = document.getElementById('logging_div');
	var request = new XMLHttpRequest();
	request.multipart = true;
	request.open('GET', './api/borderWeird', true);
	request.onreadystatechange = function () {
		loggerDiv.inne
		loggerDiv.innerText = request.responseText;
	};
	request.send();
}

main();
*/

function appendDiv(response){
	var text = response.body;
	var loggerDiv = document.getElementById('logging_div');
	var newElem = document.createElement('div')
	newElem.innerText = text;
	loggerDiv.prepend(newElem);
}

function main(){
	var ws = new WebSocket('ws://192.168.0.102:15674/ws');
	
	
	var client = Stomp.over(ws);
	var on_connect = function() {
		var id = client.subscribe("/exchange/border_cross", function(d) {
			appendDiv(d);
		  });
	    console.log("ID jsonofied:"+JSON.stringify(id))
	};
	var on_error =  function() {
	    console.log('error');
	};
	client.onreceive = function(response) {
		console.log('Got it');
		appendDiv(response);
    }
	
	client.connect('reader_tester', 'reader', on_connect, on_error, '/');
}

main();
