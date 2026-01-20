package com.kamesan.erpapi.products.service;

import com.kamesan.erpapi.common.exception.BusinessException;
import com.kamesan.erpapi.products.dto.ProductComboDto;
import com.kamesan.erpapi.products.dto.ProductComboDto.*;
import com.kamesan.erpapi.products.entity.Product;
import com.kamesan.erpapi.products.entity.ProductCombo;
import com.kamesan.erpapi.products.entity.ProductCombo.ComboType;
import com.kamesan.erpapi.products.entity.ProductComboItem;
import com.kamesan.erpapi.products.repository.ProductComboRepository;
import com.kamesan.erpapi.products.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 商品組合服務
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductComboService {

    private final ProductComboRepository comboRepository;
    private final ProductRepository productRepository;

    /**
     * 建立商品組合
     */
    @Transactional
    public ProductComboDto createCombo(CreateComboRequest request) {
        log.info("建立商品組合: {}", request.getCode());

        // 檢查編碼是否重複
        if (comboRepository.existsByCode(request.getCode())) {
            throw new BusinessException("組合編碼已存在: " + request.getCode());
        }

        ProductCombo combo = ProductCombo.builder()
                .code(request.getCode())
                .name(request.getName())
                .description(request.getDescription())
                .comboType(request.getComboType() != null ? request.getComboType() : ComboType.FIXED)
                .comboPrice(request.getComboPrice())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .minSelect(request.getMinSelect())
                .maxSelect(request.getMaxSelect())
                .active(true)
                .build();

        // 新增組合項目
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            for (ComboItemRequest itemRequest : request.getItems()) {
                Product product = productRepository.findById(itemRequest.getProductId())
                        .orElseThrow(() -> new BusinessException("商品不存在: " + itemRequest.getProductId()));

                ProductComboItem item = ProductComboItem.builder()
                        .product(product)
                        .quantity(itemRequest.getQuantity() != null ? itemRequest.getQuantity() : 1)
                        .required(itemRequest.isRequired())
                        .groupName(itemRequest.getGroupName())
                        .sortOrder(itemRequest.getSortOrder() != null ? itemRequest.getSortOrder() : 0)
                        .remark(itemRequest.getRemark())
                        .build();

                combo.addItem(item);
            }
        }

        // 計算原價和折扣
        BigDecimal originalPrice = combo.calculateOriginalPrice();
        combo.setOriginalPrice(originalPrice);
        combo.setDiscountAmount(originalPrice.subtract(combo.getComboPrice()));

        ProductCombo saved = comboRepository.save(combo);
        log.info("商品組合建立成功: {}", saved.getId());

        return convertToDto(saved);
    }

    /**
     * 更新商品組合
     */
    @Transactional
    public ProductComboDto updateCombo(Long id, UpdateComboRequest request) {
        log.info("更新商品組合: {}", id);

        ProductCombo combo = comboRepository.findByIdWithItems(id)
                .orElseThrow(() -> new BusinessException("組合不存在: " + id));

        // 更新基本資訊
        if (request.getName() != null) {
            combo.setName(request.getName());
        }
        if (request.getDescription() != null) {
            combo.setDescription(request.getDescription());
        }
        if (request.getComboType() != null) {
            combo.setComboType(request.getComboType());
        }
        if (request.getComboPrice() != null) {
            combo.setComboPrice(request.getComboPrice());
        }
        combo.setStartDate(request.getStartDate());
        combo.setEndDate(request.getEndDate());
        combo.setActive(request.isActive());
        combo.setMinSelect(request.getMinSelect());
        combo.setMaxSelect(request.getMaxSelect());

        // 更新組合項目
        if (request.getItems() != null) {
            combo.getItems().clear();

            for (ComboItemRequest itemRequest : request.getItems()) {
                Product product = productRepository.findById(itemRequest.getProductId())
                        .orElseThrow(() -> new BusinessException("商品不存在: " + itemRequest.getProductId()));

                ProductComboItem item = ProductComboItem.builder()
                        .product(product)
                        .quantity(itemRequest.getQuantity() != null ? itemRequest.getQuantity() : 1)
                        .required(itemRequest.isRequired())
                        .groupName(itemRequest.getGroupName())
                        .sortOrder(itemRequest.getSortOrder() != null ? itemRequest.getSortOrder() : 0)
                        .remark(itemRequest.getRemark())
                        .build();

                combo.addItem(item);
            }
        }

        // 重新計算原價和折扣
        BigDecimal originalPrice = combo.calculateOriginalPrice();
        combo.setOriginalPrice(originalPrice);
        combo.setDiscountAmount(originalPrice.subtract(combo.getComboPrice()));

        ProductCombo saved = comboRepository.save(combo);
        log.info("商品組合更新成功: {}", saved.getId());

        return convertToDto(saved);
    }

    /**
     * 取得組合詳情
     */
    @Transactional(readOnly = true)
    public ProductComboDto getCombo(Long id) {
        ProductCombo combo = comboRepository.findByIdWithItemsAndProducts(id)
                .orElseThrow(() -> new BusinessException("組合不存在: " + id));
        return convertToDto(combo);
    }

    /**
     * 根據編碼取得組合
     */
    @Transactional(readOnly = true)
    public ProductComboDto getComboByCode(String code) {
        ProductCombo combo = comboRepository.findByCode(code)
                .orElseThrow(() -> new BusinessException("組合不存在: " + code));
        return convertToDto(combo);
    }

    /**
     * 查詢組合列表
     */
    @Transactional(readOnly = true)
    public Page<ProductComboDto> searchCombos(ComboSearchCriteria criteria, Pageable pageable) {
        Page<ProductCombo> combos = comboRepository.findByConditions(
                criteria.getKeyword(),
                criteria.getComboType(),
                criteria.getActive(),
                pageable);

        return combos.map(this::convertToDto);
    }

    /**
     * 取得可銷售的組合
     */
    @Transactional(readOnly = true)
    public List<ProductComboDto> getSellableCombos() {
        List<ProductCombo> combos = comboRepository.findSellableCombos(LocalDate.now());
        return combos.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 啟用/停用組合
     */
    @Transactional
    public ProductComboDto toggleActive(Long id) {
        ProductCombo combo = comboRepository.findById(id)
                .orElseThrow(() -> new BusinessException("組合不存在: " + id));

        combo.setActive(!combo.isActive());
        ProductCombo saved = comboRepository.save(combo);

        log.info("組合 {} 狀態已變更為: {}", id, saved.isActive() ? "啟用" : "停用");
        return convertToDto(saved);
    }

    /**
     * 刪除組合
     */
    @Transactional
    public void deleteCombo(Long id) {
        ProductCombo combo = comboRepository.findById(id)
                .orElseThrow(() -> new BusinessException("組合不存在: " + id));

        comboRepository.delete(combo);
        log.info("商品組合已刪除: {}", id);
    }

    /**
     * 查詢包含特定商品的組合
     */
    @Transactional(readOnly = true)
    public List<ProductComboDto> findCombosByProduct(Long productId) {
        List<ProductCombo> combos = comboRepository.findByProductId(productId);
        return combos.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 查詢即將到期的組合
     */
    @Transactional(readOnly = true)
    public List<ProductComboDto> findExpiringCombos(int daysAhead) {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(daysAhead);

        List<ProductCombo> combos = comboRepository.findExpiringCombos(startDate, endDate);
        return combos.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 計算組合售價
     * 用於結帳時計算組合實際售價
     */
    public BigDecimal calculateComboPrice(Long comboId, List<Long> selectedProductIds) {
        ProductCombo combo = comboRepository.findByIdWithItemsAndProducts(comboId)
                .orElseThrow(() -> new BusinessException("組合不存在: " + comboId));

        if (combo.getComboType() == ComboType.FIXED) {
            // 固定組合直接返回組合價
            return combo.getComboPrice();
        }

        // 選配組合：計算選中商品的比例價格
        BigDecimal totalOriginal = combo.getOriginalPrice();
        BigDecimal selectedOriginal = combo.getItems().stream()
                .filter(item -> selectedProductIds.contains(item.getProduct().getId()))
                .map(ProductComboItem::calculateSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalOriginal.compareTo(BigDecimal.ZERO) == 0) {
            return selectedOriginal;
        }

        // 按比例計算折扣
        BigDecimal ratio = selectedOriginal.divide(totalOriginal, 4, RoundingMode.HALF_UP);
        return combo.getComboPrice().multiply(ratio).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 轉換為 DTO
     */
    private ProductComboDto convertToDto(ProductCombo combo) {
        BigDecimal discountPercentage = BigDecimal.ZERO;
        if (combo.getOriginalPrice() != null && combo.getOriginalPrice().compareTo(BigDecimal.ZERO) > 0) {
            discountPercentage = combo.getDiscountAmount()
                    .multiply(new BigDecimal("100"))
                    .divide(combo.getOriginalPrice(), 2, RoundingMode.HALF_UP);
        }

        List<ComboItemDto> itemDtos = combo.getItems().stream()
                .map(this::convertItemToDto)
                .collect(Collectors.toList());

        return ProductComboDto.builder()
                .id(combo.getId())
                .code(combo.getCode())
                .name(combo.getName())
                .description(combo.getDescription())
                .comboType(combo.getComboType())
                .comboTypeLabel(combo.getComboType().getLabel())
                .originalPrice(combo.getOriginalPrice())
                .comboPrice(combo.getComboPrice())
                .discountAmount(combo.getDiscountAmount())
                .discountPercentage(discountPercentage)
                .startDate(combo.getStartDate())
                .endDate(combo.getEndDate())
                .active(combo.isActive())
                .sellable(combo.isSellable())
                .minSelect(combo.getMinSelect())
                .maxSelect(combo.getMaxSelect())
                .items(itemDtos)
                .createdAt(combo.getCreatedAt())
                .updatedAt(combo.getUpdatedAt())
                .build();
    }

    /**
     * 轉換項目為 DTO
     */
    private ComboItemDto convertItemToDto(ProductComboItem item) {
        Product product = item.getProduct();
        return ComboItemDto.builder()
                .id(item.getId())
                .productId(product != null ? product.getId() : null)
                .productSku(product != null ? product.getSku() : null)
                .productName(product != null ? product.getName() : null)
                .productPrice(product != null ? product.getSellingPrice() : null)
                .quantity(item.getQuantity())
                .subtotal(item.calculateSubtotal())
                .required(item.isRequired())
                .groupName(item.getGroupName())
                .sortOrder(item.getSortOrder())
                .remark(item.getRemark())
                .build();
    }
}
