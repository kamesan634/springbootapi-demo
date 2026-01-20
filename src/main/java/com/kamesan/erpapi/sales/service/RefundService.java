package com.kamesan.erpapi.sales.service;

import com.kamesan.erpapi.common.exception.BusinessException;
import com.kamesan.erpapi.inventory.entity.MovementType;
import com.kamesan.erpapi.inventory.service.InventoryService;
import com.kamesan.erpapi.sales.dto.*;
import com.kamesan.erpapi.sales.entity.*;
import com.kamesan.erpapi.sales.repository.*;
import com.kamesan.erpapi.system.service.NumberRuleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 退貨單服務
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefundService {

    private final RefundRepository refundRepository;
    private final RefundItemRepository refundItemRepository;
    private final OrderRepository orderRepository;
    private final NumberRuleService numberRuleService;
    private final InventoryService inventoryService;

    /**
     * 建立退貨單
     */
    @Transactional
    public RefundDto createRefund(CreateRefundRequest request) {
        log.info("建立退貨單，訂單 ID: {}", request.getOrderId());

        // 驗證原訂單
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> BusinessException.notFound("訂單", request.getOrderId()));

        if (order.getStatus() != OrderStatus.PAID) {
            throw BusinessException.validationFailed("訂單狀態為 " + order.getStatus() + "，無法退貨");
        }

        // 產生退貨單編號
        String refundNo = numberRuleService.generateNextNumber("REFUND");

        // 建立退貨單
        Refund refund = Refund.builder()
                .refundNo(refundNo)
                .orderId(order.getId())
                .orderNo(order.getOrderNo())
                .storeId(request.getStoreId())
                .customerId(order.getCustomerId())
                .refundDate(LocalDate.now())
                .refundTime(LocalTime.now())
                .refundMethod(request.getRefundMethod() != null ? request.getRefundMethod() : Refund.RefundMethod.ORIGINAL)
                .reason(request.getReason())
                .notes(request.getNotes())
                .build();

        // 建立退貨明細
        for (CreateRefundRequest.CreateRefundItemRequest itemRequest : request.getItems()) {
            RefundItem item = RefundItem.builder()
                    .orderItemId(itemRequest.getOrderItemId())
                    .productId(itemRequest.getProductId())
                    .productName(itemRequest.getProductName())
                    .quantity(itemRequest.getQuantity())
                    .unitPrice(itemRequest.getUnitPrice())
                    .reason(itemRequest.getReason())
                    .isRestock(itemRequest.getIsRestock() != null ? itemRequest.getIsRestock() : true)
                    .build();
            refund.addRefundItem(item);
        }

        // 計算金額
        refund.calculateTotals();

        // 儲存
        Refund saved = refundRepository.save(refund);
        log.info("退貨單建立成功，編號: {}", saved.getRefundNo());

        return convertToDto(saved);
    }

    /**
     * 根據 ID 查詢退貨單
     */
    @Transactional(readOnly = true)
    public RefundDto getRefundById(Long id) {
        Refund refund = refundRepository.findByIdWithItems(id)
                .orElseThrow(() -> BusinessException.notFound("退貨單", id));
        return convertToDto(refund);
    }

    /**
     * 根據編號查詢退貨單
     */
    @Transactional(readOnly = true)
    public RefundDto getRefundByNo(String refundNo) {
        Refund refund = refundRepository.findByRefundNo(refundNo)
                .orElseThrow(() -> new BusinessException(404, "退貨單編號 " + refundNo + " 不存在"));
        return convertToDto(refund);
    }

    /**
     * 核准退貨單
     */
    @Transactional
    public RefundDto approveRefund(Long id, Long approvedBy) {
        log.info("核准退貨單，ID: {}", id);

        Refund refund = refundRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("退貨單", id));

        if (refund.getStatus() != Refund.RefundStatus.PENDING) {
            throw BusinessException.validationFailed("退貨單狀態為 " + refund.getStatus() + "，無法核准");
        }

        refund.setStatus(Refund.RefundStatus.APPROVED);
        refund.setApprovedBy(approvedBy);
        refund.setApprovedAt(java.time.LocalDateTime.now());

        Refund saved = refundRepository.save(refund);
        log.info("退貨單核准成功，編號: {}", saved.getRefundNo());

        return convertToDto(saved);
    }

    /**
     * 完成退貨
     *
     * @param id         退貨單 ID
     * @param operatorId 操作人員 ID
     * @return 退貨單資訊
     */
    @Transactional
    public RefundDto completeRefund(Long id, Long operatorId) {
        log.info("完成退貨，ID: {}", id);

        // 取得退貨單及明細
        Refund refund = refundRepository.findByIdWithItems(id)
                .orElseThrow(() -> BusinessException.notFound("退貨單", id));

        if (refund.getStatus() != Refund.RefundStatus.APPROVED) {
            throw BusinessException.validationFailed("退貨單狀態為 " + refund.getStatus() + "，無法完成");
        }

        refund.setStatus(Refund.RefundStatus.COMPLETED);
        Refund saved = refundRepository.save(refund);

        // 處理庫存回補
        processInventoryRestock(saved, operatorId);

        log.info("退貨完成，編號: {}", saved.getRefundNo());

        return convertToDto(saved);
    }

    /**
     * 處理庫存回補
     *
     * <p>遍歷退貨明細，針對需要回補庫存的項目執行入庫操作</p>
     *
     * @param refund     退貨單
     * @param operatorId 操作人員 ID
     */
    private void processInventoryRestock(Refund refund, Long operatorId) {
        Long warehouseId = refund.getStoreId(); // 使用門市 ID 作為倉庫 ID

        for (RefundItem item : refund.getRefundItems()) {
            if (Boolean.TRUE.equals(item.getIsRestock())) {
                log.info("庫存回補: productId={}, quantity={}, warehouseId={}",
                        item.getProductId(), item.getQuantity(), warehouseId);

                String remark = "客戶退貨";
                if (item.getReason() != null && !item.getReason().isEmpty()) {
                    remark += " - " + item.getReason();
                }

                inventoryService.executeMovement(
                        item.getProductId(),
                        warehouseId,
                        MovementType.RETURN_IN,
                        item.getQuantity(),
                        refund.getRefundNo(),
                        remark,
                        operatorId
                );
            }
        }
    }

    /**
     * 拒絕退貨
     */
    @Transactional
    public RefundDto rejectRefund(Long id, String reason) {
        log.info("拒絕退貨，ID: {}", id);

        Refund refund = refundRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("退貨單", id));

        if (refund.getStatus() != Refund.RefundStatus.PENDING) {
            throw BusinessException.validationFailed("退貨單狀態為 " + refund.getStatus() + "，無法拒絕");
        }

        refund.setStatus(Refund.RefundStatus.REJECTED);
        refund.setNotes((refund.getNotes() != null ? refund.getNotes() + "\n" : "") + "拒絕原因：" + reason);

        Refund saved = refundRepository.save(refund);
        log.info("退貨已拒絕，編號: {}", saved.getRefundNo());

        return convertToDto(saved);
    }

    /**
     * 查詢退貨單（分頁）
     */
    @Transactional(readOnly = true)
    public Page<RefundDto> queryRefunds(Long storeId, Long customerId, Refund.RefundStatus status,
                                         LocalDate startDate, LocalDate endDate, Pageable pageable) {
        return refundRepository.findByConditions(storeId, customerId, status, startDate, endDate, pageable)
                .map(this::convertToDto);
    }

    private RefundDto convertToDto(Refund refund) {
        List<RefundItemDto> itemDtos = refund.getRefundItems().stream()
                .map(this::convertItemToDto)
                .collect(Collectors.toList());

        return RefundDto.builder()
                .id(refund.getId())
                .refundNo(refund.getRefundNo())
                .orderId(refund.getOrderId())
                .orderNo(refund.getOrderNo())
                .storeId(refund.getStoreId())
                .customerId(refund.getCustomerId())
                .refundDate(refund.getRefundDate())
                .refundTime(refund.getRefundTime())
                .subtotal(refund.getSubtotal())
                .taxAmount(refund.getTaxAmount())
                .totalAmount(refund.getTotalAmount())
                .refundMethod(refund.getRefundMethod())
                .status(refund.getStatus())
                .reason(refund.getReason())
                .notes(refund.getNotes())
                .approvedBy(refund.getApprovedBy())
                .approvedAt(refund.getApprovedAt())
                .items(itemDtos)
                .createdAt(refund.getCreatedAt())
                .updatedAt(refund.getUpdatedAt())
                .build();
    }

    private RefundItemDto convertItemToDto(RefundItem item) {
        return RefundItemDto.builder()
                .id(item.getId())
                .refundId(item.getRefund() != null ? item.getRefund().getId() : null)
                .orderItemId(item.getOrderItemId())
                .productId(item.getProductId())
                .productName(item.getProductName())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .subtotal(item.getSubtotal())
                .reason(item.getReason())
                .isRestock(item.getIsRestock())
                .build();
    }
}
