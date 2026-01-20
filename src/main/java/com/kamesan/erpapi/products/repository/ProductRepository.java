package com.kamesan.erpapi.products.repository;

import com.kamesan.erpapi.products.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 商品 Repository
 *
 * <p>提供商品資料的存取操作。</p>
 *
 * <h2>主要功能：</h2>
 * <ul>
 *   <li>根據貨號(SKU)查詢商品</li>
 *   <li>根據條碼查詢商品</li>
 *   <li>根據分類查詢商品</li>
 *   <li>多條件搜尋商品</li>
 *   <li>庫存預警查詢</li>
 * </ul>
 *
 * <p>實作 {@link JpaSpecificationExecutor} 以支援動態查詢條件。</p>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    /**
     * 根據貨號(SKU)查詢商品
     *
     * @param sku 商品貨號
     * @return 商品（Optional）
     */
    Optional<Product> findBySku(String sku);

    /**
     * 根據條碼查詢商品
     *
     * @param barcode 條碼
     * @return 商品（Optional）
     */
    Optional<Product> findByBarcode(String barcode);

    /**
     * 檢查貨號是否存在
     *
     * @param sku 商品貨號
     * @return 是否存在
     */
    boolean existsBySku(String sku);

    /**
     * 檢查貨號是否存在（排除指定 ID）
     *
     * @param sku 商品貨號
     * @param id  要排除的 ID
     * @return 是否存在
     */
    boolean existsBySkuAndIdNot(String sku, Long id);

    /**
     * 檢查條碼是否存在
     *
     * @param barcode 條碼
     * @return 是否存在
     */
    boolean existsByBarcode(String barcode);

    /**
     * 檢查條碼是否存在（排除指定 ID）
     *
     * @param barcode 條碼
     * @param id      要排除的 ID
     * @return 是否存在
     */
    boolean existsByBarcodeAndIdNot(String barcode, Long id);

    /**
     * 根據分類 ID 查詢商品
     *
     * @param categoryId 分類 ID
     * @param pageable   分頁參數
     * @return 商品分頁
     */
    @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId")
    Page<Product> findByCategoryId(@Param("categoryId") Long categoryId, Pageable pageable);

    /**
     * 根據分類 ID 查詢啟用的商品
     *
     * @param categoryId 分類 ID
     * @param pageable   分頁參數
     * @return 商品分頁
     */
    @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId AND p.active = true")
    Page<Product> findActiveByCategoryId(@Param("categoryId") Long categoryId, Pageable pageable);

    /**
     * 查詢所有啟用的商品
     *
     * @param pageable 分頁參數
     * @return 商品分頁
     */
    Page<Product> findByActiveTrue(Pageable pageable);

    /**
     * 根據關鍵字搜尋商品（名稱、貨號、條碼）
     *
     * @param keyword  關鍵字
     * @param pageable 分頁參數
     * @return 商品分頁
     */
    @Query("SELECT p FROM Product p WHERE " +
            "p.sku LIKE %:keyword% OR " +
            "p.name LIKE %:keyword% OR " +
            "p.barcode LIKE %:keyword%")
    Page<Product> search(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 根據關鍵字搜尋啟用的商品
     *
     * @param keyword  關鍵字
     * @param pageable 分頁參數
     * @return 商品分頁
     */
    @Query("SELECT p FROM Product p WHERE p.active = true AND (" +
            "p.sku LIKE %:keyword% OR " +
            "p.name LIKE %:keyword% OR " +
            "p.barcode LIKE %:keyword%)")
    Page<Product> searchActive(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 根據分類路徑查詢商品（包含子分類）
     *
     * @param pathPrefix 分類路徑前綴
     * @param pageable   分頁參數
     * @return 商品分頁
     */
    @Query("SELECT p FROM Product p WHERE p.category.path LIKE :pathPrefix%")
    Page<Product> findByCategoryPathPrefix(@Param("pathPrefix") String pathPrefix, Pageable pageable);

    /**
     * 查詢庫存低於安全庫存的商品
     * <p>注意：這裡只查詢商品定義的安全庫存，實際庫存需要從庫存模組取得</p>
     *
     * @param pageable 分頁參數
     * @return 商品分頁
     */
    @Query("SELECT p FROM Product p WHERE p.active = true AND p.safetyStock > 0")
    Page<Product> findProductsWithSafetyStock(Pageable pageable);

    /**
     * 根據單位 ID 查詢商品數量
     *
     * @param unitId 單位 ID
     * @return 商品數量
     */
    @Query("SELECT COUNT(p) FROM Product p WHERE p.unit.id = :unitId")
    long countByUnitId(@Param("unitId") Long unitId);

    /**
     * 根據稅別 ID 查詢商品數量
     *
     * @param taxTypeId 稅別 ID
     * @return 商品數量
     */
    @Query("SELECT COUNT(p) FROM Product p WHERE p.taxType.id = :taxTypeId")
    long countByTaxTypeId(@Param("taxTypeId") Long taxTypeId);

    /**
     * 查詢指定分類下的商品 ID 列表
     *
     * @param categoryId 分類 ID
     * @return 商品 ID 列表
     */
    @Query("SELECT p.id FROM Product p WHERE p.category.id = :categoryId")
    List<Long> findIdsByCategoryId(@Param("categoryId") Long categoryId);

    /**
     * 批次查詢商品（根據 ID 列表）
     *
     * @param ids 商品 ID 列表
     * @return 商品列表
     */
    @Query("SELECT p FROM Product p WHERE p.id IN :ids")
    List<Product> findByIdIn(@Param("ids") List<Long> ids);

    /**
     * 根據貨號列表查詢商品
     *
     * @param skus 貨號列表
     * @return 商品列表
     */
    @Query("SELECT p FROM Product p WHERE p.sku IN :skus")
    List<Product> findBySkuIn(@Param("skus") List<String> skus);

    /**
     * 查詢所有啟用商品（不分頁）
     *
     * @return 啟用商品列表
     */
    List<Product> findByActiveTrue();

    /**
     * 根據分類查詢商品（不分頁）
     *
     * @param categoryId 分類 ID
     * @return 商品列表
     */
    List<Product> findByCategoryId(Long categoryId);

    /**
     * 根據分類查詢啟用商品（不分頁）
     *
     * @param categoryId 分類 ID
     * @return 啟用商品列表
     */
    List<Product> findByCategoryIdAndActiveTrue(Long categoryId);
}
