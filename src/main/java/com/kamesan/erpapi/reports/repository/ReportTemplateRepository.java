package com.kamesan.erpapi.reports.repository;

import com.kamesan.erpapi.reports.entity.ReportSchedule.ReportType;
import com.kamesan.erpapi.reports.entity.ReportTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 報表範本 Repository
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Repository
public interface ReportTemplateRepository extends JpaRepository<ReportTemplate, Long> {

    /**
     * 根據報表類型查詢
     */
    List<ReportTemplate> findByReportType(ReportType reportType);

    /**
     * 查詢公開範本
     */
    List<ReportTemplate> findByIsPublicTrue();

    /**
     * 查詢系統範本
     */
    List<ReportTemplate> findByIsSystemTrue();

    /**
     * 查詢用戶的範本
     */
    List<ReportTemplate> findByOwnerId(Long ownerId);

    /**
     * 查詢用戶可見的範本（自己的 + 公開的 + 系統的）
     */
    @Query("SELECT t FROM ReportTemplate t WHERE t.ownerId = :userId OR t.isPublic = true OR t.isSystem = true")
    List<ReportTemplate> findAccessibleByUser(@Param("userId") Long userId);

    /**
     * 綜合條件查詢
     */
    @Query("SELECT t FROM ReportTemplate t WHERE " +
           "(:keyword IS NULL OR t.name LIKE %:keyword% OR t.description LIKE %:keyword%) AND " +
           "(:reportType IS NULL OR t.reportType = :reportType) AND " +
           "(t.ownerId = :userId OR t.isPublic = true OR t.isSystem = true)")
    Page<ReportTemplate> findByConditions(
            @Param("keyword") String keyword,
            @Param("reportType") ReportType reportType,
            @Param("userId") Long userId,
            Pageable pageable);

    /**
     * 查詢熱門範本
     */
    @Query("SELECT t FROM ReportTemplate t WHERE t.isPublic = true OR t.isSystem = true ORDER BY t.usageCount DESC")
    List<ReportTemplate> findPopularTemplates(Pageable pageable);

    /**
     * 統計各報表類型的範本數量
     */
    @Query("SELECT t.reportType, COUNT(t) FROM ReportTemplate t GROUP BY t.reportType")
    List<Object[]> countByReportType();
}
