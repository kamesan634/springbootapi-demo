package com.kamesan.erpapi.inventory.controller;

import com.kamesan.erpapi.common.dto.ApiResponse;
import com.kamesan.erpapi.inventory.dto.AdjustInventoryRequest;
import com.kamesan.erpapi.inventory.dto.InventoryDto;
import com.kamesan.erpapi.inventory.dto.InventoryMovementDto;
import com.kamesan.erpapi.inventory.entity.MovementType;
import com.kamesan.erpapi.inventory.service.InventoryService;
import com.kamesan.erpapi.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 庫存控制器
 *
 * <p>處理庫存相關的 API 請求，包括：</p>
 * <ul>
 *   <li>GET /api/v1/inventory - 查詢特定商品和倉庫的庫存</li>
 *   <li>GET /api/v1/inventory/product/{productId} - 查詢商品在所有倉庫的庫存</li>
 *   <li>GET /api/v1/inventory/warehouse/{warehouseId} - 分頁查詢倉庫的所有庫存</li>
 *   <li>GET /api/v1/inventory/low-stock - 查詢低庫存預警</li>
 *   <li>POST /api/v1/inventory/adjust - 庫存調整（盤盈/盤虧）</li>
 *   <li>GET /api/v1/inventory/movements - 查詢庫存異動記錄</li>
 *   <li>GET /api/v1/inventory/movements/search - 複合條件查詢異動記錄</li>
 * </ul>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
@Tag(name = "庫存管理", description = "庫存查詢、調整、異動記錄")
public class InventoryController {

    /**
     * 庫存服務
     */
    private final InventoryService inventoryService;

    // ==================== 庫存查詢 ====================

    /**
     * 分頁查詢所有庫存
     *
     * @param pageable 分頁參數
     * @return 庫存分頁
     */
    @GetMapping("/all")
    @Operation(summary = "查詢所有庫存", description = "分頁查詢所有庫存記錄")
    public ApiResponse<Page<InventoryDto>> getAllInventories(
            @PageableDefault(size = 20, sort = "productId", direction = Sort.Direction.ASC)
            Pageable pageable) {

        log.info("分頁查詢所有庫存");

        Page<InventoryDto> inventories = inventoryService.getAllInventories(pageable);
        return ApiResponse.success(inventories);
    }

    /**
     * 查詢特定商品和倉庫的庫存
     *
     * @param productId   商品 ID
     * @param warehouseId 倉庫 ID
     * @return 庫存資訊
     */
    @GetMapping
    @Operation(summary = "查詢庫存", description = "根據商品 ID 和倉庫 ID 查詢庫存")
    public ApiResponse<InventoryDto> getInventory(
            @Parameter(description = "商品 ID", required = true)
            @RequestParam Long productId,
            @Parameter(description = "倉庫 ID", required = true)
            @RequestParam Long warehouseId) {

        log.info("查詢庫存: productId={}, warehouseId={}", productId, warehouseId);

        InventoryDto inventory = inventoryService.getInventory(productId, warehouseId);
        return ApiResponse.success(inventory);
    }

    /**
     * 查詢商品在所有倉庫的庫存
     *
     * @param productId 商品 ID
     * @return 庫存列表
     */
    @GetMapping("/product/{productId}")
    @Operation(summary = "查詢商品所有庫存", description = "查詢商品在所有倉庫的庫存")
    public ApiResponse<List<InventoryDto>> getInventoriesByProduct(
            @Parameter(description = "商品 ID", required = true)
            @PathVariable Long productId) {

        log.info("查詢商品所有庫存: productId={}", productId);

        List<InventoryDto> inventories = inventoryService.getInventoriesByProduct(productId);
        return ApiResponse.success(inventories);
    }

    /**
     * 分頁查詢倉庫的所有庫存
     *
     * @param warehouseId 倉庫 ID
     * @param pageable    分頁參數
     * @return 庫存分頁
     */
    @GetMapping("/warehouse/{warehouseId}")
    @Operation(summary = "查詢倉庫庫存", description = "分頁查詢倉庫的所有庫存")
    public ApiResponse<Page<InventoryDto>> getInventoriesByWarehouse(
            @Parameter(description = "倉庫 ID", required = true)
            @PathVariable Long warehouseId,
            @PageableDefault(size = 20, sort = "productId", direction = Sort.Direction.ASC)
            Pageable pageable) {

        log.info("查詢倉庫庫存: warehouseId={}", warehouseId);

        Page<InventoryDto> inventories = inventoryService.getInventoriesByWarehouse(warehouseId, pageable);
        return ApiResponse.success(inventories);
    }

