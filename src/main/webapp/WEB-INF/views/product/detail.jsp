<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>

<!DOCTYPE html>

<html lang="vi">

<head>

<meta charset="UTF-8">

<meta name="viewport" content="width=device-width, initial-scale=1">

<meta name="csrf-token" content="${_csrf.token}">

<title>Chi tiết sản phẩm - UTEExpress</title>

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


					<li class="nav-item"><a class="nav-link" href="/cart"> Giỏ
							hàng </a></li>

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

		<!-- Loading -->

		<div id="loading" class="text-center py-5">

			<div class="spinner-border text-primary" role="status"></div>

			<div class="mt-3">Đang tải sản phẩm...</div>

		</div>


		<!-- Error -->

		<div id="error" class="alert alert-danger d-none"></div>


		<!-- Product -->

		<div id="product" class="d-none">

			<div class="row g-5">


				<!-- ==========================
                     PRODUCT IMAGE
                     ========================== -->

				<div class="col-lg-6">

					<div id="productImage"></div>

				</div>


				<!-- ==========================
                     PRODUCT INFORMATION
                     ========================== -->

				<div class="col-lg-6">

					<div class="mb-2">

						<span id="categoryName" class="badge text-bg-primary"> </span>

					</div>


					<h1 id="productName" class="fw-bold mb-3"></h1>


					<div id="productPrice" class="display-6 fw-bold text-primary mb-4">

					</div>


					<div class="mb-4">

						<div class="mb-2">

							<strong> Cửa hàng: </strong> <span id="shopName"> </span>

						</div>


						<div class="mb-2">

							<strong> Tồn kho: </strong> <span id="stock"> </span>

						</div>


						<div class="mb-2">

							<strong> Trạng thái: </strong> <span id="stockStatus"> </span>

						</div>

					</div>


					<hr>


					<!-- Quantity -->

					<div class="row align-items-end g-3 mb-4">

						<div class="col-sm-4">

							<label for="quantity" class="form-label fw-bold"> Số
								lượng </label> <input id="quantity" type="number" class="form-control"
								min="1" value="1">

						</div>


						<div class="col-sm-8">

							<button id="addCartBtn" type="button"
								class="btn btn-primary btn-lg w-100" onclick="addToCart()">

								<i class="bi bi-cart-plus"></i> Thêm vào giỏ hàng

							</button>

						</div>

					</div>


					<!-- Description -->

					<div class="mt-4">

						<h5 class="fw-bold">Mô tả sản phẩm</h5>

						<p id="description" class="text-muted"></p>

					</div>


					<!-- Back -->

					<div class="mt-4">

						<a href="/product/list" class="btn btn-outline-secondary"> <i
							class="bi bi-arrow-left"></i> Quay lại sản phẩm

						</a>

					</div>

				</div>

			</div>

		</div>

	</main>


	<!-- ==========================
         BOOTSTRAP
         ========================== -->

	<script
		src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js">
    </script>


	<!-- ==========================
         APP.JS
         ========================== -->

	<script src="${pageContext.request.contextPath}/resources/js/app.js">
    </script>


	<!-- ==========================
         DETAIL SCRIPT
         ========================== -->

	<script>

        var product = null;


        /* ==========================
           GET PRODUCT ID
           ========================== */

        function getProductId() {

            var params =
                new URLSearchParams(
                    window.location.search
                );

            return params.get('id');

        }


        /* ==========================
           SHOW ERROR
           ========================== */

        function showError(message) {

            document
                .getElementById('loading')
                .classList.add('d-none');


            document
                .getElementById('product')
                .classList.add('d-none');


            var error =
                document.getElementById('error');


            error.textContent =
                message;


            error.classList.remove('d-none');

        }


        /* ==========================
           LOAD PRODUCT
           ========================== */

        function loadProduct() {

            var id =
                getProductId();


            if (!id) {

                showError(
                    'Không tìm thấy ID sản phẩm.'
                );

                return;

            }


            var url =
                '/api/products/' +
                encodeURIComponent(id);


            console.log(
                'Loading product:',
                url
            );


            UTE.req(url)

                .then(function (data) {

                    console.log(
                        'Product API response:',
                        data
                    );


                    product = data;


                    renderProduct(data);

                })

                .catch(function (error) {

                    console.error(
                        'Lỗi tải sản phẩm:',
                        error
                    );


                    showError(
                        'Không tải được sản phẩm. ' +
                        error.message
                    );

                });

        }


        /* ==========================
           RENDER PRODUCT
           ========================== */

        function renderProduct(p) {

            if (!p) {

                showError(
                    'Dữ liệu sản phẩm không hợp lệ.'
                );

                return;

            }


            /* ==========================
               IMAGE
               ========================== */

            var imageContainer =
                document.getElementById(
                    'productImage'
                );


            if (p.image) {

                imageContainer.innerHTML =
                    '<img src="' +
                    UTE.esc(p.image) +
                    '" ' +
                    'class="w-100 rounded-4" ' +
                    'style="max-height:500px;object-fit:cover" ' +
                    'alt="' +
                    UTE.esc(p.name) +
                    '">';

            } else {

                imageContainer.innerHTML =
                    '<div ' +
                    'class="product-placeholder rounded-4" ' +
                    'style="height:500px">' +

                        '<i class="bi bi-box-seam"></i>' +

                    '</div>';

            }


            /* ==========================
               NAME
               ========================== */

            document
                .getElementById('productName')
                .textContent =
                p.name || '';


            /* ==========================
               PRICE
               ========================== */

            document
                .getElementById('productPrice')
                .textContent =
                UTE.money(p.price);


            /* ==========================
               CATEGORY
               ========================== */

            var categoryName =
                'Chưa phân loại';


            if (p.category &&
                p.category.name) {

                categoryName =
                    p.category.name;

            }


            document
                .getElementById('categoryName')
                .textContent =
                categoryName;


            /* ==========================
               SHOP
               ========================== */

            var shopName =
                'UTEExpress';


            if (p.shop &&
                p.shop.name) {

                shopName =
                    p.shop.name;

            }


            document
                .getElementById('shopName')
                .textContent =
                shopName;


            /* ==========================
               STOCK
               ========================== */

            var stock =
                Number(p.stock || 0);


            document
                .getElementById('stock')
                .textContent =
                stock;


            /* ==========================
               STOCK STATUS
               ========================== */

            var stockStatus =
                document.getElementById(
                    'stockStatus'
                );


            var addCartBtn =
                document.getElementById(
                    'addCartBtn'
                );


            if (!p.active) {

                stockStatus.textContent =
                    'Ngừng kinh doanh';

                stockStatus.className =
                    'badge text-bg-secondary';

                addCartBtn.disabled =
                    true;

            }

            else if (stock <= 0) {

                stockStatus.textContent =
                    'Hết hàng';

                stockStatus.className =
                    'badge text-bg-danger';

                addCartBtn.disabled =
                    true;

            }

            else {

                stockStatus.textContent =
                    'Còn hàng';

                stockStatus.className =
                    'badge text-bg-success';

                addCartBtn.disabled =
                    false;

            }


            /* ==========================
               DESCRIPTION
               ========================== */

            document
                .getElementById('description')
                .textContent =
                p.description ||
                'Chưa có mô tả sản phẩm.';


            /* ==========================
               QUANTITY MAX
               ========================== */

            var quantity =
                document.getElementById(
                    'quantity'
                );


            quantity.max =
                stock;


            if (stock <= 0) {

                quantity.value =
                    1;

            }


            /* ==========================
               SHOW PRODUCT
               ========================== */

            document
                .getElementById('loading')
                .classList.add('d-none');


            document
                .getElementById('product')
                .classList.remove('d-none');

        }


        /* ==========================
           ADD TO CART
           ========================== */

           function addToCart() {

        	    if (!product || !product.id) {
        	        alert('Không tìm thấy sản phẩm.');
        	        return;
        	    }

        	    var quantityInput = document.getElementById('quantity');
        	    var quantity = parseInt(quantityInput.value, 10);

        	    if (isNaN(quantity) || quantity <= 0) {
        	        alert('Số lượng không hợp lệ.');
        	        return;
        	    }

        	    var stock = Number(product.stock || 0);

        	    if (quantity > stock) {
        	        alert('Số lượng vượt quá tồn kho.');
        	        return;
        	    }

        	    UTE.me()
        	        .then(function (user) {

        	            if (!user) {
        	                alert('Vui lòng đăng nhập để thêm sản phẩm vào giỏ hàng.');
        	                window.location.href = '/login';
        	                return null;
        	            }

        	            var url =
        	                '/api/carts/user/' +
        	                encodeURIComponent(user.id) +
        	                '/items?productId=' +
        	                encodeURIComponent(product.id) +
        	                '&quantity=' +
        	                encodeURIComponent(quantity);

        	            console.log('Add to cart:', url);

        	            return UTE.req(url, {
        	                method: 'POST'
        	            });
        	        })
        	        .then(function (cart) {

        	            if (!cart) {
        	                return;
        	            }

        	            console.log('Cart API response:', cart);

        	            alert('Đã thêm sản phẩm vào giỏ hàng.');

        	            window.location.href = '/cart';
        	        })
        	        .catch(function (error) {

        	            console.error('Lỗi thêm vào giỏ hàng:', error);

        	            alert(
        	                'Không thể thêm sản phẩm vào giỏ hàng.\n' +
        	                error.message
        	            );
        	        });
        	}

        /* ==========================
           START
           ========================== */

        loadProduct();

    </script>

</body>

</html>