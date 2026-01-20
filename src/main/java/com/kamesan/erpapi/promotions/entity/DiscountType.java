package com.kamesan.erpapi.promotions.entity;

/**
 * 折扣類型枚舉
 *
 * <p>定義系統支援的折扣計算方式，包括：</p>
 * <ul>
 *   <li>{@link #PERCENTAGE} - 百分比折扣：依照設定的百分比進行折扣</li>
 *   <li>{@link #FIXED_AMOUNT} - 固定金額折扣：直接扣除固定金額</li>
 * </ul>
 *
 * <p>此枚舉用於促銷活動 ({@link Promotion}) 和優惠券 ({@link Coupon}) 的折扣計算。</p>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
public enum DiscountType {

    /**
     * 百分比折扣
     * <p>依照設定的百分比對商品或訂單進行折扣</p>
     * <p>例如：discount_value = 10 表示打9折（減10%）</p>
     */
    PERCENTAGE("百分比折扣"),

    /**
     * 固定金額折扣
     * <p>直接從商品或訂單金額中扣除固定金額</p>
     * <p>例如：discount_value = 100 表示直接減100元</p>
     */
    FIXED_AMOUNT("固定金額折扣");

    /**
     * 折扣類型的中文描述
     */
    private final String description;

    /**
     * 建構子
     *
     * @param description 折扣類型的中文描述
     */
    DiscountType(String description) {
        this.description = description;
    }

    /**
     * 取得折扣類型的中文描述
     *
     * @return 折扣類型的中文描述
     */
    public String getDescription() {
        return description;
    }
}
