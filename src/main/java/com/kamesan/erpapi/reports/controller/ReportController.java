package com.kamesan.erpapi.reports.controller;

import com.kamesan.erpapi.common.dto.ApiResponse;
import com.kamesan.erpapi.reports.dto.*;
import com.kamesan.erpapi.reports.service.ReportExportService;
import com.kamesan.erpapi.reports.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 報表 Controller
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Tag(name = "報表", description = "報表與儀表板 API")
public class ReportController {

    private final ReportService reportService;
    private final ReportExportService reportExportService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    @GetMapping("/dashboard")
    @Operation(summary = "取得儀表板", description = "取得儀表板總覽資料")
    public ResponseEntity<ApiResponse<DashboardDto>> getDashboard(
            @Parameter(description = "門市 ID") @RequestParam(required = false) Long storeId) {

        DashboardDto dashboard = reportService.getDashboard(storeId);
        return ResponseEntity.ok(ApiResponse.success(dashboard));
    }

    @GetMapping("/sales")
    @Operation(summary = "取得銷售報表", description = "取得指定期間的銷售報表")
    public ResponseEntity<ApiResponse<SalesReportDto>> getSalesReport(
            @Parameter(description = "開始日期") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "結束日期") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "門市 ID") @RequestParam(required = false) Long storeId) {

        SalesReportDto report = reportService.getSalesReport(startDate, endDate, storeId);
        return ResponseEntity.ok(ApiResponse.success(report));
    }

    @GetMapping("/inventory")
    @Operation(summary = "取得庫存報表", description = "取得庫存報表")
    public ResponseEntity<ApiResponse<InventoryReportDto>> getInventoryReport(
            @Parameter(description = "倉庫 ID") @RequestParam(required = false) Long warehouseId) {

        InventoryReportDto report = reportService.getInventoryReport(warehouseId);
        return ResponseEntity.ok(ApiResponse.success(report));
    }

    @GetMapping("/purchasing")
    @Operation(summary = "取得採購報表", description = "取得指定期間的採購報表")
    public ResponseEntity<ApiResponse<PurchasingReportDto>> getPurchasingReport(
            @Parameter(description = "開始日期") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "結束日期") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        PurchasingReportDto report = reportService.getPurchasingReport(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(report));
    }

    @GetMapping("/sales/daily")
    @Operation(summary = "取得每日銷售", description = "取得指定日期的銷售明細")
    public ResponseEntity<ApiResponse<SalesReportDto>> getDailySales(
            @Parameter(description = "日期") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Parameter(description = "門市 ID") @RequestParam(required = false) Long storeId) {

        SalesReportDto report = reportService.getSalesReport(date, date, storeId);
        return ResponseEntity.ok(ApiResponse.success(report));
    }

    @GetMapping("/sales/monthly")
    @Operation(summary = "取得月度銷售", description = "取得指定月份的銷售報表")
    public ResponseEntity<ApiResponse<SalesReportDto>> getMonthlySales(
            @Parameter(description = "年份") @RequestParam int year,
            @Parameter(description = "月份") @RequestParam int month,
            @Parameter(description = "門市 ID") @RequestParam(required = false) Long storeId) {

        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        SalesReportDto report = reportService.getSalesReport(startDate, endDate, storeId);
        return ResponseEntity.ok(ApiResponse.success(report));
    }

    // ==================== 報表匯出 API ====================

    @GetMapping("/sales/export/excel")
    @Operation(summary = "匯出銷售報表 Excel", description = "將銷售報表匯出為 Excel 格式")
    public ResponseEntity<byte[]> exportSalesReportToExcel(
            @Parameter(description = "開始日期") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "結束日期") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "門市 ID") @RequestParam(required = false) Long storeId) throws IOException {

        SalesReportDto report = reportService.getSalesReport(startDate, endDate, storeId);
        byte[] content = reportExportService.exportSalesReportToExcel(report);

        String filename = "sales_report_" + startDate.format(DATE_FORMATTER) + "_" + endDate.format(DATE_FORMATTER) + ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .contentLength(content.length)
                .body(content);
    }

    @GetMapping("/sales/export/csv")
    @Operation(summary = "匯出銷售報表 CSV", description = "將銷售報表匯出為 CSV 格式")
    public ResponseEntity<byte[]> exportSalesReportToCsv(
            @Parameter(description = "開始日期") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "結束日期") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "門市 ID") @RequestParam(required = false) Long storeId) throws IOException {

        SalesReportDto report = reportService.getSalesReport(startDate, endDate, storeId);
        byte[] content = reportExportService.exportSalesReportToCsv(report);

        String filename = "sales_report_" + startDate.format(DATE_FORMATTER) + "_" + endDate.format(DATE_FORMATTER) + ".csv";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .contentLength(content.length)
                .body(content);
    }

    @GetMapping("/sales/export/pdf")
    @Operation(summary = "匯出銷售報表 PDF", description = "將銷售報表匯出為 PDF 格式")
    public ResponseEntity<byte[]> exportSalesReportToPdf(
            @Parameter(description = "開始日期") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "結束日期") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "門市 ID") @RequestParam(required = false) Long storeId) throws Exception {

        SalesReportDto report = reportService.getSalesReport(startDate, endDate, storeId);
        byte[] content = reportExportService.exportSalesReportToPdf(report);

        String filename = "sales_report_" + startDate.format(DATE_FORMATTER) + "_" + endDate.format(DATE_FORMATTER) + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(content.length)
                .body(content);
    }

    @GetMapping("/inventory/export/excel")
    @Operation(summary = "匯出庫存報表 Excel", description = "將庫存報表匯出為 Excel 格式")
    public ResponseEntity<byte[]> exportInventoryReportToExcel(
            @Parameter(description = "倉庫 ID") @RequestParam(required = false) Long warehouseId) throws IOException {

        InventoryReportDto report = reportService.getInventoryReport(warehouseId);
        byte[] content = reportExportService.exportInventoryReportToExcel(report);

        String filename = "inventory_report_" + LocalDate.now().format(DATE_FORMATTER) + ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .contentLength(content.length)
                .body(content);
    }

    @GetMapping("/inventory/export/csv")
    @Operation(summary = "匯出庫存報表 CSV", description = "將庫存報表匯出為 CSV 格式")
    public ResponseEntity<byte[]> exportInventoryReportToCsv(
            @Parameter(description = "倉庫 ID") @RequestParam(required = false) Long warehouseId) throws IOException {

        InventoryReportDto report = reportService.getInventoryReport(warehouseId);
        byte[] content = reportExportService.exportInventoryReportToCsv(report);

        String filename = "inventory_report_" + LocalDate.now().format(DATE_FORMATTER) + ".csv";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .contentLength(content.length)
                .body(content);
    }

    // ==================== 利潤分析 API ====================

    @GetMapping("/profit-analysis")
    @Operation(summary = "取得利潤分析", description = "取得指定期間的利潤分析報表")
    public ResponseEntity<ApiResponse<ProfitAnalysisDto>> getProfitAnalysis(
            @Parameter(description = "開始日期") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "結束日期") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "門市 ID") @RequestParam(required = false) Long storeId) {

        ProfitAnalysisDto report = reportService.getProfitAnalysis(startDate, endDate, storeId);
        return ResponseEntity.ok(ApiResponse.success(report));
    }

    // ==================== 期間比較 API ====================

    @GetMapping("/comparison/yoy")
    @Operation(summary = "年對年比較", description = "取得年對年銷售比較（同比）")
    public ResponseEntity<ApiResponse<PeriodComparisonDto>> getYearOverYearComparison(
            @Parameter(description = "年份") @RequestParam int year,
            @Parameter(description = "月份") @RequestParam int month,
            @Parameter(description = "門市 ID") @RequestParam(required = false) Long storeId) {

        PeriodComparisonDto report = reportService.getYearOverYearComparison(year, month, storeId);
        return ResponseEntity.ok(ApiResponse.success(report));
    }

    @GetMapping("/comparison/mom")
    @Operation(summary = "月對月比較", description = "取得月對月銷售比較（環比）")
    public ResponseEntity<ApiResponse<PeriodComparisonDto>> getMonthOverMonthComparison(
            @Parameter(description = "年份") @RequestParam int year,
            @Parameter(description = "月份") @RequestParam int month,
            @Parameter(description = "門市 ID") @RequestParam(required = false) Long storeId) {

        PeriodComparisonDto report = reportService.getMonthOverMonthComparison(year, month, storeId);
        return ResponseEntity.ok(ApiResponse.success(report));
    }

    @GetMapping("/comparison/custom")
    @Operation(summary = "自訂期間比較", description = "自訂兩個期間進行銷售比較")
    public ResponseEntity<ApiResponse<PeriodComparisonDto>> getCustomPeriodComparison(
            @Parameter(description = "本期開始日期") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate currentStart,
            @Parameter(description = "本期結束日期") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate currentEnd,
            @Parameter(description = "上期開始日期") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate previousStart,
            @Parameter(description = "上期結束日期") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate previousEnd) {

        PeriodComparisonDto report = reportService.getPeriodComparison(currentStart, currentEnd, previousStart, previousEnd, "CUSTOM");
        return ResponseEntity.ok(ApiResponse.success(report));
    }
}
