package com.kamesan.erpapi.accounts.entity;

import com.kamesan.erpapi.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * 使用者實體
 *
 * <p>系統使用者資訊，包含帳號、密碼、角色等。</p>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    /**
     * 使用者名稱（帳號）
     * <p>唯一識別碼，用於登入</p>
     */
    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    /**
     * 密碼
     * <p>使用 BCrypt 加密儲存</p>
     */
    @Column(name = "password", nullable = false, length = 255)
    private String password;

    /**
     * 姓名
     */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /**
     * Email
     */
    @Column(name = "email", unique = true, length = 100)
    private String email;

    /**
     * 手機號碼
     */
    @Column(name = "phone", length = 20)
    private String phone;

    /**
     * 角色
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    /**
     * 所屬門市/倉庫
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_stores",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "store_id")
    )
    @Builder.Default
    private Set<Store> stores = new HashSet<>();

    /**
     * 頭像 URL
     */
    @Column(name = "avatar", length = 255)
    private String avatar;

    /**
     * 是否啟用
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;

    /**
     * 最後登入時間
     */
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    /**
     * 最後登入 IP
     */
    @Column(name = "last_login_ip", length = 50)
    private String lastLoginIp;

    /**
     * 登入失敗次數
     */
    @Column(name = "login_fail_count")
    @Builder.Default
    private Integer loginFailCount = 0;

    /**
     * 帳號鎖定時間
     */
    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;

    /**
     * 密碼最後修改時間
     */
    @Column(name = "password_changed_at")
    private LocalDateTime passwordChangedAt;

    /**
     * 備註
     */
    @Column(name = "notes", length = 500)
    private String notes;

    /**
     * 判斷帳號是否被鎖定
     *
     * @return 是否被鎖定
     */
    public boolean isLocked() {
        if (lockedUntil == null) {
            return false;
        }
        return LocalDateTime.now().isBefore(lockedUntil);
    }

    /**
     * 增加登入失敗次數
     */
    public void incrementLoginFailCount() {
        this.loginFailCount = (this.loginFailCount == null ? 0 : this.loginFailCount) + 1;
    }

    /**
     * 重置登入失敗次數
     */
    public void resetLoginFailCount() {
        this.loginFailCount = 0;
        this.lockedUntil = null;
    }

    /**
     * 記錄登入成功
     *
     * @param ip 登入 IP
     */
    public void recordLoginSuccess(String ip) {
        this.lastLoginAt = LocalDateTime.now();
        this.lastLoginIp = ip;
        resetLoginFailCount();
    }

    /**
     * 鎖定帳號
     *
     * @param minutes 鎖定分鐘數
     */
    public void lock(int minutes) {
        this.lockedUntil = LocalDateTime.now().plusMinutes(minutes);
    }

    /**
     * 檢查密碼是否過期
     *
     * @param expirationDays 密碼有效天數
     * @return 是否過期
     */
    public boolean isPasswordExpired(int expirationDays) {
        if (expirationDays <= 0) {
            return false; // 不啟用密碼過期
        }
        if (passwordChangedAt == null) {
            return true; // 從未修改過密碼，視為已過期
        }
        LocalDateTime expirationDate = passwordChangedAt.plusDays(expirationDays);
        return LocalDateTime.now().isAfter(expirationDate);
    }

    /**
     * 計算密碼剩餘有效天數
     *
     * @param expirationDays 密碼有效天數
     * @return 剩餘天數（負數表示已過期）
     */
    public long getPasswordRemainingDays(int expirationDays) {
        if (expirationDays <= 0 || passwordChangedAt == null) {
            return -1;
        }
        LocalDateTime expirationDate = passwordChangedAt.plusDays(expirationDays);
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDateTime.now(), expirationDate);
    }
}
