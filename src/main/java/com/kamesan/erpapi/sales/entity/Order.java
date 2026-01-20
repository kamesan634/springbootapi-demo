package com.kamesan.erpapi.sales.entity;

import com.kamesan.erpapi.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 訂單實體
 *
 * <p>代表一筆銷售訂單，包含訂單基本資訊、金額明細及相關關聯。</p>
 *
 * <p>主要功能：</p>
 * <ul>
 *   <li>記錄訂單基本資訊（訂單編號、門市、客戶等）</li>
 *   <li>管理訂單金額（小計、折扣、稅額、總金額）</li>
 *   <li>追蹤訂單狀態變更</li>
 *   <li>關聯訂單明細項目</li>
 *   <li>關聯付款記錄</li>
 * </ul>
 *
 * <p>金額計算公式：</p>
 * <pre>
 * 總金額 = 小計 - 折扣金額 + 稅額
 * </pre>
 *
 * @author ERP System Team
 * @version 1.0.0
 * @see OrderItem
 * @see Payment
 * @see OrderStatus
 */
@Entity
@Table(name = "orders", indexes = {
        @Index(name = "idx_orders_order_no", columnList = "order_no"),
        @Index(name = "idx_orders_store_id", columnList = "store_id"),
        @Index(name = "idx_orders_customer_id", columnList = "customer_id"),
        @Index(name = "idx_orders_order_date", columnList = "order_date"),
        @Index(name = "idx_orders_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order extends BaseEntity {

    /**
     * 訂單編號
     * <p>系統自動產生的唯一識別碼，格式為：ORD + 年月日時分秒 + 流水號</p>
     * <p>範例：ORD20240115143022001</p>
     */
    @Column(name = "order_no", nullable = false, unique = true, length = 30)
    private String orderNo;

    /**
     * 門市 ID
     * <p>訂單所屬門市的識別碼，關聯到 stores 資料表</p>
     */
    @Column(name = "store_id", nullable = false)
    private Long storeId;

    /**
     * 客戶 ID
     * <p>購買客戶的識別碼，關聯到 customers 資料表</p>
     * <p>可為空，代表非會員購買</p>
     */
    @Column(name = "customer_id")
    private Long customerId;

    /**
     * 訂單日期
     * <p>訂單建立的日期</p>
     */
    @Column(name = "order_date", nullable = false)
    private LocalDate orderDate;

    /**
     * 訂單時間
     * <p>訂單建立的時間</p>
     */
    @Column(name = "order_time", nullable = false)
    private LocalTime orderTime;

    /**
     * 商品小計金額
     * <p>所有訂單明細的金額加總（未扣除折扣、未加稅）</p>
     * <p>精確度：整數 10 位，小數 2 位</p>
     */
    @Column(name = "subtotal", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    /**
     * 折扣金額
     * <p>訂單層級的折扣金額（優惠券、滿額折扣等）</p>
     * <p>預設值為 0</p>
     */
    @Column(name = "discount_amount", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    /**
     * 稅額
     * <p>營業稅或其他稅額</p>
     * <p>預設值為 0</p>
     */
    @Column(name = "tax_amount", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal taxAmount = BigDecimal.ZERO;

    /**
     * 總金額
     * <p>訂單最終應付金額 = 小計 - 折扣金額 + 稅額</p>
     */
    @Column(name = "total_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal totalAmount;

    /**
     * 已付金額
     * <p>客戶實際支付的金額</p>
     */
    @Column(name = "paid_amount", nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal paidAmount = BigDecimal.ZERO;

    /**
     * 找零金額
     * <p>應找給客戶的金額</p>
     */
    @Column(name = "change_amount", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal changeAmount = BigDecimal.ZERO;

    /**
     * 獲得點數
     * <p>此訂單可獲得的會員點數</p>
     */
    @Column(name = "points_earned")
    @Builder.Default
    private Integer pointsEarned = 0;

    /**
     * 使用點數
     * <p>此訂單使用的會員點數折抵</p>
     */
    @Column(name = "points_used")
    @Builder.Default
    private Integer pointsUsed = 0;

    /**
     * 優惠券 ID
     * <p>此訂單使用的優惠券</p>
     */
    @Column(name = "coupon_id")
    private Long couponId;

    /**
     * 訂單狀態
     * <p>訂單目前的處理狀態</p>
     *
     * @see OrderStatus
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;

    /**
     * 備註
     * <p>訂單相關備註說明</p>
     */
    @Column(name = "notes", length = 500)
    private String notes;

    /**
     * 訂單明細項目列表
     * <p>一對多關聯，訂單包含的所有商品明細</p>
     * <p>使用 CascadeType.ALL 確保明細隨訂單一同儲存</p>
     * <p>orphanRemoval = true 確保孤立明細被刪除</p>
     */
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<OrderItem> orderItems = new ArrayList<>();

    /**
     * 付款記錄列表
     * <p>一對多關聯，訂單的所有付款記錄</p>
     * <p>使用 Set 避免 Hibernate MultipleBagFetchException</p>
     */
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Payment> payments = new HashSet<>();

    /**
     * 新增訂單明細項目
     *
     * <p>建立訂單與明細之間的雙向關聯</p>
     *
     * @param item 要新增的訂單明細項目
     */
    public void addOrderItem(OrderItem item) {
        orderItems.add(item);
        item.setOrder(this);
    }

    /**
     * 移除訂單明細項目
     *
     * <p>解除訂單與明細之間的雙向關聯</p>
     *
     * @param item 要移除的訂單明細項目
     */
    public void removeOrderItem(OrderItem item) {
        orderItems.remove(item);
        item.setOrder(null);
    }

    /**
     * 新增付款記錄
     *
     * <p>建立訂單與付款記錄之間的雙向關聯</p>
     *
     * @param payment 要新增的付款記錄
     */
    public void addPayment(Payment payment) {
        payments.add(payment);
        payment.setOrder(this);
    }

    /**
     * 移除付款記錄
     *
     * <p>解除訂單與付款記錄之間的雙向關聯</p>
     *
     * @param payment 要移除的付款記錄
     */
    public void removePayment(Payment payment) {
        payments.remove(payment);
        payment.setOrder(null);
    }

    /**
     * 計算並更新訂單金額
     *
     * <p>根據訂單明細重新計算小計，並更新總金額</p>
     * <p>公式：總金額 = 小計 - 折扣金額 + 稅額</p>
     */
    public void calculateTotals() {
        // 計算小計（所有明細的小計加總）
        this.subtotal = orderItems.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 計算總金額
        this.totalAmount = this.subtotal
                .subtract(this.discountAmount != null ? this.discountAmount : BigDecimal.ZERO)
                .add(this.taxAmount != null ? this.taxAmount : BigDecimal.ZERO);
    }

    /**
     * 檢查訂單是否可取消
     *
     * <p>只有待處理狀態的訂單才能取消</p>
     *
     * @return 是否可取消
     */
    public boolean isCancellable() {
        return this.status == OrderStatus.PENDING;
    }

    /**
     * 檢查訂單是否可退款
     *
     * <p>只有已付款狀態的訂單才能退款</p>
     *
     * @return 是否可退款
     */
    public boolean isRefundable() {
        return this.status == OrderStatus.PAID;
    }

    /**
     * 取消訂單
     *
     * @throws IllegalStateException 當訂單狀態不允許取消時拋出
     */
    public void cancel() {
        if (!isCancellable()) {
            throw new IllegalStateException("訂單狀態為 " + status.getDescription() + "，無法取消");
        }
        this.status = OrderStatus.CANCELLED;
    }

    /**
     * 標記訂單為已付款
     *
     * @throws IllegalStateException 當訂單狀態不允許付款時拋出
     */
    public void markAsPaid() {
        if (this.status != OrderStatus.PENDING) {
            throw new IllegalStateException("訂單狀態為 " + status.getDescription() + "，無法標記為已付款");
        }
        this.status = OrderStatus.PAID;
    }

    /**
     * 標記訂單為已退款
     *
     * @throws IllegalStateException 當訂單狀態不允許退款時拋出
     */
    public void markAsRefunded() {
        if (!isRefundable()) {
            throw new IllegalStateException("訂單狀態為 " + status.getDescription() + "，無法退款");
        }
        this.status = OrderStatus.REFUNDED;
    }

    /**
     * 計算已付款總額
     *
     * @return 所有付款記錄的金額加總
     */
    public BigDecimal getTotalPaidAmount() {
        return payments.stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 檢查是否已完全付款
     *
     * @return 已付款金額是否大於等於總金額
     */
    public boolean isFullyPaid() {
        return getTotalPaidAmount().compareTo(totalAmount) >= 0;
    }
}
