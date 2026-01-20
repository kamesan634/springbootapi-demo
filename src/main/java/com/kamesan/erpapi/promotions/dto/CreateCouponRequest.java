package com.kamesan.erpapi.promotions.dto;

import com.kamesan.erpapi.promotions.entity.Coupon;
import com.kamesan.erpapi.promotions.entity.DiscountType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 建立優惠券請求 DTO
 *
 * <p>用於接收建立或更新優惠券的 API 請求資料。</p>
 *
 * <p>驗證規則：</p>
 * <ul>
 *   <li>優惠碼：必填，長度 2-50 字元，僅允許英文字母、數字和底線</li>
 *   <li>名稱：必填，長度 2-100 字元</li>
 *   <li>折扣類型：必填</li>
 *   <li>折扣值：必填，必須大於 0</li>
 *   <li>最低訂單金額：選填，若有設定必須大於等於 0</li>
 *   <li>開始日期：必填</li>
 *   <li>結束日期：必填，必須在開始日期之後</li>
 *   <li>最大使用次數：選填，若有設定必須大於 0</li>
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
@Schema(description = "建立優惠券請求")
public class CreateCouponRequest {

    /**
     * 優惠碼
     * <p>必填，長度 2-50 字元</p>
     * <p>僅允許英文字母、數字和底線，將自動轉換為大寫</p>
     */
    @NotBlank(message = "優惠碼不能為空")
    @Size(min = 2, max = 50, message = "優惠碼長度必須在 2-50 字元之間")
    @Pattern(regexp = "^[A-Za-z0-9_]+$", message = "優惠碼只能包含英文字母、數字和底線")
    @Schema(description = "優惠碼（僅允許英文字母、數字和底線）", example = "SAVE100", required = true)
    private String code;

    /**
     * 優惠券名稱
     * <p>必填，長度 2-100 字元</p>
     */
    @NotBlank(message = "優惠券名稱不能為空")
    @Size(min = 2, max = 100, message = "優惠券名稱長度必須在 2-100 字元之間")
    @Schema(description = "優惠券名稱", example = "滿500減100優惠券", required = true)
    private String name;

    /**
     * 折扣類型
     * <p>必填，可選值：PERCENTAGE、FIXED_AMOUNT</p>
     */
    @NotNull(message = "折扣類型不能為空")
    @Schema(description = "折扣類型", example = "FIXED_AMOUNT", required = true)
    private DiscountType discountType;

    /**
     * 折扣值
     * <p>必填，必須大於 0</p>
     * <p>百分比折扣時為折扣百分比（例如：15 表示減 15%）</p>
     * <p>固定金額折扣時為折扣金額（例如：100 表示減 100 元）</p>
     */
    @NotNull(message = "折扣值不能為空")
    @DecimalMin(value = "0.01", message = "折扣值必須大於 0")
    @Digits(integer = 8, fraction = 2, message = "折扣值格式不正確，最多 8 位整數和 2 位小數")
    @Schema(description = "折扣值（百分比折扣時為折扣百分比，固定金額時為折扣金額）", example = "100", required = true)
    private BigDecimal discountValue;

    /**
     * 最低訂單金額
     * <p>選填，若有設定必須大於等於 0</p>
     * <p>訂單金額必須達到此門檻才能使用優惠券</p>
     */
    @DecimalMin(value = "0", message = "最低訂單金額不能為負數")
    @Digits(integer = 8, fraction = 2, message = "最低訂單金額格式不正確，最多 8 位整數和 2 位小數")
    @Schema(description = "最低訂單金額（達到此金額才能使用優惠券）", example = "500")
    private BigDecimal minOrderAmount;

    /**
     * 開始日期時間
     * <p>必填</p>
     */
    @NotNull(message = "開始日期不能為空")
    @Schema(description = "開始日期時間", example = "2024-01-01T00:00:00", required = true)
    private LocalDateTime startDate;

    /**
     * 結束日期時間
     * <p>必填</p>
     */
    @NotNull(message = "結束日期不能為空")
    @Schema(description = "結束日期時間", example = "2024-01-31T23:59:59", required = true)
    private LocalDateTime endDate;

    /**
     * 最大使用次數
     * <p>選填，若有設定必須大於 0</p>
     * <p>若不設定則表示無使用次數限制</p>
     */
    @Min(value = 1, message = "最大使用次數必須大於 0")
    @Schema(description = "最大使用次數（不設定則無限制）", example = "1000")
    private Integer maxUses;

    /**
     * 是否啟用
     * <p>預設為 true</p>
     */
    @Schema(description = "是否啟用", example = "true")
    @Builder.Default
    private Boolean active = true;

    /**
     * 轉換為優惠券實體
     *
     * <p>將請求 DTO 轉換為優惠券實體，用於建立新的優惠券。</p>
     * <p>優惠碼會自動轉換為大寫。</p>
     *
     * @return 優惠券實體
     */
    public Coupon toEntity() {
        return Coupon.builder()
                .code(this.code != null ? this.code.toUpperCase() : null)
                .name(this.name)
                .discountType(this.discountType)
                .discountValue(this.discountValue)
                .minOrderAmount(this.minOrderAmount)
                .startDate(this.startDate)
                .endDate(this.endDate)
                .maxUses(this.maxUses)
                .usedCount(0)
                .active(this.active != null ? this.active : true)
                .build();
    }

    /**
     * 更新優惠券實體
     *
     * <p>將請求 DTO 的資料更新到現有的優惠券實體。</p>
     * <p>優惠碼會自動轉換為大寫。</p>
     *
     * @param coupon 要更新的優惠券實體
     */
    public void updateEntity(Coupon coupon) {
        if (this.code != null) {
            coupon.setCode(this.code.toUpperCase());
        }
        coupon.setName(this.name);
        coupon.setDiscountType(this.discountType);
        coupon.setDiscountValue(this.discountValue);
        coupon.setMinOrderAmount(this.minOrderAmount);
        coupon.setStartDate(this.startDate);
        coupon.setEndDate(this.endDate);
        coupon.setMaxUses(this.maxUses);
        if (this.active != null) {
            coupon.setActive(this.active);
        }
    }
}
