<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>

<!DOCTYPE html>

<html lang="vi">

<head>

<meta charset="UTF-8">

<meta name="viewport" content="width=device-width, initial-scale=1">

<meta name="csrf-token" content="${_csrf.token}">

<title>Giỏ hàng - UTEExpress</title>

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


	<!-- ==========================
     NAVBAR
     ========================== -->

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


					<li class="nav-item"><a class="nav-link active" href="/cart">

							Giỏ hàng </a></li>

				</ul>


				<a class="btn btn-light btn-sm me-2" href="/login"> Đăng nhập </a> <a
					class="btn btn-outline-light btn-sm" href="/register"> Đăng ký

				</a>

			</div>

		</div>

	</nav>



	<!-- ==========================
     MAIN
     ========================== -->

	<main class="container py-5">


		<h2 class="fw-bold mb-4">

			<i class="bi bi-cart3"></i> Giỏ hàng

		</h2>


		<!-- Loading -->

		<div id="loading" class="text-center py-5">

			<div class="spinner-border text-primary" role="status"></div>

			<div class="mt-3">Đang tải giỏ hàng...</div>

		</div>


		<!-- Error -->

		<div id="error" class="alert alert-danger d-none"></div>


		<!-- Cart -->

		<div id="cartContent" class="d-none">


			<div class="row g-4">


				<!-- ==========================
			     CART ITEMS
			     ========================== -->

				<div class="col-lg-8">

					<div id="items" class="page-card p-3"></div>

				</div>



				<!-- ==========================
			     SUMMARY
			     ========================== -->

				<div class="col-lg-4">

					<div class="page-card p-4">

						<h5 class="fw-bold mb-4">Tóm tắt đơn hàng</h5>


						<div class="d-flex justify-content-between mb-3">

							<span> Tạm tính </span> <strong id="subtotal"> 0 ₫ </strong>

						</div>


						<div class="d-flex justify-content-between mb-3">

							<span> Phí vận chuyển </span> <strong> Tính khi thanh
								toán </strong>

						</div>


						<hr>


						<div class="d-flex justify-content-between mb-4">

							<span class="fw-bold"> Tổng cộng </span> <strong id="total"
								class="text-primary fs-5"> 0 ₫ </strong>

						</div>


						<button type="button" class="btn btn-primary w-100 mb-2"
							onclick="checkout()">

							<i class="bi bi-credit-card"></i> Tiến hành thanh toán

						</button>


						<a href="/product/list" class="btn btn-outline-secondary w-100">

							<i class="bi bi-arrow-left"></i> Tiếp tục mua hàng

						</a>


						<button type="button" class="btn btn-outline-danger w-100 mt-2"
							onclick="clearCart()">

							<i class="bi bi-trash"></i> Xóa toàn bộ giỏ hàng

						</button>

					</div>

				</div>

			</div>

		</div>



		<!-- ==========================
	     EMPTY CART
	     ========================== -->

		<div id="empty" class="d-none text-center py-5">

			<div class="page-card p-5">

				<i class="bi bi-cart-x" style="font-size: 64px"> </i>


				<h4 class="mt-3">Giỏ hàng đang trống</h4>


				<p class="text-muted">Hãy chọn sản phẩm và thêm vào giỏ hàng.</p>


				<a href="/product/list" class="btn btn-primary"> Xem sản phẩm </a>

			</div>

		</div>


	</main>



	<!-- ==========================
     FOOTER
     ========================== -->

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



	<!-- ==========================
     CART SCRIPT
     ========================== -->

	<script>


var currentUser = null;

var currentCart = null;


/* ==========================
   SHOW ERROR
   ========================== */

function showError(message) {

	document
		.getElementById('loading')
		.classList.add('d-none');


	document
		.getElementById('cartContent')
		.classList.add('d-none');


	var error =
		document.getElementById('error');


	error.textContent =
		message;


	error.classList.remove('d-none');

}


/* ==========================
   LOAD USER
   ========================== */

