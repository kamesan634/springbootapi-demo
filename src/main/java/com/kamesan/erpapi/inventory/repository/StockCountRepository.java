package com.kamesan.erpapi.inventory.repository;

import com.kamesan.erpapi.inventory.entity.StockCount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface StockCountRepository extends JpaRepository<StockCount, Long> {

    Optional<StockCount> findByCountNo(String countNo);

    boolean existsByCountNo(String countNo);

    Page<StockCount> findByWarehouseId(Long warehouseId, Pageable pageable);

    Page<StockCount> findByStatus(StockCount.CountStatus status, Pageable pageable);

    @Query("SELECT s FROM StockCount s WHERE " +
            "(:warehouseId IS NULL OR s.warehouseId = :warehouseId) AND " +
            "(:status IS NULL OR s.status = :status) AND " +
            "(:startDate IS NULL OR s.countDate >= :startDate) AND " +
            "(:endDate IS NULL OR s.countDate <= :endDate)")
    Page<StockCount> findByConditions(
            @Param("warehouseId") Long warehouseId,
            @Param("status") StockCount.CountStatus status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );

    @Query("SELECT s FROM StockCount s LEFT JOIN FETCH s.items WHERE s.id = :id")
    Optional<StockCount> findByIdWithItems(@Param("id") Long id);
}
