package com.kamesan.erpapi.reports.repository;

import com.kamesan.erpapi.reports.entity.ReportExecution;
import com.kamesan.erpapi.reports.entity.ReportSchedule.ExecutionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 報表執行記錄 Repository
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Repository
public interface ReportExecutionRepository extends JpaRepository<ReportExecution, Long> {

    /**
     * 根據排程 ID 查詢執行記錄（分頁）
     */
    Page<ReportExecution> findByScheduleIdOrderByStartTimeDesc(Long scheduleId, Pageable pageable);

    /**
     * 根據排程 ID 查詢最近的執行記錄
     */
    List<ReportExecution> findTop10ByScheduleIdOrderByStartTimeDesc(Long scheduleId);

    /**
     * 根據狀態查詢
     */
    List<ReportExecution> findByStatus(ExecutionStatus status);

    /**
     * 查詢指定時間範圍內的執行記錄
     */
    @Query("SELECT e FROM ReportExecution e WHERE e.startTime BETWEEN :startTime AND :endTime")
    List<ReportExecution> findByDateRange(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 統計指定時間範圍內的成功執行數
     */
    @Query("SELECT COUNT(e) FROM ReportExecution e WHERE e.status = 'SUCCESS' " +
           "AND e.startTime BETWEEN :startTime AND :endTime")
    Long countSuccessByDateRange(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 統計指定時間範圍內的失敗執行數
     */
    @Query("SELECT COUNT(e) FROM ReportExecution e WHERE e.status = 'FAILED' " +
           "AND e.startTime BETWEEN :startTime AND :endTime")
    Long countFailedByDateRange(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 查詢未通知的成功執行記錄
     */
    @Query("SELECT e FROM ReportExecution e WHERE e.status = 'SUCCESS' AND e.notified = false")
    List<ReportExecution> findUnnotifiedSuccessExecutions();

    /**
     * 查詢指定排程的最後一次執行
     */
    @Query("SELECT e FROM ReportExecution e WHERE e.schedule.id = :scheduleId ORDER BY e.startTime DESC LIMIT 1")
    ReportExecution findLastExecutionByScheduleId(@Param("scheduleId") Long scheduleId);

    /**
     * 統計今日執行數
     */
    @Query("SELECT COUNT(e) FROM ReportExecution e WHERE e.startTime >= :todayStart")
    Long countTodayExecutions(@Param("todayStart") LocalDateTime todayStart);
}
