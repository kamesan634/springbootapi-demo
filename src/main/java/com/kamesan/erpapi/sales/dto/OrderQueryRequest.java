package com.kamesan.erpapi.sales.dto;

import com.kamesan.erpapi.sales.entity.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 訂單查詢請求 DTO
 *
 * <p>用於接收訂單查詢的條件參數。</p>
 *
 * <p>支援的查詢條件：</p>
 * <ul>
 *   <li>訂單編號（模糊查詢）</li>
 *   <li>門市 ID</li>
 *   <li>客戶 ID</li>
 *   <li>訂單狀態</li>
 *   <li>訂單日期範圍</li>
 * </ul>
 *
 * <p>所有條件均為選填，可組合使用。</p>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "訂單查詢請求")
public class OrderQueryRequest {

    /**
     * 訂單編號關鍵字
     * <p>模糊查詢，輸入部分訂單編號即可</p>
     */
    @Schema(description = "訂單編號關鍵字（模糊查詢）", example = "ORD2024")
    private String orderNoKeyword;

    /**
     * 門市 ID
     */
    @Schema(description = "門市 ID", example = "1")
    private Long storeId;

    /**
     * 客戶 ID
     */
    @Schema(description = "客戶 ID", example = "100")
    private Long customerId;

    /**
     * 訂單狀態
     */
    @Schema(description = "訂單狀態", example = "PAID")
    private OrderStatus status;

    /**
     * 開始日期時間
     * <p>查詢此時間之後的訂單</p>
     */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Schema(description = "開始日期時間", example = "2024-01-01T00:00:00")
    private LocalDateTime startDate;

    /**
     * 結束日期時間
     * <p>查詢此時間之前的訂單</p>
     */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Schema(description = "結束日期時間", example = "2024-01-31T23:59:59")
    private LocalDateTime endDate;
}
