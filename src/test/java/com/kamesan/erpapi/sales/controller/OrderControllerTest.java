package com.kamesan.erpapi.sales.controller;

import com.kamesan.erpapi.BaseIntegrationTest;
import com.kamesan.erpapi.products.entity.Category;
import com.kamesan.erpapi.products.entity.Product;
import com.kamesan.erpapi.products.entity.TaxType;
import com.kamesan.erpapi.products.entity.Unit;
import com.kamesan.erpapi.products.repository.CategoryRepository;
import com.kamesan.erpapi.products.repository.ProductRepository;
import com.kamesan.erpapi.products.repository.TaxTypeRepository;
import com.kamesan.erpapi.products.repository.UnitRepository;
import com.kamesan.erpapi.sales.dto.CreateOrderRequest;
import com.kamesan.erpapi.sales.entity.Order;
import com.kamesan.erpapi.sales.entity.OrderStatus;
import com.kamesan.erpapi.sales.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 訂單控制器測試類別
 *
 * <p>測試訂單相關的 API 端點 CRUD 操作：</p>
 * <ul>
 *   <li>POST /api/v1/orders - 建立訂單</li>
 *   <li>GET /api/v1/orders - 查詢訂單列表</li>
 *   <li>GET /api/v1/orders/{id} - 查詢訂單詳情</li>
 *   <li>PUT /api/v1/orders/{id} - 更新訂單</li>
 *   <li>POST /api/v1/orders/{id}/cancel - 取消訂單</li>
 * </ul>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@DisplayName("訂單控制器測試")
class OrderControllerTest extends BaseIntegrationTest {

    /**
     * API 基礎路徑
     */
    private static final String ORDERS_API = "/api/v1/orders";

    @Autowired
    private OrderRepository orderRepository;

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
     * 測試用訂單
     */
    private Order testOrder;

    /**
     * 在每個測試前初始化測試資料
     */
    @BeforeEach
    void setUpOrderTestData() {
        // 初始化商品依賴
        Category category = categoryRepository.findByCode("ORDER-CAT").orElseGet(() -> {
            Category cat = new Category();
            cat.setCode("ORDER-CAT");
            cat.setName("訂單測試分類");
            cat.setActive(true);
            return categoryRepository.save(cat);
        });

        Unit unit = unitRepository.findByCode("ORDER-UNIT").orElseGet(() -> {
            Unit u = new Unit();
            u.setCode("ORDER-UNIT");
            u.setName("個");
            u.setActive(true);
            return unitRepository.save(u);
        });

        TaxType taxType = taxTypeRepository.findByCode("ORDER-TAX").orElseGet(() -> {
            TaxType t = new TaxType();
            t.setCode("ORDER-TAX");
            t.setName("應稅");
            t.setRate(new BigDecimal("0.05"));
            t.setActive(true);
            return taxTypeRepository.save(t);
        });

        // 初始化測試商品
        testProduct = productRepository.findBySku("ORDER-TEST-001").orElseGet(() -> {
            Product product = new Product();
            product.setSku("ORDER-TEST-001");
            product.setName("訂單測試商品");
            product.setCategory(category);
            product.setUnit(unit);
            product.setTaxType(taxType);
            product.setCostPrice(new BigDecimal("50.00"));
            product.setSellingPrice(new BigDecimal("100.00"));
            product.setBarcode("4712000000001");
            product.setActive(true);
            return productRepository.save(product);
        });

        // 初始化測試訂單
        testOrder = orderRepository.findByOrderNo("ORD-TEST-001").orElseGet(() -> {
            Order order = new Order();
            order.setOrderNo("ORD-TEST-001");
            order.setStoreId(testStore.getId());
            order.setOrderDate(LocalDate.now());
            order.setOrderTime(LocalTime.now());
            order.setSubtotal(new BigDecimal("200.00"));
            order.setDiscountAmount(new BigDecimal("0.00"));
            order.setTaxAmount(new BigDecimal("10.00"));
            order.setTotalAmount(new BigDecimal("210.00"));
            order.setPaidAmount(BigDecimal.ZERO);
            order.setChangeAmount(BigDecimal.ZERO);
            order.setStatus(OrderStatus.PENDING);
            return orderRepository.save(order);
        });
    }

    /**
     * 建立訂單 API 測試
     */
    @Nested
    @DisplayName("POST /api/v1/orders - 建立訂單")
    class CreateOrderTests {

