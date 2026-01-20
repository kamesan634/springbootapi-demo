package com.kamesan.erpapi.products.controller;

import com.kamesan.erpapi.BaseIntegrationTest;
import com.kamesan.erpapi.products.dto.CategoryDto;
import com.kamesan.erpapi.products.entity.Category;
import com.kamesan.erpapi.products.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 商品分類控制器測試類別
 */
@DisplayName("商品分類控制器測試")
class CategoryControllerTest extends BaseIntegrationTest {

    private static final String CATEGORIES_API = "/api/v1/categories";

    @Autowired
    private CategoryRepository categoryRepository;

    private Category testRootCategory;
    private Category testChildCategory;

    @BeforeEach
    void setUpCategoryTestData() {
        // 建立根分類
        testRootCategory = categoryRepository.findByCode("TEST-ROOT").orElseGet(() -> {
            Category cat = new Category();
            cat.setCode("TEST-ROOT");
            cat.setName("測試根分類");
            cat.setLevel(1);
            cat.setPath("0");
            cat.setActive(true);
            cat.setSortOrder(0);
            return categoryRepository.save(cat);
        });

        // 建立子分類
        testChildCategory = categoryRepository.findByCode("TEST-CHILD").orElseGet(() -> {
            Category cat = new Category();
            cat.setCode("TEST-CHILD");
            cat.setName("測試子分類");
            cat.setParent(testRootCategory);
            cat.setLevel(2);
            cat.setPath(testRootCategory.getId().toString());
            cat.setActive(true);
            cat.setSortOrder(0);
            return categoryRepository.save(cat);
        });
    }

    @Nested
    @DisplayName("POST /api/v1/categories - 建立分類")
    class CreateCategoryTests {

