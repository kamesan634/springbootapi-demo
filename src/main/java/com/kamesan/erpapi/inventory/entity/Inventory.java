package com.kamesan.erpapi.inventory.entity;

import com.kamesan.erpapi.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 庫存實體
 *
 * <p>記錄每個商品在各個倉庫的庫存資訊，包含：</p>
 * <ul>
 *   <li>商品 ID (product_id) - 關聯到商品資料表</li>
 *   <li>倉庫 ID (warehouse_id) - 關聯到倉庫/門市資料表</li>
 *   <li>庫存數量 (quantity) - 目前可用的庫存數量</li>
 *   <li>保留數量 (reserved_quantity) - 已被訂單預留但尚未出貨的數量</li>
 *   <li>最後異動日期 (last_movement_date) - 最近一次庫存異動的時間</li>
 * </ul>
 *
 * <p>實際可銷售數量 = quantity - reserved_quantity</p>
 *
 * <p>使用複合唯一索引確保同一商品在同一倉庫只有一筆庫存記錄。</p>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Entity
@Table(name = "inventories",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_inventory_product_warehouse",
                        columnNames = {"product_id", "warehouse_id"}
                )
        },
        indexes = {
                @Index(name = "idx_inventory_product", columnList = "product_id"),
                @Index(name = "idx_inventory_warehouse", columnList = "warehouse_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inventory extends BaseEntity {

    /**
     * 商品 ID
     * <p>關聯到 products 資料表的主鍵</p>
     */
    @Column(name = "product_id", nullable = false)
    private Long productId;

    /**
     * 倉庫 ID
     * <p>關聯到 stores（門市/倉庫）資料表的主鍵</p>
     */
    @Column(name = "warehouse_id", nullable = false)
    private Long warehouseId;

    /**
     * 庫存數量
     * <p>目前可用的庫存數量，不可為負數</p>
     */
    @Column(name = "quantity", nullable = false)
    @Builder.Default
    private Integer quantity = 0;

    /**
     * 保留數量
     * <p>已被訂單預留但尚未出貨的數量，用於防止超賣</p>
     */
    @Column(name = "reserved_quantity", nullable = false)
    @Builder.Default
    private Integer reservedQuantity = 0;

    /**
     * 最後異動日期
     * <p>記錄最近一次庫存異動的時間，便於追蹤和查詢</p>
     */
    @Column(name = "last_movement_date")
    private LocalDateTime lastMovementDate;

    /**
     * 計算可銷售數量
     *
     * <p>可銷售數量 = 庫存數量 - 保留數量</p>
     *
     * @return 可銷售的庫存數量
     */
    public int getAvailableQuantity() {
        return quantity - reservedQuantity;
    }

    /**
     * 增加庫存數量
     *
     * <p>用於入庫操作（進貨、退貨入庫、調撥入庫、盤盈）</p>
     *
     * @param amount 要增加的數量，必須為正數
     * @throws IllegalArgumentException 如果數量小於等於 0
     */
    public void increaseQuantity(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("增加數量必須為正數");
        }
        this.quantity += amount;
        this.lastMovementDate = LocalDateTime.now();
    }

    /**
     * 減少庫存數量
     *
     * <p>用於出庫操作（銷售、退貨出庫、調撥出庫、盤虧）</p>
     *
     * @param amount 要減少的數量，必須為正數
     * @throws IllegalArgumentException 如果數量小於等於 0 或庫存不足
     */
    public void decreaseQuantity(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("減少數量必須為正數");
        }
        if (this.quantity < amount) {
            throw new IllegalArgumentException("庫存數量不足");
        }
        this.quantity -= amount;
        this.lastMovementDate = LocalDateTime.now();
    }

    /**
     * 預留庫存
     *
     * <p>當訂單成立時，預留庫存以防止超賣</p>
     *
     * @param amount 要預留的數量，必須為正數
     * @throws IllegalArgumentException 如果數量小於等於 0 或可用庫存不足
     */
    public void reserve(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("預留數量必須為正數");
        }
        if (getAvailableQuantity() < amount) {
            throw new IllegalArgumentException("可用庫存不足，無法預留");
        }
        this.reservedQuantity += amount;
    }

    /**
     * 釋放預留庫存
     *
     * <p>當訂單取消或完成出貨時，釋放預留的庫存</p>
     *
     * @param amount 要釋放的數量，必須為正數
     * @throws IllegalArgumentException 如果數量小於等於 0 或超過已預留數量
     */
    public void release(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("釋放數量必須為正數");
        }
        if (this.reservedQuantity < amount) {
            throw new IllegalArgumentException("釋放數量超過已預留數量");
        }
        this.reservedQuantity -= amount;
    }

    /**
     * 確認出貨
     *
     * <p>訂單出貨時，同時減少庫存和釋放預留數量</p>
     *
     * @param amount 出貨數量，必須為正數
     * @throws IllegalArgumentException 如果數量無效或庫存/預留數量不足
     */
    public void confirmShipment(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("出貨數量必須為正數");
        }
        if (this.quantity < amount) {
            throw new IllegalArgumentException("庫存數量不足");
        }
        if (this.reservedQuantity < amount) {
            throw new IllegalArgumentException("預留數量不足，可能資料不一致");
        }
        this.quantity -= amount;
        this.reservedQuantity -= amount;
        this.lastMovementDate = LocalDateTime.now();
    }

    /**
     * 檢查庫存是否足夠
     *
     * @param amount 需要的數量
     * @return 如果可用庫存大於等於需要的數量，返回 true
     */
    public boolean hasAvailableStock(int amount) {
        return getAvailableQuantity() >= amount;
    }
}
