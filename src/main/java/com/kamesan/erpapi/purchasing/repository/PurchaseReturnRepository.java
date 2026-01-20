package com.kamesan.erpapi.purchasing.repository;

import com.kamesan.erpapi.purchasing.entity.PurchaseReturn;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface PurchaseReturnRepository extends JpaRepository<PurchaseReturn, Long> {

    Optional<PurchaseReturn> findByReturnNo(String returnNo);

    boolean existsByReturnNo(String returnNo);

    Page<PurchaseReturn> findBySupplierId(Long supplierId, Pageable pageable);

    Page<PurchaseReturn> findByStatus(PurchaseReturn.ReturnStatus status, Pageable pageable);

    @Query("SELECT p FROM PurchaseReturn p WHERE " +
            "(:supplierId IS NULL OR p.supplierId = :supplierId) AND " +
            "(:warehouseId IS NULL OR p.warehouseId = :warehouseId) AND " +
            "(:status IS NULL OR p.status = :status) AND " +
            "(:startDate IS NULL OR p.returnDate >= :startDate) AND " +
            "(:endDate IS NULL OR p.returnDate <= :endDate)")
    Page<PurchaseReturn> findByConditions(
            @Param("supplierId") Long supplierId,
            @Param("warehouseId") Long warehouseId,
            @Param("status") PurchaseReturn.ReturnStatus status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );

    @Query("SELECT p FROM PurchaseReturn p LEFT JOIN FETCH p.items WHERE p.id = :id")
    Optional<PurchaseReturn> findByIdWithItems(@Param("id") Long id);
}
