package com.kamesan.erpapi.promotions.repository;

import com.kamesan.erpapi.promotions.entity.Promotion;
import com.kamesan.erpapi.promotions.entity.PromotionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 促銷活動 Repository
 *
 * <p>提供促銷活動資料的存取操作，包括基本的 CRUD 和多種查詢方法。</p>
 *
 * <p>主要功能：</p>
 * <ul>
 *   <li>根據名稱查詢促銷活動</li>
 *   <li>查詢有效的促銷活動</li>
 *   <li>根據促銷類型查詢</li>
 *   <li>根據日期範圍查詢</li>
 *   <li>搜尋促銷活動</li>
 * </ul>
 *
 * @author ERP System Team
 * @version 1.0.0
 * @see Promotion 促銷活動實體
 */
@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long> {

    /**
     * 根據促銷活動名稱查詢
     *
     * @param name 促銷活動名稱
     * @return 促銷活動（Optional）
     */
    Optional<Promotion> findByName(String name);

    /**
     * 檢查促銷活動名稱是否存在
     *
     * @param name 促銷活動名稱
     * @return 是否存在
     */
    boolean existsByName(String name);

    /**
     * 根據促銷類型查詢促銷活動
     *
     * @param type     促銷類型
     * @param pageable 分頁參數
     * @return 促銷活動分頁
     */
    Page<Promotion> findByType(PromotionType type, Pageable pageable);

    /**
     * 查詢啟用中的促銷活動
     *
     * @param pageable 分頁參數
     * @return 促銷活動分頁
     */
    Page<Promotion> findByActiveTrue(Pageable pageable);

    /**
     * 查詢停用的促銷活動
     *
     * @param pageable 分頁參數
     * @return 促銷活動分頁
     */
    Page<Promotion> findByActiveFalse(Pageable pageable);

    /**
     * 查詢目前正在進行中的促銷活動
     *
     * <p>條件：</p>
     * <ul>
     *   <li>活動已啟用 (active = true)</li>
     *   <li>目前時間在開始日期之後</li>
     *   <li>目前時間在結束日期之前</li>
     * </ul>
     *
     * @param now      目前時間
     * @param pageable 分頁參數
     * @return 促銷活動分頁
     */
    @Query("SELECT p FROM Promotion p WHERE p.active = true AND p.startDate <= :now AND p.endDate >= :now")
    Page<Promotion> findOngoingPromotions(@Param("now") LocalDateTime now, Pageable pageable);

    /**
     * 查詢目前正在進行中的促銷活動（列表）
     *
     * @param now 目前時間
     * @return 促銷活動列表
     */
    @Query("SELECT p FROM Promotion p WHERE p.active = true AND p.startDate <= :now AND p.endDate >= :now ORDER BY p.startDate DESC")
    List<Promotion> findOngoingPromotions(@Param("now") LocalDateTime now);

    /**
     * 查詢即將開始的促銷活動
     *
     * <p>條件：開始日期在目前時間之後</p>
     *
     * @param now      目前時間
     * @param pageable 分頁參數
     * @return 促銷活動分頁
     */
    @Query("SELECT p FROM Promotion p WHERE p.startDate > :now ORDER BY p.startDate ASC")
    Page<Promotion> findUpcomingPromotions(@Param("now") LocalDateTime now, Pageable pageable);

    /**
     * 查詢已結束的促銷活動
     *
     * <p>條件：結束日期在目前時間之前</p>
     *
     * @param now      目前時間
     * @param pageable 分頁參數
     * @return 促銷活動分頁
     */
    @Query("SELECT p FROM Promotion p WHERE p.endDate < :now ORDER BY p.endDate DESC")
    Page<Promotion> findExpiredPromotions(@Param("now") LocalDateTime now, Pageable pageable);

    /**
     * 根據日期範圍查詢促銷活動
     *
     * <p>查詢在指定日期範圍內有效的促銷活動</p>
     *
     * @param startDate 開始日期
     * @param endDate   結束日期
     * @param pageable  分頁參數
     * @return 促銷活動分頁
     */
    @Query("SELECT p FROM Promotion p WHERE " +
            "(p.startDate BETWEEN :startDate AND :endDate) OR " +
            "(p.endDate BETWEEN :startDate AND :endDate) OR " +
            "(p.startDate <= :startDate AND p.endDate >= :endDate)")
    Page<Promotion> findByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    /**
     * 搜尋促銷活動（根據名稱或描述）
     *
     * @param keyword  關鍵字
     * @param pageable 分頁參數
     * @return 促銷活動分頁
     */
    @Query("SELECT p FROM Promotion p WHERE " +
            "p.name LIKE %:keyword% OR " +
            "p.description LIKE %:keyword%")
    Page<Promotion> search(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 根據促銷類型和啟用狀態查詢促銷活動
     *
     * @param type     促銷類型
     * @param active   是否啟用
     * @param pageable 分頁參數
     * @return 促銷活動分頁
     */
    Page<Promotion> findByTypeAndActive(PromotionType type, boolean active, Pageable pageable);

    /**
     * 統計啟用中的促銷活動數量
     *
     * @return 啟用中的促銷活動數量
     */
    long countByActiveTrue();

    /**
     * 統計正在進行中的促銷活動數量
     *
     * @param now 目前時間
     * @return 正在進行中的促銷活動數量
     */
    @Query("SELECT COUNT(p) FROM Promotion p WHERE p.active = true AND p.startDate <= :now AND p.endDate >= :now")
    long countOngoingPromotions(@Param("now") LocalDateTime now);
}
