-- =============================================================================
-- 零售業簡易ERP系統 - Flyway Migration V2
-- 建立商品模組資料表
-- =============================================================================
-- 版本: V2
-- 建立日期: 2025-01-01
-- 說明: 建立稅別、單位、商品分類、商品等資料表
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. 稅別資料表 (tax_types)
-- -----------------------------------------------------------------------------
CREATE TABLE tax_types (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主鍵 ID',
    code VARCHAR(20) NOT NULL UNIQUE COMMENT '稅別代碼',
    name VARCHAR(50) NOT NULL COMMENT '稅別名稱',
    rate DECIMAL(5,2) NOT NULL COMMENT '稅率 (%)',
    is_default BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否為預設',
    is_active BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否啟用',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    created_by BIGINT COMMENT '建立者 ID',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
    updated_by BIGINT COMMENT '更新者 ID',

    INDEX idx_tax_types_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='稅別資料表';

-- -----------------------------------------------------------------------------
-- 2. 單位資料表 (units)
-- -----------------------------------------------------------------------------
CREATE TABLE units (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主鍵 ID',
    code VARCHAR(20) NOT NULL UNIQUE COMMENT '單位代碼',
    name VARCHAR(50) NOT NULL COMMENT '單位名稱',
    is_active BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否啟用',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    created_by BIGINT COMMENT '建立者 ID',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
    updated_by BIGINT COMMENT '更新者 ID',

    INDEX idx_units_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='單位資料表';

-- -----------------------------------------------------------------------------
-- 3. 商品分類資料表 (categories)
-- -----------------------------------------------------------------------------
CREATE TABLE categories (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主鍵 ID',
    code VARCHAR(50) NOT NULL UNIQUE COMMENT '分類代碼',
    name VARCHAR(100) NOT NULL COMMENT '分類名稱',
    parent_id BIGINT COMMENT '父分類 ID',
    level INT NOT NULL DEFAULT 1 COMMENT '層級',
    path VARCHAR(255) COMMENT '路徑（如：1/2/3）',
    description VARCHAR(500) COMMENT '分類描述',
    is_active BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否啟用',
    sort_order INT DEFAULT 0 COMMENT '排序順序',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    created_by BIGINT COMMENT '建立者 ID',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
    updated_by BIGINT COMMENT '更新者 ID',

    INDEX idx_categories_code (code),
    INDEX idx_categories_parent (parent_id),
    INDEX idx_categories_active (is_active),

    CONSTRAINT fk_categories_parent FOREIGN KEY (parent_id) REFERENCES categories(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品分類資料表';

-- -----------------------------------------------------------------------------
-- 4. 商品資料表 (products)
-- -----------------------------------------------------------------------------
CREATE TABLE products (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主鍵 ID',
    sku VARCHAR(50) NOT NULL UNIQUE COMMENT '商品編號 (SKU)',
    name VARCHAR(200) NOT NULL COMMENT '商品名稱',
    description TEXT COMMENT '商品描述',
    category_id BIGINT COMMENT '分類 ID',
    unit_id BIGINT NOT NULL COMMENT '單位 ID',
    tax_type_id BIGINT NOT NULL COMMENT '稅別 ID',
    cost_price DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '成本價',
    selling_price DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '售價',
    barcode VARCHAR(50) COMMENT '主條碼',
    safety_stock INT DEFAULT 0 COMMENT '安全庫存',
    is_active BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否啟用',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    created_by BIGINT COMMENT '建立者 ID',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
    updated_by BIGINT COMMENT '更新者 ID',

    INDEX idx_products_sku (sku),
    INDEX idx_products_barcode (barcode),
    INDEX idx_products_category (category_id),
    INDEX idx_products_active (is_active),

    CONSTRAINT fk_products_category FOREIGN KEY (category_id) REFERENCES categories(id),
    CONSTRAINT fk_products_unit FOREIGN KEY (unit_id) REFERENCES units(id),
    CONSTRAINT fk_products_tax_type FOREIGN KEY (tax_type_id) REFERENCES tax_types(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品資料表';

-- -----------------------------------------------------------------------------
-- 5. 商品條碼資料表 (product_barcodes)
-- -----------------------------------------------------------------------------
CREATE TABLE product_barcodes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主鍵 ID',
    product_id BIGINT NOT NULL COMMENT '商品 ID',
    barcode VARCHAR(50) NOT NULL COMMENT '條碼',
    is_primary BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否為主條碼',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    created_by BIGINT COMMENT '建立者 ID',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
    updated_by BIGINT COMMENT '更新者 ID',

    INDEX idx_product_barcodes_barcode (barcode),
    INDEX idx_product_barcodes_product (product_id),
    UNIQUE KEY uk_barcode (barcode),

    CONSTRAINT fk_product_barcodes_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品條碼資料表';
