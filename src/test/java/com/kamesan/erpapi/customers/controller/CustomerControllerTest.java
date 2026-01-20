package com.kamesan.erpapi.customers.controller;

import com.kamesan.erpapi.BaseIntegrationTest;
import com.kamesan.erpapi.customers.dto.CreateCustomerRequest;
import com.kamesan.erpapi.customers.dto.UpdateCustomerRequest;
import com.kamesan.erpapi.customers.entity.Customer;
import com.kamesan.erpapi.customers.entity.CustomerLevel;
import com.kamesan.erpapi.customers.repository.CustomerLevelRepository;
import com.kamesan.erpapi.customers.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 客戶控制器測試類別
 *
 * <p>測試客戶相關的 API 端點 CRUD 操作：</p>
 * <ul>
 *   <li>POST /api/v1/customers - 建立客戶</li>
 *   <li>GET /api/v1/customers - 查詢客戶列表</li>
 *   <li>GET /api/v1/customers/{id} - 查詢客戶詳情</li>
 *   <li>PUT /api/v1/customers/{id} - 更新客戶</li>
 *   <li>DELETE /api/v1/customers/{id} - 刪除客戶</li>
 * </ul>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@DisplayName("客戶控制器測試")
class CustomerControllerTest extends BaseIntegrationTest {

    /**
     * API 基礎路徑
     */
    private static final String CUSTOMERS_API = "/api/v1/customers";

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CustomerLevelRepository customerLevelRepository;

    /**
     * 測試用會員等級
     */
    private CustomerLevel testLevel;

    /**
     * 測試用客戶
     */
    private Customer testCustomer;

    /**
     * 在每個測試前初始化測試資料
     */
    @BeforeEach
    void setUpCustomerTestData() {
        // 初始化會員等級
        testLevel = customerLevelRepository.findByCode("TEST-LEVEL").orElseGet(() -> {
            CustomerLevel level = new CustomerLevel();
            level.setCode("TEST-LEVEL");
            level.setName("測試等級");
            level.setDiscountRate(new BigDecimal("0.95"));
            level.setUpgradeCondition(new BigDecimal("1000.00"));
            level.setPointsMultiplier(new BigDecimal("1.5"));
            level.setDescription("測試用會員等級");
            level.setActive(true);
            return customerLevelRepository.save(level);
        });

        // 初始化測試客戶
        testCustomer = customerRepository.findByMemberNo("M202401010001").orElseGet(() -> {
            Customer customer = new Customer();
            customer.setMemberNo("M202401010001");
            customer.setName("測試客戶");
            customer.setPhone("0912345678");
            customer.setEmail("test@example.com");
            customer.setGender("M");
            customer.setBirthday(LocalDate.of(1990, 5, 15));
            customer.setLevel(testLevel);
            customer.setTotalPoints(100);
            customer.setTotalSpent(new BigDecimal("5000.00"));
            customer.setActive(true);
            return customerRepository.save(customer);
        });
    }

    /**
     * 建立客戶 API 測試
     */
    @Nested
    @DisplayName("POST /api/v1/customers - 建立客戶")
    class CreateCustomerTests {

