package com.kamesan.erpapi.purchasing.controller;

import com.kamesan.erpapi.common.dto.ApiResponse;
import com.kamesan.erpapi.purchasing.dto.CreateSupplierRequest;
import com.kamesan.erpapi.purchasing.dto.SupplierDto;
import com.kamesan.erpapi.purchasing.dto.UpdateSupplierRequest;
import com.kamesan.erpapi.purchasing.service.SupplierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 供應商控制器
 *
 * <p>處理供應商相關的 HTTP 請求，提供 RESTful API 端點。</p>
 *
 * <h2>API 端點清單</h2>
 * <table border="1">
 *   <tr><th>方法</th><th>路徑</th><th>說明</th></tr>
 *   <tr><td>POST</td><td>/api/v1/suppliers</td><td>新增供應商</td></tr>
 *   <tr><td>GET</td><td>/api/v1/suppliers</td><td>查詢所有供應商（分頁）</td></tr>
 *   <tr><td>GET</td><td>/api/v1/suppliers/{id}</td><td>根據 ID 查詢供應商</td></tr>
 *   <tr><td>GET</td><td>/api/v1/suppliers/code/{code}</td><td>根據代碼查詢供應商</td></tr>
 *   <tr><td>GET</td><td>/api/v1/suppliers/active</td><td>查詢啟用中的供應商</td></tr>
 *   <tr><td>GET</td><td>/api/v1/suppliers/search</td><td>搜尋供應商</td></tr>
 *   <tr><td>PUT</td><td>/api/v1/suppliers/{id}</td><td>更新供應商</td></tr>
 *   <tr><td>DELETE</td><td>/api/v1/suppliers/{id}</td><td>刪除供應商</td></tr>
 *   <tr><td>PATCH</td><td>/api/v1/suppliers/{id}/toggle-status</td><td>切換啟用狀態</td></tr>
 *   <tr><td>PATCH</td><td>/api/v1/suppliers/{id}/activate</td><td>啟用供應商</td></tr>
 *   <tr><td>PATCH</td><td>/api/v1/suppliers/{id}/deactivate</td><td>停用供應商</td></tr>
 * </table>
 *
 * <h2>回應格式</h2>
 * <p>所有 API 回應都使用 {@link ApiResponse} 統一格式。</p>
 *
 * @author ERP System Team
 * @version 1.0.0
 * @see SupplierService
 * @see SupplierDto
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/suppliers")
@RequiredArgsConstructor
@Tag(name = "供應商管理", description = "供應商的新增、查詢、更新、刪除等操作")
public class SupplierController {

    /**
     * 供應商服務
     *
     * <p>處理供應商相關的業務邏輯。</p>
     */
    private final SupplierService supplierService;

