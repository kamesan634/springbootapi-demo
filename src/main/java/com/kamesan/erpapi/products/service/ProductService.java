package com.kamesan.erpapi.products.service;

import com.kamesan.erpapi.common.exception.BusinessException;
import com.kamesan.erpapi.products.dto.CreateProductRequest;
import com.kamesan.erpapi.products.dto.ProductDto;
import com.kamesan.erpapi.products.dto.UpdateProductRequest;
import com.kamesan.erpapi.products.entity.*;
import com.kamesan.erpapi.products.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 商品服務
 *
 * <p>處理商品相關的業務邏輯，包括：</p>
 * <ul>
 *   <li>商品 CRUD 操作</li>
 *   <li>商品查詢與搜尋</li>
 *   <li>條碼管理</li>
 *   <li>商品驗證</li>
 * </ul>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    /**
     * 商品 Repository
     */
    private final ProductRepository productRepository;

    /**
     * 商品條碼 Repository
     */
    private final ProductBarcodeRepository productBarcodeRepository;

    /**
     * 分類 Repository
     */
    private final CategoryRepository categoryRepository;

    /**
     * 單位 Repository
     */
    private final UnitRepository unitRepository;

    /**
     * 稅別 Repository
     */
    private final TaxTypeRepository taxTypeRepository;

    /**
     * 建立商品
     *
     * <p>建立新商品，包含以下驗證：</p>
     * <ul>
     *   <li>貨號(SKU)唯一性檢查</li>
     *   <li>條碼唯一性檢查</li>
     *   <li>分類、單位、稅別存在性檢查</li>
     * </ul>
     *
     * @param request 建立商品請求
     * @return 建立的商品 DTO
     */
    @Transactional
    public ProductDto createProduct(CreateProductRequest request) {
        log.info("建立商品: {}", request.getSku());

        // 檢查貨號是否已存在
        if (productRepository.existsBySku(request.getSku())) {
            throw BusinessException.alreadyExists("商品", "貨號", request.getSku());
        }

        // 檢查條碼是否已存在
        if (request.getBarcode() != null && !request.getBarcode().isEmpty()) {
            if (productRepository.existsByBarcode(request.getBarcode()) ||
                    productBarcodeRepository.existsByBarcode(request.getBarcode())) {
                throw BusinessException.alreadyExists("商品", "條碼", request.getBarcode());
            }
        }

        // 建立商品實體
        Product product = Product.builder()
                .sku(request.getSku())
                .name(request.getName())
                .description(request.getDescription())
                .costPrice(request.getCostPrice() != null ? request.getCostPrice() : BigDecimal.ZERO)
                .sellingPrice(request.getSellingPrice())
                .barcode(request.getBarcode())
                .safetyStock(request.getSafetyStock() != null ? request.getSafetyStock() : 0)
                .active(request.getActive() != null ? request.getActive() : true)
                .build();

        // 設定分類
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> BusinessException.notFound("分類", request.getCategoryId()));
            product.setCategory(category);
        }

        // 設定單位
        if (request.getUnitId() != null) {
            Unit unit = unitRepository.findById(request.getUnitId())
                    .orElseThrow(() -> BusinessException.notFound("單位", request.getUnitId()));
            product.setUnit(unit);
        }

        // 設定稅別（若未指定則使用預設稅別）
        if (request.getTaxTypeId() != null) {
            TaxType taxType = taxTypeRepository.findById(request.getTaxTypeId())
                    .orElseThrow(() -> BusinessException.notFound("稅別", request.getTaxTypeId()));
            product.setTaxType(taxType);
        } else {
            taxTypeRepository.findDefaultTaxType().ifPresent(product::setTaxType);
        }

        // 儲存商品
        product = productRepository.save(product);

        // 處理主要條碼
        if (request.getBarcode() != null && !request.getBarcode().isEmpty()) {
            ProductBarcode primaryBarcode = ProductBarcode.builder()
                    .product(product)
                    .barcode(request.getBarcode())
                    .primary(true)
                    .build();
            productBarcodeRepository.save(primaryBarcode);
        }

        // 處理額外條碼
        if (request.getBarcodes() != null && !request.getBarcodes().isEmpty()) {
            for (CreateProductRequest.BarcodeRequest barcodeRequest : request.getBarcodes()) {
                // 檢查條碼是否已存在
                if (productBarcodeRepository.existsByBarcode(barcodeRequest.getBarcode())) {
                    throw BusinessException.alreadyExists("商品", "條碼", barcodeRequest.getBarcode());
                }

                ProductBarcode barcode = ProductBarcode.builder()
                        .product(product)
                        .barcode(barcodeRequest.getBarcode())
                        .barcodeType(barcodeRequest.getBarcodeType())
                        .notes(barcodeRequest.getNotes())
                        .primary(false)
                        .build();
                productBarcodeRepository.save(barcode);
            }
        }

        log.info("商品建立成功: ID={}, SKU={}", product.getId(), product.getSku());

        return convertToDto(product);
    }

    /**
     * 更新商品
     *
     * <p>更新現有商品，包含以下驗證：</p>
     * <ul>
     *   <li>貨號(SKU)唯一性檢查（排除自身）</li>
     *   <li>條碼唯一性檢查（排除自身）</li>
     *   <li>分類、單位、稅別存在性檢查</li>
     * </ul>
     *
     * @param id      商品 ID
     * @param request 更新商品請求
     * @return 更新後的商品 DTO
     */
    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public ProductDto updateProduct(Long id, UpdateProductRequest request) {
        log.info("更新商品: ID={}", id);

        // 查詢商品
        Product product = productRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("商品", id));

        // 檢查貨號是否已被其他商品使用
        if (productRepository.existsBySkuAndIdNot(request.getSku(), id)) {
            throw BusinessException.alreadyExists("商品", "貨號", request.getSku());
        }

        // 檢查條碼是否已被其他商品使用
        if (request.getBarcode() != null && !request.getBarcode().isEmpty()) {
            if (productRepository.existsByBarcodeAndIdNot(request.getBarcode(), id)) {
                throw BusinessException.alreadyExists("商品", "條碼", request.getBarcode());
            }
        }

        // 更新基本資訊
        product.setSku(request.getSku());
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setCostPrice(request.getCostPrice() != null ? request.getCostPrice() : BigDecimal.ZERO);
        product.setSellingPrice(request.getSellingPrice());
        product.setBarcode(request.getBarcode());
        product.setSafetyStock(request.getSafetyStock() != null ? request.getSafetyStock() : 0);

        if (request.getActive() != null) {
            product.setActive(request.getActive());
        }

        // 更新分類
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> BusinessException.notFound("分類", request.getCategoryId()));
            product.setCategory(category);
        } else {
            product.setCategory(null);
        }

        // 更新單位
        if (request.getUnitId() != null) {
            Unit unit = unitRepository.findById(request.getUnitId())
                    .orElseThrow(() -> BusinessException.notFound("單位", request.getUnitId()));
            product.setUnit(unit);
        } else {
            product.setUnit(null);
        }

        // 更新稅別
        if (request.getTaxTypeId() != null) {
            TaxType taxType = taxTypeRepository.findById(request.getTaxTypeId())
                    .orElseThrow(() -> BusinessException.notFound("稅別", request.getTaxTypeId()));
            product.setTaxType(taxType);
        }

        // 處理條碼更新
        if (request.getBarcodes() != null) {
            updateProductBarcodes(product, request.getBarcodes());
        }

        // 儲存商品
        product = productRepository.save(product);

        log.info("商品更新成功: ID={}, SKU={}", product.getId(), product.getSku());

        return convertToDto(product);
    }

    /**
     * 更新商品條碼列表
     *
     * @param product  商品實體
     * @param barcodes 條碼請求列表
     */
    private void updateProductBarcodes(Product product, List<UpdateProductRequest.BarcodeRequest> barcodes) {
        // 刪除現有條碼
        productBarcodeRepository.deleteByProductId(product.getId());

        // 新增條碼
        for (UpdateProductRequest.BarcodeRequest barcodeRequest : barcodes) {
            // 檢查條碼是否已被其他商品使用
            if (productBarcodeRepository.existsByBarcode(barcodeRequest.getBarcode())) {
                ProductBarcode existingBarcode = productBarcodeRepository.findByBarcode(barcodeRequest.getBarcode())
                        .orElse(null);
                if (existingBarcode != null && !existingBarcode.getProduct().getId().equals(product.getId())) {
                    throw BusinessException.alreadyExists("商品", "條碼", barcodeRequest.getBarcode());
                }
            }

            ProductBarcode barcode = ProductBarcode.builder()
                    .product(product)
                    .barcode(barcodeRequest.getBarcode())
                    .primary(barcodeRequest.getPrimary() != null && barcodeRequest.getPrimary())
                    .barcodeType(barcodeRequest.getBarcodeType())
                    .notes(barcodeRequest.getNotes())
                    .build();
            productBarcodeRepository.save(barcode);
        }
    }

    /**
     * 根據 ID 查詢商品
     *
     * @param id 商品 ID
     * @return 商品 DTO
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "products", key = "'id:' + #id")
    public ProductDto getProductById(Long id) {
        log.debug("從資料庫查詢商品: ID={}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("商品", id));
        return convertToDto(product);
    }

    /**
     * 根據貨號查詢商品
     *
     * @param sku 商品貨號
     * @return 商品 DTO
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "products", key = "'sku:' + #sku")
    public ProductDto getProductBySku(String sku) {
        log.debug("從資料庫查詢商品: SKU={}", sku);
        Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> BusinessException.notFound("商品", "SKU: " + sku));
        return convertToDto(product);
    }

    /**
     * 根據條碼查詢商品
     *
     * @param barcode 條碼
     * @return 商品 DTO
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "products", key = "'barcode:' + #barcode")
    public ProductDto getProductByBarcode(String barcode) {
        log.debug("從資料庫查詢商品: 條碼={}", barcode);
        // 先從商品主條碼查詢
        Product product = productRepository.findByBarcode(barcode).orElse(null);

        // 若未找到，則從商品條碼表查詢
        if (product == null) {
            Long productId = productBarcodeRepository.findProductIdByBarcode(barcode)
                    .orElseThrow(() -> BusinessException.notFound("商品", "條碼: " + barcode));
            product = productRepository.findById(productId)
                    .orElseThrow(() -> BusinessException.notFound("商品", productId));
        }

        return convertToDto(product);
    }

    /**
     * 刪除商品
     *
     * <p>實際執行軟刪除（將 active 設為 false）</p>
     *
     * @param id 商品 ID
     */
    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public void deleteProduct(Long id) {
        log.info("刪除商品: ID={}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("商品", id));

        // 軟刪除：將商品設為停用
        product.setActive(false);
        productRepository.save(product);

        log.info("商品刪除成功（軟刪除）: ID={}", id);
    }

    /**
     * 永久刪除商品
     *
     * <p>警告：此操作會永久刪除商品及其所有條碼</p>
     *
     * @param id 商品 ID
     */
    @Transactional
    public void hardDeleteProduct(Long id) {
        log.warn("永久刪除商品: ID={}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("商品", id));

        // 刪除所有條碼
        productBarcodeRepository.deleteByProductId(id);

        // 刪除商品
        productRepository.delete(product);

        log.info("商品永久刪除成功: ID={}", id);
    }

    /**
     * 查詢所有商品（分頁）
     *
     * @param pageable 分頁參數
     * @return 商品分頁
     */
    @Transactional(readOnly = true)
    public Page<ProductDto> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable)
                .map(this::convertToDto);
    }

    /**
     * 查詢啟用的商品（分頁）
     *
     * @param pageable 分頁參數
     * @return 商品分頁
     */
    @Transactional(readOnly = true)
    public Page<ProductDto> getActiveProducts(Pageable pageable) {
        return productRepository.findByActiveTrue(pageable)
                .map(this::convertToDto);
    }

    /**
     * 搜尋商品
     *
     * @param keyword  關鍵字（名稱、貨號、條碼）
     * @param pageable 分頁參數
     * @return 商品分頁
     */
    @Transactional(readOnly = true)
    public Page<ProductDto> searchProducts(String keyword, Pageable pageable) {
        return productRepository.search(keyword, pageable)
                .map(this::convertToDto);
    }

    /**
     * 根據分類查詢商品
     *
     * @param categoryId 分類 ID
     * @param pageable   分頁參數
     * @return 商品分頁
     */
    @Transactional(readOnly = true)
    public Page<ProductDto> getProductsByCategory(Long categoryId, Pageable pageable) {
        return productRepository.findByCategoryId(categoryId, pageable)
                .map(this::convertToDto);
    }

    /**
     * 將商品實體轉換為 DTO
     *
     * @param product 商品實體
     * @return 商品 DTO
     */
    private ProductDto convertToDto(Product product) {
        ProductDto.ProductDtoBuilder builder = ProductDto.builder()
                .id(product.getId())
                .sku(product.getSku())
                .name(product.getName())
                .description(product.getDescription())
                .costPrice(product.getCostPrice())
                .sellingPrice(product.getSellingPrice())
                .barcode(product.getBarcode())
                .safetyStock(product.getSafetyStock())
                .active(product.isActive())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt());

        // 計算毛利和毛利率
        builder.grossProfit(product.calculateGrossProfit());
        builder.grossProfitMargin(product.calculateGrossProfitMargin());

        // 設定分類資訊
        if (product.getCategory() != null) {
            Category category = product.getCategory();
            builder.category(ProductDto.CategoryInfo.builder()
                    .id(category.getId())
                    .code(category.getCode())
                    .name(category.getName())
                    .fullPathName(category.getFullPathName())
                    .build());
        }

        // 設定單位資訊
        if (product.getUnit() != null) {
            Unit unit = product.getUnit();
            builder.unit(ProductDto.UnitInfo.builder()
                    .id(unit.getId())
                    .code(unit.getCode())
                    .name(unit.getName())
                    .build());
        }

        // 設定稅別資訊
        if (product.getTaxType() != null) {
            TaxType taxType = product.getTaxType();
            builder.taxType(ProductDto.TaxTypeInfo.builder()
                    .id(taxType.getId())
                    .code(taxType.getCode())
                    .name(taxType.getName())
                    .rate(taxType.getRate())
                    .build());

            // 計算含稅售價
            builder.taxIncludedPrice(product.calculateTaxIncludedPrice());
        } else {
            builder.taxIncludedPrice(product.getSellingPrice());
        }

        // 設定條碼列表
        List<ProductBarcode> barcodes = productBarcodeRepository.findByProductId(product.getId());
        if (barcodes != null && !barcodes.isEmpty()) {
            List<ProductDto.BarcodeInfo> barcodeInfoList = barcodes.stream()
                    .map(barcode -> ProductDto.BarcodeInfo.builder()
                            .id(barcode.getId())
                            .barcode(barcode.getBarcode())
                            .primary(barcode.isPrimary())
                            .barcodeType(barcode.getBarcodeType())
                            .notes(barcode.getNotes())
                            .build())
                    .collect(Collectors.toList());
            builder.barcodes(barcodeInfoList);
        } else {
            builder.barcodes(new ArrayList<>());
        }

        return builder.build();
    }
}
