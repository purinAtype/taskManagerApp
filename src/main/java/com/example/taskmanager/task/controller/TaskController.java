package com.example.taskmanager.task.controller;

import com.example.taskmanager.common.entity.TaskCategory;
import com.example.taskmanager.common.enums.TaskPriority;
import com.example.taskmanager.common.enums.TaskStatus;
import com.example.taskmanager.task.converter.TaskConverter;
import com.example.taskmanager.task.dto.TaskDto;
import com.example.taskmanager.task.form.TaskForm;
import com.example.taskmanager.task.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * タスク管理のコントローラー.
 *
 * <p>タスクの一覧表示、詳細表示、登録、編集、削除を行う。</p>
 */
@Slf4j
@Controller
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskController {

    /** タスクサービス */
    private final TaskService taskService;

    /** タスクコンバーター */
    private final TaskConverter taskConverter;

    /**
     * タスク一覧を表示する.
     *
     * @param status     フィルター用ステータス（任意）
     * @param priority   フィルター用優先度（任意）
     * @param categoryId フィルター用カテゴリーID（任意）
     * @param model      ビューに渡すモデル
     * @return タスク一覧画面のビュー名
     */
    @GetMapping
    public String list(@RequestParam(required = false) TaskStatus status,
                       @RequestParam(required = false) TaskPriority priority,
                       @RequestParam(required = false) Long categoryId,
                       Model model) {
        log.debug("GET /tasks - status={}, priority={}, categoryId={}", status, priority, categoryId);

        List<TaskDto> tasks;
        if (status != null || priority != null || categoryId != null) {
            tasks = taskService.findByCondition(status, priority, categoryId);
        } else {
            tasks = taskService.findAll();
        }

        model.addAttribute("tasks", tasks);
        model.addAttribute("statuses", TaskStatus.values());
        model.addAttribute("priorities", TaskPriority.values());
        model.addAttribute("categories", taskService.findAllCategories());
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedPriority", priority);
        model.addAttribute("selectedCategoryId", categoryId);

        return "task/list";
    }

    /**
     * タスク詳細を表示する.
     *
     * @param id    表示するタスクのID
     * @param model ビューに渡すモデル
     * @return タスク詳細画面のビュー名
     */
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        log.debug("GET /tasks/{}", id);
        TaskDto task = taskService.findById(id);
        model.addAttribute("task", task);
        return "task/detail";
    }

    /**
     * タスク新規登録フォームを表示する.
     *
     * @param model ビューに渡すモデル
     * @return タスク登録フォーム画面のビュー名
     */
    @GetMapping("/new")
    public String newForm(Model model) {
        log.debug("GET /tasks/new");
        model.addAttribute("taskForm", TaskForm.builder()
                .status(TaskStatus.TODO)
                .priority(TaskPriority.MEDIUM)
                .build());
        model.addAttribute("statuses", TaskStatus.values());
        model.addAttribute("priorities", TaskPriority.values());
        model.addAttribute("categories", taskService.findAllCategories());
        return "task/form";
    }

    /**
     * タスクを新規登録する.
     *
     * @param taskForm           登録するタスクのフォームデータ
     * @param bindingResult      バリデーション結果
     * @param model              ビューに渡すモデル
     * @param redirectAttributes リダイレクト時に渡す属性
     * @return 成功時は詳細画面へリダイレクト、エラー時はフォーム画面のビュー名
     */
    @PostMapping
    public String create(@Valid @ModelAttribute TaskForm taskForm,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        log.debug("POST /tasks - {}", taskForm.getTitle());

        if (bindingResult.hasErrors()) {
            model.addAttribute("statuses", TaskStatus.values());
            model.addAttribute("priorities", TaskPriority.values());
            model.addAttribute("categories", taskService.findAllCategories());
            return "task/form";
        }

        TaskDto created = taskService.create(taskForm);
        redirectAttributes.addFlashAttribute("successMessage", "タスクを登録しました");
        return "redirect:/tasks/" + created.getId();
    }

    /**
     * タスク編集フォームを表示する.
     *
     * @param id    編集するタスクのID
     * @param model ビューに渡すモデル
     * @return タスク編集フォーム画面のビュー名
     */
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        log.debug("GET /tasks/{}/edit", id);
        TaskDto task = taskService.findById(id);
        model.addAttribute("taskForm", taskConverter.toForm(task));
        model.addAttribute("taskId", id);
        model.addAttribute("statuses", TaskStatus.values());
        model.addAttribute("priorities", TaskPriority.values());
        model.addAttribute("categories", taskService.findAllCategories());
        return "task/edit";
    }

    /**
     * タスクを更新する.
     *
     * @param id                 更新するタスクのID
     * @param taskForm           更新するタスクのフォームデータ
     * @param bindingResult      バリデーション結果
     * @param model              ビューに渡すモデル
     * @param redirectAttributes リダイレクト時に渡す属性
     * @return 成功時は詳細画面へリダイレクト、エラー時は編集フォーム画面のビュー名
     */
    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute TaskForm taskForm,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        log.debug("POST /tasks/{} - {}", id, taskForm.getTitle());

        if (bindingResult.hasErrors()) {
            model.addAttribute("taskId", id);
            model.addAttribute("statuses", TaskStatus.values());
            model.addAttribute("priorities", TaskPriority.values());
            model.addAttribute("categories", taskService.findAllCategories());
            return "task/edit";
        }

        taskService.update(id, taskForm);
        redirectAttributes.addFlashAttribute("successMessage", "タスクを更新しました");
        return "redirect:/tasks/" + id;
    }

    /**
     * タスクを削除する.
     *
     * @param id                 削除するタスクのID
     * @param redirectAttributes リダイレクト時に渡す属性
     * @return タスク一覧画面へのリダイレクト
     */
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        log.debug("POST /tasks/{}/delete", id);
        taskService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "タスクを削除しました");
        return "redirect:/tasks";
    }
}
