package com.kamesan.erpapi.promotions.controller;

import com.kamesan.erpapi.common.dto.ApiResponse;
import com.kamesan.erpapi.promotions.dto.CreatePromotionRequest;
import com.kamesan.erpapi.promotions.dto.PromotionDto;
import com.kamesan.erpapi.promotions.entity.PromotionType;
import com.kamesan.erpapi.promotions.service.PromotionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 促銷活動控制器
 *
 * <p>處理促銷活動相關的 API 請求，包括：</p>
 * <ul>
 *   <li>GET /api/v1/promotions - 取得所有促銷活動</li>
 *   <li>GET /api/v1/promotions/{id} - 取得單一促銷活動</li>
 *   <li>POST /api/v1/promotions - 建立促銷活動</li>
 *   <li>PUT /api/v1/promotions/{id} - 更新促銷活動</li>
 *   <li>DELETE /api/v1/promotions/{id} - 刪除促銷活動</li>
 *   <li>PUT /api/v1/promotions/{id}/activate - 啟用促銷活動</li>
 *   <li>PUT /api/v1/promotions/{id}/deactivate - 停用促銷活動</li>
 *   <li>GET /api/v1/promotions/ongoing - 取得進行中的促銷活動</li>
 *   <li>GET /api/v1/promotions/upcoming - 取得即將開始的促銷活動</li>
 *   <li>GET /api/v1/promotions/expired - 取得已結束的促銷活動</li>
 *   <li>GET /api/v1/promotions/search - 搜尋促銷活動</li>
 *   <li>GET /api/v1/promotions/{id}/calculate - 計算折扣金額</li>
 * </ul>
 *
 * @author ERP System Team
 * @version 1.0.0
 * @see PromotionService 促銷活動服務
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/promotions")
@RequiredArgsConstructor
@Tag(name = "促銷活動管理", description = "促銷活動的 CRUD 和查詢操作")
public class PromotionController {

    /**
     * 促銷活動服務
     */
    private final PromotionService promotionService;

    /**
     * 取得所有促銷活動
     *
     * @param pageable 分頁參數
     * @return 促銷活動分頁
     */
    @GetMapping
    @Operation(summary = "取得所有促銷活動", description = "分頁取得所有促銷活動列表")
    public ApiResponse<Page<PromotionDto>> getAllPromotions(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        log.debug("取得所有促銷活動，分頁參數：{}", pageable);
        Page<PromotionDto> promotions = promotionService.getAllPromotions(pageable);

        return ApiResponse.success(promotions);
    }

    /**
     * 根據 ID 取得促銷活動
     *
     * @param id 促銷活動 ID
     * @return 促銷活動
     */
    @GetMapping("/{id}")
    @Operation(summary = "取得單一促銷活動", description = "根據 ID 取得促銷活動詳細資訊")
    public ApiResponse<PromotionDto> getPromotionById(
            @Parameter(description = "促銷活動 ID", required = true)
            @PathVariable Long id) {

        log.debug("取得促銷活動，ID：{}", id);
        PromotionDto promotion = promotionService.getPromotionById(id);

        return ApiResponse.success(promotion);
    }

    /**
     * 建立促銷活動
     *
     * @param request 建立促銷活動請求
     * @return 建立的促銷活動
     */
    @PostMapping
    @Operation(summary = "建立促銷活動", description = "建立新的促銷活動")
    public ApiResponse<PromotionDto> createPromotion(
            @Valid @RequestBody CreatePromotionRequest request) {

        log.info("建立促銷活動：{}", request.getName());
        PromotionDto promotion = promotionService.createPromotion(request);

        return ApiResponse.success("促銷活動建立成功", promotion);
    }

    /**
     * 更新促銷活動
     *
     * @param id      促銷活動 ID
     * @param request 更新促銷活動請求
     * @return 更新後的促銷活動
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新促銷活動", description = "更新現有的促銷活動")
    public ApiResponse<PromotionDto> updatePromotion(
            @Parameter(description = "促銷活動 ID", required = true)
            @PathVariable Long id,
            @Valid @RequestBody CreatePromotionRequest request) {

        log.info("更新促銷活動，ID：{}", id);
        PromotionDto promotion = promotionService.updatePromotion(id, request);

        return ApiResponse.success("促銷活動更新成功", promotion);
    }

    /**
     * 刪除促銷活動
     *
     * @param id 促銷活動 ID
     * @return 刪除結果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "刪除促銷活動", description = "刪除指定的促銷活動")
    public ApiResponse<Void> deletePromotion(
            @Parameter(description = "促銷活動 ID", required = true)
            @PathVariable Long id) {

        log.info("刪除促銷活動，ID：{}", id);
        promotionService.deletePromotion(id);

        return ApiResponse.success("促銷活動刪除成功", null);
    }

    /**
     * 啟用促銷活動
     *
     * @param id 促銷活動 ID
     * @return 更新後的促銷活動
     */
    @PutMapping("/{id}/activate")
    @Operation(summary = "啟用促銷活動", description = "啟用指定的促銷活動")
    public ApiResponse<PromotionDto> activatePromotion(
            @Parameter(description = "促銷活動 ID", required = true)
            @PathVariable Long id) {

        log.info("啟用促銷活動，ID：{}", id);
        PromotionDto promotion = promotionService.activatePromotion(id);

        return ApiResponse.success("促銷活動已啟用", promotion);
    }

