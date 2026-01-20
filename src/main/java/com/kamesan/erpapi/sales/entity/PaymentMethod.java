package com.kamesan.erpapi.sales.entity;

/**
 * 付款方式枚舉
 *
 * <p>定義系統支援的各種付款方式，包括：</p>
 * <ul>
 *   <li>{@link #CASH} - 現金：實體現金付款</li>
 *   <li>{@link #CREDIT_CARD} - 信用卡：使用信用卡刷卡付款</li>
 *   <li>{@link #DEBIT_CARD} - 金融卡：使用金融卡（簽帳卡）付款</li>
 *   <li>{@link #LINE_PAY} - LINE Pay：使用 LINE Pay 行動支付</li>
 *   <li>{@link #APPLE_PAY} - Apple Pay：使用 Apple Pay 行動支付</li>
 * </ul>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
public enum PaymentMethod {

    /**
     * 現金
     * <p>使用實體現金進行付款。</p>
     */
    CASH("現金"),

    /**
     * 信用卡
     * <p>使用信用卡刷卡付款，支援各大發卡銀行。</p>
     */
    CREDIT_CARD("信用卡"),

    /**
     * 金融卡
     * <p>使用金融卡（簽帳卡）付款，直接從銀行帳戶扣款。</p>
     */
    DEBIT_CARD("金融卡"),

    /**
     * LINE Pay
     * <p>使用 LINE Pay 行動支付進行付款。</p>
     */
    LINE_PAY("LINE Pay"),

    /**
     * Apple Pay
     * <p>使用 Apple Pay 行動支付進行付款。</p>
     */
    APPLE_PAY("Apple Pay");

    /**
     * 付款方式描述
     */
    private final String description;

    /**
     * 建構子
     *
     * @param description 付款方式描述
     */
    PaymentMethod(String description) {
        this.description = description;
    }

    /**
     * 取得付款方式描述
     *
     * @return 付款方式描述文字
     */
    public String getDescription() {
        return description;
    }
}
