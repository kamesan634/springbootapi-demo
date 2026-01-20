-- =============================================================================
-- 零售業簡易ERP系統 - 重置種子資料腳本
-- =============================================================================
-- 說明：此腳本用於重置測試資料，可重複執行
-- 執行方式：
--   docker exec -i erp-mysql mysql -uroot -pdev123 springbootdemo_db < scripts/reset-seed-data.sql
--   或在本地：mysql -h localhost -P 3305 -uroot -pdev123 springbootdemo_db < scripts/reset-seed-data.sql
-- =============================================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =============================================================================
-- 1. 清除現有資料（按依賴順序反向刪除）
-- =============================================================================

-- 銷售相關
TRUNCATE TABLE payments;
TRUNCATE TABLE order_items;
TRUNCATE TABLE orders;

-- 庫存相關
TRUNCATE TABLE inventory_movements;
TRUNCATE TABLE inventories;

-- 促銷相關
TRUNCATE TABLE coupons;
TRUNCATE TABLE promotions;

-- 採購相關
TRUNCATE TABLE suppliers;

-- 客戶相關
TRUNCATE TABLE customers;
TRUNCATE TABLE customer_levels;

-- 商品相關
TRUNCATE TABLE product_barcodes;
TRUNCATE TABLE products;
TRUNCATE TABLE tax_types;
TRUNCATE TABLE units;
TRUNCATE TABLE categories;

-- 帳號相關
TRUNCATE TABLE user_stores;
TRUNCATE TABLE users;
TRUNCATE TABLE stores;
TRUNCATE TABLE roles;

SET FOREIGN_KEY_CHECKS = 1;

-- =============================================================================
-- 2. 重新載入種子資料
-- =============================================================================

-- 載入 01-seed-data.sql 的內容
SOURCE /docker-entrypoint-initdb.d/01-seed-data.sql;

-- 或者如果不是在 Docker 中執行，請直接執行以下內容：

-- =============================================================================
-- 角色資料
-- =============================================================================
INSERT INTO roles (id, code, name, description, is_active, created_at, created_by) VALUES
(1, 'ADMIN', '系統管理員', '擁有系統所有權限的管理員角色', true, NOW(), 1),
(2, 'MANAGER', '門市店長', '門市管理權限，可管理門市營運', true, NOW(), 1),
(3, 'CASHIER', '收銀員', '收銀台操作權限，處理銷售交易', true, NOW(), 1),
(4, 'WAREHOUSE', '倉管人員', '倉庫管理權限，處理進出貨作業', true, NOW(), 1),
(5, 'VIEWER', '檢視者', '唯讀權限，僅能查看資料', true, NOW(), 1);

-- =============================================================================
-- 門市資料
-- =============================================================================
INSERT INTO stores (id, code, name, address, phone, type, is_active, created_at, created_by) VALUES
(1, 'HQ', '總公司', '台北市信義區信義路五段7號', '02-2345-6789', 'WAREHOUSE', true, NOW(), 1),
(2, 'S001', '台北旗艦店', '台北市大安區忠孝東路四段123號', '02-2771-1234', 'STORE', true, NOW(), 1),
(3, 'S002', '新北板橋店', '新北市板橋區文化路一段88號', '02-2965-5678', 'STORE', true, NOW(), 1),
(4, 'S003', '台中逢甲店', '台中市西屯區文華路99號', '04-2452-3456', 'STORE', true, NOW(), 1),
(5, 'W001', '北區物流中心', '新北市五股區五權路100號', '02-2291-7890', 'WAREHOUSE', true, NOW(), 1),
(6, 'W002', '中區物流中心', '台中市大雅區中清路200號', '04-2567-8901', 'WAREHOUSE', true, NOW(), 1);

