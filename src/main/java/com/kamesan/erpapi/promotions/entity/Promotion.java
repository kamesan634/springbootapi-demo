package com.kamesan.erpapi.promotions.entity;

import com.kamesan.erpapi.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 促銷活動實體
 *
 * <p>管理系統中的促銷活動資訊，支援多種促銷類型和折扣方式。</p>
 *
 * <p>支援的促銷類型 ({@link PromotionType})：</p>
 * <ul>
 *   <li>DISCOUNT - 折扣活動</li>
 *   <li>BUY_X_GET_Y - 買X送Y</li>
 *   <li>BUNDLE - 組合優惠</li>
 * </ul>
 *
 * <p>支援的折扣方式 ({@link DiscountType})：</p>
 * <ul>
 *   <li>PERCENTAGE - 百分比折扣</li>
 *   <li>FIXED_AMOUNT - 固定金額折扣</li>
 * </ul>
 *
 * <p>使用範例：</p>
 * <pre>
 * // 建立一個打8折的促銷活動
 * Promotion promotion = Promotion.builder()
 *     .name("週年慶大特價")
 *     .description("全館商品8折優惠")
 *     .type(PromotionType.DISCOUNT)
 *     .discountType(DiscountType.PERCENTAGE)
 *     .discountValue(new BigDecimal("20"))
 *     .startDate(LocalDateTime.now())
 *     .endDate(LocalDateTime.now().plusDays(7))
 *     .active(true)
 *     .build();
 * </pre>
 *
 * @author ERP System Team
 * @version 1.0.0
 * @see PromotionType 促銷活動類型枚舉
 * @see DiscountType 折扣類型枚舉
 */
@Entity
@Table(name = "promotions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Promotion extends BaseEntity {

    /**
     * 促銷活動名稱
     * <p>用於識別和顯示促銷活動的名稱</p>
     * <p>例如：「週年慶特惠」、「雙11購物節」</p>
     */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /**
     * 促銷活動描述
     * <p>詳細說明促銷活動的內容和規則</p>
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * 促銷活動類型
     * <p>定義促銷活動的運作方式</p>
     *
     * @see PromotionType
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private PromotionType type;

    /**
     * 折扣類型
     * <p>定義折扣的計算方式：百分比或固定金額</p>
     *
     * @see DiscountType
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false, length = 20)
    private DiscountType discountType;

    /**
     * 折扣值
     * <p>根據 {@link #discountType} 的不同有不同的意義：</p>
     * <ul>
     *   <li>PERCENTAGE：折扣百分比（例如：20 表示減 20%，即打 8 折）</li>
     *   <li>FIXED_AMOUNT：折扣金額（例如：100 表示減 100 元）</li>
     * </ul>
     * <p>精度：最多 10 位數，小數點後 2 位</p>
     */
    @Column(name = "discount_value", nullable = false, precision = 10, scale = 2)
    private BigDecimal discountValue;

    /**
     * 促銷活動開始日期時間
     * <p>促銷活動生效的起始時間點</p>
     */
    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    /**
     * 促銷活動結束日期時間
     * <p>促銷活動失效的結束時間點</p>
     */
    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    /**
     * 是否啟用
     * <p>控制促銷活動是否可用，即使在有效期間內也可以手動停用</p>
     * <p>預設為 true（啟用）</p>
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;

    /**
     * 判斷促銷活動是否正在進行中
     *
     * <p>促銷活動正在進行的條件：</p>
     * <ol>
     *   <li>活動已啟用 ({@link #active} = true)</li>
     *   <li>目前時間在開始日期之後</li>
     *   <li>目前時間在結束日期之前</li>
     * </ol>
     *
     * @return true 表示促銷活動正在進行中，false 表示未進行
     */
    public boolean isOngoing() {
        if (!active) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(startDate) && now.isBefore(endDate);
    }

    /**
     * 判斷促銷活動是否尚未開始
     *
     * @return true 表示促銷活動尚未開始，false 表示已開始或已結束
     */
    public boolean isUpcoming() {
        return LocalDateTime.now().isBefore(startDate);
    }

    /**
     * 判斷促銷活動是否已結束
     *
     * @return true 表示促銷活動已結束，false 表示尚未結束
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(endDate);
    }

    /**
     * 計算折扣金額
     *
     * <p>根據折扣類型計算實際折扣金額：</p>
     * <ul>
     *   <li>PERCENTAGE：原始金額 * (折扣百分比 / 100)</li>
     *   <li>FIXED_AMOUNT：直接返回固定折扣金額（不超過原始金額）</li>
     * </ul>
     *
     * @param originalAmount 原始金額
     * @return 折扣金額（不會超過原始金額）
     */
    public BigDecimal calculateDiscount(BigDecimal originalAmount) {
        if (originalAmount == null || originalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal discount;
        if (discountType == DiscountType.PERCENTAGE) {
            // 百分比折扣
            discount = originalAmount.multiply(discountValue)
                    .divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
        } else {
            // 固定金額折扣
            discount = discountValue;
        }

        // 折扣金額不能超過原始金額
        return discount.min(originalAmount);
    }
}
