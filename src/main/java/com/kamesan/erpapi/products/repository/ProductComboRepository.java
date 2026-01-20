package com.kamesan.erpapi.products.repository;

import com.kamesan.erpapi.products.entity.ProductCombo;
import com.kamesan.erpapi.products.entity.ProductCombo.ComboType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 商品組合 Repository
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Repository
public interface ProductComboRepository extends JpaRepository<ProductCombo, Long> {

    /**
     * 根據編碼查詢
     */
    Optional<ProductCombo> findByCode(String code);

    /**
     * 檢查編碼是否存在
     */
    boolean existsByCode(String code);

    /**
     * 查詢啟用的組合
     */
    List<ProductCombo> findByActiveTrue();

    /**
     * 查詢指定類型的組合
     */
    List<ProductCombo> findByComboType(ComboType comboType);

    /**
     * 查詢當前有效的組合
     */
    @Query("SELECT c FROM ProductCombo c WHERE c.active = true " +
           "AND (c.startDate IS NULL OR c.startDate <= :date) " +
           "AND (c.endDate IS NULL OR c.endDate >= :date)")
    List<ProductCombo> findSellableCombos(@Param("date") LocalDate date);

    /**
     * 綜合條件查詢
     */
    @Query("SELECT c FROM ProductCombo c WHERE " +
           "(:keyword IS NULL OR c.name LIKE %:keyword% OR c.code LIKE %:keyword%) AND " +
           "(:comboType IS NULL OR c.comboType = :comboType) AND " +
           "(:active IS NULL OR c.active = :active)")
    Page<ProductCombo> findByConditions(
            @Param("keyword") String keyword,
            @Param("comboType") ComboType comboType,
            @Param("active") Boolean active,
            Pageable pageable);

    /**
     * 查詢包含特定商品的組合
     */
    @Query("SELECT DISTINCT c FROM ProductCombo c JOIN c.items i WHERE i.product.id = :productId")
    List<ProductCombo> findByProductId(@Param("productId") Long productId);

    /**
     * 抓取組合及其項目
     */
    @Query("SELECT DISTINCT c FROM ProductCombo c LEFT JOIN FETCH c.items WHERE c.id = :id")
    Optional<ProductCombo> findByIdWithItems(@Param("id") Long id);

    /**
     * 抓取組合及其項目和商品
     */
    @Query("SELECT DISTINCT c FROM ProductCombo c " +
           "LEFT JOIN FETCH c.items i " +
           "LEFT JOIN FETCH i.product " +
           "WHERE c.id = :id")
    Optional<ProductCombo> findByIdWithItemsAndProducts(@Param("id") Long id);

    /**
     * 查詢即將到期的組合
     */
    @Query("SELECT c FROM ProductCombo c WHERE c.active = true " +
           "AND c.endDate IS NOT NULL " +
           "AND c.endDate BETWEEN :startDate AND :endDate")
    List<ProductCombo> findExpiringCombos(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * 統計各類型組合數量
     */
    @Query("SELECT c.comboType, COUNT(c) FROM ProductCombo c WHERE c.active = true GROUP BY c.comboType")
    List<Object[]> countByComboType();
}
