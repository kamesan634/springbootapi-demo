package com.kamesan.erpapi.system.controller;

import com.kamesan.erpapi.BaseIntegrationTest;
import com.kamesan.erpapi.system.entity.NumberRule;
import com.kamesan.erpapi.system.repository.NumberRuleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 編號規則控制器測試類別
 */
@DisplayName("編號規則控制器測試")
class NumberRuleControllerTest extends BaseIntegrationTest {

    private static final String NUMBER_RULES_API = "/api/v1/number-rules";

    @Autowired
    private NumberRuleRepository numberRuleRepository;

    private NumberRule testRule;

    @BeforeEach
    void setUpNumberRuleTestData() {
        testRule = numberRuleRepository.findByRuleCode("TEST_RULE").orElseGet(() -> {
            NumberRule rule = new NumberRule();
            rule.setRuleCode("TEST_RULE");
            rule.setRuleName("測試編號規則");
            rule.setPrefix("TEST");
            rule.setDateFormat("yyyyMMdd");
            rule.setSequenceLength(4);
            rule.setCurrentSequence(1L);
            rule.setResetPeriod(NumberRule.ResetPeriod.DAILY);
            rule.setIsActive(true);
            return numberRuleRepository.save(rule);
        });
    }

    @Nested
    @DisplayName("GET /api/v1/number-rules - 取得所有規則")
    class GetAllRulesTests {

        @Test
        @DisplayName("應返回所有編號規則")
        void getAll_ShouldReturnAllRules() throws Exception {
            String token = generateAdminToken();

            mockMvc.perform(get(NUMBER_RULES_API)
                            .header("Authorization", bearerToken(token)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray());
        }

        @Test
        @DisplayName("未驗證應返回 403")
        void getAll_WithoutAuth_ShouldReturnForbidden() throws Exception {
            mockMvc.perform(get(NUMBER_RULES_API))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/number-rules/{ruleCode} - 取得規則")
    class GetRuleByCodeTests {

        @Test
        @DisplayName("有效規則代碼應返回規則")
        void getByCode_WithValidCode_ShouldReturnRule() throws Exception {
            String token = generateAdminToken();

            mockMvc.perform(get(NUMBER_RULES_API + "/TEST_RULE")
                            .header("Authorization", bearerToken(token)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.ruleCode").value("TEST_RULE"));
        }

        @Test
        @DisplayName("不存在的規則代碼應返回 404")
        void getByCode_WithInvalidCode_ShouldReturnNotFound() throws Exception {
            String token = generateAdminToken();

            mockMvc.perform(get(NUMBER_RULES_API + "/NOT_EXIST")
                            .header("Authorization", bearerToken(token)))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/number-rules/{ruleCode}/generate - 產生編號")
    class GenerateNumberTests {

        @Test
        @DisplayName("應產生下一個編號")
        void generate_ShouldReturnNextNumber() throws Exception {
            String token = generateAdminToken();

            mockMvc.perform(post(NUMBER_RULES_API + "/TEST_RULE/generate")
                            .header("Authorization", bearerToken(token)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.number").exists());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/number-rules/{ruleCode}/preview - 預覽編號")
    class PreviewNumberTests {

        @Test
        @DisplayName("應預覽編號格式")
        void preview_ShouldReturnPreviewNumber() throws Exception {
            String token = generateAdminToken();

            mockMvc.perform(get(NUMBER_RULES_API + "/TEST_RULE/preview")
                            .header("Authorization", bearerToken(token)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.preview").exists());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/number-rules/{ruleCode}/reset - 重置流水號")
    class ResetSequenceTests {

        @Test
        @DisplayName("應重置流水號成功")
        void reset_ShouldResetSequence() throws Exception {
            String token = generateAdminToken();

            mockMvc.perform(post(NUMBER_RULES_API + "/TEST_RULE/reset")
                            .header("Authorization", bearerToken(token)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }
}
