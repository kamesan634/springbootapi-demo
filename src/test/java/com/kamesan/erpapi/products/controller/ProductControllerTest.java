package com.kamesan.erpapi.products.controller;

import com.kamesan.erpapi.BaseIntegrationTest;
import com.kamesan.erpapi.products.dto.CreateProductRequest;
import com.kamesan.erpapi.products.dto.UpdateProductRequest;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 商品控制器測試類別
 *
 * <p>測試商品相關的 API 端點 CRUD 操作：</p>
 * <ul>
 *   <li>POST /api/v1/products - 建立商品</li>
 *   <li>GET /api/v1/products - 查詢商品列表</li>
 *   <li>GET /api/v1/products/{id} - 查詢商品詳情</li>
 *   <li>PUT /api/v1/products/{id} - 更新商品</li>
 *   <li>DELETE /api/v1/products/{id} - 刪除商品</li>
 * </ul>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@DisplayName("商品控制器測試")
class ProductControllerTest extends BaseIntegrationTest {

    /**
     * API 基礎路徑
     */
    private static final String PRODUCTS_API = "/api/v1/products";

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UnitRepository unitRepository;

    @Autowired
    private TaxTypeRepository taxTypeRepository;

    /**
     * 測試用分類
     */
    private Category testCategory;

    /**
     * 測試用單位
     */
    private Unit testUnit;

    /**
     * 測試用稅別
     */
    private TaxType testTaxType;

    /**
     * 測試用商品
     */
    private Product testProduct;

    /**
     * 在每個測試前初始化測試資料
     */
    @BeforeEach
    void setUpProductTestData() {
        // 初始化分類
        testCategory = categoryRepository.findByCode("TEST-CAT").orElseGet(() -> {
            Category cat = new Category();
            cat.setCode("TEST-CAT");
            cat.setName("測試分類");
            cat.setActive(true);
            return categoryRepository.save(cat);
        });

        // 初始化單位
        testUnit = unitRepository.findByCode("TEST-UNIT").orElseGet(() -> {
            Unit unit = new Unit();
            unit.setCode("TEST-UNIT");
            unit.setName("個");
            unit.setActive(true);
            return unitRepository.save(unit);
        });

        // 初始化稅別
        testTaxType = taxTypeRepository.findByCode("TEST-TAX").orElseGet(() -> {
            TaxType taxType = new TaxType();
            taxType.setCode("TEST-TAX");
            taxType.setName("測試稅別");
            taxType.setRate(new BigDecimal("0.05"));
            taxType.setActive(true);
            return taxTypeRepository.save(taxType);
        });

        // 初始化測試商品
        testProduct = productRepository.findBySku("TEST-SKU-001").orElseGet(() -> {
            Product product = new Product();
            product.setSku("TEST-SKU-001");
            product.setName("測試商品");
            product.setDescription("這是測試商品");
            product.setCategory(testCategory);
            product.setUnit(testUnit);
            product.setTaxType(testTaxType);
            product.setCostPrice(new BigDecimal("100.00"));
            product.setSellingPrice(new BigDecimal("200.00"));
            product.setBarcode("4710000000001");
            product.setSafetyStock(10);
            product.setActive(true);
            return productRepository.save(product);
        });
    }

    /**
     * 建立商品 API 測試
     */
    @Nested
    @DisplayName("POST /api/v1/products - 建立商品")
    class CreateProductTests {

