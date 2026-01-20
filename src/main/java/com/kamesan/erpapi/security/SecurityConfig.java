package com.kamesan.erpapi.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.List;

/**
 * Spring Security 配置
 *
 * <p>配置安全相關設定，包括：</p>
 * <ul>
 *   <li>JWT 認證過濾器</li>
 *   <li>路徑權限設定</li>
 *   <li>密碼編碼器</li>
 *   <li>CORS 設定</li>
 * </ul>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    /**
     * JWT 認證過濾器
     */
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * API 限流過濾器
     */
    private final RateLimitFilter rateLimitFilter;

    /**
     * 使用者詳細資訊服務
     */
    private final UserDetailsService userDetailsService;

    /**
     * CORS 配置來源
     */
    private final CorsConfigurationSource corsConfigurationSource;

    /**
     * 白名單路徑
     */
    @Value("${app.security.whitelist}")
    private List<String> whitelist;

    /**
     * 配置安全過濾器鏈
     *
     * @param http HttpSecurity
     * @return SecurityFilterChain
     * @throws Exception 配置異常
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 停用 CSRF（使用 JWT，不需要 CSRF）
                .csrf(AbstractHttpConfigurer::disable)
                // 啟用 CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                // 設定 Session 管理（無狀態）
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 設定路徑權限
                .authorizeHttpRequests(auth -> auth
                        // 白名單路徑不需要認證
                        .requestMatchers(whitelist.toArray(new String[0])).permitAll()
                        // 其他路徑需要認證
                        .anyRequest().authenticated()
                )
                // 設定認證提供者
                .authenticationProvider(authenticationProvider())
                // 加入 JWT 過濾器（在 UsernamePasswordAuthenticationFilter 之前）
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                // 加入限流過濾器（在 UsernamePasswordAuthenticationFilter 之前，會在 JWT 過濾器之前執行）
                .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * 密碼編碼器
     *
     * <p>使用 BCrypt 演算法進行密碼加密。</p>
     *
     * @return PasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 認證提供者
     *
     * @return AuthenticationProvider
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * 認證管理器
     *
     * @param config AuthenticationConfiguration
     * @return AuthenticationManager
     * @throws Exception 配置異常
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
