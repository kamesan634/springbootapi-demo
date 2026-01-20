package com.kamesan.erpapi.reports.dto;

import com.kamesan.erpapi.reports.entity.ReportSchedule.ReportType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 報表範本 DTO
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportTemplateDto {

    private Long id;
    private String name;
    private String description;
    private ReportType reportType;
    private String reportTypeLabel;
    private List<ColumnConfig> columns;
    private List<FilterConfig> filters;
    private List<GroupingConfig> grouping;
    private List<SortingConfig> sorting;
    private List<SummaryConfig> summary;
    private StyleConfig style;
    private String headerText;
    private String footerText;
    private boolean isPublic;
    private Long ownerId;
    private String ownerName;
    private boolean isSystem;
    private Integer usageCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * 欄位設定
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ColumnConfig {
        private String field;
        private String label;
        private boolean visible;
        private Integer order;
        private Integer width;
        private String format;
        private String alignment;
    }

    /**
     * 篩選條件
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FilterConfig {
        private String field;
        private String operator;
        private Object value;
        private String valueType;
    }

    /**
     * 分組設定
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GroupingConfig {
        private String field;
        private String order;
        private boolean showSubtotal;
    }

    /**
     * 排序設定
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SortingConfig {
        private String field;
        private String direction;
    }

    /**
     * 彙總設定
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SummaryConfig {
        private String field;
        private String aggregation;
        private String label;
    }

    /**
     * 樣式設定
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StyleConfig {
        private String headerColor;
        private String headerFontColor;
        private Integer fontSize;
        private String fontFamily;
        private boolean showGridLines;
        private boolean zebra;
        private String orientation;
        private String pageSize;
    }

    /**
     * 建立範本請求
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateTemplateRequest {
        private String name;
        private String description;
        private ReportType reportType;
        private List<ColumnConfig> columns;
        private List<FilterConfig> filters;
        private List<GroupingConfig> grouping;
        private List<SortingConfig> sorting;
        private List<SummaryConfig> summary;
        private StyleConfig style;
        private String headerText;
        private String footerText;
        private boolean isPublic;
    }

    /**
     * 更新範本請求
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateTemplateRequest {
        private String name;
        private String description;
        private List<ColumnConfig> columns;
        private List<FilterConfig> filters;
        private List<GroupingConfig> grouping;
        private List<SortingConfig> sorting;
        private List<SummaryConfig> summary;
        private StyleConfig style;
        private String headerText;
        private String footerText;
        private boolean isPublic;
    }

    /**
     * 可用欄位資訊
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AvailableField {
        private String field;
        private String label;
        private String dataType;
        private boolean sortable;
        private boolean filterable;
        private boolean aggregatable;
        private List<String> availableOperators;
    }

    /**
     * 報表類型欄位定義
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReportTypeDefinition {
        private ReportType reportType;
        private String label;
        private List<AvailableField> availableFields;
    }
}
