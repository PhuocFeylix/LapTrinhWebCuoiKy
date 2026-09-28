var UTE = {

    csrf: function () {
        var meta = document.querySelector('meta[name="csrf-token"]');
        return meta ? (meta.content || '') : '';
    },

    req: function (url, opt) {
        opt = opt || {};

        var headers = opt.headers || {};
        var method = (opt.method || 'GET').toUpperCase();
        var token = this.csrf();

        if (token && method !== 'GET') {
            headers['X-CSRF-TOKEN'] = token;
        }

        opt.headers = headers;

        if (opt.body && typeof opt.body !== 'string') {
            opt.headers['Content-Type'] = 'application/json';
            opt.body = JSON.stringify(opt.body);
        }

        return fetch(url, opt)
            .then(function (response) {
                if (!response.ok) {
                    throw new Error('HTTP ' + response.status);
                }

                if (response.status === 204) {
                    return null;
                }

                var contentType =
                    response.headers.get('content-type') || '';

                if (contentType.indexOf('json') !== -1) {
                    return response.json();
                }

                return response.text();
            });
    },

    money: function (value) {
        return new Intl.NumberFormat('vi-VN', {
            style: 'currency',
            currency: 'VND'
        }).format(Number(value || 0));
    },

    esc: function (value) {
        var text = value === null || value === undefined
            ? ''
            : String(value);

        return text.replace(/[&<>"']/g, function (character) {
            var map = {
                '&': '&amp;',
                '<': '&lt;',
                '>': '&gt;',
                '"': '&quot;',
                "'": '&#039;'
            };

            return map[character];
        });
    },

    me: function () {
        return this.req('/api/me')
            .catch(function () {
                return null;
            });
    }
};


function card(product) {

    product = product || {};

    var imageHtml;

    if (product.image) {
        imageHtml =
            '<img src="' + UTE.esc(product.image) + '"' +
            ' class="product-img w-100"' +
            ' alt="' + UTE.esc(product.name) + '">';
    } else {
        imageHtml =
            '<div class="product-placeholder">' +
            '<i class="bi bi-box-seam"></i>' +
            '</div>';
    }

    var shopName = 'UTEExpress';

    if (product.shop && product.shop.name) {
        shopName = product.shop.name;
    }

    return (
        '<div class="col-12 col-sm-6 col-lg-3">' +
            '<div class="card-product">' +

                imageHtml +

                '<div class="p-3">' +

                    '<div class="small text-muted">' +
                        UTE.esc(shopName) +
                    '</div>' +

                    '<h6 class="fw-bold mt-1">' +
                        UTE.esc(product.name) +
                    '</h6>' +

                    '<div class="price mb-3">' +
                        UTE.money(product.price) +
                    '</div>' +

                    '<a href="/product/detail?id=' +
                        encodeURIComponent(product.id) +
                        '" class="btn btn-outline-primary btn-sm w-100">' +
                        'Xem sản phẩm' +
                    '</a>' +

                '</div>' +
            '</div>' +
        '</div>'
    );
}
