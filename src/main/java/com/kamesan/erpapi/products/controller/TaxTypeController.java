package com.kamesan.erpapi.products.controller;

import com.kamesan.erpapi.common.dto.ApiResponse;
import com.kamesan.erpapi.common.dto.PageResponse;
import com.kamesan.erpapi.common.exception.BusinessException;
import com.kamesan.erpapi.products.entity.TaxType;
import com.kamesan.erpapi.products.repository.ProductRepository;
import com.kamesan.erpapi.products.repository.TaxTypeRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 稅別控制器
 *
 * <p>處理稅別相關的 API 請求，包括：</p>
 * <ul>
 *   <li>POST /api/v1/tax-types - 建立稅別</li>
 *   <li>GET /api/v1/tax-types - 查詢稅別列表</li>
 *   <li>GET /api/v1/tax-types/{id} - 根據 ID 查詢稅別</li>
 *   <li>GET /api/v1/tax-types/code/{code} - 根據代碼查詢稅別</li>
 *   <li>PUT /api/v1/tax-types/{id} - 更新稅別</li>
 *   <li>DELETE /api/v1/tax-types/{id} - 刪除稅別</li>
 *   <li>GET /api/v1/tax-types/active - 查詢啟用的稅別</li>
 * </ul>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/tax-types")
@RequiredArgsConstructor
@Tag(name = "稅別管理", description = "稅別 CRUD 操作")
public class TaxTypeController {

    /**
     * 稅別 Repository
     */
    private final TaxTypeRepository taxTypeRepository;

    /**
     * 商品 Repository（用於檢查關聯）
     */
    private final ProductRepository productRepository;

    /**
     * 建立稅別
     *
     * @param request 建立稅別請求
     * @return 建立的稅別資料
     */
    @PostMapping
    @Operation(summary = "建立稅別", description = "建立新的稅別")
    @Transactional
    public ApiResponse<TaxTypeDto> createTaxType(
            @Valid @RequestBody CreateTaxTypeRequest request) {

        log.info("收到建立稅別請求: Code={}", request.getCode());

        // 檢查代碼是否已存在
        if (taxTypeRepository.existsByCode(request.getCode())) {
            throw BusinessException.alreadyExists("稅別", "代碼", request.getCode());
        }

        // 如果設為預設，先清除其他預設
        if (request.getIsDefault() != null && request.getIsDefault()) {
            clearDefaultTaxType();
        }

        TaxType taxType = TaxType.builder()
                .code(request.getCode())
                .name(request.getName())
                .rate(request.getRate())
                .isDefault(request.getIsDefault() != null && request.getIsDefault())
                .active(request.getActive() != null ? request.getActive() : true)
                .build();

        taxType = taxTypeRepository.save(taxType);

        log.info("稅別建立成功: ID={}, Code={}", taxType.getId(), taxType.getCode());

        return ApiResponse.success("稅別建立成功", convertToDto(taxType));
    }

    /**
     * 查詢稅別列表（分頁）
     *
     * @param page    頁碼（從 1 開始）
     * @param size    每頁筆數
     * @param sortBy  排序欄位
     * @param sortDir 排序方向
     * @return 稅別分頁
     */
    @GetMapping
    @Operation(summary = "查詢稅別列表", description = "分頁查詢稅別")
    public ApiResponse<PageResponse<TaxTypeDto>> getTaxTypes(
            @Parameter(description = "頁碼（從 1 開始）")
            @RequestParam(defaultValue = "1") int page,

            @Parameter(description = "每頁筆數")
            @RequestParam(defaultValue = "20") int size,

            @Parameter(description = "排序欄位")
            @RequestParam(defaultValue = "code") String sortBy,

            @Parameter(description = "排序方向（asc/desc）")
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page - 1, size, sort);

        Page<TaxType> taxTypes = taxTypeRepository.findAll(pageable);

