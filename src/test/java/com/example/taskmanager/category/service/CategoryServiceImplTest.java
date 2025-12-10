package com.example.taskmanager.category.service;

import com.example.taskmanager.category.converter.CategoryConverter;
import com.example.taskmanager.category.dto.CategoryDto;
import com.example.taskmanager.category.form.CategoryForm;
import com.example.taskmanager.category.mapper.CategoryCustomMapper;
import com.example.taskmanager.common.entity.TaskCategory;
import com.example.taskmanager.common.entity.TaskCategoryExample;
import com.example.taskmanager.common.exception.CategoryInUseException;
import com.example.taskmanager.common.exception.CategoryNotFoundException;
import com.example.taskmanager.common.mapper.TaskCategoryMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * CategoryServiceImplの単体テスト.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryServiceImplのテスト")
class CategoryServiceImplTest {

    @Mock
    private TaskCategoryMapper taskCategoryMapper;

    @Mock
    private CategoryCustomMapper categoryCustomMapper;

    @Mock
    private CategoryConverter categoryConverter;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private TaskCategory testCategory;
    private CategoryDto testCategoryDto;
    private CategoryForm testCategoryForm;

    @BeforeEach
    void setUp() {
        testCategory = new TaskCategory();
        testCategory.setId(1L);
        testCategory.setName("仕事");
        testCategory.setDescription("仕事関連のタスク");
        testCategory.setColor("#007bff");
        testCategory.setDisplayOrder(1);
        testCategory.setCreatedAt(LocalDateTime.now());
        testCategory.setUpdatedAt(LocalDateTime.now());

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
    @DisplayName("findAllのテスト")
    class FindAllTest {

        @Test
        @DisplayName("全カテゴリーを表示順で取得できる")
        void shouldReturnAllCategoriesOrderedByDisplayOrder() {
            // given
            TaskCategory category2 = new TaskCategory();
            category2.setId(2L);
            category2.setName("プライベート");
            category2.setDisplayOrder(2);

            List<TaskCategory> categories = Arrays.asList(testCategory, category2);
            when(taskCategoryMapper.selectByExample(any(TaskCategoryExample.class))).thenReturn(categories);
            when(categoryConverter.toDtoList(categories)).thenReturn(
                    Arrays.asList(testCategoryDto,
                            CategoryDto.builder().id(2L).name("プライベート").displayOrder(2).build())
            );

            // when
            List<CategoryDto> result = categoryService.findAll();

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getName()).isEqualTo("仕事");
            assertThat(result.get(1).getName()).isEqualTo("プライベート");
            verify(taskCategoryMapper).selectByExample(any(TaskCategoryExample.class));
            verify(categoryConverter).toDtoList(categories);
        }

        @Test
        @DisplayName("カテゴリーが存在しない場合は空のリストを返す")
        void shouldReturnEmptyListWhenNoCategories() {
            // given
            when(taskCategoryMapper.selectByExample(any(TaskCategoryExample.class))).thenReturn(List.of());
            when(categoryConverter.toDtoList(List.of())).thenReturn(List.of());

            // when
            List<CategoryDto> result = categoryService.findAll();

            // then
            assertThat(result).isEmpty();
            verify(taskCategoryMapper).selectByExample(any(TaskCategoryExample.class));
        }

        @Test
        @DisplayName("display_orderが同じ場合はnameでソートされる")
        void shouldSortByNameWhenDisplayOrderIsSame() {
            // given
            TaskCategory category1 = new TaskCategory();
            category1.setId(1L);
            category1.setName("仕事");
            category1.setDisplayOrder(1);

            TaskCategory category2 = new TaskCategory();
            category2.setId(2L);
            category2.setName("プライベート");
            category2.setDisplayOrder(1);

            TaskCategory category3 = new TaskCategory();
            category3.setId(3L);
            category3.setName("買い物");
            category3.setDisplayOrder(1);

            List<TaskCategory> categories = Arrays.asList(category1, category2, category3);
            when(taskCategoryMapper.selectByExample(any(TaskCategoryExample.class))).thenReturn(categories);

            CategoryDto dto1 = CategoryDto.builder().id(1L).name("仕事").displayOrder(1).build();
            CategoryDto dto2 = CategoryDto.builder().id(2L).name("プライベート").displayOrder(1).build();
            CategoryDto dto3 = CategoryDto.builder().id(3L).name("買い物").displayOrder(1).build();
            when(categoryConverter.toDtoList(categories)).thenReturn(Arrays.asList(dto1, dto2, dto3));

            // when
            List<CategoryDto> result = categoryService.findAll();

            // then
            assertThat(result).hasSize(3);
            verify(taskCategoryMapper).selectByExample(any(TaskCategoryExample.class));
            verify(categoryConverter).toDtoList(categories);
        }
    }

    @Nested
    @DisplayName("findByIdのテスト")
    class FindByIdTest {

        @Test
        @DisplayName("IDでカテゴリーを取得できる")
        void shouldReturnCategoryById() {
            // given
            when(taskCategoryMapper.selectByPrimaryKey(1L)).thenReturn(testCategory);
            when(categoryConverter.toDto(testCategory)).thenReturn(testCategoryDto);

            // when
            CategoryDto result = categoryService.findById(1L);

            // then
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getName()).isEqualTo("仕事");
            assertThat(result.getColor()).isEqualTo("#007bff");
            verify(taskCategoryMapper).selectByPrimaryKey(1L);
            verify(categoryConverter).toDto(testCategory);
        }

