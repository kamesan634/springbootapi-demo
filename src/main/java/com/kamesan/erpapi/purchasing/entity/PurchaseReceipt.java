package com.kamesan.erpapi.purchasing.entity;

import com.kamesan.erpapi.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 採購收貨單實體
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Entity
@Table(name = "purchase_receipts", indexes = {
        @Index(name = "idx_purchase_receipts_no", columnList = "receipt_no"),
        @Index(name = "idx_purchase_receipts_po", columnList = "purchase_order_id"),
        @Index(name = "idx_purchase_receipts_supplier", columnList = "supplier_id"),
        @Index(name = "idx_purchase_receipts_date", columnList = "receipt_date"),
        @Index(name = "idx_purchase_receipts_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseReceipt extends BaseEntity {

    @Column(name = "receipt_no", nullable = false, unique = true, length = 30)
    private String receiptNo;

    @Column(name = "purchase_order_id", nullable = false)
    private Long purchaseOrderId;

    @Column(name = "supplier_id", nullable = false)
    private Long supplierId;

    @Column(name = "warehouse_id", nullable = false)
    private Long warehouseId;

    @Column(name = "receipt_date", nullable = false)
    private LocalDate receiptDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private ReceiptStatus status = ReceiptStatus.DRAFT;

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

    @Column(name = "invoice_no", length = 30)
    private String invoiceNo;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "received_by")
    private Long receivedBy;

    @OneToMany(mappedBy = "purchaseReceipt", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<PurchaseReceiptItem> items = new ArrayList<>();

    public void addItem(PurchaseReceiptItem item) {
        items.add(item);
        item.setPurchaseReceipt(this);
    }

    public void calculateTotals() {
        this.subtotal = items.stream()
                .map(PurchaseReceiptItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.totalAmount = this.subtotal.add(this.taxAmount);
        this.totalItems = items.size();
        this.totalQuantity = items.stream().mapToInt(PurchaseReceiptItem::getReceivedQuantity).sum();
    }

    public enum ReceiptStatus {
        DRAFT, RECEIVED, INSPECTING, COMPLETED, REJECTED
    }
}
