package com.kamesan.erpapi.products.controller;

import com.kamesan.erpapi.common.dto.ApiResponse;
import com.kamesan.erpapi.common.dto.PageResponse;
import com.kamesan.erpapi.products.dto.CreateProductRequest;
import com.kamesan.erpapi.products.dto.ProductDto;
import com.kamesan.erpapi.products.dto.UpdateProductRequest;
import com.kamesan.erpapi.products.entity.Product;
import com.kamesan.erpapi.products.repository.ProductRepository;
import com.kamesan.erpapi.products.service.ProductImportExportService;
import com.kamesan.erpapi.products.service.ProductService;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 商品控制器
 *
 * <p>處理商品相關的 API 請求，包括：</p>
 * <ul>
 *   <li>POST /api/v1/products - 建立商品</li>
 *   <li>GET /api/v1/products - 查詢商品列表</li>
 *   <li>GET /api/v1/products/{id} - 根據 ID 查詢商品</li>
 *   <li>GET /api/v1/products/sku/{sku} - 根據貨號查詢商品</li>
 *   <li>GET /api/v1/products/barcode/{barcode} - 根據條碼查詢商品</li>
 *   <li>PUT /api/v1/products/{id} - 更新商品</li>
 *   <li>DELETE /api/v1/products/{id} - 刪除商品</li>
 *   <li>GET /api/v1/products/search - 搜尋商品</li>
 *   <li>GET /api/v1/products/category/{categoryId} - 根據分類查詢商品</li>
 * </ul>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "商品管理", description = "商品 CRUD 操作及查詢")
public class ProductController {

    /**
     * 商品服務
     */
    private final ProductService productService;
    private final ProductImportExportService importExportService;
    private final ProductRepository productRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * 建立商品
     *
     * @param request 建立商品請求
     * @return 建立的商品資料
     */
    @PostMapping
    @Operation(summary = "建立商品", description = "建立新商品，包含基本資訊、分類、單位、稅別、條碼等")
    public ApiResponse<ProductDto> createProduct(
            @Valid @RequestBody CreateProductRequest request) {

        log.info("收到建立商品請求: SKU={}", request.getSku());

        ProductDto product = productService.createProduct(request);

        return ApiResponse.success("商品建立成功", product);
    }

    /**
     * 查詢商品列表（分頁）
     *
     * @param page       頁碼（從 1 開始）
     * @param size       每頁筆數
     * @param sortBy     排序欄位
     * @param sortDir    排序方向
     * @param activeOnly 是否只查詢啟用的商品
     * @return 商品分頁
     */
    @GetMapping
    @Operation(summary = "查詢商品列表", description = "分頁查詢商品，支援排序")
    public ApiResponse<PageResponse<ProductDto>> getProducts(
            @Parameter(description = "頁碼（從 1 開始）")
            @RequestParam(defaultValue = "1") int page,

            @Parameter(description = "每頁筆數")
            @RequestParam(defaultValue = "20") int size,

            @Parameter(description = "排序欄位")
            @RequestParam(defaultValue = "createdAt") String sortBy,

            @Parameter(description = "排序方向（asc/desc）")
            @RequestParam(defaultValue = "desc") String sortDir,

            @Parameter(description = "是否只查詢啟用的商品")
            @RequestParam(defaultValue = "false") boolean activeOnly) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(Math.max(0, page - 1), size, sort);

        Page<ProductDto> products = activeOnly
                ? productService.getActiveProducts(pageable)
                : productService.getAllProducts(pageable);

