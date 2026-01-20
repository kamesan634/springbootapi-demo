package com.kamesan.erpapi.purchasing.repository;

import com.kamesan.erpapi.purchasing.entity.PurchaseOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {

    Optional<PurchaseOrder> findByPoNo(String poNo);

    boolean existsByPoNo(String poNo);

    Page<PurchaseOrder> findBySupplierId(Long supplierId, Pageable pageable);

    Page<PurchaseOrder> findByWarehouseId(Long warehouseId, Pageable pageable);

    Page<PurchaseOrder> findByStatus(PurchaseOrder.PurchaseOrderStatus status, Pageable pageable);

    @Query("SELECT p FROM PurchaseOrder p WHERE " +
            "(:supplierId IS NULL OR p.supplierId = :supplierId) AND " +
            "(:warehouseId IS NULL OR p.warehouseId = :warehouseId) AND " +
            "(:status IS NULL OR p.status = :status) AND " +
            "(:startDate IS NULL OR p.orderDate >= :startDate) AND " +
            "(:endDate IS NULL OR p.orderDate <= :endDate)")
    Page<PurchaseOrder> findByConditions(
            @Param("supplierId") Long supplierId,
            @Param("warehouseId") Long warehouseId,
            @Param("status") PurchaseOrder.PurchaseOrderStatus status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );

    @Query("SELECT p FROM PurchaseOrder p LEFT JOIN FETCH p.items WHERE p.id = :id")
    Optional<PurchaseOrder> findByIdWithItems(@Param("id") Long id);
}
