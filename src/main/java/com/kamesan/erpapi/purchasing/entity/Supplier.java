package com.kamesan.erpapi.purchasing.entity;

import com.kamesan.erpapi.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * 供應商實體
 *
 * <p>供應商是企業採購原物料或商品的合作夥伴。</p>
 * <p>此實體記錄供應商的基本資訊，包括：</p>
 * <ul>
 *   <li>供應商代碼 - 唯一識別碼，用於快速查詢</li>
 *   <li>供應商名稱 - 公司或商號名稱</li>
 *   <li>聯絡人資訊 - 包含聯絡人姓名、電話、Email</li>
 *   <li>地址 - 供應商營業地址</li>
 *   <li>付款條件 - 約定的付款方式（如：月結30天、貨到付款等）</li>
 *   <li>啟用狀態 - 控制供應商是否可用於採購</li>
 * </ul>
 *
 * <p>繼承 {@link BaseEntity} 以獲得通用欄位（id、建立時間、更新時間等）。</p>
 *
 * @author ERP System Team
 * @version 1.0.0
 * @see BaseEntity
 */
@Entity
@Table(name = "suppliers", indexes = {
        @Index(name = "idx_supplier_code", columnList = "code"),
        @Index(name = "idx_supplier_name", columnList = "name"),
        @Index(name = "idx_supplier_is_active", columnList = "is_active")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Supplier extends BaseEntity {

    /**
     * 供應商代碼
     *
     * <p>唯一識別碼，用於系統內部快速查詢和識別供應商。</p>
     * <p>建議格式：SUP + 流水號，例如：SUP001、SUP002</p>
     * <p>此欄位為必填且不可重複。</p>
     */
    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    /**
     * 供應商名稱
     *
     * <p>供應商的公司名稱或商號名稱。</p>
     * <p>此欄位為必填，用於顯示和報表。</p>
     */
    @Column(name = "name", nullable = false, length = 200)
    private String name;

    /**
     * 聯絡人姓名
     *
     * <p>供應商的主要聯絡窗口姓名。</p>
     * <p>當需要聯繫供應商時，可透過此聯絡人進行溝通。</p>
     */
    @Column(name = "contact_person", length = 100)
    private String contactPerson;

    /**
     * 聯絡電話
     *
     * <p>供應商的聯絡電話號碼。</p>
     * <p>可填寫市話或手機號碼。</p>
     */
    @Column(name = "phone", length = 30)
    private String phone;

    /**
     * 電子郵件
     *
     * <p>供應商的電子郵件地址。</p>
     * <p>用於寄送採購單、對帳單等文件。</p>
     */
    @Column(name = "email", length = 100)
    private String email;

    /**
     * 地址
     *
     * <p>供應商的營業地址或寄送地址。</p>
     * <p>可用於貨物退貨、拜訪等用途。</p>
     */
    @Column(name = "address", length = 500)
    private String address;

    /**
     * 付款條件
     *
     * <p>與供應商約定的付款方式和期限。</p>
     * <p>常見的付款條件包括：</p>
     * <ul>
     *   <li>貨到付款（COD）</li>
     *   <li>月結30天（NET30）</li>
     *   <li>月結60天（NET60）</li>
     *   <li>預付款</li>
     * </ul>
     */
    @Column(name = "payment_terms", length = 200)
    private String paymentTerms;

    /**
     * 是否啟用
     *
     * <p>控制供應商是否可用於新的採購訂單。</p>
     * <p>當設為 false 時：</p>
     * <ul>
     *   <li>供應商不會出現在採購選單中</li>
     *   <li>無法建立新的採購訂單給此供應商</li>
     *   <li>歷史採購記錄仍可查詢</li>
     * </ul>
     * <p>預設值為 true（啟用）。</p>
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    /**
     * 備註
     *
     * <p>供應商的額外說明或注意事項。</p>
     * <p>可記錄特殊要求、合作歷史等資訊。</p>
     */
    @Column(name = "notes", length = 1000)
    private String notes;
}
