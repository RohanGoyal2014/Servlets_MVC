<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>ResetPassword</title>
<jsp:include page="headers.html"/>
</head>
<body>
	<div id="error_box" style="display:none">
		<b>Errors:</b>
		<ul id="error_list">
		</ul>
	</div>
	<form action="accounts" method="post" id="passwordSetForm">
		<input type="hidden" name="command" value="setNewPassword">
		<input type="password" placeholder="Password" name="password" required>
		<input type="password" placeholder="Confirm Password" name="confirmPassword" required> 
		<input type="submit">
	</form>
	<p id="loading" style="display:none"><br><b>Loading...</b></p>
	<script>
	$("#passwordSetForm").submit(function(e) {
		document.getElementById('loading').style.display="block";
	    e.preventDefault();
		
	    var form = $(this);
	    var url = form.attr('action');

	    
	    
	    $.ajax({
	           type: "POST",
	           url: url,
	           data: form.serialize(),
	           success: function(data)
	           {
	        	   document.getElementById('loading').style.display="none";
	        	   $('#error_list').empty()
	                var json_data=JSON.parse(data);
	    			if(json_data['success']===false) {
	    				document.getElementById('error_box').style.display="block";
	    				$("#error_list").append(
    	    					
	    						"<li>" + json_data['error'] + "</li>"		
	    	    				
	    				);
	    			} else {
	    				document.getElementById('error_box').style.display="none";
	    				document.getElementById('passwordSetForm').style.display="none";
	    				location.href="accounts";
	    			}			        
	           }
	         });


	});
	</script>
</body>
</html>