package com.kamesan.erpapi.promotions.entity;

import com.kamesan.erpapi.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 優惠券實體
 *
 * <p>管理系統中的優惠券資訊，提供給顧客使用以獲取折扣優惠。</p>
 *
 * <p>優惠券特點：</p>
 * <ul>
 *   <li>擁有唯一的優惠碼 ({@link #code})，顧客使用時需輸入</li>
 *   <li>支援最低消費金額限制 ({@link #minOrderAmount})</li>
 *   <li>支援使用次數限制 ({@link #maxUses})</li>
 *   <li>自動追蹤已使用次數 ({@link #usedCount})</li>
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
 * // 建立一個滿500減100的優惠券
 * Coupon coupon = Coupon.builder()
 *     .code("SAVE100")
 *     .name("滿500減100優惠券")
 *     .discountType(DiscountType.FIXED_AMOUNT)
 *     .discountValue(new BigDecimal("100"))
 *     .minOrderAmount(new BigDecimal("500"))
 *     .startDate(LocalDateTime.now())
 *     .endDate(LocalDateTime.now().plusMonths(1))
 *     .maxUses(1000)
 *     .active(true)
 *     .build();
 * </pre>
 *
 * @author ERP System Team
 * @version 1.0.0
 * @see DiscountType 折扣類型枚舉
 */
@Entity
@Table(name = "coupons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coupon extends BaseEntity {

    /**
     * 優惠碼
     * <p>顧客使用優惠券時需輸入的唯一代碼</p>
     * <p>必須是唯一的，且不區分大小寫（建議統一使用大寫）</p>
     * <p>例如：「SAVE100」、「VIP2024」、「WELCOME」</p>
     */
    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    /**
     * 優惠券名稱
     * <p>用於識別和顯示優惠券的名稱</p>
     * <p>例如：「新會員首購優惠」、「滿千折百」</p>
     */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

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
     *   <li>PERCENTAGE：折扣百分比（例如：15 表示減 15%，即打 85 折）</li>
     *   <li>FIXED_AMOUNT：折扣金額（例如：100 表示減 100 元）</li>
     * </ul>
     * <p>精度：最多 10 位數，小數點後 2 位</p>
     */
    @Column(name = "discount_value", nullable = false, precision = 10, scale = 2)
    private BigDecimal discountValue;

    /**
     * 最低訂單金額限制
     * <p>訂單金額必須達到此門檻才能使用此優惠券</p>
     * <p>若為 null 或 0，表示無最低消費限制</p>
     * <p>精度：最多 10 位數，小數點後 2 位</p>
     */
    @Column(name = "min_order_amount", precision = 10, scale = 2)
    private BigDecimal minOrderAmount;

    /**
     * 優惠券開始日期時間
     * <p>優惠券生效的起始時間點</p>
     */
    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    /**
     * 優惠券結束日期時間
     * <p>優惠券失效的結束時間點</p>
     */
    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    /**
     * 最大使用次數
     * <p>限制此優惠券最多可被使用的總次數</p>
     * <p>若為 null，表示無使用次數限制</p>
     */
    @Column(name = "max_uses")
    private Integer maxUses;

    /**
     * 已使用次數
     * <p>追蹤此優惠券已被使用的次數</p>
     * <p>每次成功使用優惠券時應呼叫 {@link #incrementUsedCount()} 方法</p>
     */
    @Column(name = "used_count", nullable = false)
    @Builder.Default
    private Integer usedCount = 0;

    /**
     * 是否啟用
     * <p>控制優惠券是否可用，即使在有效期間內也可以手動停用</p>
     * <p>預設為 true（啟用）</p>
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;

    /**
     * 判斷優惠券是否有效
     *
     * <p>優惠券有效的條件：</p>
     * <ol>
     *   <li>優惠券已啟用 ({@link #active} = true)</li>
     *   <li>目前時間在開始日期之後</li>
     *   <li>目前時間在結束日期之前</li>
     *   <li>尚未達到最大使用次數（若有設定）</li>
     * </ol>
     *
     * @return true 表示優惠券有效，false 表示無效
     */
    public boolean isValid() {
        if (!active) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(startDate) || now.isAfter(endDate)) {
            return false;
        }
        // 檢查是否已達到最大使用次數
        if (maxUses != null && usedCount >= maxUses) {
            return false;
        }
        return true;
    }

    /**
     * 判斷優惠券是否尚未生效
     *
     * @return true 表示優惠券尚未生效，false 表示已生效或已過期
     */
    public boolean isUpcoming() {
        return LocalDateTime.now().isBefore(startDate);
    }

    /**
     * 判斷優惠券是否已過期
     *
     * @return true 表示優惠券已過期，false 表示尚未過期
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(endDate);
    }

    /**
     * 判斷優惠券是否已用完
     *
     * @return true 表示優惠券已達到最大使用次數，false 表示仍可使用
     */
    public boolean isExhausted() {
        return maxUses != null && usedCount >= maxUses;
    }

    /**
     * 檢查訂單金額是否符合最低消費限制
     *
     * @param orderAmount 訂單金額
     * @return true 表示符合最低消費限制，false 表示不符合
     */
    public boolean meetsMinOrderAmount(BigDecimal orderAmount) {
        if (minOrderAmount == null || minOrderAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return true;
        }
        if (orderAmount == null) {
            return false;
        }
        return orderAmount.compareTo(minOrderAmount) >= 0;
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

    /**
     * 增加使用次數
     * <p>每次成功使用優惠券後應呼叫此方法</p>
     */
    public void incrementUsedCount() {
        this.usedCount = (this.usedCount == null ? 0 : this.usedCount) + 1;
    }

    /**
     * 取得剩餘可用次數
     *
     * @return 剩餘可用次數，若無使用次數限制則返回 null
     */
    public Integer getRemainingUses() {
        if (maxUses == null) {
            return null;
        }
        int remaining = maxUses - (usedCount == null ? 0 : usedCount);
        return Math.max(0, remaining);
    }
}
