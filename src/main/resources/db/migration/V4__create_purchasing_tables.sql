-- =============================================================================
-- 零售業簡易ERP系統 - Flyway Migration V4
-- 建立採購模組資料表
-- =============================================================================
-- 版本: V4
-- 建立日期: 2025-01-01
-- 說明: 建立供應商資料表
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. 供應商資料表 (suppliers)
-- -----------------------------------------------------------------------------
CREATE TABLE suppliers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主鍵 ID',
    code VARCHAR(20) NOT NULL UNIQUE COMMENT '供應商代碼',
    name VARCHAR(100) NOT NULL COMMENT '供應商名稱',
    short_name VARCHAR(50) COMMENT '簡稱',
    contact_person VARCHAR(50) COMMENT '聯絡人',
    phone VARCHAR(20) COMMENT '電話',
    mobile VARCHAR(20) COMMENT '手機',
    fax VARCHAR(20) COMMENT '傳真',
    email VARCHAR(100) COMMENT 'Email',
    address VARCHAR(500) COMMENT '地址',
    tax_id VARCHAR(20) COMMENT '統一編號',
    payment_terms ENUM('CASH', 'COD', 'NET30', 'NET60', 'NET90') DEFAULT 'NET30' COMMENT '付款條件',
    bank_name VARCHAR(100) COMMENT '銀行名稱',
    bank_account VARCHAR(50) COMMENT '銀行帳號',
    notes TEXT COMMENT '備註',
    is_active BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否啟用',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    created_by BIGINT COMMENT '建立者 ID',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
    updated_by BIGINT COMMENT '更新者 ID',

    INDEX idx_suppliers_code (code),
    INDEX idx_suppliers_name (name),
    INDEX idx_suppliers_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='供應商資料表';

-- -----------------------------------------------------------------------------
-- 2. 供應商價格資料表 (supplier_prices)
-- -----------------------------------------------------------------------------
CREATE TABLE supplier_prices (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主鍵 ID',
    supplier_id BIGINT NOT NULL COMMENT '供應商 ID',
    product_id BIGINT NOT NULL COMMENT '商品 ID',
    supplier_sku VARCHAR(50) COMMENT '供應商商品編號',
    unit_price DECIMAL(12,2) NOT NULL COMMENT '進貨價格',
    min_order_quantity INT DEFAULT 1 COMMENT '最低訂購量',
    lead_time_days INT COMMENT '前置天數',
    is_primary BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否為主要供應商',
    effective_date DATE NOT NULL COMMENT '生效日期',
    expiry_date DATE COMMENT '失效日期',
    notes VARCHAR(200) COMMENT '備註',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    created_by BIGINT COMMENT '建立者 ID',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
    updated_by BIGINT COMMENT '更新者 ID',

    INDEX idx_supplier_prices_supplier (supplier_id),
    INDEX idx_supplier_prices_product (product_id),
    UNIQUE KEY uk_supplier_product_date (supplier_id, product_id, effective_date),

    CONSTRAINT fk_supplier_prices_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers(id),
    CONSTRAINT fk_supplier_prices_product FOREIGN KEY (product_id) REFERENCES products(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='供應商價格資料表';
