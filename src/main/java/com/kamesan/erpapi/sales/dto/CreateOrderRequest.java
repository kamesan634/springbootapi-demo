package com.kamesan.erpapi.sales.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 建立訂單請求 DTO
 *
 * <p>用於接收建立新訂單的 API 請求資料。</p>
 *
 * <p>驗證規則：</p>
 * <ul>
 *   <li>門市 ID：必填</li>
 *   <li>訂單明細：必填，至少一項</li>
 *   <li>金額：必須大於等於 0</li>
 * </ul>
 *
 * <p>使用範例：</p>
 * <pre>
 * {
 *   "storeId": 1,
 *   "customerId": 100,
 *   "items": [
 *     {
 *       "productId": 1,
 *       "quantity": 2,
 *       "unitPrice": 750.00,
 *       "discountAmount": 50.00
 *     }
 *   ],
 *   "discountAmount": 100.00,
 *   "notes": "請盡快出貨"
 * }
 * </pre>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "建立訂單請求")
public class CreateOrderRequest {

    /**
     * 門市 ID
     * <p>必填，訂單所屬門市</p>
     */
    @NotNull(message = "門市 ID 不能為空")
    @Schema(description = "門市 ID", example = "1", required = true)
    private Long storeId;

    /**
     * 客戶 ID
     * <p>選填，非會員購買可不填</p>
     */
    @Schema(description = "客戶 ID（非會員可不填）", example = "100")
    private Long customerId;

    /**
     * 訂單明細項目列表
     * <p>必填，至少包含一個明細項目</p>
     */
    @NotEmpty(message = "訂單明細不能為空")
    @Valid
    @Schema(description = "訂單明細項目列表", required = true)
    private List<CreateOrderItemRequest> items;

    /**
     * 訂單折扣金額
     * <p>選填，預設為 0</p>
     */
    @DecimalMin(value = "0", message = "折扣金額不能為負數")
    @Schema(description = "訂單折扣金額", example = "100.00")
    private BigDecimal discountAmount;

    /**
     * 稅額
     * <p>選填，預設為 0</p>
     */
    @DecimalMin(value = "0", message = "稅額不能為負數")
    @Schema(description = "稅額", example = "70.00")
    private BigDecimal taxAmount;

    /**
     * 備註
     */
    @Size(max = 500, message = "備註不能超過 500 字")
    @Schema(description = "備註", example = "請盡快出貨")
    private String notes;

    /**
     * 建立訂單明細項目請求
     *
     * <p>用於接收單一訂單明細的請求資料</p>
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "訂單明細項目請求")
    public static class CreateOrderItemRequest {

        /**
         * 商品 ID
         * <p>必填</p>
         */
        @NotNull(message = "商品 ID 不能為空")
        @Schema(description = "商品 ID", example = "100", required = true)
        private Long productId;

        /**
         * 購買數量
         * <p>必填，必須大於 0</p>
         */
        @NotNull(message = "購買數量不能為空")
        @Min(value = 1, message = "購買數量必須大於 0")
        @Schema(description = "購買數量", example = "2", required = true)
        private Integer quantity;

        /**
         * 商品單價
         * <p>必填，必須大於 0</p>
         */
        @NotNull(message = "商品單價不能為空")
        @DecimalMin(value = "0.01", message = "商品單價必須大於 0")
        @Schema(description = "商品單價", example = "750.00", required = true)
        private BigDecimal unitPrice;

        /**
         * 折扣金額
         * <p>選填，預設為 0</p>
         */
        @DecimalMin(value = "0", message = "折扣金額不能為負數")
        @Schema(description = "折扣金額", example = "50.00")
        private BigDecimal discountAmount;
    }
}
