package com.kamesan.erpapi.sales.controller;

import com.kamesan.erpapi.common.dto.ApiResponse;
import com.kamesan.erpapi.common.dto.PageResponse;
import com.kamesan.erpapi.sales.dto.CreateRefundRequest;
import com.kamesan.erpapi.sales.dto.RefundDto;
import com.kamesan.erpapi.sales.entity.Refund;
import com.kamesan.erpapi.sales.service.RefundService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

/**
 * 退貨單 Controller
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/refunds")
@RequiredArgsConstructor
@Tag(name = "退貨單", description = "退貨單管理 API")
public class RefundController {

    private final RefundService refundService;

    @PostMapping
    @Operation(summary = "建立退貨單", description = "建立新的退貨單")
    public ResponseEntity<ApiResponse<RefundDto>> createRefund(
            @Valid @RequestBody CreateRefundRequest request) {
        RefundDto refund = refundService.createRefund(request);
        return ResponseEntity.ok(ApiResponse.success("退貨單建立成功", refund));
    }

    @GetMapping("/{id}")
    @Operation(summary = "查詢退貨單", description = "根據 ID 查詢退貨單")
    public ResponseEntity<ApiResponse<RefundDto>> getRefundById(
            @Parameter(description = "退貨單 ID") @PathVariable Long id) {
        RefundDto refund = refundService.getRefundById(id);
        return ResponseEntity.ok(ApiResponse.success(refund));
    }

    @GetMapping("/no/{refundNo}")
    @Operation(summary = "根據編號查詢", description = "根據退貨單編號查詢")
    public ResponseEntity<ApiResponse<RefundDto>> getRefundByNo(
            @Parameter(description = "退貨單編號") @PathVariable String refundNo) {
        RefundDto refund = refundService.getRefundByNo(refundNo);
        return ResponseEntity.ok(ApiResponse.success(refund));
    }

    @GetMapping
    @Operation(summary = "查詢退貨單列表", description = "根據條件查詢退貨單（分頁）")
    public ResponseEntity<ApiResponse<PageResponse<RefundDto>>> queryRefunds(
            @Parameter(description = "門市 ID") @RequestParam(required = false) Long storeId,
            @Parameter(description = "客戶 ID") @RequestParam(required = false) Long customerId,
            @Parameter(description = "狀態") @RequestParam(required = false) Refund.RefundStatus status,
            @Parameter(description = "開始日期") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "結束日期") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<RefundDto> page = refundService.queryRefunds(storeId, customerId, status, startDate, endDate, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(page)));
    }

    @PostMapping("/{id}/approve")
    @Operation(summary = "核准退貨單", description = "核准待處理的退貨單")
    public ResponseEntity<ApiResponse<RefundDto>> approveRefund(
            @Parameter(description = "退貨單 ID") @PathVariable Long id,
            @RequestBody Map<String, Long> body) {
        Long approvedBy = body.get("approvedBy");
        RefundDto refund = refundService.approveRefund(id, approvedBy);
        return ResponseEntity.ok(ApiResponse.success("退貨單核准成功", refund));
    }

    @PostMapping("/{id}/complete")
    @Operation(summary = "完成退貨", description = "完成已核准的退貨單，並處理庫存回補")
    public ResponseEntity<ApiResponse<RefundDto>> completeRefund(
            @Parameter(description = "退貨單 ID") @PathVariable Long id,
            @RequestBody Map<String, Long> body) {
        Long operatorId = body.get("operatorId");
        RefundDto refund = refundService.completeRefund(id, operatorId);
        return ResponseEntity.ok(ApiResponse.success("退貨完成", refund));
    }

    @PostMapping("/{id}/reject")
    @Operation(summary = "拒絕退貨", description = "拒絕退貨申請")
    public ResponseEntity<ApiResponse<RefundDto>> rejectRefund(
            @Parameter(description = "退貨單 ID") @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String reason = body.get("reason");
        RefundDto refund = refundService.rejectRefund(id, reason);
        return ResponseEntity.ok(ApiResponse.success("退貨已拒絕", refund));
    }
}
