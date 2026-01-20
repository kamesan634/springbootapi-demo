package com.kamesan.erpapi.sales.service;

import com.kamesan.erpapi.common.exception.BusinessException;
import com.kamesan.erpapi.sales.dto.CashierShiftDto;
import com.kamesan.erpapi.sales.entity.CashierShift;
import com.kamesan.erpapi.sales.repository.CashierShiftRepository;
import com.kamesan.erpapi.system.service.NumberRuleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 收銀班次服務
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CashierShiftService {

    private final CashierShiftRepository cashierShiftRepository;
    private final NumberRuleService numberRuleService;

    /**
     * 開班
     */
    @Transactional
    public CashierShiftDto openShift(Long storeId, Long cashierId, BigDecimal openingAmount) {
        log.info("開班，門市: {}, 收銀員: {}", storeId, cashierId);

        // 檢查是否有未結班次
        cashierShiftRepository.findOpenShiftByCashier(cashierId)
                .ifPresent(shift -> {
                    throw BusinessException.validationFailed("收銀員尚有未結班次: " + shift.getShiftNo());
                });

        // 產生班次編號
        String shiftNo = numberRuleService.generateNextNumber("CASHIER_SHIFT");

        // 建立班次
        CashierShift shift = CashierShift.builder()
                .shiftNo(shiftNo)
                .storeId(storeId)
                .cashierId(cashierId)
                .shiftDate(LocalDate.now())
                .startTime(LocalDateTime.now())
                .openingAmount(openingAmount)
                .build();

        CashierShift saved = cashierShiftRepository.save(shift);
        log.info("開班成功，班次編號: {}", saved.getShiftNo());

        return convertToDto(saved);
    }

    /**
     * 結班
     */
    @Transactional
    public CashierShiftDto closeShift(Long shiftId, BigDecimal closingAmount, String notes) {
        log.info("結班，班次 ID: {}", shiftId);

        CashierShift shift = cashierShiftRepository.findById(shiftId)
                .orElseThrow(() -> BusinessException.notFound("班次", shiftId));

        if (shift.getStatus() != CashierShift.ShiftStatus.OPEN) {
            throw BusinessException.validationFailed("班次狀態為 " + shift.getStatus() + "，無法結班");
        }

        shift.closeShift(closingAmount);
        shift.setNotes(notes);

        CashierShift saved = cashierShiftRepository.save(shift);
        log.info("結班成功，班次編號: {}", saved.getShiftNo());

        return convertToDto(saved);
    }

    /**
     * 取得收銀員目前班次
     */
    @Transactional(readOnly = true)
    public CashierShiftDto getCurrentShift(Long cashierId) {
        CashierShift shift = cashierShiftRepository.findOpenShiftByCashier(cashierId)
                .orElseThrow(() -> new BusinessException(404, "收銀員目前無開啟的班次"));
        return convertToDto(shift);
    }

    /**
     * 根據 ID 查詢班次
     */
    @Transactional(readOnly = true)
    public CashierShiftDto getShiftById(Long id) {
        CashierShift shift = cashierShiftRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("班次", id));
        return convertToDto(shift);
    }

    /**
     * 更新班次銷售統計
     */
    @Transactional
    public void updateShiftSales(Long shiftId, BigDecimal salesAmount, BigDecimal cashAmount,
                                  BigDecimal cardAmount, BigDecimal otherAmount, boolean isRefund) {
        CashierShift shift = cashierShiftRepository.findById(shiftId)
                .orElseThrow(() -> BusinessException.notFound("班次", shiftId));

        if (isRefund) {
            shift.setTotalRefunds(shift.getTotalRefunds().add(salesAmount));
            shift.setRefundCount(shift.getRefundCount() + 1);
        } else {
            shift.setTotalSales(shift.getTotalSales().add(salesAmount));
            shift.setCashSales(shift.getCashSales().add(cashAmount));
            shift.setCardSales(shift.getCardSales().add(cardAmount));
            shift.setOtherSales(shift.getOtherSales().add(otherAmount));
            shift.setOrderCount(shift.getOrderCount() + 1);
        }

        cashierShiftRepository.save(shift);
    }

    /**
     * 查詢班次（分頁）
     */
    @Transactional(readOnly = true)
    public Page<CashierShiftDto> queryShifts(Long storeId, Long cashierId, CashierShift.ShiftStatus status,
                                              LocalDate startDate, LocalDate endDate, Pageable pageable) {
        return cashierShiftRepository.findByConditions(storeId, cashierId, status, startDate, endDate, pageable)
                .map(this::convertToDto);
    }

    private CashierShiftDto convertToDto(CashierShift shift) {
        return CashierShiftDto.builder()
                .id(shift.getId())
                .shiftNo(shift.getShiftNo())
                .storeId(shift.getStoreId())
                .cashierId(shift.getCashierId())
                .shiftDate(shift.getShiftDate())
                .startTime(shift.getStartTime())
                .endTime(shift.getEndTime())
                .openingAmount(shift.getOpeningAmount())
                .closingAmount(shift.getClosingAmount())
                .expectedAmount(shift.getExpectedAmount())
                .differenceAmount(shift.getDifferenceAmount())
                .totalSales(shift.getTotalSales())
                .totalRefunds(shift.getTotalRefunds())
                .cashSales(shift.getCashSales())
                .cardSales(shift.getCardSales())
                .otherSales(shift.getOtherSales())
                .orderCount(shift.getOrderCount())
                .refundCount(shift.getRefundCount())
                .status(shift.getStatus())
                .notes(shift.getNotes())
                .createdAt(shift.getCreatedAt())
                .updatedAt(shift.getUpdatedAt())
                .build();
    }
}
