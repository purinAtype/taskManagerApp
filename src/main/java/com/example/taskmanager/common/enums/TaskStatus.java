package com.example.taskmanager.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * タスクのステータスを表す列挙型.
 *
 * <p>タスクの進捗状態を管理するために使用する。</p>
 */
@Getter
@RequiredArgsConstructor
public enum TaskStatus {

    /** 未着手 */
    TODO("未着手"),

    /** 進行中 */
    IN_PROGRESS("進行中"),

    /** 完了 */
    DONE("完了");

    /** 画面表示用の名称 */
    private final String displayName;
}