        return ApiResponse.success(PageResponse.of(products));
    }

    /**
     * 根據 ID 查詢商品
     *
     * @param id 商品 ID
     * @return 商品資料
     */
    @GetMapping("/{id}")
    @Operation(summary = "根據 ID 查詢商品", description = "根據商品 ID 查詢商品詳細資料")
    public ApiResponse<ProductDto> getProductById(
            @Parameter(description = "商品 ID", required = true)
            @PathVariable Long id) {

        ProductDto product = productService.getProductById(id);

        return ApiResponse.success(product);
    }

    /**
     * 根據貨號查詢商品
     *
     * @param sku 商品貨號
     * @return 商品資料
     */
    @GetMapping("/sku/{sku}")
    @Operation(summary = "根據貨號查詢商品", description = "根據商品貨號 (SKU) 查詢商品詳細資料")
    public ApiResponse<ProductDto> getProductBySku(
            @Parameter(description = "商品貨號", required = true)
            @PathVariable String sku) {

        ProductDto product = productService.getProductBySku(sku);

        return ApiResponse.success(product);
    }

    /**
     * 根據條碼查詢商品
     *
     * @param barcode 條碼
     * @return 商品資料
     */
    @GetMapping("/barcode/{barcode}")
    @Operation(summary = "根據條碼查詢商品", description = "根據條碼查詢商品，支援主條碼和附加條碼")
    public ApiResponse<ProductDto> getProductByBarcode(
            @Parameter(description = "條碼", required = true)
            @PathVariable String barcode) {

        ProductDto product = productService.getProductByBarcode(barcode);

        return ApiResponse.success(product);
    }

    /**
     * 更新商品
     *
     * @param id      商品 ID
     * @param request 更新商品請求
     * @return 更新後的商品資料
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新商品", description = "更新商品資料，包含基本資訊、分類、單位、稅別、條碼等")
    public ApiResponse<ProductDto> updateProduct(
            @Parameter(description = "商品 ID", required = true)
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductRequest request) {

        log.info("收到更新商品請求: ID={}", id);

        ProductDto product = productService.updateProduct(id, request);

        return ApiResponse.success("商品更新成功", product);
    }

    /**
     * 刪除商品（軟刪除）
     *
     * @param id 商品 ID
     * @return 刪除結果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "刪除商品", description = "軟刪除商品（將商品設為停用狀態）")
    public ApiResponse<Void> deleteProduct(
            @Parameter(description = "商品 ID", required = true)
            @PathVariable Long id) {

        log.info("收到刪除商品請求: ID={}", id);

        productService.deleteProduct(id);

        return ApiResponse.success("商品刪除成功", null);
    }

    /**
     * 永久刪除商品
     *
     * @param id 商品 ID
     * @return 刪除結果
     */
    @DeleteMapping("/{id}/permanent")
    @Operation(summary = "永久刪除商品", description = "永久刪除商品及其所有條碼，此操作不可復原")
    public ApiResponse<Void> hardDeleteProduct(
            @Parameter(description = "商品 ID", required = true)
            @PathVariable Long id) {

        log.warn("收到永久刪除商品請求: ID={}", id);

        productService.hardDeleteProduct(id);

        return ApiResponse.success("商品已永久刪除", null);
    }

    /**
     * 搜尋商品
     *
     * @param keyword 關鍵字
     * @param page    頁碼
     * @param size    每頁筆數
     * @return 商品分頁
     */
    @GetMapping("/search")
    @Operation(summary = "搜尋商品", description = "根據關鍵字搜尋商品（名稱、貨號、條碼）")
    public ApiResponse<PageResponse<ProductDto>> searchProducts(
            @Parameter(description = "搜尋關鍵字", required = true)
            @RequestParam String keyword,

            @Parameter(description = "頁碼（從 1 開始）")
            @RequestParam(defaultValue = "1") int page,

            @Parameter(description = "每頁筆數")
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(Math.max(0, page - 1), size, Sort.by("name").ascending());

        Page<ProductDto> products = productService.searchProducts(keyword, pageable);

        return ApiResponse.success(PageResponse.of(products));
    }

    /**
     * 根據分類查詢商品
     *
     * @param categoryId 分類 ID
     * @param page       頁碼
     * @param size       每頁筆數
     * @return 商品分頁
     */
    @GetMapping("/category/{categoryId}")
    @Operation(summary = "根據分類查詢商品", description = "查詢指定分類下的商品")
    public ApiResponse<PageResponse<ProductDto>> getProductsByCategory(
            @Parameter(description = "分類 ID", required = true)
            @PathVariable Long categoryId,

            @Parameter(description = "頁碼（從 1 開始）")
            @RequestParam(defaultValue = "1") int page,

            @Parameter(description = "每頁筆數")
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(Math.max(0, page - 1), size, Sort.by("name").ascending());

        Page<ProductDto> products = productService.getProductsByCategory(categoryId, pageable);

        return ApiResponse.success(PageResponse.of(products));
    }

    // ==================== 匯入匯出 API ====================

    /**
     * 匯出商品為 Excel
     */
    @GetMapping("/export/excel")
    @Operation(summary = "匯出商品 Excel", description = "將商品資料匯出為 Excel 格式")
    public ResponseEntity<byte[]> exportProductsToExcel(
            @Parameter(description = "分類 ID（可選）") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "是否只匯出啟用商品") @RequestParam(defaultValue = "true") boolean activeOnly)
            throws IOException {

        List<Product> products = getProductsForExport(categoryId, activeOnly);
        byte[] content = importExportService.exportProductsToExcel(products);

        String filename = "products_" + LocalDate.now().format(DATE_FORMATTER) + ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .contentLength(content.length)
                .body(content);
    }

    /**
     * 匯出商品為 CSV
     */
    @GetMapping("/export/csv")
    @Operation(summary = "匯出商品 CSV", description = "將商品資料匯出為 CSV 格式")
    public ResponseEntity<byte[]> exportProductsToCsv(
            @Parameter(description = "分類 ID（可選）") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "是否只匯出啟用商品") @RequestParam(defaultValue = "true") boolean activeOnly)
            throws IOException {

        List<Product> products = getProductsForExport(categoryId, activeOnly);
        byte[] content = importExportService.exportProductsToCsv(products);

        String filename = "products_" + LocalDate.now().format(DATE_FORMATTER) + ".csv";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .contentLength(content.length)
                .body(content);
    }

    /**
     * 下載匯入範本
     */
    @GetMapping("/import/template")
    @Operation(summary = "下載匯入範本", description = "下載商品匯入的 Excel 範本")
    public ResponseEntity<byte[]> downloadImportTemplate() throws IOException {
        byte[] content = importExportService.generateImportTemplate();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"product_import_template.xlsx\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .contentLength(content.length)
                .body(content);
    }

    /**
     * 從 Excel 匯入商品
     */
    @PostMapping("/import/excel")
    @Operation(summary = "從 Excel 匯入商品", description = "從 Excel 檔案匯入商品資料")
    public ApiResponse<ProductImportExportService.ImportResult> importProductsFromExcel(
            @Parameter(description = "Excel 檔案") @RequestParam("file") MultipartFile file)
            throws IOException {

        if (file.isEmpty()) {
            return ApiResponse.error(400, "請上傳檔案", null);
        }

        ProductImportExportService.ImportResult result = importExportService.importProductsFromExcel(file);

        String message = String.format("匯入完成：成功 %d 筆，失敗 %d 筆",
                result.successCount(), result.getErrorCount());

        return ApiResponse.success(message, result);
    }

    /**
     * 從 CSV 匯入商品
     */
    @PostMapping("/import/csv")
    @Operation(summary = "從 CSV 匯入商品", description = "從 CSV 檔案匯入商品資料")
    public ApiResponse<ProductImportExportService.ImportResult> importProductsFromCsv(
            @Parameter(description = "CSV 檔案") @RequestParam("file") MultipartFile file)
            throws Exception {

        if (file.isEmpty()) {
            return ApiResponse.error(400, "請上傳檔案", null);
        }

        ProductImportExportService.ImportResult result = importExportService.importProductsFromCsv(file);

        String message = String.format("匯入完成：成功 %d 筆，失敗 %d 筆",
                result.successCount(), result.getErrorCount());

        return ApiResponse.success(message, result);
    }

    private List<Product> getProductsForExport(Long categoryId, boolean activeOnly) {
        if (categoryId != null) {
            if (activeOnly) {
                return productRepository.findByCategoryIdAndActiveTrue(categoryId);
            } else {
                return productRepository.findByCategoryId(categoryId);
            }
        } else {
            if (activeOnly) {
                return productRepository.findByActiveTrue();
            } else {
                return productRepository.findAll();
            }
        }
    }
}