    /**
     * 查詢商品的總庫存數量
     *
     * @param productId 商品 ID
     * @return 總庫存數量
     */
    @GetMapping("/product/{productId}/total")
    @Operation(summary = "查詢商品總庫存", description = "查詢商品在所有倉庫的總庫存數量")
    public ApiResponse<Integer> getTotalQuantity(
            @Parameter(description = "商品 ID", required = true)
            @PathVariable Long productId) {

        log.info("查詢商品總庫存: productId={}", productId);

        Integer total = inventoryService.getTotalQuantity(productId);
        return ApiResponse.success(total);
    }

    /**
     * 查詢商品的可用庫存總數
     *
     * @param productId 商品 ID
     * @return 可用庫存總數
     */
    @GetMapping("/product/{productId}/available")
    @Operation(summary = "查詢商品可用庫存", description = "查詢商品在所有倉庫的可用庫存總數（扣除預留）")
    public ApiResponse<Integer> getAvailableQuantity(
            @Parameter(description = "商品 ID", required = true)
            @PathVariable Long productId) {

        log.info("查詢商品可用庫存: productId={}", productId);

        Integer available = inventoryService.getTotalAvailableQuantity(productId);
        return ApiResponse.success(available);
    }

    /**
     * 查詢低於安全庫存的記錄
     *
     * @param threshold 安全庫存閾值
     * @param pageable  分頁參數
     * @return 低庫存記錄分頁
     */
    @GetMapping("/low-stock")
    @Operation(summary = "低庫存預警", description = "查詢低於安全庫存閾值的記錄")
    public ApiResponse<Page<InventoryDto>> getLowStockInventories(
            @Parameter(description = "安全庫存閾值", required = true)
            @RequestParam Integer threshold,
            @PageableDefault(size = 20, sort = "quantity", direction = Sort.Direction.ASC)
            Pageable pageable) {

        log.info("查詢低庫存預警: threshold={}", threshold);

        Page<InventoryDto> inventories = inventoryService.getLowStockInventories(threshold, pageable);
        return ApiResponse.success(inventories);
    }

    // ==================== 庫存調整 ====================

    /**
     * 庫存調整（盤盈/盤虧）
     *
     * @param request       調整請求
     * @param userPrincipal 當前登入使用者
     * @return 調整後的庫存資訊
     */
    @PostMapping("/adjust")
    @Operation(summary = "庫存調整", description = "執行庫存盤盈/盤虧調整")
    public ApiResponse<InventoryDto> adjustInventory(
            @Valid @RequestBody AdjustInventoryRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        log.info("庫存調整請求: productId={}, warehouseId={}, type={}, quantity={}, operator={}",
                request.getProductId(), request.getWarehouseId(),
                request.getAdjustmentType(), request.getQuantity(),
                userPrincipal.getId());

        InventoryDto inventory = inventoryService.adjustInventory(request, userPrincipal.getId());
        return ApiResponse.success("庫存調整成功", inventory);
    }

    // ==================== 異動記錄查詢 ====================

    /**
     * 查詢商品的庫存異動記錄
     *
     * @param productId 商品 ID
     * @param pageable  分頁參數
     * @return 異動記錄分頁
     */
    @GetMapping("/movements/product/{productId}")
    @Operation(summary = "查詢商品異動記錄", description = "查詢特定商品的庫存異動記錄")
    public ApiResponse<Page<InventoryMovementDto>> getMovementsByProduct(
            @Parameter(description = "商品 ID", required = true)
            @PathVariable Long productId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        log.info("查詢商品異動記錄: productId={}", productId);

        Page<InventoryMovementDto> movements = inventoryService.getMovementsByProduct(productId, pageable);
        return ApiResponse.success(movements);
    }

