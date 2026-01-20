package com.kamesan.erpapi.products.entity;

import com.kamesan.erpapi.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * 商品組合項目實體
 *
 * <p>定義組合中的單一商品項目：</p>
 * <ul>
 *   <li>關聯的商品</li>
 *   <li>數量</li>
 *   <li>是否為必選項</li>
 *   <li>項目分組（用於選配組合）</li>
 * </ul>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Entity
@Table(name = "product_combo_items", indexes = {
        @Index(name = "idx_combo_items_combo_id", columnList = "combo_id"),
        @Index(name = "idx_combo_items_product_id", columnList = "product_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductComboItem extends BaseEntity {

    /**
     * 所屬組合
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "combo_id", nullable = false)
    private ProductCombo combo;

    /**
     * 關聯商品
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /**
     * 數量
     */
    @Column(name = "quantity", nullable = false)
    @Builder.Default
    private Integer quantity = 1;

    /**
     * 是否為必選項
     * 用於選配組合，標記某些項目為必選
     */
    @Column(name = "is_required")
    @Builder.Default
    private boolean required = false;

    /**
     * 項目分組
     * 用於選配組合，同一分組只能選一個
     */
    @Column(name = "group_name", length = 50)
    private String groupName;

    /**
     * 排序順序
     */
    @Column(name = "sort_order")
    @Builder.Default
    private Integer sortOrder = 0;

    /**
     * 備註
     */
    @Column(name = "remark", length = 500)
    private String remark;

    /**
     * 計算項目小計（原價）
     */
    public BigDecimal calculateSubtotal() {
        if (product == null || product.getSellingPrice() == null) {
            return BigDecimal.ZERO;
        }
        return product.getSellingPrice().multiply(BigDecimal.valueOf(quantity));
    }
}
