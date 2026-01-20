package com.kamesan.erpapi.purchasing.entity;

import com.kamesan.erpapi.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 採購退貨單實體
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Entity
@Table(name = "purchase_returns", indexes = {
        @Index(name = "idx_purchase_returns_no", columnList = "return_no"),
        @Index(name = "idx_purchase_returns_supplier", columnList = "supplier_id"),
        @Index(name = "idx_purchase_returns_date", columnList = "return_date"),
        @Index(name = "idx_purchase_returns_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseReturn extends BaseEntity {

    @Column(name = "return_no", nullable = false, unique = true, length = 30)
    private String returnNo;

    @Column(name = "purchase_receipt_id")
    private Long purchaseReceiptId;

    @Column(name = "supplier_id", nullable = false)
    private Long supplierId;

    @Column(name = "warehouse_id", nullable = false)
    private Long warehouseId;

    @Column(name = "return_date", nullable = false)
    private LocalDate returnDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private ReturnStatus status = ReturnStatus.DRAFT;

    @Column(name = "total_items")
    @Builder.Default
    private Integer totalItems = 0;

    @Column(name = "total_quantity")
    @Builder.Default
    private Integer totalQuantity = 0;

    @Column(name = "subtotal", nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "tax_amount", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "total_amount", nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "reason", length = 500)
    private String reason;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "approved_by")
    private Long approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "shipped_at")
    private LocalDateTime shippedAt;

    @OneToMany(mappedBy = "purchaseReturn", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<PurchaseReturnItem> items = new ArrayList<>();

    public void addItem(PurchaseReturnItem item) {
        items.add(item);
        item.setPurchaseReturn(this);
    }

    public void calculateTotals() {
        this.subtotal = items.stream()
                .map(PurchaseReturnItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.totalAmount = this.subtotal.add(this.taxAmount);
        this.totalItems = items.size();
        this.totalQuantity = items.stream().mapToInt(PurchaseReturnItem::getQuantity).sum();
    }

    public void approve(Long approvedBy) {
        this.status = ReturnStatus.APPROVED;
        this.approvedBy = approvedBy;
        this.approvedAt = LocalDateTime.now();
    }

    public enum ReturnStatus {
        DRAFT, PENDING, APPROVED, SHIPPED, COMPLETED, REJECTED
    }
}
