package com.kamesan.erpapi.promotions.dto;

import com.kamesan.erpapi.promotions.entity.Coupon;
import com.kamesan.erpapi.promotions.entity.DiscountType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 優惠券回應 DTO
 *
 * <p>用於 API 回應的優惠券資料傳輸物件，包含優惠券的完整資訊。</p>
 *
 * <p>包含欄位：</p>
 * <ul>
 *   <li>基本資訊：ID、優惠碼、名稱</li>
 *   <li>折扣設定：折扣類型、折扣值、最低訂單金額</li>
 *   <li>時間設定：開始日期、結束日期</li>
 *   <li>使用限制：最大使用次數、已使用次數、剩餘可用次數</li>
 *   <li>狀態資訊：是否啟用、是否有效、是否即將生效、是否已過期、是否已用完</li>
 *   <li>審計資訊：建立時間、更新時間</li>
 * </ul>
 *
 * @author ERP System Team
 * @version 1.0.0
 * @see Coupon 優惠券實體
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "優惠券回應")
public class CouponDto {

    /**
     * 優惠券 ID
     */
    @Schema(description = "優惠券 ID", example = "1")
    private Long id;

    /**
     * 優惠碼
     */
    @Schema(description = "優惠碼", example = "SAVE100")
    private String code;

    /**
     * 優惠券名稱
     */
    @Schema(description = "優惠券名稱", example = "滿500減100優惠券")
    private String name;

    /**
     * 折扣類型
     */
    @Schema(description = "折扣類型", example = "FIXED_AMOUNT")
    private DiscountType discountType;

    /**
     * 折扣類型描述
     */
    @Schema(description = "折扣類型描述", example = "固定金額折扣")
    private String discountTypeDescription;

    /**
     * 折扣值
     */
    @Schema(description = "折扣值（百分比折扣時為折扣百分比，固定金額時為折扣金額）", example = "100")
    private BigDecimal discountValue;

    /**
     * 最低訂單金額
     */
    @Schema(description = "最低訂單金額", example = "500")
    private BigDecimal minOrderAmount;

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
     * 最大使用次數
     */
    @Schema(description = "最大使用次數", example = "1000")
    private Integer maxUses;

    /**
     * 已使用次數
     */
    @Schema(description = "已使用次數", example = "150")
    private Integer usedCount;

    /**
     * 剩餘可用次數
     */
    @Schema(description = "剩餘可用次數（若無使用次數限制則為 null）", example = "850")
    private Integer remainingUses;

    /**
     * 是否啟用
     */
    @Schema(description = "是否啟用", example = "true")
    private boolean active;

    /**
     * 是否有效（可使用）
     */
    @Schema(description = "是否有效（可使用）", example = "true")
    private boolean valid;

    /**
     * 是否即將生效
     */
    @Schema(description = "是否即將生效", example = "false")
    private boolean upcoming;

    /**
     * 是否已過期
     */
    @Schema(description = "是否已過期", example = "false")
    private boolean expired;

    /**
     * 是否已用完
     */
    @Schema(description = "是否已用完", example = "false")
    private boolean exhausted;

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
     * 從優惠券實體建立 DTO
     *
     * <p>將優惠券實體轉換為 DTO，並計算各種狀態資訊。</p>
     *
     * @param coupon 優惠券實體
     * @return 優惠券 DTO
     */
    public static CouponDto fromEntity(Coupon coupon) {
        if (coupon == null) {
            return null;
        }

        return CouponDto.builder()
                .id(coupon.getId())
                .code(coupon.getCode())
                .name(coupon.getName())
                .discountType(coupon.getDiscountType())
                .discountTypeDescription(coupon.getDiscountType() != null ? coupon.getDiscountType().getDescription() : null)
                .discountValue(coupon.getDiscountValue())
                .minOrderAmount(coupon.getMinOrderAmount())
                .startDate(coupon.getStartDate())
                .endDate(coupon.getEndDate())
                .maxUses(coupon.getMaxUses())
                .usedCount(coupon.getUsedCount())
                .remainingUses(coupon.getRemainingUses())
                .active(coupon.isActive())
                .valid(coupon.isValid())
                .upcoming(coupon.isUpcoming())
                .expired(coupon.isExpired())
                .exhausted(coupon.isExhausted())
                .createdAt(coupon.getCreatedAt())
                .updatedAt(coupon.getUpdatedAt())
                .build();
    }
}
