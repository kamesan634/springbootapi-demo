package com.kamesan.erpapi.reports.entity;

import com.kamesan.erpapi.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 排程報表實體
 *
 * <p>定義報表排程任務：</p>
 * <ul>
 *   <li>報表類型（銷售、庫存、採購等）</li>
 *   <li>排程頻率（每日、每週、每月）</li>
 *   <li>執行時間</li>
 *   <li>匯出格式</li>
 *   <li>通知設定</li>
 * </ul>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Entity
@Table(name = "report_schedules", indexes = {
        @Index(name = "idx_report_schedules_active", columnList = "is_active"),
        @Index(name = "idx_report_schedules_type", columnList = "report_type"),
        @Index(name = "idx_report_schedules_next_run", columnList = "next_run_time")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportSchedule extends BaseEntity {

    /**
     * 排程名稱
     */
    @Column(name = "name", nullable = false, length = 200)
    private String name;

    /**
     * 排程描述
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * 報表類型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "report_type", nullable = false, length = 50)
    private ReportType reportType;

    /**
     * 排程頻率
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "frequency", nullable = false, length = 20)
    private ScheduleFrequency frequency;

    /**
     * Cron 表達式（進階排程用）
     */
    @Column(name = "cron_expression", length = 100)
    private String cronExpression;

    /**
     * 執行時間（時）
     */
    @Column(name = "run_hour")
    @Builder.Default
    private Integer runHour = 8;

    /**
     * 執行時間（分）
     */
    @Column(name = "run_minute")
    @Builder.Default
    private Integer runMinute = 0;

    /**
     * 每週執行日（1-7，週一到週日）
     */
    @Column(name = "day_of_week")
    private Integer dayOfWeek;

    /**
     * 每月執行日（1-31）
     */
    @Column(name = "day_of_month")
    private Integer dayOfMonth;

    /**
     * 匯出格式
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "export_format", nullable = false, length = 20)
    @Builder.Default
    private ExportFormat exportFormat = ExportFormat.EXCEL;

    /**
     * 報表參數（JSON 格式）
     * 例如：{"storeId": 1, "dateRange": "LAST_MONTH"}
     */
    @Column(name = "parameters", columnDefinition = "TEXT")
    private String parameters;

    /**
     * 通知方式
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", length = 20)
    @Builder.Default
    private NotificationType notificationType = NotificationType.EMAIL;

    /**
     * 通知對象（Email 或用戶 ID，多個以逗號分隔）
     */
    @Column(name = "recipients", length = 500)
    private String recipients;

    /**
     * 是否啟用
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;

    /**
     * 上次執行時間
     */
    @Column(name = "last_run_time")
    private LocalDateTime lastRunTime;

    /**
     * 下次執行時間
     */
    @Column(name = "next_run_time")
    private LocalDateTime nextRunTime;

    /**
     * 上次執行狀態
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "last_run_status", length = 20)
    private ExecutionStatus lastRunStatus;

    /**
     * 執行記錄
     */
    @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ReportExecution> executions = new ArrayList<>();

    /**
     * 報表類型
     */
    public enum ReportType {
        SALES("銷售報表"),
        INVENTORY("庫存報表"),
        PURCHASING("採購報表"),
        PROFIT("利潤報表"),
        CUSTOMER_RFM("客戶 RFM 分析"),
        DASHBOARD("儀表板");

        private final String label;

        ReportType(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    /**
     * 排程頻率
     */
    public enum ScheduleFrequency {
        DAILY("每日"),
        WEEKLY("每週"),
        MONTHLY("每月"),
        CUSTOM("自訂");

        private final String label;

        ScheduleFrequency(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    /**
     * 匯出格式
     */
    public enum ExportFormat {
        EXCEL("Excel"),
        CSV("CSV"),
        PDF("PDF");

        private final String label;

        ExportFormat(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    /**
     * 通知方式
     */
    public enum NotificationType {
        EMAIL("Email"),
        SYSTEM("系統通知"),
        NONE("不通知");

        private final String label;

        NotificationType(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    /**
     * 執行狀態
     */
    public enum ExecutionStatus {
        SUCCESS("成功"),
        FAILED("失敗"),
        RUNNING("執行中");

        private final String label;

        ExecutionStatus(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    /**
     * 新增執行記錄
     */
    public void addExecution(ReportExecution execution) {
        executions.add(execution);
        execution.setSchedule(this);
    }
}
