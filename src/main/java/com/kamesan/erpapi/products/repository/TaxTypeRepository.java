package com.kamesan.erpapi.products.repository;

import com.kamesan.erpapi.products.entity.TaxType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 稅別 Repository
 *
 * <p>提供稅別資料的存取操作。</p>
 *
 * <h2>主要功能：</h2>
 * <ul>
 *   <li>根據代碼查詢稅別</li>
 *   <li>查詢預設稅別</li>
 *   <li>查詢啟用的稅別</li>
 *   <li>關鍵字搜尋</li>
 * </ul>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Repository
public interface TaxTypeRepository extends JpaRepository<TaxType, Long> {

    /**
     * 根據稅別代碼查詢
     *
     * @param code 稅別代碼
     * @return 稅別（Optional）
     */
    Optional<TaxType> findByCode(String code);

    /**
     * 檢查稅別代碼是否存在
     *
     * @param code 稅別代碼
     * @return 是否存在
     */
    boolean existsByCode(String code);

    /**
     * 檢查稅別代碼是否存在（排除指定 ID）
     *
     * @param code 稅別代碼
     * @param id   要排除的 ID
     * @return 是否存在
     */
    boolean existsByCodeAndIdNot(String code, Long id);

    /**
     * 查詢預設稅別
     *
     * @return 預設稅別（Optional）
     */
    @Query("SELECT t FROM TaxType t WHERE t.isDefault = true")
    Optional<TaxType> findDefaultTaxType();

    /**
     * 查詢所有啟用的稅別
     *
     * @return 啟用的稅別列表
     */
    List<TaxType> findByActiveTrue();

    /**
     * 查詢所有啟用的稅別（分頁）
     *
     * @param pageable 分頁參數
     * @return 稅別分頁
     */
    Page<TaxType> findByActiveTrue(Pageable pageable);

    /**
     * 根據名稱或代碼搜尋稅別
     *
     * @param keyword  關鍵字
     * @param pageable 分頁參數
     * @return 稅別分頁
     */
    @Query("SELECT t FROM TaxType t WHERE " +
            "t.code LIKE %:keyword% OR " +
            "t.name LIKE %:keyword%")
    Page<TaxType> search(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 查詢所有啟用的稅別（依排序）
     *
     * @return 稅別列表
     */
    @Query("SELECT t FROM TaxType t WHERE t.active = true ORDER BY t.isDefault DESC, t.code ASC")
    List<TaxType> findAllActiveOrderByDefaultAndCode();
}
