package com.kamesan.erpapi.products.repository;

import com.kamesan.erpapi.products.entity.Unit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 單位 Repository
 *
 * <p>提供單位資料的存取操作。</p>
 *
 * <h2>主要功能：</h2>
 * <ul>
 *   <li>根據代碼查詢單位</li>
 *   <li>查詢啟用的單位</li>
 *   <li>關鍵字搜尋</li>
 * </ul>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Repository
public interface UnitRepository extends JpaRepository<Unit, Long> {

    /**
     * 根據單位代碼查詢
     *
     * @param code 單位代碼
     * @return 單位（Optional）
     */
    Optional<Unit> findByCode(String code);

    /**
     * 檢查單位代碼是否存在
     *
     * @param code 單位代碼
     * @return 是否存在
     */
    boolean existsByCode(String code);

    /**
     * 檢查單位代碼是否存在（排除指定 ID）
     *
     * @param code 單位代碼
     * @param id   要排除的 ID
     * @return 是否存在
     */
    boolean existsByCodeAndIdNot(String code, Long id);

    /**
     * 查詢所有啟用的單位
     *
     * @return 啟用的單位列表
     */
    List<Unit> findByActiveTrue();

    /**
     * 查詢所有啟用的單位（分頁）
     *
     * @param pageable 分頁參數
     * @return 單位分頁
     */
    Page<Unit> findByActiveTrue(Pageable pageable);

    /**
     * 根據名稱或代碼搜尋單位
     *
     * @param keyword  關鍵字
     * @param pageable 分頁參數
     * @return 單位分頁
     */
    @Query("SELECT u FROM Unit u WHERE " +
            "u.code LIKE %:keyword% OR " +
            "u.name LIKE %:keyword%")
    Page<Unit> search(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 查詢所有啟用的單位（依代碼排序）
     *
     * @return 單位列表
     */
    @Query("SELECT u FROM Unit u WHERE u.active = true ORDER BY u.code ASC")
    List<Unit> findAllActiveOrderByCode();
}
