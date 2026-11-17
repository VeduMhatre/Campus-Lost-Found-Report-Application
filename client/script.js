function toggleKept(){
	let type = document.getElementById("type").value;
	let keptWrap = document.getElementById("keptWrap");
	
	if(type === "FOUND"){
		keptWrap.hidden = false;
	}
	else{
		keptWrap.hidden = true;
	}
}

function sendReport(event){
		
	event.preventDefault();
	
	let type = document.getElementById("type")
	let name = document.getElementById("name");
	let email = document.getElementById("email");
	let itemName = document.getElementById("itemName");
	let category = document.getElementById("category");
	let place = document.getElementById("place");
	let date = document.getElementById("date");
	let keptAt = document.getElementById("keptAt");
	let description = document.getElementById("description");
	let button = document.getElementById("submitBtn");
	let msg = document.getElementById("msg");

	if(name.value.trim()==="" || email.value.trim()==="" || itemName.value.trim()==="" || date.value.trim()==="" || description.value.trim()===""){
		alert("Please Fill all required Field");
		msg.textContent="";
		name.focus();
		return;
	}

	let url = "http://localhost:8080/report";

	let formData = {
		type: type.value,
		name: name.value,
		email: email.value,
		itemName: itemName.value,
		category: category.value,
		place: place.value,
		date: date.value,
		keptAt: keptAt.value,
		description: description.value
	};
	
	let options = {
		method: "POST",
		headers: {
			"Content-Type": "application/json"
		},
		body: JSON.stringify(formData)
	};
	
	button.disabled = true;
	msg.textContent = "Sending...";


	fetch(url,options)
	.then(res => res.json())
	.then(data => {
		if(data.status === "success"){
			msg.textContent =  data.msg + " Your ID is " + data.reportId;
			document.getElementById("reportForm").reset();
			toggleKept();
		}
		else{
			msg.textContent = data.errmsg;
		}
	})
	.catch(err=>{
		msg.textContent =  "Error Connecting to Server...";
	})
	.finally(()=>{
		button.disabled = false;
	});

}











