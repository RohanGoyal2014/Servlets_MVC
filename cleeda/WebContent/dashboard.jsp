<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Dashboard</title>
<jsp:include page="headers.html"/>
</head>
<body>
<c:if test="${ sessionScope.user==null }">
	<c:redirect url="login.jsp"  />	
</c:if>
<h1> Dashboard </h1>
<form action="accounts" method="post" id="logoutForm">
	<input type="hidden" name="command" value="logout">
	<input type="submit" value="Logout">
</form>
<p id="error" style="display:none"><br>Something went wrong...Please try again</p>
<script>
$("#logoutForm").submit(function(e) {
    e.preventDefault();
	
    var form = $(this);
    var url = form.attr('action');
	document.getElementById('error').style.display="none";
    
    
    $.ajax({
           type: "POST",
           url: url,
           data: form.serialize(),
           success: function(data)
           {
                var json_data=JSON.parse(data);
                if(json_data['success']===false) {
                	document.getElementById('error').style.display="visible";
                } else {
                	location.reload();
                }
    					        
           }
         });


});
</script>
</body>
</html>