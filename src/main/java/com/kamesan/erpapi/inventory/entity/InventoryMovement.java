package com.kamesan.erpapi.inventory.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 庫存異動記錄實體
 *
 * <p>記錄每一次庫存變動的詳細資訊，用於追蹤庫存變化歷程，包含：</p>
 * <ul>
 *   <li>商品 ID (product_id) - 異動的商品</li>
 *   <li>倉庫 ID (warehouse_id) - 異動發生的倉庫</li>
 *   <li>異動類型 (movement_type) - 入庫/出庫的類型</li>
 *   <li>異動數量 (quantity) - 本次異動的數量（絕對值）</li>
 *   <li>異動前數量 (before_quantity) - 異動前的庫存數量</li>
 *   <li>異動後數量 (after_quantity) - 異動後的庫存數量</li>
 *   <li>參考單號 (reference_no) - 關聯的單據編號（如採購單號、銷售單號）</li>
 *   <li>建立時間 (created_at) - 異動發生的時間</li>
 * </ul>
 *
 * <p>此資料表僅供新增和查詢，不允許修改和刪除，以確保庫存異動歷史的完整性。</p>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Entity
@Table(name = "inventory_movements",
        indexes = {
                @Index(name = "idx_movement_product", columnList = "product_id"),
                @Index(name = "idx_movement_warehouse", columnList = "warehouse_id"),
                @Index(name = "idx_movement_type", columnList = "movement_type"),
                @Index(name = "idx_movement_reference", columnList = "reference_no"),
                @Index(name = "idx_movement_created_at", columnList = "created_at")
        }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryMovement implements Serializable {

    /**
     * 主鍵 ID
     * <p>使用自動遞增策略</p>
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

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
     * 異動類型
     * <p>使用 MovementType 枚舉定義的類型</p>
     *
     * @see MovementType
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false, length = 20)
    private MovementType movementType;

    /**
     * 異動數量
     * <p>本次異動的數量，為正整數（入庫/出庫的絕對值）</p>
     */
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    /**
     * 異動前數量
     * <p>執行異動操作前的庫存數量</p>
     */
    @Column(name = "before_quantity", nullable = false)
    private Integer beforeQuantity;

    /**
     * 異動後數量
     * <p>執行異動操作後的庫存數量</p>
     */
    @Column(name = "after_quantity", nullable = false)
    private Integer afterQuantity;

    /**
     * 參考單號
     * <p>關聯的業務單據編號，如：</p>
     * <ul>
     *   <li>PO-20240101-001（採購單）</li>
     *   <li>SO-20240101-001（銷售單）</li>
     *   <li>TR-20240101-001（調撥單）</li>
     *   <li>ADJ-20240101-001（盤點調整單）</li>
     * </ul>
     */
    @Column(name = "reference_no", length = 50)
    private String referenceNo;

    /**
     * 備註說明
     * <p>異動的額外說明或原因</p>
     */
    @Column(name = "remark", length = 500)
    private String remark;

    /**
     * 操作人員 ID
     * <p>執行此異動操作的使用者 ID</p>
     */
    @Column(name = "operator_id")
    private Long operatorId;

    /**
     * 建立時間
     * <p>由 JPA Auditing 自動填入，不可更新</p>
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 計算庫存變化量
     *
     * <p>根據異動類型返回帶正負號的變化量</p>
     *
     * @return 入庫為正數，出庫為負數
     */
    public int getQuantityChange() {
        return quantity * movementType.getQuantitySign();
    }

    /**
     * 驗證異動前後數量的一致性
     *
     * <p>檢查 afterQuantity 是否等於 beforeQuantity + 變化量</p>
     *
     * @return 如果數量計算正確返回 true
     */
    public boolean isQuantityConsistent() {
        return afterQuantity == beforeQuantity + getQuantityChange();
    }

    /**
     * 靜態工廠方法：建立庫存異動記錄
     *
     * @param productId      商品 ID
     * @param warehouseId    倉庫 ID
     * @param movementType   異動類型
     * @param quantity       異動數量
     * @param beforeQuantity 異動前數量
     * @param referenceNo    參考單號
     * @param remark         備註
     * @param operatorId     操作人員 ID
     * @return 庫存異動記錄實例
     */
    public static InventoryMovement create(
            Long productId,
            Long warehouseId,
            MovementType movementType,
            Integer quantity,
            Integer beforeQuantity,
            String referenceNo,
            String remark,
            Long operatorId) {

        int afterQuantity = beforeQuantity + (quantity * movementType.getQuantitySign());

        return InventoryMovement.builder()
                .productId(productId)
                .warehouseId(warehouseId)
                .movementType(movementType)
                .quantity(quantity)
                .beforeQuantity(beforeQuantity)
                .afterQuantity(afterQuantity)
                .referenceNo(referenceNo)
                .remark(remark)
                .operatorId(operatorId)
                .build();
    }
}
