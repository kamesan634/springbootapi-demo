package com.kamesan.erpapi.promotions.dto;

import com.kamesan.erpapi.promotions.entity.DiscountType;
import com.kamesan.erpapi.promotions.entity.Promotion;
import com.kamesan.erpapi.promotions.entity.PromotionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 建立促銷活動請求 DTO
 *
 * <p>用於接收建立或更新促銷活動的 API 請求資料。</p>
 *
 * <p>驗證規則：</p>
 * <ul>
 *   <li>名稱：必填，長度 2-100 字元</li>
 *   <li>描述：選填，最大 500 字元</li>
 *   <li>促銷類型：必填</li>
 *   <li>折扣類型：必填</li>
 *   <li>折扣值：必填，必須大於 0</li>
 *   <li>開始日期：必填</li>
 *   <li>結束日期：必填，必須在開始日期之後</li>
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
@Schema(description = "建立促銷活動請求")
public class CreatePromotionRequest {

    /**
     * 促銷活動名稱
     * <p>必填，長度 2-100 字元</p>
     */
    @NotBlank(message = "促銷活動名稱不能為空")
    @Size(min = 2, max = 100, message = "促銷活動名稱長度必須在 2-100 字元之間")
    @Schema(description = "促銷活動名稱", example = "週年慶大特價", required = true)
    private String name;

    /**
     * 促銷活動描述
     * <p>選填，最大 500 字元</p>
     */
    @Size(max = 500, message = "促銷活動描述最多 500 字元")
    @Schema(description = "促銷活動描述", example = "全館商品8折優惠，限時一週")
    private String description;

    /**
     * 促銷活動類型
     * <p>必填，可選值：DISCOUNT、BUY_X_GET_Y、BUNDLE</p>
     */
    @NotNull(message = "促銷活動類型不能為空")
    @Schema(description = "促銷活動類型", example = "DISCOUNT", required = true)
    private PromotionType type;

    /**
     * 折扣類型
     * <p>必填，可選值：PERCENTAGE、FIXED_AMOUNT</p>
     */
    @NotNull(message = "折扣類型不能為空")
    @Schema(description = "折扣類型", example = "PERCENTAGE", required = true)
    private DiscountType discountType;

    /**
     * 折扣值
     * <p>必填，必須大於 0</p>
     * <p>百分比折扣時為折扣百分比（例如：20 表示減 20%）</p>
     * <p>固定金額折扣時為折扣金額（例如：100 表示減 100 元）</p>
     */
    @NotNull(message = "折扣值不能為空")
    @DecimalMin(value = "0.01", message = "折扣值必須大於 0")
    @Digits(integer = 8, fraction = 2, message = "折扣值格式不正確，最多 8 位整數和 2 位小數")
    @Schema(description = "折扣值（百分比折扣時為折扣百分比，固定金額時為折扣金額）", example = "20", required = true)
    private BigDecimal discountValue;

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
     * 是否啟用
     * <p>預設為 true</p>
     */
    @Schema(description = "是否啟用", example = "true")
    @Builder.Default
    private Boolean active = true;

    /**
     * 轉換為促銷活動實體
     *
     * <p>將請求 DTO 轉換為促銷活動實體，用於建立新的促銷活動。</p>
     *
     * @return 促銷活動實體
     */
    public Promotion toEntity() {
        return Promotion.builder()
                .name(this.name)
                .description(this.description)
                .type(this.type)
                .discountType(this.discountType)
                .discountValue(this.discountValue)
                .startDate(this.startDate)
                .endDate(this.endDate)
                .active(this.active != null ? this.active : true)
                .build();
    }

    /**
     * 更新促銷活動實體
     *
     * <p>將請求 DTO 的資料更新到現有的促銷活動實體。</p>
     *
     * @param promotion 要更新的促銷活動實體
     */
    public void updateEntity(Promotion promotion) {
        promotion.setName(this.name);
        promotion.setDescription(this.description);
        promotion.setType(this.type);
        promotion.setDiscountType(this.discountType);
        promotion.setDiscountValue(this.discountValue);
        promotion.setStartDate(this.startDate);
        promotion.setEndDate(this.endDate);
        if (this.active != null) {
            promotion.setActive(this.active);
        }
    }
}
