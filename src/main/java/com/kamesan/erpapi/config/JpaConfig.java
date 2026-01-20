package com.kamesan.erpapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * JPA 配置
 *
 * <p>配置 JPA Auditing 功能，自動填入以下欄位：</p>
 * <ul>
 *   <li>createdAt - 建立時間</li>
 *   <li>createdBy - 建立者 ID</li>
 *   <li>updatedAt - 更新時間</li>
 *   <li>updatedBy - 更新者 ID</li>
 * </ul>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaConfig {

    /**
     * 提供目前操作者 ID
     *
     * <p>從 Spring Security Context 取得目前登入使用者的 ID。</p>
     *
     * @return AuditorAware 實例
     */
    @Bean
    public AuditorAware<Long> auditorProvider() {
        return () -> {
            // 取得目前的認證資訊
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            // 如果沒有認證或是匿名使用者，回傳空值
            if (authentication == null ||
                    !authentication.isAuthenticated() ||
                    "anonymousUser".equals(authentication.getPrincipal())) {
                return Optional.empty();
            }

            // 取得使用者 ID
            Object principal = authentication.getPrincipal();
            if (principal instanceof Long) {
                return Optional.of((Long) principal);
            } else if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
                // 如果是 UserDetails，嘗試從 username 取得 ID
                // 實際實作中，應該使用自訂的 UserDetails 類別
                try {
                    String username = ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
                    return Optional.of(Long.parseLong(username));
                } catch (NumberFormatException e) {
                    return Optional.empty();
                }
            }

            return Optional.empty();
        };
    }
}