    /**
     * 查詢倉庫的庫存異動記錄
     *
     * @param warehouseId 倉庫 ID
     * @param pageable    分頁參數
     * @return 異動記錄分頁
     */
    @GetMapping("/movements/warehouse/{warehouseId}")
    @Operation(summary = "查詢倉庫異動記錄", description = "查詢特定倉庫的庫存異動記錄")
    public ApiResponse<Page<InventoryMovementDto>> getMovementsByWarehouse(
            @Parameter(description = "倉庫 ID", required = true)
            @PathVariable Long warehouseId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        log.info("查詢倉庫異動記錄: warehouseId={}", warehouseId);

        Page<InventoryMovementDto> movements = inventoryService.getMovementsByWarehouse(warehouseId, pageable);
        return ApiResponse.success(movements);
    }

    /**
     * 查詢商品在特定倉庫的異動記錄
     *
     * @param productId   商品 ID
     * @param warehouseId 倉庫 ID
     * @param pageable    分頁參數
     * @return 異動記錄分頁
     */
    @GetMapping("/movements")
    @Operation(summary = "查詢庫存異動記錄", description = "根據商品和倉庫查詢異動記錄")
    public ApiResponse<Page<InventoryMovementDto>> getMovements(
            @Parameter(description = "商品 ID", required = true)
            @RequestParam Long productId,
            @Parameter(description = "倉庫 ID", required = true)
            @RequestParam Long warehouseId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        log.info("查詢庫存異動記錄: productId={}, warehouseId={}", productId, warehouseId);

        Page<InventoryMovementDto> movements = inventoryService
                .getMovementsByProductAndWarehouse(productId, warehouseId, pageable);
        return ApiResponse.success(movements);
    }

    /**
     * 根據參考單號查詢異動記錄
     *
     * @param referenceNo 參考單號
     * @return 異動記錄列表
     */
    @GetMapping("/movements/reference/{referenceNo}")
    @Operation(summary = "根據單號查詢異動", description = "根據參考單號（如採購單號、銷售單號）查詢異動記錄")
    public ApiResponse<List<InventoryMovementDto>> getMovementsByReferenceNo(
            @Parameter(description = "參考單號", required = true)
            @PathVariable String referenceNo) {

        log.info("根據參考單號查詢異動記錄: referenceNo={}", referenceNo);

        List<InventoryMovementDto> movements = inventoryService.getMovementsByReferenceNo(referenceNo);
        return ApiResponse.success(movements);
    }

    /**
     * 複合條件查詢異動記錄
     *
     * @param productId    商品 ID（可選）
     * @param warehouseId  倉庫 ID（可選）
     * @param movementType 異動類型（可選）
     * @param startDate    開始日期
     * @param endDate      結束日期
     * @param pageable     分頁參數
     * @return 異動記錄分頁
     */
    @GetMapping("/movements/search")
    @Operation(summary = "搜尋異動記錄", description = "複合條件查詢庫存異動記錄")
    public ApiResponse<Page<InventoryMovementDto>> searchMovements(
            @Parameter(description = "商品 ID（可選）")
            @RequestParam(required = false) Long productId,
            @Parameter(description = "倉庫 ID（可選）")
            @RequestParam(required = false) Long warehouseId,
            @Parameter(description = "異動類型（可選）")
            @RequestParam(required = false) MovementType movementType,
            @Parameter(description = "開始日期", example = "2024-01-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "結束日期", example = "2024-01-31")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        log.info("搜尋異動記錄: productId={}, warehouseId={}, type={}, startDate={}, endDate={}",
                productId, warehouseId, movementType, startDate, endDate);

        Page<InventoryMovementDto> movements = inventoryService.searchMovements(
                productId, warehouseId, movementType, startDate, endDate, pageable);
        return ApiResponse.success(movements);
    }

    /**
     * 取得所有異動類型
     *
     * <p>供前端下拉選單使用</p>
     *
     * @return 異動類型列表
     */
    @GetMapping("/movement-types")
    @Operation(summary = "取得異動類型列表", description = "取得所有庫存異動類型，供下拉選單使用")
    public ApiResponse<MovementType[]> getMovementTypes() {
        return ApiResponse.success(MovementType.values());
    }
}
