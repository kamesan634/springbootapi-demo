package com.kamesan.erpapi.sales.entity;

import com.kamesan.erpapi.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 付款記錄實體
 *
 * <p>記錄訂單的付款資訊，包含付款方式、金額、時間等。</p>
 *
 * <p>主要功能：</p>
 * <ul>
 *   <li>記錄付款方式（現金、信用卡、行動支付等）</li>
 *   <li>記錄付款金額和時間</li>
 *   <li>追蹤付款參考編號（交易編號）</li>
 *   <li>支援單一訂單多筆付款（分期或混合付款）</li>
 * </ul>
 *
 * <p>使用情境：</p>
 * <ul>
 *   <li>單一付款：一筆訂單對應一筆付款記錄</li>
 *   <li>分次付款：一筆訂單對應多筆付款記錄</li>
 *   <li>混合付款：同一訂單使用多種付款方式</li>
 * </ul>
 *
 * @author ERP System Team
 * @version 1.0.0
 * @see Order
 * @see PaymentMethod
 */
@Entity
@Table(name = "payments", indexes = {
        @Index(name = "idx_payments_order_id", columnList = "order_id"),
        @Index(name = "idx_payments_payment_date", columnList = "payment_date"),
        @Index(name = "idx_payments_reference_no", columnList = "reference_no")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment extends BaseEntity {

    /**
     * 所屬訂單
     * <p>多對一關聯，此付款記錄所屬的訂單</p>
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    /**
     * 付款方式
     * <p>使用的付款方式類型</p>
     *
     * @see PaymentMethod
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 20)
    private PaymentMethod paymentMethod;

    /**
     * 付款金額
     * <p>此筆付款的金額</p>
     * <p>精確度：整數 10 位，小數 2 位</p>
     */
    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    /**
     * 付款日期
     * <p>實際完成付款的日期</p>
     */
    @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate;

    /**
     * 付款時間
     * <p>實際完成付款的時間</p>
     */
    @Column(name = "payment_time", nullable = false)
    private LocalTime paymentTime;

    /**
     * 交易參考編號
     * <p>付款交易的參考編號，用於追蹤和對帳</p>
     * <ul>
     *   <li>信用卡：授權碼或交易編號</li>
     *   <li>行動支付：交易編號</li>
     *   <li>現金：收據編號（可選）</li>
     * </ul>
     */
    @Column(name = "reference_no", length = 50)
    private String referenceNo;

    /**
     * 建立付款記錄的便捷方法
     *
     * <p>建立付款記錄並設定付款時間為當前時間</p>
     *
     * @param order         所屬訂單
     * @param paymentMethod 付款方式
     * @param amount        付款金額
     * @param referenceNo   交易參考編號
     * @return 付款記錄實體
     */
    public static Payment create(Order order, PaymentMethod paymentMethod,
                                  BigDecimal amount, String referenceNo) {
        LocalDateTime now = LocalDateTime.now();
        Payment payment = Payment.builder()
                .order(order)
                .paymentMethod(paymentMethod)
                .amount(amount)
                .paymentDate(now.toLocalDate())
                .paymentTime(now.toLocalTime())
                .referenceNo(referenceNo)
                .build();
        return payment;
    }

    /**
     * 建立現金付款記錄的便捷方法
     *
     * @param order  所屬訂單
     * @param amount 付款金額
     * @return 現金付款記錄
     */
    public static Payment createCashPayment(Order order, BigDecimal amount) {
        return create(order, PaymentMethod.CASH, amount, null);
    }

    /**
     * 建立信用卡付款記錄的便捷方法
     *
     * @param order       所屬訂單
     * @param amount      付款金額
     * @param referenceNo 授權碼或交易編號
     * @return 信用卡付款記錄
     */
    public static Payment createCreditCardPayment(Order order, BigDecimal amount, String referenceNo) {
        return create(order, PaymentMethod.CREDIT_CARD, amount, referenceNo);
    }

    /**
     * 檢查是否為電子支付
     *
     * <p>判斷付款方式是否為電子支付（非現金）</p>
     *
     * @return 是否為電子支付
     */
    public boolean isElectronicPayment() {
        return this.paymentMethod != PaymentMethod.CASH;
    }

    /**
     * 檢查是否為行動支付
     *
     * <p>判斷付款方式是否為行動支付（LINE Pay 或 Apple Pay）</p>
     *
     * @return 是否為行動支付
     */
    public boolean isMobilePayment() {
        return this.paymentMethod == PaymentMethod.LINE_PAY ||
                this.paymentMethod == PaymentMethod.APPLE_PAY;
    }

    /**
     * 取得付款方式描述
     *
     * @return 付款方式的中文描述
     */
    public String getPaymentMethodDescription() {
        return this.paymentMethod.getDescription();
    }
}
