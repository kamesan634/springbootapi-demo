package com.kamesan.erpapi.sales.controller;

import com.kamesan.erpapi.common.dto.ApiResponse;
import com.kamesan.erpapi.common.dto.PageResponse;
import com.kamesan.erpapi.sales.dto.CashierShiftDto;
import com.kamesan.erpapi.sales.entity.CashierShift;
import com.kamesan.erpapi.sales.service.CashierShiftService;
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
 * 收銀班次 Controller
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/cashier-shifts")
@RequiredArgsConstructor
@Tag(name = "收銀班次", description = "收銀班次管理 API")
public class CashierShiftController {

    private final CashierShiftService cashierShiftService;

    @PostMapping("/open")
    @Operation(summary = "開班", description = "收銀員開始班次")
    public ResponseEntity<ApiResponse<CashierShiftDto>> openShift(@RequestBody Map<String, Object> body) {
        Long storeId = ((Number) body.get("storeId")).longValue();
        Long cashierId = ((Number) body.get("cashierId")).longValue();
        BigDecimal openingAmount = new BigDecimal(body.get("openingAmount").toString());

        CashierShiftDto shift = cashierShiftService.openShift(storeId, cashierId, openingAmount);
        return ResponseEntity.ok(ApiResponse.success("開班成功", shift));
    }

    @PostMapping("/{id}/close")
    @Operation(summary = "結班", description = "收銀員結束班次")
    public ResponseEntity<ApiResponse<CashierShiftDto>> closeShift(
            @Parameter(description = "班次 ID") @PathVariable Long id,
            @RequestBody Map<String, Object> body) {

        BigDecimal closingAmount = new BigDecimal(body.get("closingAmount").toString());
        String notes = (String) body.get("notes");

        CashierShiftDto shift = cashierShiftService.closeShift(id, closingAmount, notes);
        return ResponseEntity.ok(ApiResponse.success("結班成功", shift));
    }

    @GetMapping("/{id}")
    @Operation(summary = "查詢班次", description = "根據 ID 查詢班次")
    public ResponseEntity<ApiResponse<CashierShiftDto>> getShiftById(
            @Parameter(description = "班次 ID") @PathVariable Long id) {
        CashierShiftDto shift = cashierShiftService.getShiftById(id);
        return ResponseEntity.ok(ApiResponse.success(shift));
    }

    @GetMapping("/current/{cashierId}")
    @Operation(summary = "查詢目前班次", description = "查詢收銀員目前開啟的班次")
    public ResponseEntity<ApiResponse<CashierShiftDto>> getCurrentShift(
            @Parameter(description = "收銀員 ID") @PathVariable Long cashierId) {
        CashierShiftDto shift = cashierShiftService.getCurrentShift(cashierId);
        return ResponseEntity.ok(ApiResponse.success(shift));
    }

    @GetMapping
    @Operation(summary = "查詢班次列表", description = "根據條件查詢班次（分頁）")
    public ResponseEntity<ApiResponse<PageResponse<CashierShiftDto>>> queryShifts(
            @Parameter(description = "門市 ID") @RequestParam(required = false) Long storeId,
            @Parameter(description = "收銀員 ID") @RequestParam(required = false) Long cashierId,
            @Parameter(description = "狀態") @RequestParam(required = false) CashierShift.ShiftStatus status,
            @Parameter(description = "開始日期") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "結束日期") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<CashierShiftDto> page = cashierShiftService.queryShifts(storeId, cashierId, status, startDate, endDate, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(page)));
    }
}
