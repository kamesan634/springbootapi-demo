package com.kamesan.erpapi.accounts.entity;

import com.kamesan.erpapi.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

/**
 * 角色實體
 *
 * <p>系統角色定義，用於權限控制。</p>
 *
 * <h2>預設角色：</h2>
 * <ul>
 *   <li>ADMIN - 系統管理員</li>
 *   <li>MANAGER - 店長/經理</li>
 *   <li>STAFF - 一般員工</li>
 *   <li>CASHIER - 收銀員</li>
 *   <li>WAREHOUSE - 倉管人員</li>
 * </ul>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role extends BaseEntity {

    /**
     * 角色代碼
     * <p>唯一識別碼，如：ADMIN、MANAGER、STAFF</p>
     */
    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    /**
     * 角色名稱
     */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /**
     * 角色描述
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * 是否為系統角色
     * <p>系統角色不可刪除</p>
     */
    @Column(name = "is_system", nullable = false)
    @Builder.Default
    private boolean system = false;

    /**
     * 是否啟用
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;

    /**
     * 排序順序
     */
    @Column(name = "sort_order")
    @Builder.Default
    private Integer sortOrder = 0;

    /**
     * 權限列表（JSON 格式儲存）
     * <p>儲存格式：["permission1", "permission2", ...]</p>
     */
    @Column(name = "permissions", columnDefinition = "JSON")
    private String permissions;

    /**
     * 角色的使用者
     */
    @OneToMany(mappedBy = "role", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<User> users = new HashSet<>();
}
