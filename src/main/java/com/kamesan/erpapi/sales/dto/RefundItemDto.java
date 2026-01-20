package com.kamesan.erpapi.sales.dto;

import lombok.*;

import java.math.BigDecimal;

/**
 * 退貨單明細 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefundItemDto {
    private Long id;
    private Long refundId;
    private Long orderItemId;
    private Long productId;
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
    private String reason;
    private Boolean isRestock;
}
