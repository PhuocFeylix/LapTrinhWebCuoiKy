USE uteexpress;

START TRANSACTION;

-- =========================================================
-- 1. KIỂM TRA / HIỂN THỊ ROLE HIỆN TẠI
-- =========================================================

SELECT '=== ROLES ===' AS INFO;

SELECT
    id,
    name
FROM roles
ORDER BY id;


-- =========================================================
-- 2. TẠO USER VENDOR
-- =========================================================
-- Password sử dụng cùng password hash hiện tại của ADMIN.
-- Username: vendor
-- Email: vendor@uteexpress.com
--
-- Nếu vendor đã tồn tại thì không tạo thêm.
-- =========================================================

INSERT INTO users
    (
        email,
        enabled,
        full_name,
        password,
        phone,
        username,
        role_id
    )
SELECT
    'vendor@uteexpress.com',
    1,
    'UTEExpress Vendor',
    password,
    '0900000001',
    'vendor',
    r.id
FROM users u
JOIN roles r
    ON r.name = 'VENDOR'
WHERE u.id = 1
  AND NOT EXISTS (
      SELECT 1
      FROM users x
      WHERE x.username = 'vendor'
         OR x.email = 'vendor@uteexpress.com'
  )
LIMIT 1;


-- =========================================================
-- 3. KIỂM TRA USER
-- =========================================================

SELECT '=== USERS ===' AS INFO;

SELECT
    id,
    username,
    email,
    full_name,
    phone,
    enabled,
    role_id
FROM users
ORDER BY id;


-- =========================================================
-- 4. TẠO CATEGORY
-- =========================================================

INSERT INTO categories
    (
        active,
        description,
        name
    )
SELECT
    1,
    'Điện thoại, smartphone và các thiết bị di động',
    'Điện thoại'
WHERE NOT EXISTS (
    SELECT 1
    FROM categories
    WHERE name = 'Điện thoại'
);


INSERT INTO categories
    (
        active,
        description,
        name
    )
SELECT
    1,
    'Máy tính xách tay và laptop',
    'Laptop'
WHERE NOT EXISTS (
    SELECT 1
    FROM categories
    WHERE name = 'Laptop'
);


INSERT INTO categories
    (
        active,
        description,
        name
    )
SELECT
    1,
    'Phụ kiện công nghệ và thiết bị điện tử',
    'Phụ kiện'
WHERE NOT EXISTS (
    SELECT 1
    FROM categories
    WHERE name = 'Phụ kiện'
);


INSERT INTO categories
    (
        active,
        description,
        name
    )
SELECT
    1,
    'Thiết bị âm thanh như tai nghe và loa',
    'Âm thanh'
WHERE NOT EXISTS (
    SELECT 1
    FROM categories
    WHERE name = 'Âm thanh'
);


INSERT INTO categories
    (
        active,
        description,
        name
    )
SELECT
    1,
    'Thiết bị điện tử và thiết bị gia dụng',
    'Điện tử gia dụng'
WHERE NOT EXISTS (
    SELECT 1
    FROM categories
    WHERE name = 'Điện tử gia dụng'
);


-- =========================================================
-- 5. KIỂM TRA CATEGORY
-- =========================================================

SELECT '=== CATEGORIES ===' AS INFO;

SELECT
    id,
    name,
    active,
    description
FROM categories
ORDER BY id;


-- =========================================================
-- 6. TẠO SHOP
-- =========================================================
-- Shop được liên kết với user có username = vendor.
-- Nếu shop đã tồn tại thì không tạo thêm.
-- =========================================================

INSERT INTO shops
    (
        active,
        address,
        description,
        logo,
        name,
        phone,
        vendor_id
    )
SELECT
    1,
    'TP. Thủ Đức, TP. Hồ Chí Minh',
    'Cửa hàng công nghệ UTEExpress',
    'https://via.placeholder.com/300x300?text=UTE+Shop',
    'UTE Technology',
    '0900000001',
    u.id
FROM users u
JOIN roles r
    ON r.id = u.role_id
WHERE u.username = 'vendor'
  AND r.name = 'VENDOR'
  AND NOT EXISTS (
      SELECT 1
      FROM shops s
      WHERE s.name = 'UTE Technology'
  )
LIMIT 1;


-- =========================================================
-- 7. KIỂM TRA SHOP
-- =========================================================

SELECT '=== SHOPS ===' AS INFO;

SELECT
    id,
    name,
    active,
    address,
    phone,
    vendor_id
FROM shops
ORDER BY id;


-- =========================================================
-- 8. TẠO PRODUCTS
-- =========================================================
-- Không sử dụng ID cố định.
-- Tự tìm category bằng tên và shop bằng tên.
--
-- Nếu Product cùng tên + cùng Shop đã tồn tại
-- thì không insert lại.
-- =========================================================


-- ---------------------------------------------------------
-- 8.1 iPhone 15
-- ---------------------------------------------------------

INSERT INTO products
    (
        active,
        description,
        image,
        name,
        price,
        stock,
        category_id,
        shop_id
    )
