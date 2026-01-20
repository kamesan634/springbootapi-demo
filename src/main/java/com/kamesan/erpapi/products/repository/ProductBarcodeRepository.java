package com.kamesan.erpapi.products.repository;

import com.kamesan.erpapi.products.entity.ProductBarcode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 商品條碼 Repository
 *
 * <p>提供商品條碼資料的存取操作。</p>
 *
 * <h2>主要功能：</h2>
 * <ul>
 *   <li>根據條碼查詢商品</li>
 *   <li>查詢商品的所有條碼</li>
 *   <li>檢查條碼是否存在</li>
 * </ul>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Repository
public interface ProductBarcodeRepository extends JpaRepository<ProductBarcode, Long> {

    /**
     * 根據條碼查詢
     *
     * @param barcode 條碼
     * @return 商品條碼（Optional）
     */
    Optional<ProductBarcode> findByBarcode(String barcode);

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
     * 根據商品 ID 查詢所有條碼
     *
     * @param productId 商品 ID
     * @return 條碼列表
     */
    @Query("SELECT pb FROM ProductBarcode pb WHERE pb.product.id = :productId ORDER BY pb.primary DESC")
    List<ProductBarcode> findByProductId(@Param("productId") Long productId);

    /**
     * 根據商品 ID 查詢主要條碼
     *
     * @param productId 商品 ID
     * @return 主要條碼（Optional）
     */
    @Query("SELECT pb FROM ProductBarcode pb WHERE pb.product.id = :productId AND pb.primary = true")
    Optional<ProductBarcode> findPrimaryByProductId(@Param("productId") Long productId);

    /**
     * 根據條碼查詢商品 ID
     *
     * @param barcode 條碼
     * @return 商品 ID（Optional）
     */
    @Query("SELECT pb.product.id FROM ProductBarcode pb WHERE pb.barcode = :barcode")
    Optional<Long> findProductIdByBarcode(@Param("barcode") String barcode);

    /**
     * 刪除商品的所有條碼
     *
     * @param productId 商品 ID
     */
    void deleteByProductId(Long productId);

    /**
     * 統計商品的條碼數量
     *
     * @param productId 商品 ID
     * @return 條碼數量
     */
    @Query("SELECT COUNT(pb) FROM ProductBarcode pb WHERE pb.product.id = :productId")
    long countByProductId(@Param("productId") Long productId);

    /**
     * 批次查詢多個條碼
     *
     * @param barcodes 條碼列表
     * @return 商品條碼列表
     */
    @Query("SELECT pb FROM ProductBarcode pb WHERE pb.barcode IN :barcodes")
    List<ProductBarcode> findByBarcodeIn(@Param("barcodes") List<String> barcodes);
}
