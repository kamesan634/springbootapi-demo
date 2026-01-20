package com.kamesan.erpapi.products.entity;

import com.kamesan.erpapi.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 商品實體
 *
 * <p>核心商品資料實體，包含商品的基本資訊、價格、庫存設定等。</p>
 *
 * <h2>主要欄位：</h2>
 * <ul>
 *   <li>sku - 商品貨號（唯一識別碼）</li>
 *   <li>name - 商品名稱</li>
 *   <li>description - 商品描述</li>
 *   <li>category - 商品分類</li>
 *   <li>unit - 計量單位</li>
 *   <li>taxType - 稅別</li>
 *   <li>costPrice - 成本價</li>
 *   <li>sellingPrice - 售價</li>
 *   <li>barcode - 主要條碼</li>
 *   <li>safetyStock - 安全庫存量</li>
 *   <li>isActive - 是否啟用</li>
 * </ul>
 *
 * <h2>關聯實體：</h2>
 * <ul>
 *   <li>Category - 商品分類（多對一）</li>
 *   <li>Unit - 計量單位（多對一）</li>
 *   <li>TaxType - 稅別（多對一）</li>
 *   <li>ProductBarcode - 商品條碼（一對多）</li>
 * </ul>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Entity
@Table(name = "products", indexes = {
        @Index(name = "idx_products_sku", columnList = "sku"),
        @Index(name = "idx_products_barcode", columnList = "barcode"),
        @Index(name = "idx_products_category_id", columnList = "category_id"),
        @Index(name = "idx_products_is_active", columnList = "is_active")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product extends BaseEntity {

    /**
     * 商品貨號（SKU）
     * <p>Stock Keeping Unit，唯一識別商品的編碼</p>
     * <p>通常用於內部管理和庫存追蹤</p>
     */
    @Column(name = "sku", nullable = false, unique = true, length = 50)
    private String sku;

    /**
     * 商品名稱
     */
    @Column(name = "name", nullable = false, length = 200)
    private String name;

    /**
     * 商品描述
     * <p>詳細的商品說明文字</p>
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * 商品分類
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    /**
     * 計量單位
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_id")
    private Unit unit;

    /**
     * 稅別
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tax_type_id")
    private TaxType taxType;

    /**
     * 成本價
     * <p>商品的進貨成本價格</p>
     * <p>使用 BigDecimal 確保金額計算的精確性</p>
     */
    @Column(name = "cost_price", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal costPrice = BigDecimal.ZERO;

    /**
     * 售價
     * <p>商品的銷售價格</p>
     */
    @Column(name = "selling_price", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal sellingPrice = BigDecimal.ZERO;

    /**
     * 主要條碼
     * <p>商品的主要條碼，通常為 EAN-13 或 UPC 碼</p>
     */
    @Column(name = "barcode", length = 50)
    private String barcode;

    /**
     * 安全庫存量
     * <p>當庫存低於此數量時會發出預警</p>
     */
    @Column(name = "safety_stock")
    @Builder.Default
    private Integer safetyStock = 0;

    /**
     * 是否啟用
     * <p>停用的商品不會出現在銷售選單中，但歷史交易記錄不受影響</p>
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;

    /**
     * 商品的所有條碼
     * <p>一個商品可以有多個條碼（如不同包裝規格）</p>
     */
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductBarcode> barcodes = new ArrayList<>();

    /**
     * 計算毛利
     *
     * @return 毛利（售價 - 成本價）
     */
    public BigDecimal calculateGrossProfit() {
        if (sellingPrice == null || costPrice == null) {
            return BigDecimal.ZERO;
        }
        return sellingPrice.subtract(costPrice);
    }

    /**
     * 計算毛利率
     *
     * @return 毛利率（百分比）
     */
    public BigDecimal calculateGrossProfitMargin() {
        if (sellingPrice == null || sellingPrice.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal profit = calculateGrossProfit();
        return profit.multiply(new BigDecimal("100"))
                .divide(sellingPrice, 2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * 計算含稅售價
     *
     * @return 含稅售價
     */
    public BigDecimal calculateTaxIncludedPrice() {
        if (taxType == null) {
            return sellingPrice;
        }
        return taxType.calculateTaxIncludedAmount(sellingPrice);
    }

    /**
     * 新增條碼
     *
     * @param productBarcode 商品條碼
     */
    public void addBarcode(ProductBarcode productBarcode) {
        barcodes.add(productBarcode);
        productBarcode.setProduct(this);
    }

    /**
     * 移除條碼
     *
     * @param productBarcode 商品條碼
     */
    public void removeBarcode(ProductBarcode productBarcode) {
        barcodes.remove(productBarcode);
        productBarcode.setProduct(null);
    }

    /**
     * 取得主要條碼物件
     *
     * @return 主要條碼物件，若無則回傳 null
     */
    public ProductBarcode getPrimaryBarcodeEntity() {
        return barcodes.stream()
                .filter(ProductBarcode::isPrimary)
                .findFirst()
                .orElse(null);
    }
}
