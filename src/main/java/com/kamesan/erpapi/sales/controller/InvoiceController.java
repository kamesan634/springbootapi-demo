package com.kamesan.erpapi.sales.controller;

import com.kamesan.erpapi.common.dto.ApiResponse;
import com.kamesan.erpapi.common.dto.PageResponse;
import com.kamesan.erpapi.sales.dto.InvoiceDto;
import com.kamesan.erpapi.sales.entity.Invoice;
import com.kamesan.erpapi.sales.service.InvoiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/**
 * 發票 Controller
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/invoices")
@RequiredArgsConstructor
@Tag(name = "發票", description = "發票管理 API")
public class InvoiceController {

    private final InvoiceService invoiceService;

    @PostMapping
    @Operation(summary = "開立發票", description = "為訂單開立發票")
    public ResponseEntity<ApiResponse<InvoiceDto>> issueInvoice(@RequestBody Map<String, Object> body) {
        Long orderId = ((Number) body.get("orderId")).longValue();
        Long storeId = ((Number) body.get("storeId")).longValue();
        BigDecimal salesAmount = new BigDecimal(body.get("salesAmount").toString());
        BigDecimal taxAmount = body.get("taxAmount") != null ? new BigDecimal(body.get("taxAmount").toString()) : BigDecimal.ZERO;
        Invoice.InvoiceType invoiceType = body.get("invoiceType") != null ?
                Invoice.InvoiceType.valueOf(body.get("invoiceType").toString()) : Invoice.InvoiceType.B2C;
        String buyerTaxId = (String) body.get("buyerTaxId");
        String buyerName = (String) body.get("buyerName");
        Invoice.CarrierType carrierType = body.get("carrierType") != null ?
                Invoice.CarrierType.valueOf(body.get("carrierType").toString()) : Invoice.CarrierType.NONE;
        String carrierNo = (String) body.get("carrierNo");
        String donationCode = (String) body.get("donationCode");

        InvoiceDto invoice = invoiceService.issueInvoice(orderId, storeId, salesAmount, taxAmount,
                invoiceType, buyerTaxId, buyerName, carrierType, carrierNo, donationCode);
        return ResponseEntity.ok(ApiResponse.success("發票開立成功", invoice));
    }

    @GetMapping("/{id}")
    @Operation(summary = "查詢發票", description = "根據 ID 查詢發票")
    public ResponseEntity<ApiResponse<InvoiceDto>> getInvoiceById(
            @Parameter(description = "發票 ID") @PathVariable Long id) {
        InvoiceDto invoice = invoiceService.getInvoiceById(id);
        return ResponseEntity.ok(ApiResponse.success(invoice));
    }

    @GetMapping("/no/{invoiceNo}")
    @Operation(summary = "根據號碼查詢", description = "根據發票號碼查詢")
    public ResponseEntity<ApiResponse<InvoiceDto>> getInvoiceByNo(
            @Parameter(description = "發票號碼") @PathVariable String invoiceNo) {
        InvoiceDto invoice = invoiceService.getInvoiceByNo(invoiceNo);
        return ResponseEntity.ok(ApiResponse.success(invoice));
    }

    @GetMapping("/order/{orderId}")
    @Operation(summary = "根據訂單查詢", description = "根據訂單 ID 查詢發票")
    public ResponseEntity<ApiResponse<InvoiceDto>> getInvoiceByOrderId(
            @Parameter(description = "訂單 ID") @PathVariable Long orderId) {
        InvoiceDto invoice = invoiceService.getInvoiceByOrderId(orderId);
        return ResponseEntity.ok(ApiResponse.success(invoice));
    }

    @PostMapping("/{id}/void")
    @Operation(summary = "作廢發票", description = "作廢已開立的發票")
    public ResponseEntity<ApiResponse<InvoiceDto>> voidInvoice(
            @Parameter(description = "發票 ID") @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String reason = body.get("reason");
        InvoiceDto invoice = invoiceService.voidInvoice(id, reason);
        return ResponseEntity.ok(ApiResponse.success("發票已作廢", invoice));
    }

    @PostMapping("/{id}/print")
    @Operation(summary = "記錄列印", description = "記錄發票列印次數")
    public ResponseEntity<ApiResponse<Void>> recordPrint(
            @Parameter(description = "發票 ID") @PathVariable Long id) {
        invoiceService.recordPrint(id);
        return ResponseEntity.ok(ApiResponse.success("列印記錄已更新", null));
    }

    @GetMapping
    @Operation(summary = "查詢發票列表", description = "根據條件查詢發票（分頁）")
    public ResponseEntity<ApiResponse<PageResponse<InvoiceDto>>> queryInvoices(
            @Parameter(description = "門市 ID") @RequestParam(required = false) Long storeId,
            @Parameter(description = "狀態") @RequestParam(required = false) Invoice.InvoiceStatus status,
            @Parameter(description = "發票類型") @RequestParam(required = false) Invoice.InvoiceType invoiceType,
            @Parameter(description = "開始日期") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "結束日期") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<InvoiceDto> page = invoiceService.queryInvoices(storeId, status, invoiceType, startDate, endDate, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(page)));
    }
}
