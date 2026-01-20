package com.kamesan.erpapi.promotions.controller;

import com.kamesan.erpapi.BaseIntegrationTest;
import com.kamesan.erpapi.promotions.dto.CreatePromotionRequest;
import com.kamesan.erpapi.promotions.entity.DiscountType;
import com.kamesan.erpapi.promotions.entity.Promotion;
import com.kamesan.erpapi.promotions.entity.PromotionType;
import com.kamesan.erpapi.promotions.repository.PromotionRepository;
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
 * 促銷活動控制器測試類別
 *
 * <p>測試促銷相關的 API 端點 CRUD 操作：</p>
 * <ul>
 *   <li>POST /api/v1/promotions - 建立促銷活動</li>
 *   <li>GET /api/v1/promotions - 查詢促銷列表</li>
 *   <li>GET /api/v1/promotions/{id} - 查詢促銷詳情</li>
 *   <li>PUT /api/v1/promotions/{id} - 更新促銷</li>
 *   <li>DELETE /api/v1/promotions/{id} - 刪除促銷</li>
 * </ul>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@DisplayName("促銷活動控制器測試")
class PromotionControllerTest extends BaseIntegrationTest {

    /**
     * API 基礎路徑
     */
    private static final String PROMOTIONS_API = "/api/v1/promotions";

    @Autowired
    private PromotionRepository promotionRepository;

    /**
     * 測試用促銷活動
     */
    private Promotion testPromotion;

    /**
     * 在每個測試前初始化測試資料
     */
    @BeforeEach
    void setUpPromotionTestData() {
        // 初始化測試促銷活動
        testPromotion = promotionRepository.findByName("測試促銷活動").orElseGet(() -> {
            Promotion promotion = new Promotion();
            promotion.setName("測試促銷活動");
            promotion.setDescription("這是測試促銷活動");
            promotion.setType(PromotionType.DISCOUNT);
            promotion.setDiscountType(DiscountType.PERCENTAGE);
            promotion.setDiscountValue(new BigDecimal("10.00"));
            promotion.setStartDate(LocalDateTime.now().minusDays(1));
            promotion.setEndDate(LocalDateTime.now().plusDays(30));
            promotion.setActive(true);
            return promotionRepository.save(promotion);
        });
    }

    /**
     * 建立促銷活動 API 測試
     */
    @Nested
    @DisplayName("POST /api/v1/promotions - 建立促銷活動")
    class CreatePromotionTests {

        /**
         * 測試建立促銷活動成功
         */
        @Test
        @DisplayName("有效資料應建立促銷成功")
        void create_WithValidData_ShouldCreatePromotion() throws Exception {
            // Arrange
            String token = generateAdminToken();
            CreatePromotionRequest request = CreatePromotionRequest.builder()
                    .name("新促銷活動")
                    .description("新的促銷活動描述")
                    .type(PromotionType.DISCOUNT)
                    .discountType(DiscountType.FIXED_AMOUNT)
                    .discountValue(new BigDecimal("100.00"))
                    .startDate(LocalDateTime.now())
                    .endDate(LocalDateTime.now().plusDays(30))
                    .build();

            // Act & Assert
            mockMvc.perform(post(PROMOTIONS_API)
                            .header("Authorization", bearerToken(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.name").value("新促銷活動"));
        }

        /**
         * 測試缺少必填欄位
         */
        @Test
        @DisplayName("缺少必填欄位應返回驗證錯誤")
        void create_WithMissingRequiredFields_ShouldReturnBadRequest() throws Exception {
            // Arrange
            String token = generateAdminToken();
            CreatePromotionRequest request = CreatePromotionRequest.builder()
                    // name 缺少
                    .type(PromotionType.DISCOUNT)
                    .build();

            // Act & Assert
            mockMvc.perform(post(PROMOTIONS_API)
                            .header("Authorization", bearerToken(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }

    /**
     * 查詢促銷列表 API 測試
     */
    @Nested
    @DisplayName("GET /api/v1/promotions - 查詢促銷列表")
    class GetPromotionsTests {

        /**
         * 測試查詢促銷列表成功
         */
        @Test
        @DisplayName("應返回促銷列表")
        void getAll_ShouldReturnPromotionList() throws Exception {
            // Arrange
            String token = generateAdminToken();

            // Act & Assert
            mockMvc.perform(get(PROMOTIONS_API)
                            .header("Authorization", bearerToken(token)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isArray());
        }
    }

    /**
     * 查詢促銷詳情 API 測試
     */
    @Nested
    @DisplayName("GET /api/v1/promotions/{id} - 查詢促銷詳情")
    class GetPromotionByIdTests {

        /**
         * 測試查詢促銷詳情成功
         */
        @Test
        @DisplayName("有效 ID 應返回促銷詳情")
        void getById_WithValidId_ShouldReturnPromotion() throws Exception {
            // Arrange
            String token = generateAdminToken();

            // Act & Assert
            mockMvc.perform(get(PROMOTIONS_API + "/" + testPromotion.getId())
                            .header("Authorization", bearerToken(token)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(testPromotion.getId()))
                    .andExpect(jsonPath("$.data.name").value("測試促銷活動"));
        }

        /**
         * 測試查詢不存在的促銷
         */
        @Test
        @DisplayName("不存在的 ID 應返回 404")
        void getById_WithInvalidId_ShouldReturnNotFound() throws Exception {
            // Arrange
            String token = generateAdminToken();

            // Act & Assert
            mockMvc.perform(get(PROMOTIONS_API + "/99999")
                            .header("Authorization", bearerToken(token)))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }

    /**
     * 刪除促銷 API 測試
     */
    @Nested
    @DisplayName("DELETE /api/v1/promotions/{id} - 刪除促銷")
    class DeletePromotionTests {

        /**
         * 測試刪除促銷成功
         */
        @Test
        @DisplayName("有效 ID 應刪除促銷成功")
        void delete_WithValidId_ShouldDeletePromotion() throws Exception {
            // Arrange
            String token = generateAdminToken();

            // Act & Assert
            mockMvc.perform(delete(PROMOTIONS_API + "/" + testPromotion.getId())
                            .header("Authorization", bearerToken(token)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }
}
