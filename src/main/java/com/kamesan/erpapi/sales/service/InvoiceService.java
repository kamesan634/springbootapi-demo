package com.kamesan.erpapi.sales.service;

import com.kamesan.erpapi.common.exception.BusinessException;
import com.kamesan.erpapi.sales.dto.InvoiceDto;
import com.kamesan.erpapi.sales.entity.Invoice;
import com.kamesan.erpapi.sales.repository.InvoiceRepository;
import com.kamesan.erpapi.system.service.NumberRuleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 發票服務
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final NumberRuleService numberRuleService;

    /**
     * 開立發票
     */
    @Transactional
    public InvoiceDto issueInvoice(Long orderId, Long storeId, BigDecimal salesAmount, BigDecimal taxAmount,
                                    Invoice.InvoiceType invoiceType, String buyerTaxId, String buyerName,
                                    Invoice.CarrierType carrierType, String carrierNo, String donationCode) {
        log.info("開立發票，訂單 ID: {}", orderId);

        // 檢查訂單是否已開過發票
        invoiceRepository.findByOrderId(orderId).ifPresent(invoice -> {
            throw BusinessException.alreadyExists("發票", "訂單 ID", orderId.toString());
        });

        // 產生發票號碼
        String invoiceNo = numberRuleService.generateNextNumber("INVOICE");

        // 建立發票
        Invoice invoice = Invoice.builder()
                .invoiceNo(invoiceNo)
                .invoiceType(invoiceType)
                .orderId(orderId)
                .storeId(storeId)
                .invoiceDate(LocalDate.now())
                .buyerTaxId(buyerTaxId)
                .buyerName(buyerName)
                .salesAmount(salesAmount)
                .taxAmount(taxAmount)
                .totalAmount(salesAmount.add(taxAmount))
                .carrierType(carrierType)
                .carrierNo(carrierNo)
                .donationCode(donationCode)
                .build();

        Invoice saved = invoiceRepository.save(invoice);
        log.info("發票開立成功，發票號碼: {}", saved.getInvoiceNo());

        return convertToDto(saved);
    }

    /**
     * 作廢發票
     */
    @Transactional
    public InvoiceDto voidInvoice(Long id, String reason) {
        log.info("作廢發票，ID: {}", id);

        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("發票", id));

        if (invoice.getStatus() != Invoice.InvoiceStatus.ISSUED) {
            throw BusinessException.validationFailed("發票狀態為 " + invoice.getStatus() + "，無法作廢");
        }

        invoice.voidInvoice(reason);
        Invoice saved = invoiceRepository.save(invoice);

        log.info("發票已作廢，發票號碼: {}", saved.getInvoiceNo());

        return convertToDto(saved);
    }

    /**
     * 根據 ID 查詢發票
     */
    @Transactional(readOnly = true)
    public InvoiceDto getInvoiceById(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("發票", id));
        return convertToDto(invoice);
    }

    /**
     * 根據發票號碼查詢發票
     */
    @Transactional(readOnly = true)
    public InvoiceDto getInvoiceByNo(String invoiceNo) {
        Invoice invoice = invoiceRepository.findByInvoiceNo(invoiceNo)
                .orElseThrow(() -> new BusinessException(404, "發票號碼 " + invoiceNo + " 不存在"));
        return convertToDto(invoice);
    }

    /**
     * 根據訂單 ID 查詢發票
     */
    @Transactional(readOnly = true)
    public InvoiceDto getInvoiceByOrderId(Long orderId) {
        Invoice invoice = invoiceRepository.findByOrderId(orderId)
                .orElseThrow(() -> new BusinessException(404, "訂單 " + orderId + " 尚未開立發票"));
        return convertToDto(invoice);
    }

    /**
     * 記錄列印次數
     */
    @Transactional
    public void recordPrint(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("發票", id));

        invoice.setPrintCount(invoice.getPrintCount() + 1);
        invoiceRepository.save(invoice);
    }

    /**
     * 查詢發票（分頁）
     */
    @Transactional(readOnly = true)
    public Page<InvoiceDto> queryInvoices(Long storeId, Invoice.InvoiceStatus status, Invoice.InvoiceType invoiceType,
                                           LocalDate startDate, LocalDate endDate, Pageable pageable) {
        return invoiceRepository.findByConditions(storeId, status, invoiceType, startDate, endDate, pageable)
                .map(this::convertToDto);
    }

    private InvoiceDto convertToDto(Invoice invoice) {
        return InvoiceDto.builder()
                .id(invoice.getId())
                .invoiceNo(invoice.getInvoiceNo())
                .invoiceType(invoice.getInvoiceType())
                .orderId(invoice.getOrderId())
                .refundId(invoice.getRefundId())
                .storeId(invoice.getStoreId())
                .invoiceDate(invoice.getInvoiceDate())
                .buyerTaxId(invoice.getBuyerTaxId())
                .buyerName(invoice.getBuyerName())
                .salesAmount(invoice.getSalesAmount())
                .taxAmount(invoice.getTaxAmount())
                .totalAmount(invoice.getTotalAmount())
                .carrierType(invoice.getCarrierType())
                .carrierNo(invoice.getCarrierNo())
                .donationCode(invoice.getDonationCode())
                .status(invoice.getStatus())
                .voidReason(invoice.getVoidReason())
                .voidDate(invoice.getVoidDate())
                .printCount(invoice.getPrintCount())
                .createdAt(invoice.getCreatedAt())
                .updatedAt(invoice.getUpdatedAt())
                .build();
    }
}
