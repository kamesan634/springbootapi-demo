package com.kamesan.erpapi.reports.entity;

import com.kamesan.erpapi.common.entity.BaseEntity;
import com.kamesan.erpapi.reports.entity.ReportSchedule.ExecutionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 報表執行記錄實體
 *
 * <p>記錄每次排程報表的執行結果：</p>
 * <ul>
 *   <li>執行時間</li>
 *   <li>執行狀態</li>
 *   <li>產生的檔案路徑</li>
 *   <li>錯誤訊息（如果有）</li>
 * </ul>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Entity
@Table(name = "report_executions", indexes = {
        @Index(name = "idx_report_executions_schedule_id", columnList = "schedule_id"),
        @Index(name = "idx_report_executions_status", columnList = "status"),
        @Index(name = "idx_report_executions_start_time", columnList = "start_time")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportExecution extends BaseEntity {

    /**
     * 所屬排程
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    private ReportSchedule schedule;

    /**
     * 開始時間
     */
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    /**
     * 結束時間
     */
    @Column(name = "end_time")
    private LocalDateTime endTime;

    /**
     * 執行狀態
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private ExecutionStatus status = ExecutionStatus.RUNNING;

    /**
     * 產生的檔案路徑
     */
    @Column(name = "file_path", length = 500)
    private String filePath;

    /**
     * 檔案大小（bytes）
     */
    @Column(name = "file_size")
    private Long fileSize;

    /**
     * 執行時使用的參數（JSON）
     */
    @Column(name = "parameters", columnDefinition = "TEXT")
    private String parameters;

    /**
     * 錯誤訊息
     */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    /**
     * 是否已通知
     */
    @Column(name = "is_notified")
    @Builder.Default
    private boolean notified = false;

    /**
     * 通知時間
     */
    @Column(name = "notified_at")
    private LocalDateTime notifiedAt;

    /**
     * 執行者 ID（手動執行時記錄）
     */
    @Column(name = "executed_by")
    private Long executedBy;

    /**
     * 是否為手動執行
     */
    @Column(name = "is_manual")
    @Builder.Default
    private boolean manual = false;

    /**
     * 計算執行時間（秒）
     */
    public Long calculateDurationSeconds() {
        if (startTime == null || endTime == null) {
            return null;
        }
        return java.time.Duration.between(startTime, endTime).getSeconds();
    }

    /**
     * 標記為完成
     */
    public void markCompleted(String filePath, Long fileSize) {
        this.status = ExecutionStatus.SUCCESS;
        this.endTime = LocalDateTime.now();
        this.filePath = filePath;
        this.fileSize = fileSize;
    }

    /**
     * 標記為失敗
     */
    public void markFailed(String errorMessage) {
        this.status = ExecutionStatus.FAILED;
        this.endTime = LocalDateTime.now();
        this.errorMessage = errorMessage;
    }
}
