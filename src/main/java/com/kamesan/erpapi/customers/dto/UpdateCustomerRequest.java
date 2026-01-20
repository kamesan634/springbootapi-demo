package com.kamesan.erpapi.customers.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 更新會員請求 DTO
 *
 * <p>用於接收前端更新會員的請求資料。</p>
 * <p>所有欄位皆為選填，只更新有提供值的欄位。</p>
 *
 * <h2>驗證規則：</h2>
 * <ul>
 *   <li>姓名 - 若提供，需 2-100 字元</li>
 *   <li>手機 - 若提供，需符合格式</li>
 *   <li>Email - 若提供，需符合 Email 格式</li>
 *   <li>性別 - 若提供，限定 M/F/O</li>
 *   <li>生日 - 若提供，必須是過去的日期</li>
 * </ul>
 *
 * <h2>注意事項：</h2>
 * <ul>
 *   <li>會員編號不可修改</li>
 *   <li>累積點數和消費金額不可直接修改（需透過專用 API）</li>
 *   <li>等級變更需謹慎，可能影響會員權益</li>
 * </ul>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "更新會員請求")
public class UpdateCustomerRequest {

    /**
     * 會員姓名
     * <p>選填欄位，若提供則更新</p>
     */
    @Size(min = 2, max = 100, message = "會員姓名長度必須在 2-100 字元之間")
    @Schema(description = "會員姓名", example = "王小明")
    private String name;

    /**
     * 手機號碼
     * <p>選填欄位，需符合台灣手機號碼格式</p>
     */
    @Pattern(regexp = "^09\\d{8}$", message = "手機號碼格式不正確（應為 09 開頭的 10 位數字）")
    @Schema(description = "手機號碼", example = "0912345678")
    private String phone;

    /**
     * 電子郵件
     * <p>選填欄位，需符合 Email 格式</p>
     */
    @Email(message = "Email 格式不正確")
    @Size(max = 100, message = "Email 長度不能超過 100 字元")
    @Schema(description = "電子郵件", example = "customer@example.com")
    private String email;

    /**
     * 性別
     * <p>選填欄位，限定 M（男）、F（女）、O（其他）</p>
     */
    @Pattern(regexp = "^[MFO]$", message = "性別必須是 M（男）、F（女）或 O（其他）")
    @Schema(description = "性別（M/F/O）", example = "M")
    private String gender;

    /**
     * 生日
     * <p>選填欄位，必須是過去的日期</p>
     */
    @Past(message = "生日必須是過去的日期")
    @Schema(description = "生日", example = "1990-01-15")
    private LocalDate birthday;

    /**
     * 會員等級 ID
     * <p>選填欄位，變更等級需謹慎</p>
     */
    @Schema(description = "會員等級 ID", example = "2")
    private Long levelId;

    /**
     * 是否啟用
     * <p>選填欄位，停用會員將無法進行消費</p>
     */
    @Schema(description = "是否啟用", example = "true")
    private Boolean active;

    /**
     * 地址
     * <p>選填欄位</p>
     */
    @Size(max = 255, message = "地址長度不能超過 255 字元")
    @Schema(description = "地址", example = "台北市中正區重慶南路一段100號")
    private String address;

    /**
     * 備註
     * <p>選填欄位</p>
     */
    @Size(max = 500, message = "備註長度不能超過 500 字元")
    @Schema(description = "備註", example = "VIP 客戶，需優先服務")
    private String notes;

    /**
     * 檢查是否有任何欄位需要更新
     *
     * @return 是否有欄位需要更新
     */
    public boolean hasAnyFieldToUpdate() {
        return name != null || phone != null || email != null ||
                gender != null || birthday != null || levelId != null ||
                active != null || address != null || notes != null;
    }
}
