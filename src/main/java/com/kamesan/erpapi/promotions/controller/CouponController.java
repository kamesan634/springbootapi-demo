package com.kamesan.erpapi.promotions.controller;

import com.kamesan.erpapi.common.dto.ApiResponse;
import com.kamesan.erpapi.promotions.dto.CouponDto;
import com.kamesan.erpapi.promotions.dto.CreateCouponRequest;
import com.kamesan.erpapi.promotions.service.CouponService;
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
 * 優惠券控制器
 *
 * <p>處理優惠券相關的 API 請求，包括：</p>
 * <ul>
 *   <li>GET /api/v1/coupons - 取得所有優惠券</li>
 *   <li>GET /api/v1/coupons/{id} - 取得單一優惠券</li>
 *   <li>GET /api/v1/coupons/code/{code} - 根據優惠碼取得優惠券</li>
 *   <li>POST /api/v1/coupons - 建立優惠券</li>
 *   <li>PUT /api/v1/coupons/{id} - 更新優惠券</li>
 *   <li>DELETE /api/v1/coupons/{id} - 刪除優惠券</li>
 *   <li>PUT /api/v1/coupons/{id}/activate - 啟用優惠券</li>
 *   <li>PUT /api/v1/coupons/{id}/deactivate - 停用優惠券</li>
 *   <li>GET /api/v1/coupons/valid - 取得有效的優惠券</li>
 *   <li>GET /api/v1/coupons/upcoming - 取得即將生效的優惠券</li>
 *   <li>GET /api/v1/coupons/expired - 取得已過期的優惠券</li>
 *   <li>GET /api/v1/coupons/search - 搜尋優惠券</li>
 *   <li>POST /api/v1/coupons/validate - 驗證優惠券</li>
 *   <li>POST /api/v1/coupons/use - 使用優惠券</li>
 *   <li>GET /api/v1/coupons/calculate - 計算折扣金額</li>
 * </ul>
 *
 * @author ERP System Team
 * @version 1.0.0
 * @see CouponService 優惠券服務
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/coupons")
@RequiredArgsConstructor
@Tag(name = "優惠券管理", description = "優惠券的 CRUD、驗證和使用操作")
public class CouponController {

    /**
     * 優惠券服務
     */
    private final CouponService couponService;

    /**
     * 取得所有優惠券
     *
     * @param pageable 分頁參數
     * @return 優惠券分頁
     */
    @GetMapping
    @Operation(summary = "取得所有優惠券", description = "分頁取得所有優惠券列表")
    public ApiResponse<Page<CouponDto>> getAllCoupons(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        log.debug("取得所有優惠券，分頁參數：{}", pageable);
        Page<CouponDto> coupons = couponService.getAllCoupons(pageable);

        return ApiResponse.success(coupons);
    }

    /**
     * 根據 ID 取得優惠券
     *
     * @param id 優惠券 ID
     * @return 優惠券
     */
    @GetMapping("/{id}")
    @Operation(summary = "取得單一優惠券", description = "根據 ID 取得優惠券詳細資訊")
    public ApiResponse<CouponDto> getCouponById(
            @Parameter(description = "優惠券 ID", required = true)
            @PathVariable Long id) {

        log.debug("取得優惠券，ID：{}", id);
        CouponDto coupon = couponService.getCouponById(id);

        return ApiResponse.success(coupon);
    }

    /**
     * 根據優惠碼取得優惠券
     *
     * @param code 優惠碼
     * @return 優惠券
     */
    @GetMapping("/code/{code}")
    @Operation(summary = "根據優惠碼取得優惠券", description = "根據優惠碼取得優惠券詳細資訊")
    public ApiResponse<CouponDto> getCouponByCode(
            @Parameter(description = "優惠碼", required = true)
            @PathVariable String code) {

        log.debug("根據優惠碼取得優惠券，優惠碼：{}", code);
        CouponDto coupon = couponService.getCouponByCode(code);

        return ApiResponse.success(coupon);
    }

    /**
     * 建立優惠券
     *
     * @param request 建立優惠券請求
     * @return 建立的優惠券
     */
    @PostMapping
    @Operation(summary = "建立優惠券", description = "建立新的優惠券")
    public ApiResponse<CouponDto> createCoupon(
            @Valid @RequestBody CreateCouponRequest request) {

        log.info("建立優惠券：{}", request.getCode());
        CouponDto coupon = couponService.createCoupon(request);

        return ApiResponse.success("優惠券建立成功", coupon);
    }

