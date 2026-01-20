package com.kamesan.erpapi.system.service;

import com.kamesan.erpapi.system.dto.AuditLogDto;
import com.kamesan.erpapi.system.entity.AuditLog;
import com.kamesan.erpapi.system.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 稽核日誌服務
 *
 * <p>提供稽核日誌的記錄和查詢功能</p>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    /**
     * 非同步記錄稽核日誌
     */
    @Async
    @Transactional
    public void logAsync(String action, String entityType, Long entityId, String entityName,
                         String oldValue, String newValue, Long userId, String username,
                         String ipAddress, String userAgent, String requestUrl, String requestMethod) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .entityName(entityName)
                    .oldValue(oldValue)
                    .newValue(newValue)
                    .userId(userId)
                    .username(username)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .requestUrl(requestUrl)
                    .requestMethod(requestMethod)
                    .build();

            auditLogRepository.save(auditLog);
            log.debug("稽核日誌已記錄：{} - {} - {}", action, entityType, entityId);
        } catch (Exception e) {
            log.error("記錄稽核日誌失敗：{}", e.getMessage(), e);
        }
    }

    /**
     * 記錄稽核日誌（同步）
     */
    @Transactional
    public AuditLogDto log(String action, String entityType, Long entityId, String entityName,
                           Long userId, String username) {
        AuditLog auditLog = AuditLog.builder()
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .entityName(entityName)
                .userId(userId)
                .username(username)
                .build();

        AuditLog saved = auditLogRepository.save(auditLog);
        return convertToDto(saved);
    }

    /**
     * 根據實體查詢日誌
     */
    @Transactional(readOnly = true)
    public List<AuditLogDto> getLogsByEntity(String entityType, Long entityId) {
        return auditLogRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(entityType, entityId)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 根據使用者查詢日誌
     */
    @Transactional(readOnly = true)
    public Page<AuditLogDto> getLogsByUserId(Long userId, Pageable pageable) {
        return auditLogRepository.findByUserId(userId, pageable)
                .map(this::convertToDto);
    }

    /**
     * 根據操作類型查詢日誌
     */
    @Transactional(readOnly = true)
    public Page<AuditLogDto> getLogsByAction(String action, Pageable pageable) {
        return auditLogRepository.findByAction(action, pageable)
                .map(this::convertToDto);
    }

    /**
     * 根據時間範圍查詢日誌
     */
    @Transactional(readOnly = true)
    public Page<AuditLogDto> getLogsByTimeRange(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable) {
        return auditLogRepository.findByCreatedAtBetween(startTime, endTime, pageable)
                .map(this::convertToDto);
    }

    /**
     * 綜合條件查詢日誌
     */
    @Transactional(readOnly = true)
    public Page<AuditLogDto> queryLogs(String action, String entityType, Long userId,
                                        LocalDateTime startTime, LocalDateTime endTime, Pageable pageable) {
        return auditLogRepository.findByConditions(action, entityType, userId, startTime, endTime, pageable)
                .map(this::convertToDto);
    }

    private AuditLogDto convertToDto(AuditLog auditLog) {
        return AuditLogDto.builder()
                .id(auditLog.getId())
                .action(auditLog.getAction())
                .entityType(auditLog.getEntityType())
                .entityId(auditLog.getEntityId())
                .entityName(auditLog.getEntityName())
                .oldValue(auditLog.getOldValue())
                .newValue(auditLog.getNewValue())
                .userId(auditLog.getUserId())
                .username(auditLog.getUsername())
                .ipAddress(auditLog.getIpAddress())
                .userAgent(auditLog.getUserAgent())
                .requestUrl(auditLog.getRequestUrl())
                .requestMethod(auditLog.getRequestMethod())
                .storeId(auditLog.getStoreId())
                .createdAt(auditLog.getCreatedAt())
                .build();
    }
}
