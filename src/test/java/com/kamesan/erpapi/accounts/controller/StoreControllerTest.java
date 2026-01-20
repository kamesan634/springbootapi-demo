package com.kamesan.erpapi.accounts.controller;

import com.kamesan.erpapi.BaseIntegrationTest;
import com.kamesan.erpapi.accounts.entity.Store;
import com.kamesan.erpapi.accounts.repository.StoreRepository;
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
 * 門市/倉庫控制器測試類別
 */
@DisplayName("門市/倉庫控制器測試")
class StoreControllerTest extends BaseIntegrationTest {

    private static final String STORES_API = "/api/v1/stores";

    @Autowired
    private StoreRepository storeRepository;

    private Store testStoreEntity;

    @BeforeEach
    void setUpStoreTestData() {
        testStoreEntity = storeRepository.findByCode("TEST-STORE").orElseGet(() -> {
            Store store = new Store();
            store.setCode("TEST-STORE");
            store.setName("測試門市");
            store.setType(Store.StoreType.STORE);
            store.setAddress("台北市測試區測試路100號");
            store.setPhone("02-1234-5678");
            store.setEmail("test@store.com");
            store.setBusinessHours("09:00-21:00");
            store.setMain(false);
            store.setActive(true);
            store.setSortOrder(0);
            return storeRepository.save(store);
        });
    }

    @Nested
    @DisplayName("POST /api/v1/stores - 建立門市/倉庫")
    class CreateStoreTests {

