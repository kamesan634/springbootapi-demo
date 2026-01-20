package com.kamesan.erpapi.products.entity;

import com.kamesan.erpapi.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * 稅別實體
 *
 * <p>用於定義商品的稅務類型，包含稅率設定。</p>
 *
 * <h2>主要欄位：</h2>
 * <ul>
 *   <li>code - 稅別代碼（唯一識別碼）</li>
 *   <li>name - 稅別名稱</li>
 *   <li>rate - 稅率（百分比）</li>
 *   <li>isDefault - 是否為預設稅別</li>
 *   <li>isActive - 是否啟用</li>
 * </ul>
 *
 * <h2>常見稅別範例：</h2>
 * <ul>
 *   <li>應稅 - 5%</li>
 *   <li>零稅率 - 0%</li>
 *   <li>免稅 - 0%</li>
 * </ul>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Entity
@Table(name = "tax_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaxType extends BaseEntity {

    /**
     * 稅別代碼
     * <p>唯一識別碼，如：TAX、ZERO、EXEMPT</p>
     */
    @Column(name = "code", nullable = false, unique = true, length = 20)
    private String code;

    /**
     * 稅別名稱
     * <p>顯示用名稱，如：應稅、零稅率、免稅</p>
     */
    @Column(name = "name", nullable = false, length = 50)
    private String name;

    /**
     * 稅率
     * <p>以百分比表示，如 5.00 表示 5%</p>
     * <p>使用 BigDecimal 確保精確計算</p>
     */
    @Column(name = "rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal rate;

    /**
     * 是否為預設稅別
     * <p>新增商品時預設使用的稅別</p>
     * <p>系統中應只有一個預設稅別</p>
     */
    @Column(name = "is_default", nullable = false)
    @Builder.Default
    private boolean isDefault = false;

    /**
     * 是否啟用
     * <p>停用的稅別不會出現在選單中，但已使用該稅別的商品不受影響</p>
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;

    /**
     * 計算含稅金額
     *
     * @param amount 未稅金額
     * @return 含稅金額
     */
    public BigDecimal calculateTaxIncludedAmount(BigDecimal amount) {
        if (amount == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal taxMultiplier = BigDecimal.ONE.add(rate.divide(new BigDecimal("100")));
        return amount.multiply(taxMultiplier);
    }

    /**
     * 計算稅額
     *
     * @param amount 未稅金額
     * @return 稅額
     */
    public BigDecimal calculateTaxAmount(BigDecimal amount) {
        if (amount == null) {
            return BigDecimal.ZERO;
        }
        return amount.multiply(rate).divide(new BigDecimal("100"));
    }
}
