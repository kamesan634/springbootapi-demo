-- =============================================================================
-- 零售業簡易ERP系統 - Flyway Migration V11
-- 新增商品條碼類型欄位
-- =============================================================================
-- 版本: V11
-- 建立日期: 2025-01-20
-- 說明: 在 product_barcodes 表中新增 barcode_type 和 notes 欄位
-- =============================================================================

-- 新增 barcode_type 欄位
ALTER TABLE product_barcodes
    ADD COLUMN barcode_type VARCHAR(20) NULL COMMENT '條碼類型（如：EAN13、UPC、CODE128）' AFTER is_primary;

-- 新增 notes 欄位
ALTER TABLE product_barcodes
    ADD COLUMN notes VARCHAR(200) NULL COMMENT '備註說明' AFTER barcode_type;
