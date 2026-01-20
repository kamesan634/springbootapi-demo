-- =============================================================================
-- 零售業簡易ERP系統 - Flyway Migration V7
-- 建立銷售模組資料表
-- =============================================================================
-- 版本: V7
-- 建立日期: 2025-01-01
-- 說明: 建立訂單、訂單明細、付款記錄資料表
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. 訂單資料表 (orders)
-- -----------------------------------------------------------------------------
CREATE TABLE orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主鍵 ID',
    order_no VARCHAR(30) NOT NULL UNIQUE COMMENT '訂單編號',
    store_id BIGINT NOT NULL COMMENT '門市 ID',
    customer_id BIGINT COMMENT '會員 ID',
    order_date DATE NOT NULL COMMENT '訂單日期',
    order_time TIME NOT NULL COMMENT '訂單時間',
    subtotal DECIMAL(14,2) NOT NULL DEFAULT 0 COMMENT '小計（未折扣）',
    discount_amount DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '折扣金額',
    tax_amount DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '稅額',
    total_amount DECIMAL(14,2) NOT NULL DEFAULT 0 COMMENT '總金額',
    paid_amount DECIMAL(14,2) NOT NULL DEFAULT 0 COMMENT '已付金額',
    change_amount DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '找零金額',
    points_earned INT DEFAULT 0 COMMENT '獲得點數',
    points_used INT DEFAULT 0 COMMENT '使用點數',
    coupon_id BIGINT COMMENT '使用的優惠券 ID',
    status ENUM('PENDING', 'PAID', 'CANCELLED', 'REFUNDED') NOT NULL DEFAULT 'PENDING' COMMENT '訂單狀態',
    notes TEXT COMMENT '備註',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    created_by BIGINT COMMENT '建立者 ID（收銀員）',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
    updated_by BIGINT COMMENT '更新者 ID',

    INDEX idx_orders_no (order_no),
    INDEX idx_orders_store (store_id),
    INDEX idx_orders_customer (customer_id),
    INDEX idx_orders_date (order_date),
    INDEX idx_orders_status (status),

    CONSTRAINT fk_orders_store FOREIGN KEY (store_id) REFERENCES stores(id),
    CONSTRAINT fk_orders_customer FOREIGN KEY (customer_id) REFERENCES customers(id),
    CONSTRAINT fk_orders_coupon FOREIGN KEY (coupon_id) REFERENCES coupons(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='訂單資料表';

-- -----------------------------------------------------------------------------
-- 2. 訂單明細資料表 (order_items)
-- -----------------------------------------------------------------------------
CREATE TABLE order_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主鍵 ID',
    order_id BIGINT NOT NULL COMMENT '訂單 ID',
    product_id BIGINT NOT NULL COMMENT '商品 ID',
    product_name VARCHAR(200) NOT NULL COMMENT '商品名稱（快照）',
    quantity INT NOT NULL COMMENT '數量',
    unit_price DECIMAL(12,2) NOT NULL COMMENT '單價',
    cost_price DECIMAL(12,2) NOT NULL COMMENT '成本價（快照）',
    discount_amount DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '折扣金額',
    tax_amount DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '稅額',
    subtotal DECIMAL(12,2) NOT NULL COMMENT '小計',
    notes VARCHAR(200) COMMENT '備註',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',

    INDEX idx_order_items_order (order_id),
    INDEX idx_order_items_product (product_id),

    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_order_items_product FOREIGN KEY (product_id) REFERENCES products(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='訂單明細資料表';

-- -----------------------------------------------------------------------------
-- 3. 付款記錄資料表 (payments)
-- -----------------------------------------------------------------------------
CREATE TABLE payments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主鍵 ID',
    order_id BIGINT NOT NULL COMMENT '訂單 ID',
    payment_method ENUM('CASH', 'CREDIT_CARD', 'DEBIT_CARD', 'LINE_PAY', 'APPLE_PAY', 'OTHER') NOT NULL COMMENT '付款方式',
    amount DECIMAL(12,2) NOT NULL COMMENT '付款金額',
    payment_date DATE NOT NULL COMMENT '付款日期',
    payment_time TIME NOT NULL COMMENT '付款時間',
    reference_no VARCHAR(100) COMMENT '交易參考編號',
    card_last_four VARCHAR(4) COMMENT '卡號末四碼',
    notes VARCHAR(200) COMMENT '備註',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    created_by BIGINT COMMENT '建立者 ID',

    INDEX idx_payments_order (order_id),
    INDEX idx_payments_date (payment_date),
    INDEX idx_payments_method (payment_method),

    CONSTRAINT fk_payments_order FOREIGN KEY (order_id) REFERENCES orders(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='付款記錄資料表';

-- 更新優惠券使用記錄的外鍵
ALTER TABLE coupon_usages ADD CONSTRAINT fk_coupon_usages_order FOREIGN KEY (order_id) REFERENCES orders(id);
