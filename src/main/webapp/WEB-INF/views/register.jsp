<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%><!DOCTYPE html>
<html lang="vi">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width,initial-scale=1">
<meta name="csrf-token" content="${_csrf.token}">
<title>Đăng ký - UTEExpress</title>
<link
	href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
	rel="stylesheet">
<link
	href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css"
	rel="stylesheet">
<link href="${pageContext.request.contextPath}/resources/css/style.css"
	rel="stylesheet">
</head>
<body>
	<nav class="navbar navbar-expand-lg navbar-dark navbar-ute">
		<div class="container">
			<a class="navbar-brand fw-bold" href="/">🚚 UTEExpress</a>
			<button class="navbar-toggler" data-bs-toggle="collapse"
				data-bs-target="#nav">
				<span class="navbar-toggler-icon"></span>
			</button>
			<div id="nav" class="collapse navbar-collapse">
				<ul class="navbar-nav me-auto">
					<li class="nav-item"><a class="nav-link" href="/">Trang
							chủ</a></li>
					<li class="nav-item"><a class="nav-link" href="/product/list">Sản
							phẩm</a></li>
					<li class="nav-item"><a class="nav-link" href="/cart">Giỏ
							hàng</a></li>
				</ul>
				<a class="btn btn-light btn-sm me-2" href="/login">Đăng nhập</a><a
					class="btn btn-outline-light btn-sm" href="/register">Đăng ký</a>
			</div>
		</div>
	</nav>
	<main class="container py-5">
		<div class="row justify-content-center">
			<div class="col-lg-6">
				<div class="page-card p-4">
					<h2 class="fw-bold text-center">Tạo tài khoản</h2>
					<div class="alert alert-info">Giao diện đã sẵn sàng. Backend
						hiện chưa có API đăng ký + OTP.</div>
					<div class="row g-3">
						<div class="col-md-6">
							<label>Username</label><input class="form-control">
						</div>
						<div class="col-md-6">
							<label>Email</label><input class="form-control">
						</div>
						<div class="col-md-6">
							<label>Họ và tên</label><input class="form-control">
						</div>
						<div class="col-md-6">
							<label>Số điện thoại</label><input class="form-control">
						</div>
						<div class="col-md-6">
							<label>Mật khẩu</label><input type="password"
								class="form-control">
						</div>
						<div class="col-md-6">
							<label>Xác nhận</label><input type="password"
								class="form-control">
						</div>
					</div>
					<button class="btn btn-primary w-100 mt-4"
						onclick="alert('API đăng ký + OTP chưa có trong backend')">Đăng
						ký</button>
				</div>
			</div>
		</div>
	</main>
	<footer class="footer">
		<div class="container">
			<b>UTEExpress</b> — Hệ thống quản lý chuỗi giao nhận Logistic
			UTEExpress.
		</div>
	</footer>
	<script
		src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
	<script src="${pageContext.request.contextPath}/resources/js/app.js"></script>
</body>
</html>