function loadUser() {

	return UTE.me()

		.then(function(user) {

			if (!user) {

				throw new Error(
					'Vui lòng đăng nhập để xem giỏ hàng.'
				);

			}


			currentUser = user;


			console.log(
				'Current user:',
				user
			);


			return user;

		});

}


/* ==========================
   LOAD CART
   ========================== */

function loadCart() {

	if (!currentUser ||
		!currentUser.id) {

		showError(
			'Không xác định được tài khoản.'
		);

		return;

	}


	var url =
		'/api/carts/user/' +
		encodeURIComponent(
			currentUser.id
		);


	console.log(
		'Loading cart:',
		url
	);


	UTE.req(url)

		.then(function(cart) {

			console.log(
				'Cart API response:',
				cart
			);


			currentCart =
				cart;


			renderCart(cart);

		})

		.catch(function(error) {

			console.error(
				'Lỗi tải giỏ hàng:',
				error
			);


			showError(
				'Không tải được giỏ hàng. ' +
				error.message
			);

		});

}


/* ==========================
   RENDER CART
   ========================== */

function renderCart(cart) {

	document
		.getElementById('loading')
		.classList.add('d-none');


	var items =
		cart.items || [];


	/* ==========================
	   EMPTY
	   ========================== */

	if (items.length === 0) {

		document
			.getElementById('cartContent')
			.classList.add('d-none');


		document
			.getElementById('empty')
			.classList.remove('d-none');


		return;

	}


	document
		.getElementById('empty')
		.classList.add('d-none');


	document
		.getElementById('cartContent')
		.classList.remove('d-none');


	var html =
		'';


	/* ==========================
	   ITEMS
	   ========================== */

	for (
		var i = 0;
		i < items.length;
		i++
	) {

		html +=
			renderItem(
				items[i]
			);

	}


	document
		.getElementById('items')
		.innerHTML =
		html;


	/* ==========================
	   TOTAL
	   ========================== */

	var total =
		cart.totalAmount || 0;


	document
		.getElementById('subtotal')
		.textContent =
		UTE.money(total);


	document
		.getElementById('total')
		.textContent =
		UTE.money(total);

}


/* ==========================
   RENDER ITEM
   ========================== */

function renderItem(item) {

	var product =
		item.product || {};


	var imageHtml;


	if (product.image) {

		imageHtml =

			'<img ' +

				'src="' +
				UTE.esc(product.image) +
				'" ' +

				'alt="' +
				UTE.esc(product.name) +
				'" ' +

				'class="rounded" ' +

				'style="' +
					'width:90px;' +
					'height:90px;' +
					'object-fit:cover;' +
				'">' ;

	} else {

		imageHtml =

			'<div ' +

				'class="d-flex ' +
				'align-items-center ' +
				'justify-content-center ' +
				'bg-light rounded" ' +

				'style="' +
					'width:90px;' +
					'height:90px;' +
				'">' +

				'<i ' +
					'class="bi bi-box-seam fs-2 text-muted">' +
				'</i>' +

			'</div>';

	}


	var quantity =
		Number(
			item.quantity || 0
		);


	var unitPrice =
		Number(
			item.unitPrice || 0
		);


	var subtotal =
		unitPrice * quantity;


	return (

		'<div ' +

			'class="border-bottom py-3" ' +

			'id="cart-item-' +
			item.id +
			'">' +


			'<div class="row align-items-center g-3">' +


				/* IMAGE */

				'<div class="col-auto">' +

					imageHtml +

				'</div>' +


				/* PRODUCT */

				'<div class="col">' +

					'<h6 class="fw-bold mb-1">' +

						UTE.esc(
							product.name ||
							'Sản phẩm'
						) +

					'</h6>' +


					'<div class="text-muted small">' +

						UTE.money(unitPrice) +

						' / sản phẩm' +

					'</div>' +

				'</div>' +


				/* QUANTITY */

				'<div class="col-md-3">' +

					'<div ' +
						'class="input-group input-group-sm">' +

						'<button ' +

							'class="btn btn-outline-secondary" ' +

							'type="button" ' +

							'onclick="changeQuantity(' +
								product.id +
								',' +
								(quantity - 1) +
							')">' +

							'<i class="bi bi-dash"></i>' +

						'</button>' +


						'<input ' +

							'type="number" ' +

							'class="form-control text-center" ' +

							'value="' +
							quantity +
							'" ' +

							'min="1" ' +

							'onchange="changeQuantity(' +
								product.id +
								', this.value)">' +

						'</input>' +


						'<button ' +

							'class="btn btn-outline-secondary" ' +

							'type="button" ' +

							'onclick="changeQuantity(' +
								product.id +
								',' +
								(quantity + 1) +
							')">' +

							'<i class="bi bi-plus"></i>' +

						'</button>' +

					'</div>' +

				'</div>' +


				/* SUBTOTAL */

				'<div class="col-md-2 text-end">' +

					'<strong>' +

						UTE.money(subtotal) +

					'</strong>' +

				'</div>' +


				/* DELETE */

				'<div class="col-auto">' +

					'<button ' +

						'type="button" ' +

						'class="btn btn-outline-danger btn-sm" ' +

						'onclick="removeItem(' +
							product.id +
						')">' +

						'<i class="bi bi-trash"></i>' +

					'</button>' +

				'</div>' +


			'</div>' +

		'</div>'

	);

}


