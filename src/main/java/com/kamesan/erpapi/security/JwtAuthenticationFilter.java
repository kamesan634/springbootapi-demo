package com.kamesan.erpapi.security;

import com.kamesan.erpapi.security.redis.TokenBlacklistService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 認證過濾器
 *
 * <p>攔截所有請求，驗證 JWT Token 並設定認證資訊。</p>
 *
 * <h2>處理流程：</h2>
 * <ol>
 *   <li>從請求標頭取得 JWT Token</li>
 *   <li>驗證 Token 是否有效</li>
 *   <li>從 Token 取得使用者資訊</li>
 *   <li>設定 Spring Security 認證資訊</li>
 * </ol>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /**
     * JWT Token 提供者
     */
    private final JwtTokenProvider tokenProvider;

    /**
     * 使用者詳細資訊服務
     */
    private final UserDetailsService userDetailsService;

    /**
     * Token 黑名單服務
     */
    private final TokenBlacklistService tokenBlacklistService;

    /**
     * 過濾請求
     *
     * @param request     HTTP 請求
     * @param response    HTTP 回應
     * @param filterChain 過濾器鏈
     * @throws ServletException Servlet 異常
     * @throws IOException      IO 異常
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        try {
            // 取得 JWT Token
            String jwt = getJwtFromRequest(request);

            // 驗證 Token
            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                // 檢查 Token 是否在黑名單中
                if (tokenBlacklistService.isBlacklisted(jwt)) {
                    log.debug("Token 在黑名單中，拒絕存取");
                    filterChain.doFilter(request, response);
                    return;
                }

                // 從 Token 取得使用者 ID
                Long userId = tokenProvider.getUserIdFromToken(jwt);
                String username = tokenProvider.getUsernameFromToken(jwt);

                // 載入使用者資訊
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // 建立認證物件
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                // 設定請求詳細資訊
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 設定 Security Context
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("使用者 {} (ID: {}) 認證成功", username, userId);
            }
        } catch (Exception ex) {
            log.error("無法設定使用者認證資訊: {}", ex.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 從請求標頭取得 JWT Token
     *
     * @param request HTTP 請求
     * @return JWT Token（不含 Bearer 前綴）
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        return null;
    }
}