    /**
     * 新增供應商
     *
     * <p>建立新的供應商資料。</p>
     *
     * <p>請求範例：</p>
     * <pre>
     * POST /api/v1/suppliers
     * {
     *   "code": "SUP001",
     *   "name": "優質材料供應有限公司",
     *   "contactPerson": "王小明",
     *   "phone": "02-12345678",
     *   "email": "contact@supplier.com",
     *   "address": "台北市中山區民生東路100號",
     *   "paymentTerms": "月結30天",
     *   "isActive": true,
     *   "notes": "優質供應商"
     * }
     * </pre>
     *
     * @param request 新增供應商請求，包含供應商基本資訊
     * @return 新增成功的供應商資訊
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "新增供應商", description = "建立新的供應商資料")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "新增成功",
                    content = @Content(schema = @Schema(implementation = SupplierDto.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "請求參數錯誤"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "供應商代碼或名稱已存在")
    })
    public ApiResponse<SupplierDto> createSupplier(
            @Valid @RequestBody CreateSupplierRequest request) {
        log.info("收到新增供應商請求，代碼: {}, 名稱: {}", request.getCode(), request.getName());

        SupplierDto supplier = supplierService.createSupplier(request);

        return ApiResponse.success("供應商新增成功", supplier);
    }

    /**
     * 查詢所有供應商（分頁）
     *
     * <p>回傳所有供應商資料，支援分頁、排序和狀態篩選。</p>
     *
     * <p>請求範例：</p>
     * <pre>
     * GET /api/v1/suppliers?page=0&size=10&sort=name,asc
     * GET /api/v1/suppliers?page=0&size=10&isActive=true
     * </pre>
     *
     * @param isActive 啟用狀態篩選（選填，null 表示全部）
     * @param pageable 分頁參數（page: 頁碼, size: 每頁筆數, sort: 排序）
     * @return 供應商分頁結果
     */
    @GetMapping
    @Operation(summary = "查詢所有供應商", description = "取得供應商列表，支援分頁、排序和狀態篩選")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "查詢成功")
    })
    public ApiResponse<Page<SupplierDto>> getAllSuppliers(
            @Parameter(description = "啟用狀態篩選（true: 啟用, false: 停用, 不填: 全部）")
            @RequestParam(required = false) Boolean isActive,
            @Parameter(description = "分頁參數")
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        log.debug("查詢所有供應商，isActive: {}, 分頁: {}", isActive, pageable);

        Page<SupplierDto> suppliers;
        if (isActive != null) {
            suppliers = supplierService.getSuppliersByStatus(isActive, pageable);
        } else {
            suppliers = supplierService.getAllSuppliers(pageable);
        }

        return ApiResponse.success(suppliers);
    }

    /**
     * 根據 ID 查詢供應商
     *
     * <p>請求範例：</p>
     * <pre>
     * GET /api/v1/suppliers/1
     * </pre>
     *
     * @param id 供應商 ID
     * @return 供應商資訊
     */
    @GetMapping("/{id}")
    @Operation(summary = "根據 ID 查詢供應商", description = "取得指定 ID 的供應商詳細資訊")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "查詢成功",
                    content = @Content(schema = @Schema(implementation = SupplierDto.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "供應商不存在")
    })
    public ApiResponse<SupplierDto> getSupplierById(
            @Parameter(description = "供應商 ID", required = true, example = "1")
            @PathVariable Long id) {
        log.debug("根據 ID 查詢供應商，ID: {}", id);

        SupplierDto supplier = supplierService.getSupplierById(id);

        return ApiResponse.success(supplier);
    }

    /**
     * 根據代碼查詢供應商
     *
     * <p>請求範例：</p>
     * <pre>
     * GET /api/v1/suppliers/code/SUP001
     * </pre>
     *
     * @param code 供應商代碼
     * @return 供應商資訊
     */
    @GetMapping("/code/{code}")
    @Operation(summary = "根據代碼查詢供應商", description = "取得指定代碼的供應商詳細資訊")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "查詢成功",
                    content = @Content(schema = @Schema(implementation = SupplierDto.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "供應商不存在")
    })
    public ApiResponse<SupplierDto> getSupplierByCode(
            @Parameter(description = "供應商代碼", required = true, example = "SUP001")
            @PathVariable String code) {
        log.debug("根據代碼查詢供應商，代碼: {}", code);

        SupplierDto supplier = supplierService.getSupplierByCode(code);

        return ApiResponse.success(supplier);
    }

    /**
     * 查詢啟用中的供應商（不分頁）
     *
     * <p>回傳所有啟用中的供應商，適用於下拉選單等場景。</p>
     *
     * <p>請求範例：</p>
     * <pre>
     * GET /api/v1/suppliers/active
     * </pre>
     *
     * @return 啟用中的供應商清單
     */
    @GetMapping("/active")
    @Operation(summary = "查詢啟用中的供應商", description = "取得所有啟用中的供應商列表，適用於下拉選單")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "查詢成功")
    })
    public ApiResponse<List<SupplierDto>> getActiveSuppliers() {
        log.debug("查詢啟用中的供應商");

        List<SupplierDto> suppliers = supplierService.getActiveSuppliers();

        return ApiResponse.success(suppliers);
    }

    /**
     * 搜尋供應商
     *
     * <p>根據關鍵字搜尋供應商，會比對代碼、名稱、聯絡人、電子郵件、電話等欄位。</p>
     *
     * <p>請求範例：</p>
     * <pre>
     * GET /api/v1/suppliers/search?keyword=優質&page=0&size=10
     * GET /api/v1/suppliers/search?keyword=王小明&activeOnly=true
     * </pre>
     *
     * @param keyword    搜尋關鍵字
     * @param activeOnly 是否只搜尋啟用中的供應商
     * @param pageable   分頁參數
     * @return 符合條件的供應商分頁結果
     */
    @GetMapping("/search")
    @Operation(summary = "搜尋供應商", description = "根據關鍵字搜尋供應商（比對代碼、名稱、聯絡人、電子郵件、電話）")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "搜尋成功")
    })
    public ApiResponse<Page<SupplierDto>> searchSuppliers(
            @Parameter(description = "搜尋關鍵字", required = true, example = "優質")
            @RequestParam String keyword,
            @Parameter(description = "是否只搜尋啟用中的供應商")
            @RequestParam(defaultValue = "false") Boolean activeOnly,
            @Parameter(description = "分頁參數")
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        log.debug("搜尋供應商，關鍵字: {}, 只搜尋啟用: {}, 分頁: {}", keyword, activeOnly, pageable);

        Page<SupplierDto> suppliers;
        if (activeOnly) {
            suppliers = supplierService.searchActiveSuppliers(keyword, pageable);
        } else {
            suppliers = supplierService.searchSuppliers(keyword, pageable);
        }

        return ApiResponse.success(suppliers);
    }

    /**
     * 更新供應商
     *
     * <p>更新指定供應商的資料。</p>
     *
     * <p>請求範例：</p>
     * <pre>
     * PUT /api/v1/suppliers/1
     * {
     *   "code": "SUP001",
     *   "name": "優質材料供應有限公司（更新）",
     *   "contactPerson": "王小明",
     *   "phone": "02-12345678",
     *   "email": "new@supplier.com",
     *   "address": "台北市中山區民生東路200號",
     *   "paymentTerms": "月結60天",
     *   "isActive": true,
     *   "notes": "更新備註"
     * }
     * </pre>
     *
     * @param id      供應商 ID
     * @param request 更新供應商請求
     * @return 更新後的供應商資訊
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新供應商", description = "更新指定 ID 的供應商資料")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "更新成功",
                    content = @Content(schema = @Schema(implementation = SupplierDto.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "請求參數錯誤"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "供應商不存在"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "供應商代碼或名稱已被其他供應商使用")
    })
    public ApiResponse<SupplierDto> updateSupplier(
            @Parameter(description = "供應商 ID", required = true, example = "1")
            @PathVariable Long id,
            @Valid @RequestBody UpdateSupplierRequest request) {
        log.info("收到更新供應商請求，ID: {}", id);

        SupplierDto supplier = supplierService.updateSupplier(id, request);

        return ApiResponse.success("供應商更新成功", supplier);
    }

    /**
     * 刪除供應商
     *
     * <p>刪除指定的供應商資料。此操作為永久刪除，無法復原。</p>
     *
     * <p>請求範例：</p>
     * <pre>
     * DELETE /api/v1/suppliers/1
     * </pre>
     *
     * @param id 供應商 ID
     * @return 刪除結果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "刪除供應商", description = "刪除指定 ID 的供應商（永久刪除）")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "刪除成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "供應商不存在")
    })
    public ApiResponse<Void> deleteSupplier(
            @Parameter(description = "供應商 ID", required = true, example = "1")
            @PathVariable Long id) {
        log.info("收到刪除供應商請求，ID: {}", id);

        supplierService.deleteSupplier(id);

        return ApiResponse.success("供應商刪除成功", null);
    }

    /**
     * 切換供應商啟用狀態
     *
     * <p>將供應商的啟用狀態反轉（啟用 <-> 停用）。</p>
     *
     * <p>請求範例：</p>
     * <pre>
     * PATCH /api/v1/suppliers/1/toggle-status
     * </pre>
     *
     * @param id 供應商 ID
     * @return 更新後的供應商資訊
     */
    @PatchMapping("/{id}/toggle-status")
    @Operation(summary = "切換供應商啟用狀態", description = "將供應商的啟用狀態反轉（啟用 <-> 停用）")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "狀態切換成功",
                    content = @Content(schema = @Schema(implementation = SupplierDto.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "供應商不存在")
    })
    public ApiResponse<SupplierDto> toggleSupplierStatus(
            @Parameter(description = "供應商 ID", required = true, example = "1")
            @PathVariable Long id) {
        log.info("收到切換供應商狀態請求，ID: {}", id);

        SupplierDto supplier = supplierService.toggleSupplierStatus(id);

        String statusText = supplier.getIsActive() ? "啟用" : "停用";
        return ApiResponse.success("供應商已" + statusText, supplier);
    }

    /**
     * 啟用供應商
     *
     * <p>將指定的供應商設為啟用狀態。</p>
     *
     * <p>請求範例：</p>
     * <pre>
     * PATCH /api/v1/suppliers/1/activate
     * </pre>
     *
     * @param id 供應商 ID
     * @return 更新後的供應商資訊
     */
    @PatchMapping("/{id}/activate")
    @Operation(summary = "啟用供應商", description = "將指定的供應商設為啟用狀態")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "啟用成功",
                    content = @Content(schema = @Schema(implementation = SupplierDto.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "供應商不存在")
    })
    public ApiResponse<SupplierDto> activateSupplier(
            @Parameter(description = "供應商 ID", required = true, example = "1")
            @PathVariable Long id) {
        log.info("收到啟用供應商請求，ID: {}", id);

        SupplierDto supplier = supplierService.activateSupplier(id);

        return ApiResponse.success("供應商已啟用", supplier);
    }

    /**
     * 停用供應商
     *
     * <p>將指定的供應商設為停用狀態。停用後無法用於新的採購訂單。</p>
     *
     * <p>請求範例：</p>
     * <pre>
     * PATCH /api/v1/suppliers/1/deactivate
     * </pre>
     *
     * @param id 供應商 ID
     * @return 更新後的供應商資訊
     */
    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "停用供應商", description = "將指定的供應商設為停用狀態")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "停用成功",
                    content = @Content(schema = @Schema(implementation = SupplierDto.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "供應商不存在")
    })
    public ApiResponse<SupplierDto> deactivateSupplier(
            @Parameter(description = "供應商 ID", required = true, example = "1")
            @PathVariable Long id) {
        log.info("收到停用供應商請求，ID: {}", id);

        SupplierDto supplier = supplierService.deactivateSupplier(id);

        return ApiResponse.success("供應商已停用", supplier);
    }

    /**
     * 統計供應商數量
     *
     * <p>回傳供應商的統計資訊。</p>
     *
     * <p>請求範例：</p>
     * <pre>
     * GET /api/v1/suppliers/stats/count
     * </pre>
     *
     * @return 供應商數量統計
     */
    @GetMapping("/stats/count")
    @Operation(summary = "統計供應商數量", description = "取得供應商的數量統計")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "查詢成功")
    })
    public ApiResponse<SupplierCountStats> getSupplierCountStats() {
        log.debug("查詢供應商數量統計");

        long total = supplierService.countSuppliers();
        long active = supplierService.countActiveSuppliers();

        SupplierCountStats stats = new SupplierCountStats(total, active, total - active);

        return ApiResponse.success(stats);
    }

    /**
     * 供應商數量統計 DTO
     *
     * <p>用於回傳供應商數量統計資訊。</p>
     *
     * @param total    供應商總數
     * @param active   啟用中的供應商數量
     * @param inactive 停用的供應商數量
     */
    @Schema(description = "供應商數量統計")
    public record SupplierCountStats(
            @Schema(description = "供應商總數", example = "100")
            long total,
            @Schema(description = "啟用中的供應商數量", example = "85")
            long active,
            @Schema(description = "停用的供應商數量", example = "15")
            long inactive
    ) {
    }
}
