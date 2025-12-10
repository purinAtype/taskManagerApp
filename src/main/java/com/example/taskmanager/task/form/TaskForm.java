package com.example.taskmanager.task.form;

import com.example.taskmanager.common.enums.TaskPriority;
import com.example.taskmanager.common.enums.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * タスクのフォームクラス.
 *
 * <p>タスクの新規登録・編集時に画面から受け取るデータを保持する。
 * バリデーションアノテーションによる入力検証を行う。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskForm {

    /** タイトル（必須、最大100文字） */
    @NotBlank(message = "タイトルを入力してください")
    @Size(max = 100, message = "タイトルは100文字以内で入力してください")
    private String title;

    /** 説明（任意、最大1000文字） */
    @Size(max = 1000, message = "説明は1000文字以内で入力してください")
    private String description;

    /** ステータス（必須） */
    @NotNull(message = "ステータスを選択してください")
    private TaskStatus status;

    /** 優先度（必須） */
    @NotNull(message = "優先度を選択してください")
    private TaskPriority priority;

    /** カテゴリーID（任意） */
    private Long categoryId;

    /** 期限日（任意） */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dueDate;
}
