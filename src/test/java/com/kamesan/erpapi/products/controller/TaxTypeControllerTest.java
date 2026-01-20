package com.kamesan.erpapi.products.controller;

import com.kamesan.erpapi.BaseIntegrationTest;
import com.kamesan.erpapi.products.entity.TaxType;
import com.kamesan.erpapi.products.repository.TaxTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 稅別控制器測試類別
 */
@DisplayName("稅別控制器測試")
class TaxTypeControllerTest extends BaseIntegrationTest {

    private static final String TAX_TYPES_API = "/api/v1/tax-types";

    @Autowired
    private TaxTypeRepository taxTypeRepository;

    private TaxType testTaxType;

    @BeforeEach
    void setUpTaxTypeTestData() {
        testTaxType = taxTypeRepository.findByCode("TEST-TAX").orElseGet(() -> {
            TaxType taxType = new TaxType();
            taxType.setCode("TEST-TAX");
            taxType.setName("測試稅別");
            taxType.setRate(new BigDecimal("5.00"));
            taxType.setDefault(false);
            taxType.setActive(true);
            return taxTypeRepository.save(taxType);
        });
    }

    @Nested
    @DisplayName("POST /api/v1/tax-types - 建立稅別")
    class CreateTaxTypeTests {

        @Test
        @DisplayName("有效資料應建立稅別成功")
        void create_WithValidData_ShouldCreateTaxType() throws Exception {
            String token = generateAdminToken();
            TaxTypeController.CreateTaxTypeRequest request = TaxTypeController.CreateTaxTypeRequest.builder()
                    .code("NEW-TAX-001")
                    .name("新稅別")
                    .rate(new BigDecimal("10.00"))
                    .isDefault(false)
                    .active(true)
                    .build();

            mockMvc.perform(post(TAX_TYPES_API)
                            .header("Authorization", bearerToken(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.code").value("NEW-TAX-001"))
                    .andExpect(jsonPath("$.data.name").value("新稅別"));
        }

        @Test
        @DisplayName("未驗證應返回 403")
        void create_WithoutAuth_ShouldReturnForbidden() throws Exception {
            TaxTypeController.CreateTaxTypeRequest request = TaxTypeController.CreateTaxTypeRequest.builder()
                    .code("NEW-TAX-002")
                    .name("新稅別2")
                    .rate(new BigDecimal("5.00"))
                    .build();

            mockMvc.perform(post(TAX_TYPES_API)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("缺少必填欄位應返回驗證錯誤")
        void create_WithMissingRequiredFields_ShouldReturnBadRequest() throws Exception {
            String token = generateAdminToken();
            TaxTypeController.CreateTaxTypeRequest request = TaxTypeController.CreateTaxTypeRequest.builder()
                    .code("NEW-TAX-003")
                    // name 缺少
                    .build();

            mockMvc.perform(post(TAX_TYPES_API)
                            .header("Authorization", bearerToken(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("重複代碼應返回錯誤")
        void create_WithDuplicateCode_ShouldReturnError() throws Exception {
            String token = generateAdminToken();
            TaxTypeController.CreateTaxTypeRequest request = TaxTypeController.CreateTaxTypeRequest.builder()
                    .code("TEST-TAX")
                    .name("重複稅別")
                    .rate(new BigDecimal("5.00"))
                    .build();

            mockMvc.perform(post(TAX_TYPES_API)
                            .header("Authorization", bearerToken(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andDo(print())
                    .andExpect(status().isConflict());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/tax-types - 查詢稅別列表")
    class GetTaxTypesTests {

        @Test
        @DisplayName("應返回稅別列表")
        void getAll_ShouldReturnTaxTypeList() throws Exception {
            String token = generateAdminToken();

            mockMvc.perform(get(TAX_TYPES_API)
                            .header("Authorization", bearerToken(token)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isArray());
        }

        @Test
        @DisplayName("應支援分頁查詢")
        void getAll_WithPagination_ShouldReturnPagedResults() throws Exception {
            String token = generateAdminToken();

            mockMvc.perform(get(TAX_TYPES_API)
                            .header("Authorization", bearerToken(token))
                            .param("page", "1")
                            .param("size", "10"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.size").value(10))
                    .andExpect(jsonPath("$.data.page").value(1));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/tax-types/{id} - 根據 ID 查詢稅別")
    class GetTaxTypeByIdTests {

        @Test
        @DisplayName("有效 ID 應返回稅別詳情")
        void getById_WithValidId_ShouldReturnTaxType() throws Exception {
            String token = generateAdminToken();

            mockMvc.perform(get(TAX_TYPES_API + "/" + testTaxType.getId())
                            .header("Authorization", bearerToken(token)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(testTaxType.getId()))
                    .andExpect(jsonPath("$.data.code").value("TEST-TAX"));
        }

        @Test
        @DisplayName("不存在的 ID 應返回 404")
        void getById_WithInvalidId_ShouldReturnNotFound() throws Exception {
            String token = generateAdminToken();

            mockMvc.perform(get(TAX_TYPES_API + "/99999")
                            .header("Authorization", bearerToken(token)))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/tax-types/code/{code} - 根據代碼查詢稅別")
    class GetTaxTypeByCodeTests {

        @Test
        @DisplayName("有效代碼應返回稅別")
        void getByCode_WithValidCode_ShouldReturnTaxType() throws Exception {
            String token = generateAdminToken();

            mockMvc.perform(get(TAX_TYPES_API + "/code/TEST-TAX")
                            .header("Authorization", bearerToken(token)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.code").value("TEST-TAX"));
        }

        @Test
        @DisplayName("不存在的代碼應返回 404")
        void getByCode_WithInvalidCode_ShouldReturnNotFound() throws Exception {
            String token = generateAdminToken();

            mockMvc.perform(get(TAX_TYPES_API + "/code/NOT-EXIST")
                            .header("Authorization", bearerToken(token)))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/tax-types/{id} - 更新稅別")
    class UpdateTaxTypeTests {

        @Test
        @DisplayName("有效資料應更新稅別成功")
        void update_WithValidData_ShouldUpdateTaxType() throws Exception {
            String token = generateAdminToken();
            TaxTypeController.UpdateTaxTypeRequest request = TaxTypeController.UpdateTaxTypeRequest.builder()
                    .code("TEST-TAX")
                    .name("更新後的稅別名稱")
                    .rate(new BigDecimal("8.00"))
                    .active(true)
                    .build();

            mockMvc.perform(put(TAX_TYPES_API + "/" + testTaxType.getId())
                            .header("Authorization", bearerToken(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.name").value("更新後的稅別名稱"));
        }

        @Test
        @DisplayName("更新不存在的稅別應返回 404")
        void update_WithInvalidId_ShouldReturnNotFound() throws Exception {
            String token = generateAdminToken();
            TaxTypeController.UpdateTaxTypeRequest request = TaxTypeController.UpdateTaxTypeRequest.builder()
                    .code("NOT-EXIST")
                    .name("更新稅別")
                    .rate(new BigDecimal("5.00"))
                    .build();

            mockMvc.perform(put(TAX_TYPES_API + "/99999")
                            .header("Authorization", bearerToken(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/tax-types/{id} - 刪除稅別")
    class DeleteTaxTypeTests {

        @Test
        @DisplayName("刪除不存在的稅別應返回 404")
        void delete_WithInvalidId_ShouldReturnNotFound() throws Exception {
            String token = generateAdminToken();

            mockMvc.perform(delete(TAX_TYPES_API + "/99999")
                            .header("Authorization", bearerToken(token)))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/tax-types/active - 查詢啟用的稅別")
    class GetActiveTaxTypesTests {

        @Test
        @DisplayName("應返回啟用的稅別列表")
        void getActive_ShouldReturnActiveTaxTypes() throws Exception {
            String token = generateAdminToken();

            mockMvc.perform(get(TAX_TYPES_API + "/active")
                            .header("Authorization", bearerToken(token)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray());
        }
    }
}
