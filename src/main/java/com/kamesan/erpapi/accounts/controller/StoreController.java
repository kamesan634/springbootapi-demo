package com.kamesan.erpapi.accounts.controller;

import com.kamesan.erpapi.accounts.entity.Store;
import com.kamesan.erpapi.accounts.repository.StoreRepository;
import com.kamesan.erpapi.common.dto.ApiResponse;
import com.kamesan.erpapi.common.dto.PageResponse;
import com.kamesan.erpapi.common.exception.BusinessException;
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
 * 門市/倉庫控制器
 *
 * <p>處理門市/倉庫相關的 API 請求，包括：</p>
 * <ul>
 *   <li>POST /api/v1/stores - 建立門市/倉庫</li>
 *   <li>GET /api/v1/stores - 查詢門市/倉庫列表</li>
 *   <li>GET /api/v1/stores/{id} - 根據 ID 查詢</li>
 *   <li>GET /api/v1/stores/code/{code} - 根據代碼查詢</li>
 *   <li>PUT /api/v1/stores/{id} - 更新門市/倉庫</li>
 *   <li>DELETE /api/v1/stores/{id} - 刪除門市/倉庫</li>
 *   <li>GET /api/v1/stores/active - 查詢啟用的門市/倉庫</li>
 *   <li>GET /api/v1/stores/stores - 查詢所有門市</li>
 *   <li>GET /api/v1/stores/warehouses - 查詢所有倉庫</li>
 * </ul>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/stores")
@RequiredArgsConstructor
@Tag(name = "門市/倉庫管理", description = "門市/倉庫 CRUD 操作")
public class StoreController {

    private final StoreRepository storeRepository;

    /**
     * 建立門市/倉庫
     */
    @PostMapping
    @Operation(summary = "建立門市/倉庫", description = "建立新的門市或倉庫")
    @Transactional
    public ApiResponse<StoreDto> createStore(
            @Valid @RequestBody CreateStoreRequest request) {

        log.info("收到建立門市/倉庫請求: Code={}", request.getCode());

        if (storeRepository.existsByCode(request.getCode())) {
            throw BusinessException.alreadyExists("門市/倉庫", "代碼", request.getCode());
        }

        Store store = Store.builder()
                .code(request.getCode())
                .name(request.getName())
                .type(request.getType())
                .address(request.getAddress())
                .phone(request.getPhone())
                .email(request.getEmail())
                .businessHours(request.getBusinessHours())
                .main(request.getMain() != null && request.getMain())
                .active(request.getActive() != null ? request.getActive() : true)
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0)
                .notes(request.getNotes())
                .build();

        store = storeRepository.save(store);

        log.info("門市/倉庫建立成功: ID={}, Code={}", store.getId(), store.getCode());

