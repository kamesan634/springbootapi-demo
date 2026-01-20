package com.kamesan.erpapi.system.controller;

import com.kamesan.erpapi.BaseIntegrationTest;
import com.kamesan.erpapi.system.dto.CreateSystemParameterRequest;
import com.kamesan.erpapi.system.entity.SystemParameter;
import com.kamesan.erpapi.system.repository.SystemParameterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 系統參數控制器測試類別
 */
@DisplayName("系統參數控制器測試")
class SystemParameterControllerTest extends BaseIntegrationTest {

    private static final String SYSTEM_PARAMS_API = "/api/v1/system-parameters";

    @Autowired
    private SystemParameterRepository systemParameterRepository;

    private SystemParameter testParameter;

    @BeforeEach
    void setUpSystemParameterTestData() {
        testParameter = systemParameterRepository.findByCategoryAndParamKey("TEST", "TEST_KEY").orElseGet(() -> {
            SystemParameter param = new SystemParameter();
            param.setCategory("TEST");
            param.setParamKey("TEST_KEY");
            param.setParamValue("test_value");
            param.setDescription("測試參數");
            param.setIsSystem(false);
            return systemParameterRepository.save(param);
        });
    }

    @Nested
    @DisplayName("GET /api/v1/system-parameters - 取得所有參數")
    class GetAllParametersTests {

        @Test
        @DisplayName("應返回所有參數")
        void getAll_ShouldReturnAllParameters() throws Exception {
            String token = generateAdminToken();

            mockMvc.perform(get(SYSTEM_PARAMS_API)
                            .header("Authorization", bearerToken(token)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray());
        }

        @Test
        @DisplayName("未驗證應返回 403")
        void getAll_WithoutAuth_ShouldReturnForbidden() throws Exception {
            mockMvc.perform(get(SYSTEM_PARAMS_API))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/system-parameters/category/{category} - 根據類別取得參數")
    class GetParametersByCategoryTests {

        @Test
        @DisplayName("應返回指定類別的參數")
        void getByCategory_ShouldReturnCategoryParameters() throws Exception {
            String token = generateAdminToken();

            mockMvc.perform(get(SYSTEM_PARAMS_API + "/category/TEST")
                            .header("Authorization", bearerToken(token)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/system-parameters/{category}/{paramKey} - 取得參數值")
    class GetParameterValueTests {

        @Test
        @DisplayName("有效鍵應返回參數值")
        void getValue_WithValidKey_ShouldReturnValue() throws Exception {
            String token = generateAdminToken();

            mockMvc.perform(get(SYSTEM_PARAMS_API + "/TEST/TEST_KEY")
                            .header("Authorization", bearerToken(token)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").value("test_value"));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/system-parameters - 新增參數")
    class CreateParameterTests {

        @Test
        @DisplayName("有效資料應新增參數成功")
        void create_WithValidData_ShouldCreateParameter() throws Exception {
            String token = generateAdminToken();
            CreateSystemParameterRequest request = CreateSystemParameterRequest.builder()
                    .category("TEST")
                    .paramKey("NEW_KEY")
                    .paramValue("new_value")
                    .description("新參數")
                    .build();

            mockMvc.perform(post(SYSTEM_PARAMS_API)
                            .header("Authorization", bearerToken(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.paramKey").value("NEW_KEY"));
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/system-parameters/{category}/{paramKey} - 更新參數值")
    class UpdateParameterValueTests {

        @Test
        @DisplayName("有效資料應更新參數成功")
        void update_WithValidData_ShouldUpdateParameter() throws Exception {
            String token = generateAdminToken();
            Map<String, String> body = new HashMap<>();
            body.put("value", "updated_value");

            mockMvc.perform(put(SYSTEM_PARAMS_API + "/TEST/TEST_KEY")
                            .header("Authorization", bearerToken(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(body)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/system-parameters/{id} - 刪除參數")
    class DeleteParameterTests {

        @Test
        @DisplayName("有效 ID 應刪除參數成功")
        void delete_WithValidId_ShouldDeleteParameter() throws Exception {
            String token = generateAdminToken();

            mockMvc.perform(delete(SYSTEM_PARAMS_API + "/" + testParameter.getId())
                            .header("Authorization", bearerToken(token)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }
}
