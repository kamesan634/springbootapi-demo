package com.kamesan.erpapi.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kamesan.erpapi.common.dto.ApiResponse;
import com.kamesan.erpapi.security.redis.RateLimitService;
import com.kamesan.erpapi.security.redis.RateLimitService.RateLimitResult;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

/**
 * API 限流過濾器
 *
 * <p>攔截所有請求，進行限流檢查。</p>
 *
 * <h2>限流規則：</h2>
 * <ul>
 *   <li>登入 API: 每分鐘最多 10 次</li>
 *   <li>一般 API: 每分鐘最多 100 次</li>
 *   <li>管理 API: 每分鐘最多 60 次</li>
 * </ul>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Slf4j
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;
    private final ObjectMapper objectMapper;

    /**
     * 是否啟用限流
     */
    @Value("${app.rate-limit.enabled:true}")
    private boolean enabled;

    /**
     * 登入 API 限流次數
     */
    @Value("${app.rate-limit.login-limit:10}")
    private int loginLimit;

    /**
     * 登入 API 時間窗口（秒）
     */
    @Value("${app.rate-limit.login-window:60}")
    private int loginWindow;

    /**
     * 一般 API 限流次數
     */
    @Value("${app.rate-limit.api-limit:100}")
    private int apiLimit;

    /**
     * 一般 API 時間窗口（秒）
     */
    @Value("${app.rate-limit.api-window:60}")
    private int apiWindow;

    /**
     * 限流規則映射（端點 -> 限流配置）
     */
    private static final Map<String, int[]> ENDPOINT_LIMITS = Map.of(
            "/api/v1/auth/login", new int[]{10, 60},     // 登入：10次/分鐘
            "/api/v1/auth/refresh", new int[]{30, 60},   // Token刷新：30次/分鐘
            "/api/v1/orders", new int[]{60, 60},         // 訂單：60次/分鐘
            "/api/v1/products", new int[]{200, 60}       // 商品：200次/分鐘
    );

    public RateLimitFilter(RateLimitService rateLimitService, ObjectMapper objectMapper) {
        this.rateLimitService = rateLimitService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        // 如果限流未啟用，直接放行
        if (!enabled) {
            filterChain.doFilter(request, response);
            return;
        }

        // 跳過靜態資源和健康檢查
        String uri = request.getRequestURI();
        if (shouldSkip(uri)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 取得客戶端 IP
        String ip = getClientIp(request);

        // 取得端點對應的限流配置
        int[] limitConfig = getEndpointLimit(uri);
        int limit = limitConfig[0];
        int window = limitConfig[1];

        // 檢查限流
        RateLimitResult result = rateLimitService.checkIpRateLimit(ip, normalizeEndpoint(uri), limit, window);

        // 設定回應標頭
        response.setHeader("X-RateLimit-Limit", String.valueOf(result.getLimit()));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(result.getRemaining()));
        response.setHeader("X-RateLimit-Reset", String.valueOf(result.getResetSeconds()));

        // 如果超過限流
        if (!result.isAllowed()) {
            log.warn("IP {} 觸發限流，端點: {}, 計數: {}/{}", ip, uri, result.getCurrentCount(), limit);

            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");

            ApiResponse<Void> errorResponse = ApiResponse.error(
                    429,
                    String.format("請求過於頻繁，請 %d 秒後再試", result.getResetSeconds())
            );

            response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 判斷是否應跳過限流檢查
     */
    private boolean shouldSkip(String uri) {
        return uri.startsWith("/swagger") ||
                uri.startsWith("/v3/api-docs") ||
                uri.startsWith("/actuator") ||
                uri.equals("/favicon.ico") ||
                uri.startsWith("/static/") ||
                uri.startsWith("/css/") ||
                uri.startsWith("/js/");
    }

    /**
     * 取得端點對應的限流配置
     *
     * @param uri 請求 URI
     * @return [限制次數, 時間窗口]
     */
    private int[] getEndpointLimit(String uri) {
        // 先檢查精確匹配
        for (Map.Entry<String, int[]> entry : ENDPOINT_LIMITS.entrySet()) {
            if (uri.startsWith(entry.getKey())) {
                return entry.getValue();
            }
        }

        // 登入相關 API
        if (uri.contains("/auth/login")) {
            return new int[]{loginLimit, loginWindow};
        }

        // 預設使用一般 API 限流
        return new int[]{apiLimit, apiWindow};
    }

    /**
     * 正規化端點（移除路徑參數）
     *
     * @param uri 請求 URI
     * @return 正規化後的端點
     */
    private String normalizeEndpoint(String uri) {
        // 移除路徑參數（數字 ID）
        return uri.replaceAll("/\\d+", "/{id}");
    }

    /**
     * 取得客戶端 IP
     *
     * @param request HTTP 請求
     * @return 客戶端 IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        // 如果有多個 IP，取第一個
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip;
    }
}
