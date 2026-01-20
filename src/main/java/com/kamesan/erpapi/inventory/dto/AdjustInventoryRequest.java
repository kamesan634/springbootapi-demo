package com.kamesan.erpapi.inventory.dto;

import com.kamesan.erpapi.inventory.entity.MovementType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 庫存調整請求 DTO
 *
 * <p>用於庫存盤點調整操作的請求資料，支援盤盈入庫和盤虧出庫。</p>
 *
 * <p>使用場景：</p>
 * <ul>
 *   <li>庫存盤點發現實際數量與帳面不符</li>
 *   <li>庫存人工調整（需記錄原因）</li>
 *   <li>損耗報廢處理</li>
 * </ul>
 *
 * <p>注意事項：</p>
 * <ul>
 *   <li>adjustmentType 只能是 ADJUST_IN（盤盈）或 ADJUST_OUT（盤虧）</li>
 *   <li>quantity 必須為正整數</li>
 *   <li>reason 為必填欄位，需說明調整原因</li>
 * </ul>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "庫存調整請求")
public class AdjustInventoryRequest {

    /**
     * 商品 ID
     * <p>要調整庫存的商品</p>
     */
    @NotNull(message = "商品 ID 不能為空")
    @Schema(description = "商品 ID", example = "100", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long productId;

    /**
     * 倉庫 ID
     * <p>要調整庫存的倉庫</p>
     */
    @NotNull(message = "倉庫 ID 不能為空")
    @Schema(description = "倉庫 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long warehouseId;

    /**
     * 調整類型
     * <p>只能是 ADJUST_IN（盤盈入庫）或 ADJUST_OUT（盤虧出庫）</p>
     */
    @NotNull(message = "調整類型不能為空")
    @Schema(description = "調整類型（ADJUST_IN: 盤盈入庫, ADJUST_OUT: 盤虧出庫）",
            example = "ADJUST_IN",
            requiredMode = Schema.RequiredMode.REQUIRED,
            allowableValues = {"ADJUST_IN", "ADJUST_OUT"})
    private MovementType adjustmentType;

    /**
     * 調整數量
     * <p>要調整的數量，必須為正整數</p>
     */
    @NotNull(message = "調整數量不能為空")
    @Min(value = 1, message = "調整數量必須大於 0")
    @Schema(description = "調整數量（正整數）", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer quantity;

    /**
     * 調整原因
     * <p>必填欄位，需說明調整的原因</p>
     */
    @NotNull(message = "調整原因不能為空")
    @Size(min = 2, max = 500, message = "調整原因長度必須在 2-500 字之間")
    @Schema(description = "調整原因",
            example = "盤點發現實際數量多於帳面數量",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String reason;

    /**
     * 參考單號
     * <p>可選欄位，關聯的盤點單或其他單據編號</p>
     */
    @Size(max = 50, message = "參考單號長度不能超過 50 字")
    @Schema(description = "參考單號", example = "ADJ-20240115-001")
    private String referenceNo;

    /**
     * 驗證調整類型是否有效
     *
     * <p>只允許 ADJUST_IN 和 ADJUST_OUT 兩種類型</p>
     *
     * @return 如果調整類型有效返回 true
     */
    public boolean isValidAdjustmentType() {
        return adjustmentType == MovementType.ADJUST_IN ||
                adjustmentType == MovementType.ADJUST_OUT;
    }

    /**
     * 判斷是否為盤盈調整
     *
     * @return 如果是盤盈（增加庫存）返回 true
     */
    public boolean isAdjustIn() {
        return adjustmentType == MovementType.ADJUST_IN;
    }

    /**
     * 判斷是否為盤虧調整
     *
     * @return 如果是盤虧（減少庫存）返回 true
     */
    public boolean isAdjustOut() {
        return adjustmentType == MovementType.ADJUST_OUT;
    }

    /**
     * 取得調整類型的中文描述
     *
     * @return 調整類型中文描述
     */
    public String getAdjustmentTypeDescription() {
        return adjustmentType != null ? adjustmentType.getDescription() : null;
    }
}
