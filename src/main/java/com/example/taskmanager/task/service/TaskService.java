package com.example.taskmanager.task.service;

import com.example.taskmanager.common.entity.TaskCategory;
import com.example.taskmanager.common.enums.TaskPriority;
import com.example.taskmanager.common.enums.TaskStatus;
import com.example.taskmanager.task.dto.TaskDto;
import com.example.taskmanager.task.form.TaskForm;

import java.util.List;

/**
 * タスク管理のサービスインターフェース.
 */
public interface TaskService {

    /**
     * 全タスクを取得する.
     *
     * @return タスクDTOのリスト
     */
    List<TaskDto> findAll();

    /**
     * 条件を指定してタスクを検索する.
     *
     * @param status     ステータス（nullの場合は条件なし）
     * @param priority   優先度（nullの場合は条件なし）
     * @param categoryId カテゴリーID（nullの場合は条件なし）
     * @return 条件に一致するタスクDTOのリスト
     */
    List<TaskDto> findByCondition(TaskStatus status, TaskPriority priority, Long categoryId);

    /**
     * IDを指定してタスクを取得する.
     *
     * @param id タスクID
     * @return タスクDTO
     * @throws com.example.taskmanager.common.exception.TaskNotFoundException タスクが見つからない場合
     */
    TaskDto findById(Long id);

    /**
     * タスクを新規作成する.
     *
     * @param form タスクフォーム
     * @return 作成されたタスクDTO
     */
    TaskDto create(TaskForm form);

    /**
     * タスクを更新する.
     *
     * @param id   更新するタスクのID
     * @param form タスクフォーム
     * @return 更新されたタスクDTO
     * @throws com.example.taskmanager.common.exception.TaskNotFoundException タスクが見つからない場合
     */
    TaskDto update(Long id, TaskForm form);

    /**
     * タスクを削除する.
     *
     * @param id 削除するタスクのID
     * @throws com.example.taskmanager.common.exception.TaskNotFoundException タスクが見つからない場合
     */
    void delete(Long id);

    /**
     * 全カテゴリーを表示順で取得する.
     *
     * @return カテゴリーエンティティのリスト
     */
    List<TaskCategory> findAllCategories();
}
