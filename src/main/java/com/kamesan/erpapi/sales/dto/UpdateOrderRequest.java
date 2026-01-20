package com.kamesan.erpapi.sales.dto;

import com.kamesan.erpapi.sales.entity.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 更新訂單請求 DTO
 *
 * <p>用於接收更新訂單資訊的 API 請求資料。</p>
 *
 * <p>可更新欄位：</p>
 * <ul>
 *   <li>客戶 ID</li>
 *   <li>折扣金額</li>
 *   <li>稅額</li>
 *   <li>訂單狀態</li>
 *   <li>備註</li>
 * </ul>
 *
 * <p>注意事項：</p>
 * <ul>
 *   <li>所有欄位均為選填，只更新有提供值的欄位</li>
 *   <li>更新折扣或稅額會重新計算總金額</li>
 *   <li>狀態更新需符合狀態轉換規則</li>
 * </ul>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "更新訂單請求")
public class UpdateOrderRequest {

    /**
     * 客戶 ID
     */
    @Schema(description = "客戶 ID", example = "100")
    private Long customerId;

    /**
     * 訂單折扣金額
     */
    @DecimalMin(value = "0", message = "折扣金額不能為負數")
    @Schema(description = "訂單折扣金額", example = "150.00")
    private BigDecimal discountAmount;

    /**
     * 稅額
     */
    @DecimalMin(value = "0", message = "稅額不能為負數")
    @Schema(description = "稅額", example = "80.00")
    private BigDecimal taxAmount;

    /**
     * 訂單狀態
     * <p>需符合狀態轉換規則</p>
     */
    @Schema(description = "訂單狀態", example = "PAID")
    private OrderStatus status;

    /**
     * 備註
     */
    @Size(max = 500, message = "備註不能超過 500 字")
    @Schema(description = "備註", example = "客戶要求更改配送地址")
    private String notes;
}
