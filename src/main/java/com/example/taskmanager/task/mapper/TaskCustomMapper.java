package com.example.taskmanager.task.mapper;

import com.example.taskmanager.common.entity.Task;
import com.example.taskmanager.task.dto.TaskDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * タスク管理用のカスタムMyBatisマッパーインターフェース.
 *
 * <p>タスクのCRUD操作およびカテゴリー情報を含む検索機能を提供する。
 * SQLマッピングはresources/mapper/custom/task/TaskCustomMapper.xmlで定義。</p>
 */
@Mapper
public interface TaskCustomMapper {

    /**
     * 全タスクを取得する.
     *
     * @return タスクエンティティのリスト
     */
    List<Task> selectAll();

    /**
     * 条件を指定してタスクを検索する.
     *
     * <p>各パラメータがnullの場合、その条件は無視される。</p>
     *
     * @param status ステータス（TODO, IN_PROGRESS, DONE）
     * @param priority 優先度（HIGH, MEDIUM, LOW）
     * @param categoryId カテゴリーID
     * @return 条件に一致するタスクエンティティのリスト
     */
    List<Task> selectByCondition(@Param("status") String status,
                                  @Param("priority") String priority,
                                  @Param("categoryId") Long categoryId);

    /**
     * IDを指定してタスクを1件取得する.
     *
     * @param id タスクID
     * @return タスクエンティティ（存在しない場合はnull）
     */
    Task selectById(@Param("id") Long id);

    /**
     * カテゴリー情報を含む全タスクを取得する.
     *
     * <p>タスクカテゴリーテーブルとJOINし、カテゴリー名・色情報を含むDTOを返す。</p>
     *
     * @return カテゴリー情報付きTaskDtoのリスト
     */
    List<TaskDto> selectAllWithCategory();

    /**
     * カテゴリー情報を含むタスクを条件指定で検索する.
     *
     * <p>各パラメータがnullの場合、その条件は無視される。</p>
     *
     * @param status ステータス（TODO, IN_PROGRESS, DONE）
     * @param priority 優先度（HIGH, MEDIUM, LOW）
     * @param categoryId カテゴリーID
     * @return 条件に一致するカテゴリー情報付きTaskDtoのリスト
     */
    List<TaskDto> selectByConditionWithCategory(@Param("status") String status,
                                                 @Param("priority") String priority,
                                                 @Param("categoryId") Long categoryId);

    /**
     * カテゴリー情報を含むタスクをIDで1件取得する.
     *
     * @param id タスクID
     * @return カテゴリー情報付きTaskDto（存在しない場合はnull）
     */
    TaskDto selectByIdWithCategory(@Param("id") Long id);
}
