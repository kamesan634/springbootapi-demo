package com.kamesan.erpapi.purchasing.repository;

import com.kamesan.erpapi.purchasing.entity.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 供應商 Repository
 *
 * <p>提供供應商資料的存取操作，繼承 {@link JpaRepository} 獲得基本的 CRUD 功能。</p>
 *
 * <p>主要功能：</p>
 * <ul>
 *   <li>基本的新增、查詢、更新、刪除操作（繼承自 JpaRepository）</li>
 *   <li>根據供應商代碼查詢</li>
 *   <li>根據供應商名稱模糊查詢</li>
 *   <li>檢查供應商代碼是否存在</li>
 *   <li>查詢啟用中的供應商</li>
 *   <li>關鍵字搜尋（名稱、代碼、聯絡人）</li>
 * </ul>
 *
 * @author ERP System Team
 * @version 1.0.0
 * @see Supplier
 * @see JpaRepository
 */
@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    /**
     * 根據供應商代碼查詢
     *
     * <p>供應商代碼為唯一值，因此最多只會回傳一筆資料。</p>
     *
     * @param code 供應商代碼
     * @return 供應商（Optional 包裝，可能為空）
     */
    Optional<Supplier> findByCode(String code);

    /**
     * 根據供應商名稱精確查詢
     *
     * @param name 供應商名稱
     * @return 供應商（Optional 包裝，可能為空）
     */
    Optional<Supplier> findByName(String name);

    /**
     * 根據供應商名稱模糊查詢
     *
     * <p>使用 LIKE 進行模糊比對，可查詢名稱中包含指定關鍵字的供應商。</p>
     *
     * @param name     供應商名稱關鍵字
     * @param pageable 分頁參數
     * @return 供應商分頁結果
     */
    Page<Supplier> findByNameContaining(String name, Pageable pageable);

    /**
     * 檢查供應商代碼是否存在
     *
     * <p>用於新增供應商時，檢查代碼是否已被使用。</p>
     *
     * @param code 供應商代碼
     * @return 若代碼已存在回傳 true，否則回傳 false
     */
    boolean existsByCode(String code);

    /**
     * 檢查供應商名稱是否存在
     *
     * <p>用於新增供應商時，檢查名稱是否已被使用。</p>
     *
     * @param name 供應商名稱
     * @return 若名稱已存在回傳 true，否則回傳 false
     */
    boolean existsByName(String name);

    /**
     * 檢查是否存在其他供應商使用相同代碼
     *
     * <p>用於更新供應商時，檢查代碼是否與其他供應商重複。</p>
     *
     * @param code 供應商代碼
     * @param id   排除的供應商 ID（當前編輯的供應商）
     * @return 若存在其他供應商使用相同代碼回傳 true，否則回傳 false
     */
    boolean existsByCodeAndIdNot(String code, Long id);

    /**
     * 檢查是否存在其他供應商使用相同名稱
     *
     * <p>用於更新供應商時，檢查名稱是否與其他供應商重複。</p>
     *
     * @param name 供應商名稱
     * @param id   排除的供應商 ID（當前編輯的供應商）
     * @return 若存在其他供應商使用相同名稱回傳 true，否則回傳 false
     */
    boolean existsByNameAndIdNot(String name, Long id);

    /**
     * 查詢所有啟用中的供應商
     *
     * <p>回傳所有 isActive = true 的供應商，不分頁。</p>
     * <p>適用於下拉選單等需要完整清單的場景。</p>
     *
     * @return 啟用中的供應商清單
     */
    List<Supplier> findByIsActiveTrue();

    /**
     * 查詢啟用中的供應商（分頁）
     *
     * <p>回傳所有 isActive = true 的供應商，支援分頁。</p>
     *
     * @param pageable 分頁參數
     * @return 啟用中的供應商分頁結果
     */
    Page<Supplier> findByIsActiveTrue(Pageable pageable);

    /**
     * 查詢停用的供應商（分頁）
     *
     * <p>回傳所有 isActive = false 的供應商，支援分頁。</p>
     *
     * @param pageable 分頁參數
     * @return 停用的供應商分頁結果
     */
    Page<Supplier> findByIsActiveFalse(Pageable pageable);

    /**
     * 根據啟用狀態查詢供應商（分頁）
     *
     * @param isActive 啟用狀態
     * @param pageable 分頁參數
     * @return 供應商分頁結果
     */
    Page<Supplier> findByIsActive(Boolean isActive, Pageable pageable);

    /**
     * 搜尋供應商
     *
     * <p>根據關鍵字搜尋供應商，會比對以下欄位：</p>
     * <ul>
     *   <li>供應商代碼</li>
     *   <li>供應商名稱</li>
     *   <li>聯絡人姓名</li>
     *   <li>電子郵件</li>
     *   <li>電話號碼</li>
     * </ul>
     *
     * @param keyword  搜尋關鍵字
     * @param pageable 分頁參數
     * @return 符合條件的供應商分頁結果
     */
    @Query("SELECT s FROM Supplier s WHERE " +
            "s.code LIKE %:keyword% OR " +
            "s.name LIKE %:keyword% OR " +
            "s.contactPerson LIKE %:keyword% OR " +
            "s.email LIKE %:keyword% OR " +
            "s.phone LIKE %:keyword%")
    Page<Supplier> search(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 搜尋啟用中的供應商
     *
     * <p>根據關鍵字搜尋啟用中的供應商。</p>
     *
     * @param keyword  搜尋關鍵字
     * @param pageable 分頁參數
     * @return 符合條件的啟用供應商分頁結果
     */
    @Query("SELECT s FROM Supplier s WHERE s.isActive = true AND (" +
            "s.code LIKE %:keyword% OR " +
            "s.name LIKE %:keyword% OR " +
            "s.contactPerson LIKE %:keyword% OR " +
            "s.email LIKE %:keyword% OR " +
            "s.phone LIKE %:keyword%)")
    Page<Supplier> searchActive(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 根據付款條件查詢供應商
     *
     * @param paymentTerms 付款條件
     * @param pageable     分頁參數
     * @return 符合條件的供應商分頁結果
     */
    Page<Supplier> findByPaymentTermsContaining(String paymentTerms, Pageable pageable);

    /**
     * 統計啟用中的供應商數量
     *
     * @return 啟用中的供應商數量
     */
    long countByIsActiveTrue();

    /**
     * 統計停用的供應商數量
     *
     * @return 停用的供應商數量
     */
    long countByIsActiveFalse();
}