        /**
         * 測試建立訂單成功
         */
        @Test
        @DisplayName("有效資料應建立訂單成功")
        void create_WithValidData_ShouldCreateOrder() throws Exception {
            // Arrange
            String token = generateAdminToken();
            CreateOrderRequest.CreateOrderItemRequest itemRequest = CreateOrderRequest.CreateOrderItemRequest.builder()
                    .productId(testProduct.getId())
                    .quantity(2)
                    .unitPrice(testProduct.getSellingPrice())
                    .build();

            CreateOrderRequest request = CreateOrderRequest.builder()
                    .storeId(testStore.getId())
                    .items(List.of(itemRequest))
                    .notes("測試訂單")
                    .build();

            // Act & Assert
            mockMvc.perform(post(ORDERS_API)
                            .header("Authorization", bearerToken(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.orderNo").isNotEmpty())
                    .andExpect(jsonPath("$.data.status").value("PENDING"));
        }

        /**
         * 測試建立訂單缺少商品項目
         */
        @Test
        @DisplayName("缺少商品項目應返回錯誤")
        void create_WithoutItems_ShouldReturnBadRequest() throws Exception {
            // Arrange
            String token = generateAdminToken();
            CreateOrderRequest request = CreateOrderRequest.builder()
                    .storeId(testStore.getId())
                    .items(List.of()) // 空的商品列表
                    .build();

            // Act & Assert
            mockMvc.perform(post(ORDERS_API)
                            .header("Authorization", bearerToken(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }

    /**
     * 查詢訂單列表 API 測試
     */
    @Nested
    @DisplayName("GET /api/v1/orders - 查詢訂單列表")
    class GetOrdersTests {

        /**
         * 測試查詢訂單列表成功
         */
        @Test
        @DisplayName("應返回訂單列表")
        void getAll_ShouldReturnOrderList() throws Exception {
            // Arrange
            String token = generateAdminToken();

            // Act & Assert
            mockMvc.perform(get(ORDERS_API)
                            .header("Authorization", bearerToken(token)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isArray());
        }

        /**
         * 測試按狀態查詢訂單
         */
        @Test
        @DisplayName("應支援按狀態查詢")
        void getAll_ByStatus_ShouldReturnFilteredResults() throws Exception {
            // Arrange
            String token = generateAdminToken();

            // Act & Assert
            mockMvc.perform(get(ORDERS_API + "/status/PENDING")
                            .header("Authorization", bearerToken(token)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        /**
         * 測試按門市查詢訂單
         */
        @Test
        @DisplayName("應支援按門市查詢")
        void getAll_ByStore_ShouldReturnFilteredResults() throws Exception {
            // Arrange
            String token = generateAdminToken();

            // Act & Assert
            mockMvc.perform(get(ORDERS_API + "/store/" + testStore.getId())
                            .header("Authorization", bearerToken(token)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    /**
     * 查詢訂單詳情 API 測試
     */
    @Nested
    @DisplayName("GET /api/v1/orders/{id} - 查詢訂單詳情")
    class GetOrderByIdTests {

        /**
         * 測試查詢訂單詳情成功
         */
        @Test
        @DisplayName("有效 ID 應返回訂單詳情")
        void getById_WithValidId_ShouldReturnOrder() throws Exception {
            // Arrange
            String token = generateAdminToken();

            // Act & Assert
            mockMvc.perform(get(ORDERS_API + "/" + testOrder.getId())
                            .header("Authorization", bearerToken(token)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(testOrder.getId()))
                    .andExpect(jsonPath("$.data.orderNo").value("ORD-TEST-001"));
        }

        /**
         * 測試查詢不存在的訂單
         */
        @Test
        @DisplayName("不存在的 ID 應返回 404")
        void getById_WithInvalidId_ShouldReturnNotFound() throws Exception {
            // Arrange
            String token = generateAdminToken();

            // Act & Assert
            mockMvc.perform(get(ORDERS_API + "/99999")
                            .header("Authorization", bearerToken(token)))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }

        /**
         * 測試按訂單編號查詢
         */
        @Test
        @DisplayName("按訂單編號查詢應返回訂單")
        void getByOrderNo_ShouldReturnOrder() throws Exception {
            // Arrange
            String token = generateAdminToken();

            // Act & Assert
            mockMvc.perform(get(ORDERS_API + "/by-order-no/ORD-TEST-001")
                            .header("Authorization", bearerToken(token)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.orderNo").value("ORD-TEST-001"));
        }
    }

    /**
     * 取消訂單 API 測試
     */
    @Nested
    @DisplayName("POST /api/v1/orders/{id}/cancel - 取消訂單")
    class CancelOrderTests {

        /**
         * 測試取消訂單成功
         */
        @Test
        @DisplayName("待處理訂單應能取消")
        void cancel_PendingOrder_ShouldSucceed() throws Exception {
            // Arrange
            String token = generateAdminToken();

            // Act & Assert
            mockMvc.perform(post(ORDERS_API + "/" + testOrder.getId() + "/cancel")
                            .header("Authorization", bearerToken(token)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.status").value("CANCELLED"));
        }
    }

    /**
     * 刪除訂單 API 測試
     */
    @Nested
    @DisplayName("DELETE /api/v1/orders/{id} - 刪除訂單")
    class DeleteOrderTests {

        /**
         * 測試刪除訂單成功
         */
        @Test
        @DisplayName("有效 ID 應刪除訂單成功")
        void delete_WithValidId_ShouldDeleteOrder() throws Exception {
            // Arrange
            String token = generateAdminToken();

            // Act & Assert
            mockMvc.perform(delete(ORDERS_API + "/" + testOrder.getId())
                            .header("Authorization", bearerToken(token)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }
}
