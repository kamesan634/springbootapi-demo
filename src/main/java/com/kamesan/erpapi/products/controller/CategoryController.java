package com.kamesan.erpapi.products.controller;

import com.kamesan.erpapi.common.dto.ApiResponse;
import com.kamesan.erpapi.common.dto.PageResponse;
import com.kamesan.erpapi.products.dto.CategoryDto;
import com.kamesan.erpapi.products.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品分類控制器
 *
 * <p>處理商品分類相關的 API 請求，包括：</p>
 * <ul>
 *   <li>POST /api/v1/categories - 建立分類</li>
 *   <li>GET /api/v1/categories - 查詢分類列表</li>
 *   <li>GET /api/v1/categories/{id} - 根據 ID 查詢分類</li>
 *   <li>GET /api/v1/categories/code/{code} - 根據代碼查詢分類</li>
 *   <li>PUT /api/v1/categories/{id} - 更新分類</li>
 *   <li>DELETE /api/v1/categories/{id} - 刪除分類</li>
 *   <li>GET /api/v1/categories/tree - 取得分類樹狀結構</li>
 *   <li>GET /api/v1/categories/{parentId}/children - 取得子分類</li>
 * </ul>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Tag(name = "商品分類管理", description = "商品分類 CRUD 操作及樹狀結構查詢")
public class CategoryController {

    /**
     * 分類服務
     */
    private final CategoryService categoryService;

    /**
     * 建立分類
     *
     * @param request 建立分類請求
     * @return 建立的分類資料
     */
    @PostMapping
    @Operation(summary = "建立分類", description = "建立新的商品分類，支援階層結構")
    public ApiResponse<CategoryDto> createCategory(
            @Valid @RequestBody CategoryDto.CreateCategoryRequest request) {

        log.info("收到建立分類請求: Code={}", request.getCode());

        CategoryDto category = categoryService.createCategory(request);

        return ApiResponse.success("分類建立成功", category);
    }

    /**
     * 查詢分類列表（分頁）
     *
     * @param page       頁碼（從 1 開始）
     * @param size       每頁筆數
     * @param sortBy     排序欄位
     * @param sortDir    排序方向
     * @param activeOnly 是否只查詢啟用的分類
     * @return 分類分頁
     */
    @GetMapping
    @Operation(summary = "查詢分類列表", description = "分頁查詢商品分類，支援排序")
    public ApiResponse<PageResponse<CategoryDto>> getCategories(
            @Parameter(description = "頁碼（從 1 開始）")
            @RequestParam(defaultValue = "1") int page,

            @Parameter(description = "每頁筆數")
            @RequestParam(defaultValue = "20") int size,

            @Parameter(description = "排序欄位")
            @RequestParam(defaultValue = "sortOrder") String sortBy,

            @Parameter(description = "排序方向（asc/desc）")
            @RequestParam(defaultValue = "asc") String sortDir,

            @Parameter(description = "是否只查詢啟用的分類")
            @RequestParam(defaultValue = "false") boolean activeOnly) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page - 1, size, sort);

        Page<CategoryDto> categories = activeOnly
                ? categoryService.getActiveCategories(pageable)
                : categoryService.getAllCategories(pageable);

        return ApiResponse.success(PageResponse.of(categories));
    }

    /**
     * 根據 ID 查詢分類
     *
     * @param id 分類 ID
     * @return 分類資料
     */
    @GetMapping("/{id}")
    @Operation(summary = "根據 ID 查詢分類", description = "根據分類 ID 查詢分類詳細資料")
    public ApiResponse<CategoryDto> getCategoryById(
            @Parameter(description = "分類 ID", required = true)
            @PathVariable Long id) {

        CategoryDto category = categoryService.getCategoryById(id);

        return ApiResponse.success(category);
    }

    /**
     * 根據代碼查詢分類
     *
     * @param code 分類代碼
     * @return 分類資料
     */
    @GetMapping("/code/{code}")
    @Operation(summary = "根據代碼查詢分類", description = "根據分類代碼查詢分類詳細資料")
    public ApiResponse<CategoryDto> getCategoryByCode(
            @Parameter(description = "分類代碼", required = true)
            @PathVariable String code) {

        CategoryDto category = categoryService.getCategoryByCode(code);

        return ApiResponse.success(category);
    }

    /**
     * 更新分類
     *
     * @param id      分類 ID
     * @param request 更新分類請求
     * @return 更新後的分類資料
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新分類", description = "更新分類資料，支援變更父分類")
    public ApiResponse<CategoryDto> updateCategory(
            @Parameter(description = "分類 ID", required = true)
            @PathVariable Long id,
            @Valid @RequestBody CategoryDto.UpdateCategoryRequest request) {

        log.info("收到更新分類請求: ID={}", id);

        CategoryDto category = categoryService.updateCategory(id, request);

        return ApiResponse.success("分類更新成功", category);
    }

    /**
     * 刪除分類
     *
     * @param id 分類 ID
     * @return 刪除結果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "刪除分類", description = "刪除分類，若有子分類或商品使用則無法刪除")
    public ApiResponse<Void> deleteCategory(
            @Parameter(description = "分類 ID", required = true)
            @PathVariable Long id) {

        log.info("收到刪除分類請求: ID={}", id);

        categoryService.deleteCategory(id);

        return ApiResponse.success("分類刪除成功", null);
    }

    /**
     * 取得分類樹狀結構
     *
     * @param includeInactive 是否包含停用的分類
     * @return 分類樹狀結構
     */
    @GetMapping("/tree")
    @Operation(summary = "取得分類樹狀結構", description = "取得完整的分類樹狀結構，從根分類開始")
    public ApiResponse<List<CategoryDto>> getCategoryTree(
            @Parameter(description = "是否包含停用的分類")
            @RequestParam(defaultValue = "false") boolean includeInactive) {

        List<CategoryDto> tree = includeInactive
                ? categoryService.getAllCategoryTree()
                : categoryService.getCategoryTree();

        return ApiResponse.success(tree);
    }

    /**
     * 取得子分類
     *
     * @param parentId 父分類 ID
     * @return 子分類列表
     */
    @GetMapping("/{parentId}/children")
    @Operation(summary = "取得子分類", description = "取得指定分類的直接子分類")
    public ApiResponse<List<CategoryDto>> getChildCategories(
            @Parameter(description = "父分類 ID", required = true)
            @PathVariable Long parentId) {

        List<CategoryDto> children = categoryService.getChildCategories(parentId);

        return ApiResponse.success(children);
    }

    /**
     * 搜尋分類
     *
     * @param keyword 關鍵字
     * @param page    頁碼
     * @param size    每頁筆數
     * @return 分類分頁
     */
    @GetMapping("/search")
    @Operation(summary = "搜尋分類", description = "根據關鍵字搜尋分類（名稱、代碼）")
    public ApiResponse<PageResponse<CategoryDto>> searchCategories(
            @Parameter(description = "搜尋關鍵字", required = true)
            @RequestParam String keyword,

            @Parameter(description = "頁碼（從 1 開始）")
            @RequestParam(defaultValue = "1") int page,

            @Parameter(description = "每頁筆數")
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("sortOrder").ascending());

        Page<CategoryDto> categories = categoryService.searchCategories(keyword, pageable);

        return ApiResponse.success(PageResponse.of(categories));
    }
}
