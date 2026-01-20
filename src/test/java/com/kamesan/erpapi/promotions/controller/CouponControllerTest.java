package com.kamesan.erpapi.promotions.controller;

import com.kamesan.erpapi.BaseIntegrationTest;
import com.kamesan.erpapi.promotions.dto.CreateCouponRequest;
import com.kamesan.erpapi.promotions.entity.Coupon;
import com.kamesan.erpapi.promotions.entity.DiscountType;
import com.kamesan.erpapi.promotions.repository.CouponRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 優惠券控制器測試類別
 *
 * <p>測試優惠券相關的 API 端點 CRUD 操作：</p>
 * <ul>
 *   <li>POST /api/v1/coupons - 建立優惠券</li>
 *   <li>GET /api/v1/coupons - 查詢優惠券列表</li>
 *   <li>GET /api/v1/coupons/{id} - 查詢優惠券詳情</li>
 *   <li>DELETE /api/v1/coupons/{id} - 刪除優惠券</li>
 * </ul>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@DisplayName("優惠券控制器測試")
class CouponControllerTest extends BaseIntegrationTest {

    /**
     * API 基礎路徑
     */
    private static final String COUPONS_API = "/api/v1/coupons";

    @Autowired
    private CouponRepository couponRepository;

    /**
     * 測試用優惠券
     */
    private Coupon testCoupon;

    /**
     * 在每個測試前初始化測試資料
     */
    @BeforeEach
    void setUpCouponTestData() {
        // 初始化測試優惠券
        testCoupon = couponRepository.findByCode("COUPON_TEST-001").orElseGet(() -> {
            Coupon coupon = new Coupon();
            coupon.setCode("COUPON_TEST-001");
            coupon.setName("測試優惠券");
            coupon.setDiscountType(DiscountType.FIXED_AMOUNT);
            coupon.setDiscountValue(new BigDecimal("100.00"));
            coupon.setMinOrderAmount(new BigDecimal("500.00"));
            coupon.setStartDate(LocalDateTime.now().minusDays(1));
            coupon.setEndDate(LocalDateTime.now().plusDays(30));
            coupon.setMaxUses(100);
            coupon.setUsedCount(0);
            coupon.setActive(true);
            return couponRepository.save(coupon);
        });
    }

    /**
     * 建立優惠券 API 測試
     */
    @Nested
    @DisplayName("POST /api/v1/coupons - 建立優惠券")
    class CreateCouponTests {

