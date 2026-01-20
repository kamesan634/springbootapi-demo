package com.kamesan.erpapi.reports.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 採購報表 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchasingReportDto {

    private LocalDate startDate;
    private LocalDate endDate;

    // 摘要
    private BigDecimal totalPurchaseAmount;
    private BigDecimal totalReturnAmount;
    private BigDecimal netPurchaseAmount;
    private Integer purchaseOrderCount;
    private Integer receiptCount;
    private Integer returnCount;
    private BigDecimal avgOrderAmount;

    // 供應商統計
    private List<SupplierSummary> supplierSummary;

    // 商品採購統計
    private List<ProductPurchase> productPurchases;

    // 每月趨勢
    private List<MonthlyPurchase> monthlyTrend;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SupplierSummary {
        private Long supplierId;
        private String supplierName;
        private Integer orderCount;
        private BigDecimal purchaseAmount;
        private BigDecimal returnAmount;
        private BigDecimal netAmount;
        private BigDecimal percentage;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProductPurchase {
        private Long productId;
        private String productName;
        private Integer quantity;
        private BigDecimal amount;
        private BigDecimal avgUnitPrice;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MonthlyPurchase {
        private String month;
        private BigDecimal purchaseAmount;
        private BigDecimal returnAmount;
        private Integer orderCount;
    }
}
