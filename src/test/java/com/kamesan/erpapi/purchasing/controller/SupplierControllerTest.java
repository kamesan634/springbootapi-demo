package com.kamesan.erpapi.purchasing.controller;

import com.kamesan.erpapi.BaseIntegrationTest;
import com.kamesan.erpapi.purchasing.dto.CreateSupplierRequest;
import com.kamesan.erpapi.purchasing.dto.UpdateSupplierRequest;
import com.kamesan.erpapi.purchasing.entity.Supplier;
import com.kamesan.erpapi.purchasing.repository.SupplierRepository;
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
 * 供應商控制器測試類別
 *
 * <p>測試供應商相關的 API 端點 CRUD 操作：</p>
 * <ul>
 *   <li>POST /api/v1/suppliers - 建立供應商</li>
 *   <li>GET /api/v1/suppliers - 查詢供應商列表</li>
 *   <li>GET /api/v1/suppliers/{id} - 查詢供應商詳情</li>
 *   <li>PUT /api/v1/suppliers/{id} - 更新供應商</li>
 *   <li>DELETE /api/v1/suppliers/{id} - 刪除供應商</li>
 * </ul>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@DisplayName("供應商控制器測試")
class SupplierControllerTest extends BaseIntegrationTest {

    /**
     * API 基礎路徑
     */
    private static final String SUPPLIERS_API = "/api/v1/suppliers";

    @Autowired
    private SupplierRepository supplierRepository;

    /**
     * 測試用供應商
     */
    private Supplier testSupplier;

    /**
     * 在每個測試前初始化測試資料
     */
    @BeforeEach
    void setUpSupplierTestData() {
        // 初始化測試供應商
        testSupplier = supplierRepository.findByCode("SUP-TEST-001").orElseGet(() -> {
            Supplier supplier = new Supplier();
            supplier.setCode("SUP-TEST-001");
            supplier.setName("測試供應商");
            supplier.setContactPerson("測試聯絡人");
            supplier.setPhone("02-1234-5678");
            supplier.setEmail("supplier@test.com");
            supplier.setAddress("台北市測試區測試路1號");
            supplier.setPaymentTerms("NET30");
            supplier.setIsActive(true);
            return supplierRepository.save(supplier);
        });
    }

    /**
     * 建立供應商 API 測試
     */
    @Nested
    @DisplayName("POST /api/v1/suppliers - 建立供應商")
    class CreateSupplierTests {

