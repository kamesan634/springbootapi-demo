package com.kamesan.erpapi.purchasing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 供應商資料傳輸物件
 *
 * <p>用於回傳供應商資訊給前端的 DTO（Data Transfer Object）。</p>
 *
 * <p>此 DTO 包含供應商的完整資訊，用於：</p>
 * <ul>
 *   <li>查詢供應商詳情</li>
 *   <li>列表顯示</li>
 *   <li>新增/更新後的回應資料</li>
 * </ul>
 *
 * <p>與 Entity 的差異：</p>
 * <ul>
 *   <li>不包含敏感或內部使用的欄位</li>
 *   <li>可根據前端需求調整欄位結構</li>
 *   <li>避免直接暴露 Entity 結構</li>
 * </ul>
 *
 * @author ERP System Team
 * @version 1.0.0
 * @see com.kamesan.erpapi.purchasing.entity.Supplier
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "供應商資料傳輸物件")
public class SupplierDto {

    /**
     * 供應商 ID
     *
     * <p>系統自動產生的唯一識別碼。</p>
     */
    @Schema(description = "供應商 ID", example = "1")
    private Long id;

    /**
     * 供應商代碼
     *
     * <p>供應商的唯一代碼，用於快速識別。</p>
     */
    @Schema(description = "供應商代碼", example = "SUP001")
    private String code;

    /**
     * 供應商名稱
     *
     * <p>供應商的公司或商號名稱。</p>
     */
    @Schema(description = "供應商名稱", example = "優質材料供應有限公司")
    private String name;

    /**
     * 聯絡人姓名
     *
     * <p>供應商的主要聯絡窗口。</p>
     */
    @Schema(description = "聯絡人姓名", example = "王小明")
    private String contactPerson;

    /**
     * 聯絡電話
     *
     * <p>供應商的聯絡電話。</p>
     */
    @Schema(description = "聯絡電話", example = "02-12345678")
    private String phone;

    /**
     * 電子郵件
     *
     * <p>供應商的電子郵件地址。</p>
     */
    @Schema(description = "電子郵件", example = "contact@supplier.com")
    private String email;

    /**
     * 地址
     *
     * <p>供應商的營業地址。</p>
     */
    @Schema(description = "地址", example = "台北市中山區民生東路100號")
    private String address;

    /**
     * 付款條件
     *
     * <p>與供應商約定的付款方式。</p>
     */
    @Schema(description = "付款條件", example = "月結30天")
    private String paymentTerms;

    /**
     * 是否啟用
     *
     * <p>供應商的啟用狀態。</p>
     */
    @Schema(description = "是否啟用", example = "true")
    private Boolean isActive;

    /**
     * 備註
     *
     * <p>供應商的額外說明。</p>
     */
    @Schema(description = "備註", example = "優質供應商，配合度高")
    private String notes;

    /**
     * 建立時間
     *
     * <p>供應商資料建立的時間。</p>
     */
    @Schema(description = "建立時間", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;

    /**
     * 更新時間
     *
     * <p>供應商資料最後更新的時間。</p>
     */
    @Schema(description = "更新時間", example = "2024-01-20T14:45:00")
    private LocalDateTime updatedAt;

    /**
     * 建立者 ID
     *
     * <p>建立此供應商資料的使用者 ID。</p>
     */
    @Schema(description = "建立者 ID", example = "1")
    private Long createdBy;

    /**
     * 更新者 ID
     *
     * <p>最後更新此供應商資料的使用者 ID。</p>
     */
    @Schema(description = "更新者 ID", example = "1")
    private Long updatedBy;
}
