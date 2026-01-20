package com.kamesan.erpapi.products.controller;

import com.kamesan.erpapi.common.dto.ApiResponse;
import com.kamesan.erpapi.common.dto.PageResponse;
import com.kamesan.erpapi.products.dto.ProductComboDto;
import com.kamesan.erpapi.products.dto.ProductComboDto.*;
import com.kamesan.erpapi.products.entity.ProductCombo.ComboType;
import com.kamesan.erpapi.products.service.ProductComboService;
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

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品組合/套餐 Controller
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/products/combos")
@RequiredArgsConstructor
@Tag(name = "商品組合", description = "商品組合/套餐管理 API")
public class ProductComboController {

    private final ProductComboService comboService;

    @PostMapping
    @Operation(summary = "建立商品組合", description = "建立新的商品組合/套餐")
    public ApiResponse<ProductComboDto> createCombo(@RequestBody CreateComboRequest request) {
        log.info("建立商品組合: {}", request.getCode());
        ProductComboDto combo = comboService.createCombo(request);
        return ApiResponse.success(combo);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新商品組合", description = "更新指定的商品組合")
    public ApiResponse<ProductComboDto> updateCombo(
            @Parameter(description = "組合 ID") @PathVariable Long id,
            @RequestBody UpdateComboRequest request) {
        log.info("更新商品組合: {}", id);
        ProductComboDto combo = comboService.updateCombo(id, request);
        return ApiResponse.success(combo);
    }

    @GetMapping("/{id}")
    @Operation(summary = "取得組合詳情", description = "取得指定商品組合的詳細資訊")
    public ApiResponse<ProductComboDto> getCombo(
            @Parameter(description = "組合 ID") @PathVariable Long id) {
        ProductComboDto combo = comboService.getCombo(id);
        return ApiResponse.success(combo);
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "根據編碼取得組合", description = "根據組合編碼取得詳細資訊")
    public ApiResponse<ProductComboDto> getComboByCode(
            @Parameter(description = "組合編碼") @PathVariable String code) {
        ProductComboDto combo = comboService.getComboByCode(code);
        return ApiResponse.success(combo);
    }

    @GetMapping
    @Operation(summary = "查詢組合列表", description = "根據條件查詢商品組合")
    public ApiResponse<PageResponse<ProductComboDto>> searchCombos(
            @Parameter(description = "關鍵字") @RequestParam(required = false) String keyword,
            @Parameter(description = "組合類型") @RequestParam(required = false) ComboType comboType,
            @Parameter(description = "是否啟用") @RequestParam(required = false) Boolean active,
            @Parameter(description = "頁碼") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每頁筆數") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "排序欄位") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "排序方向") @RequestParam(defaultValue = "DESC") Sort.Direction direction) {

        ComboSearchCriteria criteria = ComboSearchCriteria.builder()
                .keyword(keyword)
                .comboType(comboType)
                .active(active)
                .build();

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<ProductComboDto> combos = comboService.searchCombos(criteria, pageable);

        return ApiResponse.success(PageResponse.of(combos));
    }

    @GetMapping("/sellable")
    @Operation(summary = "取得可銷售組合", description = "取得目前可銷售的商品組合")
    public ApiResponse<List<ProductComboDto>> getSellableCombos() {
        List<ProductComboDto> combos = comboService.getSellableCombos();
        return ApiResponse.success(combos);
    }

    @PutMapping("/{id}/toggle-active")
    @Operation(summary = "切換啟用狀態", description = "啟用或停用指定的商品組合")
    public ApiResponse<ProductComboDto> toggleActive(
            @Parameter(description = "組合 ID") @PathVariable Long id) {
        log.info("切換組合啟用狀態: {}", id);
        ProductComboDto combo = comboService.toggleActive(id);
        return ApiResponse.success(combo);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "刪除商品組合", description = "刪除指定的商品組合")
    public ApiResponse<Void> deleteCombo(
            @Parameter(description = "組合 ID") @PathVariable Long id) {
        log.info("刪除商品組合: {}", id);
        comboService.deleteCombo(id);
        return ApiResponse.success(null);
    }

    @GetMapping("/by-product/{productId}")
    @Operation(summary = "查詢包含商品的組合", description = "查詢包含指定商品的所有組合")
    public ApiResponse<List<ProductComboDto>> findCombosByProduct(
            @Parameter(description = "商品 ID") @PathVariable Long productId) {
        List<ProductComboDto> combos = comboService.findCombosByProduct(productId);
        return ApiResponse.success(combos);
    }

    @GetMapping("/expiring")
    @Operation(summary = "查詢即將到期組合", description = "查詢指定天數內即將到期的組合")
    public ApiResponse<List<ProductComboDto>> findExpiringCombos(
            @Parameter(description = "天數") @RequestParam(defaultValue = "7") int daysAhead) {
        List<ProductComboDto> combos = comboService.findExpiringCombos(daysAhead);
        return ApiResponse.success(combos);
    }

    @PostMapping("/{id}/calculate-price")
    @Operation(summary = "計算組合售價", description = "計算選配組合的實際售價")
    public ApiResponse<BigDecimal> calculateComboPrice(
            @Parameter(description = "組合 ID") @PathVariable Long id,
            @Parameter(description = "選中的商品 ID 列表") @RequestBody List<Long> selectedProductIds) {
        BigDecimal price = comboService.calculateComboPrice(id, selectedProductIds);
        return ApiResponse.success(price);
    }
}