SELECT
    1,
    'Điện thoại thông minh màn hình OLED, hiệu năng cao.',
    'https://via.placeholder.com/500x500?text=iPhone+15',
    'iPhone 15',
    18990000,
    20,
    c.id,
    s.id
FROM categories c
JOIN shops s
WHERE c.name = 'Điện thoại'
  AND s.name = 'UTE Technology'
  AND NOT EXISTS (
      SELECT 1
      FROM products p
      WHERE p.name = 'iPhone 15'
        AND p.shop_id = s.id
  )
LIMIT 1;


-- ---------------------------------------------------------
-- 8.2 Samsung Galaxy S24
-- ---------------------------------------------------------

INSERT INTO products
    (
        active,
        description,
        image,
        name,
        price,
        stock,
        category_id,
        shop_id
    )
SELECT
    1,
    'Điện thoại Android màn hình AMOLED, camera chất lượng cao.',
    'https://via.placeholder.com/500x500?text=Samsung+S24',
    'Samsung Galaxy S24',
    19990000,
    15,
    c.id,
    s.id
FROM categories c
JOIN shops s
WHERE c.name = 'Điện thoại'
  AND s.name = 'UTE Technology'
  AND NOT EXISTS (
      SELECT 1
      FROM products p
      WHERE p.name = 'Samsung Galaxy S24'
        AND p.shop_id = s.id
  )
LIMIT 1;


-- ---------------------------------------------------------
-- 8.3 Dell Inspiron 15
-- ---------------------------------------------------------

INSERT INTO products
    (
        active,
        description,
        image,
        name,
        price,
        stock,
        category_id,
        shop_id
    )
SELECT
    1,
    'Laptop phục vụ học tập, văn phòng và lập trình.',
    'https://via.placeholder.com/500x500?text=Dell+Inspiron',
    'Dell Inspiron 15',
    15990000,
    10,
    c.id,
    s.id
FROM categories c
JOIN shops s
WHERE c.name = 'Laptop'
  AND s.name = 'UTE Technology'
  AND NOT EXISTS (
      SELECT 1
      FROM products p
      WHERE p.name = 'Dell Inspiron 15'
        AND p.shop_id = s.id
  )
LIMIT 1;


-- ---------------------------------------------------------
-- 8.4 Lenovo LOQ
-- ---------------------------------------------------------

INSERT INTO products
    (
        active,
        description,
        image,
        name,
        price,
        stock,
        category_id,
        shop_id
    )
SELECT
    1,
    'Laptop hiệu năng cao dành cho lập trình và đồ họa.',
    'https://via.placeholder.com/500x500?text=Lenovo+LOQ',
    'Lenovo LOQ',
    22990000,
    8,
    c.id,
    s.id
FROM categories c
JOIN shops s
WHERE c.name = 'Laptop'
  AND s.name = 'UTE Technology'
  AND NOT EXISTS (
      SELECT 1
      FROM products p
      WHERE p.name = 'Lenovo LOQ'
        AND p.shop_id = s.id
  )
LIMIT 1;


-- ---------------------------------------------------------
-- 8.5 Logitech Wireless Mouse
-- ---------------------------------------------------------

INSERT INTO products
    (
        active,
        description,
        image,
        name,
        price,
        stock,
        category_id,
        shop_id
    )
SELECT
    1,
    'Chuột không dây thiết kế nhỏ gọn, phù hợp học tập và văn phòng.',
    'https://via.placeholder.com/500x500?text=Wireless+Mouse',
    'Logitech Wireless Mouse',
    450000,
    50,
    c.id,
    s.id
FROM categories c
JOIN shops s
WHERE c.name = 'Phụ kiện'
  AND s.name = 'UTE Technology'
  AND NOT EXISTS (
      SELECT 1
      FROM products p
      WHERE p.name = 'Logitech Wireless Mouse'
        AND p.shop_id = s.id
  )
LIMIT 1;


-- ---------------------------------------------------------
-- 8.6 Mechanical Keyboard
-- ---------------------------------------------------------

INSERT INTO products
    (
        active,
        description,
        image,
        name,
        price,
        stock,
        category_id,
        shop_id
    )
SELECT
    1,
    'Bàn phím cơ dành cho học tập, lập trình và chơi game.',
    'https://via.placeholder.com/500x500?text=Mechanical+Keyboard',
    'Mechanical Keyboard',
    890000,
    30,
    c.id,
    s.id
FROM categories c
JOIN shops s
WHERE c.name = 'Phụ kiện'
  AND s.name = 'UTE Technology'
  AND NOT EXISTS (
      SELECT 1
      FROM products p
      WHERE p.name = 'Mechanical Keyboard'
        AND p.shop_id = s.id
  )
LIMIT 1;


-- ---------------------------------------------------------
-- 8.7 Bluetooth Headphone
-- ---------------------------------------------------------

INSERT INTO products
    (
        active,
        description,
        image,
        name,
        price,
        stock,
        category_id,
        shop_id
    )
SELECT
    1,
    'Tai nghe Bluetooth chống ồn, phù hợp nghe nhạc và học tập.',
    'https://via.placeholder.com/500x500?text=Bluetooth+Headphone',
    'Bluetooth Headphone',
    1290000,
    25,
    c.id,
    s.id
