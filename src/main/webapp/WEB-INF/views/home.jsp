<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%><!DOCTYPE html>
<html lang="vi">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width,initial-scale=1">
<meta name="csrf-token" content="${_csrf.token}">
<title>Trang chủ - UTEExpress</title>
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
<<<<<<< HEAD
				<a class="btn btn-light btn-sm me-2" href="/login">Đăng nhập</a><a
					class="btn btn-outline-light btn-sm" href="/register">Đăng ký</a>
=======
				<%
				org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder
						.getContext().getAuthentication();

				boolean loggedIn = auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal());

				String username = loggedIn ? auth.getName() : null;
				%>

				<%
				if (loggedIn) {
				%>

				<div class="dropdown">
					<button class="btn btn-light btn-sm dropdown-toggle" type="button"
						data-bs-toggle="dropdown" aria-expanded="false">
						👋 Xin chào, <strong><%=username%></strong>
					</button>

					<ul class="dropdown-menu dropdown-menu-end">
						<li><a class="dropdown-item" href="/profile"> <i
								class="bi bi-person me-2"></i> Hồ sơ
						</a></li>

						<li><a class="dropdown-item" href="/order/history"> <i
								class="bi bi-receipt me-2"></i> Đơn hàng của tôi
						</a></li>

						<li>
							<hr class="dropdown-divider">
						</li>

						<li>
							<form action="${pageContext.request.contextPath}/logout"
								method="post" class="m-0">

								<input type="hidden" name="_csrf" value="${_csrf.token}">

								<button type="submit" class="dropdown-item text-danger">
									<i class="bi bi-box-arrow-right me-2"></i> Đăng xuất
								</button>
							</form>
						</li>
					</ul>
				</div>

				<%
				} else {
				%>

				<a class="btn btn-light btn-sm me-2"
					href="${pageContext.request.contextPath}/login"> Đăng nhập </a> <a
					class="btn btn-outline-light btn-sm"
					href="${pageContext.request.contextPath}/register"> Đăng ký </a>

				<%
				}
				%>
>>>>>>> 2b622bd (Them 1 vai frontend va khoi tao lai git do xoa nham file)
			</div>
		</div>
	</nav>
	<main class="container pb-5">
		<section class="hero">
			<div class="row align-items-center">
				<div class="col-lg-7">
					<span class="badge text-bg-primary">LOGISTIC UTEEXPRESS</span>
					<h1 class="display-5 fw-bold mt-3">
						Mua sắm thuận tiện,<br>giao nhận minh bạch.
					</h1>
					<p class="lead text-secondary">Khám phá sản phẩm và quản lý đơn
						hàng trên một hệ thống duy nhất.</p>
					<a href="/product/list" class="btn btn-primary btn-lg">Khám phá
						sản phẩm</a>
				</div>
				<div class="col-lg-5 text-center">
					<i class="bi bi-truck text-primary" style="font-size: 10rem"></i>
				</div>
			</div>
		</section>
		<section class="py-5">
			<h3 class="fw-bold">Danh mục</h3>
			<div id="cats" class="d-flex flex-wrap gap-2">Đang tải...</div>
		</section>
		<section>
			<div class="d-flex justify-content-between">
				<h3 class="fw-bold">Sản phẩm mới</h3>
				<a href="/product/list">Xem tất cả</a>
			</div>
			<div id="products" class="row g-4 mt-1"></div>
		</section>
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
	<script>(async()=>{try{let c=await UTE.req('/api/categories');document.getElementById('cats').innerHTML=c.map(x=>`<a class="category-chip" href="/product/list?category=${x.id}">${UTE.esc(x.name)}</a>`).join('');let p=await UTE.req('/api/products?page=0&size=8');document.getElementById('products').innerHTML=(p.content||[]).map(card).join('')||'<div class="text-muted">Chưa có sản phẩm.</div>'}catch(e){document.getElementById('products').innerHTML='<div class="alert alert-danger">Không tải được dữ liệu.</div>'}})()</script>
</body>
</html>