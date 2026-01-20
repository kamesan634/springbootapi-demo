package com.kamesan.erpapi.sales.dto;

import com.kamesan.erpapi.sales.entity.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 訂單 DTO（資料傳輸物件）
 *
 * <p>用於 API 回應的訂單資料結構，包含訂單完整資訊。</p>
 *
 * <p>主要用途：</p>
 * <ul>
 *   <li>查詢訂單時的回應資料</li>
 *   <li>訂單列表的資料結構</li>
 *   <li>訂單詳情的完整資訊</li>
 * </ul>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "訂單資料")
public class OrderDto {

    /**
     * 訂單 ID
     */
    @Schema(description = "訂單 ID", example = "1")
    private Long id;

    /**
     * 訂單編號
     */
    @Schema(description = "訂單編號", example = "ORD20240115143022001")
    private String orderNo;

    /**
     * 門市 ID
     */
    @Schema(description = "門市 ID", example = "1")
    private Long storeId;

    /**
     * 門市名稱
     * <p>從關聯表查詢取得，方便前端顯示</p>
     */
    @Schema(description = "門市名稱", example = "台北信義店")
    private String storeName;

    /**
     * 客戶 ID
     */
    @Schema(description = "客戶 ID", example = "100")
    private Long customerId;

    /**
     * 客戶姓名
     * <p>從關聯表查詢取得，方便前端顯示</p>
     */
    @Schema(description = "客戶姓名", example = "王小明")
    private String customerName;

    /**
     * 訂單日期時間
     */
    @Schema(description = "訂單日期時間", example = "2024-01-15T14:30:22")
    private LocalDateTime orderDate;

    /**
     * 商品小計金額
     */
    @Schema(description = "商品小計金額", example = "1500.00")
    private BigDecimal subtotal;

    /**
     * 折扣金額
     */
    @Schema(description = "折扣金額", example = "100.00")
    private BigDecimal discountAmount;

    /**
     * 稅額
     */
    @Schema(description = "稅額", example = "70.00")
    private BigDecimal taxAmount;

    /**
     * 總金額
     */
    @Schema(description = "總金額", example = "1470.00")
    private BigDecimal totalAmount;

    /**
     * 訂單狀態
     */
    @Schema(description = "訂單狀態", example = "PAID")
    private OrderStatus status;

    /**
     * 訂單狀態描述
     */
    @Schema(description = "訂單狀態描述", example = "已付款")
    private String statusDescription;

    /**
     * 備註
     */
    @Schema(description = "備註", example = "請盡快出貨")
    private String notes;

    /**
     * 訂單明細項目列表
     */
    @Schema(description = "訂單明細項目列表")
    private List<OrderItemDto> items;

    /**
     * 付款記錄列表
     */
    @Schema(description = "付款記錄列表")
    private List<PaymentDto> payments;

    /**
     * 已付款金額
     */
    @Schema(description = "已付款金額", example = "1470.00")
    private BigDecimal paidAmount;

    /**
     * 建立者 ID
     */
    @Schema(description = "建立者 ID", example = "1")
    private Long createdBy;

    /**
     * 建立時間
     */
    @Schema(description = "建立時間", example = "2024-01-15T14:30:22")
    private LocalDateTime createdAt;

    /**
     * 更新時間
     */
    @Schema(description = "更新時間", example = "2024-01-15T14:35:00")
    private LocalDateTime updatedAt;
}
