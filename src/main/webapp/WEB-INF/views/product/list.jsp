<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>

<html lang="vi">

<head>

    <meta charset="UTF-8">

    <meta name="viewport"
        content="width=device-width,initial-scale=1">

    <meta name="csrf-token"
        content="${_csrf.token}">

    <title>Sản phẩm - UTEExpress</title>

    <link
        href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
        rel="stylesheet">

    <link
        href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css"
        rel="stylesheet">

    <link
        href="${pageContext.request.contextPath}/resources/css/style.css"
        rel="stylesheet">

</head>

<body>

    <!-- ==========================
         NAVBAR
         ========================== -->

    <nav class="navbar navbar-expand-lg navbar-dark navbar-ute">

        <div class="container">

            <a class="navbar-brand fw-bold" href="/">
                🚚 UTEExpress
            </a>

            <button
                class="navbar-toggler"
                data-bs-toggle="collapse"
                data-bs-target="#nav">

                <span class="navbar-toggler-icon"></span>

            </button>

            <div id="nav"
                class="collapse navbar-collapse">

                <ul class="navbar-nav me-auto">

                    <li class="nav-item">

                        <a class="nav-link" href="/">
                            Trang chủ
                        </a>

                    </li>

                    <li class="nav-item">

                        <a class="nav-link active"
                            href="/product/list">

                            Sản phẩm

                        </a>

                    </li>

                    <li class="nav-item">

                        <a class="nav-link"
                            href="/cart">

                            Giỏ hàng

                        </a>

                    </li>

                </ul>

                <a
                    class="btn btn-light btn-sm me-2"
                    href="/login">

                    Đăng nhập

                </a>

                <a
                    class="btn btn-outline-light btn-sm"
                    href="/register">

                    Đăng ký

                </a>

            </div>

        </div>

    </nav>


    <!-- ==========================
         MAIN
         ========================== -->

    <main class="container py-4">

        <!-- SEARCH -->

        <div class="page-card p-3 mb-4">

            <div class="row g-2">

                <div class="col-md-8">

                    <input
                        id="q"
                        class="form-control"
                        placeholder="Tìm kiếm sản phẩm...">

                </div>

                <div class="col-md-2">

                    <select
                        id="size"
                        class="form-select">

                        <option value="8">
                            8
                        </option>

                        <option value="12">
                            12
                        </option>

                        <option value="20">
                            20
                        </option>

                    </select>

                </div>

                <div class="col-md-2">

                    <button
                        type="button"
                        class="btn btn-primary w-100"
                        onclick="load(0)">

                        Tìm kiếm

                    </button>

                </div>

            </div>

        </div>


        <!-- ==========================
             PRODUCT LIST
             ========================== -->

        <div
            id="products"
            class="row g-4">

        </div>


        <!-- ==========================
             PAGINATION
             ========================== -->

        <div
            id="pages"
            class="d-flex justify-content-center gap-1 my-4">

        </div>

    </main>


    <!-- ==========================
         FOOTER
         ========================== -->

    <footer class="footer">

        <div class="container">

            <b>UTEExpress</b>
            — Hệ thống quản lý chuỗi giao nhận Logistic UTEExpress.

        </div>

    </footer>


    <!-- ==========================
         BOOTSTRAP
         ========================== -->

    <script
        src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js">
    </script>


    <!-- ==========================
         APP.JS
         ========================== -->

    <script
        src="${pageContext.request.contextPath}/resources/js/app.js">
    </script>


    <!-- ==========================
         PRODUCT SCRIPT
         ========================== -->

    <script>

        function load(page) {

            var q =
                document
                    .getElementById('q')
                    .value
                    .trim();


            var params =
                new URLSearchParams(
                    window.location.search
                );


            var cat =
                params.get('category');


            var size =
                document
                    .getElementById('size')
                    .value;


            var url;


            /* ==========================
               CATEGORY
               ========================== */

            if (cat) {

                url =
                    '/api/products/category/' +
                    encodeURIComponent(cat) +
                    '?page=' +
                    page +
                    '&size=' +
                    size;

            }


            /* ==========================
               SEARCH
               ========================== */

            else if (q) {

                url =
                    '/api/products/search?keyword=' +
                    encodeURIComponent(q) +
                    '&page=' +
                    page +
                    '&size=' +
                    size;

            }


            /* ==========================
               ALL PRODUCTS
               ========================== */

            else {

                url =
                    '/api/products?page=' +
                    page +
                    '&size=' +
                    size;

            }


            console.log(
                'Loading products:',
                url
            );


            /* ==========================
               CALL API
               Không dùng async / await
               ========================== */

            UTE.req(url)

                .then(function (data) {

                    console.log(
                        'API response:',
                        data
                    );


                    /* ==========================
                       GET PRODUCT LIST
                       ========================== */

                    var products =
                        data.content || [];


                    var productHtml =
                        '';


                    /* ==========================
                       NO PRODUCT
                       ========================== */

                    if (products.length === 0) {

                        productHtml =
                            '<div class="col-12">' +

                                '<div class="alert alert-info">' +

                                    'Không có sản phẩm.' +

                                '</div>' +

                            '</div>';

                    }


                    /* ==========================
                       SHOW PRODUCTS
                       ========================== */

                    else {

                        for (
                            var i = 0;
                            i < products.length;
                            i++
                        ) {

                            productHtml +=
                                card(products[i]);

                        }

                    }


                    document
                        .getElementById('products')
                        .innerHTML =
                        productHtml;


                    /* ==========================
                       PAGINATION
                       ========================== */

                    var totalPages =
                        data.totalPages || 0;


                    var pageHtml =
                        '';


                    for (
                        var p = 0;
                        p < totalPages;
                        p++
                    ) {

                        var buttonClass =
                            p === page
                                ? 'btn-primary'
                                : 'btn-outline-primary';


                        pageHtml +=

                            '<button ' +

                                'type="button" ' +

                                'class="btn btn-sm ' +
                                buttonClass +
                                '" ' +

                                'onclick="load(' +
                                p +
                                ')">' +

                                (p + 1) +

                            '</button>';

                    }


                    document
                        .getElementById('pages')
                        .innerHTML =
                        pageHtml;

                })


                /* ==========================
                   ERROR
                   ========================== */

                .catch(function (error) {

                    console.error(
                        'Lỗi tải danh sách sản phẩm:',
                        error
                    );


                    document
                        .getElementById('products')
                        .innerHTML =

                        '<div class="col-12">' +

                            '<div class="alert alert-danger">' +

                                '<strong>' +

                                    'Không tải được sản phẩm.' +

                                '</strong>' +

                                '<br>' +

                                error.message +

                            '</div>' +

                        '</div>';

                });

        }


        /* ==========================
           LOAD TRANG ĐẦU TIÊN
           ========================== */

        load(0);

    </script>


</body>

</html>