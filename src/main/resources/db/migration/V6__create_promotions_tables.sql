-- =============================================================================
-- 零售業簡易ERP系統 - Flyway Migration V6
-- 建立促銷模組資料表
-- =============================================================================
-- 版本: V6
-- 建立日期: 2025-01-01
-- 說明: 建立促銷活動、優惠券資料表
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. 促銷活動資料表 (promotions)
-- -----------------------------------------------------------------------------
CREATE TABLE promotions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主鍵 ID',
    name VARCHAR(100) NOT NULL COMMENT '活動名稱',
    description TEXT COMMENT '活動描述',
    type ENUM('DISCOUNT', 'BUY_X_GET_Y', 'BUNDLE') NOT NULL COMMENT '促銷類型',
    discount_type ENUM('PERCENTAGE', 'FIXED_AMOUNT') COMMENT '折扣類型',
    discount_value DECIMAL(10,2) COMMENT '折扣值（百分比或固定金額）',
    min_purchase_amount DECIMAL(12,2) DEFAULT 0 COMMENT '最低消費金額',
    start_date DATE NOT NULL COMMENT '開始日期',
    end_date DATE NOT NULL COMMENT '結束日期',
    is_active BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否啟用',
    priority INT DEFAULT 0 COMMENT '優先順序（數字越大優先）',
    notes TEXT COMMENT '備註',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    created_by BIGINT COMMENT '建立者 ID',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
    updated_by BIGINT COMMENT '更新者 ID',

    INDEX idx_promotions_dates (start_date, end_date),
    INDEX idx_promotions_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='促銷活動資料表';

-- -----------------------------------------------------------------------------
-- 2. 促銷商品關聯表 (promotion_products)
-- -----------------------------------------------------------------------------
CREATE TABLE promotion_products (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主鍵 ID',
    promotion_id BIGINT NOT NULL COMMENT '促銷活動 ID',
    product_id BIGINT COMMENT '商品 ID（空表示全部商品）',
    category_id BIGINT COMMENT '分類 ID（空表示全部分類）',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',

    INDEX idx_promo_products_promotion (promotion_id),
    INDEX idx_promo_products_product (product_id),

    CONSTRAINT fk_promo_products_promotion FOREIGN KEY (promotion_id) REFERENCES promotions(id) ON DELETE CASCADE,
    CONSTRAINT fk_promo_products_product FOREIGN KEY (product_id) REFERENCES products(id),
    CONSTRAINT fk_promo_products_category FOREIGN KEY (category_id) REFERENCES categories(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='促銷商品關聯表';

-- -----------------------------------------------------------------------------
-- 3. 優惠券資料表 (coupons)
-- -----------------------------------------------------------------------------
CREATE TABLE coupons (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主鍵 ID',
    code VARCHAR(50) NOT NULL UNIQUE COMMENT '優惠券代碼',
    name VARCHAR(100) NOT NULL COMMENT '優惠券名稱',
    description TEXT COMMENT '優惠券描述',
    discount_type ENUM('PERCENTAGE', 'FIXED_AMOUNT') NOT NULL COMMENT '折扣類型',
    discount_value DECIMAL(10,2) NOT NULL COMMENT '折扣值',
    min_order_amount DECIMAL(12,2) DEFAULT 0 COMMENT '最低訂單金額',
    max_discount_amount DECIMAL(12,2) COMMENT '最高折扣金額',
    start_date DATE NOT NULL COMMENT '開始日期',
    end_date DATE NOT NULL COMMENT '結束日期',
    max_uses INT COMMENT '最大使用次數（空表示無限）',
    used_count INT NOT NULL DEFAULT 0 COMMENT '已使用次數',
    max_uses_per_customer INT DEFAULT 1 COMMENT '每位會員最大使用次數',
    is_active BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否啟用',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    created_by BIGINT COMMENT '建立者 ID',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
    updated_by BIGINT COMMENT '更新者 ID',

    INDEX idx_coupons_code (code),
    INDEX idx_coupons_dates (start_date, end_date),
    INDEX idx_coupons_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='優惠券資料表';

-- -----------------------------------------------------------------------------
-- 4. 優惠券使用記錄表 (coupon_usages)
-- -----------------------------------------------------------------------------
CREATE TABLE coupon_usages (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主鍵 ID',
    coupon_id BIGINT NOT NULL COMMENT '優惠券 ID',
    customer_id BIGINT COMMENT '會員 ID',
    order_id BIGINT COMMENT '訂單 ID',
    discount_amount DECIMAL(12,2) NOT NULL COMMENT '折扣金額',
    used_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '使用時間',

    INDEX idx_coupon_usages_coupon (coupon_id),
    INDEX idx_coupon_usages_customer (customer_id),
    INDEX idx_coupon_usages_order (order_id),

    CONSTRAINT fk_coupon_usages_coupon FOREIGN KEY (coupon_id) REFERENCES coupons(id),
    CONSTRAINT fk_coupon_usages_customer FOREIGN KEY (customer_id) REFERENCES customers(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='優惠券使用記錄表';
