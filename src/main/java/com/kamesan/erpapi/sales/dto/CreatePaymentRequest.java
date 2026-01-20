package com.kamesan.erpapi.sales.dto;

import com.kamesan.erpapi.sales.entity.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 建立付款請求 DTO
 *
 * <p>用於接收建立付款記錄的 API 請求資料。</p>
 *
 * <p>驗證規則：</p>
 * <ul>
 *   <li>訂單 ID：必填</li>
 *   <li>付款方式：必填</li>
 *   <li>付款金額：必填，必須大於 0</li>
 * </ul>
 *
 * <p>使用範例：</p>
 * <pre>
 * {
 *   "orderId": 1,
 *   "paymentMethod": "CREDIT_CARD",
 *   "amount": 1470.00,
 *   "referenceNo": "TXN123456789"
 * }
 * </pre>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "建立付款請求")
public class CreatePaymentRequest {

    /**
     * 訂單 ID
     * <p>必填，要付款的訂單</p>
     */
    @NotNull(message = "訂單 ID 不能為空")
    @Schema(description = "訂單 ID", example = "1", required = true)
    private Long orderId;

    /**
     * 付款方式
     * <p>必填</p>
     */
    @NotNull(message = "付款方式不能為空")
    @Schema(description = "付款方式", example = "CREDIT_CARD", required = true)
    private PaymentMethod paymentMethod;

    /**
     * 付款金額
     * <p>必填，必須大於 0</p>
     */
    @NotNull(message = "付款金額不能為空")
    @DecimalMin(value = "0.01", message = "付款金額必須大於 0")
    @Schema(description = "付款金額", example = "1470.00", required = true)
    private BigDecimal amount;

    /**
     * 交易參考編號
     * <p>選填，用於信用卡或電子支付的交易追蹤</p>
     */
    @Size(max = 50, message = "交易參考編號不能超過 50 字")
    @Schema(description = "交易參考編號", example = "TXN123456789")
    private String referenceNo;
}