-- =============================================================================
-- 使用者資料（密碼: password123）
-- =============================================================================
INSERT INTO users (id, username, password, name, email, phone, role_id, is_active, created_at, created_by) VALUES
(1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '系統管理員', 'admin@erp.demo.com', '0912-345-678', 1, true, NOW(), 1),
(2, 'manager01', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '王大明', 'manager01@erp.demo.com', '0923-456-789', 2, true, NOW(), 1),
(3, 'cashier01', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '李小華', 'cashier01@erp.demo.com', '0934-567-890', 3, true, NOW(), 1),
(4, 'cashier02', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '張美玲', 'cashier02@erp.demo.com', '0945-678-901', 3, true, NOW(), 1),
(5, 'warehouse01', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '陳建志', 'warehouse01@erp.demo.com', '0956-789-012', 4, true, NOW(), 1);

-- =============================================================================
-- 使用者門市關聯
-- =============================================================================
INSERT INTO user_stores (user_id, store_id) VALUES
(1, 1), (2, 2), (3, 2), (4, 3), (5, 5), (5, 6);

-- =============================================================================
-- 分類資料
-- =============================================================================
INSERT INTO categories (id, code, name, description, parent_id, sort_order, is_active, created_at, created_by) VALUES
(1, 'ELEC', '3C電子', '電子產品、3C周邊', NULL, 1, true, NOW(), 1),
(2, 'ELEC-PHONE', '手機配件', '手機殼、充電器、傳輸線等', 1, 1, true, NOW(), 1),
(3, 'ELEC-COMP', '電腦周邊', '鍵盤、滑鼠、USB設備等', 1, 2, true, NOW(), 1),
(4, 'FOOD', '食品飲料', '各類食品與飲料', NULL, 2, true, NOW(), 1),
(5, 'FOOD-SNACK', '零食餅乾', '各類零食、餅乾、糖果', 4, 1, true, NOW(), 1),
(6, 'FOOD-DRINK', '飲料', '各類飲料、礦泉水、果汁', 4, 2, true, NOW(), 1),
(7, 'HOME', '居家生活', '居家用品、清潔用品', NULL, 3, true, NOW(), 1),
(8, 'BEAUTY', '美妝保養', '化妝品、保養品、個人護理', NULL, 4, true, NOW(), 1);

-- =============================================================================
-- 單位資料
-- =============================================================================
INSERT INTO units (id, code, name, description, is_active, created_at, created_by) VALUES
(1, 'PCS', '個', '以個數計算', true, NOW(), 1),
(2, 'BOX', '盒', '以盒計算', true, NOW(), 1),
(3, 'PKG', '包', '以包計算', true, NOW(), 1),
(4, 'BTL', '瓶', '以瓶計算', true, NOW(), 1),
(5, 'CAN', '罐', '以罐計算', true, NOW(), 1),
(6, 'SET', '組', '以組計算', true, NOW(), 1),
(7, 'KG', '公斤', '以公斤計算', true, NOW(), 1);

-- =============================================================================
-- 稅別資料
-- =============================================================================
INSERT INTO tax_types (id, code, name, rate, description, is_active, created_at, created_by) VALUES
(1, 'TAX5', '應稅5%', 0.05, '一般商品營業稅', true, NOW(), 1),
(2, 'TAX0', '零稅率', 0.00, '外銷商品零稅率', true, NOW(), 1),
(3, 'EXEMPT', '免稅', 0.00, '免稅商品', true, NOW(), 1);

