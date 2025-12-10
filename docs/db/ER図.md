# ER図

**Source**: `src/main/resources/schema.sql`
**Date**: 2025-12-09

## Schema

```mermaid
erDiagram
    TASK_CATEGORIES {
        bigint id PK "ID"
        varchar name "カテゴリー名"
        varchar description "説明"
        varchar color "カラーコード"
        int display_order "表示順"
        timestamp created_at "作成日時"
        timestamp updated_at "更新日時"
    }

    TASKS {
        bigint id PK "ID"
        varchar title "タイトル"
        text description "説明"
        varchar status "ステータス"
        varchar priority "優先度"
        bigint category_id FK "カテゴリーID"
        date due_date "期限"
        timestamp created_at "作成日時"
        timestamp updated_at "更新日時"
    }

    TASK_CATEGORIES ||--o{ TASKS : "has"
```

## Tables

| No | Physical Name | Logical Name | Comment |
|:---|:---|:---|:---|
| 1 | task_categories | タスクカテゴリー | タスクカテゴリー管理テーブル |
| 2 | tasks | タスク | タスク管理テーブル |

## Relationships

| From Table | Relationship | To Table | Description |
|:---|:---|:---|:---|
| TASK_CATEGORIES | 1対多 | TASKS | 1つのカテゴリーは複数のタスクを持つ |

## Indexes

### task_categories
- `idx_task_categories_name` on `name`
- `idx_task_categories_display_order` on `display_order`

### tasks
- `idx_tasks_status` on `status`
- `idx_tasks_priority` on `priority`
- `idx_tasks_category_id` on `category_id`
- `idx_tasks_due_date` on `due_date`

## Constraints

### Foreign Keys
- **fk_tasks_category**: `tasks.category_id` → `task_categories.id`
  - ON DELETE: SET NULL
  - 説明: タスクは任意でカテゴリーに紐づく。カテゴリー削除時はNULLに設定される。
