-- =============================================================================
-- 零售業簡易ERP系統 - 種子資料 (Seed Data)
-- =============================================================================
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- 1. 角色資料 (VIEWER 和 PURCHASER 已在 V9 建立)
INSERT IGNORE INTO roles (code, name, description, is_system, is_active, sort_order, created_at, created_by) VALUES
('ADMIN', '系統管理員', '擁有系統所有權限的管理員角色', true, true, 1, NOW(), NULL),
('MANAGER', '門市店長', '門市管理權限，可管理門市營運', false, true, 2, NOW(), NULL),
('CASHIER', '收銀員', '收銀台操作權限，處理銷售交易', false, true, 3, NOW(), NULL),
('WAREHOUSE', '倉管人員', '倉庫管理權限，處理進出貨作業', false, true, 7, NOW(), NULL);

-- 2. 門市/倉庫資料
INSERT IGNORE INTO stores (code, name, address, phone, type, is_active, created_at, created_by) VALUES
('HQ', '總公司', '台北市信義區信義路五段7號', '02-2345-6789', 'WAREHOUSE', true, NOW(), NULL),
('S001', '台北旗艦店', '台北市大安區忠孝東路四段123號', '02-2771-1234', 'STORE', true, NOW(), NULL),
('S002', '新北板橋店', '新北市板橋區文化路一段88號', '02-2965-5678', 'STORE', true, NOW(), NULL),
('S003', '台中逢甲店', '台中市西屯區文華路99號', '04-2452-3456', 'STORE', true, NOW(), NULL),
('W001', '北區物流中心', '新北市五股區五權路100號', '02-2291-7890', 'WAREHOUSE', true, NOW(), NULL),
('W002', '中區物流中心', '台中市大雅區中清路200號', '04-2567-8901', 'WAREHOUSE', true, NOW(), NULL);

-- 3. 使用者資料 (密碼: password123)
INSERT IGNORE INTO users (username, password, name, email, phone, role_id, avatar, is_active, created_at, created_by)
SELECT 'admin', '$2a$10$o/c.aHAZGVYTS6bdIrvuJ.l7KbpdnVbOmsu66DFACj1YdmKp5P4Ta', '系統管理員', 'admin@erp.demo.com', '0912-345-678', id, NULL, true, NOW(), NULL FROM roles WHERE code = 'ADMIN';
INSERT IGNORE INTO users (username, password, name, email, phone, role_id, avatar, is_active, created_at, created_by)
SELECT 'manager01', '$2a$10$o/c.aHAZGVYTS6bdIrvuJ.l7KbpdnVbOmsu66DFACj1YdmKp5P4Ta', '王大明', 'manager01@erp.demo.com', '0923-456-789', id, NULL, true, NOW(), NULL FROM roles WHERE code = 'MANAGER';
INSERT IGNORE INTO users (username, password, name, email, phone, role_id, avatar, is_active, created_at, created_by)
SELECT 'cashier01', '$2a$10$o/c.aHAZGVYTS6bdIrvuJ.l7KbpdnVbOmsu66DFACj1YdmKp5P4Ta', '李小華', 'cashier01@erp.demo.com', '0934-567-890', id, NULL, true, NOW(), NULL FROM roles WHERE code = 'CASHIER';
INSERT IGNORE INTO users (username, password, name, email, phone, role_id, avatar, is_active, created_at, created_by)
SELECT 'cashier02', '$2a$10$o/c.aHAZGVYTS6bdIrvuJ.l7KbpdnVbOmsu66DFACj1YdmKp5P4Ta', '張美玲', 'cashier02@erp.demo.com', '0945-678-901', id, NULL, true, NOW(), NULL FROM roles WHERE code = 'CASHIER';
INSERT IGNORE INTO users (username, password, name, email, phone, role_id, avatar, is_active, created_at, created_by)
SELECT 'warehouse01', '$2a$10$o/c.aHAZGVYTS6bdIrvuJ.l7KbpdnVbOmsu66DFACj1YdmKp5P4Ta', '陳建志', 'warehouse01@erp.demo.com', '0956-789-012', id, NULL, true, NOW(), NULL FROM roles WHERE code = 'WAREHOUSE';

