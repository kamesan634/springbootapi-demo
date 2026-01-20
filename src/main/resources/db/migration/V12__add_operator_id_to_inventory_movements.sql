-- =============================================================================
-- V12: 新增 operator_id 欄位到 inventory_movements 表
-- =============================================================================

ALTER TABLE inventory_movements
ADD COLUMN operator_id BIGINT COMMENT '操作人員 ID' AFTER reference_no;

-- 新增索引
CREATE INDEX idx_inventory_movements_operator ON inventory_movements(operator_id);