        @Test
        @DisplayName("存在しないIDの場合は例外をスローする")
        void shouldThrowExceptionWhenCategoryNotFound() {
            // given
            when(taskCategoryMapper.selectByPrimaryKey(999L)).thenReturn(null);

            // when & then
            assertThatThrownBy(() -> categoryService.findById(999L))
                    .isInstanceOf(CategoryNotFoundException.class)
                    .hasMessageContaining("999");
            verify(taskCategoryMapper).selectByPrimaryKey(999L);
            verify(categoryConverter, never()).toDto(any());
        }
    }

    @Nested
    @DisplayName("createのテスト")
    class CreateTest {

        @Test
        @DisplayName("カテゴリーを作成できる")
        void shouldCreateCategory() {
            // given
            when(categoryConverter.toEntity(testCategoryForm)).thenReturn(testCategory);
            when(taskCategoryMapper.insertSelective(any(TaskCategory.class))).thenReturn(1);
            when(categoryConverter.toDto(testCategory)).thenReturn(testCategoryDto);

            // when
            CategoryDto result = categoryService.create(testCategoryForm);

            // then
            assertThat(result.getName()).isEqualTo("仕事");
            assertThat(result.getColor()).isEqualTo("#007bff");
            verify(categoryConverter).toEntity(testCategoryForm);
            verify(taskCategoryMapper).insertSelective(any(TaskCategory.class));
            verify(categoryConverter).toDto(testCategory);
        }
    }

    @Nested
    @DisplayName("updateのテスト")
    class UpdateTest {

        @Test
        @DisplayName("カテゴリーを更新できる")
        void shouldUpdateCategory() {
            // given
            CategoryForm updateForm = CategoryForm.builder()
                    .name("仕事(更新)")
                    .description("更新された説明")
                    .color("#ff5733")
                    .displayOrder(2)
                    .build();

            when(taskCategoryMapper.selectByPrimaryKey(1L)).thenReturn(testCategory);
            doNothing().when(categoryConverter).updateEntity(any(CategoryForm.class), any(TaskCategory.class));
            when(taskCategoryMapper.updateByPrimaryKeySelective(any(TaskCategory.class))).thenReturn(1);
            when(categoryConverter.toDto(testCategory)).thenReturn(
                    CategoryDto.builder()
                            .id(1L)
                            .name("仕事(更新)")
                            .description("更新された説明")
                            .color("#ff5733")
                            .displayOrder(2)
                            .build()
            );

            // when
            CategoryDto result = categoryService.update(1L, updateForm);

            // then
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getName()).isEqualTo("仕事(更新)");
            verify(taskCategoryMapper).selectByPrimaryKey(1L);
            verify(categoryConverter).updateEntity(any(CategoryForm.class), any(TaskCategory.class));
            verify(taskCategoryMapper).updateByPrimaryKeySelective(any(TaskCategory.class));
            verify(categoryConverter).toDto(testCategory);
        }

        @Test
        @DisplayName("存在しないカテゴリーの更新は例外をスローする")
        void shouldThrowExceptionWhenUpdatingNonExistentCategory() {
            // given
            when(taskCategoryMapper.selectByPrimaryKey(999L)).thenReturn(null);

            // when & then
            assertThatThrownBy(() -> categoryService.update(999L, testCategoryForm))
                    .isInstanceOf(CategoryNotFoundException.class)
                    .hasMessageContaining("999");
            verify(taskCategoryMapper).selectByPrimaryKey(999L);
            verify(taskCategoryMapper, never()).updateByPrimaryKeySelective(any());
        }
    }

    @Nested
    @DisplayName("deleteのテスト")
    class DeleteTest {

        @Test
        @DisplayName("カテゴリーを削除できる")
        void shouldDeleteCategory() {
            // given
            when(taskCategoryMapper.selectByPrimaryKey(1L)).thenReturn(testCategory);
            when(categoryCustomMapper.countTasksByCategoryId(1L)).thenReturn(0L);
            when(taskCategoryMapper.deleteByPrimaryKey(1L)).thenReturn(1);

            // when
            categoryService.delete(1L);

            // then
            verify(taskCategoryMapper).selectByPrimaryKey(1L);
            verify(categoryCustomMapper).countTasksByCategoryId(1L);
            verify(taskCategoryMapper).deleteByPrimaryKey(1L);
        }

        @Test
        @DisplayName("存在しないカテゴリーの削除は例外をスローする")
        void shouldThrowExceptionWhenDeletingNonExistentCategory() {
            // given
            when(taskCategoryMapper.selectByPrimaryKey(999L)).thenReturn(null);

            // when & then
            assertThatThrownBy(() -> categoryService.delete(999L))
                    .isInstanceOf(CategoryNotFoundException.class)
                    .hasMessageContaining("999");
            verify(taskCategoryMapper).selectByPrimaryKey(999L);
            verify(categoryCustomMapper, never()).countTasksByCategoryId(anyLong());
            verify(taskCategoryMapper, never()).deleteByPrimaryKey(anyLong());
        }

        @Test
        @DisplayName("使用中のカテゴリーの削除は例外をスローする")
        void shouldThrowExceptionWhenDeletingCategoryInUse() {
            // given
            when(taskCategoryMapper.selectByPrimaryKey(1L)).thenReturn(testCategory);
            when(categoryCustomMapper.countTasksByCategoryId(1L)).thenReturn(5L);

            // when & then
            assertThatThrownBy(() -> categoryService.delete(1L))
                    .isInstanceOf(CategoryInUseException.class)
                    .hasMessageContaining("使用中のため削除できません");
            verify(taskCategoryMapper).selectByPrimaryKey(1L);
            verify(categoryCustomMapper).countTasksByCategoryId(1L);
            verify(taskCategoryMapper, never()).deleteByPrimaryKey(anyLong());
        }
    }
}