        /**
         * 測試建立供應商成功
         */
        @Test
        @DisplayName("有效資料應建立供應商成功")
        void create_WithValidData_ShouldCreateSupplier() throws Exception {
            // Arrange
            String token = generateAdminToken();
            CreateSupplierRequest request = CreateSupplierRequest.builder()
                    .code("SUP-NEW-001")
                    .name("新供應商")
                    .contactPerson("新聯絡人")
                    .phone("02-9876-5432")
                    .email("newsupplier@test.com")
                    .address("新北市新莊區新莊路1號")
                    .paymentTerms("NET45")
                    .build();

            // Act & Assert
            mockMvc.perform(post(SUPPLIERS_API)
                            .header("Authorization", bearerToken(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.code").value("SUP-NEW-001"))
                    .andExpect(jsonPath("$.data.name").value("新供應商"));
        }

        /**
         * 測試缺少必填欄位
         */
        @Test
        @DisplayName("缺少必填欄位應返回驗證錯誤")
        void create_WithMissingRequiredFields_ShouldReturnBadRequest() throws Exception {
            // Arrange
            String token = generateAdminToken();
            CreateSupplierRequest request = CreateSupplierRequest.builder()
                    .code("SUP-NEW-002")
                    // name 缺少
                    .build();

            // Act & Assert
            mockMvc.perform(post(SUPPLIERS_API)
                            .header("Authorization", bearerToken(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        /**
         * 測試重複代碼
         */
        @Test
        @DisplayName("重複代碼應返回錯誤")
        void create_WithDuplicateCode_ShouldReturnError() throws Exception {
            // Arrange
            String token = generateAdminToken();
            CreateSupplierRequest request = CreateSupplierRequest.builder()
                    .code("SUP-TEST-001") // 已存在的代碼
                    .name("重複供應商")
                    .build();

            // Act & Assert
            mockMvc.perform(post(SUPPLIERS_API)
                            .header("Authorization", bearerToken(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andDo(print())
                    .andExpect(status().isConflict());
        }
    }

    /**
     * 查詢供應商列表 API 測試
     */
    @Nested
    @DisplayName("GET /api/v1/suppliers - 查詢供應商列表")
    class GetSuppliersTests {

        /**
         * 測試查詢供應商列表成功
         */
        @Test
        @DisplayName("應返回供應商列表")
        void getAll_ShouldReturnSupplierList() throws Exception {
            // Arrange
            String token = generateAdminToken();

            // Act & Assert
            mockMvc.perform(get(SUPPLIERS_API)
                            .header("Authorization", bearerToken(token)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isArray());
        }

        /**
         * 測試分頁查詢
         */
        @Test
        @DisplayName("應支援分頁查詢")
        void getAll_WithPagination_ShouldReturnPagedResults() throws Exception {
            // Arrange
            String token = generateAdminToken();

            // Act & Assert
            mockMvc.perform(get(SUPPLIERS_API)
                            .header("Authorization", bearerToken(token))
                            .param("page", "0")
                            .param("size", "10"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.size").value(10))
                    .andExpect(jsonPath("$.data.number").value(0));
        }
    }

    /**
     * 查詢供應商詳情 API 測試
     */
    @Nested
    @DisplayName("GET /api/v1/suppliers/{id} - 查詢供應商詳情")
    class GetSupplierByIdTests {

        /**
         * 測試查詢供應商詳情成功
         */
        @Test
        @DisplayName("有效 ID 應返回供應商詳情")
        void getById_WithValidId_ShouldReturnSupplier() throws Exception {
            // Arrange
            String token = generateAdminToken();

            // Act & Assert
            mockMvc.perform(get(SUPPLIERS_API + "/" + testSupplier.getId())
                            .header("Authorization", bearerToken(token)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(testSupplier.getId()))
                    .andExpect(jsonPath("$.data.code").value("SUP-TEST-001"));
        }

        /**
         * 測試查詢不存在的供應商
         */
        @Test
        @DisplayName("不存在的 ID 應返回 404")
        void getById_WithInvalidId_ShouldReturnNotFound() throws Exception {
            // Arrange
            String token = generateAdminToken();

            // Act & Assert
            mockMvc.perform(get(SUPPLIERS_API + "/99999")
                            .header("Authorization", bearerToken(token)))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }

    /**
     * 更新供應商 API 測試
     */
    @Nested
    @DisplayName("PUT /api/v1/suppliers/{id} - 更新供應商")
    class UpdateSupplierTests {

        /**
         * 測試更新供應商成功
         */
        @Test
        @DisplayName("有效資料應更新供應商成功")
        void update_WithValidData_ShouldUpdateSupplier() throws Exception {
            // Arrange
            String token = generateAdminToken();
            UpdateSupplierRequest request = UpdateSupplierRequest.builder()
                    .code("SUP-TEST-001")
                    .name("更新後的供應商名稱")
                    .contactPerson("新聯絡人")
                    .phone("02-5555-6666")
                    .build();

            // Act & Assert
            mockMvc.perform(put(SUPPLIERS_API + "/" + testSupplier.getId())
                            .header("Authorization", bearerToken(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.name").value("更新後的供應商名稱"));
        }

        /**
         * 測試更新不存在的供應商
         */
        @Test
        @DisplayName("更新不存在的供應商應返回 404")
        void update_WithInvalidId_ShouldReturnNotFound() throws Exception {
            // Arrange
            String token = generateAdminToken();
            UpdateSupplierRequest request = UpdateSupplierRequest.builder()
                    .code("SUP-NOT-EXIST")
                    .name("更新供應商")
                    .build();

            // Act & Assert
            mockMvc.perform(put(SUPPLIERS_API + "/99999")
                            .header("Authorization", bearerToken(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }

    /**
     * 刪除供應商 API 測試
     */
    @Nested
    @DisplayName("DELETE /api/v1/suppliers/{id} - 刪除供應商")
    class DeleteSupplierTests {

        /**
         * 測試刪除供應商成功
         */
        @Test
        @DisplayName("有效 ID 應刪除供應商成功")
        void delete_WithValidId_ShouldDeleteSupplier() throws Exception {
            // Arrange
            String token = generateAdminToken();

            // Act & Assert
            mockMvc.perform(delete(SUPPLIERS_API + "/" + testSupplier.getId())
                            .header("Authorization", bearerToken(token)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        /**
         * 測試刪除不存在的供應商
         */
        @Test
        @DisplayName("刪除不存在的供應商應返回 404")
        void delete_WithInvalidId_ShouldReturnNotFound() throws Exception {
            // Arrange
            String token = generateAdminToken();

            // Act & Assert
            mockMvc.perform(delete(SUPPLIERS_API + "/99999")
                            .header("Authorization", bearerToken(token)))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }
}
