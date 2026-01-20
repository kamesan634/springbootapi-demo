package com.kamesan.erpapi.common.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 基礎實體類別
 *
 * <p>所有實體類別的父類別，提供共用欄位：</p>
 * <ul>
 *   <li>id - 主鍵 ID</li>
 *   <li>createdAt - 建立時間</li>
 *   <li>createdBy - 建立者 ID</li>
 *   <li>updatedAt - 更新時間</li>
 *   <li>updatedBy - 更新者 ID</li>
 * </ul>
 *
 * <p>使用 JPA Auditing 自動填入時間和操作者資訊。</p>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity implements Serializable {

    /**
     * 主鍵 ID
     * <p>使用自動遞增策略</p>
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * 建立時間
     * <p>由 JPA Auditing 自動填入，不可更新</p>
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 建立者 ID
     * <p>由 JPA Auditing 自動填入，不可更新</p>
     */
    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private Long createdBy;

    /**
     * 更新時間
     * <p>由 JPA Auditing 自動填入</p>
     */
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 更新者 ID
     * <p>由 JPA Auditing 自動填入</p>
     */
    @LastModifiedBy
    @Column(name = "updated_by")
    private Long updatedBy;
}
