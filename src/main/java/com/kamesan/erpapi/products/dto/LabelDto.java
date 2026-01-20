package com.kamesan.erpapi.products.dto;

import com.kamesan.erpapi.products.entity.LabelTemplate.BarcodeType;
import com.kamesan.erpapi.products.entity.LabelTemplate.LabelType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 標籤列印 DTO
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Data
@Builder
public class LabelDto {

    /**
     * 標籤範本 DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LabelTemplateDto {
        private Long id;
        private String name;
        private String description;
        private LabelType labelType;
        private String labelTypeLabel;
        private BigDecimal width;
        private BigDecimal height;
        private Integer columnsPerRow;
        private BigDecimal horizontalGap;
        private BigDecimal verticalGap;
        private BigDecimal marginTop;
        private BigDecimal marginLeft;
        private BarcodeType barcodeType;
        private String barcodeTypeLabel;
        private BigDecimal barcodeWidth;
        private BigDecimal barcodeHeight;
        private boolean showProductName;
        private boolean showSku;
        private boolean showPrice;
        private boolean showBarcode;
        private boolean showBarcodeText;
        private Integer fontSize;
        private Integer priceFontSize;
        private boolean isDefault;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    /**
     * 建立範本請求
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateTemplateRequest {
        private String name;
        private String description;
        private LabelType labelType;
        private BigDecimal width;
        private BigDecimal height;
        private Integer columnsPerRow;
        private BigDecimal horizontalGap;
        private BigDecimal verticalGap;
        private BigDecimal marginTop;
        private BigDecimal marginLeft;
        private BarcodeType barcodeType;
        private BigDecimal barcodeWidth;
        private BigDecimal barcodeHeight;
        private boolean showProductName;
        private boolean showSku;
        private boolean showPrice;
        private boolean showBarcode;
        private boolean showBarcodeText;
        private Integer fontSize;
        private Integer priceFontSize;
    }

    /**
     * 列印請求
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PrintRequest {
        private Long templateId;
        private List<PrintItem> items;
        private String outputFormat;  // PDF, ZPL, HTML
    }

    /**
     * 列印項目
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PrintItem {
        private Long productId;
        private Integer quantity;
        private BigDecimal customPrice;
        private String customText;
    }

    /**
     * 標籤資料（用於產生標籤）
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LabelData {
        private Long productId;
        private String productName;
        private String sku;
        private String barcode;
        private BigDecimal price;
        private String unitName;
        private String categoryName;
        private String customText;
    }

    /**
     * 列印預覽回應
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PrintPreviewResponse {
        private LabelTemplateDto template;
        private List<LabelData> labels;
        private Integer totalLabels;
        private Integer totalPages;
        private String previewHtml;
    }

    /**
     * 列印結果
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PrintResult {
        private boolean success;
        private String message;
        private String filePath;
        private byte[] content;
        private String contentType;
        private Integer printedCount;
    }
}
