package com.kamesan.erpapi.sales.controller;

import com.kamesan.erpapi.common.dto.ApiResponse;
import com.kamesan.erpapi.sales.dto.CreatePaymentRequest;
import com.kamesan.erpapi.sales.dto.PaymentDto;
import com.kamesan.erpapi.sales.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 付款控制器
 *
 * <p>處理付款相關的 API 請求，包括：</p>
 * <ul>
 *   <li>POST /api/v1/payments - 建立付款記錄（處理付款）</li>
 *   <li>GET /api/v1/payments/{id} - 查詢付款記錄詳情</li>
 *   <li>GET /api/v1/payments/order/{orderId} - 查詢訂單的付款記錄</li>
 *   <li>GET /api/v1/payments - 查詢付款記錄列表（依日期範圍）</li>
 * </ul>
 *
 * <p>付款處理流程：</p>
 * <ol>
 *   <li>驗證訂單狀態（必須為待處理）</li>
 *   <li>建立付款記錄</li>
 *   <li>檢查是否完全付款</li>
 *   <li>若完全付款則更新訂單狀態為已付款</li>
 * </ol>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "付款管理", description = "付款處理、付款記錄查詢等操作")
public class PaymentController {

    /**
     * 訂單服務
     * <p>付款處理由訂單服務統一管理</p>
     */
    private final OrderService orderService;

    /**
     * 處理付款
     *
     * <p>為指定訂單建立付款記錄。</p>
     * <p>當付款金額達到或超過訂單總金額時，訂單狀態會自動變更為已付款。</p>
     *
     * <p>支援的付款方式：</p>
     * <ul>
     *   <li>CASH - 現金</li>
     *   <li>CREDIT_CARD - 信用卡</li>
     *   <li>DEBIT_CARD - 金融卡</li>
     *   <li>LINE_PAY - LINE Pay</li>
     *   <li>APPLE_PAY - Apple Pay</li>
     * </ul>
     *
     * @param request 建立付款請求
     * @return 建立成功的付款記錄
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "處理付款", description = "為訂單建立付款記錄，完成付款後訂單狀態會自動更新")
    public ApiResponse<PaymentDto> processPayment(
            @Valid @RequestBody CreatePaymentRequest request) {

        log.info("收到付款請求，訂單 ID: {}, 付款方式: {}, 金額: {}",
                request.getOrderId(), request.getPaymentMethod(), request.getAmount());

        PaymentDto payment = orderService.processPayment(request);

        return ApiResponse.success("付款處理成功", payment);
    }

    /**
     * 根據 ID 查詢付款記錄
     *
     * @param id 付款記錄 ID
     * @return 付款記錄詳情
     */
    @GetMapping("/{id}")
    @Operation(summary = "查詢付款記錄", description = "根據付款記錄 ID 查詢詳細資訊")
    public ApiResponse<PaymentDto> getPaymentById(
            @Parameter(description = "付款記錄 ID", required = true) @PathVariable Long id) {

        log.debug("查詢付款記錄，付款 ID: {}", id);

        PaymentDto payment = orderService.getPaymentById(id);

        return ApiResponse.success(payment);
    }

    /**
     * 查詢訂單的付款記錄
     *
     * <p>查詢指定訂單的所有付款記錄，依付款時間正序排列。</p>
     *
     * @param orderId 訂單 ID
     * @return 付款記錄列表
     */
    @GetMapping("/order/{orderId}")
    @Operation(summary = "查詢訂單付款記錄", description = "查詢指定訂單的所有付款記錄")
    public ApiResponse<List<PaymentDto>> getPaymentsByOrder(
            @Parameter(description = "訂單 ID", required = true) @PathVariable Long orderId) {

        log.debug("查詢訂單付款記錄，訂單 ID: {}", orderId);

        List<PaymentDto> payments = orderService.getPaymentsByOrderId(orderId);

        return ApiResponse.success(payments);
    }

    /**
     * 查詢付款記錄列表（依日期範圍）
     *
     * <p>查詢指定日期範圍內的付款記錄，支援分頁。</p>
     *
     * @param startDate 開始日期時間
     * @param endDate   結束日期時間
     * @param pageable  分頁參數
     * @return 付款記錄分頁結果
     */
    @GetMapping
    @Operation(summary = "查詢付款記錄列表", description = "依日期範圍查詢付款記錄（分頁）")
    public ApiResponse<Page<PaymentDto>> getPayments(
            @Parameter(description = "開始日期時間", example = "2024-01-01T00:00:00")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "結束日期時間", example = "2024-01-31T23:59:59")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @ParameterObject @PageableDefault(size = 20, sort = "paymentDate", direction = Sort.Direction.DESC) Pageable pageable) {

        log.debug("查詢付款記錄，開始日期: {}, 結束日期: {}", startDate, endDate);

        Page<PaymentDto> payments = orderService.getPaymentsByDateRange(startDate, endDate, pageable);

        return ApiResponse.success(payments);
    }
}
