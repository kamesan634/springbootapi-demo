package com.kamesan.erpapi.products.entity;

import com.kamesan.erpapi.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 商品分類實體
 *
 * <p>用於建立商品的階層式分類結構，支援多層級分類。</p>
 *
 * <h2>主要欄位：</h2>
 * <ul>
 *   <li>code - 分類代碼（唯一識別碼）</li>
 *   <li>name - 分類名稱</li>
 *   <li>parent - 父分類（支援階層結構）</li>
 *   <li>level - 分類層級（1 為根層級）</li>
 *   <li>path - 分類路徑（如：1/2/3，用於快速查詢）</li>
 *   <li>sortOrder - 排序順序</li>
 *   <li>isActive - 是否啟用</li>
 * </ul>
 *
 * <h2>分類結構範例：</h2>
 * <pre>
 * 食品 (level=1, path=1)
 *   ├── 飲料 (level=2, path=1/2)
 *   │     ├── 茶類 (level=3, path=1/2/5)
 *   │     └── 咖啡 (level=3, path=1/2/6)
 *   └── 零食 (level=2, path=1/3)
 * 日用品 (level=1, path=4)
 * </pre>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category extends BaseEntity {

    /**
     * 分類代碼
     * <p>唯一識別碼，用於識別分類</p>
     */
    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    /**
     * 分類名稱
     * <p>顯示用名稱</p>
     */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /**
     * 父分類
     * <p>根分類的 parent 為 null</p>
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    /**
     * 子分類列表
     * <p>用於載入該分類的所有直接子分類</p>
     */
    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    @Builder.Default
    @OrderBy("sortOrder ASC")
    private List<Category> children = new ArrayList<>();

    /**
     * 分類層級
     * <p>1 表示根層級，數字越大層級越深</p>
     */
    @Column(name = "level", nullable = false)
    @Builder.Default
    private Integer level = 1;

    /**
     * 分類路徑
     * <p>格式：祖先ID/父ID/本身ID，用於快速查詢所有子孫分類</p>
     * <p>例如：1/2/3 表示 ID 為 3 的分類，其父為 2，祖父為 1</p>
     */
    @Column(name = "path", length = 500)
    private String path;

    /**
     * 是否啟用
     * <p>停用的分類及其子分類都不會出現在選單中</p>
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;

    /**
     * 排序順序
     * <p>數字越小排序越前面</p>
     */
    @Column(name = "sort_order")
    @Builder.Default
    private Integer sortOrder = 0;

    /**
     * 該分類下的商品
     */
    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Product> products = new ArrayList<>();

    /**
     * 判斷是否為根分類
     *
     * @return 是否為根分類
     */
    public boolean isRoot() {
        return parent == null;
    }

    /**
     * 判斷是否有子分類
     *
     * @return 是否有子分類
     */
    public boolean hasChildren() {
        return children != null && !children.isEmpty();
    }

    /**
     * 取得完整路徑名稱
     * <p>從根分類到本分類的名稱，用 > 分隔</p>
     *
     * @return 完整路徑名稱，如：食品 > 飲料 > 茶類
     */
    public String getFullPathName() {
        if (parent == null) {
            return name;
        }
        return parent.getFullPathName() + " > " + name;
    }

    /**
     * 更新路徑
     * <p>根據父分類更新本分類的路徑</p>
     */
    public void updatePath() {
        if (parent == null) {
            this.path = String.valueOf(getId());
            this.level = 1;
        } else {
            this.path = parent.getPath() + "/" + getId();
            this.level = parent.getLevel() + 1;
        }
    }
}