-- 4. 使用者門市關聯
INSERT IGNORE INTO user_stores (user_id, store_id)
SELECT u.id, s.id FROM users u, stores s WHERE u.username = 'admin' AND s.code = 'HQ';
INSERT IGNORE INTO user_stores (user_id, store_id)
SELECT u.id, s.id FROM users u, stores s WHERE u.username = 'manager01' AND s.code = 'S001';
INSERT IGNORE INTO user_stores (user_id, store_id)
SELECT u.id, s.id FROM users u, stores s WHERE u.username = 'cashier01' AND s.code = 'S001';
INSERT IGNORE INTO user_stores (user_id, store_id)
SELECT u.id, s.id FROM users u, stores s WHERE u.username = 'cashier02' AND s.code = 'S002';
INSERT IGNORE INTO user_stores (user_id, store_id)
SELECT u.id, s.id FROM users u, stores s WHERE u.username = 'warehouse01' AND s.code = 'W001';
INSERT IGNORE INTO user_stores (user_id, store_id)
SELECT u.id, s.id FROM users u, stores s WHERE u.username = 'warehouse01' AND s.code = 'W002';

-- 5. 分類資料
INSERT IGNORE INTO categories (code, name, description, parent_id, sort_order, is_active, created_at, created_by) VALUES
('ELEC', '3C電子', '電子產品、3C周邊', NULL, 1, true, NOW(), NULL);
SET @elec_id = LAST_INSERT_ID();

INSERT IGNORE INTO categories (code, name, description, parent_id, sort_order, is_active, created_at, created_by)
SELECT 'ELEC-PHONE', '手機配件', '手機殼、充電器、傳輸線等', id, 1, true, NOW(), NULL FROM categories WHERE code = 'ELEC';
INSERT IGNORE INTO categories (code, name, description, parent_id, sort_order, is_active, created_at, created_by)
SELECT 'ELEC-COMP', '電腦周邊', '鍵盤、滑鼠、USB設備等', id, 2, true, NOW(), NULL FROM categories WHERE code = 'ELEC';

INSERT IGNORE INTO categories (code, name, description, parent_id, sort_order, is_active, created_at, created_by) VALUES
('FOOD', '食品飲料', '各類食品與飲料', NULL, 2, true, NOW(), NULL);

INSERT IGNORE INTO categories (code, name, description, parent_id, sort_order, is_active, created_at, created_by)
SELECT 'FOOD-SNACK', '零食餅乾', '各類零食、餅乾、糖果', id, 1, true, NOW(), NULL FROM categories WHERE code = 'FOOD';
INSERT IGNORE INTO categories (code, name, description, parent_id, sort_order, is_active, created_at, created_by)
SELECT 'FOOD-DRINK', '飲料', '各類飲料、礦泉水、果汁', id, 2, true, NOW(), NULL FROM categories WHERE code = 'FOOD';

INSERT IGNORE INTO categories (code, name, description, parent_id, sort_order, is_active, created_at, created_by) VALUES
('HOME', '居家生活', '居家用品、清潔用品', NULL, 3, true, NOW(), NULL),
('BEAUTY', '美妝保養', '化妝品、保養品、個人護理', NULL, 4, true, NOW(), NULL);

-- 6. 計量單位資料
INSERT IGNORE INTO units (code, name, is_active, created_at, created_by) VALUES
('PCS', '個', true, NOW(), NULL),
('BOX', '盒', true, NOW(), NULL),
('PKG', '包', true, NOW(), NULL),
('BTL', '瓶', true, NOW(), NULL),
('CAN', '罐', true, NOW(), NULL),
('SET', '組', true, NOW(), NULL),
('KG', '公斤', true, NOW(), NULL);

-- 7. 稅別資料
INSERT IGNORE INTO tax_types (code, name, rate, is_active, created_at, created_by) VALUES
('TAX5', '應稅5%', 0.05, true, NOW(), NULL),
('TAX0', '零稅率', 0.00, true, NOW(), NULL),
('EXEMPT', '免稅', 0.00, true, NOW(), NULL);

-- 8. 商品資料
INSERT IGNORE INTO products (sku, name, description, category_id, unit_id, tax_type_id, cost_price, selling_price, barcode, safety_stock, is_active, created_at, created_by)
SELECT 'PHN-CASE-001', 'iPhone 15 透明保護殼', '高透明TPU材質，防摔防刮', c.id, u.id, t.id, 80.00, 199.00, '4710001000011', 50, true, NOW(), NULL
FROM categories c, units u, tax_types t WHERE c.code = 'ELEC-PHONE' AND u.code = 'PCS' AND t.code = 'TAX5';

INSERT IGNORE INTO products (sku, name, description, category_id, unit_id, tax_type_id, cost_price, selling_price, barcode, safety_stock, is_active, created_at, created_by)
SELECT 'PHN-CHG-001', 'USB-C 快充充電器 20W', '支援PD快充，適用iPhone/Android', c.id, u.id, t.id, 150.00, 399.00, '4710001000028', 30, true, NOW(), NULL
FROM categories c, units u, tax_types t WHERE c.code = 'ELEC-PHONE' AND u.code = 'PCS' AND t.code = 'TAX5';

