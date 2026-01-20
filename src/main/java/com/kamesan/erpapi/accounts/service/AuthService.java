package com.kamesan.erpapi.accounts.service;

import com.kamesan.erpapi.accounts.dto.LoginRequest;
import com.kamesan.erpapi.accounts.dto.LoginResponse;
import com.kamesan.erpapi.accounts.entity.User;
import com.kamesan.erpapi.accounts.repository.UserRepository;
import com.kamesan.erpapi.common.exception.BusinessException;
import com.kamesan.erpapi.security.JwtTokenProvider;
import com.kamesan.erpapi.security.UserPrincipal;
import com.kamesan.erpapi.security.redis.OnlineUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

/**
 * 認證服務
 *
 * <p>處理使用者認證相關的業務邏輯，包括：</p>
 * <ul>
 *   <li>使用者登入</li>
 *   <li>Token 刷新</li>
 *   <li>密碼重設</li>
 * </ul>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    /**
     * 認證管理器
     */
    private final AuthenticationManager authenticationManager;

    /**
     * JWT Token 提供者
     */
    private final JwtTokenProvider tokenProvider;

    /**
     * 使用者 Repository
     */
    private final UserRepository userRepository;

    /**
     * 密碼編碼器
     */
    private final PasswordEncoder passwordEncoder;

    /**
     * 密碼政策服務
     */
    private final PasswordPolicyService passwordPolicyService;

    /**
     * 在線使用者服務
     */
    private final OnlineUserService onlineUserService;

    /**
     * 最大登入失敗次數
     */
    @Value("${app.security.max-login-attempts:5}")
    private int maxLoginAttempts;

    /**
     * 帳號鎖定時間（分鐘）
     */
    @Value("${app.security.lock-duration-minutes:30}")
    private int lockDurationMinutes;

    /**
     * 使用者登入
     *
     * @param request 登入請求
     * @param ip      登入 IP
     * @return 登入回應（包含 Token 和使用者資訊）
     */
    @Transactional
    public LoginResponse login(LoginRequest request, String ip) {
        // 查詢使用者
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadCredentialsException("帳號或密碼錯誤"));

        // 檢查帳號狀態
        if (!user.isActive()) {
            throw new BusinessException(403, "帳號已停用，請聯繫管理員");
        }

        // 檢查帳號是否被鎖定
        if (user.isLocked()) {
            throw new BusinessException(403, "帳號已被鎖定，請稍後再試");
        }

        try {
            // 執行認證
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            // 設定 Security Context
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 記錄登入成功
            user.recordLoginSuccess(ip);
            userRepository.save(user);

            log.info("使用者 {} 登入成功，IP: {}", user.getUsername(), ip);

            // 記錄使用者上線狀態
            Long storeId = user.getStores() != null && !user.getStores().isEmpty()
                    ? user.getStores().iterator().next().getId() : null;
            onlineUserService.userOnline(user.getId(), user.getUsername(), user.getName(), storeId, ip);

            // 產生 Token
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            String accessToken = tokenProvider.generateToken(userPrincipal);
            String refreshToken = tokenProvider.generateRefreshToken(userPrincipal);

            // 檢查密碼過期狀態
            boolean passwordExpired = passwordPolicyService.isPasswordExpired(user);
            boolean passwordExpiringSoon = passwordPolicyService.isPasswordExpiringSoon(user);
            long remainingDays = passwordPolicyService.getPasswordRemainingDays(user);

            // 建立回應
            return LoginResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(tokenProvider.getExpiration() / 1000)
                    .user(LoginResponse.UserInfo.builder()
                            .id(user.getId())
                            .username(user.getUsername())
                            .name(user.getName())
                            .email(user.getEmail())
                            .role(user.getRole().getCode())
                            .roleName(user.getRole().getName())
                            .build())
                    .passwordExpired(passwordExpired)
                    .passwordRemainingDays(remainingDays)
                    .passwordChangeRequired(passwordExpired || passwordExpiringSoon)
                    .build();

        } catch (BadCredentialsException e) {
            // 記錄登入失敗
            handleLoginFailure(user);
            throw new BadCredentialsException("帳號或密碼錯誤");
        }
    }

    /**
     * 處理登入失敗
     *
     * @param user 使用者
     */
    private void handleLoginFailure(User user) {
        user.incrementLoginFailCount();

        // 檢查是否需要鎖定帳號
        if (user.getLoginFailCount() >= maxLoginAttempts) {
            user.lock(lockDurationMinutes);
            log.warn("使用者 {} 登入失敗次數過多，帳號已被鎖定 {} 分鐘",
                    user.getUsername(), lockDurationMinutes);
        }

        userRepository.save(user);
    }

    /**
     * 刷新 Token
     *
     * @param refreshToken Refresh Token
     * @return 新的登入回應
     */
    @Transactional(readOnly = true)
    public LoginResponse refreshToken(String refreshToken) {
        // 驗證 Refresh Token
        if (!tokenProvider.validateToken(refreshToken)) {
            throw new BusinessException(401, "Refresh Token 無效或已過期");
        }

        // 取得使用者 ID
        Long userId = tokenProvider.getUserIdFromToken(refreshToken);

        // 查詢使用者
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(401, "使用者不存在"));

        // 檢查帳號狀態
        if (!user.isActive()) {
            throw new BusinessException(403, "帳號已停用");
        }

        // 產生新的 Token
        UserPrincipal userPrincipal = UserPrincipal.create(
                user,
                Collections.singletonList(user.getRole().getCode())
        );
        String newAccessToken = tokenProvider.generateToken(userPrincipal);
        String newRefreshToken = tokenProvider.generateRefreshToken(userPrincipal);

        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(tokenProvider.getExpiration() / 1000)
                .user(LoginResponse.UserInfo.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .name(user.getName())
                        .email(user.getEmail())
                        .role(user.getRole().getCode())
                        .roleName(user.getRole().getName())
                        .build())
                .build();
    }

    /**
     * 修改密碼
     *
     * @param userId      使用者 ID
     * @param oldPassword 舊密碼
     * @param newPassword 新密碼
     */
    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> BusinessException.notFound("使用者", userId));

        // 驗證舊密碼
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw BusinessException.validationFailed("舊密碼不正確");
        }

        // 檢查新舊密碼是否相同
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw BusinessException.validationFailed("新密碼不能與舊密碼相同");
        }

        // 更新密碼
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordChangedAt(java.time.LocalDateTime.now());
        userRepository.save(user);

        log.info("使用者 {} 已修改密碼", user.getUsername());
    }
}
