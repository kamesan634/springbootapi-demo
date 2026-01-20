package com.kamesan.erpapi.reports.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 庫存報表 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryReportDto {

    private LocalDate reportDate;
    private Long warehouseId;
    private String warehouseName;

    // 摘要
    private Integer totalProducts;
    private Integer totalQuantity;
    private BigDecimal totalValue;
    private Integer lowStockCount;
    private Integer outOfStockCount;
    private Integer overstockCount;

    // 庫存明細
    private List<ProductInventory> inventoryList;

    // 類別統計
    private List<CategoryInventory> categoryInventory;

    // 庫存異動統計
    private List<MovementSummary> movementSummary;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProductInventory {
        private Long productId;
        private String productCode;
        private String productName;
        private String categoryName;
        private Integer quantity;
        private Integer reservedQuantity;
        private Integer availableQuantity;
        private BigDecimal unitCost;
        private BigDecimal totalValue;
        private Integer safetyStock;
        private String status; // NORMAL, LOW, OUT_OF_STOCK, OVERSTOCK
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CategoryInventory {
        private Long categoryId;
        private String categoryName;
        private Integer productCount;
        private Integer totalQuantity;
        private BigDecimal totalValue;
        private BigDecimal percentage;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MovementSummary {
        private String movementType;
        private Integer count;
        private Integer quantity;
        private BigDecimal value;
    }
}
