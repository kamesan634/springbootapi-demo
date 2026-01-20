package com.kamesan.erpapi.system.dto;

import lombok.*;
import java.time.LocalDateTime;

/**
 * 稽核日誌 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLogDto {
    private Long id;
    private String action;
    private String entityType;
    private Long entityId;
    private String entityName;
    private String oldValue;
    private String newValue;
    private Long userId;
    private String username;
    private String ipAddress;
    private String userAgent;
    private String requestUrl;
    private String requestMethod;
    private Long storeId;
    private LocalDateTime createdAt;
}
