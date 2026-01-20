-- =============================================================================
-- 零售業簡易ERP系統 - Flyway Migration V5
-- 建立庫存模組資料表
-- =============================================================================
-- 版本: V5
-- 建立日期: 2025-01-01
-- 說明: 建立庫存、庫存異動資料表
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. 庫存資料表 (inventory)
-- -----------------------------------------------------------------------------
CREATE TABLE inventory (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主鍵 ID',
    product_id BIGINT NOT NULL COMMENT '商品 ID',
    warehouse_id BIGINT NOT NULL COMMENT '倉庫 ID',
    quantity INT NOT NULL DEFAULT 0 COMMENT '庫存數量',
    reserved_quantity INT NOT NULL DEFAULT 0 COMMENT '已預留數量',
    last_count_date DATE COMMENT '最後盤點日期',
    last_movement_date DATETIME COMMENT '最後異動日期',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    created_by BIGINT COMMENT '建立者 ID',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
    updated_by BIGINT COMMENT '更新者 ID',

    UNIQUE KEY uk_product_warehouse (product_id, warehouse_id),
    INDEX idx_inventory_product (product_id),
    INDEX idx_inventory_warehouse (warehouse_id),
    INDEX idx_inventory_quantity (quantity),

    CONSTRAINT fk_inventory_product FOREIGN KEY (product_id) REFERENCES products(id),
    CONSTRAINT fk_inventory_warehouse FOREIGN KEY (warehouse_id) REFERENCES stores(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='庫存資料表';

-- -----------------------------------------------------------------------------
-- 2. 庫存異動資料表 (inventory_movements)
-- -----------------------------------------------------------------------------
CREATE TABLE inventory_movements (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主鍵 ID',
    product_id BIGINT NOT NULL COMMENT '商品 ID',
    warehouse_id BIGINT NOT NULL COMMENT '倉庫 ID',
    movement_type ENUM(
        'PURCHASE_IN',   -- 採購入庫
        'SALES_OUT',     -- 銷售出庫
        'RETURN_IN',     -- 退貨入庫
        'RETURN_OUT',    -- 退貨出庫
        'TRANSFER_IN',   -- 調撥入庫
        'TRANSFER_OUT',  -- 調撥出庫
        'ADJUST_IN',     -- 調整入庫
        'ADJUST_OUT',    -- 調整出庫
        'COUNT_IN',      -- 盤盈入庫
        'COUNT_OUT'      -- 盤虧出庫
    ) NOT NULL COMMENT '異動類型',
    quantity INT NOT NULL COMMENT '異動數量',
    before_quantity INT NOT NULL COMMENT '異動前數量',
    after_quantity INT NOT NULL COMMENT '異動後數量',
    unit_cost DECIMAL(12,2) COMMENT '單位成本',
    reference_type VARCHAR(50) COMMENT '關聯單據類型',
    reference_id BIGINT COMMENT '關聯單據 ID',
    reference_no VARCHAR(30) COMMENT '關聯單據編號',
    notes VARCHAR(200) COMMENT '備註',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    created_by BIGINT COMMENT '建立者 ID',

    INDEX idx_movements_product (product_id),
    INDEX idx_movements_warehouse (warehouse_id),
    INDEX idx_movements_type (movement_type),
    INDEX idx_movements_created (created_at),
    INDEX idx_movements_reference (reference_type, reference_id),

    CONSTRAINT fk_movements_product FOREIGN KEY (product_id) REFERENCES products(id),
    CONSTRAINT fk_movements_warehouse FOREIGN KEY (warehouse_id) REFERENCES stores(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='庫存異動資料表';
