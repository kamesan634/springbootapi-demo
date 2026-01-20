package com.kamesan.erpapi.reports.service;

import com.kamesan.erpapi.reports.dto.*;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.opencsv.CSVWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 報表匯出服務
 *
 * <p>支援將報表資料匯出為 Excel、CSV、PDF 格式</p>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportExportService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 匯出銷售報表為 Excel
     */
    public byte[] exportSalesReportToExcel(SalesReportDto report) throws IOException {
        log.info("匯出銷售報表 Excel: {} ~ {}", report.getStartDate(), report.getEndDate());

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("銷售報表");

            // 建立標題樣式
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle currencyStyle = createCurrencyStyle(workbook);

            int rowNum = 0;

            // 標題
            Row titleRow = sheet.createRow(rowNum++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("銷售報表 " + report.getStartDate() + " ~ " + report.getEndDate());
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));

            rowNum++; // 空行

            // 摘要資訊
            createSummaryRow(sheet, rowNum++, "總銷售額", report.getTotalSales(), currencyStyle);
            createSummaryRow(sheet, rowNum++, "總退貨金額", report.getTotalRefunds(), currencyStyle);
            createSummaryRow(sheet, rowNum++, "淨銷售額", report.getNetSales(), currencyStyle);
            createSummaryRow(sheet, rowNum++, "訂單數量", BigDecimal.valueOf(report.getOrderCount()), dataStyle);
            createSummaryRow(sheet, rowNum++, "平均訂單金額", report.getAvgOrderAmount(), currencyStyle);

            rowNum++; // 空行

            // 每日銷售明細標題
            Row headerRow = sheet.createRow(rowNum++);
            String[] headers = {"日期", "銷售額", "退貨金額", "淨銷售額", "訂單數", "退貨數"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // 每日銷售資料
            for (SalesReportDto.DailySales daily : report.getDailySales()) {
                Row dataRow = sheet.createRow(rowNum++);
                dataRow.createCell(0).setCellValue(daily.getDate().format(DATE_FORMATTER));
                createCurrencyCell(dataRow, 1, daily.getSales(), currencyStyle);
                createCurrencyCell(dataRow, 2, daily.getRefunds(), currencyStyle);
                createCurrencyCell(dataRow, 3, daily.getNetSales(), currencyStyle);
                dataRow.createCell(4).setCellValue(daily.getOrderCount());
                dataRow.createCell(5).setCellValue(daily.getRefundCount());
            }

            // 自動調整欄寬
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    /**
     * 匯出銷售報表為 CSV
     */
    public byte[] exportSalesReportToCsv(SalesReportDto report) throws IOException {
        log.info("匯出銷售報表 CSV: {} ~ {}", report.getStartDate(), report.getEndDate());

        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             OutputStreamWriter writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
             CSVWriter csvWriter = new CSVWriter(writer)) {

            // 寫入 BOM for Excel 相容
            out.write(0xEF);
            out.write(0xBB);
            out.write(0xBF);

            // 標題行
            csvWriter.writeNext(new String[]{"銷售報表", report.getStartDate() + " ~ " + report.getEndDate()});
            csvWriter.writeNext(new String[]{});

            // 摘要資訊
            csvWriter.writeNext(new String[]{"總銷售額", formatCurrency(report.getTotalSales())});
            csvWriter.writeNext(new String[]{"總退貨金額", formatCurrency(report.getTotalRefunds())});
            csvWriter.writeNext(new String[]{"淨銷售額", formatCurrency(report.getNetSales())});
            csvWriter.writeNext(new String[]{"訂單數量", String.valueOf(report.getOrderCount())});
            csvWriter.writeNext(new String[]{"平均訂單金額", formatCurrency(report.getAvgOrderAmount())});
            csvWriter.writeNext(new String[]{});

            // 每日銷售明細標題
            csvWriter.writeNext(new String[]{"日期", "銷售額", "退貨金額", "淨銷售額", "訂單數", "退貨數"});

            // 每日銷售資料
            for (SalesReportDto.DailySales daily : report.getDailySales()) {
                csvWriter.writeNext(new String[]{
                        daily.getDate().format(DATE_FORMATTER),
                        formatCurrency(daily.getSales()),
                        formatCurrency(daily.getRefunds()),
                        formatCurrency(daily.getNetSales()),
                        String.valueOf(daily.getOrderCount()),
                        String.valueOf(daily.getRefundCount())
                });
            }

            csvWriter.flush();
            return out.toByteArray();
        }
    }

    /**
     * 匯出銷售報表為 PDF
     */
    public byte[] exportSalesReportToPdf(SalesReportDto report) throws IOException, DocumentException {
        log.info("匯出銷售報表 PDF: {} ~ {}", report.getStartDate(), report.getEndDate());

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, out);

            document.open();

            // 設定字型（支援中文）
            BaseFont baseFont = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
            com.lowagie.text.Font titleFont = new com.lowagie.text.Font(baseFont, 18, com.lowagie.text.Font.BOLD);
            com.lowagie.text.Font headerFont = new com.lowagie.text.Font(baseFont, 10, com.lowagie.text.Font.BOLD);
            com.lowagie.text.Font normalFont = new com.lowagie.text.Font(baseFont, 10, com.lowagie.text.Font.NORMAL);

            // 標題
            Paragraph title = new Paragraph("銷售報表", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Paragraph period = new Paragraph(
                    report.getStartDate() + " ~ " + report.getEndDate(), normalFont);
            period.setAlignment(Element.ALIGN_CENTER);
            period.setSpacingAfter(20);
            document.add(period);

            // 摘要表格
            PdfPTable summaryTable = new PdfPTable(2);
            summaryTable.setWidthPercentage(50);
            summaryTable.setHorizontalAlignment(Element.ALIGN_LEFT);
            summaryTable.setSpacingAfter(20);

            addPdfRow(summaryTable, "總銷售額", formatCurrency(report.getTotalSales()), headerFont, normalFont);
            addPdfRow(summaryTable, "總退貨金額", formatCurrency(report.getTotalRefunds()), headerFont, normalFont);
            addPdfRow(summaryTable, "淨銷售額", formatCurrency(report.getNetSales()), headerFont, normalFont);
            addPdfRow(summaryTable, "訂單數量", String.valueOf(report.getOrderCount()), headerFont, normalFont);
            addPdfRow(summaryTable, "平均訂單金額", formatCurrency(report.getAvgOrderAmount()), headerFont, normalFont);

            document.add(summaryTable);

            // 每日銷售明細標題
            Paragraph detailTitle = new Paragraph("每日銷售明細", headerFont);
            detailTitle.setSpacingAfter(10);
            document.add(detailTitle);

            // 每日銷售表格
            PdfPTable detailTable = new PdfPTable(6);
            detailTable.setWidthPercentage(100);
            detailTable.setWidths(new float[]{2, 2, 2, 2, 1, 1});

            // 表頭
            String[] headers = {"日期", "銷售額", "退貨金額", "淨銷售額", "訂單數", "退貨數"};
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                cell.setBackgroundColor(new java.awt.Color(200, 200, 200));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(5);
                detailTable.addCell(cell);
            }

            // 資料
            for (SalesReportDto.DailySales daily : report.getDailySales()) {
                addPdfCell(detailTable, daily.getDate().format(DATE_FORMATTER), normalFont);
                addPdfCell(detailTable, formatCurrency(daily.getSales()), normalFont);
                addPdfCell(detailTable, formatCurrency(daily.getRefunds()), normalFont);
                addPdfCell(detailTable, formatCurrency(daily.getNetSales()), normalFont);
                addPdfCell(detailTable, String.valueOf(daily.getOrderCount()), normalFont);
                addPdfCell(detailTable, String.valueOf(daily.getRefundCount()), normalFont);
            }

            document.add(detailTable);
            document.close();

            return out.toByteArray();
        }
    }

    /**
     * 匯出庫存報表為 Excel
     */
    public byte[] exportInventoryReportToExcel(InventoryReportDto report) throws IOException {
        log.info("匯出庫存報表 Excel");

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("庫存報表");

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle currencyStyle = createCurrencyStyle(workbook);

            int rowNum = 0;

            // 標題
            Row titleRow = sheet.createRow(rowNum++);
            titleRow.createCell(0).setCellValue("庫存報表 " + report.getReportDate());

            rowNum++;

            // 摘要
            createSummaryRow(sheet, rowNum++, "商品種類", BigDecimal.valueOf(report.getTotalProducts()), null);
            createSummaryRow(sheet, rowNum++, "總庫存數量", BigDecimal.valueOf(report.getTotalQuantity()), null);
            createSummaryRow(sheet, rowNum++, "庫存總價值", report.getTotalValue(), currencyStyle);
            createSummaryRow(sheet, rowNum++, "低庫存商品", BigDecimal.valueOf(report.getLowStockCount()), null);
            createSummaryRow(sheet, rowNum++, "缺貨商品", BigDecimal.valueOf(report.getOutOfStockCount()), null);

            rowNum++;

            // 庫存明細標題
            Row headerRow = sheet.createRow(rowNum++);
            String[] headers = {"商品編號", "商品名稱", "類別", "數量", "保留數量", "可用數量", "單位成本", "庫存價值", "狀態"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // 庫存明細資料
            for (InventoryReportDto.ProductInventory item : report.getInventoryList()) {
                Row dataRow = sheet.createRow(rowNum++);
                dataRow.createCell(0).setCellValue(item.getProductCode() != null ? item.getProductCode() : "");
                dataRow.createCell(1).setCellValue(item.getProductName() != null ? item.getProductName() : "");
                dataRow.createCell(2).setCellValue(item.getCategoryName() != null ? item.getCategoryName() : "");
                dataRow.createCell(3).setCellValue(item.getQuantity() != null ? item.getQuantity() : 0);
                dataRow.createCell(4).setCellValue(item.getReservedQuantity() != null ? item.getReservedQuantity() : 0);
                dataRow.createCell(5).setCellValue(item.getAvailableQuantity() != null ? item.getAvailableQuantity() : 0);
                createCurrencyCell(dataRow, 6, item.getUnitCost(), currencyStyle);
                createCurrencyCell(dataRow, 7, item.getTotalValue(), currencyStyle);
                dataRow.createCell(8).setCellValue(item.getStatus() != null ? item.getStatus() : "");
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    /**
     * 匯出庫存報表為 CSV
     */
    public byte[] exportInventoryReportToCsv(InventoryReportDto report) throws IOException {
        log.info("匯出庫存報表 CSV");

        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             OutputStreamWriter writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
             CSVWriter csvWriter = new CSVWriter(writer)) {

            out.write(0xEF);
            out.write(0xBB);
            out.write(0xBF);

            csvWriter.writeNext(new String[]{"庫存報表", report.getReportDate().toString()});
            csvWriter.writeNext(new String[]{});
            csvWriter.writeNext(new String[]{"商品種類", String.valueOf(report.getTotalProducts())});
            csvWriter.writeNext(new String[]{"總庫存數量", String.valueOf(report.getTotalQuantity())});
            csvWriter.writeNext(new String[]{"庫存總價值", formatCurrency(report.getTotalValue())});
            csvWriter.writeNext(new String[]{"低庫存商品", String.valueOf(report.getLowStockCount())});
            csvWriter.writeNext(new String[]{"缺貨商品", String.valueOf(report.getOutOfStockCount())});
            csvWriter.writeNext(new String[]{});

            csvWriter.writeNext(new String[]{"商品編號", "商品名稱", "類別", "數量", "保留數量", "可用數量", "單位成本", "庫存價值", "狀態"});

            for (InventoryReportDto.ProductInventory item : report.getInventoryList()) {
                csvWriter.writeNext(new String[]{
                        item.getProductCode() != null ? item.getProductCode() : "",
                        item.getProductName() != null ? item.getProductName() : "",
                        item.getCategoryName() != null ? item.getCategoryName() : "",
                        String.valueOf(item.getQuantity() != null ? item.getQuantity() : 0),
                        String.valueOf(item.getReservedQuantity() != null ? item.getReservedQuantity() : 0),
                        String.valueOf(item.getAvailableQuantity() != null ? item.getAvailableQuantity() : 0),
                        formatCurrency(item.getUnitCost()),
                        formatCurrency(item.getTotalValue()),
                        item.getStatus() != null ? item.getStatus() : ""
                });
            }

            csvWriter.flush();
            return out.toByteArray();
        }
    }

    // ==================== 輔助方法 ====================

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createCurrencyStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("#,##0.00"));
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private void createSummaryRow(Sheet sheet, int rowNum, String label, BigDecimal value, CellStyle style) {
        Row row = sheet.createRow(rowNum);
        row.createCell(0).setCellValue(label);
        Cell valueCell = row.createCell(1);
        if (value != null) {
            valueCell.setCellValue(value.doubleValue());
            if (style != null) {
                valueCell.setCellStyle(style);
            }
        }
    }

    private void createCurrencyCell(Row row, int colIndex, BigDecimal value, CellStyle style) {
        Cell cell = row.createCell(colIndex);
        if (value != null) {
            cell.setCellValue(value.doubleValue());
            cell.setCellStyle(style);
        }
    }

    private String formatCurrency(BigDecimal value) {
        if (value == null) {
            return "0.00";
        }
        return String.format("%,.2f", value);
    }

    private void addPdfRow(PdfPTable table, String label, String value, com.lowagie.text.Font labelFont, com.lowagie.text.Font valueFont) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setPadding(5);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setPadding(5);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(valueCell);
    }

    private void addPdfCell(PdfPTable table, String value, com.lowagie.text.Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(value, font));
        cell.setPadding(3);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }
}
