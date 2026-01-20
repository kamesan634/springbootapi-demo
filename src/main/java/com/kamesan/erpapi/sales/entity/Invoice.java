package com.kamesan.erpapi.sales.entity;

import com.kamesan.erpapi.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 發票實體
 *
 * <p>記錄電子發票相關資訊</p>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Entity
@Table(name = "invoices", indexes = {
        @Index(name = "idx_invoices_no", columnList = "invoice_no"),
        @Index(name = "idx_invoices_order", columnList = "order_id"),
        @Index(name = "idx_invoices_store", columnList = "store_id"),
        @Index(name = "idx_invoices_date", columnList = "invoice_date"),
        @Index(name = "idx_invoices_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice extends BaseEntity {

    @Column(name = "invoice_no", nullable = false, unique = true, length = 20)
    private String invoiceNo;

    @Enumerated(EnumType.STRING)
    @Column(name = "invoice_type", nullable = false, length = 20)
    @Builder.Default
    private InvoiceType invoiceType = InvoiceType.B2C;

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "refund_id")
    private Long refundId;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(name = "invoice_date", nullable = false)
    private LocalDate invoiceDate;

    @Column(name = "buyer_tax_id", length = 20)
    private String buyerTaxId;

    @Column(name = "buyer_name", length = 100)
    private String buyerName;

    @Column(name = "sales_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal salesAmount;

    @Column(name = "tax_amount", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "total_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "carrier_type", length = 20)
    @Builder.Default
    private CarrierType carrierType = CarrierType.NONE;

    @Column(name = "carrier_no", length = 50)
    private String carrierNo;

    @Column(name = "donation_code", length = 10)
    private String donationCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private InvoiceStatus status = InvoiceStatus.ISSUED;

    @Column(name = "void_reason", length = 200)
    private String voidReason;

    @Column(name = "void_date")
    private LocalDate voidDate;

    @Column(name = "print_count")
    @Builder.Default
    private Integer printCount = 0;

    public void voidInvoice(String reason) {
        this.status = InvoiceStatus.VOID;
        this.voidReason = reason;
        this.voidDate = LocalDate.now();
    }

    public enum InvoiceType {
        B2C, B2B, DONATION
    }

    public enum CarrierType {
        MOBILE, NATURAL_PERSON, NONE
    }

    public enum InvoiceStatus {
        ISSUED, VOID, ALLOWANCE
    }
}
