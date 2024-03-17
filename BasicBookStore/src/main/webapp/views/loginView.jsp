<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>loginView</title>
</head>
<body>
	<jsp:include page="_header.jsp"></jsp:include>
	<jsp:include page="_menu.jsp"></jsp:include>
	<div align="center">
		<p style="color: red;">${errors}</p>
		<form action="login" method="post">
			<label for="username"><b>Tài khoản</b></label><br> <input type="text"
				name="username" id="username" value="${loginForm.username }">
			<br>
			<br> <label for="password"><b>Mật khẩu</b></label><br> 
			<input type="password" name="password" id="password" value="${loginForm.password }">
			<br><br>
				<label for="rememberMe">Ghi nhớ</label>
				<input type="checkbox" name="rememberMe" value="Y" ${loginForm.rememberMe} />
				 <br> <br>
				  <input type="submit" value="Đăng nhập"> <a
				href="${pageContext.request.contextPath}/">Bỏ qua</a>
		</form>
	</div>
	<jsp:include page="_footer.jsp"></jsp:include>
</body>
</html>