package com.example.taskmanager.category.form;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * カテゴリーのフォームクラス.
 *
 * <p>カテゴリーの新規登録・編集時に画面から受け取るデータを保持する。
 * バリデーションアノテーションによる入力検証を行う。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryForm {

    /** カテゴリー名（必須、最大50文字） */
    @NotBlank(message = "カテゴリー名を入力してください")
    @Size(max = 50, message = "カテゴリー名は50文字以内で入力してください")
    private String name;

    /** 説明（任意、最大200文字） */
    @Size(max = 200, message = "説明は200文字以内で入力してください")
    private String description;

    /** カラーコード（任意、HEX形式: #RRGGBB） */
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "カラーコードの形式が不正です（例: #FF5733）")
    private String color;

    /** 表示順（任意、0以上） */
    @Min(value = 0, message = "表示順は0以上の整数を入力してください")
    private Integer displayOrder;
}
