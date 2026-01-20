package com.kamesan.erpapi.customers.entity;

import com.kamesan.erpapi.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 會員實體
 *
 * <p>儲存會員的基本資料和消費相關資訊，包括：</p>
 * <ul>
 *   <li>基本資料 - 會員編號、姓名、電話、Email、性別、生日</li>
 *   <li>等級資訊 - 所屬會員等級</li>
 *   <li>消費資訊 - 累積點數、累積消費金額</li>
 *   <li>狀態資訊 - 註冊日期、啟用狀態</li>
 * </ul>
 *
 * <h2>會員編號規則：</h2>
 * <p>格式：M + 年月日 + 4位流水號，例如：M202501060001</p>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Entity
@Table(name = "customers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer extends BaseEntity {

    /**
     * 會員編號
     * <p>唯一識別碼，格式：M + 年月日 + 4位流水號</p>
     * <p>例如：M202501060001</p>
     */
    @Column(name = "member_no", nullable = false, unique = true, length = 20)
    private String memberNo;

    /**
     * 會員姓名
     */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /**
     * 手機號碼
     * <p>主要聯絡方式，可用於簡訊通知</p>
     */
    @Column(name = "phone", length = 20)
    private String phone;

    /**
     * 電子郵件
     * <p>可用於電子報和促銷訊息發送</p>
     */
    @Column(name = "email", length = 100)
    private String email;

    /**
     * 性別
     * <p>M: 男性, F: 女性, O: 其他</p>
     */
    @Column(name = "gender", length = 1)
    private String gender;

    /**
     * 生日
     * <p>用於生日優惠和行銷活動</p>
     */
    @Column(name = "birthday")
    private LocalDate birthday;

    /**
     * 會員等級
     * <p>多對一關聯，每個會員屬於一個等級</p>
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "level_id", nullable = false)
    private CustomerLevel level;

    /**
     * 累積點數
     * <p>目前可用的點數餘額</p>
     */
    @Column(name = "total_points", nullable = false)
    @Builder.Default
    private Integer totalPoints = 0;

    /**
     * 累積消費金額
     * <p>歷史消費總金額，用於會員等級升級判斷</p>
     */
    @Column(name = "total_spent", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal totalSpent = BigDecimal.ZERO;

    /**
     * 註冊日期
     * <p>會員加入的日期</p>
     */
    @Column(name = "register_date", nullable = false)
    @Builder.Default
    private LocalDateTime registerDate = LocalDateTime.now();

    /**
     * 是否啟用
     * <p>停用的會員無法進行消費和點數操作</p>
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;

    /**
     * 備註
     * <p>會員的特殊備註事項</p>
     */
    @Column(name = "notes", length = 500)
    private String notes;

    /**
     * 地址
     * <p>會員的收件地址</p>
     */
    @Column(name = "address", length = 255)
    private String address;

    /**
     * 增加點數
     *
     * @param points 要增加的點數
     * @throws IllegalArgumentException 如果點數為負數
     */
    public void addPoints(int points) {
        if (points < 0) {
            throw new IllegalArgumentException("增加的點數不能為負數");
        }
        this.totalPoints = (this.totalPoints == null ? 0 : this.totalPoints) + points;
    }

    /**
     * 扣除點數
     *
     * @param points 要扣除的點數
     * @throws IllegalArgumentException 如果點數為負數或餘額不足
     */
    public void deductPoints(int points) {
        if (points < 0) {
            throw new IllegalArgumentException("扣除的點數不能為負數");
        }
        int currentPoints = this.totalPoints == null ? 0 : this.totalPoints;
        if (currentPoints < points) {
            throw new IllegalArgumentException("點數餘額不足");
        }
        this.totalPoints = currentPoints - points;
    }

    /**
     * 檢查點數是否足夠
     *
     * @param points 需要的點數
     * @return 是否足夠
     */
    public boolean hasEnoughPoints(int points) {
        int currentPoints = this.totalPoints == null ? 0 : this.totalPoints;
        return currentPoints >= points;
    }

    /**
     * 增加消費金額
     *
     * @param amount 消費金額
     * @throws IllegalArgumentException 如果金額為負數
     */
    public void addSpent(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("消費金額不能為負數");
        }
        this.totalSpent = (this.totalSpent == null ? BigDecimal.ZERO : this.totalSpent).add(amount);
    }

    /**
     * 判斷今天是否是會員生日
     *
     * @return 是否生日
     */
    public boolean isBirthdayToday() {
        if (birthday == null) {
            return false;
        }
        LocalDate today = LocalDate.now();
        return birthday.getMonthValue() == today.getMonthValue()
                && birthday.getDayOfMonth() == today.getDayOfMonth();
    }

    /**
     * 判斷本月是否是會員生日月
     *
     * @return 是否生日月
     */
    public boolean isBirthdayMonth() {
        if (birthday == null) {
            return false;
        }
        return birthday.getMonthValue() == LocalDate.now().getMonthValue();
    }

    /**
     * 計算會員年齡
     *
     * @return 年齡，若無生日資料則返回 null
     */
    public Integer getAge() {
        if (birthday == null) {
            return null;
        }
        LocalDate today = LocalDate.now();
        int age = today.getYear() - birthday.getYear();
        if (today.getMonthValue() < birthday.getMonthValue()
                || (today.getMonthValue() == birthday.getMonthValue()
                    && today.getDayOfMonth() < birthday.getDayOfMonth())) {
            age--;
        }
        return age;
    }

    /**
     * 取得性別顯示文字
     *
     * @return 性別文字（男/女/其他/未設定）
     */
    public String getGenderDisplay() {
        if (gender == null) {
            return "未設定";
        }
        return switch (gender.toUpperCase()) {
            case "M" -> "男";
            case "F" -> "女";
            case "O" -> "其他";
            default -> "未設定";
        };
    }
}