/* ==========================
   CHANGE QUANTITY
   ========================== */

function changeQuantity(
	productId,
	quantity
) {

	quantity =
		Number(quantity);


	if (quantity <= 0) {

		removeItem(productId);

		return;

	}


	var url =
		'/api/carts/user/' +
		encodeURIComponent(
			currentUser.id
		) +
		'/items/' +
		encodeURIComponent(
			productId
		) +
		'?quantity=' +
		encodeURIComponent(
			quantity
		);


	UTE.req(
		url,
		{
			method: 'PUT'
		}
	)

		.then(function(cart) {

			currentCart =
				cart;

			renderCart(cart);

		})

		.catch(function(error) {

			alert(
				'Không thể cập nhật số lượng: ' +
				error.message
			);

			loadCart();

		});

}


/* ==========================
   REMOVE ITEM
   ========================== */

function removeItem(productId) {

	if (!confirm(
		'Bạn có chắc muốn xóa sản phẩm này khỏi giỏ hàng?'
	)) {

		return;

	}


	var url =
		'/api/carts/user/' +
		encodeURIComponent(
			currentUser.id
		) +
		'/items/' +
		encodeURIComponent(
			productId
		);


	UTE.req(
		url,
		{
			method: 'DELETE'
		}
	)

		.then(function(cart) {

			currentCart =
				cart;

			renderCart(cart);

		})

		.catch(function(error) {

			alert(
				'Không thể xóa sản phẩm: ' +
				error.message
			);

		});

}


/* ==========================
   CLEAR CART
   ========================== */

function clearCart() {

	if (!confirm(
		'Bạn có chắc muốn xóa toàn bộ giỏ hàng?'
	)) {

		return;

	}


	var url =
		'/api/carts/user/' +
		encodeURIComponent(
			currentUser.id
		) +
		'/clear';


	UTE.req(
		url,
		{
			method: 'DELETE'
		}
	)

		.then(function(cart) {

			currentCart =
				cart;

			renderCart(cart);

		})

		.catch(function(error) {

			alert(
				'Không thể xóa giỏ hàng: ' +
				error.message
			);

		});

}


/* ==========================
   CHECKOUT
   ========================== */

function checkout() {

	if (!currentCart ||
		!currentCart.items ||
		currentCart.items.length === 0) {

		alert(
			'Giỏ hàng đang trống.'
		);

		return;

	}


	window.location.href =
		'/order/checkout';

}


/* ==========================
   START
   ========================== */

loadUser()

	.then(function() {

		loadCart();

	})

	.catch(function(error) {

		showError(
			error.message
		);

	});


</script>


</body>

</html>