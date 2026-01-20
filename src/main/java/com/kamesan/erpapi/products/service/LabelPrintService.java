package com.kamesan.erpapi.products.service;

import com.kamesan.erpapi.common.exception.BusinessException;
import com.kamesan.erpapi.products.dto.LabelDto.*;
import com.kamesan.erpapi.products.entity.LabelTemplate;
import com.kamesan.erpapi.products.entity.LabelTemplate.LabelType;
import com.kamesan.erpapi.products.entity.Product;
import com.kamesan.erpapi.products.repository.LabelTemplateRepository;
import com.kamesan.erpapi.products.repository.ProductRepository;
import com.lowagie.text.*;
import com.lowagie.text.pdf.Barcode128;
import com.lowagie.text.pdf.BarcodeEAN;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 標籤列印服務
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LabelPrintService {

    private final LabelTemplateRepository templateRepository;
    private final ProductRepository productRepository;

    /**
     * 建立標籤範本
     */
    @Transactional
    public LabelTemplateDto createTemplate(CreateTemplateRequest request) {
        log.info("建立標籤範本: {}", request.getName());

        if (templateRepository.existsByName(request.getName())) {
            throw new BusinessException("範本名稱已存在: " + request.getName());
        }

        LabelTemplate template = LabelTemplate.builder()
                .name(request.getName())
                .description(request.getDescription())
                .labelType(request.getLabelType())
                .width(request.getWidth())
                .height(request.getHeight())
                .columnsPerRow(request.getColumnsPerRow() != null ? request.getColumnsPerRow() : 1)
                .horizontalGap(request.getHorizontalGap())
                .verticalGap(request.getVerticalGap())
                .marginTop(request.getMarginTop())
                .marginLeft(request.getMarginLeft())
                .barcodeType(request.getBarcodeType())
                .barcodeWidth(request.getBarcodeWidth())
                .barcodeHeight(request.getBarcodeHeight())
                .showProductName(request.isShowProductName())
                .showSku(request.isShowSku())
                .showPrice(request.isShowPrice())
                .showBarcode(request.isShowBarcode())
                .showBarcodeText(request.isShowBarcodeText())
                .fontSize(request.getFontSize() != null ? request.getFontSize() : 10)
                .priceFontSize(request.getPriceFontSize() != null ? request.getPriceFontSize() : 14)
                .isDefault(false)
                .build();

        LabelTemplate saved = templateRepository.save(template);
        log.info("標籤範本建立成功: {}", saved.getId());

        return convertToDto(saved);
    }

    /**
     * 取得範本
     */
    @Transactional(readOnly = true)
    public LabelTemplateDto getTemplate(Long id) {
        LabelTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new BusinessException("範本不存在: " + id));
        return convertToDto(template);
    }

    /**
     * 取得所有範本
     */
    @Transactional(readOnly = true)
    public List<LabelTemplateDto> getAllTemplates() {
        return templateRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 根據類型取得範本
     */
    @Transactional(readOnly = true)
    public List<LabelTemplateDto> getTemplatesByType(LabelType labelType) {
        return templateRepository.findByLabelType(labelType).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 取得預設範本
     */
    @Transactional(readOnly = true)
    public LabelTemplateDto getDefaultTemplate(LabelType labelType) {
        LabelTemplate template = templateRepository.findByLabelTypeAndIsDefaultTrue(labelType)
                .orElseThrow(() -> new BusinessException("找不到預設範本: " + labelType));
        return convertToDto(template);
    }

    /**
     * 設為預設範本
     */
    @Transactional
    public LabelTemplateDto setAsDefault(Long id) {
        LabelTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new BusinessException("範本不存在: " + id));

        // 取消同類型的其他預設
        templateRepository.clearDefaultByType(template.getLabelType(), id);

        template.setDefault(true);
        LabelTemplate saved = templateRepository.save(template);

        log.info("已設定預設範本: {} ({})", saved.getName(), saved.getLabelType());
        return convertToDto(saved);
    }

    /**
     * 刪除範本
     */
    @Transactional
    public void deleteTemplate(Long id) {
        LabelTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new BusinessException("範本不存在: " + id));

        if (template.isDefault()) {
            throw new BusinessException("無法刪除預設範本");
        }

        templateRepository.delete(template);
        log.info("標籤範本已刪除: {}", id);
    }

    /**
     * 產生列印預覽
     */
    @Transactional(readOnly = true)
    public PrintPreviewResponse generatePreview(PrintRequest request) {
        LabelTemplate template = templateRepository.findById(request.getTemplateId())
                .orElseThrow(() -> new BusinessException("範本不存在: " + request.getTemplateId()));

        List<LabelData> labels = new ArrayList<>();

        for (PrintItem item : request.getItems()) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new BusinessException("商品不存在: " + item.getProductId()));

            for (int i = 0; i < item.getQuantity(); i++) {
                labels.add(LabelData.builder()
                        .productId(product.getId())
                        .productName(product.getName())
                        .sku(product.getSku())
                        .barcode(product.getBarcode())
                        .price(item.getCustomPrice() != null ? item.getCustomPrice() : product.getSellingPrice())
                        .unitName(product.getUnit() != null ? product.getUnit().getName() : null)
                        .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                        .customText(item.getCustomText())
                        .build());
            }
        }

        int labelsPerPage = template.getColumnsPerRow() * calculateRowsPerPage(template);
        int totalPages = (int) Math.ceil((double) labels.size() / labelsPerPage);

        String previewHtml = generatePreviewHtml(template, labels);

        return PrintPreviewResponse.builder()
                .template(convertToDto(template))
                .labels(labels)
                .totalLabels(labels.size())
                .totalPages(totalPages)
                .previewHtml(previewHtml)
                .build();
    }

    /**
     * 列印標籤（PDF）
     */
    @Transactional(readOnly = true)
    public PrintResult printLabelsPdf(PrintRequest request) {
        log.info("產生標籤 PDF");

        LabelTemplate template = templateRepository.findById(request.getTemplateId())
                .orElseThrow(() -> new BusinessException("範本不存在: " + request.getTemplateId()));

        List<LabelData> labels = prepareLabelData(request);

        try {
            byte[] pdfContent = generatePdf(template, labels);

            return PrintResult.builder()
                    .success(true)
                    .message("標籤產生成功")
                    .content(pdfContent)
                    .contentType("application/pdf")
                    .printedCount(labels.size())
                    .build();
        } catch (Exception e) {
            log.error("標籤產生失敗", e);
            return PrintResult.builder()
                    .success(false)
                    .message("標籤產生失敗: " + e.getMessage())
                    .build();
        }
    }

    /**
     * 列印標籤（HTML）
     */
    @Transactional(readOnly = true)
    public PrintResult printLabelsHtml(PrintRequest request) {
        LabelTemplate template = templateRepository.findById(request.getTemplateId())
                .orElseThrow(() -> new BusinessException("範本不存在: " + request.getTemplateId()));

        List<LabelData> labels = prepareLabelData(request);
        String html = generatePrintableHtml(template, labels);

        return PrintResult.builder()
                .success(true)
                .message("標籤產生成功")
                .content(html.getBytes())
                .contentType("text/html")
                .printedCount(labels.size())
                .build();
    }

    private List<LabelData> prepareLabelData(PrintRequest request) {
        List<LabelData> labels = new ArrayList<>();

        for (PrintItem item : request.getItems()) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new BusinessException("商品不存在: " + item.getProductId()));

            for (int i = 0; i < item.getQuantity(); i++) {
                labels.add(LabelData.builder()
                        .productId(product.getId())
                        .productName(product.getName())
                        .sku(product.getSku())
                        .barcode(product.getBarcode())
                        .price(item.getCustomPrice() != null ? item.getCustomPrice() : product.getSellingPrice())
                        .unitName(product.getUnit() != null ? product.getUnit().getName() : null)
                        .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                        .customText(item.getCustomText())
                        .build());
            }
        }

        return labels;
    }

    private byte[] generatePdf(LabelTemplate template, List<LabelData> labels) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // 計算頁面尺寸
        float pageWidth = template.getWidth().floatValue() * template.getColumnsPerRow()
                + template.getHorizontalGap().floatValue() * (template.getColumnsPerRow() - 1)
                + template.getMarginLeft().floatValue() * 2;
        pageWidth = mmToPoints(pageWidth);

        // A4 高度
        float pageHeight = mmToPoints(297);

        Document document = new Document(new Rectangle(pageWidth, pageHeight));
        PdfWriter writer = PdfWriter.getInstance(document, baos);
        document.open();

        PdfContentByte cb = writer.getDirectContent();
        Font nameFont = new Font(Font.HELVETICA, template.getFontSize());
        Font priceFont = new Font(Font.HELVETICA, template.getPriceFontSize(), Font.BOLD);

        float labelWidth = mmToPoints(template.getWidth().floatValue());
        float labelHeight = mmToPoints(template.getHeight().floatValue());
        float marginLeft = mmToPoints(template.getMarginLeft().floatValue());
        float marginTop = mmToPoints(template.getMarginTop().floatValue());
        float hGap = mmToPoints(template.getHorizontalGap().floatValue());
        float vGap = mmToPoints(template.getVerticalGap().floatValue());

        int cols = template.getColumnsPerRow();
        int labelIndex = 0;

        float y = pageHeight - marginTop;

        for (LabelData label : labels) {
            int col = labelIndex % cols;
            float x = marginLeft + col * (labelWidth + hGap);

            // 繪製標籤內容
            float currentY = y;

            if (template.isShowProductName()) {
                cb.beginText();
                cb.setFontAndSize(nameFont.getBaseFont(), template.getFontSize());
                cb.setTextMatrix(x + 2, currentY - 12);
                String name = label.getProductName();
                if (name != null && name.length() > 20) {
                    name = name.substring(0, 20) + "...";
                }
                cb.showText(name != null ? name : "");
                cb.endText();
                currentY -= 14;
            }

            if (template.isShowSku()) {
                cb.beginText();
                cb.setFontAndSize(nameFont.getBaseFont(), template.getFontSize() - 2);
                cb.setTextMatrix(x + 2, currentY - 10);
                cb.showText("SKU: " + (label.getSku() != null ? label.getSku() : ""));
                cb.endText();
                currentY -= 12;
            }

            if (template.isShowPrice()) {
                cb.beginText();
                cb.setFontAndSize(priceFont.getBaseFont(), template.getPriceFontSize());
                cb.setTextMatrix(x + 2, currentY - 14);
                String priceText = "$" + (label.getPrice() != null ? label.getPrice().toString() : "0");
                cb.showText(priceText);
                cb.endText();
                currentY -= 18;
            }

            if (template.isShowBarcode() && label.getBarcode() != null) {
                try {
                    Barcode128 barcode = new Barcode128();
                    barcode.setCode(label.getBarcode());
                    barcode.setBarHeight(mmToPoints(template.getBarcodeHeight() != null ? template.getBarcodeHeight().floatValue() : 10));

                    Image barcodeImage = barcode.createImageWithBarcode(cb, null, null);
                    barcodeImage.setAbsolutePosition(x + 2, currentY - barcodeImage.getScaledHeight() - 5);
                    document.add(barcodeImage);
                } catch (Exception e) {
                    log.warn("條碼產生失敗: {}", e.getMessage());
                }
            }

            labelIndex++;

            // 換行
            if (labelIndex % cols == 0) {
                y -= (labelHeight + vGap);

                // 換頁
                if (y < labelHeight + marginTop) {
                    document.newPage();
                    y = pageHeight - marginTop;
                }
            }
        }

        document.close();
        return baos.toByteArray();
    }

    private String generatePreviewHtml(LabelTemplate template, List<LabelData> labels) {
        StringBuilder html = new StringBuilder();
        html.append("<div style=\"font-family: Arial, sans-serif;\">");

        int cols = template.getColumnsPerRow();
        int index = 0;

        for (LabelData label : labels) {
            if (index % cols == 0) {
                if (index > 0) {
                    html.append("</div>");
                }
                html.append("<div style=\"display: flex; margin-bottom: ")
                        .append(template.getVerticalGap()).append("mm;\">");
            }

            html.append("<div style=\"width: ").append(template.getWidth()).append("mm; ")
                    .append("height: ").append(template.getHeight()).append("mm; ")
                    .append("border: 1px solid #ccc; padding: 2mm; margin-right: ")
                    .append(template.getHorizontalGap()).append("mm; box-sizing: border-box;\">");

            if (template.isShowProductName()) {
                html.append("<div style=\"font-size: ").append(template.getFontSize()).append("pt; ")
                        .append("overflow: hidden; text-overflow: ellipsis; white-space: nowrap;\">")
                        .append(label.getProductName() != null ? label.getProductName() : "")
                        .append("</div>");
            }

            if (template.isShowSku()) {
                html.append("<div style=\"font-size: ").append(template.getFontSize() - 2).append("pt; color: #666;\">")
                        .append("SKU: ").append(label.getSku() != null ? label.getSku() : "")
                        .append("</div>");
            }

            if (template.isShowPrice()) {
                html.append("<div style=\"font-size: ").append(template.getPriceFontSize()).append("pt; ")
                        .append("font-weight: bold; color: #e00;\">$")
                        .append(label.getPrice() != null ? label.getPrice() : "0")
                        .append("</div>");
            }

            if (template.isShowBarcode()) {
                html.append("<div style=\"font-family: 'Libre Barcode 128', cursive; font-size: 24pt;\">")
                        .append(label.getBarcode() != null ? label.getBarcode() : "")
                        .append("</div>");
                if (template.isShowBarcodeText()) {
                    html.append("<div style=\"font-size: 8pt; text-align: center;\">")
                            .append(label.getBarcode() != null ? label.getBarcode() : "")
                            .append("</div>");
                }
            }

            html.append("</div>");
            index++;
        }

        if (index > 0) {
            html.append("</div>");
        }
        html.append("</div>");

        return html.toString();
    }

    private String generatePrintableHtml(LabelTemplate template, List<LabelData> labels) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head><meta charset=\"UTF-8\">");
        html.append("<style>");
        html.append("@page { size: A4; margin: 10mm; }");
        html.append("body { font-family: Arial, sans-serif; margin: 0; padding: 0; }");
        html.append(".label-container { display: flex; flex-wrap: wrap; }");
        html.append(".label { border: 1px solid #ccc; padding: 2mm; box-sizing: border-box; page-break-inside: avoid; }");
        html.append("</style>");
        html.append("</head><body>");
        html.append("<div class=\"label-container\">");
        html.append(generatePreviewHtml(template, labels));
        html.append("</div></body></html>");
        return html.toString();
    }

    private int calculateRowsPerPage(LabelTemplate template) {
        // 假設 A4 紙張
        float pageHeight = 297 - template.getMarginTop().floatValue() * 2;
        float labelHeight = template.getHeight().floatValue() + template.getVerticalGap().floatValue();
        return (int) (pageHeight / labelHeight);
    }

    private float mmToPoints(float mm) {
        return mm * 72 / 25.4f;
    }

    private LabelTemplateDto convertToDto(LabelTemplate template) {
        return LabelTemplateDto.builder()
                .id(template.getId())
                .name(template.getName())
                .description(template.getDescription())
                .labelType(template.getLabelType())
                .labelTypeLabel(template.getLabelType().getLabel())
                .width(template.getWidth())
                .height(template.getHeight())
                .columnsPerRow(template.getColumnsPerRow())
                .horizontalGap(template.getHorizontalGap())
                .verticalGap(template.getVerticalGap())
                .marginTop(template.getMarginTop())
                .marginLeft(template.getMarginLeft())
                .barcodeType(template.getBarcodeType())
                .barcodeTypeLabel(template.getBarcodeType() != null ? template.getBarcodeType().getLabel() : null)
                .barcodeWidth(template.getBarcodeWidth())
                .barcodeHeight(template.getBarcodeHeight())
                .showProductName(template.isShowProductName())
                .showSku(template.isShowSku())
                .showPrice(template.isShowPrice())
                .showBarcode(template.isShowBarcode())
                .showBarcodeText(template.isShowBarcodeText())
                .fontSize(template.getFontSize())
                .priceFontSize(template.getPriceFontSize())
                .isDefault(template.isDefault())
                .createdAt(template.getCreatedAt())
                .updatedAt(template.getUpdatedAt())
                .build();
    }
}
