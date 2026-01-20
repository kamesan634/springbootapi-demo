package com.kamesan.erpapi.customers.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 建立會員請求 DTO
 *
 * <p>用於接收前端建立會員的請求資料，包含驗證規則：</p>
 * <ul>
 *   <li>姓名 - 必填，2-100 字元</li>
 *   <li>手機 - 選填，需符合格式</li>
 *   <li>Email - 選填，需符合 Email 格式</li>
 *   <li>性別 - 選填，限定 M/F/O</li>
 *   <li>生日 - 選填，必須是過去的日期</li>
 *   <li>等級 ID - 選填，若未提供則使用預設等級</li>
 * </ul>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "建立會員請求")
public class CreateCustomerRequest {

    /**
     * 會員姓名
     * <p>必填欄位</p>
     */
    @NotBlank(message = "會員姓名不能為空")
    @Size(min = 2, max = 100, message = "會員姓名長度必須在 2-100 字元之間")
    @Schema(description = "會員姓名", example = "王小明", requiredMode = Schema.RequiredMode.REQUIRED)
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
     * <p>選填欄位，若未提供則使用預設等級（普通會員）</p>
     */
    @Schema(description = "會員等級 ID（選填，預設為普通會員）", example = "1")
    private Long levelId;

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
    @Schema(description = "備註", example = "VIP 客戶")
    private String notes;

    /**
     * 初始點數
     * <p>選填欄位，預設為 0</p>
     */
    @Min(value = 0, message = "初始點數不能為負數")
    @Schema(description = "初始點數（選填，預設為 0）", example = "100")
    private Integer initialPoints;
}
