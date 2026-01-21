package com.kamesan.erpapi.inventory.controller;

import com.kamesan.erpapi.BaseIntegrationTest;
import com.kamesan.erpapi.inventory.dto.AdjustInventoryRequest;
import com.kamesan.erpapi.inventory.entity.Inventory;
import com.kamesan.erpapi.inventory.entity.MovementType;
import com.kamesan.erpapi.inventory.repository.InventoryRepository;
import com.kamesan.erpapi.products.entity.Category;
import com.kamesan.erpapi.products.entity.Product;
import com.kamesan.erpapi.products.entity.TaxType;
import com.kamesan.erpapi.products.entity.Unit;
import com.kamesan.erpapi.products.repository.CategoryRepository;
import com.kamesan.erpapi.products.repository.ProductRepository;
import com.kamesan.erpapi.products.repository.TaxTypeRepository;
import com.kamesan.erpapi.products.repository.UnitRepository;
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
 * 庫存控制器測試類別
 *
 * <p>測試庫存相關的 API 端點：</p>
 * <ul>
 *   <li>GET /api/v1/inventories - 查詢庫存列表</li>
 *   <li>GET /api/v1/inventories/{id} - 查詢庫存詳情</li>
 *   <li>POST /api/v1/inventories/adjust - 調整庫存</li>
 *   <li>GET /api/v1/inventory-movements - 查詢庫存異動記錄</li>
 * </ul>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@DisplayName("庫存控制器測試")
class InventoryControllerTest extends BaseIntegrationTest {

    /**
     * API 基礎路徑
     */
    private static final String INVENTORIES_API = "/api/v1/inventory";

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UnitRepository unitRepository;

    @Autowired
    private TaxTypeRepository taxTypeRepository;

    /**
     * 測試用商品
     */
    private Product testProduct;

    /**
     * 測試用庫存
     */
    private Inventory testInventory;

    /**
     * 在每個測試前初始化測試資料
     */
    @BeforeEach
    void setUpInventoryTestData() {
        // 初始化商品依賴
        Category category = categoryRepository.findByCode("INV-CAT").orElseGet(() -> {
            Category cat = new Category();
            cat.setCode("INV-CAT");
            cat.setName("庫存測試分類");
            cat.setActive(true);
            return categoryRepository.save(cat);
        });

        Unit unit = unitRepository.findByCode("INV-UNIT").orElseGet(() -> {
            Unit u = new Unit();
            u.setCode("INV-UNIT");
            u.setName("個");
            u.setActive(true);
            return unitRepository.save(u);
        });

        TaxType taxType = taxTypeRepository.findByCode("INV-TAX").orElseGet(() -> {
            TaxType t = new TaxType();
            t.setCode("INV-TAX");
            t.setName("應稅");
            t.setRate(new BigDecimal("0.05"));
            t.setActive(true);
            return taxTypeRepository.save(t);
        });

        // 初始化測試商品
        testProduct = productRepository.findBySku("INV-TEST-001").orElseGet(() -> {
            Product product = new Product();
            product.setSku("INV-TEST-001");
            product.setName("庫存測試商品");
            product.setCategory(category);
            product.setUnit(unit);
            product.setTaxType(taxType);
            product.setCostPrice(new BigDecimal("50.00"));
            product.setSellingPrice(new BigDecimal("100.00"));
            product.setBarcode("4711000000001");
            product.setActive(true);
            return productRepository.save(product);
        });

        // 初始化測試庫存
        testInventory = inventoryRepository.findByProductIdAndWarehouseId(testProduct.getId(), testStore.getId())
                .orElseGet(() -> {
                    Inventory inventory = new Inventory();
                    inventory.setProductId(testProduct.getId());
                    inventory.setWarehouseId(testStore.getId());
                    inventory.setQuantity(100);
                    inventory.setReservedQuantity(10);
                    return inventoryRepository.save(inventory);
                });
    }

    /**
     * 查詢庫存列表 API 測試
     */
    @Nested
    @DisplayName("GET /api/v1/inventories - 查詢庫存列表")
    class GetInventoriesTests {

