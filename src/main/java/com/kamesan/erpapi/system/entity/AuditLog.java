package com.kamesan.erpapi.system.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 稽核日誌實體
 *
 * <p>記錄系統中所有重要操作的日誌，包括：</p>
 * <ul>
 *   <li>資料的新增、修改、刪除操作</li>
 *   <li>使用者登入、登出操作</li>
 *   <li>操作者資訊及來源 IP</li>
 * </ul>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Entity
@Table(name = "audit_logs", indexes = {
        @Index(name = "idx_audit_logs_action", columnList = "action"),
        @Index(name = "idx_audit_logs_entity", columnList = "entity_type, entity_id"),
        @Index(name = "idx_audit_logs_user", columnList = "user_id"),
        @Index(name = "idx_audit_logs_created", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * 操作類型 (CREATE/UPDATE/DELETE/LOGIN/LOGOUT)
     */
    @Column(name = "action", nullable = false, length = 50)
    private String action;

    /**
     * 實體類型 (如 User, Product, Order)
     */
    @Column(name = "entity_type", nullable = false, length = 100)
    private String entityType;

    /**
     * 實體 ID
     */
    @Column(name = "entity_id")
    private Long entityId;

    /**
     * 實體名稱/描述
     */
    @Column(name = "entity_name", length = 200)
    private String entityName;

    /**
     * 變更前的值 (JSON)
     */
    @Column(name = "old_value", columnDefinition = "JSON")
    private String oldValue;

    /**
     * 變更後的值 (JSON)
     */
    @Column(name = "new_value", columnDefinition = "JSON")
    private String newValue;

    /**
     * 操作者 ID
     */
    @Column(name = "user_id")
    private Long userId;

    /**
     * 操作者帳號
     */
    @Column(name = "username", length = 50)
    private String username;

    /**
     * IP 位址
     */
    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    /**
     * 使用者代理
     */
    @Column(name = "user_agent", length = 500)
    private String userAgent;

    /**
     * 請求 URL
     */
    @Column(name = "request_url", length = 500)
    private String requestUrl;

    /**
     * HTTP 方法
     */
    @Column(name = "request_method", length = 10)
    private String requestMethod;

    /**
     * 門市 ID
     */
    @Column(name = "store_id")
    private Long storeId;

    /**
     * 建立時間
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
