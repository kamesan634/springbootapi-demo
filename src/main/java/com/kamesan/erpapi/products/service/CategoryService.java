package com.kamesan.erpapi.products.service;

import com.kamesan.erpapi.common.exception.BusinessException;
import com.kamesan.erpapi.products.dto.CategoryDto;
import com.kamesan.erpapi.products.entity.Category;
import com.kamesan.erpapi.products.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 商品分類服務
 *
 * <p>處理商品分類相關的業務邏輯，包括：</p>
 * <ul>
 *   <li>分類 CRUD 操作</li>
 *   <li>分類樹狀結構查詢</li>
 *   <li>分類路徑管理</li>
 *   <li>分類驗證</li>
 * </ul>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {

    /**
     * 分類 Repository
     */
    private final CategoryRepository categoryRepository;

    /**
     * 建立分類
     *
     * <p>建立新的商品分類，包含以下處理：</p>
     * <ul>
     *   <li>代碼唯一性檢查</li>
     *   <li>父分類存在性檢查</li>
     *   <li>自動計算層級和路徑</li>
     * </ul>
     *
     * @param request 建立分類請求
     * @return 建立的分類 DTO
     */
    @Transactional
    public CategoryDto createCategory(CategoryDto.CreateCategoryRequest request) {
        log.info("建立分類: {}", request.getCode());

        // 檢查代碼是否已存在
        if (categoryRepository.existsByCode(request.getCode())) {
            throw BusinessException.alreadyExists("分類", "代碼", request.getCode());
        }

        // 建立分類實體
        Category category = Category.builder()
                .code(request.getCode())
                .name(request.getName())
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0)
                .active(request.getActive() != null ? request.getActive() : true)
                .build();

        // 處理父分類
        if (request.getParentId() != null) {
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> BusinessException.notFound("父分類", request.getParentId()));
            category.setParent(parent);
            category.setLevel(parent.getLevel() + 1);
        } else {
            category.setLevel(1);
        }

        // 先儲存以取得 ID
        category = categoryRepository.save(category);

        // 更新路徑
        category.updatePath();
        category = categoryRepository.save(category);

        log.info("分類建立成功: ID={}, Code={}", category.getId(), category.getCode());

        return convertToDto(category, false);
    }

    /**
     * 更新分類
     *
     * <p>更新現有分類，包含以下處理：</p>
     * <ul>
     *   <li>代碼唯一性檢查（排除自身）</li>
     *   <li>父分類存在性檢查</li>
     *   <li>禁止將分類設為自己的子分類</li>
     *   <li>更新路徑和層級</li>
     * </ul>
     *
     * @param id      分類 ID
     * @param request 更新分類請求
     * @return 更新後的分類 DTO
     */
    @Transactional
    public CategoryDto updateCategory(Long id, CategoryDto.UpdateCategoryRequest request) {
        log.info("更新分類: ID={}", id);

        // 查詢分類
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("分類", id));

        // 檢查代碼是否已被其他分類使用
        if (categoryRepository.existsByCodeAndIdNot(request.getCode(), id)) {
            throw BusinessException.alreadyExists("分類", "代碼", request.getCode());
        }

        // 更新基本資訊
        category.setCode(request.getCode());
        category.setName(request.getName());

        if (request.getSortOrder() != null) {
            category.setSortOrder(request.getSortOrder());
        }

        if (request.getActive() != null) {
            category.setActive(request.getActive());
        }

        // 處理父分類變更
        boolean parentChanged = false;
        if (request.getParentId() != null) {
            // 檢查是否設為自己或自己的子分類
            if (request.getParentId().equals(id)) {
                throw BusinessException.validationFailed("不能將分類設為自己的子分類");
            }

            // 檢查是否設為自己的子孫分類
            if (isDescendant(request.getParentId(), id)) {
                throw BusinessException.validationFailed("不能將分類設為自己的子孫分類");
            }

            Category newParent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> BusinessException.notFound("父分類", request.getParentId()));

            if (category.getParent() == null || !category.getParent().getId().equals(request.getParentId())) {
                category.setParent(newParent);
                parentChanged = true;
            }
        } else {
            if (category.getParent() != null) {
                category.setParent(null);
                parentChanged = true;
            }
        }

        // 若父分類變更，更新路徑和層級
        if (parentChanged) {
            updateCategoryPathAndLevel(category);
            // 更新所有子孫分類的路徑和層級
            updateDescendantsPathAndLevel(category);
        }

        category = categoryRepository.save(category);

        log.info("分類更新成功: ID={}, Code={}", category.getId(), category.getCode());

        return convertToDto(category, false);
    }

    /**
     * 檢查是否為指定分類的子孫
     *
     * @param checkId  要檢查的分類 ID
     * @param parentId 父分類 ID
     * @return 是否為子孫
     */
    private boolean isDescendant(Long checkId, Long parentId) {
        Category category = categoryRepository.findById(checkId).orElse(null);
        while (category != null && category.getParent() != null) {
            if (category.getParent().getId().equals(parentId)) {
                return true;
            }
            category = category.getParent();
        }
        return false;
    }

    /**
     * 更新分類的路徑和層級
     *
     * @param category 分類實體
     */
    private void updateCategoryPathAndLevel(Category category) {
        if (category.getParent() == null) {
            category.setLevel(1);
            category.setPath(String.valueOf(category.getId()));
        } else {
            category.setLevel(category.getParent().getLevel() + 1);
            category.setPath(category.getParent().getPath() + "/" + category.getId());
        }
    }

    /**
     * 更新所有子孫分類的路徑和層級
     *
     * @param parent 父分類
     */
    private void updateDescendantsPathAndLevel(Category parent) {
        List<Category> children = categoryRepository.findByParentId(parent.getId());
        for (Category child : children) {
            child.setLevel(parent.getLevel() + 1);
            child.setPath(parent.getPath() + "/" + child.getId());
            categoryRepository.save(child);
            updateDescendantsPathAndLevel(child);
        }
    }

    /**
     * 根據 ID 查詢分類
     *
     * @param id 分類 ID
     * @return 分類 DTO
     */
    @Transactional(readOnly = true)
    public CategoryDto getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("分類", id));
        return convertToDto(category, true);
    }

    /**
     * 根據代碼查詢分類
     *
     * @param code 分類代碼
     * @return 分類 DTO
     */
    @Transactional(readOnly = true)
    public CategoryDto getCategoryByCode(String code) {
        Category category = categoryRepository.findByCode(code)
                .orElseThrow(() -> BusinessException.notFound("分類", "代碼: " + code));
        return convertToDto(category, true);
    }

    /**
     * 刪除分類
     *
     * <p>刪除分類前會檢查：</p>
     * <ul>
     *   <li>是否有子分類</li>
     *   <li>是否有商品使用此分類</li>
     * </ul>
     *
     * @param id 分類 ID
     */
    @Transactional
    public void deleteCategory(Long id) {
        log.info("刪除分類: ID={}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("分類", id));

        // 檢查是否有子分類
        if (categoryRepository.hasChildren(id)) {
            throw BusinessException.validationFailed("無法刪除分類，請先刪除子分類");
        }

        // 檢查是否有商品使用此分類
        long productCount = categoryRepository.countProductsByCategoryId(id);
        if (productCount > 0) {
            throw BusinessException.validationFailed(
                    String.format("無法刪除分類，有 %d 個商品使用此分類", productCount));
        }

        categoryRepository.delete(category);

        log.info("分類刪除成功: ID={}", id);
    }

    /**
     * 查詢所有分類（分頁）
     *
     * @param pageable 分頁參數
     * @return 分類分頁
     */
    @Transactional(readOnly = true)
    public Page<CategoryDto> getAllCategories(Pageable pageable) {
        return categoryRepository.findAll(pageable)
                .map(category -> convertToDto(category, false));
    }

    /**
     * 查詢所有啟用的分類（分頁）
     *
     * @param pageable 分頁參數
     * @return 分類分頁
     */
    @Transactional(readOnly = true)
    public Page<CategoryDto> getActiveCategories(Pageable pageable) {
        return categoryRepository.findByActiveTrue(pageable)
                .map(category -> convertToDto(category, false));
    }

    /**
     * 搜尋分類
     *
     * @param keyword  關鍵字
     * @param pageable 分頁參數
     * @return 分類分頁
     */
    @Transactional(readOnly = true)
    public Page<CategoryDto> searchCategories(String keyword, Pageable pageable) {
        return categoryRepository.search(keyword, pageable)
                .map(category -> convertToDto(category, false));
    }

    /**
     * 取得分類樹狀結構
     *
     * <p>從根分類開始，遞迴載入所有子分類</p>
     *
     * @return 分類樹狀結構
     */
    @Transactional(readOnly = true)
    public List<CategoryDto> getCategoryTree() {
        List<Category> rootCategories = categoryRepository.findActiveRootCategories();
        return rootCategories.stream()
                .map(category -> convertToDtoWithChildren(category))
                .collect(Collectors.toList());
    }

    /**
     * 取得所有分類樹狀結構（包含停用的）
     *
     * @return 分類樹狀結構
     */
    @Transactional(readOnly = true)
    public List<CategoryDto> getAllCategoryTree() {
        List<Category> rootCategories = categoryRepository.findRootCategories();
        return rootCategories.stream()
                .map(category -> convertToDtoWithChildren(category))
                .collect(Collectors.toList());
    }

    /**
     * 取得指定分類的子分類
     *
     * @param parentId 父分類 ID
     * @return 子分類列表
     */
    @Transactional(readOnly = true)
    public List<CategoryDto> getChildCategories(Long parentId) {
        List<Category> children = categoryRepository.findActiveByParentId(parentId);
        return children.stream()
                .map(category -> convertToDto(category, false))
                .collect(Collectors.toList());
    }

    /**
     * 將分類實體轉換為 DTO
     *
     * @param category          分類實體
     * @param includeStatistics 是否包含統計資訊
     * @return 分類 DTO
     */
    private CategoryDto convertToDto(Category category, boolean includeStatistics) {
        CategoryDto.CategoryDtoBuilder builder = CategoryDto.builder()
                .id(category.getId())
                .code(category.getCode())
                .name(category.getName())
                .level(category.getLevel())
                .path(category.getPath())
                .fullPathName(category.getFullPathName())
                .sortOrder(category.getSortOrder())
                .active(category.isActive())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt());

        // 設定父分類資訊
        if (category.getParent() != null) {
            builder.parent(CategoryDto.ParentInfo.builder()
                    .id(category.getParent().getId())
                    .code(category.getParent().getCode())
                    .name(category.getParent().getName())
                    .build());
        }

        // 檢查是否有子分類
        builder.hasChildren(categoryRepository.hasChildren(category.getId()));

        // 包含統計資訊
        if (includeStatistics) {
            builder.productCount(categoryRepository.countProductsByCategoryId(category.getId()));
        }

        return builder.build();
    }

    /**
     * 將分類實體轉換為 DTO（包含子分類）
     *
     * @param category 分類實體
     * @return 分類 DTO（含子分類）
     */
    private CategoryDto convertToDtoWithChildren(Category category) {
        CategoryDto dto = convertToDto(category, false);

        // 遞迴載入子分類
        List<Category> children = categoryRepository.findActiveByParentId(category.getId());
        if (!children.isEmpty()) {
            List<CategoryDto> childDtos = children.stream()
                    .map(this::convertToDtoWithChildren)
                    .collect(Collectors.toList());
            dto.setChildren(childDtos);
        } else {
            dto.setChildren(new ArrayList<>());
        }

        return dto;
    }
}
