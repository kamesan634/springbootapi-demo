package com.kamesan.erpapi.customers.controller;

import com.kamesan.erpapi.common.dto.ApiResponse;
import com.kamesan.erpapi.customers.dto.RfmAnalysisDto;
import com.kamesan.erpapi.customers.service.RfmAnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * RFM 客戶分析 Controller
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/customers/rfm")
@RequiredArgsConstructor
@Tag(name = "RFM 分析", description = "RFM 客戶價值分析 API")
public class RfmAnalysisController {

    private final RfmAnalysisService rfmAnalysisService;

    @GetMapping("/analyze")
    @Operation(summary = "執行 RFM 分析", description = "對指定期間的客戶進行 RFM 分析")
    public ApiResponse<RfmAnalysisDto> analyzeRfm(
            @Parameter(description = "分析起始日期")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "分析結束日期")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        log.info("執行 RFM 分析: {} ~ {}", startDate, endDate);

        RfmAnalysisDto result = rfmAnalysisService.analyzeRfm(startDate, endDate);

        return ApiResponse.success(result);
    }

    @GetMapping("/analyze/last-year")
    @Operation(summary = "執行最近一年 RFM 分析", description = "對最近一年的客戶進行 RFM 分析")
    public ApiResponse<RfmAnalysisDto> analyzeRfmLastYear() {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusYears(1);

        log.info("執行最近一年 RFM 分析: {} ~ {}", startDate, endDate);

        RfmAnalysisDto result = rfmAnalysisService.analyzeRfm(startDate, endDate);

        return ApiResponse.success(result);
    }
}
