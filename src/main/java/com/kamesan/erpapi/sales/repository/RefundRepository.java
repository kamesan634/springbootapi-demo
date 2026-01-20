package com.kamesan.erpapi.sales.repository;

import com.kamesan.erpapi.sales.entity.Refund;
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
 * 退貨單 Repository
 */
@Repository
public interface RefundRepository extends JpaRepository<Refund, Long> {

    Optional<Refund> findByRefundNo(String refundNo);

    boolean existsByRefundNo(String refundNo);

    List<Refund> findByOrderId(Long orderId);

    Page<Refund> findByStoreId(Long storeId, Pageable pageable);

    Page<Refund> findByCustomerId(Long customerId, Pageable pageable);

    Page<Refund> findByStatus(Refund.RefundStatus status, Pageable pageable);

    @Query("SELECT r FROM Refund r WHERE " +
            "(:storeId IS NULL OR r.storeId = :storeId) AND " +
            "(:customerId IS NULL OR r.customerId = :customerId) AND " +
            "(:status IS NULL OR r.status = :status) AND " +
            "(:startDate IS NULL OR r.refundDate >= :startDate) AND " +
            "(:endDate IS NULL OR r.refundDate <= :endDate)")
    Page<Refund> findByConditions(
            @Param("storeId") Long storeId,
            @Param("customerId") Long customerId,
            @Param("status") Refund.RefundStatus status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );

    @Query("SELECT r FROM Refund r LEFT JOIN FETCH r.refundItems WHERE r.id = :id")
    Optional<Refund> findByIdWithItems(@Param("id") Long id);
}
