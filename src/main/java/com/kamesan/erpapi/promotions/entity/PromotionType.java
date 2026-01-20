package com.kamesan.erpapi.promotions.entity;

/**
 * 促銷活動類型枚舉
 *
 * <p>定義系統支援的促銷活動類型，包括：</p>
 * <ul>
 *   <li>{@link #DISCOUNT} - 折扣活動：直接對商品或訂單進行折扣</li>
 *   <li>{@link #BUY_X_GET_Y} - 買X送Y活動：購買指定數量可獲得贈品或免費商品</li>
 *   <li>{@link #BUNDLE} - 組合優惠活動：多項商品組合購買享有特價</li>
 * </ul>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
public enum PromotionType {

    /**
     * 折扣活動
     * <p>直接對商品或訂單進行折扣，可搭配 {@link DiscountType} 設定折扣方式</p>
     */
    DISCOUNT("折扣活動"),

    /**
     * 買X送Y活動
     * <p>購買指定數量的商品後，可獲得額外贈品或免費商品</p>
     * <p>例如：買2送1、買5送2</p>
     */
    BUY_X_GET_Y("買X送Y"),

    /**
     * 組合優惠活動
     * <p>多項商品組合購買時享有特別優惠價格</p>
     * <p>例如：主餐+飲料組合特價</p>
     */
    BUNDLE("組合優惠");

    /**
     * 促銷類型的中文描述
     */
    private final String description;

    /**
     * 建構子
     *
     * @param description 促銷類型的中文描述
     */
    PromotionType(String description) {
        this.description = description;
    }

    /**
     * 取得促銷類型的中文描述
     *
     * @return 促銷類型的中文描述
     */
    public String getDescription() {
        return description;
    }
}
