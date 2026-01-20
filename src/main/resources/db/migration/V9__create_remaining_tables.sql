-- =============================================================================
-- 零售業簡易ERP系統 - Flyway Migration V9
-- 建立 SA 需求中所有缺失的資料表
-- =============================================================================
-- 版本: V9
-- 建立日期: 2025-01-07
-- 說明: 依據 SA 文件補齊所有缺失的資料表
-- =============================================================================

-- =============================================================================
-- M01 系統管理模組
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1.1 稽核日誌資料表 (audit_logs)
-- -----------------------------------------------------------------------------
CREATE TABLE audit_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主鍵 ID',
    action VARCHAR(50) NOT NULL COMMENT '操作類型 (CREATE/UPDATE/DELETE/LOGIN/LOGOUT)',
    entity_type VARCHAR(100) NOT NULL COMMENT '實體類型 (如 User, Product, Order)',
    entity_id BIGINT COMMENT '實體 ID',
    entity_name VARCHAR(200) COMMENT '實體名稱/描述',
    old_value JSON COMMENT '變更前的值 (JSON)',
    new_value JSON COMMENT '變更後的值 (JSON)',
    user_id BIGINT COMMENT '操作者 ID',
    username VARCHAR(50) COMMENT '操作者帳號',
    ip_address VARCHAR(50) COMMENT 'IP 位址',
    user_agent VARCHAR(500) COMMENT '使用者代理',
    request_url VARCHAR(500) COMMENT '請求 URL',
    request_method VARCHAR(10) COMMENT 'HTTP 方法',
    store_id BIGINT COMMENT '門市 ID',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',

    INDEX idx_audit_logs_action (action),
    INDEX idx_audit_logs_entity (entity_type, entity_id),
    INDEX idx_audit_logs_user (user_id),
    INDEX idx_audit_logs_created (created_at),
    INDEX idx_audit_logs_store (store_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='稽核日誌資料表';

-- -----------------------------------------------------------------------------
-- 1.2 系統參數資料表 (system_parameters)
-- -----------------------------------------------------------------------------
CREATE TABLE system_parameters (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主鍵 ID',
    category VARCHAR(50) NOT NULL COMMENT '參數類別',
    param_key VARCHAR(100) NOT NULL COMMENT '參數鍵',
    param_value VARCHAR(1000) COMMENT '參數值',
    param_type ENUM('STRING', 'NUMBER', 'BOOLEAN', 'JSON', 'DATE') DEFAULT 'STRING' COMMENT '參數類型',
    description VARCHAR(500) COMMENT '參數描述',
    is_system BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否為系統參數（不可刪除）',
    is_encrypted BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否加密儲存',
    sort_order INT DEFAULT 0 COMMENT '排序順序',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    created_by BIGINT COMMENT '建立者 ID',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
    updated_by BIGINT COMMENT '更新者 ID',

    UNIQUE KEY uk_system_params (category, param_key),
    INDEX idx_system_params_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系統參數資料表';

-- -----------------------------------------------------------------------------
-- 1.3 編號規則資料表 (number_rules)
-- -----------------------------------------------------------------------------
CREATE TABLE number_rules (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主鍵 ID',
    rule_code VARCHAR(50) NOT NULL UNIQUE COMMENT '規則代碼 (如 ORDER, PURCHASE, REFUND)',
    rule_name VARCHAR(100) NOT NULL COMMENT '規則名稱',
    prefix VARCHAR(20) COMMENT '前綴 (如 ORD, PO, REF)',
    suffix VARCHAR(20) COMMENT '後綴',
    date_format VARCHAR(20) COMMENT '日期格式 (如 yyyyMMdd, yyMM)',
    sequence_length INT NOT NULL DEFAULT 4 COMMENT '流水號長度',
    current_sequence BIGINT NOT NULL DEFAULT 0 COMMENT '目前流水號',
    reset_period ENUM('DAILY', 'MONTHLY', 'YEARLY', 'NEVER') DEFAULT 'DAILY' COMMENT '重置週期',
    last_reset_date DATE COMMENT '最後重置日期',
    sample_number VARCHAR(50) COMMENT '範例編號',
    is_active BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否啟用',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    created_by BIGINT COMMENT '建立者 ID',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
    updated_by BIGINT COMMENT '更新者 ID',

    INDEX idx_number_rules_code (rule_code),
    INDEX idx_number_rules_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='編號規則資料表';

-- =============================================================================
-- M03 銷售模組（補充）
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 3.1 退貨單資料表 (refunds)
-- -----------------------------------------------------------------------------
CREATE TABLE refunds (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主鍵 ID',
    refund_no VARCHAR(30) NOT NULL UNIQUE COMMENT '退貨單編號',
    order_id BIGINT NOT NULL COMMENT '原訂單 ID',
    order_no VARCHAR(30) NOT NULL COMMENT '原訂單編號',
    store_id BIGINT NOT NULL COMMENT '門市 ID',
    customer_id BIGINT COMMENT '會員 ID',
    refund_date DATE NOT NULL COMMENT '退貨日期',
    refund_time TIME NOT NULL COMMENT '退貨時間',
    subtotal DECIMAL(14,2) NOT NULL DEFAULT 0 COMMENT '退貨小計',
    tax_amount DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '退稅金額',
    total_amount DECIMAL(14,2) NOT NULL DEFAULT 0 COMMENT '退貨總金額',
    refund_method ENUM('CASH', 'CREDIT_CARD', 'ORIGINAL', 'STORE_CREDIT') NOT NULL DEFAULT 'ORIGINAL' COMMENT '退款方式',
    status ENUM('PENDING', 'APPROVED', 'COMPLETED', 'REJECTED') NOT NULL DEFAULT 'PENDING' COMMENT '退貨狀態',
    reason VARCHAR(500) COMMENT '退貨原因',
    notes TEXT COMMENT '備註',
    approved_by BIGINT COMMENT '核准者 ID',
    approved_at DATETIME COMMENT '核准時間',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    created_by BIGINT COMMENT '建立者 ID',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
    updated_by BIGINT COMMENT '更新者 ID',

    INDEX idx_refunds_no (refund_no),
    INDEX idx_refunds_order (order_id),
    INDEX idx_refunds_store (store_id),
    INDEX idx_refunds_customer (customer_id),
    INDEX idx_refunds_date (refund_date),
    INDEX idx_refunds_status (status),

    CONSTRAINT fk_refunds_order FOREIGN KEY (order_id) REFERENCES orders(id),
    CONSTRAINT fk_refunds_store FOREIGN KEY (store_id) REFERENCES stores(id),
    CONSTRAINT fk_refunds_customer FOREIGN KEY (customer_id) REFERENCES customers(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='退貨單資料表';

-- -----------------------------------------------------------------------------
-- 3.2 退貨單明細資料表 (refund_items)
-- -----------------------------------------------------------------------------
CREATE TABLE refund_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主鍵 ID',
    refund_id BIGINT NOT NULL COMMENT '退貨單 ID',
    order_item_id BIGINT COMMENT '原訂單明細 ID',
    product_id BIGINT NOT NULL COMMENT '商品 ID',
    product_name VARCHAR(200) NOT NULL COMMENT '商品名稱（快照）',
    quantity INT NOT NULL COMMENT '退貨數量',
    unit_price DECIMAL(12,2) NOT NULL COMMENT '單價',
    subtotal DECIMAL(12,2) NOT NULL COMMENT '小計',
    reason VARCHAR(200) COMMENT '退貨原因',
    is_restock BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否退回庫存',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',

    INDEX idx_refund_items_refund (refund_id),
    INDEX idx_refund_items_product (product_id),

    CONSTRAINT fk_refund_items_refund FOREIGN KEY (refund_id) REFERENCES refunds(id) ON DELETE CASCADE,
    CONSTRAINT fk_refund_items_order_item FOREIGN KEY (order_item_id) REFERENCES order_items(id),
    CONSTRAINT fk_refund_items_product FOREIGN KEY (product_id) REFERENCES products(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='退貨單明細資料表';

-- -----------------------------------------------------------------------------
-- 3.3 收銀班次資料表 (cashier_shifts)
-- -----------------------------------------------------------------------------
CREATE TABLE cashier_shifts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主鍵 ID',
    shift_no VARCHAR(30) NOT NULL UNIQUE COMMENT '班次編號',
    store_id BIGINT NOT NULL COMMENT '門市 ID',
    cashier_id BIGINT NOT NULL COMMENT '收銀員 ID',
    shift_date DATE NOT NULL COMMENT '班次日期',
    start_time DATETIME NOT NULL COMMENT '開班時間',
    end_time DATETIME COMMENT '結班時間',
    opening_amount DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '開班金額',
    closing_amount DECIMAL(12,2) COMMENT '結班金額',
    expected_amount DECIMAL(12,2) COMMENT '預期金額（系統計算）',
    difference_amount DECIMAL(12,2) COMMENT '差額',
    total_sales DECIMAL(14,2) DEFAULT 0 COMMENT '銷售總額',
    total_refunds DECIMAL(14,2) DEFAULT 0 COMMENT '退貨總額',
    cash_sales DECIMAL(14,2) DEFAULT 0 COMMENT '現金銷售額',
    card_sales DECIMAL(14,2) DEFAULT 0 COMMENT '刷卡銷售額',
    other_sales DECIMAL(14,2) DEFAULT 0 COMMENT '其他銷售額',
    order_count INT DEFAULT 0 COMMENT '訂單數量',
    refund_count INT DEFAULT 0 COMMENT '退貨數量',
    status ENUM('OPEN', 'CLOSED', 'RECONCILED') NOT NULL DEFAULT 'OPEN' COMMENT '班次狀態',
    notes TEXT COMMENT '備註',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    created_by BIGINT COMMENT '建立者 ID',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
    updated_by BIGINT COMMENT '更新者 ID',

    INDEX idx_cashier_shifts_no (shift_no),
    INDEX idx_cashier_shifts_store (store_id),
    INDEX idx_cashier_shifts_cashier (cashier_id),
    INDEX idx_cashier_shifts_date (shift_date),
    INDEX idx_cashier_shifts_status (status),

    CONSTRAINT fk_cashier_shifts_store FOREIGN KEY (store_id) REFERENCES stores(id),
    CONSTRAINT fk_cashier_shifts_cashier FOREIGN KEY (cashier_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收銀班次資料表';

-- -----------------------------------------------------------------------------
-- 3.4 發票資料表 (invoices)
-- -----------------------------------------------------------------------------
CREATE TABLE invoices (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主鍵 ID',
    invoice_no VARCHAR(20) NOT NULL UNIQUE COMMENT '發票號碼',
    invoice_type ENUM('B2C', 'B2B', 'DONATION') NOT NULL DEFAULT 'B2C' COMMENT '發票類型',
    order_id BIGINT COMMENT '關聯訂單 ID',
    refund_id BIGINT COMMENT '關聯退貨單 ID（若為折讓）',
    store_id BIGINT NOT NULL COMMENT '門市 ID',
    invoice_date DATE NOT NULL COMMENT '發票日期',
    buyer_tax_id VARCHAR(20) COMMENT '買受人統一編號',
    buyer_name VARCHAR(100) COMMENT '買受人名稱',
    sales_amount DECIMAL(14,2) NOT NULL COMMENT '銷售額',
    tax_amount DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '稅額',
    total_amount DECIMAL(14,2) NOT NULL COMMENT '總計',
    carrier_type ENUM('MOBILE', 'NATURAL_PERSON', 'NONE') DEFAULT 'NONE' COMMENT '載具類型',
    carrier_no VARCHAR(50) COMMENT '載具號碼',
    donation_code VARCHAR(10) COMMENT '愛心碼',
    status ENUM('ISSUED', 'VOID', 'ALLOWANCE') NOT NULL DEFAULT 'ISSUED' COMMENT '發票狀態',
    void_reason VARCHAR(200) COMMENT '作廢原因',
    void_date DATE COMMENT '作廢日期',
    print_count INT DEFAULT 0 COMMENT '列印次數',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    created_by BIGINT COMMENT '建立者 ID',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
    updated_by BIGINT COMMENT '更新者 ID',

    INDEX idx_invoices_no (invoice_no),
    INDEX idx_invoices_order (order_id),
    INDEX idx_invoices_store (store_id),
    INDEX idx_invoices_date (invoice_date),
    INDEX idx_invoices_status (status),
    INDEX idx_invoices_buyer (buyer_tax_id),

    CONSTRAINT fk_invoices_order FOREIGN KEY (order_id) REFERENCES orders(id),
    CONSTRAINT fk_invoices_refund FOREIGN KEY (refund_id) REFERENCES refunds(id),
    CONSTRAINT fk_invoices_store FOREIGN KEY (store_id) REFERENCES stores(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='發票資料表';

-- =============================================================================
-- M04 庫存模組（補充）
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 4.1 盤點單資料表 (stock_counts)
-- -----------------------------------------------------------------------------
CREATE TABLE stock_counts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主鍵 ID',
    count_no VARCHAR(30) NOT NULL UNIQUE COMMENT '盤點單編號',
    warehouse_id BIGINT NOT NULL COMMENT '倉庫 ID',
    count_date DATE NOT NULL COMMENT '盤點日期',
    count_type ENUM('FULL', 'PARTIAL', 'SPOT') NOT NULL DEFAULT 'PARTIAL' COMMENT '盤點類型',
    status ENUM('DRAFT', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED') NOT NULL DEFAULT 'DRAFT' COMMENT '盤點狀態',
    category_id BIGINT COMMENT '盤點類別（部分盤點用）',
    total_items INT DEFAULT 0 COMMENT '盤點品項數',
    variance_items INT DEFAULT 0 COMMENT '差異品項數',
    variance_amount DECIMAL(14,2) DEFAULT 0 COMMENT '差異金額',
    notes TEXT COMMENT '備註',
    started_at DATETIME COMMENT '開始時間',
    completed_at DATETIME COMMENT '完成時間',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    created_by BIGINT COMMENT '建立者 ID',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
    updated_by BIGINT COMMENT '更新者 ID',

    INDEX idx_stock_counts_no (count_no),
    INDEX idx_stock_counts_warehouse (warehouse_id),
    INDEX idx_stock_counts_date (count_date),
    INDEX idx_stock_counts_status (status),

    CONSTRAINT fk_stock_counts_warehouse FOREIGN KEY (warehouse_id) REFERENCES stores(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='盤點單資料表';

-- -----------------------------------------------------------------------------
-- 4.2 盤點單明細資料表 (stock_count_items)
-- -----------------------------------------------------------------------------
CREATE TABLE stock_count_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主鍵 ID',
    stock_count_id BIGINT NOT NULL COMMENT '盤點單 ID',
    product_id BIGINT NOT NULL COMMENT '商品 ID',
    system_quantity INT NOT NULL COMMENT '系統數量',
    actual_quantity INT COMMENT '實際數量',
    variance_quantity INT COMMENT '差異數量',
    unit_cost DECIMAL(12,2) COMMENT '單位成本',
    variance_amount DECIMAL(12,2) COMMENT '差異金額',
    notes VARCHAR(200) COMMENT '備註',
    counted_by BIGINT COMMENT '盤點人員 ID',
    counted_at DATETIME COMMENT '盤點時間',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',

    INDEX idx_stock_count_items_count (stock_count_id),
    INDEX idx_stock_count_items_product (product_id),

    CONSTRAINT fk_stock_count_items_count FOREIGN KEY (stock_count_id) REFERENCES stock_counts(id) ON DELETE CASCADE,
    CONSTRAINT fk_stock_count_items_product FOREIGN KEY (product_id) REFERENCES products(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='盤點單明細資料表';

-- -----------------------------------------------------------------------------
-- 4.3 調撥單資料表 (stock_transfers)
-- -----------------------------------------------------------------------------
CREATE TABLE stock_transfers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主鍵 ID',
    transfer_no VARCHAR(30) NOT NULL UNIQUE COMMENT '調撥單編號',
    from_warehouse_id BIGINT NOT NULL COMMENT '來源倉庫 ID',
    to_warehouse_id BIGINT NOT NULL COMMENT '目的倉庫 ID',
    transfer_date DATE NOT NULL COMMENT '調撥日期',
    status ENUM('DRAFT', 'PENDING', 'IN_TRANSIT', 'RECEIVED', 'CANCELLED') NOT NULL DEFAULT 'DRAFT' COMMENT '調撥狀態',
    total_items INT DEFAULT 0 COMMENT '品項數',
    total_quantity INT DEFAULT 0 COMMENT '總數量',
    shipped_at DATETIME COMMENT '出庫時間',
    received_at DATETIME COMMENT '入庫時間',
    shipped_by BIGINT COMMENT '出庫人員 ID',
    received_by BIGINT COMMENT '入庫人員 ID',
    notes TEXT COMMENT '備註',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    created_by BIGINT COMMENT '建立者 ID',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
    updated_by BIGINT COMMENT '更新者 ID',

    INDEX idx_stock_transfers_no (transfer_no),
    INDEX idx_stock_transfers_from (from_warehouse_id),
    INDEX idx_stock_transfers_to (to_warehouse_id),
    INDEX idx_stock_transfers_date (transfer_date),
    INDEX idx_stock_transfers_status (status),

    CONSTRAINT fk_stock_transfers_from FOREIGN KEY (from_warehouse_id) REFERENCES stores(id),
    CONSTRAINT fk_stock_transfers_to FOREIGN KEY (to_warehouse_id) REFERENCES stores(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='調撥單資料表';

-- -----------------------------------------------------------------------------
-- 4.4 調撥單明細資料表 (stock_transfer_items)
-- -----------------------------------------------------------------------------
CREATE TABLE stock_transfer_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主鍵 ID',
    transfer_id BIGINT NOT NULL COMMENT '調撥單 ID',
    product_id BIGINT NOT NULL COMMENT '商品 ID',
    quantity INT NOT NULL COMMENT '調撥數量',
    received_quantity INT COMMENT '實收數量',
    unit_cost DECIMAL(12,2) COMMENT '單位成本',
    notes VARCHAR(200) COMMENT '備註',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',

    INDEX idx_stock_transfer_items_transfer (transfer_id),
    INDEX idx_stock_transfer_items_product (product_id),

    CONSTRAINT fk_stock_transfer_items_transfer FOREIGN KEY (transfer_id) REFERENCES stock_transfers(id) ON DELETE CASCADE,
    CONSTRAINT fk_stock_transfer_items_product FOREIGN KEY (product_id) REFERENCES products(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='調撥單明細資料表';

-- -----------------------------------------------------------------------------
-- 4.5 庫存調整單資料表 (stock_adjustments)
-- -----------------------------------------------------------------------------
CREATE TABLE stock_adjustments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主鍵 ID',
    adjustment_no VARCHAR(30) NOT NULL UNIQUE COMMENT '調整單編號',
    warehouse_id BIGINT NOT NULL COMMENT '倉庫 ID',
    product_id BIGINT NOT NULL COMMENT '商品 ID',
    adjustment_type ENUM('IN', 'OUT') NOT NULL COMMENT '調整類型（入庫/出庫）',
    adjustment_reason ENUM('DAMAGE', 'LOSS', 'GIFT', 'SAMPLE', 'ERROR_CORRECTION', 'OTHER') NOT NULL COMMENT '調整原因',
    quantity INT NOT NULL COMMENT '調整數量',
    before_quantity INT NOT NULL COMMENT '調整前數量',
    after_quantity INT NOT NULL COMMENT '調整後數量',
    unit_cost DECIMAL(12,2) COMMENT '單位成本',
    total_cost DECIMAL(14,2) COMMENT '調整金額',
    status ENUM('PENDING', 'APPROVED', 'REJECTED') NOT NULL DEFAULT 'PENDING' COMMENT '狀態',
    reason_detail VARCHAR(500) COMMENT '詳細說明',
    approved_by BIGINT COMMENT '核准者 ID',
    approved_at DATETIME COMMENT '核准時間',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    created_by BIGINT COMMENT '建立者 ID',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
    updated_by BIGINT COMMENT '更新者 ID',

    INDEX idx_stock_adjustments_no (adjustment_no),
    INDEX idx_stock_adjustments_warehouse (warehouse_id),
    INDEX idx_stock_adjustments_product (product_id),
    INDEX idx_stock_adjustments_status (status),
    INDEX idx_stock_adjustments_created (created_at),

    CONSTRAINT fk_stock_adjustments_warehouse FOREIGN KEY (warehouse_id) REFERENCES stores(id),
    CONSTRAINT fk_stock_adjustments_product FOREIGN KEY (product_id) REFERENCES products(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='庫存調整單資料表';

-- =============================================================================
-- M05 採購模組（補充）
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 5.1 採購訂單資料表 (purchase_orders)
-- -----------------------------------------------------------------------------
CREATE TABLE purchase_orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主鍵 ID',
    po_no VARCHAR(30) NOT NULL UNIQUE COMMENT '採購單編號',
    supplier_id BIGINT NOT NULL COMMENT '供應商 ID',
    warehouse_id BIGINT NOT NULL COMMENT '收貨倉庫 ID',
    order_date DATE NOT NULL COMMENT '訂購日期',
    expected_date DATE COMMENT '預計到貨日期',
    status ENUM('DRAFT', 'PENDING', 'APPROVED', 'ORDERED', 'PARTIAL_RECEIVED', 'RECEIVED', 'CANCELLED') NOT NULL DEFAULT 'DRAFT' COMMENT '訂單狀態',
    subtotal DECIMAL(14,2) NOT NULL DEFAULT 0 COMMENT '小計',
    tax_amount DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '稅額',
    total_amount DECIMAL(14,2) NOT NULL DEFAULT 0 COMMENT '總金額',
    paid_amount DECIMAL(14,2) NOT NULL DEFAULT 0 COMMENT '已付金額',
    payment_status ENUM('UNPAID', 'PARTIAL', 'PAID') NOT NULL DEFAULT 'UNPAID' COMMENT '付款狀態',
    total_items INT DEFAULT 0 COMMENT '品項數',
    total_quantity INT DEFAULT 0 COMMENT '訂購總數',
    received_quantity INT DEFAULT 0 COMMENT '已收總數',
    notes TEXT COMMENT '備註',
    approved_by BIGINT COMMENT '核准者 ID',
    approved_at DATETIME COMMENT '核准時間',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    created_by BIGINT COMMENT '建立者 ID',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
    updated_by BIGINT COMMENT '更新者 ID',

    INDEX idx_purchase_orders_no (po_no),
    INDEX idx_purchase_orders_supplier (supplier_id),
    INDEX idx_purchase_orders_warehouse (warehouse_id),
    INDEX idx_purchase_orders_date (order_date),
    INDEX idx_purchase_orders_status (status),
    INDEX idx_purchase_orders_payment (payment_status),

    CONSTRAINT fk_purchase_orders_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers(id),
    CONSTRAINT fk_purchase_orders_warehouse FOREIGN KEY (warehouse_id) REFERENCES stores(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='採購訂單資料表';

-- -----------------------------------------------------------------------------
-- 5.2 採購訂單明細資料表 (purchase_order_items)
-- -----------------------------------------------------------------------------
CREATE TABLE purchase_order_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主鍵 ID',
    purchase_order_id BIGINT NOT NULL COMMENT '採購訂單 ID',
    product_id BIGINT NOT NULL COMMENT '商品 ID',
    product_name VARCHAR(200) NOT NULL COMMENT '商品名稱（快照）',
    quantity INT NOT NULL COMMENT '訂購數量',
    received_quantity INT NOT NULL DEFAULT 0 COMMENT '已收數量',
    unit_price DECIMAL(12,2) NOT NULL COMMENT '單價',
    tax_rate DECIMAL(5,2) DEFAULT 0 COMMENT '稅率',
    tax_amount DECIMAL(12,2) DEFAULT 0 COMMENT '稅額',
    subtotal DECIMAL(12,2) NOT NULL COMMENT '小計',
    notes VARCHAR(200) COMMENT '備註',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',

    INDEX idx_po_items_order (purchase_order_id),
    INDEX idx_po_items_product (product_id),

    CONSTRAINT fk_po_items_order FOREIGN KEY (purchase_order_id) REFERENCES purchase_orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_po_items_product FOREIGN KEY (product_id) REFERENCES products(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='採購訂單明細資料表';

-- -----------------------------------------------------------------------------
-- 5.3 採購收貨單資料表 (purchase_receipts)
-- -----------------------------------------------------------------------------
CREATE TABLE purchase_receipts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主鍵 ID',
    receipt_no VARCHAR(30) NOT NULL UNIQUE COMMENT '收貨單編號',
    purchase_order_id BIGINT NOT NULL COMMENT '採購訂單 ID',
    supplier_id BIGINT NOT NULL COMMENT '供應商 ID',
    warehouse_id BIGINT NOT NULL COMMENT '收貨倉庫 ID',
    receipt_date DATE NOT NULL COMMENT '收貨日期',
    status ENUM('DRAFT', 'RECEIVED', 'INSPECTING', 'COMPLETED', 'REJECTED') NOT NULL DEFAULT 'DRAFT' COMMENT '收貨狀態',
    total_items INT DEFAULT 0 COMMENT '品項數',
    total_quantity INT DEFAULT 0 COMMENT '收貨總數',
    subtotal DECIMAL(14,2) NOT NULL DEFAULT 0 COMMENT '小計',
    tax_amount DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '稅額',
    total_amount DECIMAL(14,2) NOT NULL DEFAULT 0 COMMENT '總金額',
    invoice_no VARCHAR(30) COMMENT '供應商發票號碼',
    notes TEXT COMMENT '備註',
    received_by BIGINT COMMENT '收貨人員 ID',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    created_by BIGINT COMMENT '建立者 ID',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
    updated_by BIGINT COMMENT '更新者 ID',

    INDEX idx_purchase_receipts_no (receipt_no),
    INDEX idx_purchase_receipts_po (purchase_order_id),
    INDEX idx_purchase_receipts_supplier (supplier_id),
    INDEX idx_purchase_receipts_warehouse (warehouse_id),
    INDEX idx_purchase_receipts_date (receipt_date),
    INDEX idx_purchase_receipts_status (status),

    CONSTRAINT fk_purchase_receipts_po FOREIGN KEY (purchase_order_id) REFERENCES purchase_orders(id),
    CONSTRAINT fk_purchase_receipts_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers(id),
    CONSTRAINT fk_purchase_receipts_warehouse FOREIGN KEY (warehouse_id) REFERENCES stores(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='採購收貨單資料表';

-- -----------------------------------------------------------------------------
-- 5.4 採購收貨單明細資料表 (purchase_receipt_items)
-- -----------------------------------------------------------------------------
CREATE TABLE purchase_receipt_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主鍵 ID',
    purchase_receipt_id BIGINT NOT NULL COMMENT '收貨單 ID',
    po_item_id BIGINT COMMENT '採購訂單明細 ID',
    product_id BIGINT NOT NULL COMMENT '商品 ID',
    product_name VARCHAR(200) NOT NULL COMMENT '商品名稱（快照）',
    expected_quantity INT NOT NULL COMMENT '預期數量',
    received_quantity INT NOT NULL COMMENT '實收數量',
    rejected_quantity INT DEFAULT 0 COMMENT '拒收數量',
    unit_price DECIMAL(12,2) NOT NULL COMMENT '單價',
    subtotal DECIMAL(12,2) NOT NULL COMMENT '小計',
    reject_reason VARCHAR(200) COMMENT '拒收原因',
    notes VARCHAR(200) COMMENT '備註',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',

    INDEX idx_pr_items_receipt (purchase_receipt_id),
    INDEX idx_pr_items_po_item (po_item_id),
    INDEX idx_pr_items_product (product_id),

    CONSTRAINT fk_pr_items_receipt FOREIGN KEY (purchase_receipt_id) REFERENCES purchase_receipts(id) ON DELETE CASCADE,
    CONSTRAINT fk_pr_items_po_item FOREIGN KEY (po_item_id) REFERENCES purchase_order_items(id),
    CONSTRAINT fk_pr_items_product FOREIGN KEY (product_id) REFERENCES products(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='採購收貨單明細資料表';

-- -----------------------------------------------------------------------------
-- 5.5 採購退貨單資料表 (purchase_returns)
-- -----------------------------------------------------------------------------
CREATE TABLE purchase_returns (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主鍵 ID',
    return_no VARCHAR(30) NOT NULL UNIQUE COMMENT '退貨單編號',
    purchase_receipt_id BIGINT COMMENT '採購收貨單 ID',
    supplier_id BIGINT NOT NULL COMMENT '供應商 ID',
    warehouse_id BIGINT NOT NULL COMMENT '出貨倉庫 ID',
    return_date DATE NOT NULL COMMENT '退貨日期',
    status ENUM('DRAFT', 'PENDING', 'APPROVED', 'SHIPPED', 'COMPLETED', 'REJECTED') NOT NULL DEFAULT 'DRAFT' COMMENT '退貨狀態',
    total_items INT DEFAULT 0 COMMENT '品項數',
    total_quantity INT DEFAULT 0 COMMENT '退貨總數',
    subtotal DECIMAL(14,2) NOT NULL DEFAULT 0 COMMENT '小計',
    tax_amount DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '稅額',
    total_amount DECIMAL(14,2) NOT NULL DEFAULT 0 COMMENT '總金額',
    reason VARCHAR(500) COMMENT '退貨原因',
    notes TEXT COMMENT '備註',
    approved_by BIGINT COMMENT '核准者 ID',
    approved_at DATETIME COMMENT '核准時間',
    shipped_at DATETIME COMMENT '出貨時間',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    created_by BIGINT COMMENT '建立者 ID',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
    updated_by BIGINT COMMENT '更新者 ID',

    INDEX idx_purchase_returns_no (return_no),
    INDEX idx_purchase_returns_receipt (purchase_receipt_id),
    INDEX idx_purchase_returns_supplier (supplier_id),
    INDEX idx_purchase_returns_warehouse (warehouse_id),
    INDEX idx_purchase_returns_date (return_date),
    INDEX idx_purchase_returns_status (status),

    CONSTRAINT fk_purchase_returns_receipt FOREIGN KEY (purchase_receipt_id) REFERENCES purchase_receipts(id),
    CONSTRAINT fk_purchase_returns_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers(id),
    CONSTRAINT fk_purchase_returns_warehouse FOREIGN KEY (warehouse_id) REFERENCES stores(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='採購退貨單資料表';

-- -----------------------------------------------------------------------------
-- 5.6 採購退貨單明細資料表 (purchase_return_items)
-- -----------------------------------------------------------------------------
CREATE TABLE purchase_return_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主鍵 ID',
    purchase_return_id BIGINT NOT NULL COMMENT '退貨單 ID',
    pr_item_id BIGINT COMMENT '收貨單明細 ID',
    product_id BIGINT NOT NULL COMMENT '商品 ID',
    product_name VARCHAR(200) NOT NULL COMMENT '商品名稱（快照）',
    quantity INT NOT NULL COMMENT '退貨數量',
    unit_price DECIMAL(12,2) NOT NULL COMMENT '單價',
    subtotal DECIMAL(12,2) NOT NULL COMMENT '小計',
    reason VARCHAR(200) COMMENT '退貨原因',
    notes VARCHAR(200) COMMENT '備註',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',

    INDEX idx_pret_items_return (purchase_return_id),
    INDEX idx_pret_items_pr_item (pr_item_id),
    INDEX idx_pret_items_product (product_id),

    CONSTRAINT fk_pret_items_return FOREIGN KEY (purchase_return_id) REFERENCES purchase_returns(id) ON DELETE CASCADE,
    CONSTRAINT fk_pret_items_pr_item FOREIGN KEY (pr_item_id) REFERENCES purchase_receipt_items(id),
    CONSTRAINT fk_pret_items_product FOREIGN KEY (product_id) REFERENCES products(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='採購退貨單明細資料表';

-- =============================================================================
-- 初始化資料
-- =============================================================================

-- 新增系統角色
INSERT INTO roles (code, name, description, is_system, permissions, sort_order) VALUES
('PURCHASER', '採購人員', '負責採購相關作業', TRUE, '["purchasing:read", "purchasing:write", "supplier:read", "product:read", "inventory:read"]', 4),
('VIEWER', '檢視者', '僅有讀取權限', TRUE, '["*:read"]', 5)
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- 新增編號規則
INSERT INTO number_rules (rule_code, rule_name, prefix, date_format, sequence_length, reset_period, is_active) VALUES
('ORDER', '訂單編號', 'ORD', 'yyyyMMdd', 4, 'DAILY', TRUE),
('REFUND', '退貨單編號', 'REF', 'yyyyMMdd', 4, 'DAILY', TRUE),
('PURCHASE_ORDER', '採購單編號', 'PO', 'yyyyMMdd', 4, 'DAILY', TRUE),
('PURCHASE_RECEIPT', '收貨單編號', 'PR', 'yyyyMMdd', 4, 'DAILY', TRUE),
('PURCHASE_RETURN', '採購退貨編號', 'RET', 'yyyyMMdd', 4, 'DAILY', TRUE),
('STOCK_COUNT', '盤點單編號', 'SC', 'yyyyMMdd', 4, 'DAILY', TRUE),
('STOCK_TRANSFER', '調撥單編號', 'ST', 'yyyyMMdd', 4, 'DAILY', TRUE),
('STOCK_ADJUSTMENT', '調整單編號', 'SA', 'yyyyMMdd', 4, 'DAILY', TRUE),
('CASHIER_SHIFT', '班次編號', 'SH', 'yyyyMMdd', 4, 'DAILY', TRUE),
('INVOICE', '發票編號', '', 'yyyyMM', 8, 'MONTHLY', TRUE);

-- 新增系統參數
INSERT INTO system_parameters (category, param_key, param_value, param_type, description, is_system, sort_order) VALUES
('GENERAL', 'COMPANY_NAME', '零售業簡易ERP系統', 'STRING', '公司名稱', TRUE, 1),
('GENERAL', 'COMPANY_ADDRESS', '', 'STRING', '公司地址', TRUE, 2),
('GENERAL', 'COMPANY_PHONE', '', 'STRING', '公司電話', TRUE, 3),
('GENERAL', 'COMPANY_TAX_ID', '', 'STRING', '統一編號', TRUE, 4),
('SALES', 'DEFAULT_TAX_RATE', '0.05', 'NUMBER', '預設稅率 (5%)', TRUE, 10),
('SALES', 'POINTS_RATE', '0.01', 'NUMBER', '點數累積比例 (消費1元=0.01點)', TRUE, 11),
('SALES', 'POINTS_VALUE', '1', 'NUMBER', '每點折抵金額', TRUE, 12),
('INVENTORY', 'LOW_STOCK_THRESHOLD', '10', 'NUMBER', '庫存警示門檻', TRUE, 20),
('INVENTORY', 'ALLOW_NEGATIVE_STOCK', 'false', 'BOOLEAN', '是否允許負庫存', TRUE, 21),
('PURCHASING', 'DEFAULT_PAYMENT_TERMS', 'NET30', 'STRING', '預設付款條件', TRUE, 30),
('PURCHASING', 'AUTO_APPROVE_THRESHOLD', '10000', 'NUMBER', '自動核准金額門檻', TRUE, 31);
