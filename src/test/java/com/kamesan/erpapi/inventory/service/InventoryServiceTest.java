package com.kamesan.erpapi.inventory.service;

import com.kamesan.erpapi.BaseIntegrationTest;
import com.kamesan.erpapi.inventory.dto.InventoryDto;
import com.kamesan.erpapi.products.entity.Category;
import com.kamesan.erpapi.products.entity.Product;
import com.kamesan.erpapi.products.entity.TaxType;
import com.kamesan.erpapi.products.entity.Unit;
import com.kamesan.erpapi.products.repository.CategoryRepository;
import com.kamesan.erpapi.products.repository.ProductRepository;
import com.kamesan.erpapi.products.repository.TaxTypeRepository;
import com.kamesan.erpapi.products.repository.UnitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 庫存服務測試類別
 */
@DisplayName("庫存服務測試")
class InventoryServiceTest extends BaseIntegrationTest {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UnitRepository unitRepository;

    @Autowired
    private TaxTypeRepository taxTypeRepository;

    private Product testProduct;

    @BeforeEach
    void setUpInventoryTestData() {
        // 初始化分類
        Category testCategory = categoryRepository.findByCode("INV-TEST-CAT").orElseGet(() -> {
            Category cat = new Category();
            cat.setCode("INV-TEST-CAT");
            cat.setName("庫存測試分類");
            cat.setActive(true);
            return categoryRepository.save(cat);
        });

        // 初始化單位
        Unit testUnit = unitRepository.findByCode("INV-TEST-UNIT").orElseGet(() -> {
            Unit unit = new Unit();
            unit.setCode("INV-TEST-UNIT");
            unit.setName("個");
            unit.setActive(true);
            return unitRepository.save(unit);
        });

        // 初始化稅別
        TaxType testTaxType = taxTypeRepository.findByCode("INV-TEST-TAX").orElseGet(() -> {
            TaxType taxType = new TaxType();
            taxType.setCode("INV-TEST-TAX");
            taxType.setName("測試稅別");
            taxType.setRate(new BigDecimal("0.05"));
            taxType.setActive(true);
            return taxTypeRepository.save(taxType);
        });

        // 初始化測試商品
        testProduct = productRepository.findBySku("INV-TEST-SKU").orElseGet(() -> {
            Product product = new Product();
            product.setSku("INV-TEST-SKU");
            product.setName("庫存測試商品");
            product.setCategory(testCategory);
            product.setUnit(testUnit);
            product.setTaxType(testTaxType);
            product.setCostPrice(new BigDecimal("100.00"));
            product.setSellingPrice(new BigDecimal("200.00"));
            product.setActive(true);
            return productRepository.save(product);
        });
    }

    @Nested
    @DisplayName("getAllInventories - 查詢所有庫存")
    class GetAllInventoriesTests {

        @Test
        @DisplayName("應返回庫存分頁列表")
        void getAllInventories_ShouldReturnPagedList() {
            Page<InventoryDto> result = inventoryService.getAllInventories(PageRequest.of(0, 10));
            assertNotNull(result);
        }
    }

    @Nested
    @DisplayName("getInventory - 查詢指定商品庫存")
    class GetInventoryTests {

        @Test
        @DisplayName("查詢不存在的庫存應拋出例外")
        void getInventory_WithNoInventory_ShouldThrowException() {
            // 查詢不存在的組合應拋出 BusinessException
            assertThrows(com.kamesan.erpapi.common.exception.BusinessException.class, () -> {
                inventoryService.getInventory(testProduct.getId(), testStore.getId());
            });
        }
    }

    @Nested
    @DisplayName("getInventoriesByWarehouse - 查詢倉庫庫存")
    class GetInventoriesByWarehouseTests {

        @Test
        @DisplayName("應返回倉庫庫存列表")
        void getByWarehouse_ShouldReturnInventories() {
            Page<InventoryDto> result = inventoryService.getInventoriesByWarehouse(testStore.getId(), PageRequest.of(0, 10));
            assertNotNull(result);
        }
    }

    @Nested
    @DisplayName("getInventoriesByProduct - 查詢商品庫存")
    class GetInventoriesByProductTests {

        @Test
        @DisplayName("應返回商品庫存列表")
        void getByProduct_ShouldReturnInventories() {
            java.util.List<InventoryDto> result = inventoryService.getInventoriesByProduct(testProduct.getId());
            assertNotNull(result);
        }
    }

    @Nested
    @DisplayName("getTotalQuantity - 查詢商品總數量")
    class GetTotalQuantityTests {

        @Test
        @DisplayName("應返回商品總數量")
        void getTotalQuantity_ShouldReturnQuantity() {
            Integer result = inventoryService.getTotalQuantity(testProduct.getId());
            assertNotNull(result);
            assertTrue(result >= 0);
        }
    }
}
