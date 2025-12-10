package com.example.taskmanager.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * タスクの優先度を表す列挙型.
 *
 * <p>タスクの重要度・緊急度を管理するために使用する。</p>
 */
@Getter
@RequiredArgsConstructor
public enum TaskPriority {

    /** 低優先度 */
    LOW("低"),

    /** 中優先度 */
    MEDIUM("中"),

    /** 高優先度 */
    HIGH("高");

    /** 画面表示用の名称 */
    private final String displayName;
}
