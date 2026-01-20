package com.kamesan.erpapi.promotions.dto;

import com.kamesan.erpapi.promotions.entity.DiscountType;
import com.kamesan.erpapi.promotions.entity.Promotion;
import com.kamesan.erpapi.promotions.entity.PromotionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 促銷活動回應 DTO
 *
 * <p>用於 API 回應的促銷活動資料傳輸物件，包含促銷活動的完整資訊。</p>
 *
 * <p>包含欄位：</p>
 * <ul>
 *   <li>基本資訊：ID、名稱、描述</li>
 *   <li>促銷設定：促銷類型、折扣類型、折扣值</li>
 *   <li>時間設定：開始日期、結束日期</li>
 *   <li>狀態資訊：是否啟用、是否進行中、是否即將開始、是否已結束</li>
 *   <li>審計資訊：建立時間、更新時間</li>
 * </ul>
 *
 * @author ERP System Team
 * @version 1.0.0
 * @see Promotion 促銷活動實體
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "促銷活動回應")
public class PromotionDto {

    /**
     * 促銷活動 ID
     */
    @Schema(description = "促銷活動 ID", example = "1")
    private Long id;

    /**
     * 促銷活動名稱
     */
    @Schema(description = "促銷活動名稱", example = "週年慶大特價")
    private String name;

    /**
     * 促銷活動描述
     */
    @Schema(description = "促銷活動描述", example = "全館商品8折優惠，限時一週")
    private String description;

    /**
     * 促銷活動類型
     */
    @Schema(description = "促銷活動類型", example = "DISCOUNT")
    private PromotionType type;

    /**
     * 促銷活動類型描述
     */
    @Schema(description = "促銷活動類型描述", example = "折扣活動")
    private String typeDescription;

    /**
     * 折扣類型
     */
    @Schema(description = "折扣類型", example = "PERCENTAGE")
    private DiscountType discountType;

    /**
     * 折扣類型描述
     */
    @Schema(description = "折扣類型描述", example = "百分比折扣")
    private String discountTypeDescription;

    /**
     * 折扣值
     */
    @Schema(description = "折扣值（百分比折扣時為折扣百分比，固定金額時為折扣金額）", example = "20")
    private BigDecimal discountValue;

    /**
     * 開始日期時間
     */
    @Schema(description = "開始日期時間", example = "2024-01-01T00:00:00")
    private LocalDateTime startDate;

    /**
     * 結束日期時間
     */
    @Schema(description = "結束日期時間", example = "2024-01-31T23:59:59")
    private LocalDateTime endDate;

    /**
     * 是否啟用
     */
    @Schema(description = "是否啟用", example = "true")
    private boolean active;

    /**
     * 是否正在進行中
     */
    @Schema(description = "是否正在進行中", example = "true")
    private boolean ongoing;

    /**
     * 是否即將開始
     */
    @Schema(description = "是否即將開始", example = "false")
    private boolean upcoming;

    /**
     * 是否已結束
     */
    @Schema(description = "是否已結束", example = "false")
    private boolean expired;

    /**
     * 建立時間
     */
    @Schema(description = "建立時間", example = "2024-01-01T10:00:00")
    private LocalDateTime createdAt;

    /**
     * 更新時間
     */
    @Schema(description = "更新時間", example = "2024-01-01T10:00:00")
    private LocalDateTime updatedAt;

    /**
     * 從促銷活動實體建立 DTO
     *
     * <p>將促銷活動實體轉換為 DTO，並計算各種狀態資訊。</p>
     *
     * @param promotion 促銷活動實體
     * @return 促銷活動 DTO
     */
    public static PromotionDto fromEntity(Promotion promotion) {
        if (promotion == null) {
            return null;
        }

        return PromotionDto.builder()
                .id(promotion.getId())
                .name(promotion.getName())
                .description(promotion.getDescription())
                .type(promotion.getType())
                .typeDescription(promotion.getType() != null ? promotion.getType().getDescription() : null)
                .discountType(promotion.getDiscountType())
                .discountTypeDescription(promotion.getDiscountType() != null ? promotion.getDiscountType().getDescription() : null)
                .discountValue(promotion.getDiscountValue())
                .startDate(promotion.getStartDate())
                .endDate(promotion.getEndDate())
                .active(promotion.isActive())
                .ongoing(promotion.isOngoing())
                .upcoming(promotion.isUpcoming())
                .expired(promotion.isExpired())
                .createdAt(promotion.getCreatedAt())
                .updatedAt(promotion.getUpdatedAt())
                .build();
    }
}
