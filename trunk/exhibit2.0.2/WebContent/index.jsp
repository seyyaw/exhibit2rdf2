<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page session="false" %>
<html>
	<head>
		<title>Home</title>
	</head>
	
	<body>
		<h1>Welcome!</h1>
		<form action="/exhibit2.0.2/Rdf2Json" method="GET">
		User name: <input type="text" name="userName" /><br />
		Password: <input type="password" name="pwd" /><br />
			<input type="submit" value="Login" />
		</form>  		
	</body>
</html>
