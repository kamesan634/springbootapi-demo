package com.kamesan.erpapi.products.entity;

import com.kamesan.erpapi.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * 商品條碼實體
 *
 * <p>用於管理商品的多條碼設定，一個商品可以有多個條碼。</p>
 *
 * <h2>主要欄位：</h2>
 * <ul>
 *   <li>product - 所屬商品</li>
 *   <li>barcode - 條碼號碼</li>
 *   <li>isPrimary - 是否為主要條碼</li>
 * </ul>
 *
 * <h2>使用場景：</h2>
 * <ul>
 *   <li>同商品不同包裝規格有不同條碼</li>
 *   <li>商品更換條碼但保留舊條碼查詢</li>
 *   <li>商品有多個供應商條碼</li>
 * </ul>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Entity
@Table(name = "product_barcodes", indexes = {
        @Index(name = "idx_product_barcodes_barcode", columnList = "barcode"),
        @Index(name = "idx_product_barcodes_product_id", columnList = "product_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductBarcode extends BaseEntity {

    /**
     * 所屬商品
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /**
     * 條碼號碼
     * <p>可以是 EAN-13、UPC、Code 128 等格式</p>
     */
    @Column(name = "barcode", nullable = false, length = 50)
    private String barcode;

    /**
     * 是否為主要條碼
     * <p>每個商品只能有一個主要條碼</p>
     * <p>主要條碼會同步到 Product.barcode 欄位</p>
     */
    @Column(name = "is_primary", nullable = false)
    @Builder.Default
    private boolean primary = false;

    /**
     * 條碼類型
     * <p>如：EAN13、UPC、CODE128 等</p>
     */
    @Column(name = "barcode_type", length = 20)
    private String barcodeType;

    /**
     * 備註
     * <p>條碼相關說明，如包裝規格等</p>
     */
    @Column(name = "notes", length = 200)
    private String notes;
}
