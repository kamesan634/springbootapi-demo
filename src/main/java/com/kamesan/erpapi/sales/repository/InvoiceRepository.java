package com.kamesan.erpapi.sales.repository;

import com.kamesan.erpapi.sales.entity.Invoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

/**
 * 發票 Repository
 */
@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    Optional<Invoice> findByInvoiceNo(String invoiceNo);

    boolean existsByInvoiceNo(String invoiceNo);

    Optional<Invoice> findByOrderId(Long orderId);

    Optional<Invoice> findByRefundId(Long refundId);

    Page<Invoice> findByStoreId(Long storeId, Pageable pageable);

    Page<Invoice> findByStatus(Invoice.InvoiceStatus status, Pageable pageable);

    @Query("SELECT i FROM Invoice i WHERE " +
            "(:storeId IS NULL OR i.storeId = :storeId) AND " +
            "(:status IS NULL OR i.status = :status) AND " +
            "(:invoiceType IS NULL OR i.invoiceType = :invoiceType) AND " +
            "(:startDate IS NULL OR i.invoiceDate >= :startDate) AND " +
            "(:endDate IS NULL OR i.invoiceDate <= :endDate)")
    Page<Invoice> findByConditions(
            @Param("storeId") Long storeId,
            @Param("status") Invoice.InvoiceStatus status,
            @Param("invoiceType") Invoice.InvoiceType invoiceType,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );

    Page<Invoice> findByBuyerTaxId(String buyerTaxId, Pageable pageable);
}
