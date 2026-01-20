package com.kamesan.erpapi.sales.entity;

import com.kamesan.erpapi.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 收銀班次實體
 *
 * <p>記錄收銀員的班次資訊，包括開班、結班、銷售統計等</p>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Entity
@Table(name = "cashier_shifts", indexes = {
        @Index(name = "idx_cashier_shifts_no", columnList = "shift_no"),
        @Index(name = "idx_cashier_shifts_store", columnList = "store_id"),
        @Index(name = "idx_cashier_shifts_cashier", columnList = "cashier_id"),
        @Index(name = "idx_cashier_shifts_date", columnList = "shift_date"),
        @Index(name = "idx_cashier_shifts_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CashierShift extends BaseEntity {

    @Column(name = "shift_no", nullable = false, unique = true, length = 30)
    private String shiftNo;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(name = "cashier_id", nullable = false)
    private Long cashierId;

    @Column(name = "shift_date", nullable = false)
    private LocalDate shiftDate;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "opening_amount", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal openingAmount = BigDecimal.ZERO;

    @Column(name = "closing_amount", precision = 12, scale = 2)
    private BigDecimal closingAmount;

    @Column(name = "expected_amount", precision = 12, scale = 2)
    private BigDecimal expectedAmount;

    @Column(name = "difference_amount", precision = 12, scale = 2)
    private BigDecimal differenceAmount;

    @Column(name = "total_sales", precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal totalSales = BigDecimal.ZERO;

    @Column(name = "total_refunds", precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal totalRefunds = BigDecimal.ZERO;

    @Column(name = "cash_sales", precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal cashSales = BigDecimal.ZERO;

    @Column(name = "card_sales", precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal cardSales = BigDecimal.ZERO;

    @Column(name = "other_sales", precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal otherSales = BigDecimal.ZERO;

    @Column(name = "order_count")
    @Builder.Default
    private Integer orderCount = 0;

    @Column(name = "refund_count")
    @Builder.Default
    private Integer refundCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private ShiftStatus status = ShiftStatus.OPEN;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    public void closeShift(BigDecimal closingAmount) {
        this.endTime = LocalDateTime.now();
        this.closingAmount = closingAmount;
        this.expectedAmount = openingAmount.add(cashSales).subtract(totalRefunds);
        this.differenceAmount = closingAmount.subtract(expectedAmount);
        this.status = ShiftStatus.CLOSED;
    }

    public enum ShiftStatus {
        OPEN, CLOSED, RECONCILED
    }
}
