package com.kamesan.erpapi.sales.dto;

import com.kamesan.erpapi.sales.entity.Invoice;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 發票 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceDto {
    private Long id;
    private String invoiceNo;
    private Invoice.InvoiceType invoiceType;
    private Long orderId;
    private Long refundId;
    private Long storeId;
    private LocalDate invoiceDate;
    private String buyerTaxId;
    private String buyerName;
    private BigDecimal salesAmount;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private Invoice.CarrierType carrierType;
    private String carrierNo;
    private String donationCode;
    private Invoice.InvoiceStatus status;
    private String voidReason;
    private LocalDate voidDate;
    private Integer printCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
