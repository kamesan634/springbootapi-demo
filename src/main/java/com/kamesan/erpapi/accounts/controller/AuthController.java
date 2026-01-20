package com.kamesan.erpapi.accounts.controller;

import com.kamesan.erpapi.accounts.dto.LoginRequest;
import com.kamesan.erpapi.accounts.dto.LoginResponse;
import com.kamesan.erpapi.accounts.service.AuthService;
import com.kamesan.erpapi.accounts.service.PasswordPolicyService;
import com.kamesan.erpapi.accounts.service.PasswordPolicyService.PasswordPolicyDto;
import com.kamesan.erpapi.common.dto.ApiResponse;
import com.kamesan.erpapi.security.JwtTokenProvider;
import com.kamesan.erpapi.security.redis.OnlineUserService;
import com.kamesan.erpapi.security.redis.TokenBlacklistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * 認證控制器
 *
 * <p>處理使用者認證相關的 API 請求，包括：</p>
 * <ul>
 *   <li>POST /api/v1/auth/login - 使用者登入</li>
 *   <li>POST /api/v1/auth/refresh - 刷新 Token</li>
 *   <li>POST /api/v1/auth/logout - 使用者登出</li>
 * </ul>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "認證管理", description = "使用者登入、登出、Token 刷新")
public class AuthController {

    /**
     * 認證服務
     */
    private final AuthService authService;

    /**
     * 密碼政策服務
     */
    private final PasswordPolicyService passwordPolicyService;

    /**
     * Token 黑名單服務
     */
    private final TokenBlacklistService tokenBlacklistService;

    /**
     * 在線使用者服務
     */
    private final OnlineUserService onlineUserService;

    /**
     * JWT Token 提供者
     */
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 使用者登入
     *
     * @param request        登入請求
     * @param servletRequest HTTP 請求
     * @return 登入回應（包含 Token 和使用者資訊）
     */
    @PostMapping("/login")
    @Operation(summary = "使用者登入", description = "使用帳號密碼登入，成功後回傳 JWT Token")
    public ApiResponse<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest servletRequest) {

        // 取得客戶端 IP
        String ip = getClientIp(servletRequest);

        log.info("使用者 {} 嘗試登入，IP: {}", request.getUsername(), ip);

        LoginResponse response = authService.login(request, ip);

        return ApiResponse.success("登入成功", response);
    }

    /**
     * 刷新 Token
     *
     * @param refreshToken Refresh Token
     * @return 新的登入回應
     */
    @PostMapping("/refresh")
    @Operation(summary = "刷新 Token", description = "使用 Refresh Token 取得新的 Access Token")
    public ApiResponse<LoginResponse> refreshToken(
            @RequestParam String refreshToken) {

        LoginResponse response = authService.refreshToken(refreshToken);

        return ApiResponse.success("Token 刷新成功", response);
    }

    /**
     * 使用者登出
     *
     * @param request HTTP 請求
     * @return 登出結果
     */
    @PostMapping("/logout")
    @Operation(summary = "使用者登出", description = "登出目前使用者，將 Token 加入黑名單")
    public ApiResponse<Void> logout(HttpServletRequest request) {
        // 從請求標頭取得 Token
        String token = extractToken(request);

        if (StringUtils.hasText(token)) {
            try {
                // 取得使用者 ID 並記錄下線
                Long userId = jwtTokenProvider.getUserIdFromToken(token);
                onlineUserService.userOffline(userId);
            } catch (Exception e) {
                log.warn("登出時記錄下線狀態失敗: {}", e.getMessage());
            }

            // 將 Token 加入黑名單
            boolean success = tokenBlacklistService.addToBlacklist(token);
            if (success) {
                log.info("使用者登出成功，Token 已加入黑名單");
                return ApiResponse.success("登出成功", null);
            } else {
                log.warn("登出時將 Token 加入黑名單失敗");
            }
        }

        log.info("使用者登出");
        return ApiResponse.success("登出成功", null);
    }

    /**
     * 從請求中提取 Token
     *
     * @param request HTTP 請求
     * @return Token 字串（不含 Bearer 前綴）
     */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * 取得密碼政策
     *
     * @return 密碼政策
     */
    @GetMapping("/password-policy")
    @Operation(summary = "取得密碼政策", description = "取得系統密碼政策設定")
    public ApiResponse<PasswordPolicyDto> getPasswordPolicy() {
        PasswordPolicyDto policy = passwordPolicyService.getPasswordPolicy();
        return ApiResponse.success(policy);
    }

    /**
     * 強制修改過期密碼
     *
     * @param userId      使用者 ID
     * @param oldPassword 舊密碼
     * @param newPassword 新密碼
     * @return 修改結果
     */
    @PostMapping("/change-expired-password")
    @Operation(summary = "修改過期密碼", description = "密碼過期時強制修改密碼")
    public ApiResponse<Void> changeExpiredPassword(
            @Parameter(description = "使用者 ID") @RequestParam Long userId,
            @Parameter(description = "舊密碼") @RequestParam String oldPassword,
            @Parameter(description = "新密碼") @RequestParam String newPassword) {

        log.info("使用者 {} 嘗試修改過期密碼", userId);
        passwordPolicyService.forceChangeExpiredPassword(userId, oldPassword, newPassword);
        return ApiResponse.success("密碼修改成功", null);
    }

    /**
     * 修改密碼
     *
     * @param userId      使用者 ID
     * @param oldPassword 舊密碼
     * @param newPassword 新密碼
     * @return 修改結果
     */
    @PostMapping("/change-password")
    @Operation(summary = "修改密碼", description = "修改使用者密碼")
    public ApiResponse<Void> changePassword(
            @Parameter(description = "使用者 ID") @RequestParam Long userId,
            @Parameter(description = "舊密碼") @RequestParam String oldPassword,
            @Parameter(description = "新密碼") @RequestParam String newPassword) {

        log.info("使用者 {} 修改密碼", userId);
        authService.changePassword(userId, oldPassword, newPassword);
        return ApiResponse.success("密碼修改成功", null);
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
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        // 如果有多個 IP（經過多個代理），取第一個
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip;
    }
}
