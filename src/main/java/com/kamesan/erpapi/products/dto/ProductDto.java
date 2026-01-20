package com.kamesan.erpapi.products.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品 DTO
 *
 * <p>用於 API 回應的商品資料傳輸物件，包含商品的完整資訊。</p>
 *
 * <h2>包含的關聯資訊：</h2>
 * <ul>
 *   <li>category - 分類資訊（ID、代碼、名稱）</li>
 *   <li>unit - 單位資訊（ID、代碼、名稱）</li>
 *   <li>taxType - 稅別資訊（ID、代碼、名稱、稅率）</li>
 *   <li>barcodes - 條碼列表</li>
 * </ul>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "商品資料")
public class ProductDto {

    /**
     * 商品 ID
     */
    @Schema(description = "商品 ID", example = "1")
    private Long id;

    /**
     * 商品貨號（SKU）
     */
    @Schema(description = "商品貨號（SKU）", example = "SKU001")
    private String sku;

    /**
     * 商品名稱
     */
    @Schema(description = "商品名稱", example = "咖啡豆 250g")
    private String name;

    /**
     * 商品描述
     */
    @Schema(description = "商品描述", example = "精選阿拉比卡咖啡豆")
    private String description;

    /**
     * 分類資訊
     */
    @Schema(description = "分類資訊")
    private CategoryInfo category;

    /**
     * 單位資訊
     */
    @Schema(description = "單位資訊")
    private UnitInfo unit;

    /**
     * 稅別資訊
     */
    @Schema(description = "稅別資訊")
    private TaxTypeInfo taxType;

    /**
     * 成本價
     */
    @Schema(description = "成本價", example = "150.00")
    private BigDecimal costPrice;

    /**
     * 售價
     */
    @Schema(description = "售價", example = "250.00")
    private BigDecimal sellingPrice;

    /**
     * 含稅售價
     */
    @Schema(description = "含稅售價", example = "262.50")
    private BigDecimal taxIncludedPrice;

    /**
     * 毛利
     */
    @Schema(description = "毛利", example = "100.00")
    private BigDecimal grossProfit;

    /**
     * 毛利率（百分比）
     */
    @Schema(description = "毛利率（百分比）", example = "40.00")
    private BigDecimal grossProfitMargin;

    /**
     * 主要條碼
     */
    @Schema(description = "主要條碼", example = "4710088123456")
    private String barcode;

    /**
     * 安全庫存量
     */
    @Schema(description = "安全庫存量", example = "10")
    private Integer safetyStock;

    /**
     * 是否啟用
     */
    @Schema(description = "是否啟用", example = "true")
    private boolean active;

    /**
     * 條碼列表
     */
    @Schema(description = "條碼列表")
    private List<BarcodeInfo> barcodes;

    /**
     * 建立時間
     */
    @Schema(description = "建立時間")
    private LocalDateTime createdAt;

    /**
     * 更新時間
     */
    @Schema(description = "更新時間")
    private LocalDateTime updatedAt;

    /**
     * 分類資訊內嵌類別
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "分類資訊")
    public static class CategoryInfo {
        /**
         * 分類 ID
         */
        @Schema(description = "分類 ID", example = "1")
        private Long id;

        /**
         * 分類代碼
         */
        @Schema(description = "分類代碼", example = "BEVERAGE")
        private String code;

        /**
         * 分類名稱
         */
        @Schema(description = "分類名稱", example = "飲料")
        private String name;

        /**
         * 完整路徑名稱
         */
        @Schema(description = "完整路徑名稱", example = "食品 > 飲料")
        private String fullPathName;
    }

    /**
     * 單位資訊內嵌類別
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "單位資訊")
    public static class UnitInfo {
        /**
         * 單位 ID
         */
        @Schema(description = "單位 ID", example = "1")
        private Long id;

        /**
         * 單位代碼
         */
        @Schema(description = "單位代碼", example = "PCS")
        private String code;

        /**
         * 單位名稱
         */
        @Schema(description = "單位名稱", example = "個")
        private String name;
    }

    /**
     * 稅別資訊內嵌類別
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "稅別資訊")
    public static class TaxTypeInfo {
        /**
         * 稅別 ID
         */
        @Schema(description = "稅別 ID", example = "1")
        private Long id;

        /**
         * 稅別代碼
         */
        @Schema(description = "稅別代碼", example = "TAX")
        private String code;

        /**
         * 稅別名稱
         */
        @Schema(description = "稅別名稱", example = "應稅")
        private String name;

        /**
         * 稅率
         */
        @Schema(description = "稅率（百分比）", example = "5.00")
        private BigDecimal rate;
    }

    /**
     * 條碼資訊內嵌類別
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "條碼資訊")
    public static class BarcodeInfo {
        /**
         * 條碼 ID
         */
        @Schema(description = "條碼 ID", example = "1")
        private Long id;

        /**
         * 條碼號碼
         */
        @Schema(description = "條碼號碼", example = "4710088123456")
        private String barcode;

        /**
         * 是否為主要條碼
         */
        @Schema(description = "是否為主要條碼", example = "true")
        private boolean primary;

        /**
         * 條碼類型
         */
        @Schema(description = "條碼類型", example = "EAN13")
        private String barcodeType;

        /**
         * 備註
         */
        @Schema(description = "備註", example = "原廠條碼")
        private String notes;
    }
}
