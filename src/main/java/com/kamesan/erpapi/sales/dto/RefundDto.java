package com.kamesan.erpapi.sales.dto;

import com.kamesan.erpapi.sales.entity.Refund;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * 退貨單 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefundDto {
    private Long id;
    private String refundNo;
    private Long orderId;
    private String orderNo;
    private Long storeId;
    private Long customerId;
    private LocalDate refundDate;
    private LocalTime refundTime;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private Refund.RefundMethod refundMethod;
    private Refund.RefundStatus status;
    private String reason;
    private String notes;
    private Long approvedBy;
    private LocalDateTime approvedAt;
    private List<RefundItemDto> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
