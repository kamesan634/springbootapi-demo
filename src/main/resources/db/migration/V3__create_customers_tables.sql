-- =============================================================================
-- 零售業簡易ERP系統 - Flyway Migration V3
-- 建立客戶會員模組資料表
-- =============================================================================
-- 版本: V3
-- 建立日期: 2025-01-01
-- 說明: 建立會員等級、會員資料表
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. 會員等級資料表 (customer_levels)
-- -----------------------------------------------------------------------------
CREATE TABLE customer_levels (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主鍵 ID',
    code VARCHAR(20) NOT NULL UNIQUE COMMENT '等級代碼',
    name VARCHAR(50) NOT NULL COMMENT '等級名稱',
    description VARCHAR(500) COMMENT '等級描述',
    discount_rate DECIMAL(5,2) NOT NULL DEFAULT 0 COMMENT '折扣率 (%)',
    points_multiplier DECIMAL(3,1) NOT NULL DEFAULT 1.0 COMMENT '點數倍率',
    upgrade_condition DECIMAL(12,2) DEFAULT 0 COMMENT '升級條件（累計消費金額）',
    is_default BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否為預設等級',
    is_active BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否啟用',
    sort_order INT DEFAULT 0 COMMENT '排序順序',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    created_by BIGINT COMMENT '建立者 ID',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
    updated_by BIGINT COMMENT '更新者 ID',

    INDEX idx_customer_levels_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='會員等級資料表';

-- -----------------------------------------------------------------------------
-- 2. 會員資料表 (customers)
-- -----------------------------------------------------------------------------
CREATE TABLE customers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主鍵 ID',
    member_no VARCHAR(20) NOT NULL UNIQUE COMMENT '會員編號',
    name VARCHAR(100) NOT NULL COMMENT '姓名',
    phone VARCHAR(20) UNIQUE COMMENT '手機號碼',
    email VARCHAR(100) UNIQUE COMMENT 'Email',
    gender ENUM('MALE', 'FEMALE', 'OTHER') COMMENT '性別',
    birthday DATE COMMENT '生日',
    level_id BIGINT NOT NULL COMMENT '會員等級 ID',
    total_points INT NOT NULL DEFAULT 0 COMMENT '目前點數',
    total_spent DECIMAL(14,2) NOT NULL DEFAULT 0 COMMENT '累計消費金額',
    register_date DATE NOT NULL COMMENT '註冊日期',
    address VARCHAR(500) COMMENT '地址',
    notes TEXT COMMENT '備註',
    is_active BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否啟用',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    created_by BIGINT COMMENT '建立者 ID',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
    updated_by BIGINT COMMENT '更新者 ID',

    INDEX idx_customers_member_no (member_no),
    INDEX idx_customers_phone (phone),
    INDEX idx_customers_email (email),
    INDEX idx_customers_level (level_id),
    INDEX idx_customers_active (is_active),

    CONSTRAINT fk_customers_level FOREIGN KEY (level_id) REFERENCES customer_levels(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='會員資料表';

-- -----------------------------------------------------------------------------
-- 3. 會員點數記錄表 (customer_points_log)
-- -----------------------------------------------------------------------------
CREATE TABLE customer_points_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主鍵 ID',
    customer_id BIGINT NOT NULL COMMENT '會員 ID',
    type ENUM('EARN', 'REDEEM', 'ADJUST', 'EXPIRE') NOT NULL COMMENT '異動類型',
    points INT NOT NULL COMMENT '點數（正數增加，負數減少）',
    balance INT NOT NULL COMMENT '異動後餘額',
    reference_type VARCHAR(50) COMMENT '關聯類型（如：ORDER）',
    reference_id BIGINT COMMENT '關聯 ID',
    description VARCHAR(200) COMMENT '說明',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    created_by BIGINT COMMENT '建立者 ID',

    INDEX idx_points_log_customer (customer_id),
    INDEX idx_points_log_created (created_at),

    CONSTRAINT fk_points_log_customer FOREIGN KEY (customer_id) REFERENCES customers(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='會員點數記錄表';
