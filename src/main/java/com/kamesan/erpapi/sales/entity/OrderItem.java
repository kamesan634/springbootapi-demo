package com.kamesan.erpapi.sales.entity;

import com.kamesan.erpapi.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * 訂單明細實體
 *
 * <p>代表訂單中的單一商品項目，記錄購買的商品、數量、價格等資訊。</p>
 *
 * <p>主要功能：</p>
 * <ul>
 *   <li>記錄購買的商品資訊</li>
 *   <li>記錄購買數量和單價</li>
 *   <li>計算單項小計金額</li>
 *   <li>支援單項折扣</li>
 * </ul>
 *
 * <p>金額計算公式：</p>
 * <pre>
 * 小計 = (單價 × 數量) - 折扣金額
 * </pre>
 *
 * @author ERP System Team
 * @version 1.0.0
 * @see Order
 */
@Entity
@Table(name = "order_items", indexes = {
        @Index(name = "idx_order_items_order_id", columnList = "order_id"),
        @Index(name = "idx_order_items_product_id", columnList = "product_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem extends BaseEntity {

    /**
     * 所屬訂單
     * <p>多對一關聯，此明細所屬的訂單</p>
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    /**
     * 商品 ID
     * <p>購買商品的識別碼，關聯到 products 資料表</p>
     */
    @Column(name = "product_id", nullable = false)
    private Long productId;

    /**
     * 購買數量
     * <p>購買的商品數量，必須大於 0</p>
     */
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    /**
     * 商品單價
     * <p>購買時的商品單價（可能與目前商品定價不同）</p>
     * <p>精確度：整數 10 位，小數 2 位</p>
     */
    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    /**
     * 折扣金額
     * <p>此明細項目的折扣金額（單品折扣）</p>
     * <p>預設值為 0</p>
     */
    @Column(name = "discount_amount", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    /**
     * 小計金額
     * <p>此明細項目的總金額 = (單價 × 數量) - 折扣金額</p>
     */
    @Column(name = "subtotal", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    /**
     * 計算並更新小計金額
     *
     * <p>根據單價、數量和折扣金額計算小計</p>
     * <p>公式：小計 = (單價 × 數量) - 折扣金額</p>
     *
     * <p>使用範例：</p>
     * <pre>
     * OrderItem item = OrderItem.builder()
     *     .productId(1L)
     *     .quantity(2)
     *     .unitPrice(new BigDecimal("100.00"))
     *     .discountAmount(new BigDecimal("10.00"))
     *     .build();
     * item.calculateSubtotal(); // subtotal = 190.00
     * </pre>
     */
    public void calculateSubtotal() {
        BigDecimal totalBeforeDiscount = this.unitPrice.multiply(BigDecimal.valueOf(this.quantity));
        BigDecimal discount = this.discountAmount != null ? this.discountAmount : BigDecimal.ZERO;
        this.subtotal = totalBeforeDiscount.subtract(discount);
    }

    /**
     * 建立訂單明細的便捷方法
     *
     * <p>建立明細並自動計算小計金額</p>
     *
     * @param productId      商品 ID
     * @param quantity       購買數量
     * @param unitPrice      商品單價
     * @param discountAmount 折扣金額
     * @return 已計算小計的訂單明細
     */
    public static OrderItem create(Long productId, Integer quantity,
                                   BigDecimal unitPrice, BigDecimal discountAmount) {
        OrderItem item = OrderItem.builder()
                .productId(productId)
                .quantity(quantity)
                .unitPrice(unitPrice)
                .discountAmount(discountAmount != null ? discountAmount : BigDecimal.ZERO)
                .build();
        item.calculateSubtotal();
        return item;
    }

    /**
     * 取得原價總額（未扣折扣）
     *
     * @return 單價 × 數量
     */
    public BigDecimal getOriginalTotal() {
        return this.unitPrice.multiply(BigDecimal.valueOf(this.quantity));
    }

    /**
     * 更新購買數量並重新計算小計
     *
     * @param newQuantity 新的購買數量
     * @throws IllegalArgumentException 當數量小於等於 0 時拋出
     */
    public void updateQuantity(Integer newQuantity) {
        if (newQuantity == null || newQuantity <= 0) {
            throw new IllegalArgumentException("購買數量必須大於 0");
        }
        this.quantity = newQuantity;
        calculateSubtotal();
    }

    /**
     * 更新折扣金額並重新計算小計
     *
     * @param newDiscountAmount 新的折扣金額
     */
    public void updateDiscountAmount(BigDecimal newDiscountAmount) {
        this.discountAmount = newDiscountAmount != null ? newDiscountAmount : BigDecimal.ZERO;
        calculateSubtotal();
    }
}