        @Test
        @DisplayName("有效資料應建立門市成功")
        void create_WithValidData_ShouldCreateStore() throws Exception {
            String token = generateAdminToken();
            StoreController.CreateStoreRequest request = StoreController.CreateStoreRequest.builder()
                    .code("NEW-STORE-001")
                    .name("新門市")
                    .type(Store.StoreType.STORE)
                    .address("台北市新店區")
                    .phone("02-9999-8888")
                    .active(true)
                    .build();

            mockMvc.perform(post(STORES_API)
                            .header("Authorization", bearerToken(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.code").value("NEW-STORE-001"))
                    .andExpect(jsonPath("$.data.name").value("新門市"));
        }

        @Test
        @DisplayName("有效資料應建立倉庫成功")
        void create_Warehouse_WithValidData_ShouldCreateWarehouse() throws Exception {
            String token = generateAdminToken();
            StoreController.CreateStoreRequest request = StoreController.CreateStoreRequest.builder()
                    .code("NEW-WH-001")
                    .name("新倉庫")
                    .type(Store.StoreType.WAREHOUSE)
                    .address("台北市倉庫區")
                    .active(true)
                    .build();

            mockMvc.perform(post(STORES_API)
                            .header("Authorization", bearerToken(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.code").value("NEW-WH-001"))
                    .andExpect(jsonPath("$.data.type").value("WAREHOUSE"));
        }

        @Test
        @DisplayName("未驗證應返回 403")
        void create_WithoutAuth_ShouldReturnForbidden() throws Exception {
            StoreController.CreateStoreRequest request = StoreController.CreateStoreRequest.builder()
                    .code("NEW-STORE-002")
                    .name("新門市2")
                    .build();

            mockMvc.perform(post(STORES_API)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("缺少必填欄位應返回驗證錯誤")
        void create_WithMissingRequiredFields_ShouldReturnBadRequest() throws Exception {
            String token = generateAdminToken();
            StoreController.CreateStoreRequest request = StoreController.CreateStoreRequest.builder()
                    .code("NEW-STORE-003")
                    // name 缺少
                    .build();

            mockMvc.perform(post(STORES_API)
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
            StoreController.CreateStoreRequest request = StoreController.CreateStoreRequest.builder()
                    .code("TEST-STORE")
                    .name("重複門市")
                    .build();

            mockMvc.perform(post(STORES_API)
                            .header("Authorization", bearerToken(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andDo(print())
                    .andExpect(status().isConflict());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/stores - 查詢門市/倉庫列表")
    class GetStoresTests {

        @Test
        @DisplayName("應返回門市列表")
        void getAll_ShouldReturnStoreList() throws Exception {
            String token = generateAdminToken();

            mockMvc.perform(get(STORES_API)
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

            mockMvc.perform(get(STORES_API)
                            .header("Authorization", bearerToken(token))
                            .param("page", "1")
                            .param("size", "10"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.size").value(10))
                    .andExpect(jsonPath("$.data.page").value(1));
        }

        @Test
        @DisplayName("應支援按類型篩選")
        void getAll_WithTypeFilter_ShouldReturnFilteredResults() throws Exception {
            String token = generateAdminToken();

            mockMvc.perform(get(STORES_API)
                            .header("Authorization", bearerToken(token))
                            .param("type", "STORE"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/stores/{id} - 根據 ID 查詢門市")
    class GetStoreByIdTests {

        @Test
        @DisplayName("有效 ID 應返回門市詳情")
        void getById_WithValidId_ShouldReturnStore() throws Exception {
            String token = generateAdminToken();

            mockMvc.perform(get(STORES_API + "/" + testStoreEntity.getId())
                            .header("Authorization", bearerToken(token)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(testStoreEntity.getId()))
                    .andExpect(jsonPath("$.data.code").value("TEST-STORE"));
        }

        @Test
        @DisplayName("不存在的 ID 應返回 404")
        void getById_WithInvalidId_ShouldReturnNotFound() throws Exception {
            String token = generateAdminToken();

            mockMvc.perform(get(STORES_API + "/99999")
                            .header("Authorization", bearerToken(token)))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/stores/code/{code} - 根據代碼查詢門市")
    class GetStoreByCodeTests {

        @Test
        @DisplayName("有效代碼應返回門市")
        void getByCode_WithValidCode_ShouldReturnStore() throws Exception {
            String token = generateAdminToken();

            mockMvc.perform(get(STORES_API + "/code/TEST-STORE")
                            .header("Authorization", bearerToken(token)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.code").value("TEST-STORE"));
        }

        @Test
        @DisplayName("不存在的代碼應返回 404")
        void getByCode_WithInvalidCode_ShouldReturnNotFound() throws Exception {
            String token = generateAdminToken();

            mockMvc.perform(get(STORES_API + "/code/NOT-EXIST")
                            .header("Authorization", bearerToken(token)))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/stores/{id} - 更新門市")
    class UpdateStoreTests {

        @Test
        @DisplayName("有效資料應更新門市成功")
        void update_WithValidData_ShouldUpdateStore() throws Exception {
            String token = generateAdminToken();
            StoreController.UpdateStoreRequest request = StoreController.UpdateStoreRequest.builder()
                    .name("更新後的門市名稱")
                    .address("更新後的地址")
                    .active(true)
                    .build();

            mockMvc.perform(put(STORES_API + "/" + testStoreEntity.getId())
                            .header("Authorization", bearerToken(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.name").value("更新後的門市名稱"));
        }

        @Test
        @DisplayName("更新不存在的門市應返回 404")
        void update_WithInvalidId_ShouldReturnNotFound() throws Exception {
            String token = generateAdminToken();
            StoreController.UpdateStoreRequest request = StoreController.UpdateStoreRequest.builder()
                    .name("更新門市")
                    .build();

            mockMvc.perform(put(STORES_API + "/99999")
                            .header("Authorization", bearerToken(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/stores/{id} - 刪除門市")
    class DeleteStoreTests {

        @Test
        @DisplayName("有效 ID 應軟刪除門市成功")
        void delete_WithValidId_ShouldSoftDeleteStore() throws Exception {
            String token = generateAdminToken();

            mockMvc.perform(delete(STORES_API + "/" + testStoreEntity.getId())
                            .header("Authorization", bearerToken(token)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("刪除不存在的門市應返回 404")
        void delete_WithInvalidId_ShouldReturnNotFound() throws Exception {
            String token = generateAdminToken();

            mockMvc.perform(delete(STORES_API + "/99999")
                            .header("Authorization", bearerToken(token)))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/stores/active - 查詢啟用的門市")
    class GetActiveStoresTests {

        @Test
        @DisplayName("應返回啟用的門市列表")
        void getActive_ShouldReturnActiveStores() throws Exception {
            String token = generateAdminToken();

            mockMvc.perform(get(STORES_API + "/active")
                            .header("Authorization", bearerToken(token)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/stores/type/store - 查詢所有門市")
    class GetAllStoresTests {

        @Test
        @DisplayName("應返回所有門市")
        void getAllStores_ShouldReturnStores() throws Exception {
            String token = generateAdminToken();

            mockMvc.perform(get(STORES_API + "/type/store")
                            .header("Authorization", bearerToken(token)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/stores/type/warehouse - 查詢所有倉庫")
    class GetAllWarehousesTests {

        @Test
        @DisplayName("應返回所有倉庫")
        void getAllWarehouses_ShouldReturnWarehouses() throws Exception {
            String token = generateAdminToken();

            mockMvc.perform(get(STORES_API + "/type/warehouse")
                            .header("Authorization", bearerToken(token)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray());
        }
    }
}