        /**
         * 測試建立商品成功
         */
        @Test
        @DisplayName("有效資料應建立商品成功")
        void create_WithValidData_ShouldCreateProduct() throws Exception {
            // Arrange
            String token = generateAdminToken();
            CreateProductRequest request = CreateProductRequest.builder()
                    .sku("NEW-SKU-001")
                    .name("新商品")
                    .description("這是新商品描述")
                    .categoryId(testCategory.getId())
                    .unitId(testUnit.getId())
                    .taxTypeId(testTaxType.getId())
                    .costPrice(new BigDecimal("50.00"))
                    .sellingPrice(new BigDecimal("100.00"))
                    .barcode("4710000000002")
                    .safetyStock(20)
                    .build();

            // Act & Assert
            mockMvc.perform(post(PRODUCTS_API)
                            .header("Authorization", bearerToken(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.sku").value("NEW-SKU-001"))
                    .andExpect(jsonPath("$.data.name").value("新商品"))
                    .andExpect(jsonPath("$.data.sellingPrice").value(100.00));
        }

        /**
         * 測試未驗證時建立商品失敗
         */
        @Test
        @DisplayName("未驗證應返回 401")
        void create_WithoutAuth_ShouldReturnUnauthorized() throws Exception {
            // Arrange
            CreateProductRequest request = CreateProductRequest.builder()
                    .sku("NEW-SKU-002")
                    .name("新商品2")
                    .sellingPrice(new BigDecimal("100.00"))
                    .build();

            // Act & Assert
            mockMvc.perform(post(PRODUCTS_API)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }

        /**
         * 測試缺少必填欄位
         */
        @Test
        @DisplayName("缺少必填欄位應返回驗證錯誤")
        void create_WithMissingRequiredFields_ShouldReturnBadRequest() throws Exception {
            // Arrange
            String token = generateAdminToken();
            CreateProductRequest request = CreateProductRequest.builder()
                    .sku("NEW-SKU-003")
                    // name 缺少
                    .sellingPrice(new BigDecimal("100.00"))
                    .build();

            // Act & Assert
            mockMvc.perform(post(PRODUCTS_API)
                            .header("Authorization", bearerToken(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        /**
         * 測試重複 SKU
         */
        @Test
        @DisplayName("重複 SKU 應返回錯誤")
        void create_WithDuplicateSku_ShouldReturnError() throws Exception {
            // Arrange
            String token = generateAdminToken();
            CreateProductRequest request = CreateProductRequest.builder()
                    .sku("TEST-SKU-001") // 已存在的 SKU
                    .name("重複商品")
                    .sellingPrice(new BigDecimal("100.00"))
                    .build();

            // Act & Assert
            mockMvc.perform(post(PRODUCTS_API)
                            .header("Authorization", bearerToken(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andDo(print())
                    .andExpect(status().isConflict());
        }
    }

    /**
     * 查詢商品列表 API 測試
     */
    @Nested
    @DisplayName("GET /api/v1/products - 查詢商品列表")
    class GetProductsTests {

        /**
         * 測試查詢商品列表成功
         */
        @Test
        @DisplayName("應返回商品列表")
        void getAll_ShouldReturnProductList() throws Exception {
            // Arrange
            String token = generateAdminToken();

            // Act & Assert
            mockMvc.perform(get(PRODUCTS_API)
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
            mockMvc.perform(get(PRODUCTS_API)
                            .header("Authorization", bearerToken(token))
                            .param("page", "1")
                            .param("size", "5"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.size").value(5))
                    .andExpect(jsonPath("$.data.page").value(1));
        }
    }

    /**
     * 查詢商品詳情 API 測試
     */
    @Nested
    @DisplayName("GET /api/v1/products/{id} - 查詢商品詳情")
    class GetProductByIdTests {

        /**
         * 測試查詢商品詳情成功
         */
        @Test
        @DisplayName("有效 ID 應返回商品詳情")
        void getById_WithValidId_ShouldReturnProduct() throws Exception {
            // Arrange
            String token = generateAdminToken();

            // Act & Assert
            mockMvc.perform(get(PRODUCTS_API + "/" + testProduct.getId())
                            .header("Authorization", bearerToken(token)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(testProduct.getId()))
                    .andExpect(jsonPath("$.data.sku").value("TEST-SKU-001"))
                    .andExpect(jsonPath("$.data.name").value("測試商品"));
        }

        /**
         * 測試查詢不存在的商品
         */
        @Test
        @DisplayName("不存在的 ID 應返回 404")
        void getById_WithInvalidId_ShouldReturnNotFound() throws Exception {
            // Arrange
            String token = generateAdminToken();

            // Act & Assert
            mockMvc.perform(get(PRODUCTS_API + "/99999")
                            .header("Authorization", bearerToken(token)))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }

    /**
     * 按 SKU 查詢商品 API 測試
     */
    @Nested
    @DisplayName("GET /api/v1/products/sku/{sku} - 按 SKU 查詢")
    class GetProductBySkuTests {

        /**
         * 測試按 SKU 查詢成功
         */
        @Test
        @DisplayName("有效 SKU 應返回商品")
        void getBySku_WithValidSku_ShouldReturnProduct() throws Exception {
            // Arrange
            String token = generateAdminToken();

            // Act & Assert
            mockMvc.perform(get(PRODUCTS_API + "/sku/TEST-SKU-001")
                            .header("Authorization", bearerToken(token)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.sku").value("TEST-SKU-001"));
        }
    }

    /**
     * 更新商品 API 測試
     */
    @Nested
    @DisplayName("PUT /api/v1/products/{id} - 更新商品")
    class UpdateProductTests {

        /**
         * 測試更新商品成功
         */
        @Test
        @DisplayName("有效資料應更新商品成功")
        void update_WithValidData_ShouldUpdateProduct() throws Exception {
            // Arrange
            String token = generateAdminToken();
            UpdateProductRequest request = UpdateProductRequest.builder()
                    .sku("TEST-SKU-001")
                    .name("更新後的商品名稱")
                    .description("更新後的描述")
                    .sellingPrice(new BigDecimal("300.00"))
                    .build();

            // Act & Assert
            mockMvc.perform(put(PRODUCTS_API + "/" + testProduct.getId())
                            .header("Authorization", bearerToken(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.name").value("更新後的商品名稱"))
                    .andExpect(jsonPath("$.data.sellingPrice").value(300.00));
        }

        /**
         * 測試更新不存在的商品
         */
        @Test
        @DisplayName("更新不存在的商品應返回 404")
        void update_WithInvalidId_ShouldReturnNotFound() throws Exception {
            // Arrange
            String token = generateAdminToken();
            UpdateProductRequest request = UpdateProductRequest.builder()
                    .sku("NOT-EXIST-SKU")
                    .name("更新商品")
                    .sellingPrice(new BigDecimal("100.00"))
                    .build();

            // Act & Assert
            mockMvc.perform(put(PRODUCTS_API + "/99999")
                            .header("Authorization", bearerToken(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }

    /**
     * 刪除商品 API 測試
     */
    @Nested
    @DisplayName("DELETE /api/v1/products/{id} - 刪除商品")
    class DeleteProductTests {

        /**
         * 測試刪除商品成功（軟刪除）
         */
        @Test
        @DisplayName("有效 ID 應軟刪除商品成功")
        void delete_WithValidId_ShouldSoftDeleteProduct() throws Exception {
            // Arrange
            String token = generateAdminToken();

            // Act & Assert
            mockMvc.perform(delete(PRODUCTS_API + "/" + testProduct.getId())
                            .header("Authorization", bearerToken(token)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        /**
         * 測試刪除不存在的商品
         */
        @Test
        @DisplayName("刪除不存在的商品應返回 404")
        void delete_WithInvalidId_ShouldReturnNotFound() throws Exception {
            // Arrange
            String token = generateAdminToken();

            // Act & Assert
            mockMvc.perform(delete(PRODUCTS_API + "/99999")
                            .header("Authorization", bearerToken(token)))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }

    /**
     * 搜尋商品 API 測試
     */
    @Nested
    @DisplayName("GET /api/v1/products/search - 搜尋商品")
    class SearchProductsTests {

        /**
         * 測試搜尋商品
         */
        @Test
        @DisplayName("關鍵字搜尋應返回相符商品")
        void search_WithKeyword_ShouldReturnMatchingProducts() throws Exception {
            // Arrange
            String token = generateAdminToken();

            // Act & Assert
            mockMvc.perform(get(PRODUCTS_API + "/search")
                            .header("Authorization", bearerToken(token))
                            .param("keyword", "測試"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }
}
