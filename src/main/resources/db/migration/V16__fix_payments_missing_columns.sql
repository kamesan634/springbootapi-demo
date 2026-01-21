-- =============================================================================
-- V16: 修復 payments 表缺少的欄位
-- =============================================================================

ALTER TABLE payments
ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間' AFTER created_by,
ADD COLUMN updated_by BIGINT COMMENT '更新者 ID' AFTER updated_at;
