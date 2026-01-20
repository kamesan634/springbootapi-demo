package com.kamesan.erpapi.sales.repository;

import com.kamesan.erpapi.sales.entity.CashierShift;
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
 * 收銀班次 Repository
 */
@Repository
public interface CashierShiftRepository extends JpaRepository<CashierShift, Long> {

    Optional<CashierShift> findByShiftNo(String shiftNo);

    boolean existsByShiftNo(String shiftNo);

    /**
     * 查詢收銀員目前開啟的班次
     */
    @Query("SELECT s FROM CashierShift s WHERE s.cashierId = :cashierId AND s.status = 'OPEN'")
    Optional<CashierShift> findOpenShiftByCashier(@Param("cashierId") Long cashierId);

    Page<CashierShift> findByStoreId(Long storeId, Pageable pageable);

    Page<CashierShift> findByCashierId(Long cashierId, Pageable pageable);

    Page<CashierShift> findByShiftDate(LocalDate shiftDate, Pageable pageable);

    Page<CashierShift> findByStatus(CashierShift.ShiftStatus status, Pageable pageable);

    @Query("SELECT s FROM CashierShift s WHERE " +
            "(:storeId IS NULL OR s.storeId = :storeId) AND " +
            "(:cashierId IS NULL OR s.cashierId = :cashierId) AND " +
            "(:status IS NULL OR s.status = :status) AND " +
            "(:startDate IS NULL OR s.shiftDate >= :startDate) AND " +
            "(:endDate IS NULL OR s.shiftDate <= :endDate)")
    Page<CashierShift> findByConditions(
            @Param("storeId") Long storeId,
            @Param("cashierId") Long cashierId,
            @Param("status") CashierShift.ShiftStatus status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );

    List<CashierShift> findByStoreIdAndShiftDate(Long storeId, LocalDate shiftDate);
}
