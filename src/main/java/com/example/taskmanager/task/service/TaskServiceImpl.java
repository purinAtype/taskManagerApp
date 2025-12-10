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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * タスク管理のサービス実装クラス.
 *
 * <p>タスクのCRUD操作およびカテゴリー取得機能を提供する。
 * トランザクション管理を行い、データの整合性を保証する。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    /** タスクマッパー（自動生成） */
    private final TaskMapper taskMapper;

    /** タスクカスタムマッパー */
    private final TaskCustomMapper taskCustomMapper;

    /** カテゴリーマッパー */
    private final TaskCategoryMapper taskCategoryMapper;

    /** タスクコンバーター */
    private final TaskConverter taskConverter;

    /**
     * 全タスクを取得する.
     *
     * @return タスクDTOのリスト
     */
    @Override
    @Transactional(readOnly = true)
    public List<TaskDto> findAll() {
        log.debug("Finding all tasks with category");
        return taskCustomMapper.selectAllWithCategory();
    }

    /**
     * 条件を指定してタスクを検索する.
     *
     * @param status     ステータス（nullの場合は条件なし）
     * @param priority   優先度（nullの場合は条件なし）
     * @param categoryId カテゴリーID（nullの場合は条件なし）
     * @return 条件に一致するタスクDTOのリスト
     */
    @Override
    @Transactional(readOnly = true)
    public List<TaskDto> findByCondition(TaskStatus status, TaskPriority priority, Long categoryId) {
        log.debug("Finding tasks by condition: status={}, priority={}, categoryId={}", status, priority, categoryId);
        String statusStr = status != null ? status.name() : null;
        String priorityStr = priority != null ? priority.name() : null;
        return taskCustomMapper.selectByConditionWithCategory(statusStr, priorityStr, categoryId);
    }

    /**
     * IDを指定してタスクを取得する.
     *
     * @param id タスクID
     * @return タスクDTO
     * @throws TaskNotFoundException タスクが見つからない場合
     */
    @Override
    @Transactional(readOnly = true)
    public TaskDto findById(Long id) {
        log.debug("Finding task by id: {}", id);
        TaskDto task = taskCustomMapper.selectByIdWithCategory(id);
        if (task == null) {
            throw new TaskNotFoundException(id);
        }
        return task;
    }

    /**
     * タスクを新規作成する.
     *
     * @param form タスクフォーム
     * @return 作成されたタスクDTO
     */
    @Override
    @Transactional
    public TaskDto create(TaskForm form) {
        log.debug("Creating task: {}", form.getTitle());
        Task task = taskConverter.toEntity(form);
        taskMapper.insertSelective(task);
        log.info("Task created: id={}", task.getId());
        return taskCustomMapper.selectByIdWithCategory(task.getId());
    }

    /**
     * タスクを更新する.
     *
     * @param id   更新するタスクのID
     * @param form タスクフォーム
     * @return 更新されたタスクDTO
     * @throws TaskNotFoundException タスクが見つからない場合
     */
    @Override
    @Transactional
    public TaskDto update(Long id, TaskForm form) {
        log.debug("Updating task: id={}", id);
        Task existingTask = taskMapper.selectByPrimaryKey(id);
        if (existingTask == null) {
            throw new TaskNotFoundException(id);
        }
        taskConverter.updateEntity(form, existingTask);
        taskMapper.updateByPrimaryKeySelective(existingTask);
        log.info("Task updated: id={}", id);
        return taskCustomMapper.selectByIdWithCategory(id);
    }

    /**
     * タスクを削除する.
     *
     * @param id 削除するタスクのID
     * @throws TaskNotFoundException タスクが見つからない場合
     */
    @Override
    @Transactional
    public void delete(Long id) {
        log.debug("Deleting task: id={}", id);
        Task existingTask = taskMapper.selectByPrimaryKey(id);
        if (existingTask == null) {
            throw new TaskNotFoundException(id);
        }
        taskMapper.deleteByPrimaryKey(id);
        log.info("Task deleted: id={}", id);
    }

    /**
     * 全カテゴリーを表示順で取得する.
     *
     * @return カテゴリーエンティティのリスト
     */
    @Override
    @Transactional(readOnly = true)
    public List<TaskCategory> findAllCategories() {
        log.debug("Finding all categories");
        TaskCategoryExample example = new TaskCategoryExample();
        example.setOrderByClause("DISPLAY_ORDER ASC");
        return taskCategoryMapper.selectByExample(example);
    }
}
