package com.kamesan.erpapi.customers.controller;

import com.kamesan.erpapi.BaseIntegrationTest;
import com.kamesan.erpapi.customers.entity.CustomerLevel;
import com.kamesan.erpapi.customers.repository.CustomerLevelRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 會員等級控制器測試類別
 */
@DisplayName("會員等級控制器測試")
class CustomerLevelControllerTest extends BaseIntegrationTest {

    private static final String CUSTOMER_LEVELS_API = "/api/v1/customer-levels";

    @Autowired
    private CustomerLevelRepository customerLevelRepository;

    private CustomerLevel testLevel;

    @BeforeEach
    void setUpCustomerLevelTestData() {
        testLevel = customerLevelRepository.findByCode("TEST-LEVEL").orElseGet(() -> {
            CustomerLevel level = new CustomerLevel();
            level.setCode("TEST-LEVEL");
            level.setName("測試等級");
            level.setDescription("測試用會員等級");
            level.setUpgradeCondition(new BigDecimal("0"));
            level.setDiscountRate(new BigDecimal("0.95"));
            level.setPointsMultiplier(new BigDecimal("1.0"));
            level.setSortOrder(0);
            level.setActive(true);
            return customerLevelRepository.save(level);
        });
    }

    @Nested
    @DisplayName("GET /api/v1/customer-levels - 取得所有會員等級")
    class GetAllLevelsTests {

        @Test
        @DisplayName("應返回所有會員等級")
        void getAll_ShouldReturnAllLevels() throws Exception {
            String token = generateAdminToken();

            mockMvc.perform(get(CUSTOMER_LEVELS_API)
                            .header("Authorization", bearerToken(token)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray());
        }

        @Test
        @DisplayName("未驗證應返回 403")
        void getAll_WithoutAuth_ShouldReturnForbidden() throws Exception {
            mockMvc.perform(get(CUSTOMER_LEVELS_API))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/customer-levels/active - 取得啟用的會員等級")
    class GetActiveLevelsTests {

        @Test
        @DisplayName("應返回啟用的會員等級")
        void getActive_ShouldReturnActiveLevels() throws Exception {
            String token = generateAdminToken();

            mockMvc.perform(get(CUSTOMER_LEVELS_API + "/active")
                            .header("Authorization", bearerToken(token)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/customer-levels/{id} - 根據 ID 取得會員等級")
    class GetLevelByIdTests {

        @Test
        @DisplayName("有效 ID 應返回會員等級詳情")
        void getById_WithValidId_ShouldReturnLevel() throws Exception {
            String token = generateAdminToken();

            mockMvc.perform(get(CUSTOMER_LEVELS_API + "/" + testLevel.getId())
                            .header("Authorization", bearerToken(token)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(testLevel.getId()))
                    .andExpect(jsonPath("$.data.code").value("TEST-LEVEL"));
        }

        @Test
        @DisplayName("不存在的 ID 應返回 404")
        void getById_WithInvalidId_ShouldReturnNotFound() throws Exception {
            String token = generateAdminToken();

            mockMvc.perform(get(CUSTOMER_LEVELS_API + "/99999")
                            .header("Authorization", bearerToken(token)))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }
}
