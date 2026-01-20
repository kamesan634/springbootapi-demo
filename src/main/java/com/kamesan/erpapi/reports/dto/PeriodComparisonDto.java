package com.kamesan.erpapi.reports.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 期間比較報表 DTO
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PeriodComparisonDto {

    private PeriodInfo currentPeriod;
    private PeriodInfo previousPeriod;
    private String comparisonType; // YOY, MOM, CUSTOM

    // 變化摘要
    private ComparisonSummary summary;

    // 每日比較趨勢
    private List<DailyComparison> dailyComparisons;

    // 商品變化
    private List<ProductComparison> productComparisons;

    // 分類變化
    private List<CategoryComparison> categoryComparisons;

    /**
     * 期間資訊
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PeriodInfo {
        private LocalDate startDate;
        private LocalDate endDate;
        private String label;
        private Integer days;
    }

    /**
     * 比較摘要
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ComparisonSummary {
        // 本期
        private BigDecimal currentRevenue;
        private Integer currentOrderCount;
        private BigDecimal currentAverageOrder;
        private BigDecimal currentProfit;

        // 上期
        private BigDecimal previousRevenue;
        private Integer previousOrderCount;
        private BigDecimal previousAverageOrder;
        private BigDecimal previousProfit;

        // 變化
        private BigDecimal revenueChange;
        private BigDecimal revenueChangeRate;
        private Integer orderCountChange;
        private BigDecimal orderCountChangeRate;
        private BigDecimal averageOrderChange;
        private BigDecimal averageOrderChangeRate;
        private BigDecimal profitChange;
        private BigDecimal profitChangeRate;
    }

    /**
     * 每日比較
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DailyComparison {
        private Integer dayIndex; // 第幾天 (1, 2, 3...)
        private LocalDate currentDate;
        private LocalDate previousDate;
        private BigDecimal currentRevenue;
        private BigDecimal previousRevenue;
        private BigDecimal revenueChange;
        private BigDecimal revenueChangeRate;
        private Integer currentOrderCount;
        private Integer previousOrderCount;
    }

    /**
     * 商品變化
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProductComparison {
        private Long productId;
        private String productSku;
        private String productName;
        private Integer currentQuantity;
        private Integer previousQuantity;
        private Integer quantityChange;
        private BigDecimal quantityChangeRate;
        private BigDecimal currentRevenue;
        private BigDecimal previousRevenue;
        private BigDecimal revenueChange;
        private BigDecimal revenueChangeRate;
    }

    /**
     * 分類變化
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CategoryComparison {
        private Long categoryId;
        private String categoryName;
        private BigDecimal currentRevenue;
        private BigDecimal previousRevenue;
        private BigDecimal revenueChange;
        private BigDecimal revenueChangeRate;
    }
}
