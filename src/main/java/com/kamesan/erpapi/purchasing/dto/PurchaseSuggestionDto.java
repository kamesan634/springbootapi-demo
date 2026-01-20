package com.kamesan.erpapi.purchasing.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 採購建議 DTO
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseSuggestionDto {

    private List<SuggestionItem> suggestions;
    private Integer totalItems;
    private BigDecimal totalAmount;
    private Integer criticalCount;
    private Integer warningCount;

    /**
     * 採購建議項目
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SuggestionItem {
        private Long productId;
        private String productSku;
        private String productName;
        private String categoryName;
        private Long supplierId;
        private String supplierName;
        private Integer currentStock;
        private Integer reservedStock;
        private Integer availableStock;
        private Integer safetyStock;
        private Integer averageDailySales;
        private Integer leadTimeDays;
        private Integer daysOfStock;
        private Integer suggestedQuantity;
        private BigDecimal unitCost;
        private BigDecimal totalCost;
        private PriorityLevel priority;
        private String reason;
    }

    /**
     * 優先等級
     */
    public enum PriorityLevel {
        CRITICAL("緊急"),
        HIGH("高"),
        MEDIUM("中"),
        LOW("低");

        private final String description;

        PriorityLevel(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
