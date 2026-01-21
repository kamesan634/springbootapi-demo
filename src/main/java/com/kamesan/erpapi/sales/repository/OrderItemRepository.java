package com.kamesan.erpapi.sales.repository;

import com.kamesan.erpapi.sales.entity.OrderItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 訂單明細 Repository
 *
 * <p>提供訂單明細資料的存取操作，包括：</p>
 * <ul>
 *   <li>基本 CRUD 操作（繼承自 JpaRepository）</li>
 *   <li>根據訂單查詢明細</li>
 *   <li>根據商品查詢銷售記錄</li>
 *   <li>商品銷售統計</li>
 * </ul>
 *
 * @author ERP System Team
 * @version 1.0.0
 * @see OrderItem
 */
@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    /**
     * 根據訂單 ID 查詢明細列表
     *
     * @param orderId 訂單 ID
     * @return 訂單明細列表
     */
    List<OrderItem> findByOrderId(Long orderId);

    /**
     * 根據訂單 ID 刪除所有明細
     *
     * @param orderId 訂單 ID
     */
    void deleteByOrderId(Long orderId);

    /**
     * 根據商品 ID 查詢銷售記錄（分頁）
     *
     * @param productId 商品 ID
     * @param pageable  分頁參數
     * @return 訂單明細分頁結果
     */
    Page<OrderItem> findByProductId(Long productId, Pageable pageable);

    /**
     * 計算商品的總銷售數量
     *
     * @param productId 商品 ID
     * @return 總銷售數量
     */
    @Query("SELECT COALESCE(SUM(oi.quantity), 0) FROM OrderItem oi " +
            "JOIN oi.order o WHERE oi.productId = :productId " +
            "AND o.status = 'PAID'")
    Long sumQuantityByProductId(@Param("productId") Long productId);

    /**
     * 計算商品的總銷售金額
     *
     * @param productId 商品 ID
     * @return 總銷售金額
     */
    @Query("SELECT COALESCE(SUM(oi.subtotal), 0) FROM OrderItem oi " +
            "JOIN oi.order o WHERE oi.productId = :productId " +
            "AND o.status = 'PAID'")
    BigDecimal sumSubtotalByProductId(@Param("productId") Long productId);

    /**
     * 計算商品在指定日期範圍內的銷售數量
     *
     * @param productId 商品 ID
     * @param startDate 開始日期時間
     * @param endDate   結束日期時間
     * @return 銷售數量
     */
    @Query("SELECT COALESCE(SUM(oi.quantity), 0) FROM OrderItem oi " +
            "JOIN oi.order o WHERE oi.productId = :productId " +
            "AND o.status = 'PAID' " +
            "AND o.orderDate BETWEEN :startDate AND :endDate")
    Long sumQuantityByProductIdAndDateRange(
            @Param("productId") Long productId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * 計算商品在指定日期範圍內的銷售金額
     *
     * @param productId 商品 ID
     * @param startDate 開始日期時間
     * @param endDate   結束日期時間
     * @return 銷售金額
     */
    @Query("SELECT COALESCE(SUM(oi.subtotal), 0) FROM OrderItem oi " +
            "JOIN oi.order o WHERE oi.productId = :productId " +
            "AND o.status = 'PAID' " +
            "AND o.orderDate BETWEEN :startDate AND :endDate")
    BigDecimal sumSubtotalByProductIdAndDateRange(
            @Param("productId") Long productId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * 查詢熱銷商品排行（依銷售數量）
     *
     * <p>查詢指定日期範圍內銷售數量最高的商品</p>
     *
     * @param startDate 開始日期時間
     * @param endDate   結束日期時間
     * @param pageable  分頁參數（用於限制返回數量）
     * @return 商品 ID、銷售數量、銷售金額的列表
     */
    @Query("SELECT oi.productId, SUM(oi.quantity) as totalQty, SUM(oi.subtotal) as totalAmount " +
            "FROM OrderItem oi JOIN oi.order o " +
            "WHERE o.status = 'PAID' " +
            "AND o.orderDate BETWEEN :startDate AND :endDate " +
            "GROUP BY oi.productId " +
            "ORDER BY totalQty DESC")
    Page<Object[]> findTopSellingProducts(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    /**
     * 查詢熱銷商品排行（依銷售金額）
     *
     * <p>查詢指定日期範圍內銷售金額最高的商品</p>
     *
     * @param startDate 開始日期時間
     * @param endDate   結束日期時間
     * @param pageable  分頁參數（用於限制返回數量）
     * @return 商品 ID、銷售數量、銷售金額的列表
     */
    @Query("SELECT oi.productId, SUM(oi.quantity) as totalQty, SUM(oi.subtotal) as totalAmount " +
            "FROM OrderItem oi JOIN oi.order o " +
            "WHERE o.status = 'PAID' " +
            "AND o.orderDate BETWEEN :startDate AND :endDate " +
            "GROUP BY oi.productId " +
            "ORDER BY totalAmount DESC")
    Page<Object[]> findTopRevenueProducts(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    /**
     * 查詢指定門市的熱銷商品排行
     *
     * @param storeId   門市 ID
     * @param startDate 開始日期時間
     * @param endDate   結束日期時間
     * @param pageable  分頁參數
     * @return 商品 ID、銷售數量、銷售金額的列表
     */
    @Query("SELECT oi.productId, SUM(oi.quantity) as totalQty, SUM(oi.subtotal) as totalAmount " +
            "FROM OrderItem oi JOIN oi.order o " +
            "WHERE o.storeId = :storeId " +
            "AND o.status = 'PAID' " +
            "AND o.orderDate BETWEEN :startDate AND :endDate " +
            "GROUP BY oi.productId " +
            "ORDER BY totalQty DESC")
    Page<Object[]> findTopSellingProductsByStore(
            @Param("storeId") Long storeId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    /**
     * 計算訂單的明細總數
     *
     * @param orderId 訂單 ID
     * @return 明細數量
     */
    Long countByOrderId(Long orderId);

    /**
     * 檢查商品是否有銷售記錄
     *
     * @param productId 商品 ID
     * @return 是否有銷售記錄
     */
    boolean existsByProductId(Long productId);

    /**
     * 查詢商品銷售統計（用於利潤分析）
     *
     * @param startDate 開始日期
     * @param endDate   結束日期
     * @return 商品 ID、銷售數量、銷售金額的列表
     */
    @Query("SELECT oi.productId, SUM(oi.quantity), SUM(oi.subtotal) " +
            "FROM OrderItem oi JOIN oi.order o " +
            "WHERE o.status = 'PAID' " +
            "AND o.orderDate BETWEEN :startDate AND :endDate " +
            "GROUP BY oi.productId")
    List<Object[]> findProductSalesByDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
