package com.kamesan.erpapi.purchasing.repository;

import com.kamesan.erpapi.purchasing.entity.PurchaseReceipt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseReceiptRepository extends JpaRepository<PurchaseReceipt, Long> {

    Optional<PurchaseReceipt> findByReceiptNo(String receiptNo);

    boolean existsByReceiptNo(String receiptNo);

    List<PurchaseReceipt> findByPurchaseOrderId(Long purchaseOrderId);

    Page<PurchaseReceipt> findBySupplierId(Long supplierId, Pageable pageable);

    Page<PurchaseReceipt> findByStatus(PurchaseReceipt.ReceiptStatus status, Pageable pageable);

    @Query("SELECT p FROM PurchaseReceipt p WHERE " +
            "(:supplierId IS NULL OR p.supplierId = :supplierId) AND " +
            "(:warehouseId IS NULL OR p.warehouseId = :warehouseId) AND " +
            "(:status IS NULL OR p.status = :status) AND " +
            "(:startDate IS NULL OR p.receiptDate >= :startDate) AND " +
            "(:endDate IS NULL OR p.receiptDate <= :endDate)")
    Page<PurchaseReceipt> findByConditions(
            @Param("supplierId") Long supplierId,
            @Param("warehouseId") Long warehouseId,
            @Param("status") PurchaseReceipt.ReceiptStatus status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );

    @Query("SELECT p FROM PurchaseReceipt p LEFT JOIN FETCH p.items WHERE p.id = :id")
    Optional<PurchaseReceipt> findByIdWithItems(@Param("id") Long id);
}