    /**
     * 更新優惠券
     *
     * @param id      優惠券 ID
     * @param request 更新優惠券請求
     * @return 更新後的優惠券
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新優惠券", description = "更新現有的優惠券")
    public ApiResponse<CouponDto> updateCoupon(
            @Parameter(description = "優惠券 ID", required = true)
            @PathVariable Long id,
            @Valid @RequestBody CreateCouponRequest request) {

        log.info("更新優惠券，ID：{}", id);
        CouponDto coupon = couponService.updateCoupon(id, request);

        return ApiResponse.success("優惠券更新成功", coupon);
    }

    /**
     * 刪除優惠券
     *
     * @param id 優惠券 ID
     * @return 刪除結果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "刪除優惠券", description = "刪除指定的優惠券")
    public ApiResponse<Void> deleteCoupon(
            @Parameter(description = "優惠券 ID", required = true)
            @PathVariable Long id) {

        log.info("刪除優惠券，ID：{}", id);
        couponService.deleteCoupon(id);

        return ApiResponse.success("優惠券刪除成功", null);
    }

    /**
     * 啟用優惠券
     *
     * @param id 優惠券 ID
     * @return 更新後的優惠券
     */
    @PutMapping("/{id}/activate")
    @Operation(summary = "啟用優惠券", description = "啟用指定的優惠券")
    public ApiResponse<CouponDto> activateCoupon(
            @Parameter(description = "優惠券 ID", required = true)
            @PathVariable Long id) {

        log.info("啟用優惠券，ID：{}", id);
        CouponDto coupon = couponService.activateCoupon(id);

        return ApiResponse.success("優惠券已啟用", coupon);
    }

    /**
     * 停用優惠券
     *
     * @param id 優惠券 ID
     * @return 更新後的優惠券
     */
    @PutMapping("/{id}/deactivate")
    @Operation(summary = "停用優惠券", description = "停用指定的優惠券")
    public ApiResponse<CouponDto> deactivateCoupon(
            @Parameter(description = "優惠券 ID", required = true)
            @PathVariable Long id) {

        log.info("停用優惠券，ID：{}", id);
        CouponDto coupon = couponService.deactivateCoupon(id);

        return ApiResponse.success("優惠券已停用", coupon);
    }

    /**
     * 取得有效的優惠券
     *
     * @param pageable 分頁參數
     * @return 優惠券分頁
     */
    @GetMapping("/valid")
    @Operation(summary = "取得有效的優惠券", description = "取得目前有效的優惠券列表（已啟用、在有效期內、未用完）")
    public ApiResponse<Page<CouponDto>> getValidCoupons(
            @PageableDefault(size = 10, sort = "startDate", direction = Sort.Direction.DESC)
            Pageable pageable) {

        log.debug("取得有效的優惠券");
        Page<CouponDto> coupons = couponService.getValidCoupons(pageable);

        return ApiResponse.success(coupons);
    }

    /**
     * 取得有效的優惠券（列表）
     *
     * @return 優惠券列表
     */
    @GetMapping("/valid/list")
    @Operation(summary = "取得有效的優惠券列表", description = "取得目前有效的所有優惠券（不分頁）")
    public ApiResponse<List<CouponDto>> getValidCouponsList() {

        log.debug("取得有效的優惠券列表");
        List<CouponDto> coupons = couponService.getValidCouponsList();

        return ApiResponse.success(coupons);
    }

    /**
     * 取得即將生效的優惠券
     *
     * @param pageable 分頁參數
     * @return 優惠券分頁
     */
    @GetMapping("/upcoming")
    @Operation(summary = "取得即將生效的優惠券", description = "取得尚未生效的優惠券列表")
    public ApiResponse<Page<CouponDto>> getUpcomingCoupons(
            @PageableDefault(size = 10, sort = "startDate", direction = Sort.Direction.ASC)
            Pageable pageable) {

        log.debug("取得即將生效的優惠券");
        Page<CouponDto> coupons = couponService.getUpcomingCoupons(pageable);

        return ApiResponse.success(coupons);
    }

