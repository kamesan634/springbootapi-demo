package com.kamesan.erpapi.sales.dto;

import com.kamesan.erpapi.sales.entity.CashierShift;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 收銀班次 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CashierShiftDto {
    private Long id;
    private String shiftNo;
    private Long storeId;
    private Long cashierId;
    private LocalDate shiftDate;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal openingAmount;
    private BigDecimal closingAmount;
    private BigDecimal expectedAmount;
    private BigDecimal differenceAmount;
    private BigDecimal totalSales;
    private BigDecimal totalRefunds;
    private BigDecimal cashSales;
    private BigDecimal cardSales;
    private BigDecimal otherSales;
    private Integer orderCount;
    private Integer refundCount;
    private CashierShift.ShiftStatus status;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
