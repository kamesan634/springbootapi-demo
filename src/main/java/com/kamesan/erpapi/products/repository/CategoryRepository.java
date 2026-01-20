package com.kamesan.erpapi.products.repository;

import com.kamesan.erpapi.products.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 商品分類 Repository
 *
 * <p>提供商品分類資料的存取操作。</p>
 *
 * <h2>主要功能：</h2>
 * <ul>
 *   <li>根據代碼查詢分類</li>
 *   <li>查詢根分類</li>
 *   <li>查詢子分類</li>
 *   <li>查詢分類樹狀結構</li>
 *   <li>關鍵字搜尋</li>
 * </ul>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * 根據分類代碼查詢
     *
     * @param code 分類代碼
     * @return 分類（Optional）
     */
    Optional<Category> findByCode(String code);

    /**
     * 檢查分類代碼是否存在
     *
     * @param code 分類代碼
     * @return 是否存在
     */
    boolean existsByCode(String code);

    /**
     * 檢查分類代碼是否存在（排除指定 ID）
     *
     * @param code 分類代碼
     * @param id   要排除的 ID
     * @return 是否存在
     */
    boolean existsByCodeAndIdNot(String code, Long id);

    /**
     * 查詢所有根分類（無父分類）
     *
     * @return 根分類列表
     */
    @Query("SELECT c FROM Category c WHERE c.parent IS NULL ORDER BY c.sortOrder ASC, c.code ASC")
    List<Category> findRootCategories();

    /**
     * 查詢所有啟用的根分類
     *
     * @return 根分類列表
     */
    @Query("SELECT c FROM Category c WHERE c.parent IS NULL AND c.active = true ORDER BY c.sortOrder ASC, c.code ASC")
    List<Category> findActiveRootCategories();

    /**
     * 根據父分類查詢子分類
     *
     * @param parentId 父分類 ID
     * @return 子分類列表
     */
    @Query("SELECT c FROM Category c WHERE c.parent.id = :parentId ORDER BY c.sortOrder ASC, c.code ASC")
    List<Category> findByParentId(@Param("parentId") Long parentId);

    /**
     * 根據父分類查詢啟用的子分類
     *
     * @param parentId 父分類 ID
     * @return 子分類列表
     */
    @Query("SELECT c FROM Category c WHERE c.parent.id = :parentId AND c.active = true ORDER BY c.sortOrder ASC, c.code ASC")
    List<Category> findActiveByParentId(@Param("parentId") Long parentId);

    /**
     * 根據路徑查詢所有子孫分類
     * <p>使用路徑前綴匹配查詢所有子孫分類</p>
     *
     * @param pathPrefix 路徑前綴（如：1/2/）
     * @return 子孫分類列表
     */
    @Query("SELECT c FROM Category c WHERE c.path LIKE :pathPrefix% ORDER BY c.level ASC, c.sortOrder ASC")
    List<Category> findDescendantsByPathPrefix(@Param("pathPrefix") String pathPrefix);

    /**
     * 查詢所有啟用的分類
     *
     * @return 啟用的分類列表
     */
    List<Category> findByActiveTrue();

    /**
     * 查詢所有啟用的分類（分頁）
     *
     * @param pageable 分頁參數
     * @return 分類分頁
     */
    Page<Category> findByActiveTrue(Pageable pageable);

    /**
     * 根據層級查詢分類
     *
     * @param level 層級
     * @return 分類列表
     */
    @Query("SELECT c FROM Category c WHERE c.level = :level AND c.active = true ORDER BY c.sortOrder ASC")
    List<Category> findByLevel(@Param("level") Integer level);

    /**
     * 根據名稱或代碼搜尋分類
     *
     * @param keyword  關鍵字
     * @param pageable 分頁參數
     * @return 分類分頁
     */
    @Query("SELECT c FROM Category c WHERE " +
            "c.code LIKE %:keyword% OR " +
            "c.name LIKE %:keyword%")
    Page<Category> search(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 計算分類下的商品數量
     *
     * @param categoryId 分類 ID
     * @return 商品數量
     */
    @Query("SELECT COUNT(p) FROM Product p WHERE p.category.id = :categoryId")
    long countProductsByCategoryId(@Param("categoryId") Long categoryId);

    /**
     * 檢查分類是否有子分類
     *
     * @param categoryId 分類 ID
     * @return 是否有子分類
     */
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Category c WHERE c.parent.id = :categoryId")
    boolean hasChildren(@Param("categoryId") Long categoryId);
}
