package com.kamesan.erpapi.reports.dto;

import com.kamesan.erpapi.reports.entity.ReportSchedule.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 排程報表 DTO
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportScheduleDto {

    private Long id;
    private String name;
    private String description;
    private ReportType reportType;
    private String reportTypeLabel;
    private ScheduleFrequency frequency;
    private String frequencyLabel;
    private String cronExpression;
    private Integer runHour;
    private Integer runMinute;
    private Integer dayOfWeek;
    private Integer dayOfMonth;
    private ExportFormat exportFormat;
    private String exportFormatLabel;
    private Map<String, Object> parameters;
    private NotificationType notificationType;
    private String notificationTypeLabel;
    private String recipients;
    private boolean active;
    private LocalDateTime lastRunTime;
    private LocalDateTime nextRunTime;
    private ExecutionStatus lastRunStatus;
    private String lastRunStatusLabel;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * 建立排程請求
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateScheduleRequest {
        private String name;
        private String description;
        private ReportType reportType;
        private ScheduleFrequency frequency;
        private String cronExpression;
        private Integer runHour;
        private Integer runMinute;
        private Integer dayOfWeek;
        private Integer dayOfMonth;
        private ExportFormat exportFormat;
        private Map<String, Object> parameters;
        private NotificationType notificationType;
        private String recipients;
    }

    /**
     * 更新排程請求
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateScheduleRequest {
        private String name;
        private String description;
        private ScheduleFrequency frequency;
        private String cronExpression;
        private Integer runHour;
        private Integer runMinute;
        private Integer dayOfWeek;
        private Integer dayOfMonth;
        private ExportFormat exportFormat;
        private Map<String, Object> parameters;
        private NotificationType notificationType;
        private String recipients;
        private boolean active;
    }

    /**
     * 執行記錄 DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ExecutionDto {
        private Long id;
        private Long scheduleId;
        private String scheduleName;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Long durationSeconds;
        private ExecutionStatus status;
        private String statusLabel;
        private String filePath;
        private Long fileSize;
        private String errorMessage;
        private boolean notified;
        private LocalDateTime notifiedAt;
        private Long executedBy;
        private boolean manual;
        private LocalDateTime createdAt;
    }

    /**
     * 排程統計
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ScheduleStatsDto {
        private Integer totalSchedules;
        private Integer activeSchedules;
        private Integer todayExecutions;
        private Integer successExecutions;
        private Integer failedExecutions;
        private List<ReportTypeCount> countByReportType;
    }

    /**
     * 按報表類型統計
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReportTypeCount {
        private ReportType reportType;
        private String label;
        private Integer count;
    }
}
