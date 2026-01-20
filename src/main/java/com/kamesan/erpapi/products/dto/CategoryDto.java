package com.kamesan.erpapi.products.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品分類 DTO
 *
 * <p>用於 API 回應的商品分類資料傳輸物件。</p>
 *
 * <h2>支援樹狀結構：</h2>
 * <ul>
 *   <li>parent - 父分類資訊</li>
 *   <li>children - 子分類列表</li>
 *   <li>fullPathName - 完整路徑名稱</li>
 * </ul>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "商品分類資料")
public class CategoryDto {

    /**
     * 分類 ID
     */
    @Schema(description = "分類 ID", example = "1")
    private Long id;

    /**
     * 分類代碼
     */
    @Schema(description = "分類代碼", example = "BEVERAGE")
    private String code;

    /**
     * 分類名稱
     */
    @Schema(description = "分類名稱", example = "飲料")
    private String name;

    /**
     * 父分類資訊
     */
    @Schema(description = "父分類資訊")
    private ParentInfo parent;

    /**
     * 分類層級
     */
    @Schema(description = "分類層級", example = "1")
    private Integer level;

    /**
     * 分類路徑
     */
    @Schema(description = "分類路徑", example = "1/2")
    private String path;

    /**
     * 完整路徑名稱
     */
    @Schema(description = "完整路徑名稱", example = "食品 > 飲料")
    private String fullPathName;

    /**
     * 排序順序
     */
    @Schema(description = "排序順序", example = "0")
    private Integer sortOrder;

    /**
     * 是否啟用
     */
    @Schema(description = "是否啟用", example = "true")
    private boolean active;

    /**
     * 子分類列表
     */
    @Schema(description = "子分類列表")
    private List<CategoryDto> children;

    /**
     * 商品數量
     */
    @Schema(description = "該分類下的商品數量", example = "10")
    private Long productCount;

    /**
     * 是否有子分類
     */
    @Schema(description = "是否有子分類", example = "true")
    private Boolean hasChildren;

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
     * 父分類資訊內嵌類別
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "父分類資訊")
    public static class ParentInfo {
        /**
         * 父分類 ID
         */
        @Schema(description = "父分類 ID", example = "1")
        private Long id;

        /**
         * 父分類代碼
         */
        @Schema(description = "父分類代碼", example = "FOOD")
        private String code;

        /**
         * 父分類名稱
         */
        @Schema(description = "父分類名稱", example = "食品")
        private String name;
    }

    /**
     * 建立分類請求
     * <p>用於建立新分類的請求物件</p>
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "建立分類請求")
    public static class CreateCategoryRequest {
        /**
         * 分類代碼
         */
        @NotBlank(message = "分類代碼不能為空")
        @Size(max = 50, message = "分類代碼長度不能超過 50 字元")
        @Schema(description = "分類代碼", example = "BEVERAGE", requiredMode = Schema.RequiredMode.REQUIRED)
        private String code;

        /**
         * 分類名稱
         */
        @NotBlank(message = "分類名稱不能為空")
        @Size(max = 100, message = "分類名稱長度不能超過 100 字元")
        @Schema(description = "分類名稱", example = "飲料", requiredMode = Schema.RequiredMode.REQUIRED)
        private String name;

        /**
         * 父分類 ID
         * <p>若為 null 表示為根分類</p>
         */
        @Schema(description = "父分類 ID", example = "1")
        private Long parentId;

        /**
         * 排序順序
         */
        @Schema(description = "排序順序", example = "0")
        private Integer sortOrder;

        /**
         * 是否啟用
         */
        @Schema(description = "是否啟用", example = "true")
        @Builder.Default
        private Boolean active = true;
    }

    /**
     * 更新分類請求
     * <p>用於更新分類的請求物件</p>
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "更新分類請求")
    public static class UpdateCategoryRequest {
        /**
         * 分類代碼
         */
        @NotBlank(message = "分類代碼不能為空")
        @Size(max = 50, message = "分類代碼長度不能超過 50 字元")
        @Schema(description = "分類代碼", example = "BEVERAGE", requiredMode = Schema.RequiredMode.REQUIRED)
        private String code;

        /**
         * 分類名稱
         */
        @NotBlank(message = "分類名稱不能為空")
        @Size(max = 100, message = "分類名稱長度不能超過 100 字元")
        @Schema(description = "分類名稱", example = "飲料", requiredMode = Schema.RequiredMode.REQUIRED)
        private String name;

        /**
         * 父分類 ID
         * <p>若為 null 表示為根分類</p>
         * <p>注意：更改父分類會影響路徑和層級</p>
         */
        @Schema(description = "父分類 ID", example = "1")
        private Long parentId;

        /**
         * 排序順序
         */
        @Schema(description = "排序順序", example = "0")
        private Integer sortOrder;

        /**
         * 是否啟用
         */
        @Schema(description = "是否啟用", example = "true")
        private Boolean active;
    }
}
