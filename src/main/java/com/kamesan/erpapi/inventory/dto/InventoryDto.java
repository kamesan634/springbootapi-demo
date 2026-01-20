package com.kamesan.erpapi.inventory.dto;

import com.kamesan.erpapi.inventory.entity.Inventory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 庫存資訊 DTO
 *
 * <p>用於傳輸庫存資訊，包含庫存的所有相關欄位，並計算可用庫存數量。</p>
 *
 * <p>主要欄位說明：</p>
 * <ul>
 *   <li>quantity - 實際庫存數量</li>
 *   <li>reservedQuantity - 已被訂單預留的數量</li>
 *   <li>availableQuantity - 可銷售數量（quantity - reservedQuantity）</li>
 * </ul>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "庫存資訊")
public class InventoryDto {

    /**
     * 庫存記錄 ID
     */
    @Schema(description = "庫存記錄 ID", example = "1")
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
     * 庫存數量
     * <p>目前的實際庫存數量</p>
     */
    @Schema(description = "庫存數量", example = "100")
    private Integer quantity;

    /**
     * 保留數量
     * <p>已被訂單預留但尚未出貨的數量</p>
     */
    @Schema(description = "保留數量", example = "10")
    private Integer reservedQuantity;

    /**
     * 可用數量
     * <p>可銷售的庫存數量 = quantity - reservedQuantity</p>
     */
    @Schema(description = "可用數量", example = "90")
    private Integer availableQuantity;

    /**
     * 最後異動日期
     */
    @Schema(description = "最後異動日期", example = "2024-01-15T10:30:00")
    private LocalDateTime lastMovementDate;

    /**
     * 建立時間
     */
    @Schema(description = "建立時間", example = "2024-01-01T00:00:00")
    private LocalDateTime createdAt;

    /**
     * 更新時間
     */
    @Schema(description = "更新時間", example = "2024-01-15T10:30:00")
    private LocalDateTime updatedAt;

    /**
     * 從庫存實體轉換為 DTO
     *
     * <p>基本轉換，不包含商品和倉庫名稱</p>
     *
     * @param inventory 庫存實體
     * @return 庫存 DTO
     */
    public static InventoryDto fromEntity(Inventory inventory) {
        if (inventory == null) {
            return null;
        }
        return InventoryDto.builder()
                .id(inventory.getId())
                .productId(inventory.getProductId())
                .warehouseId(inventory.getWarehouseId())
                .quantity(inventory.getQuantity())
                .reservedQuantity(inventory.getReservedQuantity())
                .availableQuantity(inventory.getAvailableQuantity())
                .lastMovementDate(inventory.getLastMovementDate())
                .createdAt(inventory.getCreatedAt())
                .updatedAt(inventory.getUpdatedAt())
                .build();
    }

    /**
     * 從庫存實體轉換為 DTO（含商品和倉庫名稱）
     *
     * @param inventory     庫存實體
     * @param productName   商品名稱
     * @param productCode   商品編號
     * @param warehouseName 倉庫名稱
     * @return 庫存 DTO
     */
    public static InventoryDto fromEntity(
            Inventory inventory,
            String productName,
            String productCode,
            String warehouseName) {
        if (inventory == null) {
            return null;
        }
        return InventoryDto.builder()
                .id(inventory.getId())
                .productId(inventory.getProductId())
                .productName(productName)
                .productCode(productCode)
                .warehouseId(inventory.getWarehouseId())
                .warehouseName(warehouseName)
                .quantity(inventory.getQuantity())
                .reservedQuantity(inventory.getReservedQuantity())
                .availableQuantity(inventory.getAvailableQuantity())
                .lastMovementDate(inventory.getLastMovementDate())
                .createdAt(inventory.getCreatedAt())
                .updatedAt(inventory.getUpdatedAt())
                .build();
    }

    /**
     * 建立庫存摘要 DTO
     *
     * <p>只包含關鍵欄位，用於列表顯示</p>
     *
     * @param inventory 庫存實體
     * @return 庫存摘要 DTO
     */
    public static InventoryDto summary(Inventory inventory) {
        if (inventory == null) {
            return null;
        }
        return InventoryDto.builder()
                .id(inventory.getId())
                .productId(inventory.getProductId())
                .warehouseId(inventory.getWarehouseId())
                .quantity(inventory.getQuantity())
                .availableQuantity(inventory.getAvailableQuantity())
                .build();
    }
}
