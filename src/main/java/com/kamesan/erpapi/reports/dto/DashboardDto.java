package com.kamesan.erpapi.reports.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 儀表板 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardDto {

    // 銷售統計
    private BigDecimal todaySales;
    private BigDecimal monthSales;
    private BigDecimal yearSales;
    private Integer todayOrderCount;
    private Integer monthOrderCount;
    private BigDecimal salesGrowthRate;

    // 庫存統計
    private Integer totalProducts;
    private Integer lowStockProducts;
    private Integer outOfStockProducts;
    private BigDecimal inventoryValue;

    // 採購統計
    private Integer pendingPurchaseOrders;
    private BigDecimal monthPurchaseAmount;

    // 客戶統計
    private Integer totalCustomers;
    private Integer newCustomersThisMonth;

    // 圖表資料
    private List<SalesChartData> salesChart;
    private List<CategorySalesData> categorySales;
    private List<TopProductData> topProducts;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SalesChartData {
        private String date;
        private BigDecimal sales;
        private Integer orderCount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CategorySalesData {
        private String categoryName;
        private BigDecimal sales;
        private BigDecimal percentage;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TopProductData {
        private Long productId;
        private String productName;
        private Integer quantity;
        private BigDecimal sales;
    }
}
