package com.kamesan.erpapi.products.service;

import com.kamesan.erpapi.common.exception.BusinessException;
import com.kamesan.erpapi.products.entity.Category;
import com.kamesan.erpapi.products.entity.Product;
import com.kamesan.erpapi.products.entity.TaxType;
import com.kamesan.erpapi.products.entity.Unit;
import com.kamesan.erpapi.products.repository.CategoryRepository;
import com.kamesan.erpapi.products.repository.ProductRepository;
import com.kamesan.erpapi.products.repository.TaxTypeRepository;
import com.kamesan.erpapi.products.repository.UnitRepository;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 商品匯入匯出服務
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductImportExportService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final UnitRepository unitRepository;
    private final TaxTypeRepository taxTypeRepository;

    private static final String[] EXPORT_HEADERS = {
            "商品貨號(SKU)*", "商品名稱*", "商品描述", "分類編號", "單位編號",
            "稅別編號", "成本價", "售價*", "主要條碼", "安全庫存", "是否啟用"
    };

    /**
     * 匯出商品為 Excel
     */
    public byte[] exportProductsToExcel(List<Product> products) throws IOException {
        log.info("匯出商品 Excel，共 {} 筆", products.size());

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("商品資料");

            // 建立標題樣式
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle currencyStyle = createCurrencyStyle(workbook);

            // 標題行
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < EXPORT_HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(EXPORT_HEADERS[i]);
                cell.setCellStyle(headerStyle);
            }

            // 資料行
            int rowNum = 1;
            for (Product product : products) {
                Row dataRow = sheet.createRow(rowNum++);
                dataRow.createCell(0).setCellValue(product.getSku());
                dataRow.createCell(1).setCellValue(product.getName());
                dataRow.createCell(2).setCellValue(product.getDescription() != null ? product.getDescription() : "");
                dataRow.createCell(3).setCellValue(product.getCategory() != null ? product.getCategory().getCode() : "");
                dataRow.createCell(4).setCellValue(product.getUnit() != null ? product.getUnit().getCode() : "");
                dataRow.createCell(5).setCellValue(product.getTaxType() != null ? product.getTaxType().getCode() : "");

                Cell costCell = dataRow.createCell(6);
                if (product.getCostPrice() != null) {
                    costCell.setCellValue(product.getCostPrice().doubleValue());
                    costCell.setCellStyle(currencyStyle);
                }

                Cell priceCell = dataRow.createCell(7);
                if (product.getSellingPrice() != null) {
                    priceCell.setCellValue(product.getSellingPrice().doubleValue());
                    priceCell.setCellStyle(currencyStyle);
                }

                dataRow.createCell(8).setCellValue(product.getBarcode() != null ? product.getBarcode() : "");
                dataRow.createCell(9).setCellValue(product.getSafetyStock() != null ? product.getSafetyStock() : 0);
                dataRow.createCell(10).setCellValue(product.isActive() ? "是" : "否");
            }

            // 自動調整欄寬
            for (int i = 0; i < EXPORT_HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    /**
     * 匯出商品為 CSV
     */
    public byte[] exportProductsToCsv(List<Product> products) throws IOException {
        log.info("匯出商品 CSV，共 {} 筆", products.size());

        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             OutputStreamWriter writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
             CSVWriter csvWriter = new CSVWriter(writer)) {

            // 寫入 BOM
            out.write(0xEF);
            out.write(0xBB);
            out.write(0xBF);

            // 標題行
            csvWriter.writeNext(EXPORT_HEADERS);

            // 資料行
            for (Product product : products) {
                csvWriter.writeNext(new String[]{
                        product.getSku(),
                        product.getName(),
                        product.getDescription() != null ? product.getDescription() : "",
                        product.getCategory() != null ? product.getCategory().getCode() : "",
                        product.getUnit() != null ? product.getUnit().getCode() : "",
                        product.getTaxType() != null ? product.getTaxType().getCode() : "",
                        product.getCostPrice() != null ? product.getCostPrice().toPlainString() : "0",
                        product.getSellingPrice() != null ? product.getSellingPrice().toPlainString() : "0",
                        product.getBarcode() != null ? product.getBarcode() : "",
                        String.valueOf(product.getSafetyStock() != null ? product.getSafetyStock() : 0),
                        product.isActive() ? "是" : "否"
                });
            }

            csvWriter.flush();
            return out.toByteArray();
        }
    }

    /**
     * 產生匯入範本 Excel
     */
    public byte[] generateImportTemplate() throws IOException {
        log.info("產生商品匯入範本");

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            // 商品資料頁
            Sheet dataSheet = workbook.createSheet("商品資料");
            CellStyle headerStyle = createHeaderStyle(workbook);

            Row headerRow = dataSheet.createRow(0);
            for (int i = 0; i < EXPORT_HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(EXPORT_HEADERS[i]);
                cell.setCellStyle(headerStyle);
            }

            // 範例資料
            Row sampleRow = dataSheet.createRow(1);
            sampleRow.createCell(0).setCellValue("SKU001");
            sampleRow.createCell(1).setCellValue("範例商品");
            sampleRow.createCell(2).setCellValue("這是商品描述");
            sampleRow.createCell(3).setCellValue("CAT001");
            sampleRow.createCell(4).setCellValue("PCS");
            sampleRow.createCell(5).setCellValue("TAX5");
            sampleRow.createCell(6).setCellValue(100);
            sampleRow.createCell(7).setCellValue(150);
            sampleRow.createCell(8).setCellValue("4710000000001");
            sampleRow.createCell(9).setCellValue(10);
            sampleRow.createCell(10).setCellValue("是");

            for (int i = 0; i < EXPORT_HEADERS.length; i++) {
                dataSheet.autoSizeColumn(i);
            }

            // 分類參考頁
            Sheet categorySheet = workbook.createSheet("分類參考");
            Row catHeader = categorySheet.createRow(0);
            catHeader.createCell(0).setCellValue("分類編號");
            catHeader.createCell(1).setCellValue("分類名稱");
            catHeader.getCell(0).setCellStyle(headerStyle);
            catHeader.getCell(1).setCellStyle(headerStyle);

            List<Category> categories = categoryRepository.findAll();
            int catRow = 1;
            for (Category cat : categories) {
                Row row = categorySheet.createRow(catRow++);
                row.createCell(0).setCellValue(cat.getCode());
                row.createCell(1).setCellValue(cat.getName());
            }
            categorySheet.autoSizeColumn(0);
            categorySheet.autoSizeColumn(1);

            // 單位參考頁
            Sheet unitSheet = workbook.createSheet("單位參考");
            Row unitHeader = unitSheet.createRow(0);
            unitHeader.createCell(0).setCellValue("單位編號");
            unitHeader.createCell(1).setCellValue("單位名稱");
            unitHeader.getCell(0).setCellStyle(headerStyle);
            unitHeader.getCell(1).setCellStyle(headerStyle);

            List<Unit> units = unitRepository.findAll();
            int unitRow = 1;
            for (Unit unit : units) {
                Row row = unitSheet.createRow(unitRow++);
                row.createCell(0).setCellValue(unit.getCode());
                row.createCell(1).setCellValue(unit.getName());
            }
            unitSheet.autoSizeColumn(0);
            unitSheet.autoSizeColumn(1);

            // 稅別參考頁
            Sheet taxSheet = workbook.createSheet("稅別參考");
            Row taxHeader = taxSheet.createRow(0);
            taxHeader.createCell(0).setCellValue("稅別編號");
            taxHeader.createCell(1).setCellValue("稅別名稱");
            taxHeader.createCell(2).setCellValue("稅率");
            taxHeader.getCell(0).setCellStyle(headerStyle);
            taxHeader.getCell(1).setCellStyle(headerStyle);
            taxHeader.getCell(2).setCellStyle(headerStyle);

            List<TaxType> taxTypes = taxTypeRepository.findAll();
            int taxRow = 1;
            for (TaxType tax : taxTypes) {
                Row row = taxSheet.createRow(taxRow++);
                row.createCell(0).setCellValue(tax.getCode());
                row.createCell(1).setCellValue(tax.getName());
                row.createCell(2).setCellValue(tax.getRate() != null ? tax.getRate().toPlainString() + "%" : "0%");
            }
            taxSheet.autoSizeColumn(0);
            taxSheet.autoSizeColumn(1);
            taxSheet.autoSizeColumn(2);

            workbook.write(out);
            return out.toByteArray();
        }
    }

    /**
     * 從 Excel 匯入商品
     */
    @Transactional
    public ImportResult importProductsFromExcel(MultipartFile file) throws IOException {
        log.info("從 Excel 匯入商品: {}", file.getOriginalFilename());

        List<Product> importedProducts = new ArrayList<>();
        List<ImportError> errors = new ArrayList<>();

        try (InputStream is = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            int lastRow = sheet.getLastRowNum();

            for (int rowNum = 1; rowNum <= lastRow; rowNum++) {
                Row row = sheet.getRow(rowNum);
                if (row == null) continue;

                try {
                    Product product = parseProductFromRow(row, rowNum);
                    if (product != null) {
                        importedProducts.add(productRepository.save(product));
                    }
                } catch (Exception e) {
                    errors.add(new ImportError(rowNum + 1, e.getMessage()));
                }
            }
        }

        log.info("匯入完成: 成功 {} 筆, 失敗 {} 筆", importedProducts.size(), errors.size());
        return new ImportResult(importedProducts.size(), errors);
    }

    /**
     * 從 CSV 匯入商品
     */
    @Transactional
    public ImportResult importProductsFromCsv(MultipartFile file) throws IOException, CsvException {
        log.info("從 CSV 匯入商品: {}", file.getOriginalFilename());

        List<Product> importedProducts = new ArrayList<>();
        List<ImportError> errors = new ArrayList<>();

        try (Reader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
             CSVReader csvReader = new CSVReader(reader)) {

            List<String[]> allRows = csvReader.readAll();

            // 跳過標題行
            for (int i = 1; i < allRows.size(); i++) {
                String[] row = allRows.get(i);
                int rowNum = i + 1;

                try {
                    Product product = parseProductFromCsvRow(row, rowNum);
                    if (product != null) {
                        importedProducts.add(productRepository.save(product));
                    }
                } catch (Exception e) {
                    errors.add(new ImportError(rowNum, e.getMessage()));
                }
            }
        }

        log.info("匯入完成: 成功 {} 筆, 失敗 {} 筆", importedProducts.size(), errors.size());
        return new ImportResult(importedProducts.size(), errors);
    }

    private Product parseProductFromRow(Row row, int rowNum) {
        String sku = getCellValueAsString(row.getCell(0));
        if (sku == null || sku.trim().isEmpty()) {
            return null; // 跳過空行
        }

        String name = getCellValueAsString(row.getCell(1));
        if (name == null || name.trim().isEmpty()) {
            throw new BusinessException(400, "第 " + (rowNum + 1) + " 行: 商品名稱不能為空");
        }

        // 檢查 SKU 是否已存在
        Optional<Product> existing = productRepository.findBySku(sku);
        Product product = existing.orElse(new Product());

        product.setSku(sku.trim());
        product.setName(name.trim());
        product.setDescription(getCellValueAsString(row.getCell(2)));

        // 分類
        String categoryCode = getCellValueAsString(row.getCell(3));
        if (categoryCode != null && !categoryCode.isEmpty()) {
            categoryRepository.findByCode(categoryCode)
                    .ifPresent(product::setCategory);
        }

        // 單位
        String unitCode = getCellValueAsString(row.getCell(4));
        if (unitCode != null && !unitCode.isEmpty()) {
            unitRepository.findByCode(unitCode)
                    .ifPresent(product::setUnit);
        }

        // 稅別
        String taxCode = getCellValueAsString(row.getCell(5));
        if (taxCode != null && !taxCode.isEmpty()) {
            taxTypeRepository.findByCode(taxCode)
                    .ifPresent(product::setTaxType);
        }

        // 成本價
        Double costPrice = getCellValueAsDouble(row.getCell(6));
        if (costPrice != null) {
            product.setCostPrice(BigDecimal.valueOf(costPrice));
        }

        // 售價
        Double sellingPrice = getCellValueAsDouble(row.getCell(7));
        if (sellingPrice != null) {
            product.setSellingPrice(BigDecimal.valueOf(sellingPrice));
        } else {
            throw new BusinessException(400, "第 " + (rowNum + 1) + " 行: 售價不能為空");
        }

        // 條碼
        product.setBarcode(getCellValueAsString(row.getCell(8)));

        // 安全庫存
        Double safetyStock = getCellValueAsDouble(row.getCell(9));
        if (safetyStock != null) {
            product.setSafetyStock(safetyStock.intValue());
        }

        // 是否啟用
        String isActive = getCellValueAsString(row.getCell(10));
        product.setActive(!"否".equals(isActive));

        return product;
    }

    private Product parseProductFromCsvRow(String[] row, int rowNum) {
        if (row.length < 2) return null;

        String sku = row[0];
        if (sku == null || sku.trim().isEmpty()) {
            return null;
        }

        String name = row.length > 1 ? row[1] : null;
        if (name == null || name.trim().isEmpty()) {
            throw new BusinessException(400, "第 " + rowNum + " 行: 商品名稱不能為空");
        }

        Optional<Product> existing = productRepository.findBySku(sku);
        Product product = existing.orElse(new Product());

        product.setSku(sku.trim());
        product.setName(name.trim());
        product.setDescription(row.length > 2 ? row[2] : null);

        // 分類
        if (row.length > 3 && row[3] != null && !row[3].isEmpty()) {
            categoryRepository.findByCode(row[3])
                    .ifPresent(product::setCategory);
        }

        // 單位
        if (row.length > 4 && row[4] != null && !row[4].isEmpty()) {
            unitRepository.findByCode(row[4])
                    .ifPresent(product::setUnit);
        }

        // 稅別
        if (row.length > 5 && row[5] != null && !row[5].isEmpty()) {
            taxTypeRepository.findByCode(row[5])
                    .ifPresent(product::setTaxType);
        }

        // 成本價
        if (row.length > 6 && row[6] != null && !row[6].isEmpty()) {
            product.setCostPrice(new BigDecimal(row[6]));
        }

        // 售價
        if (row.length > 7 && row[7] != null && !row[7].isEmpty()) {
            product.setSellingPrice(new BigDecimal(row[7]));
        } else {
            throw new BusinessException(400, "第 " + rowNum + " 行: 售價不能為空");
        }

        // 條碼
        if (row.length > 8) {
            product.setBarcode(row[8]);
        }

        // 安全庫存
        if (row.length > 9 && row[9] != null && !row[9].isEmpty()) {
            product.setSafetyStock(Integer.parseInt(row[9]));
        }

        // 是否啟用
        if (row.length > 10) {
            product.setActive(!"否".equals(row[10]));
        }

        return product;
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> null;
        };
    }

    private Double getCellValueAsDouble(Cell cell) {
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case NUMERIC -> cell.getNumericCellValue();
            case STRING -> {
                try {
                    yield Double.parseDouble(cell.getStringCellValue());
                } catch (NumberFormatException e) {
                    yield null;
                }
            }
            default -> null;
        };
    }

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

    private CellStyle createCurrencyStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("#,##0.00"));
        return style;
    }

    /**
     * 匯入結果
     */
    public record ImportResult(int successCount, List<ImportError> errors) {
        public int getErrorCount() {
            return errors.size();
        }
    }

    /**
     * 匯入錯誤
     */
    public record ImportError(int rowNumber, String message) {
    }
}
