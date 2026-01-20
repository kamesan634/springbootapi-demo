package com.kamesan.erpapi.accounts.controller;

import com.kamesan.erpapi.BaseIntegrationTest;
import com.kamesan.erpapi.accounts.dto.LoginRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 認證控制器測試類別
 *
 * <p>測試認證相關的 API 端點：</p>
 * <ul>
 *   <li>POST /api/v1/auth/login - 使用者登入</li>
 *   <li>POST /api/v1/auth/refresh - 刷新 Token</li>
 *   <li>POST /api/v1/auth/logout - 使用者登出</li>
 * </ul>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@DisplayName("認證控制器測試")
class AuthControllerTest extends BaseIntegrationTest {

    /**
     * API 基礎路徑
     */
    private static final String AUTH_API = "/api/v1/auth";

    /**
     * 登入 API 測試
     */
    @Nested
    @DisplayName("POST /api/v1/auth/login - 使用者登入")
    class LoginTests {

        /**
         * 測試正確帳號密碼登入成功
         */
        @Test
        @DisplayName("正確帳號密碼應登入成功")
        void login_WithValidCredentials_ShouldReturnToken() throws Exception {
            // Arrange - 準備測試資料
            LoginRequest request = createLoginRequest("testadmin", TEST_PASSWORD);

            // Act & Assert - 執行請求並驗證結果
            mockMvc.perform(post(AUTH_API + "/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("登入成功"))
                    .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                    .andExpect(jsonPath("$.data.refreshToken").isNotEmpty())
                    .andExpect(jsonPath("$.data.user.username").value("testadmin"));
        }

        /**
         * 測試錯誤密碼登入失敗
         */
        @Test
        @DisplayName("錯誤密碼應登入失敗")
        void login_WithWrongPassword_ShouldReturnUnauthorized() throws Exception {
            // Arrange
            LoginRequest request = createLoginRequest("testadmin", "wrongpassword");

            // Act & Assert
            mockMvc.perform(post(AUTH_API + "/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andDo(print())
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.success").value(false));
        }

        /**
         * 測試不存在的使用者登入失敗
         */
        @Test
        @DisplayName("不存在的使用者應登入失敗")
        void login_WithNonExistentUser_ShouldReturnUnauthorized() throws Exception {
            // Arrange
            LoginRequest request = createLoginRequest("nonexistent", TEST_PASSWORD);

            // Act & Assert
            mockMvc.perform(post(AUTH_API + "/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andDo(print())
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.success").value(false));
        }

        /**
         * 測試空白使用者名稱
         */
        @Test
        @DisplayName("空白使用者名稱應返回驗證錯誤")
        void login_WithBlankUsername_ShouldReturnBadRequest() throws Exception {
            // Arrange
            LoginRequest request = createLoginRequest("", TEST_PASSWORD);

            // Act & Assert
            mockMvc.perform(post(AUTH_API + "/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        /**
         * 測試空白密碼
         */
        @Test
        @DisplayName("空白密碼應返回驗證錯誤")
        void login_WithBlankPassword_ShouldReturnBadRequest() throws Exception {
            // Arrange
            LoginRequest request = createLoginRequest("testadmin", "");

            // Act & Assert
            mockMvc.perform(post(AUTH_API + "/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        /**
         * 測試一般使用者登入成功
         */
        @Test
        @DisplayName("一般使用者應能正常登入")
        void login_WithNormalUser_ShouldReturnToken() throws Exception {
            // Arrange
            LoginRequest request = createLoginRequest("testuser", TEST_PASSWORD);

            // Act & Assert
            mockMvc.perform(post(AUTH_API + "/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                    .andExpect(jsonPath("$.data.user.username").value("testuser"));
        }
    }

    /**
     * Token 刷新 API 測試
     */
    @Nested
    @DisplayName("POST /api/v1/auth/refresh - 刷新 Token")
    class RefreshTokenTests {

        /**
         * 測試使用有效 Refresh Token 刷新成功
         */
        @Test
        @DisplayName("有效 Refresh Token 應刷新成功")
        void refresh_WithValidRefreshToken_ShouldReturnNewToken() throws Exception {
            // Arrange - 先登入取得 Refresh Token
            LoginRequest loginRequest = createLoginRequest("testadmin", TEST_PASSWORD);

            String loginResponse = mockMvc.perform(post(AUTH_API + "/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(loginRequest)))
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            // 從回應中取得 refreshToken
            String refreshToken = objectMapper.readTree(loginResponse)
                    .path("data")
                    .path("refreshToken")
                    .asText();

            // Act & Assert
            mockMvc.perform(post(AUTH_API + "/refresh")
                            .param("refreshToken", refreshToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.accessToken").isNotEmpty());
        }

        /**
         * 測試使用無效 Refresh Token
         */
        @Test
        @DisplayName("無效 Refresh Token 應返回錯誤")
        void refresh_WithInvalidRefreshToken_ShouldReturnUnauthorized() throws Exception {
            // Act & Assert
            mockMvc.perform(post(AUTH_API + "/refresh")
                            .param("refreshToken", "invalid-token"))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }
    }

    /**
     * 登出 API 測試
     */
    @Nested
    @DisplayName("POST /api/v1/auth/logout - 使用者登出")
    class LogoutTests {

        /**
         * 測試登出成功
         */
        @Test
        @DisplayName("登出應返回成功")
        void logout_ShouldReturnSuccess() throws Exception {
            // Arrange
            String token = generateAdminToken();

            // Act & Assert
            mockMvc.perform(post(AUTH_API + "/logout")
                            .header("Authorization", bearerToken(token)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("登出成功"));
        }
    }
}
