package com.kamesan.erpapi.accounts.service;

import com.kamesan.erpapi.accounts.entity.User;
import com.kamesan.erpapi.accounts.repository.UserRepository;
import com.kamesan.erpapi.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 密碼政策服務
 *
 * <p>提供密碼驗證和政策檢查：</p>
 * <ul>
 *   <li>密碼強度檢查</li>
 *   <li>密碼過期檢查</li>
 *   <li>密碼歷史檢查（防止重複使用）</li>
 * </ul>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordPolicyService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 密碼過期天數（0 表示不過期）
     */
    @Value("${app.security.password-expiration-days:90}")
    private int passwordExpirationDays;

    /**
     * 密碼即將過期警告天數
     */
    @Value("${app.security.password-warning-days:14}")
    private int passwordWarningDays;

    /**
     * 最小密碼長度
     */
    @Value("${app.security.password-min-length:8}")
    private int minPasswordLength;

    /**
     * 最大密碼長度
     */
    @Value("${app.security.password-max-length:128}")
    private int maxPasswordLength;

    /**
     * 是否需要大寫字母
     */
    @Value("${app.security.password-require-uppercase:true}")
    private boolean requireUppercase;

    /**
     * 是否需要小寫字母
     */
    @Value("${app.security.password-require-lowercase:true}")
    private boolean requireLowercase;

    /**
     * 是否需要數字
     */
    @Value("${app.security.password-require-digit:true}")
    private boolean requireDigit;

    /**
     * 是否需要特殊字元
     */
    @Value("${app.security.password-require-special:false}")
    private boolean requireSpecialChar;

    private static final Pattern UPPERCASE_PATTERN = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile("[a-z]");
    private static final Pattern DIGIT_PATTERN = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL_PATTERN = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]");

    /**
     * 取得密碼過期天數
     */
    public int getPasswordExpirationDays() {
        return passwordExpirationDays;
    }

    /**
     * 取得密碼警告天數
     */
    public int getPasswordWarningDays() {
        return passwordWarningDays;
    }

    /**
     * 檢查密碼是否過期
     *
     * @param user 使用者
     * @return 是否過期
     */
    public boolean isPasswordExpired(User user) {
        return user.isPasswordExpired(passwordExpirationDays);
    }

    /**
     * 檢查密碼是否即將過期
     *
     * @param user 使用者
     * @return 是否即將過期
     */
    public boolean isPasswordExpiringSoon(User user) {
        if (passwordExpirationDays <= 0) {
            return false;
        }
        long remainingDays = user.getPasswordRemainingDays(passwordExpirationDays);
        return remainingDays >= 0 && remainingDays <= passwordWarningDays;
    }

    /**
     * 取得密碼剩餘有效天數
     *
     * @param user 使用者
     * @return 剩餘天數
     */
    public long getPasswordRemainingDays(User user) {
        return user.getPasswordRemainingDays(passwordExpirationDays);
    }

    /**
     * 驗證密碼強度
     *
     * @param password 密碼
     * @return 驗證結果（空列表表示通過）
     */
    public List<String> validatePasswordStrength(String password) {
        List<String> errors = new ArrayList<>();

        if (password == null || password.isEmpty()) {
            errors.add("密碼不能為空");
            return errors;
        }

        if (password.length() < minPasswordLength) {
            errors.add("密碼長度至少需要 " + minPasswordLength + " 個字元");
        }

        if (password.length() > maxPasswordLength) {
            errors.add("密碼長度不能超過 " + maxPasswordLength + " 個字元");
        }

        if (requireUppercase && !UPPERCASE_PATTERN.matcher(password).find()) {
            errors.add("密碼需要包含至少一個大寫字母");
        }

        if (requireLowercase && !LOWERCASE_PATTERN.matcher(password).find()) {
            errors.add("密碼需要包含至少一個小寫字母");
        }

        if (requireDigit && !DIGIT_PATTERN.matcher(password).find()) {
            errors.add("密碼需要包含至少一個數字");
        }

        if (requireSpecialChar && !SPECIAL_PATTERN.matcher(password).find()) {
            errors.add("密碼需要包含至少一個特殊字元");
        }

        return errors;
    }

    /**
     * 驗證密碼強度並拋出異常
     *
     * @param password 密碼
     */
    public void validatePasswordStrengthOrThrow(String password) {
        List<String> errors = validatePasswordStrength(password);
        if (!errors.isEmpty()) {
            throw BusinessException.validationFailed(String.join("；", errors));
        }
    }

    /**
     * 強制修改過期密碼
     *
     * @param userId      使用者 ID
     * @param oldPassword 舊密碼
     * @param newPassword 新密碼
     */
    @Transactional
    public void forceChangeExpiredPassword(Long userId, String oldPassword, String newPassword) {
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

        // 驗證新密碼強度
        validatePasswordStrengthOrThrow(newPassword);

        // 更新密碼
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordChangedAt(LocalDateTime.now());
        userRepository.save(user);

        log.info("使用者 {} 已修改過期密碼", user.getUsername());
    }

    /**
     * 取得密碼政策說明
     *
     * @return 密碼政策說明
     */
    public PasswordPolicyDto getPasswordPolicy() {
        return PasswordPolicyDto.builder()
                .minLength(minPasswordLength)
                .maxLength(maxPasswordLength)
                .requireUppercase(requireUppercase)
                .requireLowercase(requireLowercase)
                .requireDigit(requireDigit)
                .requireSpecialChar(requireSpecialChar)
                .expirationDays(passwordExpirationDays)
                .warningDays(passwordWarningDays)
                .build();
    }

    /**
     * 密碼政策 DTO
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PasswordPolicyDto {
        private int minLength;
        private int maxLength;
        private boolean requireUppercase;
        private boolean requireLowercase;
        private boolean requireDigit;
        private boolean requireSpecialChar;
        private int expirationDays;
        private int warningDays;
    }
}
