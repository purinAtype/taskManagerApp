package com.example.taskmanager.task.service;

import com.example.taskmanager.common.entity.Task;
import com.example.taskmanager.common.entity.TaskCategory;
import com.example.taskmanager.common.entity.TaskCategoryExample;
import com.example.taskmanager.common.enums.TaskPriority;
import com.example.taskmanager.common.enums.TaskStatus;
import com.example.taskmanager.common.exception.TaskNotFoundException;
import com.example.taskmanager.common.mapper.TaskCategoryMapper;
import com.example.taskmanager.common.mapper.TaskMapper;
import com.example.taskmanager.task.converter.TaskConverter;
import com.example.taskmanager.task.dto.TaskDto;
import com.example.taskmanager.task.form.TaskForm;
import com.example.taskmanager.task.mapper.TaskCustomMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * TaskServiceの単体テスト.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TaskServiceのテスト")
class TaskServiceTest {

    @Mock
    private TaskMapper taskMapper;

    @Mock
    private TaskCustomMapper taskCustomMapper;

    @Mock
    private TaskCategoryMapper taskCategoryMapper;

    @Mock
    private TaskConverter taskConverter;

    @InjectMocks
    private TaskServiceImpl taskService;

    private Task testTask;
    private TaskDto testTaskDto;
    private TaskForm testTaskForm;

