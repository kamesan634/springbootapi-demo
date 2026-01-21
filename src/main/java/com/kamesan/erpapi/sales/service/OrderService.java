package com.kamesan.erpapi.sales.service;

import com.kamesan.erpapi.accounts.repository.StoreRepository;
import com.kamesan.erpapi.common.exception.BusinessException;
import com.kamesan.erpapi.customers.repository.CustomerRepository;
import com.kamesan.erpapi.products.repository.ProductRepository;
import com.kamesan.erpapi.sales.dto.*;
import com.kamesan.erpapi.sales.entity.*;
import com.kamesan.erpapi.sales.repository.OrderItemRepository;
import com.kamesan.erpapi.sales.repository.OrderRepository;
import com.kamesan.erpapi.sales.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 訂單服務
 *
 * <p>提供訂單相關的業務邏輯處理，包括：</p>
 * <ul>
 *   <li>訂單 CRUD 操作</li>
 *   <li>訂單狀態管理</li>
 *   <li>付款處理</li>
 *   <li>訂單查詢</li>
 * </ul>
 *
 * <p>業務規則：</p>
 * <ul>
 *   <li>訂單建立時自動計算金額</li>
 *   <li>訂單編號自動產生，格式：ORD + 年月日時分秒 + 流水號</li>
 *   <li>訂單狀態轉換需符合規則</li>
 *   <li>已付款訂單不可取消，需走退款流程</li>
 * </ul>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    /**
     * 訂單 Repository
     */
    private final OrderRepository orderRepository;

    /**
     * 訂單明細 Repository
     */
    private final OrderItemRepository orderItemRepository;

    /**
     * 付款記錄 Repository
     */
    private final PaymentRepository paymentRepository;

    /**
     * 門市 Repository
     */
    private final StoreRepository storeRepository;

    /**
     * 客戶 Repository
     */
    private final CustomerRepository customerRepository;

    /**
     * 商品 Repository
     */
    private final ProductRepository productRepository;

    /**
     * 訂單編號流水號計數器
     * <p>用於產生唯一的訂單編號</p>
     */
    private final AtomicLong orderSequence = new AtomicLong(0);

    // ==================== 訂單 CRUD 操作 ====================

    /**
     * 建立新訂單
     *
     * <p>處理流程：</p>
     * <ol>
     *   <li>產生唯一訂單編號</li>
     *   <li>建立訂單明細項目</li>
     *   <li>計算訂單金額</li>
     *   <li>儲存訂單及明細</li>
     * </ol>
     *
     * @param request 建立訂單請求
     * @return 建立完成的訂單 DTO
     */
    @Transactional
    public OrderDto createOrder(CreateOrderRequest request) {
        log.info("建立新訂單，門市 ID: {}, 客戶 ID: {}", request.getStoreId(), request.getCustomerId());

        // 產生訂單編號
        String orderNo = generateOrderNo();

        // 建立訂單
        LocalDateTime now = LocalDateTime.now();
        Order order = Order.builder()
                .orderNo(orderNo)
                .storeId(request.getStoreId())
                .customerId(request.getCustomerId())
                .orderDate(now.toLocalDate())
                .orderTime(now.toLocalTime())
                .discountAmount(request.getDiscountAmount() != null ? request.getDiscountAmount() : BigDecimal.ZERO)
                .taxAmount(request.getTaxAmount() != null ? request.getTaxAmount() : BigDecimal.ZERO)
                .status(OrderStatus.PENDING)
                .notes(request.getNotes())
                .subtotal(BigDecimal.ZERO)
                .totalAmount(BigDecimal.ZERO)
                .build();

        // 建立訂單明細
        for (CreateOrderRequest.CreateOrderItemRequest itemRequest : request.getItems()) {
            OrderItem item = OrderItem.create(
                    itemRequest.getProductId(),
                    itemRequest.getQuantity(),
                    itemRequest.getUnitPrice(),
                    itemRequest.getDiscountAmount()
            );
            order.addOrderItem(item);
        }

        // 計算訂單金額
        order.calculateTotals();

        // 儲存訂單
        Order savedOrder = orderRepository.save(order);

        log.info("訂單建立成功，訂單編號: {}, 總金額: {}", savedOrder.getOrderNo(), savedOrder.getTotalAmount());

        return convertToDto(savedOrder);
    }

    /**
     * 根據 ID 查詢訂單
     *
     * @param id 訂單 ID
     * @return 訂單 DTO
     * @throws BusinessException 當訂單不存在時拋出
     */
    @Transactional(readOnly = true)
    public OrderDto getOrderById(Long id) {
        Order order = orderRepository.findByIdWithAllDetails(id)
                .orElseThrow(() -> BusinessException.notFound("訂單", id));
        return convertToDto(order);
    }

    /**
     * 根據訂單編號查詢訂單
     *
     * @param orderNo 訂單編號
     * @return 訂單 DTO
     * @throws BusinessException 當訂單不存在時拋出
     */
    @Transactional(readOnly = true)
    public OrderDto getOrderByOrderNo(String orderNo) {
        Order order = orderRepository.findByOrderNo(orderNo)
                .orElseThrow(() -> new BusinessException(404, "訂單編號 " + orderNo + " 不存在"));
        return convertToDto(order);
    }

    /**
     * 更新訂單
     *
     * <p>可更新欄位：客戶 ID、折扣金額、稅額、備註</p>
     * <p>更新金額後會重新計算總金額</p>
     *
     * @param id      訂單 ID
     * @param request 更新訂單請求
     * @return 更新後的訂單 DTO
     * @throws BusinessException 當訂單不存在或狀態不允許更新時拋出
     */
    @Transactional
    public OrderDto updateOrder(Long id, UpdateOrderRequest request) {
        log.info("更新訂單，訂單 ID: {}", id);

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("訂單", id));

        // 檢查訂單狀態（已取消或已退款的訂單不可更新）
        if (order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.REFUNDED) {
            throw BusinessException.validationFailed("訂單狀態為 " + order.getStatus().getDescription() + "，無法更新");
        }

        // 更新欄位
        if (request.getCustomerId() != null) {
            order.setCustomerId(request.getCustomerId());
        }
        if (request.getDiscountAmount() != null) {
            order.setDiscountAmount(request.getDiscountAmount());
        }
        if (request.getTaxAmount() != null) {
            order.setTaxAmount(request.getTaxAmount());
        }
        if (request.getNotes() != null) {
            order.setNotes(request.getNotes());
        }

        // 如果有更新金額相關欄位，重新計算總金額
        if (request.getDiscountAmount() != null || request.getTaxAmount() != null) {
            order.calculateTotals();
        }

        // 處理狀態更新
        if (request.getStatus() != null && request.getStatus() != order.getStatus()) {
            updateOrderStatus(order, request.getStatus());
        }

        Order savedOrder = orderRepository.save(order);

        log.info("訂單更新成功，訂單編號: {}", savedOrder.getOrderNo());

        return convertToDto(savedOrder);
    }

    /**
     * 刪除訂單
     *
     * <p>只有待處理狀態的訂單可以刪除</p>
     *
     * @param id 訂單 ID
     * @throws BusinessException 當訂單不存在或狀態不允許刪除時拋出
     */
    @Transactional
    public void deleteOrder(Long id) {
        log.info("刪除訂單，訂單 ID: {}", id);

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("訂單", id));

        // 只有待處理狀態的訂單可以刪除
        if (order.getStatus() != OrderStatus.PENDING) {
            throw BusinessException.validationFailed("訂單狀態為 " + order.getStatus().getDescription() + "，無法刪除");
        }

        orderRepository.delete(order);

        log.info("訂單刪除成功，訂單編號: {}", order.getOrderNo());
    }

    // ==================== 訂單狀態管理 ====================

    /**
     * 取消訂單
     *
     * <p>只有待處理狀態的訂單可以取消</p>
     *
     * @param id 訂單 ID
     * @return 取消後的訂單 DTO
     * @throws BusinessException 當訂單不存在或狀態不允許取消時拋出
     */
    @Transactional
    public OrderDto cancelOrder(Long id) {
        log.info("取消訂單，訂單 ID: {}", id);

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("訂單", id));

        if (!order.isCancellable()) {
            throw BusinessException.validationFailed("訂單狀態為 " + order.getStatus().getDescription() + "，無法取消");
        }

        order.cancel();
        Order savedOrder = orderRepository.save(order);

        log.info("訂單取消成功，訂單編號: {}", savedOrder.getOrderNo());

        return convertToDto(savedOrder);
    }

    /**
     * 申請退款
     *
     * <p>只有已付款狀態的訂單可以退款</p>
     *
     * @param id 訂單 ID
     * @return 退款後的訂單 DTO
     * @throws BusinessException 當訂單不存在或狀態不允許退款時拋出
     */
    @Transactional
    public OrderDto refundOrder(Long id) {
        log.info("申請退款，訂單 ID: {}", id);

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("訂單", id));

        if (!order.isRefundable()) {
            throw BusinessException.validationFailed("訂單狀態為 " + order.getStatus().getDescription() + "，無法退款");
        }

        order.markAsRefunded();
        Order savedOrder = orderRepository.save(order);

        log.info("訂單退款成功，訂單編號: {}", savedOrder.getOrderNo());

        return convertToDto(savedOrder);
    }

    // ==================== 付款處理 ====================

    /**
     * 處理付款
     *
     * <p>處理流程：</p>
     * <ol>
     *   <li>驗證訂單狀態（必須為待處理）</li>
     *   <li>建立付款記錄</li>
     *   <li>檢查是否完全付款</li>
     *   <li>更新訂單狀態為已付款</li>
     * </ol>
     *
     * @param request 建立付款請求
     * @return 付款記錄 DTO
     * @throws BusinessException 當訂單不存在或狀態不允許付款時拋出
     */
    @Transactional
    public PaymentDto processPayment(CreatePaymentRequest request) {
        log.info("處理付款，訂單 ID: {}, 付款方式: {}, 金額: {}",
                request.getOrderId(), request.getPaymentMethod(), request.getAmount());

        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> BusinessException.notFound("訂單", request.getOrderId()));

        // 檢查訂單狀態
        if (order.getStatus() != OrderStatus.PENDING) {
            throw BusinessException.validationFailed("訂單狀態為 " + order.getStatus().getDescription() + "，無法付款");
        }

        // 檢查交易參考編號是否重複
        if (request.getReferenceNo() != null && !request.getReferenceNo().isEmpty()) {
            if (paymentRepository.existsByReferenceNo(request.getReferenceNo())) {
                throw BusinessException.alreadyExists("付款記錄", "交易參考編號", request.getReferenceNo());
            }
        }

        // 建立付款記錄
        LocalDateTime paymentNow = LocalDateTime.now();
        Payment payment = Payment.builder()
                .order(order)
                .paymentMethod(request.getPaymentMethod())
                .amount(request.getAmount())
                .paymentDate(paymentNow.toLocalDate())
                .paymentTime(paymentNow.toLocalTime())
                .referenceNo(request.getReferenceNo())
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        // 檢查是否完全付款
        BigDecimal totalPaid = paymentRepository.sumAmountByOrderId(order.getId());
        if (totalPaid.compareTo(order.getTotalAmount()) >= 0) {
            order.markAsPaid();
            orderRepository.save(order);
            log.info("訂單已完全付款，訂單編號: {}", order.getOrderNo());
        }

        log.info("付款處理成功，付款 ID: {}", savedPayment.getId());

        return convertPaymentToDto(savedPayment);
    }

    /**
     * 根據訂單 ID 查詢付款記錄
     *
     * @param orderId 訂單 ID
     * @return 付款記錄列表
     */
    @Transactional(readOnly = true)
    public List<PaymentDto> getPaymentsByOrderId(Long orderId) {
        // 確認訂單存在
        if (!orderRepository.existsById(orderId)) {
            throw BusinessException.notFound("訂單", orderId);
        }

        List<Payment> payments = paymentRepository.findByOrderIdOrderByPaymentDateAsc(orderId);
        return payments.stream()
                .map(this::convertPaymentToDto)
                .collect(Collectors.toList());
    }

    /**
     * 根據付款 ID 查詢付款記錄
     *
     * @param paymentId 付款記錄 ID
     * @return 付款記錄 DTO
     * @throws BusinessException 當付款記錄不存在時拋出
     */
    @Transactional(readOnly = true)
    public PaymentDto getPaymentById(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> BusinessException.notFound("付款記錄", paymentId));
        return convertPaymentToDto(payment);
    }

    // ==================== 訂單查詢 ====================

    /**
     * 分頁查詢訂單
     *
     * <p>支援多條件組合查詢</p>
     *
     * @param queryRequest 查詢條件
     * @param pageable     分頁參數
     * @return 訂單分頁結果
     */
    @Transactional(readOnly = true)
    public Page<OrderDto> queryOrders(OrderQueryRequest queryRequest, Pageable pageable) {
        Page<Order> orderPage;

        // 如果有訂單編號關鍵字，使用模糊查詢
        if (queryRequest.getOrderNoKeyword() != null && !queryRequest.getOrderNoKeyword().isEmpty()) {
            orderPage = orderRepository.findByOrderNoContaining(queryRequest.getOrderNoKeyword(), pageable);
        } else {
            // 使用綜合條件查詢
            orderPage = orderRepository.findByConditions(
                    queryRequest.getStoreId(),
                    queryRequest.getCustomerId(),
                    queryRequest.getStatus(),
                    queryRequest.getStartDate(),
                    queryRequest.getEndDate(),
                    pageable
            );
        }

        return orderPage.map(this::convertToDto);
    }

    /**
     * 根據門市查詢訂單
     *
     * @param storeId  門市 ID
     * @param pageable 分頁參數
     * @return 訂單分頁結果
     */
    @Transactional(readOnly = true)
    public Page<OrderDto> getOrdersByStoreId(Long storeId, Pageable pageable) {
        return orderRepository.findByStoreId(storeId, pageable)
                .map(this::convertToDto);
    }

    /**
     * 根據客戶查詢訂單
     *
     * @param customerId 客戶 ID
     * @param pageable   分頁參數
     * @return 訂單分頁結果
     */
    @Transactional(readOnly = true)
    public Page<OrderDto> getOrdersByCustomerId(Long customerId, Pageable pageable) {
        return orderRepository.findByCustomerId(customerId, pageable)
                .map(this::convertToDto);
    }

    /**
     * 根據狀態查詢訂單
     *
     * @param status   訂單狀態
     * @param pageable 分頁參數
     * @return 訂單分頁結果
     */
    @Transactional(readOnly = true)
    public Page<OrderDto> getOrdersByStatus(OrderStatus status, Pageable pageable) {
        return orderRepository.findByStatus(status, pageable)
                .map(this::convertToDto);
    }

    /**
     * 查詢所有訂單（分頁）
     *
     * @param pageable 分頁參數
     * @return 訂單分頁結果
     */
    @Transactional(readOnly = true)
    public Page<OrderDto> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable)
                .map(this::convertToDto);
    }

    /**
     * 查詢付款記錄（分頁）
     *
     * @param startDate 開始日期時間
     * @param endDate   結束日期時間
     * @param pageable  分頁參數
     * @return 付款記錄分頁結果
     */
    @Transactional(readOnly = true)
    public Page<PaymentDto> getPaymentsByDateRange(LocalDate startDate, LocalDate endDate, Pageable pageable) {
        return paymentRepository.findByPaymentDateBetween(startDate, endDate, pageable)
                .map(this::convertPaymentToDto);
    }

    // ==================== 私有方法 ====================

    /**
     * 產生唯一訂單編號
     *
     * <p>格式：ORD + 年月日時分秒 + 3位流水號</p>
     * <p>範例：ORD20240115143022001</p>
     *
     * @return 訂單編號
     */
    private String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        long seq = orderSequence.incrementAndGet() % 1000;
        String orderNo = String.format("ORD%s%03d", timestamp, seq);

        // 確保訂單編號唯一
        while (orderRepository.existsByOrderNo(orderNo)) {
            seq = orderSequence.incrementAndGet() % 1000;
            orderNo = String.format("ORD%s%03d", timestamp, seq);
        }

        return orderNo;
    }

    /**
     * 更新訂單狀態
     *
     * <p>驗證狀態轉換規則：</p>
     * <ul>
     *   <li>PENDING → PAID（付款成功）</li>
     *   <li>PENDING → CANCELLED（取消訂單）</li>
     *   <li>PAID → REFUNDED（申請退款）</li>
     * </ul>
     *
     * @param order     訂單
     * @param newStatus 新狀態
     * @throws BusinessException 當狀態轉換不允許時拋出
     */
    private void updateOrderStatus(Order order, OrderStatus newStatus) {
        OrderStatus currentStatus = order.getStatus();

        // 驗證狀態轉換規則
        boolean valid = switch (newStatus) {
            case PAID -> currentStatus == OrderStatus.PENDING;
            case CANCELLED -> currentStatus == OrderStatus.PENDING;
            case REFUNDED -> currentStatus == OrderStatus.PAID;
            case PENDING -> false; // 不允許回到待處理狀態
        };

        if (!valid) {
            throw BusinessException.validationFailed(
                    String.format("無法將訂單狀態從 %s 變更為 %s",
                            currentStatus.getDescription(), newStatus.getDescription()));
        }

        order.setStatus(newStatus);
    }

    /**
     * 轉換訂單實體為 DTO
     *
     * @param order 訂單實體
     * @return 訂單 DTO
     */
    private OrderDto convertToDto(Order order) {
        // 處理可能為 null 的集合
        List<OrderItem> orderItems = order.getOrderItems() != null ? order.getOrderItems() : List.of();
        Set<Payment> payments = order.getPayments() != null ? order.getPayments() : Set.of();

        List<OrderItemDto> itemDtos = orderItems.stream()
                .map(this::convertOrderItemToDto)
                .collect(Collectors.toList());

        List<PaymentDto> paymentDtos = payments.stream()
                .map(this::convertPaymentToDto)
                .collect(Collectors.toList());

        BigDecimal paidAmount = payments.stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 合併日期和時間（處理可能的 null 值）
        LocalDateTime orderDateTime = null;
        if (order.getOrderDate() != null) {
            LocalTime time = order.getOrderTime() != null ? order.getOrderTime() : LocalTime.MIDNIGHT;
            orderDateTime = LocalDateTime.of(order.getOrderDate(), time);
        }

        // 查詢門市名稱
        String storeName = null;
        if (order.getStoreId() != null) {
            storeName = storeRepository.findById(order.getStoreId())
                    .map(store -> store.getName())
                    .orElse(null);
        }

        // 查詢客戶名稱
        String customerName = null;
        if (order.getCustomerId() != null) {
            customerName = customerRepository.findById(order.getCustomerId())
                    .map(customer -> customer.getName())
                    .orElse(null);
        }

        return OrderDto.builder()
                .id(order.getId())
                .orderNo(order.getOrderNo())
                .storeId(order.getStoreId())
                .storeName(storeName)
                .customerId(order.getCustomerId())
                .customerName(customerName)
                .orderDate(orderDateTime)
                .subtotal(order.getSubtotal())
                .discountAmount(order.getDiscountAmount())
                .taxAmount(order.getTaxAmount())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .statusDescription(order.getStatus().getDescription())
                .notes(order.getNotes())
                .items(itemDtos)
                .payments(paymentDtos)
                .paidAmount(paidAmount)
                .createdBy(order.getCreatedBy())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    /**
     * 轉換訂單明細實體為 DTO
     *
     * @param item 訂單明細實體
     * @return 訂單明細 DTO
     */
    private OrderItemDto convertOrderItemToDto(OrderItem item) {
        // 查詢商品資訊
        String productCode = null;
        String productName = null;
        if (item.getProductId() != null) {
            var product = productRepository.findById(item.getProductId()).orElse(null);
            if (product != null) {
                productCode = product.getSku();
                productName = product.getName();
            }
        }

        return OrderItemDto.builder()
                .id(item.getId())
                .orderId(item.getOrder() != null ? item.getOrder().getId() : null)
                .productId(item.getProductId())
                .productCode(productCode)
                .productName(productName)
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .discountAmount(item.getDiscountAmount())
                .subtotal(item.getSubtotal())
                .originalTotal(item.getOriginalTotal())
                .build();
    }

    /**
     * 轉換付款記錄實體為 DTO
     *
     * @param payment 付款記錄實體
     * @return 付款記錄 DTO
     */
    private PaymentDto convertPaymentToDto(Payment payment) {
        // 合併付款日期和時間
        LocalDateTime paymentDateTime = LocalDateTime.of(payment.getPaymentDate(), payment.getPaymentTime());

        return PaymentDto.builder()
                .id(payment.getId())
                .orderId(payment.getOrder() != null ? payment.getOrder().getId() : null)
                .orderNo(payment.getOrder() != null ? payment.getOrder().getOrderNo() : null)
                .paymentMethod(payment.getPaymentMethod())
                .paymentMethodDescription(payment.getPaymentMethod().getDescription())
                .amount(payment.getAmount())
                .paymentDate(paymentDateTime)
                .referenceNo(payment.getReferenceNo())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}