        @Test
        @DisplayName("有效資料應建立分類成功")
        void create_WithValidData_ShouldCreateCategory() throws Exception {
            String token = generateAdminToken();
            CategoryDto.CreateCategoryRequest request = CategoryDto.CreateCategoryRequest.builder()
                    .code("NEW-CAT-001")
                    .name("新分類")
                    .sortOrder(0)
                    .active(true)
                    .build();

            mockMvc.perform(post(CATEGORIES_API)
                            .header("Authorization", bearerToken(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.code").value("NEW-CAT-001"))
                    .andExpect(jsonPath("$.data.name").value("新分類"));
        }

        @Test
        @DisplayName("未驗證應返回 403")
        void create_WithoutAuth_ShouldReturnForbidden() throws Exception {
            CategoryDto.CreateCategoryRequest request = CategoryDto.CreateCategoryRequest.builder()
                    .code("NEW-CAT-002")
                    .name("新分類2")
                    .build();

            mockMvc.perform(post(CATEGORIES_API)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("缺少必填欄位應返回驗證錯誤")
        void create_WithMissingRequiredFields_ShouldReturnBadRequest() throws Exception {
            String token = generateAdminToken();
            CategoryDto.CreateCategoryRequest request = CategoryDto.CreateCategoryRequest.builder()
                    .code("NEW-CAT-003")
                    // name 缺少
                    .build();

            mockMvc.perform(post(CATEGORIES_API)
                            .header("Authorization", bearerToken(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("有父分類應建立子分類成功")
        void create_WithParent_ShouldCreateChildCategory() throws Exception {
            String token = generateAdminToken();
            CategoryDto.CreateCategoryRequest request = CategoryDto.CreateCategoryRequest.builder()
                    .code("NEW-CHILD-001")
                    .name("新子分類")
                    .parentId(testRootCategory.getId())
                    .sortOrder(0)
                    .active(true)
                    .build();

            mockMvc.perform(post(CATEGORIES_API)
                            .header("Authorization", bearerToken(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.code").value("NEW-CHILD-001"))
                    .andExpect(jsonPath("$.data.level").value(2));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/categories - 查詢分類列表")
    class GetCategoriesTests {

        @Test
        @DisplayName("應返回分類列表")
        void getAll_ShouldReturnCategoryList() throws Exception {
            String token = generateAdminToken();

            mockMvc.perform(get(CATEGORIES_API)
                            .header("Authorization", bearerToken(token)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isArray());
        }

        @Test
        @DisplayName("應支援分頁查詢")
        void getAll_WithPagination_ShouldReturnPagedResults() throws Exception {
            String token = generateAdminToken();

            mockMvc.perform(get(CATEGORIES_API)
                            .header("Authorization", bearerToken(token))
                            .param("page", "1")
                            .param("size", "10"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.size").value(10))
                    .andExpect(jsonPath("$.data.page").value(1));
        }

        @Test
        @DisplayName("應支援只查詢啟用的分類")
        void getAll_WithActiveOnly_ShouldReturnActiveCategories() throws Exception {
            String token = generateAdminToken();

            mockMvc.perform(get(CATEGORIES_API)
                            .header("Authorization", bearerToken(token))
                            .param("activeOnly", "true"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/categories/{id} - 根據 ID 查詢分類")
    class GetCategoryByIdTests {

        @Test
        @DisplayName("有效 ID 應返回分類詳情")
        void getById_WithValidId_ShouldReturnCategory() throws Exception {
            String token = generateAdminToken();

            mockMvc.perform(get(CATEGORIES_API + "/" + testRootCategory.getId())
                            .header("Authorization", bearerToken(token)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(testRootCategory.getId()))
                    .andExpect(jsonPath("$.data.code").value("TEST-ROOT"));
        }

        @Test
        @DisplayName("不存在的 ID 應返回 404")
        void getById_WithInvalidId_ShouldReturnNotFound() throws Exception {
            String token = generateAdminToken();

            mockMvc.perform(get(CATEGORIES_API + "/99999")
                            .header("Authorization", bearerToken(token)))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/categories/code/{code} - 根據代碼查詢分類")
    class GetCategoryByCodeTests {

        @Test
        @DisplayName("有效代碼應返回分類")
        void getByCode_WithValidCode_ShouldReturnCategory() throws Exception {
            String token = generateAdminToken();

            mockMvc.perform(get(CATEGORIES_API + "/code/TEST-ROOT")
                            .header("Authorization", bearerToken(token)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.code").value("TEST-ROOT"));
        }

        @Test
        @DisplayName("不存在的代碼應返回 404")
        void getByCode_WithInvalidCode_ShouldReturnNotFound() throws Exception {
            String token = generateAdminToken();

            mockMvc.perform(get(CATEGORIES_API + "/code/NOT-EXIST")
                            .header("Authorization", bearerToken(token)))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/categories/{id} - 更新分類")
    class UpdateCategoryTests {

        @Test
        @DisplayName("有效資料應更新分類成功")
        void update_WithValidData_ShouldUpdateCategory() throws Exception {
            String token = generateAdminToken();
            CategoryDto.UpdateCategoryRequest request = CategoryDto.UpdateCategoryRequest.builder()
                    .code("TEST-ROOT")
                    .name("更新後的分類名稱")
                    .sortOrder(1)
                    .active(true)
                    .build();

            mockMvc.perform(put(CATEGORIES_API + "/" + testRootCategory.getId())
                            .header("Authorization", bearerToken(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.name").value("更新後的分類名稱"));
        }

        @Test
        @DisplayName("更新不存在的分類應返回 404")
        void update_WithInvalidId_ShouldReturnNotFound() throws Exception {
            String token = generateAdminToken();
            CategoryDto.UpdateCategoryRequest request = CategoryDto.UpdateCategoryRequest.builder()
                    .code("NOT-EXIST")
                    .name("更新分類")
                    .build();

            mockMvc.perform(put(CATEGORIES_API + "/99999")
                            .header("Authorization", bearerToken(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/categories/{id} - 刪除分類")
    class DeleteCategoryTests {

        @Test
        @DisplayName("刪除不存在的分類應返回 404")
        void delete_WithInvalidId_ShouldReturnNotFound() throws Exception {
            String token = generateAdminToken();

            mockMvc.perform(delete(CATEGORIES_API + "/99999")
                            .header("Authorization", bearerToken(token)))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/categories/tree - 取得分類樹狀結構")
    class GetCategoryTreeTests {

        @Test
        @DisplayName("應返回分類樹狀結構")
        void getTree_ShouldReturnCategoryTree() throws Exception {
            String token = generateAdminToken();

            mockMvc.perform(get(CATEGORIES_API + "/tree")
                            .header("Authorization", bearerToken(token)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray());
        }

        @Test
        @DisplayName("包含停用分類應返回所有分類")
        void getTree_WithIncludeInactive_ShouldReturnAllCategories() throws Exception {
            String token = generateAdminToken();

            mockMvc.perform(get(CATEGORIES_API + "/tree")
                            .header("Authorization", bearerToken(token))
                            .param("includeInactive", "true"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/categories/{parentId}/children - 取得子分類")
    class GetChildCategoriesTests {

        @Test
        @DisplayName("應返回子分類列表")
        void getChildren_ShouldReturnChildCategories() throws Exception {
            String token = generateAdminToken();

            mockMvc.perform(get(CATEGORIES_API + "/" + testRootCategory.getId() + "/children")
                            .header("Authorization", bearerToken(token)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/categories/search - 搜尋分類")
    class SearchCategoriesTests {

        @Test
        @DisplayName("關鍵字搜尋應返回相符分類")
        void search_WithKeyword_ShouldReturnMatchingCategories() throws Exception {
            String token = generateAdminToken();

            mockMvc.perform(get(CATEGORIES_API + "/search")
                            .header("Authorization", bearerToken(token))
                            .param("keyword", "測試"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }
}
