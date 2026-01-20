package com.kamesan.erpapi.sales.entity;

import com.kamesan.erpapi.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 退貨單實體
 *
 * <p>記錄銷售退貨的相關資訊</p>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Entity
@Table(name = "refunds", indexes = {
        @Index(name = "idx_refunds_no", columnList = "refund_no"),
        @Index(name = "idx_refunds_order", columnList = "order_id"),
        @Index(name = "idx_refunds_store", columnList = "store_id"),
        @Index(name = "idx_refunds_date", columnList = "refund_date"),
        @Index(name = "idx_refunds_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Refund extends BaseEntity {

    @Column(name = "refund_no", nullable = false, unique = true, length = 30)
    private String refundNo;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "order_no", nullable = false, length = 30)
    private String orderNo;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "refund_date", nullable = false)
    private LocalDate refundDate;

    @Column(name = "refund_time", nullable = false)
    private LocalTime refundTime;

    @Column(name = "subtotal", nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "tax_amount", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "total_amount", nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "refund_method", nullable = false, length = 20)
    @Builder.Default
    private RefundMethod refundMethod = RefundMethod.ORIGINAL;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private RefundStatus status = RefundStatus.PENDING;

    @Column(name = "reason", length = 500)
    private String reason;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "approved_by")
    private Long approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @OneToMany(mappedBy = "refund", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<RefundItem> refundItems = new ArrayList<>();

    public void addRefundItem(RefundItem item) {
        refundItems.add(item);
        item.setRefund(this);
    }

    public void removeRefundItem(RefundItem item) {
        refundItems.remove(item);
        item.setRefund(null);
    }

    public void calculateTotals() {
        this.subtotal = refundItems.stream()
                .map(RefundItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.totalAmount = this.subtotal.add(this.taxAmount != null ? this.taxAmount : BigDecimal.ZERO);
    }

    public enum RefundMethod {
        CASH, CREDIT_CARD, ORIGINAL, STORE_CREDIT
    }

    public enum RefundStatus {
        PENDING, APPROVED, COMPLETED, REJECTED
    }
}
