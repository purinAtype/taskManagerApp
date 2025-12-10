package com.example.taskmanager.task.converter;

import com.example.taskmanager.common.entity.Task;
import com.example.taskmanager.task.dto.TaskDto;
import com.example.taskmanager.task.form.TaskForm;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

/**
 * タスクオブジェクト間の変換を行うMapStructマッパーインターフェース.
 *
 * <p>Entity、DTO、Form間の相互変換を提供する。
 * MapStructにより実装クラスが自動生成される。</p>
 */
@Mapper(componentModel = "spring")
public interface TaskConverter {

    /**
     * TaskエンティティをTaskDtoに変換する.
     *
     * <p>カテゴリー情報（categoryName, categoryColor）は変換対象外。
     * カテゴリー情報が必要な場合はMapperでJOINして取得すること。</p>
     *
     * @param entity 変換元のTaskエンティティ
     * @return 変換後のTaskDto
     */
    @Mapping(target = "categoryName", ignore = true)
    @Mapping(target = "categoryColor", ignore = true)
    TaskDto toDto(Task entity);

    /**
     * TaskエンティティのリストをTaskDtoのリストに変換する.
     *
     * @param entities 変換元のTaskエンティティリスト
     * @return 変換後のTaskDtoリスト
     */
    List<TaskDto> toDtoList(List<Task> entities);

    /**
     * TaskFormからTaskエンティティを生成する.
     *
     * <p>id、createdAt、updatedAtは変換対象外（DB側で自動設定）。</p>
     *
     * @param form 変換元のTaskForm
     * @return 変換後のTaskエンティティ
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Task toEntity(TaskForm form);

    /**
     * TaskDtoをTaskFormに変換する.
     *
     * <p>編集画面でのフォーム初期値設定に使用する。</p>
     *
     * @param dto 変換元のTaskDto
     * @return 変換後のTaskForm
     */
    TaskForm toForm(TaskDto dto);

    /**
     * TaskFormの内容で既存のTaskエンティティを更新する.
     *
     * <p>id、createdAt、updatedAtは更新対象外。</p>
     *
     * @param form 更新内容を持つTaskForm
     * @param entity 更新対象のTaskエンティティ
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(TaskForm form, @MappingTarget Task entity);
}
