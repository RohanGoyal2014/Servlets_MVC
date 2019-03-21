<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Login</title>
<jsp:include page="headers.html"/>
</head>
<body>

	<c:if test="${ sessionScope.user!=null }">
		<c:redirect url="dashboard.jsp"  />	
	</c:if>

	<div id="error_box" style="display:none">
		<b>Errors:</b>
		<ul id="error_list">
		</ul>
	</div>
	<div id="loginFormContainer"><form method="post" action="accounts" id="loginForm">
		<input type="email" placeholder="Email" name="email" required><br>
		<input type="password" placeholder="Password" name="password" required>
		<input type="hidden" name="command" value="login">
		<input type="submit">
	</form>
	<br>
		<Button onclick="switchForgotPassword()">Forgot Password</Button>
	</div>
	<div id="forgotPasswordContainer" style="display:none">
	<form action="accounts" id="forgotPasswordForm" method="post">
		<input type="hidden" name="command" value="forgotPassword">
		<input type="email" name="email" placeholder="email" required>
		<input type="submit">
	</form>
	<p id="mail_sent" style="display:none">Recovery Mail Sent. Please check your email</p>
	<br>
		<Button onclick="switchLogin()">Login</Button>
	</div>

	
	<p id="loading" style="display:none"><br><b>Loading...</b></p>
	
	<script>
	
	function switchLogin() {
		document.getElementById('loginFormContainer').style.display="block";
		document.getElementById('forgotPasswordContainer').style.display="none";
	}
	
	function switchForgotPassword() {
		document.getElementById('loginFormContainer').style.display="none";
		document.getElementById('forgotPasswordContainer').style.display="block";
	}
	
	$("#loginForm").submit(function(e) {
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
	    				
	    				for(var i=0;i<json_data['errors'].length;i++) {
	    					// console.log(json_data['errors'][i]);
	    					$("#error_list").append(
	    	    					
	    						"<li>" + json_data['errors'][i] + "</li>"		
	    	    				
	    					);
	    				}
	    			} else {
	    				//Login Success
	    				document.getElementById('error_box').style.display="none";
	    				location.reload();
	    			}			        
	           }
	         });


	});
	
	$("#forgotPasswordForm").submit(function(e) {
		document.getElementById('loading').style.display="block";
	    e.preventDefault();
		
	    var form = $(this);
	    var url = form.attr('action');

		document.getElementById('mail_sent').style.display="none";

	    
	    
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
	    				
	    				for(var i=0;i<json_data['errors'].length;i++) {
	    					// console.log(json_data['errors'][i]);
	    					$("#error_list").append(
	    	    					
	    						"<li>" + json_data['errors'][i] + "</li>"		
	    	    				
	    					);
	    				}
	    			} else {
	    				document.getElementById('error_box').style.display="none";
	    				document.getElementById('mail_sent').style.display="block";
	    			}			        
	           }
	         });


	});
	
	</script>
	
	
</body>
</html>