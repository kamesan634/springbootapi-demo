-- =============================================================================
-- V15: 修復 order_items 表缺少的欄位
-- =============================================================================

ALTER TABLE order_items
ADD COLUMN created_by BIGINT COMMENT '建立者 ID' AFTER created_at,
ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間' AFTER created_by,
ADD COLUMN updated_by BIGINT COMMENT '更新者 ID' AFTER updated_at;
