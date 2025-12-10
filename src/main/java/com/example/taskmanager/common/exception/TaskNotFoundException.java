package com.example.taskmanager.common.exception;

/**
 * タスクが見つからない場合にスローされる例外.
 *
 * <p>指定されたIDのタスクがデータベースに存在しない場合に使用する。</p>
 */
public class TaskNotFoundException extends RuntimeException {

    /**
     * タスクIDを指定して例外を生成する.
     *
     * @param id 見つからなかったタスクのID
     */
    public TaskNotFoundException(Long id) {
        super("タスクが見つかりません: ID=" + id);
    }

    /**
     * メッセージを指定して例外を生成する.
     *
     * @param message エラーメッセージ
     */
    public TaskNotFoundException(String message) {
        super(message);
    }
}
