package com.example.taskmanager;

import com.example.taskmanager.category.mapper.CategoryCustomMapper;
import com.example.taskmanager.common.mapper.TaskCategoryMapper;
import com.example.taskmanager.common.mapper.TaskMapper;
import com.example.taskmanager.task.mapper.TaskCustomMapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * タスク管理アプリケーションのエントリーポイント.
 *
 * <p>Spring Bootアプリケーションのメインクラス。
 * タスクの作成、編集、削除、一覧表示などの機能を提供する。</p>
 *
 * @author Task Manager Team
 * @version 1.0.0
 */
@SpringBootApplication
@MapperScan(basePackageClasses = {TaskMapper.class, TaskCategoryMapper.class, TaskCustomMapper.class, CategoryCustomMapper.class})
public class TaskManagerApplication {

    /**
     * アプリケーションを起動する.
     *
     * @param args コマンドライン引数
     */
    public static void main(String[] args) {
        SpringApplication.run(TaskManagerApplication.class, args);
    }
}
