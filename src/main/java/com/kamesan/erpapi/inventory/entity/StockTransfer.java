package com.kamesan.erpapi.inventory.entity;

import com.kamesan.erpapi.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 調撥單實體
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Entity
@Table(name = "stock_transfers", indexes = {
        @Index(name = "idx_stock_transfers_no", columnList = "transfer_no"),
        @Index(name = "idx_stock_transfers_from", columnList = "from_warehouse_id"),
        @Index(name = "idx_stock_transfers_to", columnList = "to_warehouse_id"),
        @Index(name = "idx_stock_transfers_date", columnList = "transfer_date"),
        @Index(name = "idx_stock_transfers_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockTransfer extends BaseEntity {

    @Column(name = "transfer_no", nullable = false, unique = true, length = 30)
    private String transferNo;

    @Column(name = "from_warehouse_id", nullable = false)
    private Long fromWarehouseId;

    @Column(name = "to_warehouse_id", nullable = false)
    private Long toWarehouseId;

    @Column(name = "transfer_date", nullable = false)
    private LocalDate transferDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private TransferStatus status = TransferStatus.DRAFT;

    @Column(name = "total_items")
    @Builder.Default
    private Integer totalItems = 0;

    @Column(name = "total_quantity")
    @Builder.Default
    private Integer totalQuantity = 0;

    @Column(name = "shipped_at")
    private LocalDateTime shippedAt;

    @Column(name = "received_at")
    private LocalDateTime receivedAt;

    @Column(name = "shipped_by")
    private Long shippedBy;

    @Column(name = "received_by")
    private Long receivedBy;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @OneToMany(mappedBy = "stockTransfer", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<StockTransferItem> items = new ArrayList<>();

    public void addItem(StockTransferItem item) {
        items.add(item);
        item.setStockTransfer(this);
    }

    public void ship(Long shippedBy) {
        this.status = TransferStatus.IN_TRANSIT;
        this.shippedAt = LocalDateTime.now();
        this.shippedBy = shippedBy;
    }

    public void receive(Long receivedBy) {
        this.status = TransferStatus.RECEIVED;
        this.receivedAt = LocalDateTime.now();
        this.receivedBy = receivedBy;
    }

    public enum TransferStatus {
        DRAFT, PENDING, IN_TRANSIT, RECEIVED, CANCELLED
    }
}
