package com.kamesan.erpapi.sales.dto;

import com.kamesan.erpapi.sales.entity.Refund;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

/**
 * 建立退貨單請求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateRefundRequest {

    @NotNull(message = "原訂單 ID 不可為空")
    private Long orderId;

    @NotNull(message = "門市 ID 不可為空")
    private Long storeId;

    private Refund.RefundMethod refundMethod;

    @Size(max = 500, message = "退貨原因長度不可超過 500 字元")
    private String reason;

    private String notes;

    @NotNull(message = "退貨明細不可為空")
    @Size(min = 1, message = "至少需要一筆退貨明細")
    private List<CreateRefundItemRequest> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateRefundItemRequest {
        private Long orderItemId;

        @NotNull(message = "商品 ID 不可為空")
        private Long productId;

        @NotNull(message = "商品名稱不可為空")
        private String productName;

        @NotNull(message = "數量不可為空")
        private Integer quantity;

        @NotNull(message = "單價不可為空")
        private java.math.BigDecimal unitPrice;

        private String reason;

        private Boolean isRestock;
    }
}
