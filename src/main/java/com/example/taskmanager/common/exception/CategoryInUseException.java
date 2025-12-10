package com.example.taskmanager.common.exception;

/**
 * カテゴリーが使用中のため削除できない場合にスローされる例外.
 *
 * <p>タスクに紐付いているカテゴリーを削除しようとした場合に使用する。</p>
 */
public class CategoryInUseException extends RuntimeException {

    /**
     * カテゴリーIDを指定して例外を生成する.
     *
     * @param id 削除できなかったカテゴリーID
     */
    public CategoryInUseException(Long id) {
        super("このカテゴリーは使用中のため削除できません。紐づくタスクを先に削除または変更してください。");
    }
}
