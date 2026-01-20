package com.kamesan.erpapi.system.repository;

import com.kamesan.erpapi.system.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 稽核日誌 Repository
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    /**
     * 根據實體類型和實體 ID 查詢日誌
     */
    List<AuditLog> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(String entityType, Long entityId);

    /**
     * 根據使用者 ID 查詢日誌
     */
    Page<AuditLog> findByUserId(Long userId, Pageable pageable);

    /**
     * 根據操作類型查詢日誌
     */
    Page<AuditLog> findByAction(String action, Pageable pageable);

    /**
     * 根據時間範圍查詢日誌
     */
    Page<AuditLog> findByCreatedAtBetween(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

    /**
     * 綜合條件查詢
     */
    @Query("SELECT a FROM AuditLog a WHERE " +
            "(:action IS NULL OR a.action = :action) AND " +
            "(:entityType IS NULL OR a.entityType = :entityType) AND " +
            "(:userId IS NULL OR a.userId = :userId) AND " +
            "(:startTime IS NULL OR a.createdAt >= :startTime) AND " +
            "(:endTime IS NULL OR a.createdAt <= :endTime) " +
            "ORDER BY a.createdAt DESC")
    Page<AuditLog> findByConditions(
            @Param("action") String action,
            @Param("entityType") String entityType,
            @Param("userId") Long userId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            Pageable pageable
    );
}