-- =============================================================================
-- 商品資料
-- =============================================================================
INSERT INTO products (id, sku, name, description, category_id, unit_id, tax_type_id, cost_price, selling_price, barcode, safety_stock, is_active, created_at, created_by) VALUES
(1, 'PHN-CASE-001', 'iPhone 15 透明保護殼', '高透明TPU材質，防摔防刮', 2, 1, 1, 80.00, 199.00, '4710001000011', 50, true, NOW(), 1),
(2, 'PHN-CHG-001', 'USB-C 快充充電器 20W', '支援PD快充，適用iPhone/Android', 2, 1, 1, 150.00, 399.00, '4710001000028', 30, true, NOW(), 1),
(3, 'PHN-CBL-001', 'USB-C to Lightning 充電線 1M', 'MFi認證，支援快充', 2, 1, 1, 120.00, 299.00, '4710001000035', 40, true, NOW(), 1),
(4, 'CMP-KB-001', '無線藍牙鍵盤', '輕薄設計，支援多設備切換', 3, 1, 1, 450.00, 990.00, '4710001000042', 20, true, NOW(), 1),
(5, 'CMP-MS-001', '無線滑鼠 2.4G', '人體工學設計，省電模式', 3, 1, 1, 180.00, 450.00, '4710001000059', 25, true, NOW(), 1),
(6, 'SNK-CHIP-001', '樂事洋芋片 經典原味 85g', '酥脆可口的經典口味', 5, 3, 1, 22.00, 35.00, '4710001000066', 100, true, NOW(), 1),
(7, 'SNK-CHIP-002', '樂事洋芋片 海苔口味 85g', '海苔風味的酥脆洋芋片', 5, 3, 1, 22.00, 35.00, '4710001000073', 100, true, NOW(), 1),
(8, 'DRK-WATER-001', '礦泉水 600ml', '純淨天然礦泉水', 6, 4, 1, 8.00, 15.00, '4710001000080', 200, true, NOW(), 1),
(9, 'DRK-TEA-001', '御茶園 綠茶 500ml', '無糖綠茶，清爽解膩', 6, 4, 1, 12.00, 25.00, '4710001000097', 150, true, NOW(), 1),
(10, 'DRK-COFFEE-001', '伯朗咖啡 曼特寧 250ml', '香醇濃郁的曼特寧咖啡', 6, 5, 1, 15.00, 30.00, '4710001000104', 100, true, NOW(), 1);

-- =============================================================================
-- 會員等級資料
-- =============================================================================
INSERT INTO customer_levels (id, code, name, discount_rate, min_spent, points_multiplier, description, is_active, created_at, created_by) VALUES
(1, 'NORMAL', '一般會員', 1.00, 0.00, 1.0, '一般會員，無特殊優惠', true, NOW(), 1),
(2, 'SILVER', '銀卡會員', 0.95, 5000.00, 1.5, '累積消費滿5000元，享95折優惠', true, NOW(), 1),
(3, 'GOLD', '金卡會員', 0.90, 20000.00, 2.0, '累積消費滿20000元，享9折優惠', true, NOW(), 1),
(4, 'VIP', 'VIP會員', 0.85, 50000.00, 3.0, '累積消費滿50000元，享85折優惠', true, NOW(), 1);

-- =============================================================================
-- 會員資料
-- =============================================================================
INSERT INTO customers (id, member_no, name, phone, email, gender, birthday, level_id, total_points, total_spent, register_date, is_active, address, notes, created_at, created_by) VALUES
(1, 'M202401010001', '林小明', '0912-111-111', 'lin.ming@example.com', 'M', '1990-05-15', 3, 2500, 25000.00, NOW(), true, '台北市大安區忠孝東路100號', 'VIP客戶，喜歡3C產品', NOW(), 1),
(2, 'M202401010002', '王小美', '0923-222-222', 'wang.mei@example.com', 'F', '1995-08-20', 2, 800, 8500.00, NOW(), true, '新北市板橋區文化路50號', '喜歡美妝產品', NOW(), 1),
(3, 'M202401010003', '張大衛', '0934-333-333', 'zhang.david@example.com', 'M', '1988-12-01', 1, 150, 1500.00, NOW(), true, '台中市西屯區台灣大道200號', NULL, NOW(), 1),
(4, 'M202401010004', '陳雅婷', '0945-444-444', 'chen.ting@example.com', 'F', '1992-03-25', 4, 6000, 65000.00, NOW(), true, '高雄市前鎮區中山路300號', 'VIP客戶，公司採購', NOW(), 1),
(5, 'M202401010005', '李志偉', '0956-555-555', 'lee.wei@example.com', 'M', '1985-07-10', 1, 50, 500.00, NOW(), true, '台北市中正區羅斯福路一段10號', '新會員', NOW(), 1);

