package com.kamesan.erpapi.system.controller;

import com.kamesan.erpapi.common.dto.ApiResponse;
import com.kamesan.erpapi.common.dto.PageResponse;
import com.kamesan.erpapi.system.dto.AuditLogDto;
import com.kamesan.erpapi.system.service.AuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 稽核日誌 Controller
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/audit-logs")
@RequiredArgsConstructor
@Tag(name = "稽核日誌", description = "稽核日誌查詢 API")
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    @Operation(summary = "查詢稽核日誌", description = "根據條件查詢稽核日誌（分頁）")
    public ResponseEntity<ApiResponse<PageResponse<AuditLogDto>>> queryLogs(
            @Parameter(description = "操作類型") @RequestParam(required = false) String action,
            @Parameter(description = "實體類型") @RequestParam(required = false) String entityType,
            @Parameter(description = "使用者 ID") @RequestParam(required = false) Long userId,
            @Parameter(description = "開始時間") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @Parameter(description = "結束時間") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<AuditLogDto> page = auditLogService.queryLogs(action, entityType, userId, startTime, endTime, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(page)));
    }

    @GetMapping("/entity/{entityType}/{entityId}")
    @Operation(summary = "查詢實體日誌", description = "根據實體類型和 ID 查詢相關的稽核日誌")
    public ResponseEntity<ApiResponse<List<AuditLogDto>>> getLogsByEntity(
            @Parameter(description = "實體類型") @PathVariable String entityType,
            @Parameter(description = "實體 ID") @PathVariable Long entityId) {

        List<AuditLogDto> logs = auditLogService.getLogsByEntity(entityType, entityId);
        return ResponseEntity.ok(ApiResponse.success(logs));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "查詢使用者日誌", description = "查詢指定使用者的操作日誌")
    public ResponseEntity<ApiResponse<PageResponse<AuditLogDto>>> getLogsByUser(
            @Parameter(description = "使用者 ID") @PathVariable Long userId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<AuditLogDto> page = auditLogService.getLogsByUserId(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(page)));
    }
}