    /**
     * 停用促銷活動
     *
     * @param id 促銷活動 ID
     * @return 更新後的促銷活動
     */
    @PutMapping("/{id}/deactivate")
    @Operation(summary = "停用促銷活動", description = "停用指定的促銷活動")
    public ApiResponse<PromotionDto> deactivatePromotion(
            @Parameter(description = "促銷活動 ID", required = true)
            @PathVariable Long id) {

        log.info("停用促銷活動，ID：{}", id);
        PromotionDto promotion = promotionService.deactivatePromotion(id);

        return ApiResponse.success("促銷活動已停用", promotion);
    }

    /**
     * 根據促銷類型取得促銷活動
     *
     * @param type     促銷類型
     * @param pageable 分頁參數
     * @return 促銷活動分頁
     */
    @GetMapping("/type/{type}")
    @Operation(summary = "根據類型取得促銷活動", description = "根據促銷類型取得促銷活動列表")
    public ApiResponse<Page<PromotionDto>> getPromotionsByType(
            @Parameter(description = "促銷類型（DISCOUNT、BUY_X_GET_Y、BUNDLE）", required = true)
            @PathVariable PromotionType type,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        log.debug("根據類型取得促銷活動，類型：{}", type);
        Page<PromotionDto> promotions = promotionService.getPromotionsByType(type, pageable);

        return ApiResponse.success(promotions);
    }

    /**
     * 取得正在進行中的促銷活動
     *
     * @param pageable 分頁參數
     * @return 促銷活動分頁
     */
    @GetMapping("/ongoing")
    @Operation(summary = "取得進行中的促銷活動", description = "取得目前正在進行中的促銷活動列表")
    public ApiResponse<Page<PromotionDto>> getOngoingPromotions(
            @PageableDefault(size = 10, sort = "startDate", direction = Sort.Direction.DESC)
            Pageable pageable) {

        log.debug("取得進行中的促銷活動");
        Page<PromotionDto> promotions = promotionService.getOngoingPromotions(pageable);

        return ApiResponse.success(promotions);
    }

    /**
     * 取得正在進行中的促銷活動（列表）
     *
     * @return 促銷活動列表
     */
    @GetMapping("/ongoing/list")
    @Operation(summary = "取得進行中的促銷活動列表", description = "取得目前正在進行中的所有促銷活動（不分頁）")
    public ApiResponse<List<PromotionDto>> getOngoingPromotionsList() {

        log.debug("取得進行中的促銷活動列表");
        List<PromotionDto> promotions = promotionService.getOngoingPromotionsList();

        return ApiResponse.success(promotions);
    }

    /**
     * 取得即將開始的促銷活動
     *
     * @param pageable 分頁參數
     * @return 促銷活動分頁
     */
    @GetMapping("/upcoming")
    @Operation(summary = "取得即將開始的促銷活動", description = "取得尚未開始的促銷活動列表")
    public ApiResponse<Page<PromotionDto>> getUpcomingPromotions(
            @PageableDefault(size = 10, sort = "startDate", direction = Sort.Direction.ASC)
            Pageable pageable) {

        log.debug("取得即將開始的促銷活動");
        Page<PromotionDto> promotions = promotionService.getUpcomingPromotions(pageable);

        return ApiResponse.success(promotions);
    }

    /**
     * 取得已結束的促銷活動
     *
     * @param pageable 分頁參數
     * @return 促銷活動分頁
     */
    @GetMapping("/expired")
    @Operation(summary = "取得已結束的促銷活動", description = "取得已結束的促銷活動列表")
    public ApiResponse<Page<PromotionDto>> getExpiredPromotions(
            @PageableDefault(size = 10, sort = "endDate", direction = Sort.Direction.DESC)
            Pageable pageable) {

        log.debug("取得已結束的促銷活動");
        Page<PromotionDto> promotions = promotionService.getExpiredPromotions(pageable);

        return ApiResponse.success(promotions);
    }

    /**
     * 搜尋促銷活動
     *
     * @param keyword  關鍵字
     * @param pageable 分頁參數
     * @return 促銷活動分頁
     */
    @GetMapping("/search")
    @Operation(summary = "搜尋促銷活動", description = "根據關鍵字搜尋促銷活動（名稱、描述）")
    public ApiResponse<Page<PromotionDto>> searchPromotions(
            @Parameter(description = "搜尋關鍵字", required = true)
            @RequestParam String keyword,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        log.debug("搜尋促銷活動，關鍵字：{}", keyword);
        Page<PromotionDto> promotions = promotionService.searchPromotions(keyword, pageable);

        return ApiResponse.success(promotions);
    }

    /**
     * 計算促銷折扣金額
     *
     * @param id             促銷活動 ID
     * @param originalAmount 原始金額
     * @return 折扣金額
     */
    @GetMapping("/{id}/calculate")
    @Operation(summary = "計算折扣金額", description = "根據促銷活動計算折扣金額")
    public ApiResponse<BigDecimal> calculateDiscount(
            @Parameter(description = "促銷活動 ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "原始金額", required = true)
            @RequestParam BigDecimal originalAmount) {

        log.debug("計算促銷折扣金額，促銷活動 ID：{}，原始金額：{}", id, originalAmount);
        BigDecimal discount = promotionService.calculateDiscount(id, originalAmount);

        return ApiResponse.success(discount);
    }

    /**
     * 統計正在進行中的促銷活動數量
     *
     * @return 促銷活動數量
     */
    @GetMapping("/count/ongoing")
    @Operation(summary = "統計進行中的促銷活動數量", description = "取得目前正在進行中的促銷活動數量")
    public ApiResponse<Long> countOngoingPromotions() {

        log.debug("統計進行中的促銷活動數量");
        long count = promotionService.countOngoingPromotions();

        return ApiResponse.success(count);
    }
}
