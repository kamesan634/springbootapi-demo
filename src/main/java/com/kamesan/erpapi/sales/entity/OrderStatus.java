package com.kamesan.erpapi.sales.entity;

/**
 * 訂單狀態枚舉
 *
 * <p>定義訂單在生命週期中的各種狀態，包括：</p>
 * <ul>
 *   <li>{@link #PENDING} - 待處理：訂單已建立但尚未付款</li>
 *   <li>{@link #PAID} - 已付款：訂單已完成付款</li>
 *   <li>{@link #CANCELLED} - 已取消：訂單已被取消</li>
 *   <li>{@link #REFUNDED} - 已退款：訂單已完成退款</li>
 * </ul>
 *
 * <p>狀態轉換規則：</p>
 * <ul>
 *   <li>PENDING → PAID（付款成功）</li>
 *   <li>PENDING → CANCELLED（取消訂單）</li>
 *   <li>PAID → REFUNDED（申請退款）</li>
 * </ul>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
public enum OrderStatus {

    /**
     * 待處理
     * <p>訂單已建立但尚未付款，等待客戶完成付款流程。</p>
     */
    PENDING("待處理"),

    /**
     * 已付款
     * <p>訂單已完成付款，可進行後續處理（如出貨）。</p>
     */
    PAID("已付款"),

    /**
     * 已取消
     * <p>訂單已被取消，不再進行後續處理。</p>
     */
    CANCELLED("已取消"),

    /**
     * 已退款
     * <p>訂單已完成退款，款項已退還給客戶。</p>
     */
    REFUNDED("已退款");

    /**
     * 狀態描述
     */
    private final String description;

    /**
     * 建構子
     *
     * @param description 狀態描述
     */
    OrderStatus(String description) {
        this.description = description;
    }

    /**
     * 取得狀態描述
     *
     * @return 狀態描述文字
     */
    public String getDescription() {
        return description;
    }
}
