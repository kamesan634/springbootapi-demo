package com.kamesan.erpapi.products.entity;

import com.kamesan.erpapi.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * 單位實體
 *
 * <p>用於定義商品的計量單位。</p>
 *
 * <h2>主要欄位：</h2>
 * <ul>
 *   <li>code - 單位代碼（唯一識別碼）</li>
 *   <li>name - 單位名稱</li>
 *   <li>isActive - 是否啟用</li>
 * </ul>
 *
 * <h2>常見單位範例：</h2>
 * <ul>
 *   <li>PCS - 個</li>
 *   <li>BOX - 箱</li>
 *   <li>KG - 公斤</li>
 *   <li>L - 公升</li>
 *   <li>M - 公尺</li>
 * </ul>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Entity
@Table(name = "units")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Unit extends BaseEntity {

    /**
     * 單位代碼
     * <p>唯一識別碼，如：PCS、BOX、KG</p>
     */
    @Column(name = "code", nullable = false, unique = true, length = 20)
    private String code;

    /**
     * 單位名稱
     * <p>顯示用名稱，如：個、箱、公斤</p>
     */
    @Column(name = "name", nullable = false, length = 50)
    private String name;

    /**
     * 是否啟用
     * <p>停用的單位不會出現在選單中，但已使用該單位的商品不受影響</p>
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;
}
