package com.kamesan.erpapi.reports.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 利潤分析報表 DTO
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfitAnalysisDto {

    private LocalDate startDate;
    private LocalDate endDate;
    private Long storeId;

    // 整體摘要
    private BigDecimal totalRevenue;
    private BigDecimal totalCost;
    private BigDecimal grossProfit;
    private BigDecimal grossProfitMargin;
    private Integer orderCount;
    private BigDecimal averageOrderValue;
    private BigDecimal averageOrderProfit;

    // 商品利潤排行
    private List<ProductProfit> topProfitProducts;
    private List<ProductProfit> bottomProfitProducts;

    // 分類利潤分析
    private List<CategoryProfit> categoryProfits;

    // 每日利潤趨勢
    private List<DailyProfit> dailyProfits;

    /**
     * 商品利潤明細
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProductProfit {
        private Long productId;
        private String productSku;
        private String productName;
        private String categoryName;
        private Integer quantitySold;
        private BigDecimal revenue;
        private BigDecimal cost;
        private BigDecimal grossProfit;
        private BigDecimal profitMargin;
        private BigDecimal contributionRatio; // 佔總利潤百分比
    }

    /**
     * 分類利潤分析
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CategoryProfit {
        private Long categoryId;
        private String categoryName;
        private Integer productCount;
        private Integer quantitySold;
        private BigDecimal revenue;
        private BigDecimal cost;
        private BigDecimal grossProfit;
        private BigDecimal profitMargin;
        private BigDecimal contributionRatio;
    }

    /**
     * 每日利潤
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DailyProfit {
        private LocalDate date;
        private Integer orderCount;
        private BigDecimal revenue;
        private BigDecimal cost;
        private BigDecimal grossProfit;
        private BigDecimal profitMargin;
    }
}
