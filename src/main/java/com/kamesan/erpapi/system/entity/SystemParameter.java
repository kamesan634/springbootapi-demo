package com.kamesan.erpapi.system.entity;

import com.kamesan.erpapi.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * 系統參數實體
 *
 * <p>儲存系統配置參數，支援多種資料類型：</p>
 * <ul>
 *   <li>STRING - 字串類型</li>
 *   <li>NUMBER - 數值類型</li>
 *   <li>BOOLEAN - 布林類型</li>
 *   <li>JSON - JSON 格式</li>
 *   <li>DATE - 日期類型</li>
 * </ul>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Entity
@Table(name = "system_parameters", indexes = {
        @Index(name = "idx_system_params_category", columnList = "category")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemParameter extends BaseEntity {

    /**
     * 參數類別
     */
    @Column(name = "category", nullable = false, length = 50)
    private String category;

    /**
     * 參數鍵
     */
    @Column(name = "param_key", nullable = false, length = 100)
    private String paramKey;

    /**
     * 參數值
     */
    @Column(name = "param_value", length = 1000)
    private String paramValue;

    /**
     * 參數類型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "param_type", length = 20)
    @Builder.Default
    private ParamType paramType = ParamType.STRING;

    /**
     * 參數描述
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * 是否為系統參數（不可刪除）
     */
    @Column(name = "is_system", nullable = false)
    @Builder.Default
    private Boolean isSystem = false;

    /**
     * 是否加密儲存
     */
    @Column(name = "is_encrypted", nullable = false)
    @Builder.Default
    private Boolean isEncrypted = false;

    /**
     * 排序順序
     */
    @Column(name = "sort_order")
    @Builder.Default
    private Integer sortOrder = 0;

    /**
     * 參數類型列舉
     */
    public enum ParamType {
        STRING, NUMBER, BOOLEAN, JSON, DATE
    }
}
