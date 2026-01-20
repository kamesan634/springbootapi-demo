package com.kamesan.erpapi.sales.controller;

import com.kamesan.erpapi.common.dto.ApiResponse;
import com.kamesan.erpapi.sales.dto.*;
import com.kamesan.erpapi.sales.entity.OrderStatus;
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
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 訂單控制器
 *
 * <p>處理訂單相關的 API 請求，包括：</p>
 * <ul>
 *   <li>POST /api/v1/orders - 建立新訂單</li>
 *   <li>GET /api/v1/orders - 查詢訂單列表（分頁）</li>
 *   <li>GET /api/v1/orders/{id} - 查詢訂單詳情</li>
 *   <li>PUT /api/v1/orders/{id} - 更新訂單</li>
 *   <li>DELETE /api/v1/orders/{id} - 刪除訂單</li>
 *   <li>POST /api/v1/orders/{id}/cancel - 取消訂單</li>
 *   <li>POST /api/v1/orders/{id}/refund - 申請退款</li>
 *   <li>GET /api/v1/orders/{id}/payments - 查詢訂單付款記錄</li>
 * </ul>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "訂單管理", description = "訂單 CRUD、狀態管理、查詢等操作")
public class OrderController {

    /**
     * 訂單服務
     */
    private final OrderService orderService;

    /**
     * 建立新訂單
     *
     * <p>建立一筆新的銷售訂單，包含訂單明細項目。</p>
     * <p>訂單編號由系統自動產生。</p>
     *
     * @param request 建立訂單請求
     * @return 建立成功的訂單資料
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "建立新訂單", description = "建立一筆新的銷售訂單，包含訂單明細項目")
    public ApiResponse<OrderDto> createOrder(
            @Valid @RequestBody CreateOrderRequest request) {

        log.info("收到建立訂單請求，門市 ID: {}", request.getStoreId());

        OrderDto order = orderService.createOrder(request);

        return ApiResponse.success("訂單建立成功", order);
    }

    /**
     * 查詢訂單列表（分頁）
     *
     * <p>支援多條件組合查詢，包括：</p>
     * <ul>
     *   <li>訂單編號模糊查詢</li>
     *   <li>門市篩選</li>
     *   <li>客戶篩選</li>
     *   <li>狀態篩選</li>
     *   <li>日期範圍篩選</li>
     * </ul>
     *
     * @param queryRequest 查詢條件
     * @param pageable     分頁參數
     * @return 訂單分頁結果
     */
    @GetMapping
    @Operation(summary = "查詢訂單列表", description = "分頁查詢訂單，支援多條件篩選")
    public ApiResponse<Page<OrderDto>> getOrders(
            @ParameterObject OrderQueryRequest queryRequest,
            @ParameterObject @PageableDefault(size = 20, sort = "orderDate", direction = Sort.Direction.DESC) Pageable pageable) {

        log.debug("查詢訂單列表，查詢條件: {}", queryRequest);

        Page<OrderDto> orders = orderService.queryOrders(queryRequest, pageable);

        return ApiResponse.success(orders);
    }

    /**
     * 根據 ID 查詢訂單詳情
     *
     * <p>查詢訂單完整資訊，包含訂單明細和付款記錄。</p>
     *
     * @param id 訂單 ID
     * @return 訂單詳情
     */
    @GetMapping("/{id}")
    @Operation(summary = "查詢訂單詳情", description = "根據訂單 ID 查詢完整訂單資訊")
    public ApiResponse<OrderDto> getOrderById(
            @Parameter(description = "訂單 ID", required = true) @PathVariable Long id) {

        log.debug("查詢訂單詳情，訂單 ID: {}", id);

        OrderDto order = orderService.getOrderById(id);

        return ApiResponse.success(order);
    }

    /**
     * 根據訂單編號查詢訂單
     *
     * @param orderNo 訂單編號
     * @return 訂單詳情
     */
    @GetMapping("/by-order-no/{orderNo}")
    @Operation(summary = "根據訂單編號查詢", description = "根據訂單編號查詢訂單資訊")
    public ApiResponse<OrderDto> getOrderByOrderNo(
            @Parameter(description = "訂單編號", required = true) @PathVariable String orderNo) {

        log.debug("根據訂單編號查詢，訂單編號: {}", orderNo);

        OrderDto order = orderService.getOrderByOrderNo(orderNo);

        return ApiResponse.success(order);
    }

