
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