        /**
         * 測試建立客戶成功
         */
        @Test
        @DisplayName("有效資料應建立客戶成功")
        void create_WithValidData_ShouldCreateCustomer() throws Exception {
            // Arrange
            String token = generateAdminToken();
            CreateCustomerRequest request = CreateCustomerRequest.builder()
                    .name("新客戶")
                    .phone("0923456789")
                    .email("newcustomer@example.com")
                    .gender("F")
                    .birthday(LocalDate.of(1995, 8, 20))
                    .levelId(testLevel.getId())
                    .address("台北市測試區")
                    .build();

            // Act & Assert
            mockMvc.perform(post(CUSTOMERS_API)
                            .header("Authorization", bearerToken(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.name").value("新客戶"))
                    .andExpect(jsonPath("$.data.memberNo").isNotEmpty());
        }

        /**
         * 測試缺少必填欄位
         */
        @Test
        @DisplayName("缺少必填欄位應返回驗證錯誤")
        void create_WithMissingRequiredFields_ShouldReturnBadRequest() throws Exception {
            // Arrange
            String token = generateAdminToken();
            CreateCustomerRequest request = CreateCustomerRequest.builder()
                    // name 缺少
                    .phone("0934567890")
                    .build();

            // Act & Assert
            mockMvc.perform(post(CUSTOMERS_API)
                            .header("Authorization", bearerToken(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }

    /**
     * 查詢客戶列表 API 測試
     */
    @Nested
    @DisplayName("GET /api/v1/customers - 查詢客戶列表")
    class GetCustomersTests {

        /**
         * 測試查詢客戶列表成功
         */
        @Test
        @DisplayName("應返回客戶列表")
        void getAll_ShouldReturnCustomerList() throws Exception {
            // Arrange
            String token = generateAdminToken();

            // Act & Assert
            mockMvc.perform(get(CUSTOMERS_API)
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
            mockMvc.perform(get(CUSTOMERS_API)
                            .header("Authorization", bearerToken(token))
                            .param("page", "1")
                            .param("size", "10"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.size").value(10))
                    .andExpect(jsonPath("$.data.page").value(1));
        }
    }

    /**
     * 查詢客戶詳情 API 測試
     */
    @Nested
    @DisplayName("GET /api/v1/customers/{id} - 查詢客戶詳情")
    class GetCustomerByIdTests {

        /**
         * 測試查詢客戶詳情成功
         */
        @Test
        @DisplayName("有效 ID 應返回客戶詳情")
        void getById_WithValidId_ShouldReturnCustomer() throws Exception {
            // Arrange
            String token = generateAdminToken();

            // Act & Assert
            mockMvc.perform(get(CUSTOMERS_API + "/" + testCustomer.getId())
                            .header("Authorization", bearerToken(token)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(testCustomer.getId()))
                    .andExpect(jsonPath("$.data.name").value("測試客戶"));
        }

        /**
         * 測試查詢不存在的客戶
         */
        @Test
        @DisplayName("不存在的 ID 應返回 404")
        void getById_WithInvalidId_ShouldReturnNotFound() throws Exception {
            // Arrange
            String token = generateAdminToken();

            // Act & Assert
            mockMvc.perform(get(CUSTOMERS_API + "/99999")
                            .header("Authorization", bearerToken(token)))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }

    /**
     * 更新客戶 API 測試
     */
    @Nested
    @DisplayName("PUT /api/v1/customers/{id} - 更新客戶")
    class UpdateCustomerTests {

        /**
         * 測試更新客戶成功
         */
        @Test
        @DisplayName("有效資料應更新客戶成功")
        void update_WithValidData_ShouldUpdateCustomer() throws Exception {
            // Arrange
            String token = generateAdminToken();
            UpdateCustomerRequest request = UpdateCustomerRequest.builder()
                    .name("更新後的客戶名稱")
                    .phone("0987654321")
                    .email("updated@example.com")
                    .build();

            // Act & Assert
            mockMvc.perform(put(CUSTOMERS_API + "/" + testCustomer.getId())
                            .header("Authorization", bearerToken(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.name").value("更新後的客戶名稱"));
        }
    }

    /**
     * 刪除客戶 API 測試
     */
    @Nested
    @DisplayName("DELETE /api/v1/customers/{id} - 刪除客戶")
    class DeleteCustomerTests {

        /**
         * 測試刪除客戶成功
         */
        @Test
        @DisplayName("有效 ID 應刪除客戶成功")
        void delete_WithValidId_ShouldDeleteCustomer() throws Exception {
            // Arrange
            String token = generateAdminToken();

            // Act & Assert
            mockMvc.perform(delete(CUSTOMERS_API + "/" + testCustomer.getId())
                            .header("Authorization", bearerToken(token)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        /**
         * 測試刪除不存在的客戶
         */
        @Test
        @DisplayName("刪除不存在的客戶應返回 404")
        void delete_WithInvalidId_ShouldReturnNotFound() throws Exception {
            // Arrange
            String token = generateAdminToken();

            // Act & Assert
            mockMvc.perform(delete(CUSTOMERS_API + "/99999")
                            .header("Authorization", bearerToken(token)))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/customers/search - 搜尋會員")
    class SearchCustomersTests {

        @Test
        @DisplayName("關鍵字搜尋應返回匹配結果")
        void search_WithKeyword_ShouldReturnMatchingResults() throws Exception {
            String token = generateAdminToken();

            mockMvc.perform(get(CUSTOMERS_API + "/search")
                            .header("Authorization", bearerToken(token))
                            .param("keyword", "測試"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isArray());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/customers/filter - 複合條件查詢")
    class FilterCustomersTests {

        @Test
        @DisplayName("複合條件查詢應返回結果")
        void filter_WithConditions_ShouldReturnResults() throws Exception {
            String token = generateAdminToken();

            mockMvc.perform(get(CUSTOMERS_API + "/filter")
                            .header("Authorization", bearerToken(token))
                            .param("active", "true"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isArray());
        }

        @Test
        @DisplayName("按等級篩選應返回結果")
        void filter_ByLevel_ShouldReturnResults() throws Exception {
            String token = generateAdminToken();

            mockMvc.perform(get(CUSTOMERS_API + "/filter")
                            .header("Authorization", bearerToken(token))
                            .param("levelId", testLevel.getId().toString()))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("按性別篩選應返回結果")
        void filter_ByGender_ShouldReturnResults() throws Exception {
            String token = generateAdminToken();

            mockMvc.perform(get(CUSTOMERS_API + "/filter")
                            .header("Authorization", bearerToken(token))
                            .param("gender", "M"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/customers/member-no/{memberNo} - 根據會員編號查詢")
    class GetByMemberNoTests {

        @Test
        @DisplayName("有效會員編號應返回會員資訊")
        void getByMemberNo_WithValidNo_ShouldReturnCustomer() throws Exception {
            String token = generateAdminToken();

            mockMvc.perform(get(CUSTOMERS_API + "/member-no/" + testCustomer.getMemberNo())
                            .header("Authorization", bearerToken(token)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.memberNo").value(testCustomer.getMemberNo()));
        }

        @Test
        @DisplayName("無效會員編號應返回 404")
        void getByMemberNo_WithInvalidNo_ShouldReturnNotFound() throws Exception {
            String token = generateAdminToken();

            mockMvc.perform(get(CUSTOMERS_API + "/member-no/INVALID-NO")
                            .header("Authorization", bearerToken(token)))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/customers/{id}/points - 點數操作")
    class PointsOperationTests {

        @Test
        @DisplayName("增加點數應成功")
        void addPoints_ShouldSucceed() throws Exception {
            String token = generateAdminToken();

            mockMvc.perform(post(CUSTOMERS_API + "/" + testCustomer.getId() + "/points/add")
                            .header("Authorization", bearerToken(token))
                            .param("points", "50")
                            .param("reason", "測試增加點數"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("扣除點數應成功")
        void deductPoints_ShouldSucceed() throws Exception {
            String token = generateAdminToken();

            mockMvc.perform(post(CUSTOMERS_API + "/" + testCustomer.getId() + "/points/deduct")
                            .header("Authorization", bearerToken(token))
                            .param("points", "10")
                            .param("reason", "測試扣除點數"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("計算可獲得點數應返回數值")
        void calculatePoints_ShouldReturnPoints() throws Exception {
            String token = generateAdminToken();

            mockMvc.perform(get(CUSTOMERS_API + "/" + testCustomer.getId() + "/points/calculate")
                            .header("Authorization", bearerToken(token))
                            .param("spentAmount", "1000.00"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isNumber());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/customers/{id}/spending - 消費記錄")
    class SpendingTests {

        @Test
        @DisplayName("記錄消費應成功")
        void recordSpending_ShouldSucceed() throws Exception {
            String token = generateAdminToken();

            mockMvc.perform(post(CUSTOMERS_API + "/" + testCustomer.getId() + "/spending")
                            .header("Authorization", bearerToken(token))
                            .param("spentAmount", "500.00"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/customers/birthday - 壽星查詢")
    class BirthdayTests {

        @Test
        @DisplayName("查詢今日壽星應返回列表")
        void getTodayBirthday_ShouldReturnList() throws Exception {
            String token = generateAdminToken();

            mockMvc.perform(get(CUSTOMERS_API + "/birthday/today")
                            .header("Authorization", bearerToken(token)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray());
        }

        @Test
        @DisplayName("查詢本月壽星應返回列表")
        void getThisMonthBirthday_ShouldReturnList() throws Exception {
            String token = generateAdminToken();

            mockMvc.perform(get(CUSTOMERS_API + "/birthday/month")
                            .header("Authorization", bearerToken(token)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/customers/statistics - 會員統計")
    class StatisticsTests {

        @Test
        @DisplayName("應返回會員統計資訊")
        void getStatistics_ShouldReturnStats() throws Exception {
            String token = generateAdminToken();

            mockMvc.perform(get(CUSTOMERS_API + "/statistics")
                            .header("Authorization", bearerToken(token)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.totalCustomers").isNumber())
                    .andExpect(jsonPath("$.data.activeCustomers").isNumber());
        }
    }
}
