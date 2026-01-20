package com.kamesan.erpapi.accounts.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登入請求 DTO
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "登入請求")
public class LoginRequest {

    /**
     * 使用者名稱
     */
    @NotBlank(message = "使用者名稱不能為空")
    @Schema(description = "使用者名稱", example = "admin")
    private String username;

    /**
     * 密碼
     */
    @NotBlank(message = "密碼不能為空")
    @Schema(description = "密碼", example = "password123")
    private String password;
}
