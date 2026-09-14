<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html lang="vi">

<head>

<meta charset="UTF-8">

<title>Đăng nhập - UTEExpress</title>

<link
	href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
	rel="stylesheet">

</head>

<body class="bg-light">

	<div class="container">

		<div class="row justify-content-center mt-5">

			<div class="col-md-5">

				<div class="card shadow">

					<div class="card-body p-4">

						<h2 class="text-center mb-3">UTEExpress</h2>

						<h4 class="text-center mb-4">Đăng nhập</h4>


						<%-- Thông báo đăng nhập thất bại --%>
						<%
						if ("true".equals(request.getParameter("error"))) {
						%>

						<div class="alert alert-danger">Sai tài khoản hoặc mật khẩu!
						</div>

						<%
						}
						%>


						<%-- Thông báo đăng xuất --%>
						<%
						if ("true".equals(request.getParameter("logout"))) {
						%>

						<div class="alert alert-success">Bạn đã đăng xuất thành
							công.</div>

						<%
						}
						%>


						<!-- Form đăng nhập -->

						<form action="${pageContext.request.contextPath}/login"
							method="post">
							<input type="hidden" name="${_csrf.parameterName}"
								value="${_csrf.token}">
							<div class="mb-3">

								<label class="form-label"> Tên đăng nhập </label> <input
									type="text" name="username" class="form-control"
									placeholder="Nhập username" required>

							</div>


							<div class="mb-3">

								<label class="form-label"> Mật khẩu </label> <input
									type="password" name="password" class="form-control"
									placeholder="Nhập mật khẩu" required>

							</div>


							<button type="submit" class="btn btn-primary w-100">

								Đăng nhập</button>

						</form>

					</div>

				</div>

			</div>

		</div>

	</div>

</body>

</html>