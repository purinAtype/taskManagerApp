package com.example.taskmanager.category.controller;

import com.example.taskmanager.category.converter.CategoryConverter;
import com.example.taskmanager.category.dto.CategoryDto;
import com.example.taskmanager.category.form.CategoryForm;
import com.example.taskmanager.category.service.CategoryService;
import com.example.taskmanager.common.exception.CategoryInUseException;
import com.example.taskmanager.common.exception.CategoryNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * CategoryControllerの単体テスト.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("CategoryControllerのテスト")
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CategoryService categoryService;

    @MockBean
    private CategoryConverter categoryConverter;

    private CategoryDto testCategoryDto;
    private CategoryForm testCategoryForm;

    @BeforeEach
    void setUp() {
        testCategoryDto = CategoryDto.builder()
                .id(1L)
                .name("仕事")
                .description("仕事関連のタスク")
                .color("#007bff")
                .displayOrder(1)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testCategoryForm = CategoryForm.builder()
                .name("仕事")
                .description("仕事関連のタスク")
                .color("#007bff")
                .displayOrder(1)
                .build();
    }

    @Nested
    @DisplayName("一覧表示のテスト")
    class ListTest {

        @Test
        @DisplayName("カテゴリー一覧を表示できる")
        void shouldShowCategoryList() throws Exception {
            // given
            CategoryDto category2 = CategoryDto.builder()
                    .id(2L)
                    .name("プライベート")
                    .description("プライベートのタスク")
                    .color("#28a745")
                    .displayOrder(2)
                    .build();
            List<CategoryDto> categories = Arrays.asList(testCategoryDto, category2);
            when(categoryService.findAll()).thenReturn(categories);

            // when & then
            mockMvc.perform(get("/categories"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("category/list"))
                    .andExpect(model().attributeExists("categories"))
                    .andExpect(model().attribute("categories", hasSize(2)))
                    .andExpect(model().attribute("categories", hasItem(
                            hasProperty("name", is("仕事"))
                    )));

            verify(categoryService).findAll();
        }
    }

    @Nested
    @DisplayName("新規登録のテスト")
    class CreateTest {

        @Test
        @DisplayName("新規登録フォームを表示できる")
        void shouldShowNewForm() throws Exception {
            // when & then
            mockMvc.perform(get("/categories/new"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("category/form"))
                    .andExpect(model().attributeExists("categoryForm"))
                    .andExpect(model().attribute("categoryForm",
                            hasProperty("color", is("#6c757d"))))
                    .andExpect(model().attribute("categoryForm",
                            hasProperty("displayOrder", is(0))));
        }

        @Test
        @DisplayName("カテゴリーを登録できる")
        void shouldCreateCategory() throws Exception {
            // given
            when(categoryService.create(any(CategoryForm.class))).thenReturn(testCategoryDto);

            // when & then
            mockMvc.perform(post("/categories")
                            .param("name", "仕事")
                            .param("description", "仕事関連のタスク")
                            .param("color", "#007bff")
                            .param("displayOrder", "1"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/categories"))
                    .andExpect(flash().attribute("successMessage", "カテゴリーを登録しました"));

            verify(categoryService).create(any(CategoryForm.class));
        }

        @Test
        @DisplayName("バリデーションエラー時はフォームを再表示")
        void shouldShowFormOnValidationError() throws Exception {
            // when & then - カテゴリー名が空
            mockMvc.perform(post("/categories")
                            .param("name", "")
                            .param("color", "#007bff")
                            .param("displayOrder", "1"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("category/form"))
                    .andExpect(model().hasErrors())
                    .andExpect(model().attributeHasFieldErrors("categoryForm", "name"));

            verify(categoryService, never()).create(any());
        }

        @Test
        @DisplayName("不正なカラーコード形式でバリデーションエラー")
        void shouldFailValidationOnInvalidColorFormat() throws Exception {
            // when & then - カラーコード形式が不正
            mockMvc.perform(post("/categories")
                            .param("name", "仕事")
                            .param("color", "invalid")
                            .param("displayOrder", "1"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("category/form"))
                    .andExpect(model().hasErrors())
                    .andExpect(model().attributeHasFieldErrors("categoryForm", "color"));

            verify(categoryService, never()).create(any());
        }

        @Test
        @DisplayName("カテゴリー名50文字（上限）で正常に登録できる")
        void shouldCreateCategoryWithName50Characters() throws Exception {
            // given
            String name50chars = "a".repeat(50);
            when(categoryService.create(any(CategoryForm.class))).thenReturn(testCategoryDto);

            // when & then
            mockMvc.perform(post("/categories")
                            .param("name", name50chars)
                            .param("color", "#007bff")
                            .param("displayOrder", "1"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/categories"));

            verify(categoryService).create(any(CategoryForm.class));
        }

        @Test
        @DisplayName("カテゴリー名51文字（上限超過）でバリデーションエラー")
        void shouldFailValidationOnName51Characters() throws Exception {
            // given
            String name51chars = "a".repeat(51);

            // when & then
            mockMvc.perform(post("/categories")
                            .param("name", name51chars)
                            .param("color", "#007bff")
                            .param("displayOrder", "1"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("category/form"))
                    .andExpect(model().hasErrors())
                    .andExpect(model().attributeHasFieldErrors("categoryForm", "name"));

            verify(categoryService, never()).create(any());
        }

        @Test
        @DisplayName("説明200文字（上限）で正常に登録できる")
        void shouldCreateCategoryWithDescription200Characters() throws Exception {
            // given
            String description200chars = "a".repeat(200);
            when(categoryService.create(any(CategoryForm.class))).thenReturn(testCategoryDto);

            // when & then
            mockMvc.perform(post("/categories")
                            .param("name", "仕事")
                            .param("description", description200chars)
                            .param("color", "#007bff")
                            .param("displayOrder", "1"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/categories"));

            verify(categoryService).create(any(CategoryForm.class));
        }

        @Test
        @DisplayName("説明201文字（上限超過）でバリデーションエラー")
        void shouldFailValidationOnDescription201Characters() throws Exception {
            // given
            String description201chars = "a".repeat(201);

            // when & then
            mockMvc.perform(post("/categories")
                            .param("name", "仕事")
                            .param("description", description201chars)
                            .param("color", "#007bff")
                            .param("displayOrder", "1"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("category/form"))
                    .andExpect(model().hasErrors())
                    .andExpect(model().attributeHasFieldErrors("categoryForm", "description"));

            verify(categoryService, never()).create(any());
        }

        @Test
        @DisplayName("表示順0（下限）で正常に登録できる")
        void shouldCreateCategoryWithDisplayOrder0() throws Exception {
            // given
            when(categoryService.create(any(CategoryForm.class))).thenReturn(testCategoryDto);

            // when & then
            mockMvc.perform(post("/categories")
                            .param("name", "仕事")
                            .param("color", "#007bff")
                            .param("displayOrder", "0"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/categories"));

            verify(categoryService).create(any(CategoryForm.class));
        }

        @Test
        @DisplayName("表示順-1（負数）でバリデーションエラー")
        void shouldFailValidationOnDisplayOrderNegative() throws Exception {
            // when & then
            mockMvc.perform(post("/categories")
                            .param("name", "仕事")
                            .param("color", "#007bff")
                            .param("displayOrder", "-1"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("category/form"))
                    .andExpect(model().hasErrors())
                    .andExpect(model().attributeHasFieldErrors("categoryForm", "displayOrder"));

            verify(categoryService, never()).create(any());
        }
    }

    @Nested
    @DisplayName("編集のテスト")
    class UpdateTest {

        @Test
        @DisplayName("編集フォームを表示できる")
        void shouldShowEditForm() throws Exception {
            // given
            when(categoryService.findById(1L)).thenReturn(testCategoryDto);
            when(categoryConverter.toForm(testCategoryDto)).thenReturn(testCategoryForm);

            // when & then
            mockMvc.perform(get("/categories/1/edit"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("category/edit"))
                    .andExpect(model().attributeExists("categoryForm"))
                    .andExpect(model().attribute("categoryId", 1L))
                    .andExpect(model().attribute("categoryForm",
                            hasProperty("name", is("仕事"))));

            verify(categoryService).findById(1L);
            verify(categoryConverter).toForm(testCategoryDto);
        }

        @Test
        @DisplayName("存在しないカテゴリーの編集はエラー")
        void shouldShowErrorWhenCategoryNotFound() throws Exception {
            // given
            when(categoryService.findById(999L)).thenThrow(new CategoryNotFoundException(999L));

            // when & then
            mockMvc.perform(get("/categories/999/edit"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("error/404"))
                    .andExpect(model().attributeExists("errorMessage"));

            verify(categoryService).findById(999L);
        }

        @Test
        @DisplayName("カテゴリーを更新できる")
        void shouldUpdateCategory() throws Exception {
            // given
            CategoryDto updatedDto = CategoryDto.builder()
                    .id(1L)
                    .name("仕事(更新)")
                    .description("更新された説明")
                    .color("#ff5733")
                    .displayOrder(2)
                    .build();
            when(categoryService.update(eq(1L), any(CategoryForm.class))).thenReturn(updatedDto);

            // when & then
            mockMvc.perform(post("/categories/1")
                            .param("name", "仕事(更新)")
                            .param("description", "更新された説明")
                            .param("color", "#ff5733")
                            .param("displayOrder", "2"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/categories"))
                    .andExpect(flash().attribute("successMessage", "カテゴリーを更新しました"));

            verify(categoryService).update(eq(1L), any(CategoryForm.class));
        }

        @Test
        @DisplayName("バリデーションエラー時は編集フォームを再表示")
        void shouldShowEditFormOnValidationError() throws Exception {
            // when & then
            mockMvc.perform(post("/categories/1")
                            .param("name", "")  // 空のカテゴリー名
                            .param("color", "#007bff")
                            .param("displayOrder", "1"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("category/edit"))
                    .andExpect(model().hasErrors())
                    .andExpect(model().attribute("categoryId", 1L))
                    .andExpect(model().attributeHasFieldErrors("categoryForm", "name"));

            verify(categoryService, never()).update(anyLong(), any());
        }

        @Test
        @DisplayName("更新時もカテゴリー名51文字でバリデーションエラー")
        void shouldFailValidationOnUpdateWithName51Characters() throws Exception {
            // given
            String name51chars = "a".repeat(51);

            // when & then
            mockMvc.perform(post("/categories/1")
                            .param("name", name51chars)
                            .param("color", "#007bff")
                            .param("displayOrder", "1"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("category/edit"))
                    .andExpect(model().hasErrors())
                    .andExpect(model().attribute("categoryId", 1L))
                    .andExpect(model().attributeHasFieldErrors("categoryForm", "name"));

            verify(categoryService, never()).update(anyLong(), any());
        }

        @Test
        @DisplayName("更新時も説明201文字でバリデーションエラー")
        void shouldFailValidationOnUpdateWithDescription201Characters() throws Exception {
            // given
            String description201chars = "a".repeat(201);

            // when & then
            mockMvc.perform(post("/categories/1")
                            .param("name", "仕事")
                            .param("description", description201chars)
                            .param("color", "#007bff")
                            .param("displayOrder", "1"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("category/edit"))
                    .andExpect(model().hasErrors())
                    .andExpect(model().attribute("categoryId", 1L))
                    .andExpect(model().attributeHasFieldErrors("categoryForm", "description"));

            verify(categoryService, never()).update(anyLong(), any());
        }

        @Test
        @DisplayName("更新時も表示順-1でバリデーションエラー")
        void shouldFailValidationOnUpdateWithDisplayOrderNegative() throws Exception {
            // when & then
            mockMvc.perform(post("/categories/1")
                            .param("name", "仕事")
                            .param("color", "#007bff")
                            .param("displayOrder", "-1"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("category/edit"))
                    .andExpect(model().hasErrors())
                    .andExpect(model().attribute("categoryId", 1L))
                    .andExpect(model().attributeHasFieldErrors("categoryForm", "displayOrder"));

            verify(categoryService, never()).update(anyLong(), any());
        }
    }

    @Nested
    @DisplayName("削除のテスト")
    class DeleteTest {

        @Test
        @DisplayName("カテゴリーを削除できる")
        void shouldDeleteCategory() throws Exception {
            // given
            doNothing().when(categoryService).delete(1L);

            // when & then
            mockMvc.perform(post("/categories/1/delete"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/categories"))
                    .andExpect(flash().attribute("successMessage", "カテゴリーを削除しました"));

            verify(categoryService).delete(1L);
        }

        @Test
        @DisplayName("使用中のカテゴリーの削除はエラーメッセージを表示")
        void shouldShowErrorWhenCategoryInUse() throws Exception {
            // given
            doThrow(new CategoryInUseException(1L)).when(categoryService).delete(1L);

            // when & then
            mockMvc.perform(post("/categories/1/delete"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/categories"))
                    .andExpect(flash().attribute("errorMessage",
                            containsString("使用中のため削除できません")));

            verify(categoryService).delete(1L);
        }
    }
}
