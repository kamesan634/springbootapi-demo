package com.kamesan.erpapi.products.entity;

import com.kamesan.erpapi.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 商品組合/套餐實體
 *
 * <p>用於定義商品組合（套餐），支援：</p>
 * <ul>
 *   <li>組合多個商品為一個套餐</li>
 *   <li>設定組合優惠價</li>
 *   <li>設定有效期間</li>
 *   <li>支援固定組合或選配組合</li>
 * </ul>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Entity
@Table(name = "product_combos", indexes = {
        @Index(name = "idx_product_combos_code", columnList = "code"),
        @Index(name = "idx_product_combos_active", columnList = "is_active"),
        @Index(name = "idx_product_combos_dates", columnList = "start_date, end_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductCombo extends BaseEntity {

    /**
     * 組合編碼
     */
    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    /**
     * 組合名稱
     */
    @Column(name = "name", nullable = false, length = 200)
    private String name;

    /**
     * 組合描述
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * 組合類型
     * FIXED - 固定組合（所有項目必選）
     * OPTIONAL - 選配組合（可選擇部分項目）
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "combo_type", nullable = false, length = 20)
    @Builder.Default
    private ComboType comboType = ComboType.FIXED;

    /**
     * 原價（組合內商品原價總和）
     */
    @Column(name = "original_price", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal originalPrice = BigDecimal.ZERO;

    /**
     * 組合售價
     */
    @Column(name = "combo_price", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal comboPrice = BigDecimal.ZERO;

    /**
     * 折扣金額
     */
    @Column(name = "discount_amount", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    /**
     * 開始日期
     */
    @Column(name = "start_date")
    private LocalDate startDate;

    /**
     * 結束日期
     */
    @Column(name = "end_date")
    private LocalDate endDate;

    /**
     * 是否啟用
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;

    /**
     * 最少選擇數量（選配組合用）
     */
    @Column(name = "min_select")
    @Builder.Default
    private Integer minSelect = 0;

    /**
     * 最多選擇數量（選配組合用）
     */
    @Column(name = "max_select")
    private Integer maxSelect;

    /**
     * 組合項目
     */
    @OneToMany(mappedBy = "combo", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductComboItem> items = new ArrayList<>();

    /**
     * 組合類型
     */
    public enum ComboType {
        FIXED("固定組合"),
        OPTIONAL("選配組合");

        private final String label;

        ComboType(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    /**
     * 新增組合項目
     */
    public void addItem(ProductComboItem item) {
        items.add(item);
        item.setCombo(this);
    }

    /**
     * 移除組合項目
     */
    public void removeItem(ProductComboItem item) {
        items.remove(item);
        item.setCombo(null);
    }

    /**
     * 計算原價總和
     */
    public BigDecimal calculateOriginalPrice() {
        return items.stream()
                .map(item -> item.getProduct().getSellingPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 計算折扣金額
     */
    public BigDecimal calculateDiscountAmount() {
        BigDecimal original = calculateOriginalPrice();
        return original.subtract(comboPrice);
    }

    /**
     * 檢查是否在有效期間內
     */
    public boolean isWithinValidPeriod() {
        LocalDate today = LocalDate.now();
        boolean afterStart = startDate == null || !today.isBefore(startDate);
        boolean beforeEnd = endDate == null || !today.isAfter(endDate);
        return afterStart && beforeEnd;
    }

    /**
     * 檢查組合是否可銷售
     */
    public boolean isSellable() {
        return active && isWithinValidPeriod();
    }
}
