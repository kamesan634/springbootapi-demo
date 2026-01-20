package com.kamesan.erpapi.inventory.dto;

import com.kamesan.erpapi.inventory.entity.InventoryMovement;
import com.kamesan.erpapi.inventory.entity.MovementType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 庫存異動記錄 DTO
 *
 * <p>用於傳輸庫存異動記錄資訊，包含異動的完整詳情。</p>
 *
 * <p>主要欄位說明：</p>
 * <ul>
 *   <li>movementType - 異動類型（入庫/出庫的各種類型）</li>
 *   <li>quantity - 異動數量（絕對值）</li>
 *   <li>beforeQuantity - 異動前庫存</li>
 *   <li>afterQuantity - 異動後庫存</li>
 *   <li>referenceNo - 關聯的業務單據編號</li>
 * </ul>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "庫存異動記錄")
public class InventoryMovementDto {

    /**
     * 異動記錄 ID
     */
    @Schema(description = "異動記錄 ID", example = "1")
    private Long id;

    /**
     * 商品 ID
     */
    @Schema(description = "商品 ID", example = "100")
    private Long productId;

    /**
     * 商品名稱
     * <p>透過關聯查詢取得，便於前端顯示</p>
     */
    @Schema(description = "商品名稱", example = "iPhone 15 Pro")
    private String productName;

    /**
     * 商品編號
     * <p>透過關聯查詢取得</p>
     */
    @Schema(description = "商品編號", example = "SKU-001")
    private String productCode;

    /**
     * 倉庫 ID
     */
    @Schema(description = "倉庫 ID", example = "1")
    private Long warehouseId;

    /**
     * 倉庫名稱
     * <p>透過關聯查詢取得，便於前端顯示</p>
     */
    @Schema(description = "倉庫名稱", example = "台北總倉")
    private String warehouseName;

    /**
     * 異動類型
     * <p>使用 MovementType 枚舉定義的類型</p>
     */
    @Schema(description = "異動類型", example = "PURCHASE_IN")
    private MovementType movementType;

    /**
     * 異動類型描述
     * <p>異動類型的中文說明</p>
     */
    @Schema(description = "異動類型描述", example = "進貨入庫")
    private String movementTypeDescription;

    /**
     * 是否為入庫操作
     */
    @Schema(description = "是否為入庫", example = "true")
    private Boolean isInbound;

    /**
     * 異動數量
     * <p>本次異動的數量（絕對值，正數）</p>
     */
    @Schema(description = "異動數量", example = "50")
    private Integer quantity;

    /**
     * 異動前數量
     * <p>執行異動操作前的庫存數量</p>
     */
    @Schema(description = "異動前數量", example = "100")
    private Integer beforeQuantity;

    /**
     * 異動後數量
     * <p>執行異動操作後的庫存數量</p>
     */
    @Schema(description = "異動後數量", example = "150")
    private Integer afterQuantity;

    /**
     * 庫存變化量
     * <p>帶正負號的變化量，入庫為正，出庫為負</p>
     */
    @Schema(description = "庫存變化量", example = "+50")
    private String quantityChange;

    /**
     * 參考單號
     * <p>關聯的業務單據編號</p>
     */
    @Schema(description = "參考單號", example = "PO-20240115-001")
    private String referenceNo;

    /**
     * 備註說明
     */
    @Schema(description = "備註說明", example = "新品進貨")
    private String remark;

    /**
     * 操作人員 ID
     */
    @Schema(description = "操作人員 ID", example = "1")
    private Long operatorId;

    /**
     * 操作人員名稱
     * <p>透過關聯查詢取得</p>
     */
    @Schema(description = "操作人員名稱", example = "管理員")
    private String operatorName;

    /**
     * 建立時間
     */
    @Schema(description = "建立時間", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;

    /**
     * 從庫存異動實體轉換為 DTO
     *
     * <p>基本轉換，不包含關聯資料的名稱</p>
     *
     * @param movement 庫存異動實體
     * @return 庫存異動 DTO
     */
    public static InventoryMovementDto fromEntity(InventoryMovement movement) {
        if (movement == null) {
            return null;
        }

        int change = movement.getQuantityChange();
        String changeStr = change >= 0 ? "+" + change : String.valueOf(change);

        return InventoryMovementDto.builder()
                .id(movement.getId())
                .productId(movement.getProductId())
                .warehouseId(movement.getWarehouseId())
                .movementType(movement.getMovementType())
                .movementTypeDescription(movement.getMovementType().getDescription())
                .isInbound(movement.getMovementType().isInbound())
                .quantity(movement.getQuantity())
                .beforeQuantity(movement.getBeforeQuantity())
                .afterQuantity(movement.getAfterQuantity())
                .quantityChange(changeStr)
                .referenceNo(movement.getReferenceNo())
                .remark(movement.getRemark())
                .operatorId(movement.getOperatorId())
                .createdAt(movement.getCreatedAt())
                .build();
    }

    /**
     * 從庫存異動實體轉換為 DTO（含關聯名稱）
     *
     * @param movement      庫存異動實體
     * @param productName   商品名稱
     * @param productCode   商品編號
     * @param warehouseName 倉庫名稱
     * @param operatorName  操作人員名稱
     * @return 庫存異動 DTO
     */
    public static InventoryMovementDto fromEntity(
            InventoryMovement movement,
            String productName,
            String productCode,
            String warehouseName,
            String operatorName) {
        if (movement == null) {
            return null;
        }

        InventoryMovementDto dto = fromEntity(movement);
        dto.setProductName(productName);
        dto.setProductCode(productCode);
        dto.setWarehouseName(warehouseName);
        dto.setOperatorName(operatorName);
        return dto;
    }

    /**
     * 建立庫存異動摘要 DTO
     *
     * <p>只包含關鍵欄位，用於列表顯示</p>
     *
     * @param movement 庫存異動實體
     * @return 庫存異動摘要 DTO
     */
    public static InventoryMovementDto summary(InventoryMovement movement) {
        if (movement == null) {
            return null;
        }

        int change = movement.getQuantityChange();
        String changeStr = change >= 0 ? "+" + change : String.valueOf(change);

        return InventoryMovementDto.builder()
                .id(movement.getId())
                .productId(movement.getProductId())
                .warehouseId(movement.getWarehouseId())
                .movementType(movement.getMovementType())
                .movementTypeDescription(movement.getMovementType().getDescription())
                .quantity(movement.getQuantity())
                .quantityChange(changeStr)
                .referenceNo(movement.getReferenceNo())
                .createdAt(movement.getCreatedAt())
                .build();
    }
}
