package com.example.taskmanager.category.service;

import com.example.taskmanager.category.dto.CategoryDto;
import com.example.taskmanager.category.form.CategoryForm;

import java.util.List;

/**
 * カテゴリー管理のサービスインターフェース.
 */
public interface CategoryService {

    /**
     * 全カテゴリーを表示順で取得する.
     *
     * @return カテゴリーDTOのリスト
     */
    List<CategoryDto> findAll();

    /**
     * IDを指定してカテゴリーを取得する.
     *
     * @param id カテゴリーID
     * @return カテゴリーDTO
     * @throws com.example.taskmanager.common.exception.CategoryNotFoundException カテゴリーが見つからない場合
     */
    CategoryDto findById(Long id);

    /**
     * カテゴリーを新規作成する.
     *
     * @param form カテゴリーフォーム
     * @return 作成されたカテゴリーDTO
     */
    CategoryDto create(CategoryForm form);

    /**
     * カテゴリーを更新する.
     *
     * @param id   更新するカテゴリーのID
     * @param form カテゴリーフォーム
     * @return 更新されたカテゴリーDTO
     * @throws com.example.taskmanager.common.exception.CategoryNotFoundException カテゴリーが見つからない場合
     */
    CategoryDto update(Long id, CategoryForm form);

    /**
     * カテゴリーを削除する.
     *
     * @param id 削除するカテゴリーのID
     * @throws com.example.taskmanager.common.exception.CategoryNotFoundException カテゴリーが見つからない場合
     * @throws com.example.taskmanager.common.exception.CategoryInUseException カテゴリーが使用中の場合
     */
    void delete(Long id);
}
