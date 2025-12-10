-- タスクカテゴリーテーブル作成
CREATE TABLE IF NOT EXISTS task_categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    description VARCHAR(200),
    color VARCHAR(7) DEFAULT '#6c757d',
    display_order INT DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- タスクカテゴリーインデックス
CREATE INDEX IF NOT EXISTS idx_task_categories_name ON task_categories(name);
CREATE INDEX IF NOT EXISTS idx_task_categories_display_order ON task_categories(display_order);

-- tasks テーブル作成
CREATE TABLE IF NOT EXISTS tasks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'TODO',
    priority VARCHAR(10) NOT NULL DEFAULT 'MEDIUM',
    category_id BIGINT,
    due_date DATE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_tasks_category FOREIGN KEY (category_id) REFERENCES task_categories(id) ON DELETE SET NULL
);

-- タスクインデックス作成
CREATE INDEX IF NOT EXISTS idx_tasks_status ON tasks(status);
CREATE INDEX IF NOT EXISTS idx_tasks_priority ON tasks(priority);
CREATE INDEX IF NOT EXISTS idx_tasks_category_id ON tasks(category_id);
CREATE INDEX IF NOT EXISTS idx_tasks_due_date ON tasks(due_date);
