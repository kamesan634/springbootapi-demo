package com.kamesan.erpapi.purchasing.controller;

import com.kamesan.erpapi.common.dto.ApiResponse;
import com.kamesan.erpapi.purchasing.dto.PurchaseSuggestionDto;
import com.kamesan.erpapi.purchasing.dto.PurchaseSuggestionDto.SuggestionItem;
import com.kamesan.erpapi.purchasing.service.PurchaseSuggestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 採購建議 Controller
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/purchasing/suggestions")
@RequiredArgsConstructor
@Tag(name = "採購建議", description = "採購建議計算與查詢 API")
public class PurchaseSuggestionController {

    private final PurchaseSuggestionService suggestionService;

    @GetMapping
    @Operation(summary = "取得採購建議", description = "根據庫存狀況、銷售歷史計算採購建議")
    public ApiResponse<PurchaseSuggestionDto> getSuggestions(
            @Parameter(description = "倉庫 ID") @RequestParam(required = false) Long warehouseId,
            @Parameter(description = "分類 ID") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "是否只顯示低庫存") @RequestParam(defaultValue = "true") boolean onlyLowStock) {

        log.info("取得採購建議: warehouseId={}, categoryId={}, onlyLowStock={}", warehouseId, categoryId, onlyLowStock);

        PurchaseSuggestionDto suggestions = suggestionService.calculateSuggestions(warehouseId, categoryId, onlyLowStock);

        return ApiResponse.success(suggestions);
    }

    @GetMapping("/out-of-stock")
    @Operation(summary = "取得缺貨商品", description = "取得目前缺貨的商品列表")
    public ApiResponse<List<SuggestionItem>> getOutOfStockProducts(
            @Parameter(description = "倉庫 ID") @RequestParam(required = false) Long warehouseId) {

        log.info("取得缺貨商品: warehouseId={}", warehouseId);

        List<SuggestionItem> items = suggestionService.getOutOfStockProducts(warehouseId);

        return ApiResponse.success(items);
    }

    @GetMapping("/low-stock")
    @Operation(summary = "取得低庫存商品", description = "取得目前低庫存的商品列表")
    public ApiResponse<List<SuggestionItem>> getLowStockProducts(
            @Parameter(description = "倉庫 ID") @RequestParam(required = false) Long warehouseId) {

        log.info("取得低庫存商品: warehouseId={}", warehouseId);

        List<SuggestionItem> items = suggestionService.getLowStockProducts(warehouseId);

        return ApiResponse.success(items);
    }
}
