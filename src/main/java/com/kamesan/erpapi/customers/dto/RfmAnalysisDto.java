package com.kamesan.erpapi.customers.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * RFM 客戶分析 DTO
 *
 * <p>RFM 分析包含：</p>
 * <ul>
 *   <li>R (Recency) - 最近一次消費距今天數</li>
 *   <li>F (Frequency) - 消費頻率</li>
 *   <li>M (Monetary) - 消費金額</li>
 * </ul>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RfmAnalysisDto {

    private LocalDate analysisDate;
    private LocalDate startDate;
    private LocalDate endDate;

    // 摘要統計
    private Integer totalCustomers;
    private Integer analyzedCustomers;
    private Map<String, Integer> segmentCounts;

    // 分群列表
    private List<CustomerRfm> customers;

    // 分群摘要
    private List<SegmentSummary> segmentSummaries;

    /**
     * 客戶 RFM 資料
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CustomerRfm {
        private Long customerId;
        private String customerName;
        private String customerCode;
        private String email;
        private String phone;

        // RFM 指標
        private LocalDate lastPurchaseDate;
        private Integer recencyDays;
        private Integer frequency;
        private BigDecimal monetary;

        // RFM 分數 (1-5)
        private Integer recencyScore;
        private Integer frequencyScore;
        private Integer monetaryScore;
        private String rfmScore;  // 例如 "543"

        // 客戶分群
        private CustomerSegment segment;
    }

    /**
     * 分群摘要
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SegmentSummary {
        private CustomerSegment segment;
        private Integer customerCount;
        private BigDecimal percentage;
        private BigDecimal avgRecency;
        private BigDecimal avgFrequency;
        private BigDecimal avgMonetary;
        private BigDecimal totalRevenue;
        private String recommendation;
    }

    /**
     * 客戶分群類型
     */
    public enum CustomerSegment {
        CHAMPIONS("冠軍客戶", "高價值、高頻率、最近購買"),
        LOYAL_CUSTOMERS("忠誠客戶", "經常購買的客戶"),
        POTENTIAL_LOYALIST("潛力忠誠客戶", "最近購買且消費不錯"),
        NEW_CUSTOMERS("新客戶", "最近才開始購買"),
        PROMISING("有潛力客戶", "最近購買但頻率低"),
        NEEDS_ATTENTION("需要關注", "曾經是好客戶但最近沒購買"),
        ABOUT_TO_SLEEP("即將流失", "很久沒購買"),
        AT_RISK("有流失風險", "曾經高消費但很久沒來"),
        CANT_LOSE_THEM("不能失去", "高價值但很久沒購買"),
        HIBERNATING("休眠客戶", "長時間沒購買"),
        LOST("流失客戶", "極長時間沒購買");

        private final String label;
        private final String description;

        CustomerSegment(String label, String description) {
            this.label = label;
            this.description = description;
        }

        public String getLabel() {
            return label;
        }

        public String getDescription() {
            return description;
        }
    }
}
