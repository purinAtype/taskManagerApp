-- MyBatis Generator用 H2データベース初期化スクリプト
-- 実行方法: H2コンソールまたはコマンドラインで実行

-- 既存テーブルの削除（再生成用）
DROP TABLE IF EXISTS tasks;
DROP TABLE IF EXISTS task_categories;

-- タスクカテゴリーテーブル作成
CREATE TABLE task_categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'カテゴリーID',
    name VARCHAR(50) NOT NULL COMMENT 'カテゴリー名',
    description VARCHAR(200) COMMENT 'カテゴリー説明',
    color VARCHAR(7) DEFAULT '#6c757d' COMMENT '表示色（HEXカラーコード）',
    display_order INT DEFAULT 0 COMMENT '表示順',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '作成日時',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新日時'
);

-- タスクカテゴリーインデックス
CREATE INDEX IF NOT EXISTS idx_task_categories_name ON task_categories(name);
CREATE INDEX IF NOT EXISTS idx_task_categories_display_order ON task_categories(display_order);

-- tasksテーブル作成（category_id追加）
CREATE TABLE tasks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'タスクID',
    title VARCHAR(100) NOT NULL COMMENT 'タイトル',
    description TEXT COMMENT '説明',
    status VARCHAR(20) NOT NULL DEFAULT 'TODO' COMMENT 'ステータス（TODO/IN_PROGRESS/DONE）',
    priority VARCHAR(10) NOT NULL DEFAULT 'MEDIUM' COMMENT '優先度（LOW/MEDIUM/HIGH）',
    category_id BIGINT COMMENT 'カテゴリーID',
    due_date DATE COMMENT '期限日',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '作成日時',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新日時',
    CONSTRAINT fk_tasks_category FOREIGN KEY (category_id) REFERENCES task_categories(id) ON DELETE SET NULL
);

-- タスクインデックス作成
CREATE INDEX IF NOT EXISTS idx_tasks_status ON tasks(status);
CREATE INDEX IF NOT EXISTS idx_tasks_priority ON tasks(priority);
CREATE INDEX IF NOT EXISTS idx_tasks_category_id ON tasks(category_id);
CREATE INDEX IF NOT EXISTS idx_tasks_due_date ON tasks(due_date);

-- 初期カテゴリーデータ
INSERT INTO task_categories (name, description, color, display_order) VALUES
('仕事', '業務関連のタスク', '#0d6efd', 1),
('プライベート', '個人的なタスク', '#198754', 2),
('学習', '勉強・スキルアップ関連', '#ffc107', 3),
('その他', '分類できないタスク', '#6c757d', 99);
