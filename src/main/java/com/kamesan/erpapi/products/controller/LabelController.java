package com.kamesan.erpapi.products.controller;

import com.kamesan.erpapi.common.dto.ApiResponse;
import com.kamesan.erpapi.products.dto.LabelDto.*;
import com.kamesan.erpapi.products.entity.LabelTemplate.LabelType;
import com.kamesan.erpapi.products.service.LabelPrintService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 標籤列印 Controller
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/labels")
@RequiredArgsConstructor
@Tag(name = "標籤列印", description = "商品標籤列印 API")
public class LabelController {

    private final LabelPrintService labelPrintService;

    // ==================== 範本管理 ====================

    @PostMapping("/templates")
    @Operation(summary = "建立範本", description = "建立新的標籤範本")
    public ApiResponse<LabelTemplateDto> createTemplate(@RequestBody CreateTemplateRequest request) {
        log.info("建立標籤範本: {}", request.getName());
        LabelTemplateDto template = labelPrintService.createTemplate(request);
        return ApiResponse.success(template);
    }

    @GetMapping("/templates/{id}")
    @Operation(summary = "取得範本", description = "取得指定標籤範本")
    public ApiResponse<LabelTemplateDto> getTemplate(
            @Parameter(description = "範本 ID") @PathVariable Long id) {
        LabelTemplateDto template = labelPrintService.getTemplate(id);
        return ApiResponse.success(template);
    }

    @GetMapping("/templates")
    @Operation(summary = "取得所有範本", description = "取得所有標籤範本")
    public ApiResponse<List<LabelTemplateDto>> getAllTemplates() {
        List<LabelTemplateDto> templates = labelPrintService.getAllTemplates();
        return ApiResponse.success(templates);
    }

    @GetMapping("/templates/type/{labelType}")
    @Operation(summary = "根據類型取得範本", description = "取得指定類型的標籤範本")
    public ApiResponse<List<LabelTemplateDto>> getTemplatesByType(
            @Parameter(description = "標籤類型") @PathVariable LabelType labelType) {
        List<LabelTemplateDto> templates = labelPrintService.getTemplatesByType(labelType);
        return ApiResponse.success(templates);
    }

    @GetMapping("/templates/default/{labelType}")
    @Operation(summary = "取得預設範本", description = "取得指定類型的預設範本")
    public ApiResponse<LabelTemplateDto> getDefaultTemplate(
            @Parameter(description = "標籤類型") @PathVariable LabelType labelType) {
        LabelTemplateDto template = labelPrintService.getDefaultTemplate(labelType);
        return ApiResponse.success(template);
    }

    @PutMapping("/templates/{id}/default")
    @Operation(summary = "設為預設範本", description = "將指定範本設為預設")
    public ApiResponse<LabelTemplateDto> setAsDefault(
            @Parameter(description = "範本 ID") @PathVariable Long id) {
        log.info("設定預設範本: {}", id);
        LabelTemplateDto template = labelPrintService.setAsDefault(id);
        return ApiResponse.success(template);
    }

    @DeleteMapping("/templates/{id}")
    @Operation(summary = "刪除範本", description = "刪除指定標籤範本")
    public ApiResponse<Void> deleteTemplate(
            @Parameter(description = "範本 ID") @PathVariable Long id) {
        log.info("刪除標籤範本: {}", id);
        labelPrintService.deleteTemplate(id);
        return ApiResponse.success(null);
    }

    // ==================== 標籤列印 ====================

    @PostMapping("/preview")
    @Operation(summary = "列印預覽", description = "產生標籤列印預覽")
    public ApiResponse<PrintPreviewResponse> generatePreview(@RequestBody PrintRequest request) {
        log.info("產生標籤預覽");
        PrintPreviewResponse preview = labelPrintService.generatePreview(request);
        return ApiResponse.success(preview);
    }

    @PostMapping("/print/pdf")
    @Operation(summary = "列印 PDF", description = "產生標籤 PDF")
    public ResponseEntity<byte[]> printLabelsPdf(@RequestBody PrintRequest request) {
        log.info("產生標籤 PDF");
        PrintResult result = labelPrintService.printLabelsPdf(request);

        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"labels.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(result.getContent());
    }

    @PostMapping("/print/html")
    @Operation(summary = "列印 HTML", description = "產生可列印的 HTML 標籤頁面")
    public ResponseEntity<byte[]> printLabelsHtml(@RequestBody PrintRequest request) {
        log.info("產生標籤 HTML");
        PrintResult result = labelPrintService.printLabelsHtml(request);

        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"labels.html\"")
                .contentType(MediaType.TEXT_HTML)
                .body(result.getContent());
    }
}
