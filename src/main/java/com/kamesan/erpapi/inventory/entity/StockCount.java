package com.kamesan.erpapi.inventory.entity;

import com.kamesan.erpapi.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 盤點單實體
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Entity
@Table(name = "stock_counts", indexes = {
        @Index(name = "idx_stock_counts_no", columnList = "count_no"),
        @Index(name = "idx_stock_counts_warehouse", columnList = "warehouse_id"),
        @Index(name = "idx_stock_counts_date", columnList = "count_date"),
        @Index(name = "idx_stock_counts_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockCount extends BaseEntity {

    @Column(name = "count_no", nullable = false, unique = true, length = 30)
    private String countNo;

    @Column(name = "warehouse_id", nullable = false)
    private Long warehouseId;

    @Column(name = "count_date", nullable = false)
    private LocalDate countDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "count_type", nullable = false, length = 20)
    @Builder.Default
    private CountType countType = CountType.PARTIAL;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private CountStatus status = CountStatus.DRAFT;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "total_items")
    @Builder.Default
    private Integer totalItems = 0;

    @Column(name = "variance_items")
    @Builder.Default
    private Integer varianceItems = 0;

    @Column(name = "variance_amount", precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal varianceAmount = BigDecimal.ZERO;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @OneToMany(mappedBy = "stockCount", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<StockCountItem> items = new ArrayList<>();

    public void addItem(StockCountItem item) {
        items.add(item);
        item.setStockCount(this);
    }

    public enum CountType {
        FULL, PARTIAL, SPOT
    }

    public enum CountStatus {
        DRAFT, IN_PROGRESS, COMPLETED, CANCELLED
    }
}