INSERT IGNORE INTO products (sku, name, description, category_id, unit_id, tax_type_id, cost_price, selling_price, barcode, safety_stock, is_active, created_at, created_by)
SELECT 'PHN-CBL-001', 'USB-C to Lightning 充電線 1M', 'MFi認證，支援快充', c.id, u.id, t.id, 120.00, 299.00, '4710001000035', 40, true, NOW(), NULL
FROM categories c, units u, tax_types t WHERE c.code = 'ELEC-PHONE' AND u.code = 'PCS' AND t.code = 'TAX5';

INSERT IGNORE INTO products (sku, name, description, category_id, unit_id, tax_type_id, cost_price, selling_price, barcode, safety_stock, is_active, created_at, created_by)
SELECT 'CMP-KB-001', '無線藍牙鍵盤', '輕薄設計，支援多設備切換', c.id, u.id, t.id, 450.00, 990.00, '4710001000042', 20, true, NOW(), NULL
FROM categories c, units u, tax_types t WHERE c.code = 'ELEC-COMP' AND u.code = 'PCS' AND t.code = 'TAX5';

INSERT IGNORE INTO products (sku, name, description, category_id, unit_id, tax_type_id, cost_price, selling_price, barcode, safety_stock, is_active, created_at, created_by)
SELECT 'CMP-MS-001', '無線滑鼠 2.4G', '人體工學設計，省電模式', c.id, u.id, t.id, 180.00, 450.00, '4710001000059', 25, true, NOW(), NULL
FROM categories c, units u, tax_types t WHERE c.code = 'ELEC-COMP' AND u.code = 'PCS' AND t.code = 'TAX5';

INSERT IGNORE INTO products (sku, name, description, category_id, unit_id, tax_type_id, cost_price, selling_price, barcode, safety_stock, is_active, created_at, created_by)
SELECT 'SNK-CHIP-001', '樂事洋芋片 經典原味 85g', '酥脆可口的經典口味', c.id, u.id, t.id, 22.00, 35.00, '4710001000066', 100, true, NOW(), NULL
FROM categories c, units u, tax_types t WHERE c.code = 'FOOD-SNACK' AND u.code = 'PKG' AND t.code = 'TAX5';

INSERT IGNORE INTO products (sku, name, description, category_id, unit_id, tax_type_id, cost_price, selling_price, barcode, safety_stock, is_active, created_at, created_by)
SELECT 'SNK-CHIP-002', '樂事洋芋片 海苔口味 85g', '海苔風味的酥脆洋芋片', c.id, u.id, t.id, 22.00, 35.00, '4710001000073', 100, true, NOW(), NULL
FROM categories c, units u, tax_types t WHERE c.code = 'FOOD-SNACK' AND u.code = 'PKG' AND t.code = 'TAX5';

INSERT IGNORE INTO products (sku, name, description, category_id, unit_id, tax_type_id, cost_price, selling_price, barcode, safety_stock, is_active, created_at, created_by)
SELECT 'DRK-WATER-001', '礦泉水 600ml', '純淨天然礦泉水', c.id, u.id, t.id, 8.00, 15.00, '4710001000080', 200, true, NOW(), NULL
FROM categories c, units u, tax_types t WHERE c.code = 'FOOD-DRINK' AND u.code = 'BTL' AND t.code = 'TAX5';

INSERT IGNORE INTO products (sku, name, description, category_id, unit_id, tax_type_id, cost_price, selling_price, barcode, safety_stock, is_active, created_at, created_by)
SELECT 'DRK-TEA-001', '御茶園 綠茶 500ml', '無糖綠茶，清爽解膩', c.id, u.id, t.id, 12.00, 25.00, '4710001000097', 150, true, NOW(), NULL
FROM categories c, units u, tax_types t WHERE c.code = 'FOOD-DRINK' AND u.code = 'BTL' AND t.code = 'TAX5';

INSERT IGNORE INTO products (sku, name, description, category_id, unit_id, tax_type_id, cost_price, selling_price, barcode, safety_stock, is_active, created_at, created_by)
SELECT 'DRK-COFFEE-001', '伯朗咖啡 曼特寧 250ml', '香醇濃郁的曼特寧咖啡', c.id, u.id, t.id, 15.00, 30.00, '4710001000104', 100, true, NOW(), NULL
FROM categories c, units u, tax_types t WHERE c.code = 'FOOD-DRINK' AND u.code = 'CAN' AND t.code = 'TAX5';

