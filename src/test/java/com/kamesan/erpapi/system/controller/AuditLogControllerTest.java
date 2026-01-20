package com.kamesan.erpapi.system.controller;

import com.kamesan.erpapi.BaseIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 稽核日誌控制器測試類別
 */
@DisplayName("稽核日誌控制器測試")
class AuditLogControllerTest extends BaseIntegrationTest {

    private static final String AUDIT_LOGS_API = "/api/v1/audit-logs";

    @Nested
    @DisplayName("GET /api/v1/audit-logs - 查詢稽核日誌")
    class QueryLogsTests {

        @Test
        @DisplayName("應返回稽核日誌列表")
        void queryLogs_ShouldReturnLogList() throws Exception {
            String token = generateAdminToken();

            mockMvc.perform(get(AUDIT_LOGS_API)
                            .header("Authorization", bearerToken(token)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("應支援按操作類型篩選")
        void queryLogs_WithAction_ShouldReturnFilteredLogs() throws Exception {
            String token = generateAdminToken();

            mockMvc.perform(get(AUDIT_LOGS_API)
                            .header("Authorization", bearerToken(token))
                            .param("action", "CREATE"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("應支援按日期範圍篩選")
        void queryLogs_WithDateRange_ShouldReturnFilteredLogs() throws Exception {
            String token = generateAdminToken();

            LocalDateTime startTime = LocalDateTime.now().minusDays(30);
            LocalDateTime endTime = LocalDateTime.now();

            mockMvc.perform(get(AUDIT_LOGS_API)
                            .header("Authorization", bearerToken(token))
                            .param("startTime", startTime.toString())
                            .param("endTime", endTime.toString()))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("未驗證應返回 403")
        void queryLogs_WithoutAuth_ShouldReturnForbidden() throws Exception {
            mockMvc.perform(get(AUDIT_LOGS_API))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/audit-logs/entity/{entityType}/{entityId} - 查詢實體日誌")
    class GetLogsByEntityTests {

        @Test
        @DisplayName("應返回實體相關日誌")
        void getByEntity_ShouldReturnEntityLogs() throws Exception {
            String token = generateAdminToken();

            mockMvc.perform(get(AUDIT_LOGS_API + "/entity/Product/1")
                            .header("Authorization", bearerToken(token)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/audit-logs/user/{userId} - 查詢使用者日誌")
    class GetLogsByUserTests {

        @Test
        @DisplayName("應返回使用者操作日誌")
        void getByUser_ShouldReturnUserLogs() throws Exception {
            String token = generateAdminToken();

            mockMvc.perform(get(AUDIT_LOGS_API + "/user/" + testAdminUser.getId())
                            .header("Authorization", bearerToken(token)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }
}
