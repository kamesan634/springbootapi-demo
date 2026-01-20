package com.kamesan.erpapi.promotions.service;

import com.kamesan.erpapi.common.exception.BusinessException;
import com.kamesan.erpapi.promotions.dto.CouponDto;
import com.kamesan.erpapi.promotions.dto.CreateCouponRequest;
import com.kamesan.erpapi.promotions.entity.Coupon;
import com.kamesan.erpapi.promotions.entity.DiscountType;
import com.kamesan.erpapi.promotions.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 優惠券服務
 *
 * <p>處理優惠券相關的業務邏輯，包括：</p>
 * <ul>
 *   <li>優惠券的 CRUD 操作</li>
 *   <li>優惠券的查詢和搜尋</li>
 *   <li>優惠券的狀態管理（啟用/停用）</li>
 *   <li>優惠券的驗證和使用</li>
 *   <li>折扣金額的計算</li>
 * </ul>
 *
 * <p>業務規則：</p>
 * <ul>
 *   <li>優惠碼不可重複（不區分大小寫）</li>
 *   <li>結束日期必須在開始日期之後</li>
 *   <li>百分比折扣值不可超過 100</li>
 *   <li>使用優惠券時需檢查有效性和最低消費金額</li>
 * </ul>
 *
 * @author ERP System Team
 * @version 1.0.0
 * @see Coupon 優惠券實體
 * @see CouponRepository 優惠券 Repository
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CouponService {

    /**
     * 優惠券 Repository
     */
    private final CouponRepository couponRepository;

    /**
     * 取得所有優惠券（分頁）
     *
     * @param pageable 分頁參數
     * @return 優惠券分頁
     */
    @Transactional(readOnly = true)
    public Page<CouponDto> getAllCoupons(Pageable pageable) {
        log.debug("取得所有優惠券，分頁參數：{}", pageable);
        return couponRepository.findAll(pageable)
                .map(CouponDto::fromEntity);
    }

    /**
     * 根據 ID 取得優惠券
     *
     * @param id 優惠券 ID
     * @return 優惠券 DTO
     * @throws BusinessException 若優惠券不存在
     */
    @Transactional(readOnly = true)
    public CouponDto getCouponById(Long id) {
        log.debug("取得優惠券，ID：{}", id);
        Coupon coupon = findCouponById(id);
        return CouponDto.fromEntity(coupon);
    }

    /**
     * 根據優惠碼取得優惠券
     *
     * @param code 優惠碼
     * @return 優惠券 DTO
     * @throws BusinessException 若優惠券不存在
     */
    @Transactional(readOnly = true)
    public CouponDto getCouponByCode(String code) {
        log.debug("根據優惠碼取得優惠券，優惠碼：{}", code);
        Coupon coupon = couponRepository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> BusinessException.notFound("優惠券", "優惠碼 " + code));
        return CouponDto.fromEntity(coupon);
    }

    /**
     * 建立優惠券
     *
     * @param request 建立優惠券請求
     * @return 建立的優惠券 DTO
     * @throws BusinessException 若優惠碼已存在或資料驗證失敗
     */
    @Transactional
    public CouponDto createCoupon(CreateCouponRequest request) {
        log.info("建立優惠券：{}", request.getCode());

        // 驗證資料
        validateCouponRequest(request, null);

        // 檢查優惠碼是否已存在（不區分大小寫）
        if (couponRepository.existsByCodeIgnoreCase(request.getCode())) {
            throw BusinessException.alreadyExists("優惠券", "優惠碼", request.getCode());
        }

        // 建立優惠券
        Coupon coupon = request.toEntity();
        coupon = couponRepository.save(coupon);

        log.info("優惠券建立成功，ID：{}，優惠碼：{}", coupon.getId(), coupon.getCode());
        return CouponDto.fromEntity(coupon);
    }

    /**
     * 更新優惠券
     *
     * @param id      優惠券 ID
     * @param request 更新優惠券請求
     * @return 更新後的優惠券 DTO
     * @throws BusinessException 若優惠券不存在或資料驗證失敗
     */
    @Transactional
    public CouponDto updateCoupon(Long id, CreateCouponRequest request) {
        log.info("更新優惠券，ID：{}", id);

        // 取得現有優惠券
        Coupon coupon = findCouponById(id);

        // 驗證資料
        validateCouponRequest(request, id);

        // 檢查優惠碼是否與其他優惠券重複
        String newCode = request.getCode().toUpperCase();
        if (!coupon.getCode().equalsIgnoreCase(newCode) &&
                couponRepository.existsByCodeIgnoreCase(newCode)) {
            throw BusinessException.alreadyExists("優惠券", "優惠碼", request.getCode());
        }

        // 更新優惠券
        request.updateEntity(coupon);
        coupon = couponRepository.save(coupon);

        log.info("優惠券更新成功，ID：{}，優惠碼：{}", coupon.getId(), coupon.getCode());
        return CouponDto.fromEntity(coupon);
    }

    /**
     * 刪除優惠券
     *
     * @param id 優惠券 ID
     * @throws BusinessException 若優惠券不存在
     */
    @Transactional
    public void deleteCoupon(Long id) {
        log.info("刪除優惠券，ID：{}", id);

        // 確認優惠券存在
        Coupon coupon = findCouponById(id);

        couponRepository.delete(coupon);

        log.info("優惠券刪除成功，ID：{}，優惠碼：{}", id, coupon.getCode());
    }

    /**
     * 啟用優惠券
     *
     * @param id 優惠券 ID
     * @return 更新後的優惠券 DTO
     * @throws BusinessException 若優惠券不存在
     */
    @Transactional
    public CouponDto activateCoupon(Long id) {
        log.info("啟用優惠券，ID：{}", id);

        Coupon coupon = findCouponById(id);
        coupon.setActive(true);
        coupon = couponRepository.save(coupon);

        log.info("優惠券已啟用，ID：{}，優惠碼：{}", id, coupon.getCode());
        return CouponDto.fromEntity(coupon);
    }

    /**
     * 停用優惠券
     *
     * @param id 優惠券 ID
     * @return 更新後的優惠券 DTO
     * @throws BusinessException 若優惠券不存在
     */
    @Transactional
    public CouponDto deactivateCoupon(Long id) {
        log.info("停用優惠券，ID：{}", id);

        Coupon coupon = findCouponById(id);
        coupon.setActive(false);
        coupon = couponRepository.save(coupon);

        log.info("優惠券已停用，ID：{}，優惠碼：{}", id, coupon.getCode());
        return CouponDto.fromEntity(coupon);
    }

    /**
     * 取得有效的優惠券
     *
     * @param pageable 分頁參數
     * @return 優惠券分頁
     */
    @Transactional(readOnly = true)
    public Page<CouponDto> getValidCoupons(Pageable pageable) {
        log.debug("取得有效的優惠券");
        return couponRepository.findValidCoupons(LocalDateTime.now(), pageable)
                .map(CouponDto::fromEntity);
    }

    /**
     * 取得有效的優惠券（列表）
     *
     * @return 優惠券列表
     */
    @Transactional(readOnly = true)
    public List<CouponDto> getValidCouponsList() {
        log.debug("取得有效的優惠券列表");
        return couponRepository.findValidCoupons(LocalDateTime.now())
                .stream()
                .map(CouponDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 取得即將生效的優惠券
     *
     * @param pageable 分頁參數
     * @return 優惠券分頁
     */
    @Transactional(readOnly = true)
    public Page<CouponDto> getUpcomingCoupons(Pageable pageable) {
        log.debug("取得即將生效的優惠券");
        return couponRepository.findUpcomingCoupons(LocalDateTime.now(), pageable)
                .map(CouponDto::fromEntity);
    }

    /**
     * 取得已過期的優惠券
     *
     * @param pageable 分頁參數
     * @return 優惠券分頁
     */
    @Transactional(readOnly = true)
    public Page<CouponDto> getExpiredCoupons(Pageable pageable) {
        log.debug("取得已過期的優惠券");
        return couponRepository.findExpiredCoupons(LocalDateTime.now(), pageable)
                .map(CouponDto::fromEntity);
    }

    /**
     * 搜尋優惠券
     *
     * @param keyword  關鍵字（優惠碼或名稱）
     * @param pageable 分頁參數
     * @return 優惠券分頁
     */
    @Transactional(readOnly = true)
    public Page<CouponDto> searchCoupons(String keyword, Pageable pageable) {
        log.debug("搜尋優惠券，關鍵字：{}", keyword);
        return couponRepository.search(keyword, pageable)
                .map(CouponDto::fromEntity);
    }

    /**
     * 驗證優惠券是否可用
     *
     * <p>驗證條件：</p>
     * <ol>
     *   <li>優惠券存在</li>
     *   <li>優惠券已啟用</li>
     *   <li>目前在有效期間內</li>
     *   <li>尚未達到最大使用次數</li>
     *   <li>訂單金額符合最低消費限制</li>
     * </ol>
     *
     * @param code        優惠碼
     * @param orderAmount 訂單金額
     * @return 優惠券 DTO
     * @throws BusinessException 若優惠券無效或不符合使用條件
     */
    @Transactional(readOnly = true)
    public CouponDto validateCoupon(String code, BigDecimal orderAmount) {
        log.debug("驗證優惠券，優惠碼：{}，訂單金額：{}", code, orderAmount);

        // 取得優惠券
        Coupon coupon = couponRepository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> BusinessException.notFound("優惠券", "優惠碼 " + code));

        // 檢查優惠券是否有效
        if (!coupon.isValid()) {
            if (!coupon.isActive()) {
                throw BusinessException.validationFailed("優惠券已停用");
            }
            if (coupon.isUpcoming()) {
                throw BusinessException.validationFailed("優惠券尚未生效");
            }
            if (coupon.isExpired()) {
                throw BusinessException.validationFailed("優惠券已過期");
            }
            if (coupon.isExhausted()) {
                throw BusinessException.validationFailed("優惠券已達到使用次數上限");
            }
        }

        // 檢查最低消費金額
        if (!coupon.meetsMinOrderAmount(orderAmount)) {
            throw BusinessException.validationFailed(
                    String.format("訂單金額需滿 %s 元才能使用此優惠券", coupon.getMinOrderAmount())
            );
        }

        log.debug("優惠券驗證通過，優惠碼：{}", code);
        return CouponDto.fromEntity(coupon);
    }

    /**
     * 使用優惠券
     *
     * <p>使用優惠券並增加使用次數。使用前會先驗證優惠券是否可用。</p>
     *
     * @param code        優惠碼
     * @param orderAmount 訂單金額
     * @return 折扣金額
     * @throws BusinessException 若優惠券無效或不符合使用條件
     */
    @Transactional
    public BigDecimal useCoupon(String code, BigDecimal orderAmount) {
        log.info("使用優惠券，優惠碼：{}，訂單金額：{}", code, orderAmount);

        // 驗證優惠券
        validateCoupon(code, orderAmount);

        // 取得優惠券並增加使用次數
        Coupon coupon = couponRepository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> BusinessException.notFound("優惠券", "優惠碼 " + code));

        coupon.incrementUsedCount();
        couponRepository.save(coupon);

        // 計算折扣金額
        BigDecimal discount = coupon.calculateDiscount(orderAmount);

        log.info("優惠券使用成功，優惠碼：{}，折扣金額：{}", code, discount);
        return discount;
    }

    /**
     * 計算優惠券折扣金額（不使用優惠券）
     *
     * <p>僅計算折扣金額，不增加使用次數。用於預覽折扣金額。</p>
     *
     * @param code        優惠碼
     * @param orderAmount 訂單金額
     * @return 折扣金額
     * @throws BusinessException 若優惠券無效或不符合使用條件
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateDiscount(String code, BigDecimal orderAmount) {
        log.debug("計算優惠券折扣金額，優惠碼：{}，訂單金額：{}", code, orderAmount);

        // 驗證優惠券
        validateCoupon(code, orderAmount);

        // 計算折扣金額
        Coupon coupon = couponRepository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> BusinessException.notFound("優惠券", "優惠碼 " + code));

        BigDecimal discount = coupon.calculateDiscount(orderAmount);
        log.debug("優惠券折扣金額計算結果：{}", discount);

        return discount;
    }

    /**
     * 統計有效的優惠券數量
     *
     * @return 有效的優惠券數量
     */
    @Transactional(readOnly = true)
    public long countValidCoupons() {
        return couponRepository.countValidCoupons(LocalDateTime.now());
    }

    /**
     * 根據 ID 查詢優惠券實體
     *
     * @param id 優惠券 ID
     * @return 優惠券實體
     * @throws BusinessException 若優惠券不存在
     */
    private Coupon findCouponById(Long id) {
        return couponRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("優惠券", id));
    }

    /**
     * 驗證優惠券請求資料
     *
     * @param request   建立/更新優惠券請求
     * @param excludeId 排除的優惠券 ID（更新時使用）
     * @throws BusinessException 若驗證失敗
     */
    private void validateCouponRequest(CreateCouponRequest request, Long excludeId) {
        // 驗證日期範圍
        if (request.getEndDate().isBefore(request.getStartDate()) ||
                request.getEndDate().isEqual(request.getStartDate())) {
            throw BusinessException.validationFailed("結束日期必須在開始日期之後");
        }

        // 驗證百分比折扣值不超過 100
        if (request.getDiscountType() == DiscountType.PERCENTAGE &&
                request.getDiscountValue().compareTo(new BigDecimal("100")) > 0) {
            throw BusinessException.validationFailed("百分比折扣值不能超過 100");
        }

        // 驗證固定金額折扣與最低消費金額的關係
        if (request.getDiscountType() == DiscountType.FIXED_AMOUNT &&
                request.getMinOrderAmount() != null &&
                request.getDiscountValue().compareTo(request.getMinOrderAmount()) > 0) {
            throw BusinessException.validationFailed("固定金額折扣不能超過最低訂單金額");
        }
    }
}
