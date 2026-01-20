package com.kamesan.erpapi.products.controller;

import com.kamesan.erpapi.BaseIntegrationTest;
import com.kamesan.erpapi.products.entity.Unit;
import com.kamesan.erpapi.products.repository.UnitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 單位控制器測試類別
 */
@DisplayName("單位控制器測試")
class UnitControllerTest extends BaseIntegrationTest {

    private static final String UNITS_API = "/api/v1/units";

    @Autowired
    private UnitRepository unitRepository;

    private Unit testUnit;

    @BeforeEach
    void setUpUnitTestData() {
        testUnit = unitRepository.findByCode("TEST-UNIT").orElseGet(() -> {
            Unit unit = new Unit();
            unit.setCode("TEST-UNIT");
            unit.setName("測試單位");
            unit.setActive(true);
            return unitRepository.save(unit);
        });
    }

    @Nested
    @DisplayName("POST /api/v1/units - 建立單位")
    class CreateUnitTests {

        @Test
        @DisplayName("有效資料應建立單位成功")
        void create_WithValidData_ShouldCreateUnit() throws Exception {
            String token = generateAdminToken();
            UnitController.CreateUnitRequest request = UnitController.CreateUnitRequest.builder()
                    .code("NEW-UNIT-001")
                    .name("新單位")
                    .active(true)
                    .build();

            mockMvc.perform(post(UNITS_API)
                            .header("Authorization", bearerToken(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.code").value("NEW-UNIT-001"))
                    .andExpect(jsonPath("$.data.name").value("新單位"));
        }

        @Test
        @DisplayName("未驗證應返回 403")
        void create_WithoutAuth_ShouldReturnForbidden() throws Exception {
            UnitController.CreateUnitRequest request = UnitController.CreateUnitRequest.builder()
                    .code("NEW-UNIT-002")
                    .name("新單位2")
                    .build();

            mockMvc.perform(post(UNITS_API)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("缺少必填欄位應返回驗證錯誤")
        void create_WithMissingRequiredFields_ShouldReturnBadRequest() throws Exception {
            String token = generateAdminToken();
            UnitController.CreateUnitRequest request = UnitController.CreateUnitRequest.builder()
                    .code("NEW-UNIT-003")
                    // name 缺少
                    .build();

            mockMvc.perform(post(UNITS_API)
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
            UnitController.CreateUnitRequest request = UnitController.CreateUnitRequest.builder()
                    .code("TEST-UNIT")
                    .name("重複單位")
                    .build();

            mockMvc.perform(post(UNITS_API)
                            .header("Authorization", bearerToken(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andDo(print())
                    .andExpect(status().isConflict());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/units - 查詢單位列表")
    class GetUnitsTests {

        @Test
        @DisplayName("應返回單位列表")
        void getAll_ShouldReturnUnitList() throws Exception {
            String token = generateAdminToken();

            mockMvc.perform(get(UNITS_API)
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

            mockMvc.perform(get(UNITS_API)
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
    @DisplayName("GET /api/v1/units/{id} - 根據 ID 查詢單位")
    class GetUnitByIdTests {

        @Test
        @DisplayName("有效 ID 應返回單位詳情")
        void getById_WithValidId_ShouldReturnUnit() throws Exception {
            String token = generateAdminToken();

            mockMvc.perform(get(UNITS_API + "/" + testUnit.getId())
                            .header("Authorization", bearerToken(token)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(testUnit.getId()))
                    .andExpect(jsonPath("$.data.code").value("TEST-UNIT"));
        }

        @Test
        @DisplayName("不存在的 ID 應返回 404")
        void getById_WithInvalidId_ShouldReturnNotFound() throws Exception {
            String token = generateAdminToken();

            mockMvc.perform(get(UNITS_API + "/99999")
                            .header("Authorization", bearerToken(token)))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/units/code/{code} - 根據代碼查詢單位")
    class GetUnitByCodeTests {

        @Test
        @DisplayName("有效代碼應返回單位")
        void getByCode_WithValidCode_ShouldReturnUnit() throws Exception {
            String token = generateAdminToken();

            mockMvc.perform(get(UNITS_API + "/code/TEST-UNIT")
                            .header("Authorization", bearerToken(token)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.code").value("TEST-UNIT"));
        }

        @Test
        @DisplayName("不存在的代碼應返回 404")
        void getByCode_WithInvalidCode_ShouldReturnNotFound() throws Exception {
            String token = generateAdminToken();

            mockMvc.perform(get(UNITS_API + "/code/NOT-EXIST")
                            .header("Authorization", bearerToken(token)))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/units/{id} - 更新單位")
    class UpdateUnitTests {

        @Test
        @DisplayName("有效資料應更新單位成功")
        void update_WithValidData_ShouldUpdateUnit() throws Exception {
            String token = generateAdminToken();
            UnitController.UpdateUnitRequest request = UnitController.UpdateUnitRequest.builder()
                    .code("TEST-UNIT")
                    .name("更新後的單位名稱")
                    .active(true)
                    .build();

            mockMvc.perform(put(UNITS_API + "/" + testUnit.getId())
                            .header("Authorization", bearerToken(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.name").value("更新後的單位名稱"));
        }

        @Test
        @DisplayName("更新不存在的單位應返回 404")
        void update_WithInvalidId_ShouldReturnNotFound() throws Exception {
            String token = generateAdminToken();
            UnitController.UpdateUnitRequest request = UnitController.UpdateUnitRequest.builder()
                    .code("NOT-EXIST")
                    .name("更新單位")
                    .build();

            mockMvc.perform(put(UNITS_API + "/99999")
                            .header("Authorization", bearerToken(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/units/{id} - 刪除單位")
    class DeleteUnitTests {

        @Test
        @DisplayName("刪除不存在的單位應返回 404")
        void delete_WithInvalidId_ShouldReturnNotFound() throws Exception {
            String token = generateAdminToken();

            mockMvc.perform(delete(UNITS_API + "/99999")
                            .header("Authorization", bearerToken(token)))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/units/active - 查詢啟用的單位")
    class GetActiveUnitsTests {

        @Test
        @DisplayName("應返回啟用的單位列表")
        void getActive_ShouldReturnActiveUnits() throws Exception {
            String token = generateAdminToken();

            mockMvc.perform(get(UNITS_API + "/active")
                            .header("Authorization", bearerToken(token)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/units/search - 搜尋單位")
    class SearchUnitsTests {

        @Test
        @DisplayName("關鍵字搜尋應返回相符單位")
        void search_WithKeyword_ShouldReturnMatchingUnits() throws Exception {
            String token = generateAdminToken();

            mockMvc.perform(get(UNITS_API + "/search")
                            .header("Authorization", bearerToken(token))
                            .param("keyword", "測試"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }
}
