-- =============================================================================
-- V8__fix_datetime_columns.sql
-- 修正日期時間欄位類型
-- =============================================================================
-- 說明：將 DATE 類型的欄位改為 DATETIME，以配合 Java LocalDateTime 類型

-- 修改 promotions 表
ALTER TABLE promotions
    MODIFY COLUMN start_date DATETIME NOT NULL COMMENT '開始日期時間',
    MODIFY COLUMN end_date DATETIME NOT NULL COMMENT '結束日期時間';

-- 修改 coupons 表
ALTER TABLE coupons
    MODIFY COLUMN start_date DATETIME NOT NULL COMMENT '開始日期時間',
    MODIFY COLUMN end_date DATETIME NOT NULL COMMENT '結束日期時間';
