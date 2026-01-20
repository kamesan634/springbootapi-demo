package com.kamesan.erpapi.reports.entity;

import com.kamesan.erpapi.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * 報表範本實體
 *
 * <p>用於定義自訂報表範本：</p>
 * <ul>
 *   <li>範本名稱和描述</li>
 *   <li>欄位選擇和排序</li>
 *   <li>篩選條件</li>
 *   <li>分組和彙總設定</li>
 *   <li>樣式設定</li>
 * </ul>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Entity
@Table(name = "report_templates", indexes = {
        @Index(name = "idx_report_templates_type", columnList = "report_type"),
        @Index(name = "idx_report_templates_public", columnList = "is_public"),
        @Index(name = "idx_report_templates_owner", columnList = "owner_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportTemplate extends BaseEntity {

    /**
     * 範本名稱
     */
    @Column(name = "name", nullable = false, length = 200)
    private String name;

    /**
     * 範本描述
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * 報表類型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "report_type", nullable = false, length = 50)
    private ReportSchedule.ReportType reportType;

    /**
     * 欄位設定（JSON 格式）
     * 例如：[{"field": "orderNo", "label": "訂單編號", "visible": true, "order": 1}]
     */
    @Column(name = "columns_config", columnDefinition = "TEXT")
    private String columnsConfig;

    /**
     * 篩選條件（JSON 格式）
     * 例如：[{"field": "status", "operator": "=", "value": "PAID"}]
     */
    @Column(name = "filters_config", columnDefinition = "TEXT")
    private String filtersConfig;

    /**
     * 分組設定（JSON 格式）
     * 例如：[{"field": "category", "order": "ASC"}]
     */
    @Column(name = "grouping_config", columnDefinition = "TEXT")
    private String groupingConfig;

    /**
     * 排序設定（JSON 格式）
     * 例如：[{"field": "orderDate", "direction": "DESC"}]
     */
    @Column(name = "sorting_config", columnDefinition = "TEXT")
    private String sortingConfig;

    /**
     * 彙總設定（JSON 格式）
     * 例如：[{"field": "totalAmount", "aggregation": "SUM"}]
     */
    @Column(name = "summary_config", columnDefinition = "TEXT")
    private String summaryConfig;

    /**
     * 樣式設定（JSON 格式）
     * 例如：{"headerColor": "#4472C4", "fontSize": 10}
     */
    @Column(name = "style_config", columnDefinition = "TEXT")
    private String styleConfig;

    /**
     * 頁首文字
     */
    @Column(name = "header_text", length = 500)
    private String headerText;

    /**
     * 頁尾文字
     */
    @Column(name = "footer_text", length = 500)
    private String footerText;

    /**
     * 是否為公開範本
     */
    @Column(name = "is_public", nullable = false)
    @Builder.Default
    private boolean isPublic = false;

    /**
     * 擁有者 ID
     */
    @Column(name = "owner_id")
    private Long ownerId;

    /**
     * 是否為系統預設範本
     */
    @Column(name = "is_system", nullable = false)
    @Builder.Default
    private boolean isSystem = false;

    /**
     * 使用次數
     */
    @Column(name = "usage_count")
    @Builder.Default
    private Integer usageCount = 0;

    /**
     * 增加使用次數
     */
    public void incrementUsageCount() {
        this.usageCount = (this.usageCount == null ? 0 : this.usageCount) + 1;
    }
}
