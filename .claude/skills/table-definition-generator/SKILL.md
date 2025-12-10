---
name: table-definition-generator
description: |
  Parses H2/MySQL DDL (CREATE TABLE/INDEX) to generate a Markdown Table Definition Document.
  Extracts Columns, Types, PK/FK (bi-directional), Constraints, Defaults, and Indexes.
allowed-tools: Read, Glob, Write
---
# DDL to Table Definition Document Generator

H2/MySQL DDLを解析し、詳細なテーブル定義書（Markdown）を作成する専門スキル。

## 処理フロー

1.  **Read**: `src/main/resources/schema.sql` (または指定ファイル) を読み込む。
2.  **Analyze**:
    * `CREATE TABLE` ブロックからカラム・制約・コメントを抽出。
    * `CREATE INDEX` ステートメントを抽出。
    * **FK解析**: 全テーブルをスキャンし、双方向（参照/被参照）のリストを作成。
3.  **Generate**: 解析した**すべてのテーブルについて**、以下のテンプレート形式で記述を生成し連結する。
4.  **Write**: `docs/db/テーブル定義書.md` に出力。

## 1. 解析・変換ルール

### カラム定義抽出
* **物理名**: そのまま抽出。
* **データ型**: `VARCHAR(255)` などサイズ込み。
* **制約**: `PRIMARY KEY`->**PK**, `FOREIGN KEY`->**FK**, `UNIQUE`->**UQ**, `AUTO_INCREMENT`->**AI**, `NOT NULL`->`✓`

### 論理名推測 (Fallback)
コメントがない場合のみ適用：
* `id`, `*_id` -> "ID", "～ID"
* `title`, `name` -> "タイトル", "名称"
* `created_at`, `updated_at` -> "作成日時", "更新日時"
* `status`, `flg` -> "ステータス", "フラグ"

## 2. 出力テンプレート (Strict Format)

以下の構造に従い、**DDL内の全テーブル**を出力すること。

```markdown
# テーブル定義書
**Source**: `{InputFilePath}`
**Date**: {YYYY-MM-DD}

## 目次
1. [TASKS](#tasks)
2. [USERS](#users)
...

---

## 詳細

### {No}. {TABLE_NAME} ({LogicalName})
**物理名**: `{table_name}`
**説明**: {TableComment}

#### カラム一覧
| No | 物理名 | 論理名 | 型 | NULL不可 | Default | 制約 | 説明 |
|---|---|---|---|:---:|---|:---:|---|
| 1 | id | ID | BIGINT | ✓ | AI | PK | 自動採番 |
| 2 | user_id | ユーザーID | BIGINT | ✓ | - | FK | |

#### 関連性
* **Primary Key**: `id`
* **Foreign Keys (Outgoing)**: このテーブル **が** 参照している
    * `user_id` -> `users.id`
* **Foreign Keys (Incoming)**: このテーブル **を** 参照している
    * なし

#### インデックス
| Index Name | Type | Columns |
|---|---|---|
| idx_tasks_status | Normal | status |

---

## 検証項目 (Self-Correction)

* [ ] **網羅性**: DDL内の全てのテーブルが含まれているか。
* [ ] **FK整合性**: `Outgoing` (参照) と `Incoming` (被参照) が矛盾なく記載されているか。
    * ※解析時、全テーブルのFK情報を一度メモリに展開してから各テーブルの記述を生成すること。
* [ ] **フォーマット**: Markdownの表崩れがないか。
* [ ] **制約記号**: PK, FK, UQ, AI が正しくマッピングされているか。