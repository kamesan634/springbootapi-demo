package com.kamesan.erpapi.purchasing.service;

import com.kamesan.erpapi.inventory.entity.Inventory;
import com.kamesan.erpapi.inventory.repository.InventoryRepository;
import com.kamesan.erpapi.products.entity.Product;
import com.kamesan.erpapi.products.repository.ProductRepository;
import com.kamesan.erpapi.purchasing.dto.PurchaseSuggestionDto;
import com.kamesan.erpapi.purchasing.dto.PurchaseSuggestionDto.PriorityLevel;
import com.kamesan.erpapi.purchasing.dto.PurchaseSuggestionDto.SuggestionItem;
import com.kamesan.erpapi.sales.repository.OrderItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 採購建議服務
 *
 * <p>根據庫存狀況、銷售歷史和安全庫存自動計算採購建議</p>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PurchaseSuggestionService {

    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final OrderItemRepository orderItemRepository;

    // 預設參數
    private static final int DEFAULT_LEAD_TIME_DAYS = 7;
    private static final int SALES_HISTORY_DAYS = 30;
    private static final int REORDER_MULTIPLIER = 2;  // 建議訂購量 = 日銷量 * 交期 * 倍數

    /**
     * 計算採購建議
     *
     * @param warehouseId 倉庫 ID（可選）
     * @param categoryId  分類 ID（可選）
     * @param onlyLowStock 是否只顯示低庫存商品
     * @return 採購建議清單
     */
    @Transactional(readOnly = true)
    public PurchaseSuggestionDto calculateSuggestions(Long warehouseId, Long categoryId, boolean onlyLowStock) {
        log.info("計算採購建議: warehouseId={}, categoryId={}, onlyLowStock={}", warehouseId, categoryId, onlyLowStock);

        // 取得所有啟用商品
        List<Product> products = getProducts(categoryId);

        // 取得庫存資料
        Map<Long, List<Inventory>> inventoryMap = getInventoryMap(warehouseId);

        // 計算各商品的銷售歷史
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(SALES_HISTORY_DAYS);

        List<SuggestionItem> suggestions = new ArrayList<>();
        int criticalCount = 0;
        int warningCount = 0;
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (Product product : products) {
            SuggestionItem item = calculateProductSuggestion(product, inventoryMap, startDate, endDate);

            if (item != null) {
                if (onlyLowStock && item.getPriority() == PriorityLevel.LOW) {
                    continue; // 跳過不需要採購的商品
                }

                suggestions.add(item);
                totalAmount = totalAmount.add(item.getTotalCost() != null ? item.getTotalCost() : BigDecimal.ZERO);

                if (item.getPriority() == PriorityLevel.CRITICAL) {
                    criticalCount++;
                } else if (item.getPriority() == PriorityLevel.HIGH) {
                    warningCount++;
                }
            }
        }

        // 按優先順序排序
        suggestions.sort(Comparator.comparing(SuggestionItem::getPriority)
                .thenComparing(SuggestionItem::getDaysOfStock, Comparator.nullsLast(Comparator.naturalOrder())));

        return PurchaseSuggestionDto.builder()
                .suggestions(suggestions)
                .totalItems(suggestions.size())
                .totalAmount(totalAmount)
                .criticalCount(criticalCount)
                .warningCount(warningCount)
                .build();
    }

    private List<Product> getProducts(Long categoryId) {
        if (categoryId != null) {
            return productRepository.findByCategoryIdAndActiveTrue(categoryId);
        }
        return productRepository.findByActiveTrue();
    }

    private Map<Long, List<Inventory>> getInventoryMap(Long warehouseId) {
        List<Inventory> inventories;
        if (warehouseId != null) {
            inventories = inventoryRepository.findByWarehouseId(warehouseId);
        } else {
            inventories = inventoryRepository.findAll();
        }

        return inventories.stream()
                .collect(Collectors.groupingBy(Inventory::getProductId));
    }

    private SuggestionItem calculateProductSuggestion(
            Product product,
            Map<Long, List<Inventory>> inventoryMap,
            LocalDateTime startDate,
            LocalDateTime endDate) {

        Long productId = product.getId();
        List<Inventory> inventories = inventoryMap.getOrDefault(productId, new ArrayList<>());

        // 計算總庫存
        int currentStock = inventories.stream()
                .mapToInt(i -> i.getQuantity() != null ? i.getQuantity() : 0)
                .sum();
        int reservedStock = inventories.stream()
                .mapToInt(i -> i.getReservedQuantity() != null ? i.getReservedQuantity() : 0)
                .sum();
        int availableStock = currentStock - reservedStock;

        // 取得安全庫存
        int safetyStock = product.getSafetyStock() != null ? product.getSafetyStock() : 0;

        // 計算銷售歷史
        Long totalSales = orderItemRepository.sumQuantityByProductIdAndDateRange(productId, startDate, endDate);
        int averageDailySales = totalSales > 0 ?
                Math.max(1, (int) Math.ceil(totalSales.doubleValue() / SALES_HISTORY_DAYS)) : 0;

        // 計算庫存可用天數
        int daysOfStock = averageDailySales > 0 ? availableStock / averageDailySales : 999;

        // 計算建議採購數量
        int leadTimeDays = DEFAULT_LEAD_TIME_DAYS;
        int suggestedQuantity = calculateSuggestedQuantity(
                availableStock, safetyStock, averageDailySales, leadTimeDays);

        // 確定優先等級
        PriorityLevel priority = determinePriority(availableStock, safetyStock, daysOfStock, leadTimeDays);

        // 如果不需要採購，返回 null 或低優先級項目
        String reason = generateReason(priority, availableStock, safetyStock, daysOfStock);

        BigDecimal unitCost = product.getCostPrice() != null ? product.getCostPrice() : BigDecimal.ZERO;
        BigDecimal totalCost = unitCost.multiply(BigDecimal.valueOf(suggestedQuantity));

        return SuggestionItem.builder()
                .productId(productId)
                .productSku(product.getSku())
                .productName(product.getName())
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .currentStock(currentStock)
                .reservedStock(reservedStock)
                .availableStock(availableStock)
                .safetyStock(safetyStock)
                .averageDailySales(averageDailySales)
                .leadTimeDays(leadTimeDays)
                .daysOfStock(daysOfStock)
                .suggestedQuantity(suggestedQuantity)
                .unitCost(unitCost)
                .totalCost(totalCost)
                .priority(priority)
                .reason(reason)
                .build();
    }

    private int calculateSuggestedQuantity(int availableStock, int safetyStock, int averageDailySales, int leadTimeDays) {
        if (averageDailySales == 0) {
            // 沒有銷售記錄，建議補到安全庫存
            return Math.max(0, safetyStock - availableStock);
        }

        // 建議數量 = (日銷量 * 交期 * 倍數 + 安全庫存) - 可用庫存
        int targetStock = averageDailySales * leadTimeDays * REORDER_MULTIPLIER + safetyStock;
        return Math.max(0, targetStock - availableStock);
    }

    private PriorityLevel determinePriority(int availableStock, int safetyStock, int daysOfStock, int leadTimeDays) {
        // 缺貨或庫存為 0
        if (availableStock <= 0) {
            return PriorityLevel.CRITICAL;
        }

        // 庫存低於安全庫存
        if (availableStock < safetyStock) {
            return PriorityLevel.CRITICAL;
        }

        // 可用天數少於交期
        if (daysOfStock < leadTimeDays) {
            return PriorityLevel.HIGH;
        }

        // 可用天數少於交期 * 2
        if (daysOfStock < leadTimeDays * 2) {
            return PriorityLevel.MEDIUM;
        }

        return PriorityLevel.LOW;
    }

    private String generateReason(PriorityLevel priority, int availableStock, int safetyStock, int daysOfStock) {
        return switch (priority) {
            case CRITICAL -> {
                if (availableStock <= 0) {
                    yield "已缺貨";
                } else if (availableStock < safetyStock) {
                    yield "庫存低於安全庫存";
                }
                yield "緊急需要補貨";
            }
            case HIGH -> String.format("庫存僅剩 %d 天用量，低於交期", daysOfStock);
            case MEDIUM -> String.format("庫存約 %d 天用量，建議補貨", daysOfStock);
            case LOW -> "庫存充足";
        };
    }

    /**
     * 取得缺貨商品列表
     */
    @Transactional(readOnly = true)
    public List<SuggestionItem> getOutOfStockProducts(Long warehouseId) {
        log.info("取得缺貨商品列表: warehouseId={}", warehouseId);

        PurchaseSuggestionDto suggestions = calculateSuggestions(warehouseId, null, true);

        return suggestions.getSuggestions().stream()
                .filter(item -> item.getAvailableStock() <= 0)
                .collect(Collectors.toList());
    }

    /**
     * 取得低庫存商品列表
     */
    @Transactional(readOnly = true)
    public List<SuggestionItem> getLowStockProducts(Long warehouseId) {
        log.info("取得低庫存商品列表: warehouseId={}", warehouseId);

        PurchaseSuggestionDto suggestions = calculateSuggestions(warehouseId, null, true);

        return suggestions.getSuggestions().stream()
                .filter(item -> item.getPriority() == PriorityLevel.CRITICAL ||
                               item.getPriority() == PriorityLevel.HIGH)
                .collect(Collectors.toList());
    }
}
