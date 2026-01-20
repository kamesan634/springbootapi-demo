package com.kamesan.erpapi.products.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 更新商品請求 DTO
 *
 * <p>用於更新商品的請求資料傳輸物件。</p>
 *
 * <h2>必填欄位：</h2>
 * <ul>
 *   <li>sku - 商品貨號</li>
 *   <li>name - 商品名稱</li>
 *   <li>sellingPrice - 售價</li>
 * </ul>
 *
 * <h2>選填欄位：</h2>
 * <ul>
 *   <li>description - 商品描述</li>
 *   <li>categoryId - 分類 ID</li>
 *   <li>unitId - 單位 ID</li>
 *   <li>taxTypeId - 稅別 ID</li>
 *   <li>costPrice - 成本價</li>
 *   <li>barcode - 主要條碼</li>
 *   <li>safetyStock - 安全庫存量</li>
 *   <li>active - 是否啟用</li>
 *   <li>barcodes - 條碼列表（會完全取代現有條碼）</li>
 * </ul>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "更新商品請求")
public class UpdateProductRequest {

    /**
     * 商品貨號（SKU）
     * <p>必須唯一，長度限制 1-50 字元</p>
     */
    @NotBlank(message = "商品貨號不能為空")
    @Size(max = 50, message = "商品貨號長度不能超過 50 字元")
    @Schema(description = "商品貨號（SKU）", example = "SKU001", requiredMode = Schema.RequiredMode.REQUIRED)
    private String sku;

    /**
     * 商品名稱
     * <p>長度限制 1-200 字元</p>
     */
    @NotBlank(message = "商品名稱不能為空")
    @Size(max = 200, message = "商品名稱長度不能超過 200 字元")
    @Schema(description = "商品名稱", example = "咖啡豆 250g", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    /**
     * 商品描述
     */
    @Schema(description = "商品描述", example = "精選阿拉比卡咖啡豆")
    private String description;

    /**
     * 分類 ID
     */
    @Schema(description = "分類 ID", example = "1")
    private Long categoryId;

    /**
     * 單位 ID
     */
    @Schema(description = "單位 ID", example = "1")
    private Long unitId;

    /**
     * 稅別 ID
     */
    @Schema(description = "稅別 ID", example = "1")
    private Long taxTypeId;

    /**
     * 成本價
     * <p>必須大於或等於 0</p>
     */
    @DecimalMin(value = "0", message = "成本價不能為負數")
    @Digits(integer = 10, fraction = 2, message = "成本價格式不正確")
    @Schema(description = "成本價", example = "150.00")
    private BigDecimal costPrice;

    /**
     * 售價
     * <p>必須大於或等於 0</p>
     */
    @NotNull(message = "售價不能為空")
    @DecimalMin(value = "0", message = "售價不能為負數")
    @Digits(integer = 10, fraction = 2, message = "售價格式不正確")
    @Schema(description = "售價", example = "250.00", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal sellingPrice;

    /**
     * 主要條碼
     * <p>長度限制最大 50 字元</p>
     */
    @Size(max = 50, message = "條碼長度不能超過 50 字元")
    @Schema(description = "主要條碼", example = "4710088123456")
    private String barcode;

    /**
     * 安全庫存量
     * <p>必須大於或等於 0</p>
     */
    @Min(value = 0, message = "安全庫存量不能為負數")
    @Schema(description = "安全庫存量", example = "10")
    private Integer safetyStock;

    /**
     * 是否啟用
     */
    @Schema(description = "是否啟用", example = "true")
    private Boolean active;

    /**
     * 條碼列表
     * <p>更新時會完全取代現有的條碼列表</p>
     * <p>若要保留現有條碼，需要在此列表中包含它們</p>
     */
    @Schema(description = "條碼列表")
    private List<BarcodeRequest> barcodes;

    /**
     * 條碼請求內嵌類別
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "條碼請求")
    public static class BarcodeRequest {
        /**
         * 條碼 ID
         * <p>更新現有條碼時使用，新增條碼時不需要</p>
         */
        @Schema(description = "條碼 ID（更新現有條碼時使用）", example = "1")
        private Long id;

        /**
         * 條碼號碼
         */
        @NotBlank(message = "條碼不能為空")
        @Size(max = 50, message = "條碼長度不能超過 50 字元")
        @Schema(description = "條碼號碼", example = "4710088123457")
        private String barcode;

        /**
         * 是否為主要條碼
         */
        @Schema(description = "是否為主要條碼", example = "false")
        private Boolean primary;

        /**
         * 條碼類型
         */
        @Size(max = 20, message = "條碼類型長度不能超過 20 字元")
        @Schema(description = "條碼類型", example = "EAN13")
        private String barcodeType;

        /**
         * 備註
         */
        @Size(max = 200, message = "備註長度不能超過 200 字元")
        @Schema(description = "備註", example = "包裝條碼")
        private String notes;
    }
}
