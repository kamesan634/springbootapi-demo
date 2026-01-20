package com.kamesan.erpapi.inventory.repository;

import com.kamesan.erpapi.inventory.entity.StockTransfer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface StockTransferRepository extends JpaRepository<StockTransfer, Long> {

    Optional<StockTransfer> findByTransferNo(String transferNo);

    boolean existsByTransferNo(String transferNo);

    Page<StockTransfer> findByFromWarehouseId(Long fromWarehouseId, Pageable pageable);

    Page<StockTransfer> findByToWarehouseId(Long toWarehouseId, Pageable pageable);

    Page<StockTransfer> findByStatus(StockTransfer.TransferStatus status, Pageable pageable);

    @Query("SELECT s FROM StockTransfer s WHERE " +
            "(:fromWarehouseId IS NULL OR s.fromWarehouseId = :fromWarehouseId) AND " +
            "(:toWarehouseId IS NULL OR s.toWarehouseId = :toWarehouseId) AND " +
            "(:status IS NULL OR s.status = :status) AND " +
            "(:startDate IS NULL OR s.transferDate >= :startDate) AND " +
            "(:endDate IS NULL OR s.transferDate <= :endDate)")
    Page<StockTransfer> findByConditions(
            @Param("fromWarehouseId") Long fromWarehouseId,
            @Param("toWarehouseId") Long toWarehouseId,
            @Param("status") StockTransfer.TransferStatus status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );

    @Query("SELECT s FROM StockTransfer s LEFT JOIN FETCH s.items WHERE s.id = :id")
    Optional<StockTransfer> findByIdWithItems(@Param("id") Long id);
}
