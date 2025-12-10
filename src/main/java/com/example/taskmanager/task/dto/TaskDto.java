package com.example.taskmanager.task.dto;

import com.example.taskmanager.common.enums.TaskPriority;
import com.example.taskmanager.common.enums.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * タスクのデータ転送オブジェクト.
 *
 * <p>画面表示用にタスク情報を保持するDTO。
 * カテゴリー情報も含めて一括で取得・表示するために使用する。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskDto {

    /** タスクID */
    private Long id;

    /** タイトル */
    private String title;

    /** 説明 */
    private String description;

    /** ステータス */
    private TaskStatus status;

    /** 優先度 */
    private TaskPriority priority;

    /** カテゴリーID */
    private Long categoryId;

    /** カテゴリー名 */
    private String categoryName;

    /** カテゴリー色 */
    private String categoryColor;

    /** 期限日 */
    private LocalDate dueDate;

    /** 作成日時 */
    private LocalDateTime createdAt;

    /** 更新日時 */
    private LocalDateTime updatedAt;

    /**
     * ステータスの表示名を取得する.
     *
     * @return ステータスの表示名、ステータスがnullの場合は空文字
     */
    public String getStatusDisplayName() {
        return status != null ? status.getDisplayName() : "";
    }

    /**
     * 優先度の表示名を取得する.
     *
     * @return 優先度の表示名、優先度がnullの場合は空文字
     */
    public String getPriorityDisplayName() {
        return priority != null ? priority.getDisplayName() : "";
    }
}