-- =============================================================================
-- 供應商資料
-- =============================================================================
INSERT INTO suppliers (id, code, name, contact_person, phone, email, address, tax_id, payment_terms, is_active, notes, created_at, created_by) VALUES
(1, 'SUP001', '大同電子股份有限公司', '張經理', '02-2543-1234', 'supplier1@tatung.com', '台北市中山區中山北路100號', '12345678', 'NET30', true, '主要3C供應商', NOW(), 1),
(2, 'SUP002', '統一企業股份有限公司', '李主任', '06-253-1234', 'supplier2@uni.com', '台南市永康區中正路200號', '22334455', 'NET45', true, '飲料與食品供應商', NOW(), 1),
(3, 'SUP003', '寶雅國際股份有限公司', '王小姐', '04-2255-6789', 'supplier3@poya.com', '台中市西屯區台灣大道300號', '33445566', 'NET30', true, '美妝與居家用品供應商', NOW(), 1),
(4, 'SUP004', '華碩電腦股份有限公司', '陳協理', '02-2894-3447', 'supplier4@asus.com', '台北市北投區立德路150號', '44556677', 'NET60', true, '電腦周邊設備供應商', NOW(), 1);

-- =============================================================================
-- 庫存資料
-- =============================================================================
INSERT INTO inventories (id, product_id, warehouse_id, quantity, reserved_quantity, last_movement_date, created_at, created_by) VALUES
(1, 1, 2, 100, 5, NOW(), NOW(), 1),
(2, 2, 2, 50, 2, NOW(), NOW(), 1),
(3, 3, 2, 80, 0, NOW(), NOW(), 1),
(4, 6, 2, 200, 10, NOW(), NOW(), 1),
(5, 8, 2, 300, 0, NOW(), NOW(), 1),
(6, 1, 5, 500, 100, NOW(), NOW(), 1),
(7, 2, 5, 300, 50, NOW(), NOW(), 1),
(8, 3, 5, 400, 30, NOW(), NOW(), 1),
(9, 4, 5, 100, 10, NOW(), NOW(), 1),
(10, 5, 5, 150, 5, NOW(), NOW(), 1);

-- =============================================================================
-- 促銷活動資料
-- =============================================================================
INSERT INTO promotions (id, code, name, description, promotion_type, discount_type, discount_value, min_purchase_amount, start_date, end_date, is_active, created_at, created_by) VALUES
(1, 'PROMO2024CNY', '2024農曆新年優惠', '全館商品滿千折百', 'GENERAL', 'FIXED_AMOUNT', 100.00, 1000.00, '2024-01-01 00:00:00', '2024-12-31 23:59:59', true, NOW(), 1),
(2, 'PROMO3C20', '3C產品8折優惠', '指定3C產品享8折優惠', 'CATEGORY', 'PERCENTAGE', 20.00, 0.00, '2024-01-15 00:00:00', '2024-12-31 23:59:59', true, NOW(), 1),
(3, 'PROMOVIP', 'VIP會員專屬優惠', 'VIP會員額外95折', 'MEMBER', 'PERCENTAGE', 5.00, 0.00, '2024-01-01 00:00:00', '2024-12-31 23:59:59', true, NOW(), 1);

-- =============================================================================
-- 優惠券資料
-- =============================================================================
INSERT INTO coupons (id, code, name, description, discount_type, discount_value, min_purchase_amount, max_discount_amount, start_date, end_date, usage_limit, used_count, is_active, created_at, created_by) VALUES
(1, 'WELCOME100', '新會員優惠券', '新會員首購折100元', 'FIXED_AMOUNT', 100.00, 500.00, NULL, '2024-01-01 00:00:00', '2024-12-31 23:59:59', 1000, 150, true, NOW(), 1),
(2, 'SUMMER15', '夏季折扣券', '夏季購物享85折', 'PERCENTAGE', 15.00, 1000.00, 500.00, '2024-06-01 00:00:00', '2024-12-31 23:59:59', 500, 0, true, NOW(), 1),
(3, 'BIRTHDAY200', '生日優惠券', '生日當月折200元', 'FIXED_AMOUNT', 200.00, 800.00, NULL, '2024-01-01 00:00:00', '2024-12-31 23:59:59', NULL, 45, true, NOW(), 1);

