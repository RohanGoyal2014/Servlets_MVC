<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Register</title>
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
	<form method="post" action="accounts" id="registerForm">
		<input type="text" name="firstName" placeholder="First Name" required><br>
		<input type="text" name="lastName" placeholder="Last Name" required><br>
		<input type="email" name="email" placeholder="Email" required><br>
		<input type="password" name="password" placeholder="Password" required><br>
		<input type="password" name="confirmPassword" placeholder="Confirm Password" required><br>
		<input type="hidden" name="command" value="register">
		<input type="submit">
	</form>
	<p id="loading"style="display:none"><br><b>Loading...</b></p>
	<p id="thanksForRegistering" style="display:none">
		Thank You for registering<br>
		<a href="login.jsp">Login</a>
	</p>
	<script>
	
	$("#registerForm").submit(function(e) {
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
	        	   $('#error_list').empty();
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
	    				document.getElementById('thanksForRegistering').style.display="block";
	    				document.getElementById('error_box').style.display="none";
	  					
	    			}			        
	           }
	         });


	});
	
	</script>
</body>
</html>