<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%><!DOCTYPE html>
<html lang="vi">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width,initial-scale=1">
<meta name="csrf-token" content="${_csrf.token}">
<title>Lịch sử đơn hàng - UTEExpress</title>
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
		<div class="page-card p-4">
			<h2 class="fw-bold">Lịch sử đơn hàng</h2>
			<p class="text-muted">Các đơn hàng của bạn.</p>
			<div id="content">Đang tải...</div>
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
	<script>(async()=>{let u=await UTE.me();document.getElementById('content').innerHTML=u?`<div class="alert alert-success">Xin chào <b>${UTE.esc(u.fullName||u.username)}</b>. API frontend đang sẵn sàng để nối dữ liệu.</div>`:'<div class="alert alert-warning">Vui lòng đăng nhập.</div>'})()</script>
</body>
</html>