    /**
     * 更新訂單
     *
     * <p>更新訂單資訊，可更新欄位包括：</p>
     * <ul>
     *   <li>客戶 ID</li>
     *   <li>折扣金額</li>
     *   <li>稅額</li>
     *   <li>訂單狀態</li>
     *   <li>備註</li>
     * </ul>
     *
     * @param id      訂單 ID
     * @param request 更新訂單請求
     * @return 更新後的訂單資料
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新訂單", description = "更新訂單資訊（客戶、金額、狀態、備註等）")
    public ApiResponse<OrderDto> updateOrder(
            @Parameter(description = "訂單 ID", required = true) @PathVariable Long id,
            @Valid @RequestBody UpdateOrderRequest request) {

        log.info("更新訂單，訂單 ID: {}", id);

        OrderDto order = orderService.updateOrder(id, request);

        return ApiResponse.success("訂單更新成功", order);
    }

    /**
     * 刪除訂單
     *
     * <p>刪除訂單及其明細。只有待處理狀態的訂單可以刪除。</p>
     *
     * @param id 訂單 ID
     * @return 刪除結果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "刪除訂單", description = "刪除訂單（只有待處理狀態可刪除）")
    public ApiResponse<Void> deleteOrder(
            @Parameter(description = "訂單 ID", required = true) @PathVariable Long id) {

        log.info("刪除訂單，訂單 ID: {}", id);

        orderService.deleteOrder(id);

        return ApiResponse.success("訂單刪除成功", null);
    }

    /**
     * 取消訂單
     *
     * <p>將訂單狀態變更為已取消。只有待處理狀態的訂單可以取消。</p>
     *
     * @param id 訂單 ID
     * @return 取消後的訂單資料
     */
    @PostMapping("/{id}/cancel")
    @Operation(summary = "取消訂單", description = "取消訂單（只有待處理狀態可取消）")
    public ApiResponse<OrderDto> cancelOrder(
            @Parameter(description = "訂單 ID", required = true) @PathVariable Long id) {

        log.info("取消訂單，訂單 ID: {}", id);

        OrderDto order = orderService.cancelOrder(id);

        return ApiResponse.success("訂單取消成功", order);
    }

    /**
     * 申請退款
     *
     * <p>將訂單狀態變更為已退款。只有已付款狀態的訂單可以退款。</p>
     *
     * @param id 訂單 ID
     * @return 退款後的訂單資料
     */
    @PostMapping("/{id}/refund")
    @Operation(summary = "申請退款", description = "申請訂單退款（只有已付款狀態可退款）")
    public ApiResponse<OrderDto> refundOrder(
            @Parameter(description = "訂單 ID", required = true) @PathVariable Long id) {

        log.info("申請退款，訂單 ID: {}", id);

        OrderDto order = orderService.refundOrder(id);

        return ApiResponse.success("退款申請成功", order);
    }

    /**
     * 查詢訂單的付款記錄
     *
     * @param id 訂單 ID
     * @return 付款記錄列表
     */
    @GetMapping("/{id}/payments")
    @Operation(summary = "查詢訂單付款記錄", description = "查詢指定訂單的所有付款記錄")
    public ApiResponse<List<PaymentDto>> getOrderPayments(
            @Parameter(description = "訂單 ID", required = true) @PathVariable Long id) {

        log.debug("查詢訂單付款記錄，訂單 ID: {}", id);

        List<PaymentDto> payments = orderService.getPaymentsByOrderId(id);

        return ApiResponse.success(payments);
    }

    /**
     * 根據門市查詢訂單
     *
     * @param storeId  門市 ID
     * @param pageable 分頁參數
     * @return 訂單分頁結果
     */
    @GetMapping("/store/{storeId}")
    @Operation(summary = "根據門市查詢訂單", description = "查詢指定門市的所有訂單")
    public ApiResponse<Page<OrderDto>> getOrdersByStore(
            @Parameter(description = "門市 ID", required = true) @PathVariable Long storeId,
            @ParameterObject @PageableDefault(size = 20, sort = "orderDate", direction = Sort.Direction.DESC) Pageable pageable) {

        log.debug("根據門市查詢訂單，門市 ID: {}", storeId);

        Page<OrderDto> orders = orderService.getOrdersByStoreId(storeId, pageable);

        return ApiResponse.success(orders);
    }

    /**
     * 根據客戶查詢訂單
     *
     * @param customerId 客戶 ID
     * @param pageable   分頁參數
     * @return 訂單分頁結果
     */
    @GetMapping("/customer/{customerId}")
    @Operation(summary = "根據客戶查詢訂單", description = "查詢指定客戶的所有訂單")
    public ApiResponse<Page<OrderDto>> getOrdersByCustomer(
            @Parameter(description = "客戶 ID", required = true) @PathVariable Long customerId,
            @ParameterObject @PageableDefault(size = 20, sort = "orderDate", direction = Sort.Direction.DESC) Pageable pageable) {

        log.debug("根據客戶查詢訂單，客戶 ID: {}", customerId);

        Page<OrderDto> orders = orderService.getOrdersByCustomerId(customerId, pageable);

        return ApiResponse.success(orders);
    }

    /**
     * 根據狀態查詢訂單
     *
     * @param status   訂單狀態
     * @param pageable 分頁參數
     * @return 訂單分頁結果
     */
    @GetMapping("/status/{status}")
    @Operation(summary = "根據狀態查詢訂單", description = "查詢指定狀態的所有訂單")
    public ApiResponse<Page<OrderDto>> getOrdersByStatus(
            @Parameter(description = "訂單狀態", required = true) @PathVariable OrderStatus status,
            @ParameterObject @PageableDefault(size = 20, sort = "orderDate", direction = Sort.Direction.DESC) Pageable pageable) {

        log.debug("根據狀態查詢訂單，狀態: {}", status);

        Page<OrderDto> orders = orderService.getOrdersByStatus(status, pageable);

        return ApiResponse.success(orders);
    }
}
