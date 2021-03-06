function sendAction(){
	var xhr = new XMLHttpRequest();
	xhr.open('POST', 'api/admin', true);
	xhr.setRequestHeader("Content-type","text/plain; charset=UTF-8");
	xhr.onreadystatechange = function() { // Call a function when the state changes.
	    if (this.readyState === XMLHttpRequest.DONE && this.status === 200) {
			var elem = document.getElementById("result");
			elem.innerText  = this.response;
	    }
	}
	var toSend;
	var input = document.getElementById('actionId');
	if (input.value){
		toSend = "action="+input.value;
	}
	var argumentKey=document.getElementById('argumentKeyId');
	var argumentValue=document.getElementById('argumentValueId');
	if (argumentKey.value && argumentValue.value){
		var argumentString = argumentKey.value+'='+argumentValue.value;
		if (toSend){
			toSend = toSend+'&'+argumentString; 
		}
		else {
			toSend = argumentString;
		}
	}
	
	if (toSend) {
		xhr.send(encodeURI(toSend));
	}
	else {
		xhr.send();
	}
}

function ping(){
	var xhr = new XMLHttpRequest();
	xhr.open('GET', 'api/admin', true);
	xhr.onload = function () {
		var elem = document.getElementById("result");
		elem.innerText  = this.response;
	};
	xhr.send(null);
}
