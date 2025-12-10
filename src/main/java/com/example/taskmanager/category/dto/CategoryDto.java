package com.example.taskmanager.category.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * カテゴリーのデータ転送オブジェクト.
 *
 * <p>画面表示用にカテゴリー情報を保持するDTO。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDto {

    /** カテゴリーID */
    private Long id;

    /** カテゴリー名 */
    private String name;

    /** 説明 */
    private String description;

    /** カラーコード（HEX形式: #RRGGBB） */
    private String color;

    /** 表示順 */
    private Integer displayOrder;

    /** 作成日時 */
    private LocalDateTime createdAt;

    /** 更新日時 */
    private LocalDateTime updatedAt;
}