        /**
         * 測試建立優惠券成功
         */
        @Test
        @DisplayName("有效資料應建立優惠券成功")
        void create_WithValidData_ShouldCreateCoupon() throws Exception {
            // Arrange
            String token = generateAdminToken();
            CreateCouponRequest request = CreateCouponRequest.builder()
                    .code("COUPON_NEW_001")
                    .name("新優惠券")
                    .discountType(DiscountType.PERCENTAGE)
                    .discountValue(new BigDecimal("15.00"))
                    .minOrderAmount(new BigDecimal("800.00"))
                    .startDate(LocalDateTime.now())
                    .endDate(LocalDateTime.now().plusDays(60))
                    .maxUses(500)
                    .build();

            // Act & Assert
            mockMvc.perform(post(COUPONS_API)
                            .header("Authorization", bearerToken(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.code").value("COUPON_NEW_001"))
                    .andExpect(jsonPath("$.data.name").value("新優惠券"));
        }

        /**
         * 測試缺少必填欄位
         */
        @Test
        @DisplayName("缺少必填欄位應返回驗證錯誤")
        void create_WithMissingRequiredFields_ShouldReturnBadRequest() throws Exception {
            // Arrange
            String token = generateAdminToken();
            CreateCouponRequest request = CreateCouponRequest.builder()
                    .code("COUPON_NEW-002")
                    // name 缺少
                    .build();

            // Act & Assert
            mockMvc.perform(post(COUPONS_API)
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
            CreateCouponRequest request = CreateCouponRequest.builder()
                    .code("COUPON_TEST-001") // 已存在的代碼
                    .name("重複優惠券")
                    .discountType(DiscountType.FIXED_AMOUNT)
                    .discountValue(new BigDecimal("50.00"))
                    .startDate(LocalDateTime.now())
                    .endDate(LocalDateTime.now().plusDays(30))
                    .build();

            // Act & Assert
            mockMvc.perform(post(COUPONS_API)
                            .header("Authorization", bearerToken(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }

    /**
     * 查詢優惠券列表 API 測試
     */
    @Nested
    @DisplayName("GET /api/v1/coupons - 查詢優惠券列表")
    class GetCouponsTests {

        /**
         * 測試查詢優惠券列表成功
         */
        @Test
        @DisplayName("應返回優惠券列表")
        void getAll_ShouldReturnCouponList() throws Exception {
            // Arrange
            String token = generateAdminToken();

            // Act & Assert
            mockMvc.perform(get(COUPONS_API)
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
            mockMvc.perform(get(COUPONS_API)
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
     * 查詢優惠券詳情 API 測試
     */
    @Nested
    @DisplayName("GET /api/v1/coupons/{id} - 查詢優惠券詳情")
    class GetCouponByIdTests {

        /**
         * 測試查詢優惠券詳情成功
         */
        @Test
        @DisplayName("有效 ID 應返回優惠券詳情")
        void getById_WithValidId_ShouldReturnCoupon() throws Exception {
            // Arrange
            String token = generateAdminToken();

            // Act & Assert
            mockMvc.perform(get(COUPONS_API + "/" + testCoupon.getId())
                            .header("Authorization", bearerToken(token)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(testCoupon.getId()))
                    .andExpect(jsonPath("$.data.code").value("COUPON_TEST-001"));
        }

        /**
         * 測試查詢不存在的優惠券
         */
        @Test
        @DisplayName("不存在的 ID 應返回 404")
        void getById_WithInvalidId_ShouldReturnNotFound() throws Exception {
            // Arrange
            String token = generateAdminToken();

            // Act & Assert
            mockMvc.perform(get(COUPONS_API + "/99999")
                            .header("Authorization", bearerToken(token)))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }

        /**
         * 測試按代碼查詢優惠券
         */
        @Test
        @DisplayName("按代碼查詢應返回優惠券")
        void getByCode_ShouldReturnCoupon() throws Exception {
            // Arrange
            String token = generateAdminToken();

            // Act & Assert
            mockMvc.perform(get(COUPONS_API + "/code/COUPON_TEST-001")
                            .header("Authorization", bearerToken(token)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.code").value("COUPON_TEST-001"));
        }
    }

    /**
     * 刪除優惠券 API 測試
     */
    @Nested
    @DisplayName("DELETE /api/v1/coupons/{id} - 刪除優惠券")
    class DeleteCouponTests {

        /**
         * 測試刪除優惠券成功
         */
        @Test
        @DisplayName("有效 ID 應刪除優惠券成功")
        void delete_WithValidId_ShouldDeleteCoupon() throws Exception {
            // Arrange
            String token = generateAdminToken();

            // Act & Assert
            mockMvc.perform(delete(COUPONS_API + "/" + testCoupon.getId())
                            .header("Authorization", bearerToken(token)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        /**
         * 測試刪除不存在的優惠券
         */
        @Test
        @DisplayName("刪除不存在的優惠券應返回 404")
        void delete_WithInvalidId_ShouldReturnNotFound() throws Exception {
            // Arrange
            String token = generateAdminToken();

            // Act & Assert
            mockMvc.perform(delete(COUPONS_API + "/99999")
                            .header("Authorization", bearerToken(token)))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/coupons/{id} - 更新優惠券")
    class UpdateCouponTests {

        @Test
        @DisplayName("更新不存在的優惠券應返回 404")
        void update_WithInvalidId_ShouldReturnNotFound() throws Exception {
            String token = generateAdminToken();
            CreateCouponRequest request = CreateCouponRequest.builder()
                    .code("COUPON_INVALID")
                    .name("更新優惠券")
                    .discountType(DiscountType.PERCENTAGE)
                    .discountValue(new BigDecimal("20.00"))
                    .startDate(LocalDateTime.now())
                    .endDate(LocalDateTime.now().plusDays(60))
                    .build();

            mockMvc.perform(put(COUPONS_API + "/99999")
                            .header("Authorization", bearerToken(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/coupons/{id}/activate - 啟停用優惠券")
    class ActivateDeactivateTests {

        @Test
        @DisplayName("應啟用優惠券成功")
        void activate_ShouldSucceed() throws Exception {
            String token = generateAdminToken();

            mockMvc.perform(put(COUPONS_API + "/" + testCoupon.getId() + "/activate")
                            .header("Authorization", bearerToken(token)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("應停用優惠券成功")
        void deactivate_ShouldSucceed() throws Exception {
            String token = generateAdminToken();

            mockMvc.perform(put(COUPONS_API + "/" + testCoupon.getId() + "/deactivate")
                            .header("Authorization", bearerToken(token)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/coupons/valid - 有效優惠券")
    class ValidCouponsTests {

        @Test
        @DisplayName("應返回有效的優惠券列表")
        void getValid_ShouldReturnCoupons() throws Exception {
            String token = generateAdminToken();

            mockMvc.perform(get(COUPONS_API + "/valid")
                            .header("Authorization", bearerToken(token)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("應返回有效的優惠券列表（不分頁）")
        void getValidList_ShouldReturnCoupons() throws Exception {
            String token = generateAdminToken();

            mockMvc.perform(get(COUPONS_API + "/valid/list")
                            .header("Authorization", bearerToken(token)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/coupons/upcoming - 即將生效優惠券")
    class UpcomingCouponsTests {

        @Test
        @DisplayName("應返回即將生效的優惠券列表")
        void getUpcoming_ShouldReturnCoupons() throws Exception {
            String token = generateAdminToken();

            mockMvc.perform(get(COUPONS_API + "/upcoming")
                            .header("Authorization", bearerToken(token)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/coupons/expired - 已過期優惠券")
    class ExpiredCouponsTests {

        @Test
        @DisplayName("應返回已過期的優惠券列表")
        void getExpired_ShouldReturnCoupons() throws Exception {
            String token = generateAdminToken();

            mockMvc.perform(get(COUPONS_API + "/expired")
                            .header("Authorization", bearerToken(token)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/coupons/search - 搜尋優惠券")
    class SearchCouponsTests {

        @Test
        @DisplayName("關鍵字搜尋應返回相符結果")
        void search_WithKeyword_ShouldReturnMatchingCoupons() throws Exception {
            String token = generateAdminToken();

            mockMvc.perform(get(COUPONS_API + "/search")
                            .header("Authorization", bearerToken(token))
                            .param("keyword", "測試"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/coupons/validate - 驗證優惠券")
    class ValidateCouponTests {

        @Test
        @DisplayName("有效優惠券應驗證通過")
        void validate_WithValidCoupon_ShouldSucceed() throws Exception {
            String token = generateAdminToken();

            mockMvc.perform(post(COUPONS_API + "/validate")
                            .header("Authorization", bearerToken(token))
                            .param("code", testCoupon.getCode())
                            .param("orderAmount", "1000.00"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/coupons/use - 使用優惠券")
    class UseCouponTests {

        @Test
        @DisplayName("使用有效優惠券應成功")
        void use_WithValidCoupon_ShouldSucceed() throws Exception {
            String token = generateAdminToken();

            mockMvc.perform(post(COUPONS_API + "/use")
                            .header("Authorization", bearerToken(token))
                            .param("code", testCoupon.getCode())
                            .param("orderAmount", "1000.00"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isNumber());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/coupons/calculate - 計算折扣")
    class CalculateDiscountTests {

        @Test
        @DisplayName("應計算折扣金額")
        void calculate_ShouldReturnDiscount() throws Exception {
            String token = generateAdminToken();

            mockMvc.perform(get(COUPONS_API + "/calculate")
                            .header("Authorization", bearerToken(token))
                            .param("code", testCoupon.getCode())
                            .param("orderAmount", "1000.00"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isNumber());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/coupons/count/valid - 統計有效優惠券")
    class CountValidTests {

        @Test
        @DisplayName("應返回有效優惠券數量")
        void countValid_ShouldReturnCount() throws Exception {
            String token = generateAdminToken();

            mockMvc.perform(get(COUPONS_API + "/count/valid")
                            .header("Authorization", bearerToken(token)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isNumber());
        }
    }
}
