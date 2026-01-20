package com.kamesan.erpapi.accounts.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登入回應 DTO
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "登入回應")
public class LoginResponse {

    /**
     * Access Token
     */
    @Schema(description = "Access Token")
    private String accessToken;

    /**
     * Refresh Token
     */
    @Schema(description = "Refresh Token")
    private String refreshToken;

    /**
     * Token 類型
     */
    @Schema(description = "Token 類型", example = "Bearer")
    @Builder.Default
    private String tokenType = "Bearer";

    /**
     * 過期時間（秒）
     */
    @Schema(description = "過期時間（秒）", example = "86400")
    private long expiresIn;

    /**
     * 使用者資訊
     */
    @Schema(description = "使用者資訊")
    private UserInfo user;

    /**
     * 密碼是否已過期
     */
    @Schema(description = "密碼是否已過期")
    @Builder.Default
    private boolean passwordExpired = false;

    /**
     * 密碼剩餘有效天數
     */
    @Schema(description = "密碼剩餘有效天數（-1 表示不適用）")
    @Builder.Default
    private long passwordRemainingDays = -1;

    /**
     * 是否需要修改密碼（即將過期警告）
     */
    @Schema(description = "是否需要修改密碼")
    @Builder.Default
    private boolean passwordChangeRequired = false;

    /**
     * 使用者資訊內部類別
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "使用者資訊")
    public static class UserInfo {
        /**
         * 使用者 ID
         */
        @Schema(description = "使用者 ID")
        private Long id;

        /**
         * 使用者名稱
         */
        @Schema(description = "使用者名稱")
        private String username;

        /**
         * 姓名
         */
        @Schema(description = "姓名")
        private String name;

        /**
         * Email
         */
        @Schema(description = "Email")
        private String email;

        /**
         * 角色代碼
         */
        @Schema(description = "角色代碼")
        private String role;

        /**
         * 角色名稱
         */
        @Schema(description = "角色名稱")
        private String roleName;
    }
}
