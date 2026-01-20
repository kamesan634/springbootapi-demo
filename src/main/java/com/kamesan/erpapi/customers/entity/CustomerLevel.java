package com.kamesan.erpapi.customers.entity;

import com.kamesan.erpapi.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

/**
 * 會員等級實體
 *
 * <p>定義會員等級及其相關權益，包括：</p>
 * <ul>
 *   <li>折扣比率 - 該等級會員可享有的折扣</li>
 *   <li>點數倍率 - 消費時獲得點數的倍率</li>
 *   <li>升級條件 - 升級至該等級所需的消費金額</li>
 * </ul>
 *
 * <h2>預設等級：</h2>
 * <ul>
 *   <li>NORMAL - 普通會員</li>
 *   <li>SILVER - 銀卡會員</li>
 *   <li>GOLD - 金卡會員</li>
 *   <li>PLATINUM - 白金會員</li>
 *   <li>DIAMOND - 鑽石會員</li>
 * </ul>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Entity
@Table(name = "customer_levels")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerLevel extends BaseEntity {

    /**
     * 等級代碼
     * <p>唯一識別碼，如：NORMAL、SILVER、GOLD、PLATINUM、DIAMOND</p>
     */
    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    /**
     * 等級名稱
     * <p>顯示用的名稱，如：普通會員、銀卡會員等</p>
     */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /**
     * 等級描述
     * <p>詳細說明該等級的權益和特色</p>
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * 折扣比率
     * <p>該等級會員可享有的折扣比率，例如 0.95 代表 95 折</p>
     * <p>數值範圍：0.00 ~ 1.00</p>
     */
    @Column(name = "discount_rate", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal discountRate = BigDecimal.ONE;

    /**
     * 點數倍率
     * <p>消費時獲得點數的倍率，例如 1.5 代表 1.5 倍點數</p>
     * <p>一般為 1.0 以上的數值</p>
     */
    @Column(name = "points_multiplier", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal pointsMultiplier = BigDecimal.ONE;

    /**
     * 升級條件（累積消費金額）
     * <p>達到此金額後可升級至該等級</p>
     * <p>設為 0 表示無條件（如普通會員）</p>
     */
    @Column(name = "upgrade_condition", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal upgradeCondition = BigDecimal.ZERO;

    /**
     * 是否啟用
     * <p>停用的等級不會顯示在前台，也不會進行自動升級</p>
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;

    /**
     * 排序順序
     * <p>用於前台顯示時的排序，數字越小越前面</p>
     * <p>建議等級越高排序越大，如：普通=1, 銀卡=2, 金卡=3</p>
     */
    @Column(name = "sort_order")
    @Builder.Default
    private Integer sortOrder = 0;

    /**
     * 該等級的會員
     * <p>一對多關聯，用於查詢該等級下的所有會員</p>
     */
    @OneToMany(mappedBy = "level", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Customer> customers = new HashSet<>();

    /**
     * 計算折扣後的金額
     *
     * @param originalAmount 原始金額
     * @return 折扣後金額
     */
    public BigDecimal calculateDiscountedAmount(BigDecimal originalAmount) {
        if (originalAmount == null) {
            return BigDecimal.ZERO;
        }
        return originalAmount.multiply(discountRate);
    }

    /**
     * 計算可獲得的點數
     *
     * @param spentAmount 消費金額
     * @param basePoints  基礎點數（每元可得幾點）
     * @return 可獲得的點數
     */
    public int calculatePoints(BigDecimal spentAmount, BigDecimal basePoints) {
        if (spentAmount == null || basePoints == null) {
            return 0;
        }
        return spentAmount.multiply(basePoints).multiply(pointsMultiplier).intValue();
    }

    /**
     * 判斷是否可升級至此等級
     *
     * @param totalSpent 累積消費金額
     * @return 是否可升級
     */
    public boolean canUpgrade(BigDecimal totalSpent) {
        if (totalSpent == null) {
            return false;
        }
        return totalSpent.compareTo(upgradeCondition) >= 0;
    }
}