        return ApiResponse.success("門市/倉庫建立成功", convertToDto(store));
    }

    /**
     * 查詢門市/倉庫列表（分頁）
     */
    @GetMapping
    @Operation(summary = "查詢門市/倉庫列表", description = "分頁查詢門市/倉庫")
    public ApiResponse<PageResponse<StoreDto>> getStores(
            @Parameter(description = "頁碼（從 1 開始）")
            @RequestParam(defaultValue = "1") int page,

            @Parameter(description = "每頁筆數")
            @RequestParam(defaultValue = "20") int size,

            @Parameter(description = "排序欄位")
            @RequestParam(defaultValue = "sortOrder") String sortBy,

            @Parameter(description = "排序方向（asc/desc）")
            @RequestParam(defaultValue = "asc") String sortDir,

            @Parameter(description = "類型篩選")
            @RequestParam(required = false) Store.StoreType type) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(Math.max(0, page - 1), size, sort);

        Page<Store> stores = storeRepository.findAll(pageable);

        List<StoreDto> dtoList = stores.getContent().stream()
                .filter(s -> type == null || s.getType() == type)
                .map(this::convertToDto)
                .collect(Collectors.toList());

        return ApiResponse.success(PageResponse.of(stores, dtoList));
    }

    /**
     * 根據 ID 查詢門市/倉庫
     */
    @GetMapping("/{id}")
    @Operation(summary = "根據 ID 查詢門市/倉庫", description = "根據門市/倉庫 ID 查詢詳細資料")
    public ApiResponse<StoreDto> getStoreById(
            @Parameter(description = "門市/倉庫 ID", required = true)
            @PathVariable Long id) {

        Store store = storeRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("門市/倉庫", id));

        return ApiResponse.success(convertToDto(store));
    }

    /**
     * 根據代碼查詢門市/倉庫
     */
    @GetMapping("/code/{code}")
    @Operation(summary = "根據代碼查詢門市/倉庫", description = "根據門市/倉庫代碼查詢詳細資料")
    public ApiResponse<StoreDto> getStoreByCode(
            @Parameter(description = "門市/倉庫代碼", required = true)
            @PathVariable String code) {

        Store store = storeRepository.findByCode(code)
                .orElseThrow(() -> BusinessException.notFound("門市/倉庫", "代碼: " + code));

        return ApiResponse.success(convertToDto(store));
    }

    /**
     * 更新門市/倉庫
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新門市/倉庫", description = "更新門市/倉庫資料")
    @Transactional
    public ApiResponse<StoreDto> updateStore(
            @Parameter(description = "門市/倉庫 ID", required = true)
            @PathVariable Long id,
            @Valid @RequestBody UpdateStoreRequest request) {

        log.info("收到更新門市/倉庫請求: ID={}", id);

        Store store = storeRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("門市/倉庫", id));

        if (request.getName() != null) {
            store.setName(request.getName());
        }
        if (request.getType() != null) {
            store.setType(request.getType());
        }
        if (request.getAddress() != null) {
            store.setAddress(request.getAddress());
        }
        if (request.getPhone() != null) {
            store.setPhone(request.getPhone());
        }
        if (request.getEmail() != null) {
            store.setEmail(request.getEmail());
        }
        if (request.getBusinessHours() != null) {
            store.setBusinessHours(request.getBusinessHours());
        }
        if (request.getMain() != null) {
            store.setMain(request.getMain());
        }
        if (request.getActive() != null) {
            store.setActive(request.getActive());
        }
        if (request.getSortOrder() != null) {
            store.setSortOrder(request.getSortOrder());
        }
        if (request.getNotes() != null) {
            store.setNotes(request.getNotes());
        }

        store = storeRepository.save(store);

        log.info("門市/倉庫更新成功: ID={}, Code={}", store.getId(), store.getCode());

        return ApiResponse.success("門市/倉庫更新成功", convertToDto(store));
    }

    /**
     * 刪除門市/倉庫（軟刪除）
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "刪除門市/倉庫", description = "將門市/倉庫設為停用狀態")
    @Transactional
    public ApiResponse<Void> deleteStore(
            @Parameter(description = "門市/倉庫 ID", required = true)
            @PathVariable Long id) {

        log.info("收到刪除門市/倉庫請求: ID={}", id);

        Store store = storeRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("門市/倉庫", id));

        store.setActive(false);
        storeRepository.save(store);

        log.info("門市/倉庫刪除（停用）成功: ID={}", id);

        return ApiResponse.success("門市/倉庫已停用", null);
    }

    /**
     * 查詢啟用的門市/倉庫列表
     */
    @GetMapping("/active")
    @Operation(summary = "查詢啟用的門市/倉庫", description = "查詢所有啟用的門市/倉庫，按排序順序")
    public ApiResponse<List<StoreDto>> getActiveStores() {

        List<Store> stores = storeRepository.findByActiveTrueOrderBySortOrder();

        List<StoreDto> dtoList = stores.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        return ApiResponse.success(dtoList);
    }

    /**
     * 查詢所有門市
     */
    @GetMapping("/type/store")
    @Operation(summary = "查詢所有門市", description = "查詢所有啟用的門市")
    public ApiResponse<List<StoreDto>> getAllStores() {

        List<Store> stores = storeRepository.findAllStores();

        List<StoreDto> dtoList = stores.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        return ApiResponse.success(dtoList);
    }

    /**
     * 查詢所有倉庫
     */
    @GetMapping("/type/warehouse")
    @Operation(summary = "查詢所有倉庫", description = "查詢所有啟用的倉庫")
    public ApiResponse<List<StoreDto>> getAllWarehouses() {

        List<Store> stores = storeRepository.findAllWarehouses();

        List<StoreDto> dtoList = stores.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        return ApiResponse.success(dtoList);
    }

    /**
     * 查詢主倉庫/總部
     */
    @GetMapping("/main")
    @Operation(summary = "查詢主倉庫/總部", description = "查詢設為主倉庫或總部的門市/倉庫")
    public ApiResponse<StoreDto> getMainStore() {

        Store store = storeRepository.findByMainTrue()
                .orElseThrow(() -> BusinessException.notFound("主倉庫/總部", "main=true"));

        return ApiResponse.success(convertToDto(store));
    }

    // ==================== DTO 轉換 ====================

    private StoreDto convertToDto(Store store) {
        return StoreDto.builder()
                .id(store.getId())
                .code(store.getCode())
                .name(store.getName())
                .type(store.getType())
                .typeName(store.getType() == Store.StoreType.STORE ? "門市" : "倉庫")
                .address(store.getAddress())
                .phone(store.getPhone())
                .email(store.getEmail())
                .businessHours(store.getBusinessHours())
                .main(store.isMain())
                .active(store.isActive())
                .sortOrder(store.getSortOrder())
                .notes(store.getNotes())
                .createdAt(store.getCreatedAt())
                .updatedAt(store.getUpdatedAt())
                .build();
    }

    // ==================== 請求/回應 DTO ====================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "門市/倉庫資料")
    public static class StoreDto {
        @Schema(description = "ID")
        private Long id;

        @Schema(description = "代碼")
        private String code;

        @Schema(description = "名稱")
        private String name;

        @Schema(description = "類型")
        private Store.StoreType type;

        @Schema(description = "類型名稱")
        private String typeName;

        @Schema(description = "地址")
        private String address;

        @Schema(description = "電話")
        private String phone;

        @Schema(description = "Email")
        private String email;

        @Schema(description = "營業時間")
        private String businessHours;

        @Schema(description = "是否為主倉庫/總部")
        private Boolean main;

        @Schema(description = "是否啟用")
        private Boolean active;

        @Schema(description = "排序順序")
        private Integer sortOrder;

        @Schema(description = "備註")
        private String notes;

        @Schema(description = "建立時間")
        private LocalDateTime createdAt;

        @Schema(description = "更新時間")
        private LocalDateTime updatedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "建立門市/倉庫請求")
    public static class CreateStoreRequest {
        @NotBlank(message = "代碼不可為空")
        @Size(max = 20, message = "代碼長度不可超過 20")
        @Schema(description = "代碼", example = "S001")
        private String code;

        @NotBlank(message = "名稱不可為空")
        @Size(max = 100, message = "名稱長度不可超過 100")
        @Schema(description = "名稱", example = "台北總店")
        private String name;

        @Schema(description = "類型", example = "STORE")
        private Store.StoreType type;

        @Schema(description = "地址")
        private String address;

        @Schema(description = "電話")
        private String phone;

        @Schema(description = "Email")
        private String email;

        @Schema(description = "營業時間")
        private String businessHours;

        @Schema(description = "是否為主倉庫/總部")
        private Boolean main;

        @Schema(description = "是否啟用")
        private Boolean active;

        @Schema(description = "排序順序")
        private Integer sortOrder;

        @Schema(description = "備註")
        private String notes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "更新門市/倉庫請求")
    public static class UpdateStoreRequest {
        @Size(max = 100, message = "名稱長度不可超過 100")
        @Schema(description = "名稱")
        private String name;

        @Schema(description = "類型")
        private Store.StoreType type;

        @Schema(description = "地址")
        private String address;

        @Schema(description = "電話")
        private String phone;

        @Schema(description = "Email")
        private String email;

        @Schema(description = "營業時間")
        private String businessHours;

        @Schema(description = "是否為主倉庫/總部")
        private Boolean main;

        @Schema(description = "是否啟用")
        private Boolean active;

        @Schema(description = "排序順序")
        private Integer sortOrder;

        @Schema(description = "備註")
        private String notes;
    }
}
