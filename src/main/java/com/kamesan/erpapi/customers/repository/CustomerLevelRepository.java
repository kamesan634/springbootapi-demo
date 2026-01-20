package com.kamesan.erpapi.customers.repository;

import com.kamesan.erpapi.customers.entity.CustomerLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * 會員等級 Repository
 *
 * <p>提供會員等級資料的存取操作，包括：</p>
 * <ul>
 *   <li>基本的 CRUD 操作（繼承自 JpaRepository）</li>
 *   <li>根據代碼查詢等級</li>
 *   <li>查詢啟用的等級</li>
 *   <li>根據消費金額查詢可升級的等級</li>
 * </ul>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Repository
public interface CustomerLevelRepository extends JpaRepository<CustomerLevel, Long> {

    /**
     * 根據等級代碼查詢
     *
     * @param code 等級代碼
     * @return 會員等級（Optional）
     */
    Optional<CustomerLevel> findByCode(String code);

    /**
     * 檢查等級代碼是否存在
     *
     * @param code 等級代碼
     * @return 是否存在
     */
    boolean existsByCode(String code);

    /**
     * 檢查等級代碼是否存在（排除指定 ID）
     * <p>用於更新時檢查代碼是否重複</p>
     *
     * @param code 等級代碼
     * @param id   要排除的 ID
     * @return 是否存在
     */
    boolean existsByCodeAndIdNot(String code, Long id);

    /**
     * 查詢所有啟用的等級
     *
     * @return 啟用的會員等級列表
     */
    List<CustomerLevel> findByActiveTrueOrderBySortOrderAsc();

    /**
     * 查詢所有等級（依排序順序）
     *
     * @return 會員等級列表
     */
    List<CustomerLevel> findAllByOrderBySortOrderAsc();

    /**
     * 分頁查詢啟用的等級
     *
     * @param pageable 分頁參數
     * @return 會員等級分頁
     */
    Page<CustomerLevel> findByActiveTrue(Pageable pageable);

    /**
     * 根據消費金額查詢可升級至的最高等級
     * <p>查詢升級條件小於等於消費金額的最高等級</p>
     *
     * @param totalSpent 累積消費金額
     * @return 可升級至的最高等級
     */
    @Query("SELECT cl FROM CustomerLevel cl " +
            "WHERE cl.active = true " +
            "AND cl.upgradeCondition <= :totalSpent " +
            "ORDER BY cl.upgradeCondition DESC " +
            "LIMIT 1")
    Optional<CustomerLevel> findHighestEligibleLevel(@Param("totalSpent") BigDecimal totalSpent);

    /**
     * 查詢消費金額可升級至的所有等級
     *
     * @param totalSpent 累積消費金額
     * @return 可升級至的等級列表
     */
    @Query("SELECT cl FROM CustomerLevel cl " +
            "WHERE cl.active = true " +
            "AND cl.upgradeCondition <= :totalSpent " +
            "ORDER BY cl.sortOrder ASC")
    List<CustomerLevel> findEligibleLevels(@Param("totalSpent") BigDecimal totalSpent);

    /**
     * 查詢下一個可升級的等級
     *
     * @param currentUpgradeCondition 目前等級的升級條件
     * @return 下一個等級（Optional）
     */
    @Query("SELECT cl FROM CustomerLevel cl " +
            "WHERE cl.active = true " +
            "AND cl.upgradeCondition > :currentUpgradeCondition " +
            "ORDER BY cl.upgradeCondition ASC " +
            "LIMIT 1")
    Optional<CustomerLevel> findNextLevel(@Param("currentUpgradeCondition") BigDecimal currentUpgradeCondition);

    /**
     * 統計該等級的會員數量
     *
     * @param levelId 等級 ID
     * @return 會員數量
     */
    @Query("SELECT COUNT(c) FROM Customer c WHERE c.level.id = :levelId")
    long countCustomersByLevelId(@Param("levelId") Long levelId);

    /**
     * 搜尋等級（根據名稱或代碼）
     *
     * @param keyword  關鍵字
     * @param pageable 分頁參數
     * @return 會員等級分頁
     */
    @Query("SELECT cl FROM CustomerLevel cl WHERE " +
            "cl.name LIKE %:keyword% OR " +
            "cl.code LIKE %:keyword%")
    Page<CustomerLevel> search(@Param("keyword") String keyword, Pageable pageable);
}
