---
name: er-diagram-generator
description: |
  H2/MySQL DDL (CREATE TABLE) analyze to generate Mermaid erDiagram.
  Visualizes tables, columns, types, PK/FK/UK, and relationships (1:1, 1:N, N:M).
  Output: Markdown file for documentation/review.
allowed-tools: Read, Glob, Write
---

# DDL to ER Diagram Generator

H2/MySQL DDLを解析し、Mermaid形式のER図をMarkdownで出力する。

## 処理フロー

1. **Read**: `src/main/resources/schema.sql` (または指定ファイル) を読み込む。
2. **Analyze**: DDLからテーブル定義、制約、リレーションシップを抽出。
3. **Generate**: 定義に基づきMermaid構文を構築。
4. **Write**: `docs/db/ER図.md` に出力。

## 1. 解析・変換ルール

### テーブル・カラム抽出

- **物理名**: 大文字に変換 (例: `users` -> `USERS`)
- **論理名**: カラム名やコメントから推測し、ダブルクォートで囲む。
- **制約**:
  - `PRIMARY KEY` -> **PK**
  - `FOREIGN KEY` -> **FK**
  - `UNIQUE` -> **UK**

### データ型マッピング

| SQL Type                   | Mermaid Type |
| -------------------------- | ------------ |
| BIGINT, INT, SMALLINT      | int          |
| VARCHAR, TEXT, CHAR        | string       |
| DATETIME, DATE, TIMESTAMP  | datetime     |
| BOOLEAN                    | boolean      |
| NUMERIC, DECIMAL           | decimal      |
| Others                     | text         |

### リレーションシップ判定ロジック

外部キー(FK)定義に基づき、以下のように判定・描画する。

| 条件                              | 記号         | 意味   | 備考                      |
| --------------------------------- | ------------ | ------ | ------------------------- |
| FKカラムに UNIQUE 制約あり        | &#124;&#124;--&#124;&#124; | 1対1   | 例: UserProfile           |
| FKカラムに UNIQUE 制約なし        | &#124;&#124;--o&#123;      | 1対多  | 通常の参照                |
| 中間テーブル (複合PKがFKのみ)     | &#125;o--o&#123;           | 多対多 | 2つの 1対多 に分解可      |

## 2. 論理名推測パターン (Column -> Logical)

- `id`, `*_id` -> "ID" / "～ID"
- `name`, `title` -> "名称" / "タイトル"
- `description`, `note` -> "説明" / "備考"
- `email`, `password` -> "メール" / "パスワード"
- `created_at`, `updated_at` -> "作成日時" / "更新日時"
- `status`, `type` -> "状態" / "種別"
- `is_*`, `has_*` -> "フラグ"

## 3. 出力テンプレート

出力ファイルは以下の構成を厳守すること。

**ファイル構成:**

```text
# ER図
**Source**: `(入力ファイルパス)`
**Date**: (YYYY-MM-DD)

## Schema

(Mermaid erDiagram ブロック)

## Tables

(テーブル一覧)
```

**Mermaid erDiagram 記述例:**

```mermaid
erDiagram
    USERS {
        bigint id PK "ID"
        varchar name "氏名"
        varchar email UK "メールアドレス"
    }
    TASKS {
        bigint id PK "ID"
        varchar title "件名"
        bigint user_id FK "ユーザーID"
    }

    USERS ||--o{ TASKS : has }
```

**テーブル一覧の記述例:**

| No | Physical Name | Logical Name | Comment              |
| -- | ------------- | ------------ | -------------------- |
| 1  | users         | ユーザー     | ユーザー管理テーブル |
| 2  | tasks         | タスク       | タスク管理テーブル   |

## 検証項目 (Self-Correction)

生成完了前に以下を自己チェックし、不備があれば修正すること。

- [ ] **網羅性**: DDLに含まれる全てのテーブルが出力されているか。
- [ ] **関係性**: 外部キー(FK)が存在する箇所には、必ずリレーション線が引かれているか。
- [ ] **構文**: Mermaid構文エラー（全角スペース、不正な記号、閉じていない括弧）がないか。
- [ ] **視認性**: 論理名（ダブルクォーテーション内）に日本語が正しく適用されているか。
