package com.kamesan.erpapi.inventory.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 盤點單明細實體
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Entity
@Table(name = "stock_count_items", indexes = {
        @Index(name = "idx_stock_count_items_count", columnList = "stock_count_id"),
        @Index(name = "idx_stock_count_items_product", columnList = "product_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockCountItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_count_id", nullable = false)
    private StockCount stockCount;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "system_quantity", nullable = false)
    private Integer systemQuantity;

    @Column(name = "actual_quantity")
    private Integer actualQuantity;

    @Column(name = "variance_quantity")
    private Integer varianceQuantity;

    @Column(name = "unit_cost", precision = 12, scale = 2)
    private BigDecimal unitCost;

    @Column(name = "variance_amount", precision = 12, scale = 2)
    private BigDecimal varianceAmount;

    @Column(name = "notes", length = 200)
    private String notes;

    @Column(name = "counted_by")
    private Long countedBy;

    @Column(name = "counted_at")
    private LocalDateTime countedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public void recordCount(Integer actualQuantity, Long countedBy) {
        this.actualQuantity = actualQuantity;
        this.varianceQuantity = actualQuantity - systemQuantity;
        this.countedBy = countedBy;
        this.countedAt = LocalDateTime.now();
        if (unitCost != null) {
            this.varianceAmount = unitCost.multiply(BigDecimal.valueOf(varianceQuantity));
        }
    }
}
