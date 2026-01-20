package com.kamesan.erpapi.purchasing.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 採購收貨單明細實體
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Entity
@Table(name = "purchase_receipt_items", indexes = {
        @Index(name = "idx_pr_items_receipt", columnList = "purchase_receipt_id"),
        @Index(name = "idx_pr_items_product", columnList = "product_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseReceiptItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_receipt_id", nullable = false)
    private PurchaseReceipt purchaseReceipt;

    @Column(name = "po_item_id")
    private Long poItemId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "product_name", nullable = false, length = 200)
    private String productName;

    @Column(name = "expected_quantity", nullable = false)
    private Integer expectedQuantity;

    @Column(name = "received_quantity", nullable = false)
    private Integer receivedQuantity;

    @Column(name = "rejected_quantity")
    @Builder.Default
    private Integer rejectedQuantity = 0;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "subtotal", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "reject_reason", length = 200)
    private String rejectReason;

    @Column(name = "notes", length = 200)
    private String notes;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        calculateSubtotal();
    }

    public void calculateSubtotal() {
        if (unitPrice != null && receivedQuantity != null) {
            this.subtotal = unitPrice.multiply(BigDecimal.valueOf(receivedQuantity));
        }
    }
}
