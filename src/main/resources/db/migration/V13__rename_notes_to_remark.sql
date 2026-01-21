-- =============================================================================
-- V13: 重新命名 inventory_movements 表的 notes 欄位為 remark
-- =============================================================================

ALTER TABLE inventory_movements
CHANGE COLUMN notes remark VARCHAR(500) COMMENT '備註';
