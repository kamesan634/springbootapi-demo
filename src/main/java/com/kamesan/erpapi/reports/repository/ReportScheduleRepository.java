package com.kamesan.erpapi.reports.repository;

import com.kamesan.erpapi.reports.entity.ReportSchedule;
import com.kamesan.erpapi.reports.entity.ReportSchedule.ReportType;
import com.kamesan.erpapi.reports.entity.ReportSchedule.ScheduleFrequency;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 排程報表 Repository
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Repository
public interface ReportScheduleRepository extends JpaRepository<ReportSchedule, Long> {

    /**
     * 查詢啟用的排程
     */
    List<ReportSchedule> findByActiveTrue();

    /**
     * 根據報表類型查詢
     */
    List<ReportSchedule> findByReportType(ReportType reportType);

    /**
     * 根據頻率查詢
     */
    List<ReportSchedule> findByFrequency(ScheduleFrequency frequency);

    /**
     * 查詢需要執行的排程
     */
    @Query("SELECT s FROM ReportSchedule s WHERE s.active = true " +
           "AND s.nextRunTime IS NOT NULL " +
           "AND s.nextRunTime <= :now")
    List<ReportSchedule> findSchedulesToRun(@Param("now") LocalDateTime now);

    /**
     * 綜合條件查詢
     */
    @Query("SELECT s FROM ReportSchedule s WHERE " +
           "(:keyword IS NULL OR s.name LIKE %:keyword%) AND " +
           "(:reportType IS NULL OR s.reportType = :reportType) AND " +
           "(:active IS NULL OR s.active = :active)")
    Page<ReportSchedule> findByConditions(
            @Param("keyword") String keyword,
            @Param("reportType") ReportType reportType,
            @Param("active") Boolean active,
            Pageable pageable);

    /**
     * 抓取排程及最近的執行記錄
     */
    @Query("SELECT s FROM ReportSchedule s LEFT JOIN FETCH s.executions e WHERE s.id = :id")
    Optional<ReportSchedule> findByIdWithExecutions(@Param("id") Long id);

    /**
     * 統計各報表類型的排程數量
     */
    @Query("SELECT s.reportType, COUNT(s) FROM ReportSchedule s WHERE s.active = true GROUP BY s.reportType")
    List<Object[]> countByReportType();

    /**
     * 統計啟用的排程數量
     */
    @Query("SELECT COUNT(s) FROM ReportSchedule s WHERE s.active = true")
    Long countActiveSchedules();
}