-- 9. 會員等級資料
INSERT IGNORE INTO customer_levels (code, name, description, discount_rate, points_multiplier, upgrade_condition, is_default, is_active, sort_order, created_at, created_by) VALUES
('NORMAL', '一般會員', '一般會員，無特殊優惠', 1.00, 1.0, 0.00, true, true, 1, NOW(), NULL),
('SILVER', '銀卡會員', '累積消費滿5000元，享95折優惠', 0.95, 1.5, 5000.00, false, true, 2, NOW(), NULL),
('GOLD', '金卡會員', '累積消費滿20000元，享9折優惠', 0.90, 2.0, 20000.00, false, true, 3, NOW(), NULL),
('VIP', 'VIP會員', '累積消費滿50000元，享85折優惠', 0.85, 3.0, 50000.00, false, true, 4, NOW(), NULL);

-- 10. 會員資料
INSERT IGNORE INTO customers (member_no, name, phone, email, gender, birthday, level_id, total_points, total_spent, register_date, is_active, address, notes, created_at, created_by)
SELECT 'M202401010001', '林小明', '0912-111-111', 'lin.ming@example.com', 'MALE', '1990-05-15', id, 2500, 25000.00, NOW(), true, '台北市大安區忠孝東路100號', 'VIP客戶，喜歡3C產品', NOW(), NULL FROM customer_levels WHERE code = 'GOLD';
INSERT IGNORE INTO customers (member_no, name, phone, email, gender, birthday, level_id, total_points, total_spent, register_date, is_active, address, notes, created_at, created_by)
SELECT 'M202401010002', '王小美', '0923-222-222', 'wang.mei@example.com', 'FEMALE', '1995-08-20', id, 800, 8500.00, NOW(), true, '新北市板橋區文化路50號', '喜歡美妝產品', NOW(), NULL FROM customer_levels WHERE code = 'SILVER';
INSERT IGNORE INTO customers (member_no, name, phone, email, gender, birthday, level_id, total_points, total_spent, register_date, is_active, address, notes, created_at, created_by)
SELECT 'M202401010003', '張大衛', '0934-333-333', 'zhang.david@example.com', 'MALE', '1988-12-01', id, 150, 1500.00, NOW(), true, '台中市西屯區台灣大道200號', NULL, NOW(), NULL FROM customer_levels WHERE code = 'NORMAL';
INSERT IGNORE INTO customers (member_no, name, phone, email, gender, birthday, level_id, total_points, total_spent, register_date, is_active, address, notes, created_at, created_by)
SELECT 'M202401010004', '陳雅婷', '0945-444-444', 'chen.ting@example.com', 'FEMALE', '1992-03-25', id, 6000, 65000.00, NOW(), true, '高雄市前鎮區中山路300號', 'VIP客戶，公司採購', NOW(), NULL FROM customer_levels WHERE code = 'VIP';
INSERT IGNORE INTO customers (member_no, name, phone, email, gender, birthday, level_id, total_points, total_spent, register_date, is_active, address, notes, created_at, created_by)
SELECT 'M202401010005', '李志偉', '0956-555-555', 'lee.wei@example.com', 'MALE', '1985-07-10', id, 50, 500.00, NOW(), true, '台北市中正區羅斯福路一段10號', '新會員', NOW(), NULL FROM customer_levels WHERE code = 'NORMAL';

-- 11. 供應商資料
INSERT IGNORE INTO suppliers (code, name, contact_person, phone, email, address, tax_id, payment_terms, is_active, notes, created_at, created_by) VALUES
('SUP001', '大同電子股份有限公司', '張經理', '02-2543-1234', 'supplier1@tatung.com', '台北市中山區中山北路100號', '12345678', 'NET30', true, '主要3C供應商', NOW(), NULL),
('SUP002', '統一企業股份有限公司', '李主任', '06-253-1234', 'supplier2@uni.com', '台南市永康區中正路200號', '22334455', 'NET45', true, '飲料與食品供應商', NOW(), NULL),
('SUP003', '寶雅國際股份有限公司', '王小姐', '04-2255-6789', 'supplier3@poya.com', '台中市西屯區台灣大道300號', '33445566', 'NET30', true, '美妝與居家用品供應商', NOW(), NULL),
('SUP004', '華碩電腦股份有限公司', '陳協理', '02-2894-3447', 'supplier4@asus.com', '台北市北投區立德路150號', '44556677', 'NET60', true, '電腦周邊設備供應商', NOW(), NULL);

-- 完成
SELECT '種子資料載入完成！' AS message;
