package com.kamesan.erpapi.customers.dto;

import com.kamesan.erpapi.customers.entity.CustomerLevel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 會員等級 DTO
 *
 * <p>用於回傳會員等級資訊給前端，包含：</p>
 * <ul>
 *   <li>基本資訊 - ID、代碼、名稱、描述</li>
 *   <li>權益資訊 - 折扣比率、點數倍率</li>
 *   <li>升級條件 - 所需消費金額</li>
 *   <li>狀態資訊 - 排序、啟用狀態</li>
 *   <li>統計資訊 - 該等級會員數量</li>
 * </ul>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "會員等級資訊")
public class CustomerLevelDto {

    /**
     * 等級 ID
     */
    @Schema(description = "等級 ID", example = "1")
    private Long id;

    /**
     * 等級代碼
     */
    @Schema(description = "等級代碼", example = "GOLD")
    private String code;

    /**
     * 等級名稱
     */
    @Schema(description = "等級名稱", example = "金卡會員")
    private String name;

    /**
     * 等級描述
     */
    @Schema(description = "等級描述", example = "享有 9 折優惠及 1.5 倍點數回饋")
    private String description;

    /**
     * 折扣比率
     * <p>例如 0.9 代表 9 折</p>
     */
    @Schema(description = "折扣比率（0.9 = 9折）", example = "0.90")
    private BigDecimal discountRate;

    /**
     * 折扣百分比顯示
     * <p>例如 "90%" 代表 9 折</p>
     */
    @Schema(description = "折扣百分比顯示", example = "90%")
    private String discountDisplay;

    /**
     * 點數倍率
     */
    @Schema(description = "點數倍率", example = "1.50")
    private BigDecimal pointsMultiplier;

    /**
     * 升級條件（累積消費金額）
     */
    @Schema(description = "升級條件（累積消費金額）", example = "10000.00")
    private BigDecimal upgradeCondition;

    /**
     * 是否啟用
     */
    @Schema(description = "是否啟用", example = "true")
    private boolean active;

    /**
     * 排序順序
     */
    @Schema(description = "排序順序", example = "3")
    private Integer sortOrder;

    /**
     * 該等級的會員數量
     */
    @Schema(description = "該等級的會員數量", example = "150")
    private Long customerCount;

    /**
     * 建立時間
     */
    @Schema(description = "建立時間")
    private LocalDateTime createdAt;

    /**
     * 更新時間
     */
    @Schema(description = "更新時間")
    private LocalDateTime updatedAt;

    /**
     * 從 Entity 轉換為 DTO
     *
     * @param entity 會員等級 Entity
     * @return 會員等級 DTO
     */
    public static CustomerLevelDto fromEntity(CustomerLevel entity) {
        if (entity == null) {
            return null;
        }

        return CustomerLevelDto.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .name(entity.getName())
                .description(entity.getDescription())
                .discountRate(entity.getDiscountRate())
                .discountDisplay(formatDiscountDisplay(entity.getDiscountRate()))
                .pointsMultiplier(entity.getPointsMultiplier())
                .upgradeCondition(entity.getUpgradeCondition())
                .active(entity.isActive())
                .sortOrder(entity.getSortOrder())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    /**
     * 從 Entity 轉換為 DTO（含會員數量）
     *
     * @param entity        會員等級 Entity
     * @param customerCount 會員數量
     * @return 會員等級 DTO
     */
    public static CustomerLevelDto fromEntity(CustomerLevel entity, long customerCount) {
        CustomerLevelDto dto = fromEntity(entity);
        if (dto != null) {
            dto.setCustomerCount(customerCount);
        }
        return dto;
    }

    /**
     * 格式化折扣顯示
     * <p>將折扣比率轉換為百分比顯示</p>
     *
     * @param discountRate 折扣比率
     * @return 百分比顯示字串
     */
    private static String formatDiscountDisplay(BigDecimal discountRate) {
        if (discountRate == null) {
            return "100%";
        }
        int percent = discountRate.multiply(BigDecimal.valueOf(100)).intValue();
        return percent + "%";
    }
}
