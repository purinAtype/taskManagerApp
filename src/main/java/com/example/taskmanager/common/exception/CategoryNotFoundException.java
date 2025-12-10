package com.example.taskmanager.common.exception;

/**
 * カテゴリーが見つからない場合にスローされる例外.
 *
 * <p>指定されたIDのカテゴリーがデータベースに存在しない場合に使用する。</p>
 */
public class CategoryNotFoundException extends RuntimeException {

    /**
     * カテゴリーIDを指定して例外を生成する.
     *
     * @param id 見つからなかったカテゴリーID
     */
    public CategoryNotFoundException(Long id) {
        super("カテゴリーが見つかりません: ID=" + id);
    }
}
