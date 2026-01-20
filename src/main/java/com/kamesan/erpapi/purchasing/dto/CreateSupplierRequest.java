package com.kamesan.erpapi.purchasing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 新增供應商請求 DTO
 *
 * <p>用於接收新增供應商的請求資料。</p>
 *
 * <p>此 DTO 包含新增供應商所需的所有欄位，並使用 Bean Validation 進行資料驗證：</p>
 * <ul>
 *   <li>供應商代碼 - 必填，最多 50 字元</li>
 *   <li>供應商名稱 - 必填，最多 200 字元</li>
 *   <li>其他欄位 - 選填</li>
 * </ul>
 *
 * <p>驗證規則：</p>
 * <ul>
 *   <li>{@code @NotBlank} - 欄位不可為空白（null、空字串或僅包含空白）</li>
 *   <li>{@code @Size} - 限制字串長度</li>
 *   <li>{@code @Email} - 驗證電子郵件格式</li>
 * </ul>
 *
 * @author ERP System Team
 * @version 1.0.0
 * @see SupplierDto
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "新增供應商請求")
public class CreateSupplierRequest {

    /**
     * 供應商代碼
     *
     * <p>供應商的唯一代碼，用於系統識別。</p>
     * <p>建議格式：SUP + 流水號，例如：SUP001</p>
     * <p>此欄位為必填，最多 50 字元。</p>
     */
    @NotBlank(message = "供應商代碼不能為空")
    @Size(max = 50, message = "供應商代碼最多 50 字元")
    @Schema(description = "供應商代碼", example = "SUP001", requiredMode = Schema.RequiredMode.REQUIRED)
    private String code;

    /**
     * 供應商名稱
     *
     * <p>供應商的公司或商號名稱。</p>
     * <p>此欄位為必填，最多 200 字元。</p>
     */
    @NotBlank(message = "供應商名稱不能為空")
    @Size(max = 200, message = "供應商名稱最多 200 字元")
    @Schema(description = "供應商名稱", example = "優質材料供應有限公司", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    /**
     * 聯絡人姓名
     *
     * <p>供應商的主要聯絡窗口姓名。</p>
     * <p>此欄位為選填，最多 100 字元。</p>
     */
    @Size(max = 100, message = "聯絡人姓名最多 100 字元")
    @Schema(description = "聯絡人姓名", example = "王小明")
    private String contactPerson;

    /**
     * 聯絡電話
     *
     * <p>供應商的聯絡電話號碼。</p>
     * <p>此欄位為選填，最多 30 字元。</p>
     */
    @Size(max = 30, message = "聯絡電話最多 30 字元")
    @Schema(description = "聯絡電話", example = "02-12345678")
    private String phone;

    /**
     * 電子郵件
     *
     * <p>供應商的電子郵件地址。</p>
     * <p>此欄位為選填，需符合電子郵件格式，最多 100 字元。</p>
     */
    @Email(message = "電子郵件格式不正確")
    @Size(max = 100, message = "電子郵件最多 100 字元")
    @Schema(description = "電子郵件", example = "contact@supplier.com")
    private String email;

    /**
     * 地址
     *
     * <p>供應商的營業地址或寄送地址。</p>
     * <p>此欄位為選填，最多 500 字元。</p>
     */
    @Size(max = 500, message = "地址最多 500 字元")
    @Schema(description = "地址", example = "台北市中山區民生東路100號")
    private String address;

    /**
     * 付款條件
     *
     * <p>與供應商約定的付款方式和期限。</p>
     * <p>此欄位為選填，最多 200 字元。</p>
     * <p>常見的付款條件：</p>
     * <ul>
     *   <li>貨到付款（COD）</li>
     *   <li>月結30天（NET30）</li>
     *   <li>月結60天（NET60）</li>
     * </ul>
     */
    @Size(max = 200, message = "付款條件最多 200 字元")
    @Schema(description = "付款條件", example = "月結30天")
    private String paymentTerms;

    /**
     * 是否啟用
     *
     * <p>供應商的啟用狀態。</p>
     * <p>此欄位為選填，預設為 true（啟用）。</p>
     */
    @Schema(description = "是否啟用", example = "true", defaultValue = "true")
    private Boolean isActive;

    /**
     * 備註
     *
     * <p>供應商的額外說明或注意事項。</p>
     * <p>此欄位為選填，最多 1000 字元。</p>
     */
    @Size(max = 1000, message = "備註最多 1000 字元")
    @Schema(description = "備註", example = "優質供應商，配合度高")
    private String notes;
}