        /**
         * 測試查詢庫存列表成功
         */
        @Test
        @DisplayName("應返回庫存列表")
        void getAll_ShouldReturnInventoryList() throws Exception {
            // Arrange
            String token = generateAdminToken();

            // Act & Assert - 使用倉庫端點查詢庫存
            mockMvc.perform(get(INVENTORIES_API + "/warehouse/" + testStore.getId())
                            .header("Authorization", bearerToken(token)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        /**
         * 測試按倉庫查詢庫存
         */
        @Test
        @DisplayName("應支援按倉庫查詢")
        void getAll_ByWarehouse_ShouldReturnFilteredResults() throws Exception {
            // Arrange
            String token = generateAdminToken();

            // Act & Assert - 使用倉庫端點查詢庫存
            mockMvc.perform(get(INVENTORIES_API + "/warehouse/" + testStore.getId())
                            .header("Authorization", bearerToken(token)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    /**
     * 查詢庫存詳情 API 測試
     */
    @Nested
    @DisplayName("GET /api/v1/inventories/{id} - 查詢庫存詳情")
    class GetInventoryByIdTests {

        /**
         * 測試查詢庫存詳情成功
         */
        @Test
        @DisplayName("有效商品和倉庫 ID 應返回庫存詳情")
        void getById_WithValidId_ShouldReturnInventory() throws Exception {
            // Arrange
            String token = generateAdminToken();

            // Act & Assert - 使用 productId 和 warehouseId 查詢
            mockMvc.perform(get(INVENTORIES_API)
                            .header("Authorization", bearerToken(token))
                            .param("productId", testProduct.getId().toString())
                            .param("warehouseId", testStore.getId().toString()))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.quantity").value(100));
        }

        /**
         * 測試查詢不存在的庫存
         */
        @Test
        @DisplayName("不存在的商品 ID 應返回 404")
        void getById_WithInvalidId_ShouldReturnNotFound() throws Exception {
            // Arrange
            String token = generateAdminToken();

            // Act & Assert - 使用不存在的 productId 和 warehouseId 查詢
            mockMvc.perform(get(INVENTORIES_API)
                            .header("Authorization", bearerToken(token))
                            .param("productId", "99999")
                            .param("warehouseId", testStore.getId().toString()))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }

    /**
     * 調整庫存 API 測試
     */
    @Nested
    @DisplayName("POST /api/v1/inventories/adjust - 調整庫存")
    class AdjustInventoryTests {

        /**
         * 測試增加庫存成功
         */
        @Test
        @DisplayName("增加庫存應成功")
        void adjust_Increase_ShouldSucceed() throws Exception {
            // Arrange
            String token = generateAdminToken();
            AdjustInventoryRequest request = AdjustInventoryRequest.builder()
                    .productId(testProduct.getId())
                    .warehouseId(testStore.getId())
                    .adjustmentType(MovementType.ADJUST_IN)
                    .quantity(50)
                    .reason("測試入庫")
                    .build();

            // Act & Assert
            mockMvc.perform(post(INVENTORIES_API + "/adjust")
                            .header("Authorization", bearerToken(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        /**
         * 測試減少庫存成功
         */
        @Test
        @DisplayName("減少庫存應成功")
        void adjust_Decrease_ShouldSucceed() throws Exception {
            // Arrange
            String token = generateAdminToken();
            AdjustInventoryRequest request = AdjustInventoryRequest.builder()
                    .productId(testProduct.getId())
                    .warehouseId(testStore.getId())
                    .adjustmentType(MovementType.ADJUST_OUT)
                    .quantity(20)
                    .reason("測試出庫")
                    .build();

            // Act & Assert
            mockMvc.perform(post(INVENTORIES_API + "/adjust")
                            .header("Authorization", bearerToken(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    /**
     * 查詢庫存異動記錄 API 測試
     */
    @Nested
    @DisplayName("GET /api/v1/inventory-movements - 查詢庫存異動記錄")
    class GetInventoryMovementsTests {

        /**
         * 測試查詢異動記錄
         */
        @Test
        @DisplayName("應返回異動記錄列表")
        void getMovements_ShouldReturnMovementList() throws Exception {
            // Arrange
            String token = generateAdminToken();

            // Act & Assert
            mockMvc.perform(get(INVENTORIES_API + "/movements/product/" + testProduct.getId())
                            .header("Authorization", bearerToken(token)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("應返回倉庫異動記錄列表")
        void getMovementsByWarehouse_ShouldReturnMovementList() throws Exception {
            String token = generateAdminToken();

            mockMvc.perform(get(INVENTORIES_API + "/movements/warehouse/" + testStore.getId())
                            .header("Authorization", bearerToken(token)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("應根據商品和倉庫查詢異動記錄")
        void getMovements_ByProductAndWarehouse_ShouldReturnMovements() throws Exception {
            String token = generateAdminToken();

            mockMvc.perform(get(INVENTORIES_API + "/movements")
                            .header("Authorization", bearerToken(token))
                            .param("productId", testProduct.getId().toString())
                            .param("warehouseId", testStore.getId().toString()))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("根據參考單號查詢應返回結果")
        void getMovementsByReferenceNo_ShouldReturnMovements() throws Exception {
            String token = generateAdminToken();

            mockMvc.perform(get(INVENTORIES_API + "/movements/reference/TEST-REF-001")
                            .header("Authorization", bearerToken(token)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("複合條件搜尋應返回結果")
        void searchMovements_ShouldReturnMovements() throws Exception {
            String token = generateAdminToken();
            String startDate = java.time.LocalDate.now().minusDays(30).format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE);
            String endDate = java.time.LocalDate.now().plusDays(1).format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE);

            mockMvc.perform(get(INVENTORIES_API + "/movements/search")
                            .header("Authorization", bearerToken(token))
                            .param("startDate", startDate)
                            .param("endDate", endDate))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/inventory/all - 查詢所有庫存")
    class GetAllInventoriesTests {

        @Test
        @DisplayName("應返回所有庫存分頁列表")
        void getAll_ShouldReturnPagedList() throws Exception {
            String token = generateAdminToken();

            mockMvc.perform(get(INVENTORIES_API + "/all")
                            .header("Authorization", bearerToken(token)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isArray());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/inventory/product/{productId} - 查詢商品庫存")
    class GetInventoriesByProductTests {

        @Test
        @DisplayName("應返回商品在所有倉庫的庫存")
        void getByProduct_ShouldReturnInventories() throws Exception {
            String token = generateAdminToken();

            mockMvc.perform(get(INVENTORIES_API + "/product/" + testProduct.getId())
                            .header("Authorization", bearerToken(token)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray());
        }

        @Test
        @DisplayName("應返回商品總庫存數量")
        void getTotalQuantity_ShouldReturnTotal() throws Exception {
            String token = generateAdminToken();

            mockMvc.perform(get(INVENTORIES_API + "/product/" + testProduct.getId() + "/total")
                            .header("Authorization", bearerToken(token)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isNumber());
        }

        @Test
        @DisplayName("應返回商品可用庫存數量")
        void getAvailableQuantity_ShouldReturnAvailable() throws Exception {
            String token = generateAdminToken();

            mockMvc.perform(get(INVENTORIES_API + "/product/" + testProduct.getId() + "/available")
                            .header("Authorization", bearerToken(token)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isNumber());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/inventory/low-stock - 低庫存預警")
    class LowStockTests {

        @Test
        @DisplayName("應返回低庫存列表")
        void getLowStock_ShouldReturnLowStockItems() throws Exception {
            String token = generateAdminToken();

            mockMvc.perform(get(INVENTORIES_API + "/low-stock")
                            .header("Authorization", bearerToken(token))
                            .param("threshold", "1000"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isArray());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/inventory/movement-types - 異動類型")
    class MovementTypesTests {

        @Test
        @DisplayName("應返回所有異動類型")
        void getMovementTypes_ShouldReturnAllTypes() throws Exception {
            String token = generateAdminToken();

            mockMvc.perform(get(INVENTORIES_API + "/movement-types")
                            .header("Authorization", bearerToken(token)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray());
        }
    }
}
