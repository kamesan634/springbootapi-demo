package com.kamesan.erpapi.products.controller;

import com.kamesan.erpapi.common.dto.ApiResponse;
import com.kamesan.erpapi.common.dto.PageResponse;
import com.kamesan.erpapi.common.exception.BusinessException;
import com.kamesan.erpapi.products.entity.Unit;
import com.kamesan.erpapi.products.repository.ProductRepository;
import com.kamesan.erpapi.products.repository.UnitRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 單位控制器
 *
 * <p>處理單位相關的 API 請求，包括：</p>
 * <ul>
 *   <li>POST /api/v1/units - 建立單位</li>
 *   <li>GET /api/v1/units - 查詢單位列表</li>
 *   <li>GET /api/v1/units/{id} - 根據 ID 查詢單位</li>
 *   <li>GET /api/v1/units/code/{code} - 根據代碼查詢單位</li>
 *   <li>PUT /api/v1/units/{id} - 更新單位</li>
 *   <li>DELETE /api/v1/units/{id} - 刪除單位</li>
 *   <li>GET /api/v1/units/active - 查詢啟用的單位</li>
 * </ul>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/units")
@RequiredArgsConstructor
@Tag(name = "單位管理", description = "單位 CRUD 操作")
public class UnitController {

    /**
     * 單位 Repository
     */
    private final UnitRepository unitRepository;

    /**
     * 商品 Repository（用於檢查關聯）
     */
    private final ProductRepository productRepository;

    /**
     * 建立單位
     *
     * @param request 建立單位請求
     * @return 建立的單位資料
     */
    @PostMapping
    @Operation(summary = "建立單位", description = "建立新的計量單位")
    @Transactional
    public ApiResponse<UnitDto> createUnit(
            @Valid @RequestBody CreateUnitRequest request) {

        log.info("收到建立單位請求: Code={}", request.getCode());

        // 檢查代碼是否已存在
        if (unitRepository.existsByCode(request.getCode())) {
            throw BusinessException.alreadyExists("單位", "代碼", request.getCode());
        }

        Unit unit = Unit.builder()
                .code(request.getCode())
                .name(request.getName())
                .active(request.getActive() != null ? request.getActive() : true)
                .build();

        unit = unitRepository.save(unit);

        log.info("單位建立成功: ID={}, Code={}", unit.getId(), unit.getCode());

        return ApiResponse.success("單位建立成功", convertToDto(unit));
    }

    /**
     * 查詢單位列表（分頁）
     *
     * @param page    頁碼（從 1 開始）
     * @param size    每頁筆數
     * @param sortBy  排序欄位
     * @param sortDir 排序方向
     * @return 單位分頁
     */
    @GetMapping
    @Operation(summary = "查詢單位列表", description = "分頁查詢單位")
    public ApiResponse<PageResponse<UnitDto>> getUnits(
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

        Pageable pageable = PageRequest.of(Math.max(0, page - 1), size, sort);

        Page<Unit> units = unitRepository.findAll(pageable);

        List<UnitDto> dtoList = units.getContent().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        return ApiResponse.success(PageResponse.of(units, dtoList));
    }

    /**
     * 根據 ID 查詢單位
     *
     * @param id 單位 ID
     * @return 單位資料
     */
    @GetMapping("/{id}")
    @Operation(summary = "根據 ID 查詢單位", description = "根據單位 ID 查詢詳細資料")
    public ApiResponse<UnitDto> getUnitById(
            @Parameter(description = "單位 ID", required = true)
            @PathVariable Long id) {

        Unit unit = unitRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("單位", id));

