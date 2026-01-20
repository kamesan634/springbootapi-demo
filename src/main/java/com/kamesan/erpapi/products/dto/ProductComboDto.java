package com.kamesan.erpapi.products.dto;

import com.kamesan.erpapi.products.entity.ProductCombo.ComboType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品組合 DTO
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductComboDto {

    private Long id;
    private String code;
    private String name;
    private String description;
    private ComboType comboType;
    private String comboTypeLabel;
    private BigDecimal originalPrice;
    private BigDecimal comboPrice;
    private BigDecimal discountAmount;
    private BigDecimal discountPercentage;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean active;
    private boolean sellable;
    private Integer minSelect;
    private Integer maxSelect;
    private List<ComboItemDto> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * 組合項目 DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ComboItemDto {
        private Long id;
        private Long productId;
        private String productSku;
        private String productName;
        private BigDecimal productPrice;
        private Integer quantity;
        private BigDecimal subtotal;
        private boolean required;
        private String groupName;
        private Integer sortOrder;
        private String remark;
    }

    /**
     * 建立組合請求
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateComboRequest {
        private String code;
        private String name;
        private String description;
        private ComboType comboType;
        private BigDecimal comboPrice;
        private LocalDate startDate;
        private LocalDate endDate;
        private Integer minSelect;
        private Integer maxSelect;
        private List<ComboItemRequest> items;
    }

    /**
     * 更新組合請求
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateComboRequest {
        private String name;
        private String description;
        private ComboType comboType;
        private BigDecimal comboPrice;
        private LocalDate startDate;
        private LocalDate endDate;
        private boolean active;
        private Integer minSelect;
        private Integer maxSelect;
        private List<ComboItemRequest> items;
    }

    /**
     * 組合項目請求
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ComboItemRequest {
        private Long productId;
        private Integer quantity;
        private boolean required;
        private String groupName;
        private Integer sortOrder;
        private String remark;
    }

    /**
     * 組合查詢條件
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ComboSearchCriteria {
        private String keyword;
        private ComboType comboType;
        private Boolean active;
        private Boolean sellable;
        private LocalDate effectiveDate;
    }

    /**
     * 組合銷售記錄
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ComboSalesDto {
        private Long comboId;
        private String comboCode;
        private String comboName;
        private Integer salesCount;
        private BigDecimal totalRevenue;
        private BigDecimal totalDiscount;
    }
}
