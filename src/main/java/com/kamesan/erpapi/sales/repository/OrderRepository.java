package com.kamesan.erpapi.sales.repository;

import com.kamesan.erpapi.sales.entity.Order;
import com.kamesan.erpapi.sales.entity.OrderStatus;
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
import java.util.Optional;

/**
 * 訂單 Repository
 *
 * <p>提供訂單資料的存取操作，包括：</p>
 * <ul>
 *   <li>基本 CRUD 操作（繼承自 JpaRepository）</li>
 *   <li>根據訂單編號查詢</li>
 *   <li>根據門市查詢訂單</li>
 *   <li>根據客戶查詢訂單</li>
 *   <li>根據狀態查詢訂單</li>
 *   <li>根據日期範圍查詢訂單</li>
 *   <li>訂單統計查詢</li>
 * </ul>
 *
 * @author ERP System Team
 * @version 1.0.0
 * @see Order
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * 根據訂單編號查詢
     *
     * <p>訂單編號是唯一識別碼，查詢結果最多一筆</p>
     *
     * @param orderNo 訂單編號
     * @return 訂單（Optional）
     */
    Optional<Order> findByOrderNo(String orderNo);

    /**
     * 檢查訂單編號是否存在
     *
     * @param orderNo 訂單編號
     * @return 是否存在
     */
    boolean existsByOrderNo(String orderNo);

    /**
     * 根據門市查詢訂單（分頁）
     *
     * @param storeId  門市 ID
     * @param pageable 分頁參數
     * @return 訂單分頁結果
     */
    Page<Order> findByStoreId(Long storeId, Pageable pageable);

    /**
     * 根據客戶查詢訂單（分頁）
     *
     * @param customerId 客戶 ID
     * @param pageable   分頁參數
     * @return 訂單分頁結果
     */
    Page<Order> findByCustomerId(Long customerId, Pageable pageable);

    /**
     * 根據客戶查詢訂單（不分頁）
     *
     * @param customerId 客戶 ID
     * @return 訂單列表
     */
    List<Order> findByCustomerIdOrderByOrderDateDesc(Long customerId);

    /**
     * 根據狀態查詢訂單（分頁）
     *
     * @param status   訂單狀態
     * @param pageable 分頁參數
     * @return 訂單分頁結果
     */
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    /**
     * 根據門市和狀態查詢訂單（分頁）
     *
     * @param storeId  門市 ID
     * @param status   訂單狀態
     * @param pageable 分頁參數
     * @return 訂單分頁結果
     */
    Page<Order> findByStoreIdAndStatus(Long storeId, OrderStatus status, Pageable pageable);

    /**
     * 根據日期範圍查詢訂單（分頁）
     *
     * @param startDate 開始日期
     * @param endDate   結束日期
     * @param pageable  分頁參數
     * @return 訂單分頁結果
     */
    @Query("SELECT o FROM Order o WHERE o.orderDate BETWEEN :startDate AND :endDate")
    Page<Order> findByOrderDateBetween(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable);

    /**
     * 根據門市和日期範圍查詢訂單（分頁）
     *
     * @param storeId   門市 ID
     * @param startDate 開始日期
     * @param endDate   結束日期
     * @param pageable  分頁參數
     * @return 訂單分頁結果
     */
    @Query("SELECT o FROM Order o WHERE o.storeId = :storeId AND o.orderDate BETWEEN :startDate AND :endDate")
    Page<Order> findByStoreIdAndOrderDateBetween(
            @Param("storeId") Long storeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable);

    /**
     * 綜合條件查詢訂單（分頁）
     *
     * <p>支援多條件組合查詢：</p>
     * <ul>
     *   <li>門市 ID（可選）</li>
     *   <li>客戶 ID（可選）</li>
     *   <li>訂單狀態（可選）</li>
     *   <li>日期範圍（可選）</li>
     * </ul>
     *
     * @param storeId    門市 ID（可為 null）
     * @param customerId 客戶 ID（可為 null）
     * @param status     訂單狀態（可為 null）
     * @param startDate  開始日期（可為 null）
     * @param endDate    結束日期（可為 null）
     * @param pageable   分頁參數
     * @return 訂單分頁結果
     */
    @Query("SELECT o FROM Order o WHERE " +
            "(:storeId IS NULL OR o.storeId = :storeId) AND " +
            "(:customerId IS NULL OR o.customerId = :customerId) AND " +
            "(:status IS NULL OR o.status = :status) AND " +
            "(:startDate IS NULL OR o.orderDate >= :startDate) AND " +
            "(:endDate IS NULL OR o.orderDate <= :endDate)")
    Page<Order> findByConditions(
            @Param("storeId") Long storeId,
            @Param("customerId") Long customerId,
            @Param("status") OrderStatus status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable);

    /**
     * 根據訂單編號模糊查詢
     *
     * @param orderNoKeyword 訂單編號關鍵字
     * @param pageable       分頁參數
     * @return 訂單分頁結果
     */
    @Query("SELECT o FROM Order o WHERE o.orderNo LIKE %:keyword%")
    Page<Order> findByOrderNoContaining(@Param("keyword") String orderNoKeyword, Pageable pageable);

    /**
     * 查詢指定門市的今日訂單數量
     *
     * @param storeId   門市 ID
     * @param startDate 開始日期
     * @param endDate   結束日期
     * @return 訂單數量
     */
    @Query("SELECT COUNT(o) FROM Order o WHERE o.storeId = :storeId AND o.orderDate BETWEEN :startDate AND :endDate")
    Long countByStoreIdAndDateRange(
            @Param("storeId") Long storeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * 計算指定門市的日期範圍內銷售總額
     *
     * @param storeId   門市 ID
     * @param status    訂單狀態（通常為 PAID）
     * @param startDate 開始日期
     * @param endDate   結束日期
     * @return 銷售總額
     */
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o " +
            "WHERE o.storeId = :storeId AND o.status = :status " +
            "AND o.orderDate BETWEEN :startDate AND :endDate")
    BigDecimal sumTotalAmountByStoreIdAndStatusAndDateRange(
            @Param("storeId") Long storeId,
            @Param("status") OrderStatus status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * 計算指定日期範圍內的銷售總額（所有門市）
     *
     * @param status    訂單狀態（通常為 PAID）
     * @param startDate 開始日期
     * @param endDate   結束日期
     * @return 銷售總額
     */
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o " +
            "WHERE o.status = :status AND o.orderDate BETWEEN :startDate AND :endDate")
    BigDecimal sumTotalAmountByStatusAndDateRange(
            @Param("status") OrderStatus status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * 查詢各門市的訂單數量統計
     *
     * @param startDate 開始日期
     * @param endDate   結束日期
     * @return 門市 ID 和訂單數量的列表
     */
    @Query("SELECT o.storeId, COUNT(o) FROM Order o " +
            "WHERE o.orderDate BETWEEN :startDate AND :endDate " +
            "GROUP BY o.storeId")
    List<Object[]> countByStoreGrouped(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * 查詢各狀態的訂單數量統計
     *
     * @param storeId 門市 ID（可為 null 表示所有門市）
     * @return 狀態和訂單數量的列表
     */
    @Query("SELECT o.status, COUNT(o) FROM Order o " +
            "WHERE (:storeId IS NULL OR o.storeId = :storeId) " +
            "GROUP BY o.status")
    List<Object[]> countByStatusGrouped(@Param("storeId") Long storeId);

    /**
     * 抓取訂單及其明細（避免 N+1 問題）
     *
     * @param orderId 訂單 ID
     * @return 包含明細的訂單（Optional）
     */
    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.orderItems WHERE o.id = :orderId")
    Optional<Order> findByIdWithItems(@Param("orderId") Long orderId);

    /**
     * 抓取訂單及其付款記錄（避免 N+1 問題）
     *
     * @param orderId 訂單 ID
     * @return 包含付款記錄的訂單（Optional）
     */
    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.payments WHERE o.id = :orderId")
    Optional<Order> findByIdWithPayments(@Param("orderId") Long orderId);

    /**
     * 抓取訂單及其所有關聯（明細和付款記錄）
     *
     * @param orderId 訂單 ID
     * @return 包含所有關聯的訂單（Optional）
     */
    @Query("SELECT DISTINCT o FROM Order o " +
            "LEFT JOIN FETCH o.orderItems " +
            "LEFT JOIN FETCH o.payments " +
            "WHERE o.id = :orderId")
    Optional<Order> findByIdWithAllDetails(@Param("orderId") Long orderId);

    /**
     * 統計日期範圍內的訂單數量
     *
     * @param startDate 開始日期
     * @param endDate   結束日期
     * @return 訂單數量
     */
    @Query("SELECT COUNT(o) FROM Order o WHERE o.orderDate BETWEEN :startDate AND :endDate")
    Long countByDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * 統計日期範圍內已付款訂單數量
     *
     * @param startDate 開始日期
     * @param endDate   結束日期
     * @return 已付款訂單數量
     */
    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = 'PAID' AND o.orderDate BETWEEN :startDate AND :endDate")
    Long countPaidByDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * 按日期彙總銷售額
     *
     * @param startDate 開始日期
     * @param endDate   結束日期
     * @return 日期和銷售額列表 [日期, 銷售額, 訂單數]
     */
    @Query("SELECT o.orderDate, COALESCE(SUM(o.totalAmount), 0), COUNT(o) " +
           "FROM Order o WHERE o.status = 'PAID' AND o.orderDate BETWEEN :startDate AND :endDate " +
           "GROUP BY o.orderDate ORDER BY o.orderDate")
    List<Object[]> sumSalesByDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * 查詢客戶訂單統計（用於 RFM 分析）
     *
     * @param customerId 客戶 ID
     * @param startDate  開始日期
     * @param endDate    結束日期
     * @return [最後購買日期, 訂單數量, 總消費金額]
     */
    @Query("SELECT MAX(o.orderDate), COUNT(o), COALESCE(SUM(o.totalAmount), 0) " +
           "FROM Order o WHERE o.customerId = :customerId AND o.status = 'PAID' " +
           "AND o.orderDate BETWEEN :startDate AND :endDate")
    List<Object[]> findCustomerOrderStats(
            @Param("customerId") Long customerId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * 統計指定狀態的訂單數量
     *
     * @param status    訂單狀態
     * @param startDate 開始日期
     * @param endDate   結束日期
     * @return 訂單數量
     */
    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = :status AND o.orderDate BETWEEN :startDate AND :endDate")
    Long countByStatusAndDateRange(
            @Param("status") OrderStatus status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * 按日期彙總退款金額
     *
     * @param startDate 開始日期
     * @param endDate   結束日期
     * @return 日期和退款額列表 [日期, 退款額, 退款數]
     */
    @Query("SELECT o.orderDate, COALESCE(SUM(o.totalAmount), 0), COUNT(o) " +
           "FROM Order o WHERE o.status = 'REFUNDED' AND o.orderDate BETWEEN :startDate AND :endDate " +
           "GROUP BY o.orderDate ORDER BY o.orderDate")
    List<Object[]> sumRefundsByDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
