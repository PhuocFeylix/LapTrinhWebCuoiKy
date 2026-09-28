<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="vi">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">

<meta name="csrf-token" content="${_csrf.token}">

<title>Hồ sơ - UTEExpress</title>

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

			<a class="navbar-brand fw-bold" href="/"> 🚚 UTEExpress </a>

			<button class="navbar-toggler" type="button"
				data-bs-toggle="collapse" data-bs-target="#nav">

				<span class="navbar-toggler-icon"></span>

			</button>

			<div id="nav" class="collapse navbar-collapse">

				<ul class="navbar-nav me-auto">

					<li class="nav-item"><a class="nav-link" href="/"> Trang
							chủ </a></li>

					<li class="nav-item"><a class="nav-link" href="/product/list">
							Sản phẩm </a></li>

					<li class="nav-item"><a class="nav-link" href="/cart"> Giỏ
							hàng </a></li>

					<li class="nav-item"><a class="nav-link active"
						href="/profile"> Hồ sơ </a></li>

					<li class="nav-item"><a class="nav-link" href="/order/history">
							Đơn hàng </a></li>

				</ul>

				<form action="${pageContext.request.contextPath}/logout"
					method="post" class="d-inline">

					<input type="hidden" name="_csrf" value="${_csrf.token}">

					<button type="submit" class="btn btn-outline-light btn-sm">

						Đăng xuất</button>

				</form>

			</div>

		</div>
	</nav>


	<main class="container py-5">

		<div class="row justify-content-center">

			<div class="col-lg-8">

				<div class="page-card p-4">

					<div class="d-flex align-items-center mb-4">

						<div
							class="rounded-circle bg-primary text-white d-flex align-items-center justify-content-center me-3"
							style="width: 70px; height: 70px; font-size: 30px;">

							<i class="bi bi-person"></i>

						</div>

						<div>

							<h2 class="fw-bold mb-1">Hồ sơ tài khoản</h2>

							<p class="text-muted mb-0">Thông tin tài khoản UTEExpress</p>

						</div>

					</div>


					<div id="loading">

						<div class="text-center py-4">

							<div class="spinner-border text-primary" role="status"></div>

							<div class="mt-2 text-muted">Đang tải thông tin tài
								khoản...</div>

						</div>

					</div>


					<div id="error" style="display: none;">

						<div class="alert alert-danger">Không thể tải thông tin tài
							khoản.</div>

					</div>


					<div id="profileData" style="display: none;">

						<div class="row g-3">

							<div class="col-md-6">

								<label class="form-label text-muted"> Tên đăng nhập </label>

								<div class="form-control bg-light" id="username"></div>

							</div>


							<div class="col-md-6">

								<label class="form-label text-muted"> Họ và tên </label>

								<div class="form-control bg-light" id="fullName"></div>

							</div>


							<div class="col-md-6">

								<label class="form-label text-muted"> Email </label>

								<div class="form-control bg-light" id="email"></div>

							</div>


							<div class="col-md-6">

								<label class="form-label text-muted"> Số điện thoại </label>

								<div class="form-control bg-light" id="phone"></div>

							</div>


							<div class="col-md-6">

								<label class="form-label text-muted"> Vai trò </label>

								<div>

									<span class="badge text-bg-primary fs-6" id="role"> </span>

								</div>

							</div>


							<div class="col-md-6">

								<label class="form-label text-muted"> Trạng thái tài
									khoản </label>

								<div>

									<span class="badge text-bg-success fs-6" id="enabled"> </span>

								</div>

							</div>

						</div>


						<hr class="my-4">


						<div class="d-flex gap-2 flex-wrap">

							<a href="/order/history" class="btn btn-primary"> <i
								class="bi bi-box-seam me-1"></i> Xem đơn hàng

							</a> <a href="/" class="btn btn-outline-secondary"> <i
								class="bi bi-house me-1"></i> Về trang chủ

							</a>

						</div>

					</div>

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
		src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js">
</script>

	<script src="${pageContext.request.contextPath}/resources/js/app.js">
</script>


	<script>

document.addEventListener('DOMContentLoaded', function () {

    loadProfile();

});


function loadProfile() {

    UTE.req('/api/me')

        .then(function (user) {

            console.log('USER FROM API:', user);

            if (!user) {

                showError();

                return;

            }

            document.getElementById('username').textContent =
                user.username || 'Chưa cập nhật';

            document.getElementById('fullName').textContent =
                user.fullName || 'Chưa cập nhật';

            document.getElementById('email').textContent =
                user.email || 'Chưa cập nhật';

            document.getElementById('phone').textContent =
                user.phone || 'Chưa cập nhật';


            var roleName = 'USER';

            if (user.role && user.role.name) {

                roleName = user.role.name;

            }

            document.getElementById('role').textContent =
                roleName;


            document.getElementById('enabled').textContent =
                user.enabled ? 'Đang hoạt động' : 'Đã khóa';


            document.getElementById('loading').style.display =
                'none';

            document.getElementById('profileData').style.display =
                'block';

        })

        .catch(function (error) {

            console.error('PROFILE ERROR:', error);

            showError();

        });

}


function showError() {

    document.getElementById('loading').style.display =
        'none';

    document.getElementById('error').style.display =
        'block';

}

</script>

</body>
</html>