        List<TaxTypeDto> dtoList = taxTypes.getContent().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        return ApiResponse.success(PageResponse.of(taxTypes, dtoList));
    }

    /**
     * 根據 ID 查詢稅別
     *
     * @param id 稅別 ID
     * @return 稅別資料
     */
    @GetMapping("/{id}")
    @Operation(summary = "根據 ID 查詢稅別", description = "根據稅別 ID 查詢詳細資料")
    public ApiResponse<TaxTypeDto> getTaxTypeById(
            @Parameter(description = "稅別 ID", required = true)
            @PathVariable Long id) {

        TaxType taxType = taxTypeRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("稅別", id));

        return ApiResponse.success(convertToDto(taxType));
    }

    /**
     * 根據代碼查詢稅別
     *
     * @param code 稅別代碼
     * @return 稅別資料
     */
    @GetMapping("/code/{code}")
    @Operation(summary = "根據代碼查詢稅別", description = "根據稅別代碼查詢詳細資料")
    public ApiResponse<TaxTypeDto> getTaxTypeByCode(
            @Parameter(description = "稅別代碼", required = true)
            @PathVariable String code) {

        TaxType taxType = taxTypeRepository.findByCode(code)
                .orElseThrow(() -> BusinessException.notFound("稅別", "代碼: " + code));

        return ApiResponse.success(convertToDto(taxType));
    }

    /**
     * 更新稅別
     *
     * @param id      稅別 ID
     * @param request 更新稅別請求
     * @return 更新後的稅別資料
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新稅別", description = "更新稅別資料")
    @Transactional
    public ApiResponse<TaxTypeDto> updateTaxType(
            @Parameter(description = "稅別 ID", required = true)
            @PathVariable Long id,
            @Valid @RequestBody UpdateTaxTypeRequest request) {

        log.info("收到更新稅別請求: ID={}", id);

        TaxType taxType = taxTypeRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("稅別", id));

        // 檢查代碼是否已被其他稅別使用
        if (taxTypeRepository.existsByCodeAndIdNot(request.getCode(), id)) {
            throw BusinessException.alreadyExists("稅別", "代碼", request.getCode());
        }

        // 如果設為預設，先清除其他預設
        if (request.getIsDefault() != null && request.getIsDefault() && !taxType.isDefault()) {
            clearDefaultTaxType();
        }

        taxType.setCode(request.getCode());
        taxType.setName(request.getName());
        taxType.setRate(request.getRate());

        if (request.getIsDefault() != null) {
            taxType.setDefault(request.getIsDefault());
        }

        if (request.getActive() != null) {
            taxType.setActive(request.getActive());
        }

        taxType = taxTypeRepository.save(taxType);

        log.info("稅別更新成功: ID={}, Code={}", taxType.getId(), taxType.getCode());

        return ApiResponse.success("稅別更新成功", convertToDto(taxType));
    }

    /**
     * 刪除稅別
     *
     * @param id 稅別 ID
     * @return 刪除結果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "刪除稅別", description = "刪除稅別，若有商品使用則無法刪除")
    @Transactional
    public ApiResponse<Void> deleteTaxType(
            @Parameter(description = "稅別 ID", required = true)
            @PathVariable Long id) {

        log.info("收到刪除稅別請求: ID={}", id);

        TaxType taxType = taxTypeRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("稅別", id));

        // 檢查是否有商品使用此稅別
        long productCount = productRepository.countByTaxTypeId(id);
        if (productCount > 0) {
            throw BusinessException.validationFailed(
                    String.format("無法刪除稅別，有 %d 個商品使用此稅別", productCount));
        }

        taxTypeRepository.delete(taxType);

        log.info("稅別刪除成功: ID={}", id);

        return ApiResponse.success("稅別刪除成功", null);
    }

    /**
     * 查詢啟用的稅別列表
     *
     * @return 啟用的稅別列表
     */
    @GetMapping("/active")
    @Operation(summary = "查詢啟用的稅別", description = "查詢所有啟用的稅別，按預設和代碼排序")
    public ApiResponse<List<TaxTypeDto>> getActiveTaxTypes() {

        List<TaxType> taxTypes = taxTypeRepository.findAllActiveOrderByDefaultAndCode();

        List<TaxTypeDto> dtoList = taxTypes.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        return ApiResponse.success(dtoList);
    }

    /**
     * 清除預設稅別
     */
    private void clearDefaultTaxType() {
        taxTypeRepository.findDefaultTaxType().ifPresent(defaultTaxType -> {
            defaultTaxType.setDefault(false);
            taxTypeRepository.save(defaultTaxType);
        });
    }

    /**
     * 將稅別實體轉換為 DTO
     *
     * @param taxType 稅別實體
     * @return 稅別 DTO
     */
    private TaxTypeDto convertToDto(TaxType taxType) {
        return TaxTypeDto.builder()
                .id(taxType.getId())
                .code(taxType.getCode())
                .name(taxType.getName())
                .rate(taxType.getRate())
                .isDefault(taxType.isDefault())
                .active(taxType.isActive())
                .createdAt(taxType.getCreatedAt())
                .updatedAt(taxType.getUpdatedAt())
                .build();
    }

    /**
     * 稅別 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "稅別資料")
    public static class TaxTypeDto {
        @Schema(description = "稅別 ID", example = "1")
        private Long id;

        @Schema(description = "稅別代碼", example = "TAX")
        private String code;

        @Schema(description = "稅別名稱", example = "應稅")
        private String name;

        @Schema(description = "稅率（百分比）", example = "5.00")
        private BigDecimal rate;

        @Schema(description = "是否為預設稅別", example = "true")
        private boolean isDefault;

        @Schema(description = "是否啟用", example = "true")
        private boolean active;

        @Schema(description = "建立時間")
        private LocalDateTime createdAt;

        @Schema(description = "更新時間")
        private LocalDateTime updatedAt;
    }

    /**
     * 建立稅別請求
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "建立稅別請求")
    public static class CreateTaxTypeRequest {
        @NotBlank(message = "稅別代碼不能為空")
        @Size(max = 20, message = "稅別代碼長度不能超過 20 字元")
        @Schema(description = "稅別代碼", example = "TAX", requiredMode = Schema.RequiredMode.REQUIRED)
        private String code;

        @NotBlank(message = "稅別名稱不能為空")
        @Size(max = 50, message = "稅別名稱長度不能超過 50 字元")
        @Schema(description = "稅別名稱", example = "應稅", requiredMode = Schema.RequiredMode.REQUIRED)
        private String name;

        @DecimalMin(value = "0", message = "稅率不能為負數")
        @Digits(integer = 3, fraction = 2, message = "稅率格式不正確")
        @Schema(description = "稅率（百分比）", example = "5.00", requiredMode = Schema.RequiredMode.REQUIRED)
        private BigDecimal rate;

        @Schema(description = "是否為預設稅別", example = "false")
        private Boolean isDefault;

        @Schema(description = "是否啟用", example = "true")
        private Boolean active;
    }

    /**
     * 更新稅別請求
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "更新稅別請求")
    public static class UpdateTaxTypeRequest {
        @NotBlank(message = "稅別代碼不能為空")
        @Size(max = 20, message = "稅別代碼長度不能超過 20 字元")
        @Schema(description = "稅別代碼", example = "TAX", requiredMode = Schema.RequiredMode.REQUIRED)
        private String code;

        @NotBlank(message = "稅別名稱不能為空")
        @Size(max = 50, message = "稅別名稱長度不能超過 50 字元")
        @Schema(description = "稅別名稱", example = "應稅", requiredMode = Schema.RequiredMode.REQUIRED)
        private String name;

        @DecimalMin(value = "0", message = "稅率不能為負數")
        @Digits(integer = 3, fraction = 2, message = "稅率格式不正確")
        @Schema(description = "稅率（百分比）", example = "5.00", requiredMode = Schema.RequiredMode.REQUIRED)
        private BigDecimal rate;

        @Schema(description = "是否為預設稅別", example = "false")
        private Boolean isDefault;

        @Schema(description = "是否啟用", example = "true")
        private Boolean active;
    }
}