-- =============================================================================
-- 訂單資料
-- =============================================================================
INSERT INTO orders (id, order_no, store_id, customer_id, order_date, subtotal, discount_amount, tax_amount, total_amount, status, notes, created_at, created_by) VALUES
(1, 'ORD20240101001', 2, 1, '2024-01-15 10:30:00', 1697.00, 100.00, 80.00, 1677.00, 'PAID', '農曆新年優惠適用', NOW(), 3),
(2, 'ORD20240101002', 2, 2, '2024-01-15 14:20:00', 598.00, 0.00, 28.00, 626.00, 'PAID', NULL, NOW(), 3),
(3, 'ORD20240101003', 3, NULL, '2024-01-15 16:45:00', 105.00, 0.00, 5.00, 110.00, 'PAID', '非會員購買', NOW(), 4),
(4, 'ORD20240101004', 2, 4, '2024-01-16 09:15:00', 4950.00, 990.00, 198.00, 4158.00, 'PAID', 'VIP會員大量採購', NOW(), 3),
(5, 'ORD20240101005', 2, 3, '2024-01-16 11:30:00', 299.00, 0.00, 15.00, 314.00, 'CANCELLED', '客戶取消訂單', NOW(), 3);

-- =============================================================================
-- 訂單明細資料
-- =============================================================================
INSERT INTO order_items (id, order_id, product_id, quantity, unit_price, discount_amount, subtotal, created_at, created_by) VALUES
(1, 1, 1, 2, 199.00, 0.00, 398.00, NOW(), 3),
(2, 1, 2, 1, 399.00, 0.00, 399.00, NOW(), 3),
(3, 1, 3, 3, 299.00, 0.00, 897.00, NOW(), 3),
(4, 2, 1, 3, 199.00, 0.00, 597.00, NOW(), 3),
(5, 3, 6, 2, 35.00, 0.00, 70.00, NOW(), 4),
(6, 3, 7, 1, 35.00, 0.00, 35.00, NOW(), 4),
(7, 4, 4, 5, 990.00, 0.00, 4950.00, NOW(), 3);

-- =============================================================================
-- 付款記錄資料
-- =============================================================================
INSERT INTO payments (id, order_id, payment_method, amount, payment_date, reference_no, notes, created_at, created_by) VALUES
(1, 1, 'CREDIT_CARD', 1677.00, '2024-01-15 10:32:00', 'CC20240115001', '信用卡付款', NOW(), 3),
(2, 2, 'CASH', 626.00, '2024-01-15 14:22:00', NULL, '現金付款', NOW(), 3),
(3, 3, 'CASH', 110.00, '2024-01-15 16:47:00', NULL, '現金付款', NOW(), 4),
(4, 4, 'BANK_TRANSFER', 4158.00, '2024-01-16 09:30:00', 'BT20240116001', '銀行轉帳', NOW(), 3);

-- =============================================================================
-- 庫存異動記錄
-- =============================================================================
INSERT INTO inventory_movements (id, inventory_id, movement_type, quantity, reason, reference_no, created_at, created_by) VALUES
(1, 1, 'IN', 100, '初始入庫', 'INIT-001', NOW(), 1),
(2, 1, 'OUT', 2, '銷售出庫', 'ORD20240101001', NOW(), 3),
(3, 2, 'IN', 50, '初始入庫', 'INIT-002', NOW(), 1),
(4, 2, 'OUT', 1, '銷售出庫', 'ORD20240101001', NOW(), 3),
(5, 6, 'IN', 500, '初始入庫', 'INIT-006', NOW(), 1),
(6, 6, 'OUT', 100, '門市調撥', 'TRANS-001', NOW(), 5);

-- =============================================================================
-- 完成
-- =============================================================================
SELECT '種子資料重置完成！' AS message;
