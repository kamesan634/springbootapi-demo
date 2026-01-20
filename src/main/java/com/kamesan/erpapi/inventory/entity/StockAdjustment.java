package com.kamesan.erpapi.inventory.entity;

import com.kamesan.erpapi.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 庫存調整單實體
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Entity
@Table(name = "stock_adjustments", indexes = {
        @Index(name = "idx_stock_adjustments_no", columnList = "adjustment_no"),
        @Index(name = "idx_stock_adjustments_warehouse", columnList = "warehouse_id"),
        @Index(name = "idx_stock_adjustments_product", columnList = "product_id"),
        @Index(name = "idx_stock_adjustments_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockAdjustment extends BaseEntity {

    @Column(name = "adjustment_no", nullable = false, unique = true, length = 30)
    private String adjustmentNo;

    @Column(name = "warehouse_id", nullable = false)
    private Long warehouseId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Enumerated(EnumType.STRING)
    @Column(name = "adjustment_type", nullable = false, length = 10)
    private AdjustmentType adjustmentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "adjustment_reason", nullable = false, length = 30)
    private AdjustmentReason adjustmentReason;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "before_quantity", nullable = false)
    private Integer beforeQuantity;

    @Column(name = "after_quantity", nullable = false)
    private Integer afterQuantity;

    @Column(name = "unit_cost", precision = 12, scale = 2)
    private BigDecimal unitCost;

    @Column(name = "total_cost", precision = 14, scale = 2)
    private BigDecimal totalCost;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private AdjustmentStatus status = AdjustmentStatus.PENDING;

    @Column(name = "reason_detail", length = 500)
    private String reasonDetail;

    @Column(name = "approved_by")
    private Long approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    public void approve(Long approvedBy) {
        this.status = AdjustmentStatus.APPROVED;
        this.approvedBy = approvedBy;
        this.approvedAt = LocalDateTime.now();
    }

    public void reject() {
        this.status = AdjustmentStatus.REJECTED;
    }

    public enum AdjustmentType {
        IN, OUT
    }

    public enum AdjustmentReason {
        DAMAGE, LOSS, GIFT, SAMPLE, ERROR_CORRECTION, OTHER
    }

    public enum AdjustmentStatus {
        PENDING, APPROVED, REJECTED
    }
}
