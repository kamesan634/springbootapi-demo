package com.kamesan.erpapi.promotions.repository;

import com.kamesan.erpapi.promotions.entity.Coupon;
import com.kamesan.erpapi.promotions.entity.DiscountType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 優惠券 Repository
 *
 * <p>提供優惠券資料的存取操作，包括基本的 CRUD 和多種查詢方法。</p>
 *
 * <p>主要功能：</p>
 * <ul>
 *   <li>根據優惠碼查詢優惠券</li>
 *   <li>查詢有效的優惠券</li>
 *   <li>根據折扣類型查詢</li>
 *   <li>更新優惠券使用次數</li>
 *   <li>搜尋優惠券</li>
 * </ul>
 *
 * @author ERP System Team
 * @version 1.0.0
 * @see Coupon 優惠券實體
 */
@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {

    /**
     * 根據優惠碼查詢優惠券
     *
     * @param code 優惠碼
     * @return 優惠券（Optional）
     */
    Optional<Coupon> findByCode(String code);

    /**
     * 根據優惠碼查詢優惠券（忽略大小寫）
     *
     * @param code 優惠碼
     * @return 優惠券（Optional）
     */
    Optional<Coupon> findByCodeIgnoreCase(String code);

    /**
     * 檢查優惠碼是否存在
     *
     * @param code 優惠碼
     * @return 是否存在
     */
    boolean existsByCode(String code);

    /**
     * 檢查優惠碼是否存在（忽略大小寫）
     *
     * @param code 優惠碼
     * @return 是否存在
     */
    boolean existsByCodeIgnoreCase(String code);

    /**
     * 根據優惠券名稱查詢
     *
     * @param name 優惠券名稱
     * @return 優惠券（Optional）
     */
    Optional<Coupon> findByName(String name);

    /**
     * 根據折扣類型查詢優惠券
     *
     * @param discountType 折扣類型
     * @param pageable     分頁參數
     * @return 優惠券分頁
     */
    Page<Coupon> findByDiscountType(DiscountType discountType, Pageable pageable);

    /**
     * 查詢啟用中的優惠券
     *
     * @param pageable 分頁參數
     * @return 優惠券分頁
     */
    Page<Coupon> findByActiveTrue(Pageable pageable);

    /**
     * 查詢停用的優惠券
     *
     * @param pageable 分頁參數
     * @return 優惠券分頁
     */
    Page<Coupon> findByActiveFalse(Pageable pageable);

    /**
     * 查詢目前有效的優惠券
     *
     * <p>條件：</p>
     * <ul>
     *   <li>優惠券已啟用 (active = true)</li>
     *   <li>目前時間在開始日期之後</li>
     *   <li>目前時間在結束日期之前</li>
     *   <li>尚未達到最大使用次數（若有設定）</li>
     * </ul>
     *
     * @param now      目前時間
     * @param pageable 分頁參數
     * @return 優惠券分頁
     */
    @Query("SELECT c FROM Coupon c WHERE c.active = true " +
            "AND c.startDate <= :now AND c.endDate >= :now " +
            "AND (c.maxUses IS NULL OR c.usedCount < c.maxUses)")
    Page<Coupon> findValidCoupons(@Param("now") LocalDateTime now, Pageable pageable);

    /**
     * 查詢目前有效的優惠券（列表）
     *
     * @param now 目前時間
     * @return 優惠券列表
     */
    @Query("SELECT c FROM Coupon c WHERE c.active = true " +
            "AND c.startDate <= :now AND c.endDate >= :now " +
            "AND (c.maxUses IS NULL OR c.usedCount < c.maxUses) " +
            "ORDER BY c.startDate DESC")
    List<Coupon> findValidCoupons(@Param("now") LocalDateTime now);

    /**
     * 查詢即將生效的優惠券
     *
     * <p>條件：開始日期在目前時間之後</p>
     *
     * @param now      目前時間
     * @param pageable 分頁參數
     * @return 優惠券分頁
     */
    @Query("SELECT c FROM Coupon c WHERE c.startDate > :now ORDER BY c.startDate ASC")
    Page<Coupon> findUpcomingCoupons(@Param("now") LocalDateTime now, Pageable pageable);

    /**
     * 查詢已過期的優惠券
     *
     * <p>條件：結束日期在目前時間之前</p>
     *
     * @param now      目前時間
     * @param pageable 分頁參數
     * @return 優惠券分頁
     */
    @Query("SELECT c FROM Coupon c WHERE c.endDate < :now ORDER BY c.endDate DESC")
    Page<Coupon> findExpiredCoupons(@Param("now") LocalDateTime now, Pageable pageable);

    /**
     * 查詢已用完的優惠券
     *
     * <p>條件：已使用次數達到或超過最大使用次數</p>
     *
     * @param pageable 分頁參數
     * @return 優惠券分頁
     */
    @Query("SELECT c FROM Coupon c WHERE c.maxUses IS NOT NULL AND c.usedCount >= c.maxUses")
    Page<Coupon> findExhaustedCoupons(Pageable pageable);

    /**
     * 搜尋優惠券（根據優惠碼或名稱）
     *
     * @param keyword  關鍵字
     * @param pageable 分頁參數
     * @return 優惠券分頁
     */
    @Query("SELECT c FROM Coupon c WHERE " +
            "c.code LIKE %:keyword% OR " +
            "c.name LIKE %:keyword%")
    Page<Coupon> search(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 增加優惠券使用次數
     *
     * @param id 優惠券 ID
     * @return 更新的記錄數
     */
    @Modifying
    @Query("UPDATE Coupon c SET c.usedCount = c.usedCount + 1 WHERE c.id = :id")
    int incrementUsedCount(@Param("id") Long id);

    /**
     * 根據日期範圍查詢優惠券
     *
     * @param startDate 開始日期
     * @param endDate   結束日期
     * @param pageable  分頁參數
     * @return 優惠券分頁
     */
    @Query("SELECT c FROM Coupon c WHERE " +
            "(c.startDate BETWEEN :startDate AND :endDate) OR " +
            "(c.endDate BETWEEN :startDate AND :endDate) OR " +
            "(c.startDate <= :startDate AND c.endDate >= :endDate)")
    Page<Coupon> findByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    /**
     * 統計啟用中的優惠券數量
     *
     * @return 啟用中的優惠券數量
     */
    long countByActiveTrue();

    /**
     * 統計目前有效的優惠券數量
     *
     * @param now 目前時間
     * @return 目前有效的優惠券數量
     */
    @Query("SELECT COUNT(c) FROM Coupon c WHERE c.active = true " +
            "AND c.startDate <= :now AND c.endDate >= :now " +
            "AND (c.maxUses IS NULL OR c.usedCount < c.maxUses)")
    long countValidCoupons(@Param("now") LocalDateTime now);
}
