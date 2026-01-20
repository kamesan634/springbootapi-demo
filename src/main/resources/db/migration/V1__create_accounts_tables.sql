-- =============================================================================
-- 零售業簡易ERP系統 - Flyway Migration V1
-- 建立帳號模組資料表
-- =============================================================================
-- 版本: V1
-- 建立日期: 2025-01-01
-- 說明: 建立角色、使用者、門市/倉庫等基礎資料表
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. 角色資料表 (roles)
-- -----------------------------------------------------------------------------
CREATE TABLE roles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主鍵 ID',
    code VARCHAR(50) NOT NULL UNIQUE COMMENT '角色代碼',
    name VARCHAR(100) NOT NULL COMMENT '角色名稱',
    description VARCHAR(500) COMMENT '角色描述',
    is_system BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否為系統角色',
    is_active BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否啟用',
    sort_order INT DEFAULT 0 COMMENT '排序順序',
    permissions JSON COMMENT '權限列表 (JSON)',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    created_by BIGINT COMMENT '建立者 ID',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
    updated_by BIGINT COMMENT '更新者 ID',

    INDEX idx_roles_code (code),
    INDEX idx_roles_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色資料表';

-- -----------------------------------------------------------------------------
-- 2. 門市/倉庫資料表 (stores)
-- -----------------------------------------------------------------------------
CREATE TABLE stores (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主鍵 ID',
    code VARCHAR(20) NOT NULL UNIQUE COMMENT '門市/倉庫代碼',
    name VARCHAR(100) NOT NULL COMMENT '門市/倉庫名稱',
    type ENUM('STORE', 'WAREHOUSE') NOT NULL COMMENT '類型: STORE=門市, WAREHOUSE=倉庫',
    address VARCHAR(500) COMMENT '地址',
    phone VARCHAR(20) COMMENT '電話',
    email VARCHAR(100) COMMENT 'Email',
    manager_id BIGINT COMMENT '負責人 ID',
    business_hours VARCHAR(50) COMMENT '營業時間',
    is_main BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否為總部/主倉庫',
    is_active BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否啟用',
    sort_order INT DEFAULT 0 COMMENT '排序順序',
    notes VARCHAR(500) COMMENT '備註',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    created_by BIGINT COMMENT '建立者 ID',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
    updated_by BIGINT COMMENT '更新者 ID',

    INDEX idx_stores_code (code),
    INDEX idx_stores_type (type),
    INDEX idx_stores_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='門市/倉庫資料表';

-- -----------------------------------------------------------------------------
-- 3. 使用者資料表 (users)
-- -----------------------------------------------------------------------------
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主鍵 ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '使用者名稱（帳號）',
    password VARCHAR(255) NOT NULL COMMENT '密碼（BCrypt 加密）',
    name VARCHAR(100) NOT NULL COMMENT '姓名',
    email VARCHAR(100) UNIQUE COMMENT 'Email',
    phone VARCHAR(20) COMMENT '手機號碼',
    role_id BIGINT NOT NULL COMMENT '角色 ID',
    avatar VARCHAR(255) COMMENT '頭像 URL',
    is_active BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否啟用',
    last_login_at DATETIME COMMENT '最後登入時間',
    last_login_ip VARCHAR(50) COMMENT '最後登入 IP',
    login_fail_count INT DEFAULT 0 COMMENT '登入失敗次數',
    locked_until DATETIME COMMENT '帳號鎖定時間',
    password_changed_at DATETIME COMMENT '密碼最後修改時間',
    notes VARCHAR(500) COMMENT '備註',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    created_by BIGINT COMMENT '建立者 ID',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
    updated_by BIGINT COMMENT '更新者 ID',

    INDEX idx_users_username (username),
    INDEX idx_users_email (email),
    INDEX idx_users_role (role_id),
    INDEX idx_users_active (is_active),

    CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='使用者資料表';

-- -----------------------------------------------------------------------------
-- 4. 使用者門市關聯表 (user_stores)
-- -----------------------------------------------------------------------------
CREATE TABLE user_stores (
    user_id BIGINT NOT NULL COMMENT '使用者 ID',
    store_id BIGINT NOT NULL COMMENT '門市 ID',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',

    PRIMARY KEY (user_id, store_id),

    CONSTRAINT fk_user_stores_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_stores_store FOREIGN KEY (store_id) REFERENCES stores(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='使用者門市關聯表';

-- 更新 stores 表的外鍵
ALTER TABLE stores ADD CONSTRAINT fk_stores_manager FOREIGN KEY (manager_id) REFERENCES users(id);
