package com.kamesan.erpapi.security;

import com.kamesan.erpapi.accounts.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 使用者主體
 *
 * <p>實作 Spring Security 的 UserDetails 介面，
 * 用於儲存認證後的使用者資訊。</p>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPrincipal implements UserDetails {

    /**
     * 使用者 ID
     */
    private Long id;

    /**
     * 使用者名稱（帳號）
     */
    private String username;

    /**
     * 密碼
     */
    private String password;

    /**
     * 姓名
     */
    private String name;

    /**
     * Email
     */
    private String email;

    /**
     * 是否啟用
     */
    private boolean enabled;

    /**
     * 權限列表
     */
    private Collection<? extends GrantedAuthority> authorities;

    /**
     * 從 User 實體建立 UserPrincipal
     *
     * @param user   使用者實體
     * @param roles  角色列表
     * @return UserPrincipal
     */
    public static UserPrincipal create(User user, List<String> roles) {
        // 將角色轉換為 GrantedAuthority
        List<GrantedAuthority> authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());

        return UserPrincipal.builder()
                .id(user.getId())
                .username(user.getUsername())
                .password(user.getPassword())
                .name(user.getName())
                .email(user.getEmail())
                .enabled(user.isActive())
                .authorities(authorities)
                .build();
    }

    /**
     * 取得權限列表
     *
     * @return 權限列表
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    /**
     * 取得密碼
     *
     * @return 密碼
     */
    @Override
    public String getPassword() {
        return password;
    }

    /**
     * 取得使用者名稱
     *
     * @return 使用者名稱
     */
    @Override
    public String getUsername() {
        return username;
    }

    /**
     * 帳號是否未過期
     *
     * @return true
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * 帳號是否未被鎖定
     *
     * @return true
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * 密碼是否未過期
     *
     * @return true
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * 帳號是否啟用
     *
     * @return 是否啟用
     */
    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
