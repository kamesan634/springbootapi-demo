package com.kamesan.erpapi.accounts.entity;

import com.kamesan.erpapi.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * 門市/倉庫實體
 *
 * <p>用於管理門市和倉庫資訊。</p>
 *
 * <h2>類型：</h2>
 * <ul>
 *   <li>STORE - 門市</li>
 *   <li>WAREHOUSE - 倉庫</li>
 * </ul>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Entity
@Table(name = "stores")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Store extends BaseEntity {

    /**
     * 門市/倉庫類型
     */
    public enum StoreType {
        /** 門市 */
        STORE,
        /** 倉庫 */
        WAREHOUSE
    }

    /**
     * 門市/倉庫代碼
     * <p>唯一識別碼，如：S001、W001</p>
     */
    @Column(name = "code", nullable = false, unique = true, length = 20)
    private String code;

    /**
     * 門市/倉庫名稱
     */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /**
     * 類型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private StoreType type;

    /**
     * 地址
     */
    @Column(name = "address", length = 500)
    private String address;

    /**
     * 電話
     */
    @Column(name = "phone", length = 20)
    private String phone;

    /**
     * Email
     */
    @Column(name = "email", length = 100)
    private String email;

    /**
     * 負責人
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private User manager;

    /**
     * 營業時間
     * <p>格式：09:00-22:00</p>
     */
    @Column(name = "business_hours", length = 50)
    private String businessHours;

    /**
     * 是否為總部/主倉庫
     */
    @Column(name = "is_main", nullable = false)
    @Builder.Default
    private boolean main = false;

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
     * 備註
     */
    @Column(name = "notes", length = 500)
    private String notes;
}
