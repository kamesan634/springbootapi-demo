package com.kamesan.erpapi.reports.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 銷售報表 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesReportDto {

    private LocalDate startDate;
    private LocalDate endDate;
    private Long storeId;
    private String storeName;

    // 摘要
    private BigDecimal totalSales;
    private BigDecimal totalRefunds;
    private BigDecimal netSales;
    private Integer orderCount;
    private Integer refundCount;
    private BigDecimal avgOrderAmount;
    private BigDecimal grossProfit;
    private BigDecimal profitMargin;

    // 每日明細
    private List<DailySales> dailySales;

    // 付款方式統計
    private List<PaymentMethodSummary> paymentMethods;

    // 時段統計
    private List<HourlySales> hourlySales;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DailySales {
        private LocalDate date;
        private BigDecimal sales;
        private BigDecimal refunds;
        private BigDecimal netSales;
        private Integer orderCount;
        private Integer refundCount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PaymentMethodSummary {
        private String paymentMethod;
        private BigDecimal amount;
        private Integer count;
        private BigDecimal percentage;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class HourlySales {
        private Integer hour;
        private BigDecimal sales;
        private Integer orderCount;
    }
}