        return ApiResponse.success(convertToDto(unit));
    }

    /**
     * 根據代碼查詢單位
     *
     * @param code 單位代碼
     * @return 單位資料
     */
    @GetMapping("/code/{code}")
    @Operation(summary = "根據代碼查詢單位", description = "根據單位代碼查詢詳細資料")
    public ApiResponse<UnitDto> getUnitByCode(
            @Parameter(description = "單位代碼", required = true)
            @PathVariable String code) {

        Unit unit = unitRepository.findByCode(code)
                .orElseThrow(() -> BusinessException.notFound("單位", "代碼: " + code));

        return ApiResponse.success(convertToDto(unit));
    }

    /**
     * 更新單位
     *
     * @param id      單位 ID
     * @param request 更新單位請求
     * @return 更新後的單位資料
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新單位", description = "更新單位資料")
    @Transactional
    public ApiResponse<UnitDto> updateUnit(
            @Parameter(description = "單位 ID", required = true)
            @PathVariable Long id,
            @Valid @RequestBody UpdateUnitRequest request) {

        log.info("收到更新單位請求: ID={}", id);

        Unit unit = unitRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("單位", id));

        // 檢查代碼是否已被其他單位使用
        if (unitRepository.existsByCodeAndIdNot(request.getCode(), id)) {
            throw BusinessException.alreadyExists("單位", "代碼", request.getCode());
        }

        unit.setCode(request.getCode());
        unit.setName(request.getName());

        if (request.getActive() != null) {
            unit.setActive(request.getActive());
        }

        unit = unitRepository.save(unit);

        log.info("單位更新成功: ID={}, Code={}", unit.getId(), unit.getCode());

        return ApiResponse.success("單位更新成功", convertToDto(unit));
    }

    /**
     * 刪除單位
     *
     * @param id 單位 ID
     * @return 刪除結果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "刪除單位", description = "刪除單位，若有商品使用則無法刪除")
    @Transactional
    public ApiResponse<Void> deleteUnit(
            @Parameter(description = "單位 ID", required = true)
            @PathVariable Long id) {

        log.info("收到刪除單位請求: ID={}", id);

        Unit unit = unitRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("單位", id));

        // 檢查是否有商品使用此單位
        long productCount = productRepository.countByUnitId(id);
        if (productCount > 0) {
            throw BusinessException.validationFailed(
                    String.format("無法刪除單位，有 %d 個商品使用此單位", productCount));
        }

        unitRepository.delete(unit);

        log.info("單位刪除成功: ID={}", id);

        return ApiResponse.success("單位刪除成功", null);
    }

    /**
     * 查詢啟用的單位列表
     *
     * @return 啟用的單位列表
     */
    @GetMapping("/active")
    @Operation(summary = "查詢啟用的單位", description = "查詢所有啟用的單位，按代碼排序")
    public ApiResponse<List<UnitDto>> getActiveUnits() {

        List<Unit> units = unitRepository.findAllActiveOrderByCode();

        List<UnitDto> dtoList = units.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        return ApiResponse.success(dtoList);
    }

    /**
     * 搜尋單位
     *
     * @param keyword 關鍵字
     * @param page    頁碼
     * @param size    每頁筆數
     * @return 單位分頁
     */
    @GetMapping("/search")
    @Operation(summary = "搜尋單位", description = "根據關鍵字搜尋單位（名稱、代碼）")
    public ApiResponse<PageResponse<UnitDto>> searchUnits(
            @Parameter(description = "搜尋關鍵字", required = true)
            @RequestParam String keyword,

            @Parameter(description = "頁碼（從 1 開始）")
            @RequestParam(defaultValue = "1") int page,

            @Parameter(description = "每頁筆數")
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(Math.max(0, page - 1), size, Sort.by("code").ascending());

        Page<Unit> units = unitRepository.search(keyword, pageable);

        List<UnitDto> dtoList = units.getContent().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        return ApiResponse.success(PageResponse.of(units, dtoList));
    }

    /**
     * 將單位實體轉換為 DTO
     *
     * @param unit 單位實體
     * @return 單位 DTO
     */
    private UnitDto convertToDto(Unit unit) {
        return UnitDto.builder()
                .id(unit.getId())
                .code(unit.getCode())
                .name(unit.getName())
                .active(unit.isActive())
                .createdAt(unit.getCreatedAt())
                .updatedAt(unit.getUpdatedAt())
                .build();
    }

    /**
     * 單位 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "單位資料")
    public static class UnitDto {
        @Schema(description = "單位 ID", example = "1")
        private Long id;

        @Schema(description = "單位代碼", example = "PCS")
        private String code;

        @Schema(description = "單位名稱", example = "個")
        private String name;

        @Schema(description = "是否啟用", example = "true")
        private boolean active;

        @Schema(description = "建立時間")
        private LocalDateTime createdAt;

        @Schema(description = "更新時間")
        private LocalDateTime updatedAt;
    }

    /**
     * 建立單位請求
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "建立單位請求")
    public static class CreateUnitRequest {
        @NotBlank(message = "單位代碼不能為空")
        @Size(max = 20, message = "單位代碼長度不能超過 20 字元")
        @Schema(description = "單位代碼", example = "PCS", requiredMode = Schema.RequiredMode.REQUIRED)
        private String code;

        @NotBlank(message = "單位名稱不能為空")
        @Size(max = 50, message = "單位名稱長度不能超過 50 字元")
        @Schema(description = "單位名稱", example = "個", requiredMode = Schema.RequiredMode.REQUIRED)
        private String name;

        @Schema(description = "是否啟用", example = "true")
        private Boolean active;
    }

    /**
     * 更新單位請求
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "更新單位請求")
    public static class UpdateUnitRequest {
        @NotBlank(message = "單位代碼不能為空")
        @Size(max = 20, message = "單位代碼長度不能超過 20 字元")
        @Schema(description = "單位代碼", example = "PCS", requiredMode = Schema.RequiredMode.REQUIRED)
        private String code;

        @NotBlank(message = "單位名稱不能為空")
        @Size(max = 50, message = "單位名稱長度不能超過 50 字元")
        @Schema(description = "單位名稱", example = "個", requiredMode = Schema.RequiredMode.REQUIRED)
        private String name;

        @Schema(description = "是否啟用", example = "true")
        private Boolean active;
    }
}