FROM categories c
JOIN shops s
WHERE c.name = 'Âm thanh'
  AND s.name = 'UTE Technology'
  AND NOT EXISTS (
      SELECT 1
      FROM products p
      WHERE p.name = 'Bluetooth Headphone'
        AND p.shop_id = s.id
  )
LIMIT 1;


-- ---------------------------------------------------------
-- 8.8 Bluetooth Speaker
-- ---------------------------------------------------------

INSERT INTO products
    (
        active,
        description,
        image,
        name,
        price,
        stock,
        category_id,
        shop_id
    )
SELECT
    1,
    'Loa Bluetooth nhỏ gọn, phù hợp sử dụng cá nhân.',
    'https://via.placeholder.com/500x500?text=Bluetooth+Speaker',
    'Bluetooth Speaker',
    690000,
    35,
    c.id,
    s.id
FROM categories c
JOIN shops s
WHERE c.name = 'Âm thanh'
  AND s.name = 'UTE Technology'
  AND NOT EXISTS (
      SELECT 1
      FROM products p
      WHERE p.name = 'Bluetooth Speaker'
        AND p.shop_id = s.id
  )
LIMIT 1;


-- ---------------------------------------------------------
-- 8.9 Monitor 24 inch
-- ---------------------------------------------------------

INSERT INTO products
    (
        active,
        description,
        image,
        name,
        price,
        stock,
        category_id,
        shop_id
    )
SELECT
    1,
    'Màn hình máy tính 24 inch Full HD, phù hợp học tập và văn phòng.',
    'https://via.placeholder.com/500x500?text=Monitor+24',
    'Monitor 24 inch',
    3290000,
    12,
    c.id,
    s.id
FROM categories c
JOIN shops s
WHERE c.name = 'Điện tử gia dụng'
  AND s.name = 'UTE Technology'
  AND NOT EXISTS (
      SELECT 1
      FROM products p
      WHERE p.name = 'Monitor 24 inch'
        AND p.shop_id = s.id
  )
LIMIT 1;


-- ---------------------------------------------------------
-- 8.10 Webcam Full HD
-- ---------------------------------------------------------

INSERT INTO products
    (
        active,
        description,
        image,
        name,
        price,
        stock,
        category_id,
        shop_id
    )
SELECT
    1,
    'Webcam Full HD dành cho học tập, họp trực tuyến và livestream.',
    'https://via.placeholder.com/500x500?text=Webcam+Full+HD',
    'Webcam Full HD',
    790000,
    20,
    c.id,
    s.id
FROM categories c
JOIN shops s
WHERE c.name = 'Phụ kiện'
  AND s.name = 'UTE Technology'
  AND NOT EXISTS (
      SELECT 1
      FROM products p
      WHERE p.name = 'Webcam Full HD'
        AND p.shop_id = s.id
  )
LIMIT 1;


-- =========================================================
-- 9. HOÀN TẤT TRANSACTION
-- =========================================================

COMMIT;


-- =========================================================
-- 10. KIỂM TRA TỔNG QUAN
-- =========================================================

SELECT '=== KIEM TRA TONG ===' AS INFO;

SELECT
    'Roles' AS table_name,
    COUNT(*) AS total
FROM roles

UNION ALL

SELECT
    'Users',
    COUNT(*)
FROM users

UNION ALL

SELECT
    'Categories',
    COUNT(*)
FROM categories

UNION ALL

SELECT
    'Shops',
    COUNT(*)
FROM shops

UNION ALL

SELECT
    'Products',
    COUNT(*)
FROM products;


-- =========================================================
-- 11. KIỂM TRA CATEGORY
-- =========================================================

SELECT '=== CATEGORY ===' AS INFO;

SELECT
    id,
    name,
    active
FROM categories
ORDER BY id;


-- =========================================================
-- 12. KIỂM TRA SHOP
-- =========================================================

SELECT '=== SHOP ===' AS INFO;

SELECT
    s.id,
    s.name,
    s.active,
    s.vendor_id,
    u.username AS vendor_username
FROM shops s
LEFT JOIN users u
    ON u.id = s.vendor_id
ORDER BY s.id;


-- =========================================================
-- 13. KIỂM TRA PRODUCT
-- =========================================================

SELECT '=== PRODUCTS ===' AS INFO;

SELECT
    p.id,
    p.name,
    p.price,
    p.stock,
    p.active,
    c.name AS category,
    s.name AS shop
FROM products p
LEFT JOIN categories c
    ON c.id = p.category_id
LEFT JOIN shops s
    ON s.id = p.shop_id
ORDER BY p.id;


-- =========================================================
-- 14. KIỂM TRA PRODUCT PUBLIC
-- Chỉ lấy product active
-- =========================================================

SELECT '=== ACTIVE PRODUCTS ===' AS INFO;

SELECT
    p.id,
    p.name,
    p.price,
    p.stock,
    c.name AS category,
    s.name AS shop
FROM products p
LEFT JOIN categories c
    ON c.id = p.category_id
LEFT JOIN shops s
    ON s.id = p.shop_id
WHERE p.active = 1
ORDER BY p.id;