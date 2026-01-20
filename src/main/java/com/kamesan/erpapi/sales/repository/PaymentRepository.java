package com.kamesan.erpapi.sales.repository;

import com.kamesan.erpapi.sales.entity.Payment;
import com.kamesan.erpapi.sales.entity.PaymentMethod;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 付款記錄 Repository
 *
 * <p>提供付款記錄資料的存取操作，包括：</p>
 * <ul>
 *   <li>基本 CRUD 操作（繼承自 JpaRepository）</li>
 *   <li>根據訂單查詢付款記錄</li>
 *   <li>根據付款方式查詢</li>
 *   <li>根據日期範圍查詢</li>
 *   <li>付款統計</li>
 * </ul>
 *
 * @author ERP System Team
 * @version 1.0.0
 * @see Payment
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * 根據訂單 ID 查詢付款記錄列表
     *
     * @param orderId 訂單 ID
     * @return 付款記錄列表
     */
    List<Payment> findByOrderId(Long orderId);

    /**
     * 根據訂單 ID 查詢付款記錄（依付款時間排序）
     *
     * @param orderId 訂單 ID
     * @return 付款記錄列表（依付款時間正序）
     */
    List<Payment> findByOrderIdOrderByPaymentDateAsc(Long orderId);

    /**
     * 根據訂單 ID 刪除所有付款記錄
     *
     * @param orderId 訂單 ID
     */
    void deleteByOrderId(Long orderId);

    /**
     * 根據交易參考編號查詢
     *
     * @param referenceNo 交易參考編號
     * @return 付款記錄（Optional）
     */
    Optional<Payment> findByReferenceNo(String referenceNo);

    /**
     * 檢查交易參考編號是否存在
     *
     * @param referenceNo 交易參考編號
     * @return 是否存在
     */
    boolean existsByReferenceNo(String referenceNo);

    /**
     * 根據付款方式查詢（分頁）
     *
     * @param paymentMethod 付款方式
     * @param pageable      分頁參數
     * @return 付款記錄分頁結果
     */
    Page<Payment> findByPaymentMethod(PaymentMethod paymentMethod, Pageable pageable);

    /**
     * 根據付款日期範圍查詢（分頁）
     *
     * @param startDate 開始日期時間
     * @param endDate   結束日期時間
     * @param pageable  分頁參數
     * @return 付款記錄分頁結果
     */
    @Query("SELECT p FROM Payment p WHERE p.paymentDate BETWEEN :startDate AND :endDate")
    Page<Payment> findByPaymentDateBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    /**
     * 根據付款方式和日期範圍查詢
     *
     * @param paymentMethod 付款方式
     * @param startDate     開始日期時間
     * @param endDate       結束日期時間
     * @param pageable      分頁參數
     * @return 付款記錄分頁結果
     */
    @Query("SELECT p FROM Payment p WHERE p.paymentMethod = :paymentMethod " +
            "AND p.paymentDate BETWEEN :startDate AND :endDate")
    Page<Payment> findByPaymentMethodAndDateRange(
            @Param("paymentMethod") PaymentMethod paymentMethod,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    /**
     * 計算訂單的已付款總額
     *
     * @param orderId 訂單 ID
     * @return 已付款總額
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.order.id = :orderId")
    BigDecimal sumAmountByOrderId(@Param("orderId") Long orderId);

    /**
     * 計算指定日期範圍內的付款總額
     *
     * @param startDate 開始日期時間
     * @param endDate   結束日期時間
     * @return 付款總額
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p " +
            "WHERE p.paymentDate BETWEEN :startDate AND :endDate")
    BigDecimal sumAmountByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * 計算指定付款方式在日期範圍內的付款總額
     *
     * @param paymentMethod 付款方式
     * @param startDate     開始日期時間
     * @param endDate       結束日期時間
     * @return 付款總額
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p " +
            "WHERE p.paymentMethod = :paymentMethod " +
            "AND p.paymentDate BETWEEN :startDate AND :endDate")
    BigDecimal sumAmountByPaymentMethodAndDateRange(
            @Param("paymentMethod") PaymentMethod paymentMethod,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * 統計各付款方式的交易數量
     *
     * @param startDate 開始日期時間
     * @param endDate   結束日期時間
     * @return 付款方式和交易數量的列表
     */
    @Query("SELECT p.paymentMethod, COUNT(p) FROM Payment p " +
            "WHERE p.paymentDate BETWEEN :startDate AND :endDate " +
            "GROUP BY p.paymentMethod")
    List<Object[]> countByPaymentMethodGrouped(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * 統計各付款方式的交易金額
     *
     * @param startDate 開始日期時間
     * @param endDate   結束日期時間
     * @return 付款方式和交易金額的列表
     */
    @Query("SELECT p.paymentMethod, SUM(p.amount) FROM Payment p " +
            "WHERE p.paymentDate BETWEEN :startDate AND :endDate " +
            "GROUP BY p.paymentMethod")
    List<Object[]> sumAmountByPaymentMethodGrouped(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * 查詢指定門市的付款記錄（分頁）
     *
     * @param storeId  門市 ID
     * @param pageable 分頁參數
     * @return 付款記錄分頁結果
     */
    @Query("SELECT p FROM Payment p WHERE p.order.storeId = :storeId")
    Page<Payment> findByStoreId(@Param("storeId") Long storeId, Pageable pageable);

    /**
     * 計算指定門市在日期範圍內的付款總額
     *
     * @param storeId   門市 ID
     * @param startDate 開始日期時間
     * @param endDate   結束日期時間
     * @return 付款總額
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p " +
            "WHERE p.order.storeId = :storeId " +
            "AND p.paymentDate BETWEEN :startDate AND :endDate")
    BigDecimal sumAmountByStoreIdAndDateRange(
            @Param("storeId") Long storeId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * 計算訂單的付款記錄數量
     *
     * @param orderId 訂單 ID
     * @return 付款記錄數量
     */
    Long countByOrderId(Long orderId);
}