    @BeforeEach
    void setUp() {
        testTask = new Task();
        testTask.setId(1L);
        testTask.setTitle("テストタスク");
        testTask.setDescription("テストの説明");
        testTask.setStatus("TODO");
        testTask.setPriority("HIGH");
        testTask.setCategoryId(1L);
        testTask.setDueDate(LocalDate.of(2025, 12, 31));
        testTask.setCreatedAt(LocalDateTime.now());
        testTask.setUpdatedAt(LocalDateTime.now());

        testTaskDto = TaskDto.builder()
                .id(1L)
                .title("テストタスク")
                .description("テストの説明")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.HIGH)
                .categoryId(1L)
                .categoryName("仕事")
                .categoryColor("#007bff")
                .dueDate(LocalDate.of(2025, 12, 31))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testTaskForm = TaskForm.builder()
                .title("テストタスク")
                .description("テストの説明")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.HIGH)
                .categoryId(1L)
                .dueDate(LocalDate.of(2025, 12, 31))
                .build();
    }

    @Nested
    @DisplayName("findAllのテスト")
    class FindAllTest {

        @Test
        @DisplayName("全タスクをカテゴリー情報付きで取得できる")
        void shouldReturnAllTasksWithCategory() {
            // given
            List<TaskDto> taskDtos = Arrays.asList(testTaskDto);
            when(taskCustomMapper.selectAllWithCategory()).thenReturn(taskDtos);

            // when
            List<TaskDto> result = taskService.findAll();

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTitle()).isEqualTo("テストタスク");
            assertThat(result.get(0).getCategoryName()).isEqualTo("仕事");
            verify(taskCustomMapper).selectAllWithCategory();
        }

        @Test
        @DisplayName("タスクが存在しない場合は空のリストを返す")
        void shouldReturnEmptyListWhenNoTasks() {
            // given
            when(taskCustomMapper.selectAllWithCategory()).thenReturn(List.of());

            // when
            List<TaskDto> result = taskService.findAll();

            // then
            assertThat(result).isEmpty();
            verify(taskCustomMapper).selectAllWithCategory();
        }
    }

    @Nested
    @DisplayName("findByConditionのテスト")
    class FindByConditionTest {

        @Test
        @DisplayName("条件指定でタスクを検索できる")
        void shouldReturnTasksByCondition() {
            // given
            List<TaskDto> taskDtos = Arrays.asList(testTaskDto);
            when(taskCustomMapper.selectByConditionWithCategory("TODO", "HIGH", 1L)).thenReturn(taskDtos);

            // when
            List<TaskDto> result = taskService.findByCondition(TaskStatus.TODO, TaskPriority.HIGH, 1L);

            // then
            assertThat(result).hasSize(1);
            verify(taskCustomMapper).selectByConditionWithCategory("TODO", "HIGH", 1L);
        }

        @Test
        @DisplayName("条件がnullの場合はnullを渡す")
        void shouldPassNullWhenConditionIsNull() {
            // given
            when(taskCustomMapper.selectByConditionWithCategory(null, null, null)).thenReturn(List.of());

            // when
            List<TaskDto> result = taskService.findByCondition(null, null, null);

            // then
            assertThat(result).isEmpty();
            verify(taskCustomMapper).selectByConditionWithCategory(null, null, null);
        }
    }

    @Nested
    @DisplayName("findByIdのテスト")
    class FindByIdTest {

        @Test
        @DisplayName("IDでタスクを取得できる")
        void shouldReturnTaskById() {
            // given
            when(taskCustomMapper.selectByIdWithCategory(1L)).thenReturn(testTaskDto);

            // when
            TaskDto result = taskService.findById(1L);

            // then
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getTitle()).isEqualTo("テストタスク");
            assertThat(result.getCategoryName()).isEqualTo("仕事");
            verify(taskCustomMapper).selectByIdWithCategory(1L);
        }

        @Test
        @DisplayName("存在しないIDの場合は例外をスローする")
        void shouldThrowExceptionWhenTaskNotFound() {
            // given
            when(taskCustomMapper.selectByIdWithCategory(999L)).thenReturn(null);

            // when & then
            assertThatThrownBy(() -> taskService.findById(999L))
                    .isInstanceOf(TaskNotFoundException.class)
                    .hasMessageContaining("999");
        }
    }

    @Nested
    @DisplayName("createのテスト")
    class CreateTest {

        @Test
        @DisplayName("タスクを作成できる")
        void shouldCreateTask() {
            // given
            when(taskConverter.toEntity(testTaskForm)).thenReturn(testTask);
            when(taskMapper.insertSelective(any(Task.class))).thenReturn(1);
            when(taskCustomMapper.selectByIdWithCategory(testTask.getId())).thenReturn(testTaskDto);

            // when
            TaskDto result = taskService.create(testTaskForm);

            // then
            assertThat(result.getTitle()).isEqualTo("テストタスク");
            verify(taskMapper).insertSelective(any(Task.class));
            verify(taskCustomMapper).selectByIdWithCategory(testTask.getId());
        }
    }

    @Nested
    @DisplayName("updateのテスト")
    class UpdateTest {

        @Test
        @DisplayName("タスクを更新できる")
        void shouldUpdateTask() {
            // given
            when(taskMapper.selectByPrimaryKey(1L)).thenReturn(testTask);
            doNothing().when(taskConverter).updateEntity(any(TaskForm.class), any(Task.class));
            when(taskMapper.updateByPrimaryKeySelective(any(Task.class))).thenReturn(1);
            when(taskCustomMapper.selectByIdWithCategory(1L)).thenReturn(testTaskDto);

            // when
            TaskDto result = taskService.update(1L, testTaskForm);

            // then
            assertThat(result.getTitle()).isEqualTo("テストタスク");
            verify(taskMapper).selectByPrimaryKey(1L);
            verify(taskConverter).updateEntity(any(TaskForm.class), any(Task.class));
            verify(taskMapper).updateByPrimaryKeySelective(any(Task.class));
            verify(taskCustomMapper).selectByIdWithCategory(1L);
        }

        @Test
        @DisplayName("存在しないタスクの更新は例外をスローする")
        void shouldThrowExceptionWhenUpdatingNonExistentTask() {
            // given
            when(taskMapper.selectByPrimaryKey(999L)).thenReturn(null);

            // when & then
            assertThatThrownBy(() -> taskService.update(999L, testTaskForm))
                    .isInstanceOf(TaskNotFoundException.class);
            verify(taskMapper, never()).updateByPrimaryKeySelective(any(Task.class));
        }
    }

    @Nested
    @DisplayName("deleteのテスト")
    class DeleteTest {

        @Test
        @DisplayName("タスクを削除できる")
        void shouldDeleteTask() {
            // given
            when(taskMapper.selectByPrimaryKey(1L)).thenReturn(testTask);
            when(taskMapper.deleteByPrimaryKey(1L)).thenReturn(1);

            // when
            taskService.delete(1L);

            // then
            verify(taskMapper).selectByPrimaryKey(1L);
            verify(taskMapper).deleteByPrimaryKey(1L);
        }

        @Test
        @DisplayName("存在しないタスクの削除は例外をスローする")
        void shouldThrowExceptionWhenDeletingNonExistentTask() {
            // given
            when(taskMapper.selectByPrimaryKey(999L)).thenReturn(null);

            // when & then
            assertThatThrownBy(() -> taskService.delete(999L))
                    .isInstanceOf(TaskNotFoundException.class);
            verify(taskMapper, never()).deleteByPrimaryKey(anyLong());
        }
    }

    @Nested
    @DisplayName("findAllCategoriesのテスト")
    class FindAllCategoriesTest {

        @Test
        @DisplayName("全カテゴリーを表示順で取得できる")
        void shouldReturnAllCategoriesOrderedByDisplayOrder() {
            // given
            TaskCategory category1 = new TaskCategory();
            category1.setId(1L);
            category1.setName("仕事");
            category1.setDisplayOrder(1);

            TaskCategory category2 = new TaskCategory();
            category2.setId(2L);
            category2.setName("プライベート");
            category2.setDisplayOrder(2);

            List<TaskCategory> categories = Arrays.asList(category1, category2);
            when(taskCategoryMapper.selectByExample(any(TaskCategoryExample.class))).thenReturn(categories);

            // when
            List<TaskCategory> result = taskService.findAllCategories();

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getName()).isEqualTo("仕事");
            assertThat(result.get(1).getName()).isEqualTo("プライベート");
            verify(taskCategoryMapper).selectByExample(any(TaskCategoryExample.class));
        }
    }
}
