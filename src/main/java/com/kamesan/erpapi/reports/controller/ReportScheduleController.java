package com.kamesan.erpapi.reports.controller;

import com.kamesan.erpapi.common.dto.ApiResponse;
import com.kamesan.erpapi.common.dto.PageResponse;
import com.kamesan.erpapi.reports.dto.ReportScheduleDto;
import com.kamesan.erpapi.reports.dto.ReportScheduleDto.*;
import com.kamesan.erpapi.reports.entity.ReportSchedule.ReportType;
import com.kamesan.erpapi.reports.service.ReportScheduleService;
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

/**
 * 排程報表 Controller
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/reports/schedules")
@RequiredArgsConstructor
@Tag(name = "排程報表", description = "排程報表管理 API")
public class ReportScheduleController {

    private final ReportScheduleService scheduleService;

    @PostMapping
    @Operation(summary = "建立排程", description = "建立新的報表排程")
    public ApiResponse<ReportScheduleDto> createSchedule(@RequestBody CreateScheduleRequest request) {
        log.info("建立排程報表: {}", request.getName());
        ReportScheduleDto schedule = scheduleService.createSchedule(request);
        return ApiResponse.success(schedule);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新排程", description = "更新指定的報表排程")
    public ApiResponse<ReportScheduleDto> updateSchedule(
            @Parameter(description = "排程 ID") @PathVariable Long id,
            @RequestBody UpdateScheduleRequest request) {
        log.info("更新排程報表: {}", id);
        ReportScheduleDto schedule = scheduleService.updateSchedule(id, request);
        return ApiResponse.success(schedule);
    }

    @GetMapping("/{id}")
    @Operation(summary = "取得排程詳情", description = "取得指定排程的詳細資訊")
    public ApiResponse<ReportScheduleDto> getSchedule(
            @Parameter(description = "排程 ID") @PathVariable Long id) {
        ReportScheduleDto schedule = scheduleService.getSchedule(id);
        return ApiResponse.success(schedule);
    }

    @GetMapping
    @Operation(summary = "查詢排程列表", description = "根據條件查詢報表排程")
    public ApiResponse<PageResponse<ReportScheduleDto>> searchSchedules(
            @Parameter(description = "關鍵字") @RequestParam(required = false) String keyword,
            @Parameter(description = "報表類型") @RequestParam(required = false) ReportType reportType,
            @Parameter(description = "是否啟用") @RequestParam(required = false) Boolean active,
            @Parameter(description = "頁碼") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每頁筆數") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "排序欄位") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "排序方向") @RequestParam(defaultValue = "DESC") Sort.Direction direction) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<ReportScheduleDto> schedules = scheduleService.searchSchedules(keyword, reportType, active, pageable);

        return ApiResponse.success(PageResponse.of(schedules));
    }

    @PutMapping("/{id}/toggle-active")
    @Operation(summary = "切換啟用狀態", description = "啟用或停用指定的報表排程")
    public ApiResponse<ReportScheduleDto> toggleActive(
            @Parameter(description = "排程 ID") @PathVariable Long id) {
        log.info("切換排程啟用狀態: {}", id);
        ReportScheduleDto schedule = scheduleService.toggleActive(id);
        return ApiResponse.success(schedule);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "刪除排程", description = "刪除指定的報表排程")
    public ApiResponse<Void> deleteSchedule(
            @Parameter(description = "排程 ID") @PathVariable Long id) {
        log.info("刪除排程報表: {}", id);
        scheduleService.deleteSchedule(id);
        return ApiResponse.success(null);
    }

    @PostMapping("/{id}/execute")
    @Operation(summary = "立即執行", description = "立即執行指定的報表排程")
    public ApiResponse<ExecutionDto> executeNow(
            @Parameter(description = "排程 ID") @PathVariable Long id,
            @Parameter(description = "執行者 ID") @RequestParam(required = false) Long executedBy) {
        log.info("手動執行排程: {}", id);
        ExecutionDto execution = scheduleService.executeNow(id, executedBy);
        return ApiResponse.success(execution);
    }

    @GetMapping("/{id}/history")
    @Operation(summary = "取得執行歷史", description = "取得指定排程的執行歷史記錄")
    public ApiResponse<PageResponse<ExecutionDto>> getExecutionHistory(
            @Parameter(description = "排程 ID") @PathVariable Long id,
            @Parameter(description = "頁碼") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每頁筆數") @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<ExecutionDto> history = scheduleService.getExecutionHistory(id, pageable);

        return ApiResponse.success(PageResponse.of(history));
    }

    @GetMapping("/stats")
    @Operation(summary = "取得排程統計", description = "取得排程報表的統計資訊")
    public ApiResponse<ScheduleStatsDto> getScheduleStats() {
        ScheduleStatsDto stats = scheduleService.getScheduleStats();
        return ApiResponse.success(stats);
    }
}
