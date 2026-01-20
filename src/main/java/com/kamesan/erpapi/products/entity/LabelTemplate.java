package com.kamesan.erpapi.products.entity;

import com.kamesan.erpapi.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * 標籤範本實體
 *
 * <p>用於定義標籤列印範本：</p>
 * <ul>
 *   <li>商品標籤</li>
 *   <li>價格標籤</li>
 *   <li>條碼標籤</li>
 *   <li>貨架標籤</li>
 * </ul>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Entity
@Table(name = "label_templates", indexes = {
        @Index(name = "idx_label_templates_type", columnList = "label_type"),
        @Index(name = "idx_label_templates_default", columnList = "is_default")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LabelTemplate extends BaseEntity {

    /**
     * 範本名稱
     */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /**
     * 範本描述
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * 標籤類型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "label_type", nullable = false, length = 30)
    private LabelType labelType;

    /**
     * 標籤寬度（mm）
     */
    @Column(name = "width", nullable = false, precision = 6, scale = 2)
    private BigDecimal width;

    /**
     * 標籤高度（mm）
     */
    @Column(name = "height", nullable = false, precision = 6, scale = 2)
    private BigDecimal height;

    /**
     * 每列標籤數
     */
    @Column(name = "columns_per_row")
    @Builder.Default
    private Integer columnsPerRow = 1;

    /**
     * 水平間距（mm）
     */
    @Column(name = "horizontal_gap", precision = 6, scale = 2)
    @Builder.Default
    private BigDecimal horizontalGap = BigDecimal.ZERO;

    /**
     * 垂直間距（mm）
     */
    @Column(name = "vertical_gap", precision = 6, scale = 2)
    @Builder.Default
    private BigDecimal verticalGap = BigDecimal.ZERO;

    /**
     * 頁面上邊距（mm）
     */
    @Column(name = "margin_top", precision = 6, scale = 2)
    @Builder.Default
    private BigDecimal marginTop = BigDecimal.ZERO;

    /**
     * 頁面左邊距（mm）
     */
    @Column(name = "margin_left", precision = 6, scale = 2)
    @Builder.Default
    private BigDecimal marginLeft = BigDecimal.ZERO;

    /**
     * 條碼類型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "barcode_type", length = 20)
    @Builder.Default
    private BarcodeType barcodeType = BarcodeType.EAN13;

    /**
     * 條碼寬度（mm）
     */
    @Column(name = "barcode_width", precision = 6, scale = 2)
    private BigDecimal barcodeWidth;

    /**
     * 條碼高度（mm）
     */
    @Column(name = "barcode_height", precision = 6, scale = 2)
    private BigDecimal barcodeHeight;

    /**
     * 顯示商品名稱
     */
    @Column(name = "show_product_name")
    @Builder.Default
    private boolean showProductName = true;

    /**
     * 顯示貨號
     */
    @Column(name = "show_sku")
    @Builder.Default
    private boolean showSku = true;

    /**
     * 顯示價格
     */
    @Column(name = "show_price")
    @Builder.Default
    private boolean showPrice = true;

    /**
     * 顯示條碼
     */
    @Column(name = "show_barcode")
    @Builder.Default
    private boolean showBarcode = true;

    /**
     * 顯示條碼數字
     */
    @Column(name = "show_barcode_text")
    @Builder.Default
    private boolean showBarcodeText = true;

    /**
     * 字體大小（pt）
     */
    @Column(name = "font_size")
    @Builder.Default
    private Integer fontSize = 10;

    /**
     * 價格字體大小（pt）
     */
    @Column(name = "price_font_size")
    @Builder.Default
    private Integer priceFontSize = 14;

    /**
     * 是否為預設範本
     */
    @Column(name = "is_default")
    @Builder.Default
    private boolean isDefault = false;

    /**
     * 自訂樣式（CSS 或設定 JSON）
     */
    @Column(name = "custom_style", columnDefinition = "TEXT")
    private String customStyle;

    /**
     * 標籤類型
     */
    public enum LabelType {
        PRODUCT("商品標籤"),
        PRICE("價格標籤"),
        BARCODE("條碼標籤"),
        SHELF("貨架標籤");

        private final String label;

        LabelType(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    /**
     * 條碼類型
     */
    public enum BarcodeType {
        EAN13("EAN-13"),
        EAN8("EAN-8"),
        CODE128("Code 128"),
        CODE39("Code 39"),
        QR("QR Code");

        private final String label;

        BarcodeType(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }
}
