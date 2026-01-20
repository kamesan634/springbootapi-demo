package com.kamesan.erpapi.inventory.repository;

import com.kamesan.erpapi.inventory.entity.StockAdjustment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface StockAdjustmentRepository extends JpaRepository<StockAdjustment, Long> {

    Optional<StockAdjustment> findByAdjustmentNo(String adjustmentNo);

    boolean existsByAdjustmentNo(String adjustmentNo);

    Page<StockAdjustment> findByWarehouseId(Long warehouseId, Pageable pageable);

    Page<StockAdjustment> findByProductId(Long productId, Pageable pageable);

    Page<StockAdjustment> findByStatus(StockAdjustment.AdjustmentStatus status, Pageable pageable);

    @Query("SELECT s FROM StockAdjustment s WHERE " +
            "(:warehouseId IS NULL OR s.warehouseId = :warehouseId) AND " +
            "(:productId IS NULL OR s.productId = :productId) AND " +
            "(:status IS NULL OR s.status = :status) AND " +
            "(:adjustmentType IS NULL OR s.adjustmentType = :adjustmentType) AND " +
            "(:startDate IS NULL OR s.createdAt >= :startDate) AND " +
            "(:endDate IS NULL OR s.createdAt <= :endDate)")
    Page<StockAdjustment> findByConditions(
            @Param("warehouseId") Long warehouseId,
            @Param("productId") Long productId,
            @Param("status") StockAdjustment.AdjustmentStatus status,
            @Param("adjustmentType") StockAdjustment.AdjustmentType adjustmentType,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );
}
