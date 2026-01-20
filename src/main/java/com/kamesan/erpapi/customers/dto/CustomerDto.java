package com.kamesan.erpapi.customers.dto;

import com.kamesan.erpapi.customers.entity.Customer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 會員 DTO
 *
 * <p>用於回傳會員資訊給前端，包含：</p>
 * <ul>
 *   <li>基本資訊 - ID、會員編號、姓名、電話、Email、性別、生日</li>
 *   <li>等級資訊 - 等級 ID、代碼、名稱</li>
 *   <li>消費資訊 - 累積點數、累積消費金額</li>
 *   <li>狀態資訊 - 註冊日期、啟用狀態</li>
 *   <li>計算資訊 - 年齡、是否生日</li>
 * </ul>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "會員資訊")
public class CustomerDto {

    /**
     * 會員 ID
     */
    @Schema(description = "會員 ID", example = "1")
    private Long id;

    /**
     * 會員編號
     */
    @Schema(description = "會員編號", example = "M202501060001")
    private String memberNo;

    /**
     * 會員姓名
     */
    @Schema(description = "會員姓名", example = "王小明")
    private String name;

    /**
     * 手機號碼
     */
    @Schema(description = "手機號碼", example = "0912345678")
    private String phone;

    /**
     * 電子郵件
     */
    @Schema(description = "電子郵件", example = "customer@example.com")
    private String email;

    /**
     * 性別代碼
     */
    @Schema(description = "性別代碼（M/F/O）", example = "M")
    private String gender;

    /**
     * 性別顯示文字
     */
    @Schema(description = "性別顯示文字", example = "男")
    private String genderDisplay;

    /**
     * 生日
     */
    @Schema(description = "生日", example = "1990-01-15")
    private LocalDate birthday;

    /**
     * 年齡
     */
    @Schema(description = "年齡", example = "35")
    private Integer age;

    /**
     * 是否今日生日
     */
    @Schema(description = "是否今日生日", example = "false")
    private boolean birthdayToday;

    /**
     * 是否本月生日
     */
    @Schema(description = "是否本月生日", example = "true")
    private boolean birthdayMonth;

    /**
     * 會員等級資訊
     */
    @Schema(description = "會員等級資訊")
    private LevelInfo level;

    /**
     * 累積點數
     */
    @Schema(description = "累積點數", example = "1500")
    private Integer totalPoints;

    /**
     * 累積消費金額
     */
    @Schema(description = "累積消費金額", example = "25000.00")
    private BigDecimal totalSpent;

    /**
     * 註冊日期
     */
    @Schema(description = "註冊日期")
    private LocalDateTime registerDate;

    /**
     * 是否啟用
     */
    @Schema(description = "是否啟用", example = "true")
    private boolean active;

    /**
     * 備註
     */
    @Schema(description = "備註", example = "VIP 客戶，需優先服務")
    private String notes;

    /**
     * 地址
     */
    @Schema(description = "地址", example = "台北市中正區重慶南路一段100號")
    private String address;

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
     * 會員等級簡要資訊
     *
     * <p>包含等級的基本資訊，用於嵌入會員 DTO 中</p>
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "會員等級簡要資訊")
    public static class LevelInfo {

        /**
         * 等級 ID
         */
        @Schema(description = "等級 ID", example = "3")
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
         * 折扣比率
         */
        @Schema(description = "折扣比率", example = "0.90")
        private BigDecimal discountRate;

        /**
         * 點數倍率
         */
        @Schema(description = "點數倍率", example = "1.50")
        private BigDecimal pointsMultiplier;
    }

    /**
     * 從 Entity 轉換為 DTO
     *
     * @param entity 會員 Entity
     * @return 會員 DTO
     */
    public static CustomerDto fromEntity(Customer entity) {
        if (entity == null) {
            return null;
        }

        CustomerDtoBuilder builder = CustomerDto.builder()
                .id(entity.getId())
                .memberNo(entity.getMemberNo())
                .name(entity.getName())
                .phone(entity.getPhone())
                .email(entity.getEmail())
                .gender(entity.getGender())
                .genderDisplay(entity.getGenderDisplay())
                .birthday(entity.getBirthday())
                .age(entity.getAge())
                .birthdayToday(entity.isBirthdayToday())
                .birthdayMonth(entity.isBirthdayMonth())
                .totalPoints(entity.getTotalPoints())
                .totalSpent(entity.getTotalSpent())
                .registerDate(entity.getRegisterDate())
                .active(entity.isActive())
                .notes(entity.getNotes())
                .address(entity.getAddress())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt());

        // 設定等級資訊
        if (entity.getLevel() != null) {
            builder.level(LevelInfo.builder()
                    .id(entity.getLevel().getId())
                    .code(entity.getLevel().getCode())
                    .name(entity.getLevel().getName())
                    .discountRate(entity.getLevel().getDiscountRate())
                    .pointsMultiplier(entity.getLevel().getPointsMultiplier())
                    .build());
        }

        return builder.build();
    }

    /**
     * 從 Entity 轉換為簡要 DTO（不含敏感資訊）
     * <p>用於列表查詢等場景</p>
     *
     * @param entity 會員 Entity
     * @return 會員簡要 DTO
     */
    public static CustomerDto fromEntitySimple(Customer entity) {
        if (entity == null) {
            return null;
        }

        CustomerDtoBuilder builder = CustomerDto.builder()
                .id(entity.getId())
                .memberNo(entity.getMemberNo())
                .name(entity.getName())
                .phone(maskPhone(entity.getPhone()))
                .email(maskEmail(entity.getEmail()))
                .gender(entity.getGender())
                .genderDisplay(entity.getGenderDisplay())
                .totalPoints(entity.getTotalPoints())
                .totalSpent(entity.getTotalSpent())
                .active(entity.isActive());

        // 設定等級資訊
        if (entity.getLevel() != null) {
            builder.level(LevelInfo.builder()
                    .id(entity.getLevel().getId())
                    .code(entity.getLevel().getCode())
                    .name(entity.getLevel().getName())
                    .build());
        }

        return builder.build();
    }

    /**
     * 遮蔽手機號碼
     * <p>將中間四碼替換為 ****</p>
     *
     * @param phone 手機號碼
     * @return 遮蔽後的手機號碼
     */
    private static String maskPhone(String phone) {
        if (phone == null || phone.length() < 8) {
            return phone;
        }
        int start = 3;
        int end = phone.length() - 3;
        return phone.substring(0, start) + "****" + phone.substring(end);
    }

    /**
     * 遮蔽 Email
     * <p>將 @ 前面的部分只顯示前兩個字元</p>
     *
     * @param email Email
     * @return 遮蔽後的 Email
     */
    private static String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }
        int atIndex = email.indexOf("@");
        if (atIndex <= 2) {
            return email;
        }
        return email.substring(0, 2) + "***" + email.substring(atIndex);
    }
}
