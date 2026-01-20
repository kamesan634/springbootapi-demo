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
import com.kamesan.erpapi.sales.dto.CreatePaymentRequest;
import com.kamesan.erpapi.sales.dto.OrderDto;
import com.kamesan.erpapi.sales.entity.Order;
import com.kamesan.erpapi.sales.entity.PaymentMethod;
import com.kamesan.erpapi.sales.repository.OrderRepository;
import com.kamesan.erpapi.sales.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 付款控制器測試類別
 */
@DisplayName("付款控制器測試")
class PaymentControllerTest extends BaseIntegrationTest {

    private static final String PAYMENTS_API = "/api/v1/payments";

    @Autowired
    private OrderService orderService;

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

    private OrderDto testOrder;
    private Product testProduct;

    @BeforeEach
    void setUpPaymentTestData() {
        // 初始化分類
        Category testCategory = categoryRepository.findByCode("PAY-TEST-CAT").orElseGet(() -> {
            Category cat = new Category();
            cat.setCode("PAY-TEST-CAT");
            cat.setName("付款測試分類");
            cat.setActive(true);
            return categoryRepository.save(cat);
        });

        // 初始化單位
        Unit testUnit = unitRepository.findByCode("PAY-TEST-UNIT").orElseGet(() -> {
            Unit unit = new Unit();
            unit.setCode("PAY-TEST-UNIT");
            unit.setName("個");
            unit.setActive(true);
            return unitRepository.save(unit);
        });

        // 初始化稅別
        TaxType testTaxType = taxTypeRepository.findByCode("PAY-TEST-TAX").orElseGet(() -> {
            TaxType taxType = new TaxType();
            taxType.setCode("PAY-TEST-TAX");
            taxType.setName("測試稅別");
            taxType.setRate(new BigDecimal("0.05"));
            taxType.setActive(true);
            return taxTypeRepository.save(taxType);
        });

        // 初始化商品
        testProduct = productRepository.findBySku("PAY-TEST-SKU").orElseGet(() -> {
            Product product = new Product();
            product.setSku("PAY-TEST-SKU");
            product.setName("付款測試商品");
            product.setCategory(testCategory);
            product.setUnit(testUnit);
            product.setTaxType(testTaxType);
            product.setCostPrice(new BigDecimal("100.00"));
            product.setSellingPrice(new BigDecimal("200.00"));
            product.setActive(true);
            return productRepository.save(product);
        });

        // 建立測試訂單
        CreateOrderRequest.CreateOrderItemRequest itemRequest = CreateOrderRequest.CreateOrderItemRequest.builder()
                .productId(testProduct.getId())
                .quantity(2)
                .unitPrice(new BigDecimal("200.00"))
                .build();

        CreateOrderRequest orderRequest = CreateOrderRequest.builder()
                .storeId(testStore.getId())
                .items(List.of(itemRequest))
                .build();

        testOrder = orderService.createOrder(orderRequest);
    }

    @Nested
    @DisplayName("POST /api/v1/payments - 處理付款")
    class ProcessPaymentTests {

        @Test
        @DisplayName("有效資料應處理付款成功")
        void processPayment_WithValidData_ShouldCreatePayment() throws Exception {
            String token = generateAdminToken();
            CreatePaymentRequest request = CreatePaymentRequest.builder()
                    .orderId(testOrder.getId())
                    .paymentMethod(PaymentMethod.CASH)
                    .amount(testOrder.getTotalAmount())
                    .build();

            mockMvc.perform(post(PAYMENTS_API)
                            .header("Authorization", bearerToken(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.orderId").value(testOrder.getId()));
        }

        @Test
        @DisplayName("未驗證應返回 403")
        void processPayment_WithoutAuth_ShouldReturnForbidden() throws Exception {
            CreatePaymentRequest request = CreatePaymentRequest.builder()
                    .orderId(testOrder.getId())
                    .paymentMethod(PaymentMethod.CASH)
                    .amount(new BigDecimal("100.00"))
                    .build();

            mockMvc.perform(post(PAYMENTS_API)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("缺少必填欄位應返回驗證錯誤")
        void processPayment_WithMissingRequiredFields_ShouldReturnBadRequest() throws Exception {
            String token = generateAdminToken();
            CreatePaymentRequest request = CreatePaymentRequest.builder()
                    .orderId(testOrder.getId())
                    // paymentMethod 缺少
                    .build();

            mockMvc.perform(post(PAYMENTS_API)
                            .header("Authorization", bearerToken(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/payments/{id} - 查詢付款記錄")
    class GetPaymentByIdTests {

        @Test
        @DisplayName("不存在的 ID 應返回 404")
        void getById_WithInvalidId_ShouldReturnNotFound() throws Exception {
            String token = generateAdminToken();

            mockMvc.perform(get(PAYMENTS_API + "/99999")
                            .header("Authorization", bearerToken(token)))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/payments/order/{orderId} - 查詢訂單付款記錄")
    class GetPaymentsByOrderTests {

        @Test
        @DisplayName("有效訂單 ID 應返回付款記錄列表")
        void getByOrderId_WithValidOrderId_ShouldReturnPayments() throws Exception {
            String token = generateAdminToken();

            mockMvc.perform(get(PAYMENTS_API + "/order/" + testOrder.getId())
                            .header("Authorization", bearerToken(token)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/payments - 查詢付款記錄列表")
    class GetPaymentsTests {

        @Test
        @DisplayName("未驗證應返回 403")
        void getAll_WithoutAuth_ShouldReturnForbidden() throws Exception {
            String startDate = LocalDateTime.now().minusDays(30).format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            String endDate = LocalDateTime.now().plusDays(1).format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME);

            mockMvc.perform(get(PAYMENTS_API)
                            .param("startDate", startDate)
                            .param("endDate", endDate))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }
    }
}
