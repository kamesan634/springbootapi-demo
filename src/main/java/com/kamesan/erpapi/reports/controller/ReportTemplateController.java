package com.kamesan.erpapi.reports.controller;

import com.kamesan.erpapi.common.dto.ApiResponse;
import com.kamesan.erpapi.common.dto.PageResponse;
import com.kamesan.erpapi.reports.dto.ReportTemplateDto;
import com.kamesan.erpapi.reports.dto.ReportTemplateDto.*;
import com.kamesan.erpapi.reports.entity.ReportSchedule.ReportType;
import com.kamesan.erpapi.reports.service.ReportTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 報表範本 Controller
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/reports/templates")
@RequiredArgsConstructor
@Tag(name = "報表範本", description = "自訂報表範本管理 API")
public class ReportTemplateController {

    private final ReportTemplateService templateService;

    @PostMapping
    @Operation(summary = "建立範本", description = "建立新的報表範本")
    public ApiResponse<ReportTemplateDto> createTemplate(
            @RequestBody CreateTemplateRequest request,
            @Parameter(description = "擁有者 ID") @RequestParam Long ownerId) {
        log.info("建立報表範本: {}", request.getName());
        ReportTemplateDto template = templateService.createTemplate(request, ownerId);
        return ApiResponse.success(template);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新範本", description = "更新指定的報表範本")
    public ApiResponse<ReportTemplateDto> updateTemplate(
            @Parameter(description = "範本 ID") @PathVariable Long id,
            @RequestBody UpdateTemplateRequest request,
            @Parameter(description = "使用者 ID") @RequestParam Long userId) {
        log.info("更新報表範本: {}", id);
        ReportTemplateDto template = templateService.updateTemplate(id, request, userId);
        return ApiResponse.success(template);
    }

    @GetMapping("/{id}")
    @Operation(summary = "取得範本詳情", description = "取得指定報表範本的詳細資訊")
    public ApiResponse<ReportTemplateDto> getTemplate(
            @Parameter(description = "範本 ID") @PathVariable Long id) {
        ReportTemplateDto template = templateService.getTemplate(id);
        return ApiResponse.success(template);
    }

    @GetMapping
    @Operation(summary = "查詢範本列表", description = "根據條件查詢報表範本")
    public ApiResponse<PageResponse<ReportTemplateDto>> searchTemplates(
            @Parameter(description = "關鍵字") @RequestParam(required = false) String keyword,
            @Parameter(description = "報表類型") @RequestParam(required = false) ReportType reportType,
            @Parameter(description = "使用者 ID") @RequestParam Long userId,
            @Parameter(description = "頁碼") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每頁筆數") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "排序欄位") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "排序方向") @RequestParam(defaultValue = "DESC") Sort.Direction direction) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<ReportTemplateDto> templates = templateService.searchTemplates(keyword, reportType, userId, pageable);

        return ApiResponse.success(PageResponse.of(templates));
    }

    @GetMapping("/accessible")
    @Operation(summary = "取得可用範本", description = "取得用戶可見的所有範本")
    public ApiResponse<List<ReportTemplateDto>> getAccessibleTemplates(
            @Parameter(description = "使用者 ID") @RequestParam Long userId) {
        List<ReportTemplateDto> templates = templateService.getAccessibleTemplates(userId);
        return ApiResponse.success(templates);
    }

    @GetMapping("/popular")
    @Operation(summary = "取得熱門範本", description = "取得最常使用的範本")
    public ApiResponse<List<ReportTemplateDto>> getPopularTemplates(
            @Parameter(description = "數量限制") @RequestParam(defaultValue = "10") int limit) {
        List<ReportTemplateDto> templates = templateService.getPopularTemplates(limit);
        return ApiResponse.success(templates);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "刪除範本", description = "刪除指定的報表範本")
    public ApiResponse<Void> deleteTemplate(
            @Parameter(description = "範本 ID") @PathVariable Long id,
            @Parameter(description = "使用者 ID") @RequestParam Long userId) {
        log.info("刪除報表範本: {}", id);
        templateService.deleteTemplate(id, userId);
        return ApiResponse.success(null);
    }

    @PostMapping("/{id}/copy")
    @Operation(summary = "複製範本", description = "複製指定的報表範本")
    public ApiResponse<ReportTemplateDto> copyTemplate(
            @Parameter(description = "範本 ID") @PathVariable Long id,
            @Parameter(description = "新範本名稱") @RequestParam String newName,
            @Parameter(description = "使用者 ID") @RequestParam Long userId) {
        log.info("複製報表範本: {}", id);
        ReportTemplateDto template = templateService.copyTemplate(id, newName, userId);
        return ApiResponse.success(template);
    }

    @GetMapping("/definitions")
    @Operation(summary = "取得欄位定義", description = "取得各報表類型的可用欄位定義")
    public ApiResponse<List<ReportTypeDefinition>> getReportTypeDefinitions() {
        List<ReportTypeDefinition> definitions = templateService.getReportTypeDefinitions();
        return ApiResponse.success(definitions);
    }
}
