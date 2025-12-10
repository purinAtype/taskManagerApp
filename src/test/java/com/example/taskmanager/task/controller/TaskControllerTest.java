package com.example.taskmanager.task.controller;

import com.example.taskmanager.common.entity.TaskCategory;
import com.example.taskmanager.common.enums.TaskPriority;
import com.example.taskmanager.common.enums.TaskStatus;
import com.example.taskmanager.common.exception.TaskNotFoundException;
import com.example.taskmanager.task.converter.TaskConverter;
import com.example.taskmanager.task.dto.TaskDto;
import com.example.taskmanager.task.form.TaskForm;
import com.example.taskmanager.task.service.TaskService;
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

import java.time.LocalDate;
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
 * TaskControllerの単体テスト.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("TaskControllerのテスト")
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskService taskService;

    @MockBean
    private TaskConverter taskConverter;

    private TaskDto testTaskDto;
    private TaskForm testTaskForm;
    private List<TaskCategory> testCategories;

    @BeforeEach
    void setUp() {
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

        TaskCategory category = new TaskCategory();
        category.setId(1L);
        category.setName("仕事");
        category.setColor("#007bff");
        testCategories = Arrays.asList(category);
    }

    @Nested
    @DisplayName("一覧表示のテスト")
    class ListTest {

        @Test
        @DisplayName("タスク一覧を表示できる")
        void shouldShowTaskList() throws Exception {
            // given
            List<TaskDto> tasks = Arrays.asList(testTaskDto);
            when(taskService.findAll()).thenReturn(tasks);
            when(taskService.findAllCategories()).thenReturn(testCategories);

            // when & then
            mockMvc.perform(get("/tasks"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("task/list"))
                    .andExpect(model().attributeExists("tasks"))
                    .andExpect(model().attribute("tasks", hasSize(1)));
        }

        @Test
        @DisplayName("フィルター付きで一覧を表示できる")
        void shouldShowFilteredTaskList() throws Exception {
            // given
            List<TaskDto> tasks = Arrays.asList(testTaskDto);
            when(taskService.findByCondition(TaskStatus.TODO, null, null)).thenReturn(tasks);
            when(taskService.findAllCategories()).thenReturn(testCategories);

            // when & then
            mockMvc.perform(get("/tasks")
                            .param("status", "TODO"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("task/list"))
                    .andExpect(model().attribute("selectedStatus", TaskStatus.TODO));
        }
    }

    @Nested
    @DisplayName("詳細表示のテスト")
    class DetailTest {

        @Test
        @DisplayName("タスク詳細を表示できる")
        void shouldShowTaskDetail() throws Exception {
            // given
            when(taskService.findById(1L)).thenReturn(testTaskDto);

            // when & then
            mockMvc.perform(get("/tasks/1"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("task/detail"))
                    .andExpect(model().attributeExists("task"))
                    .andExpect(model().attribute("task", hasProperty("title", is("テストタスク"))));
        }

        @Test
        @DisplayName("存在しないタスクの詳細はエラーページを表示")
        void shouldShowErrorWhenTaskNotFound() throws Exception {
            // given
            when(taskService.findById(999L)).thenThrow(new TaskNotFoundException(999L));

            // when & then
            mockMvc.perform(get("/tasks/999"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("error/404"))
                    .andExpect(model().attributeExists("errorMessage"));
        }
    }

    @Nested
    @DisplayName("新規登録のテスト")
    class CreateTest {

        @Test
        @DisplayName("新規登録フォームを表示できる")
        void shouldShowNewForm() throws Exception {
            // given
            when(taskService.findAllCategories()).thenReturn(testCategories);

            // when & then
            mockMvc.perform(get("/tasks/new"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("task/form"))
                    .andExpect(model().attributeExists("taskForm"));
        }

        @Test
        @DisplayName("タスクを登録できる")
        void shouldCreateTask() throws Exception {
            // given
            when(taskService.create(any(TaskForm.class))).thenReturn(testTaskDto);
            when(taskService.findAllCategories()).thenReturn(testCategories);

            // when & then
            mockMvc.perform(post("/tasks")
                            .param("title", "テストタスク")
                            .param("description", "テストの説明")
                            .param("status", "TODO")
                            .param("priority", "HIGH")
                            .param("dueDate", "2025-12-31"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/tasks/1"));
        }

        @Test
        @DisplayName("バリデーションエラー時はフォームを再表示")
        void shouldShowFormOnValidationError() throws Exception {
            // given
            when(taskService.findAllCategories()).thenReturn(testCategories);

            // when & then
            mockMvc.perform(post("/tasks")
                            .param("title", "")  // 空のタイトル
                            .param("status", "TODO")
                            .param("priority", "HIGH"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("task/form"))
                    .andExpect(model().hasErrors());
        }
    }

    @Nested
    @DisplayName("編集のテスト")
    class UpdateTest {

        @Test
        @DisplayName("編集フォームを表示できる")
        void shouldShowEditForm() throws Exception {
            // given
            when(taskService.findById(1L)).thenReturn(testTaskDto);
            when(taskConverter.toForm(testTaskDto)).thenReturn(testTaskForm);
            when(taskService.findAllCategories()).thenReturn(testCategories);

            // when & then
            mockMvc.perform(get("/tasks/1/edit"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("task/edit"))
                    .andExpect(model().attributeExists("taskForm"))
                    .andExpect(model().attribute("taskId", 1L));
        }

        @Test
        @DisplayName("タスクを更新できる")
        void shouldUpdateTask() throws Exception {
            // given
            when(taskService.update(eq(1L), any(TaskForm.class))).thenReturn(testTaskDto);
            when(taskService.findAllCategories()).thenReturn(testCategories);

            // when & then
            mockMvc.perform(post("/tasks/1")
                            .param("title", "更新タスク")
                            .param("description", "更新の説明")
                            .param("status", "IN_PROGRESS")
                            .param("priority", "MEDIUM"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/tasks/1"));
        }
    }

    @Nested
    @DisplayName("削除のテスト")
    class DeleteTest {

        @Test
        @DisplayName("タスクを削除できる")
        void shouldDeleteTask() throws Exception {
            // given
            doNothing().when(taskService).delete(1L);

            // when & then
            mockMvc.perform(post("/tasks/1/delete"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/tasks"));
        }
    }
}