    /**
     * 取得已過期的優惠券
     *
     * @param pageable 分頁參數
     * @return 優惠券分頁
     */
    @GetMapping("/expired")
    @Operation(summary = "取得已過期的優惠券", description = "取得已過期的優惠券列表")
    public ApiResponse<Page<CouponDto>> getExpiredCoupons(
            @PageableDefault(size = 10, sort = "endDate", direction = Sort.Direction.DESC)
            Pageable pageable) {

        log.debug("取得已過期的優惠券");
        Page<CouponDto> coupons = couponService.getExpiredCoupons(pageable);

        return ApiResponse.success(coupons);
    }

    /**
     * 搜尋優惠券
     *
     * @param keyword  關鍵字
     * @param pageable 分頁參數
     * @return 優惠券分頁
     */
    @GetMapping("/search")
    @Operation(summary = "搜尋優惠券", description = "根據關鍵字搜尋優惠券（優惠碼、名稱）")
    public ApiResponse<Page<CouponDto>> searchCoupons(
            @Parameter(description = "搜尋關鍵字", required = true)
            @RequestParam String keyword,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        log.debug("搜尋優惠券，關鍵字：{}", keyword);
        Page<CouponDto> coupons = couponService.searchCoupons(keyword, pageable);

        return ApiResponse.success(coupons);
    }

    /**
     * 驗證優惠券
     *
     * <p>驗證優惠券是否可用於指定的訂單金額。</p>
     *
     * @param code        優惠碼
     * @param orderAmount 訂單金額
     * @return 優惠券資訊（若驗證通過）
     */
    @PostMapping("/validate")
    @Operation(summary = "驗證優惠券", description = "驗證優惠券是否可用於指定的訂單金額")
    public ApiResponse<CouponDto> validateCoupon(
            @Parameter(description = "優惠碼", required = true)
            @RequestParam String code,
            @Parameter(description = "訂單金額", required = true)
            @RequestParam BigDecimal orderAmount) {

        log.debug("驗證優惠券，優惠碼：{}，訂單金額：{}", code, orderAmount);
        CouponDto coupon = couponService.validateCoupon(code, orderAmount);

        return ApiResponse.success("優惠券驗證通過", coupon);
    }

    /**
     * 使用優惠券
     *
     * <p>使用優惠券並返回折扣金額。使用後會增加優惠券的使用次數。</p>
     *
     * @param code        優惠碼
     * @param orderAmount 訂單金額
     * @return 折扣金額
     */
    @PostMapping("/use")
    @Operation(summary = "使用優惠券", description = "使用優惠券並返回折扣金額（會增加使用次數）")
    public ApiResponse<BigDecimal> useCoupon(
            @Parameter(description = "優惠碼", required = true)
            @RequestParam String code,
            @Parameter(description = "訂單金額", required = true)
            @RequestParam BigDecimal orderAmount) {

        log.info("使用優惠券，優惠碼：{}，訂單金額：{}", code, orderAmount);
        BigDecimal discount = couponService.useCoupon(code, orderAmount);

        return ApiResponse.success("優惠券使用成功", discount);
    }

    /**
     * 計算優惠券折扣金額
     *
     * <p>僅計算折扣金額，不實際使用優惠券。用於預覽折扣。</p>
     *
     * @param code        優惠碼
     * @param orderAmount 訂單金額
     * @return 折扣金額
     */
    @GetMapping("/calculate")
    @Operation(summary = "計算折扣金額", description = "計算優惠券的折扣金額（不實際使用優惠券）")
    public ApiResponse<BigDecimal> calculateDiscount(
            @Parameter(description = "優惠碼", required = true)
            @RequestParam String code,
            @Parameter(description = "訂單金額", required = true)
            @RequestParam BigDecimal orderAmount) {

        log.debug("計算優惠券折扣金額，優惠碼：{}，訂單金額：{}", code, orderAmount);
        BigDecimal discount = couponService.calculateDiscount(code, orderAmount);

        return ApiResponse.success(discount);
    }

    /**
     * 統計有效的優惠券數量
     *
     * @return 優惠券數量
     */
    @GetMapping("/count/valid")
    @Operation(summary = "統計有效的優惠券數量", description = "取得目前有效的優惠券數量")
    public ApiResponse<Long> countValidCoupons() {

        log.debug("統計有效的優惠券數量");
        long count = couponService.countValidCoupons();

        return ApiResponse.success(count);
    }
}
