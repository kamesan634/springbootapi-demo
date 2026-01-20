package com.kamesan.erpapi.sales.dto;

import com.kamesan.erpapi.sales.entity.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 付款記錄 DTO（資料傳輸物件）
 *
 * <p>用於 API 回應的付款記錄資料結構。</p>
 *
 * <p>主要用途：</p>
 * <ul>
 *   <li>訂單詳情中的付款記錄</li>
 *   <li>付款記錄列表</li>
 *   <li>付款處理結果回應</li>
 * </ul>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "付款記錄資料")
public class PaymentDto {

    /**
     * 付款記錄 ID
     */
    @Schema(description = "付款記錄 ID", example = "1")
    private Long id;

    /**
     * 所屬訂單 ID
     */
    @Schema(description = "所屬訂單 ID", example = "1")
    private Long orderId;

    /**
     * 訂單編號
     * <p>從訂單資料取得，方便前端顯示</p>
     */
    @Schema(description = "訂單編號", example = "ORD20240115143022001")
    private String orderNo;

    /**
     * 付款方式
     */
    @Schema(description = "付款方式", example = "CREDIT_CARD")
    private PaymentMethod paymentMethod;

    /**
     * 付款方式描述
     */
    @Schema(description = "付款方式描述", example = "信用卡")
    private String paymentMethodDescription;

    /**
     * 付款金額
     */
    @Schema(description = "付款金額", example = "1470.00")
    private BigDecimal amount;

    /**
     * 付款日期時間
     */
    @Schema(description = "付款日期時間", example = "2024-01-15T14:35:00")
    private LocalDateTime paymentDate;

    /**
     * 交易參考編號
     */
    @Schema(description = "交易參考編號", example = "TXN123456789")
    private String referenceNo;

    /**
     * 建立時間
     */
    @Schema(description = "建立時間", example = "2024-01-15T14:35:00")
    private LocalDateTime createdAt;
}
