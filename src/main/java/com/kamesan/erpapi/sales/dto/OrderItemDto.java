package com.kamesan.erpapi.sales.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 訂單明細 DTO（資料傳輸物件）
 *
 * <p>用於 API 回應的訂單明細資料結構。</p>
 *
 * <p>主要用途：</p>
 * <ul>
 *   <li>訂單詳情中的明細項目</li>
 *   <li>建立訂單時的明細資料</li>
 * </ul>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "訂單明細資料")
public class OrderItemDto {

    /**
     * 明細 ID
     */
    @Schema(description = "明細 ID", example = "1")
    private Long id;

    /**
     * 所屬訂單 ID
     */
    @Schema(description = "所屬訂單 ID", example = "1")
    private Long orderId;

    /**
     * 商品 ID
     */
    @Schema(description = "商品 ID", example = "100")
    private Long productId;

    /**
     * 商品編號
     * <p>從商品資料表查詢取得</p>
     */
    @Schema(description = "商品編號", example = "SKU-001")
    private String productCode;

    /**
     * 商品名稱
     * <p>從商品資料表查詢取得</p>
     */
    @Schema(description = "商品名稱", example = "無線藍牙耳機")
    private String productName;

    /**
     * 購買數量
     */
    @Schema(description = "購買數量", example = "2")
    private Integer quantity;

    /**
     * 商品單價
     */
    @Schema(description = "商品單價", example = "750.00")
    private BigDecimal unitPrice;

    /**
     * 折扣金額
     */
    @Schema(description = "折扣金額", example = "50.00")
    private BigDecimal discountAmount;

    /**
     * 小計金額
     */
    @Schema(description = "小計金額", example = "1450.00")
    private BigDecimal subtotal;

    /**
     * 原價總額（未扣折扣）
     */
    @Schema(description = "原價總額（未扣折扣）", example = "1500.00")
    private BigDecimal originalTotal;
}
