# API設計書

> **出力先**: `docs/api/API設計書/`
> **ファイル名**: `API-TASK-001_タスク一覧取得.md`

## 1. 文書情報

| 項目 | 内容 |
| :--- | :--- |
| **API ID** | API-TASK-001 |
| **API名** | タスク一覧取得 |
| **カテゴリ** | タスク管理 |
| **バージョン** | 1.0.0 |
| **作成日** | 2025-12-09 |
| **作成者** | API設計エージェント |
| **最終更新日** | 2025-12-09 |
| **最終更新者** | API設計エージェント |

---

## 2. API概要

| 項目 | 内容 |
| :--- | :--- |
| **エンドポイント** | `/tasks` |
| **HTTPメソッド** | `GET` |
| **機能概要** | タスク一覧を取得する。ステータス、優先度、カテゴリーIDによる絞り込み検索が可能。 |
| **処理種別** | `データ取得` |
| **トランザクション** | `なし` |
| **認証** | `不要` |
| **必要権限** | `なし` |

### 使用場面

- タスク一覧画面（SCR-TASK-001）の初期表示
- タスク一覧画面での検索条件による絞り込み
- タスク登録・編集・削除後の一覧画面への遷移

---

## 3. リクエスト仕様

### 3.1 パスパラメータ

なし

### 3.2 クエリパラメータ

| 名前 | 型 | 必須 | 説明 | デフォルト | 例 |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `status` | `String` | - | ステータスで絞り込み | - | `TODO` |
| `priority` | `String` | - | 優先度で絞り込み | - | `HIGH` |
| `categoryId` | `Long` | - | カテゴリーIDで絞り込み | - | `1` |

### 3.3 リクエストボディ

なし

### 3.4 バリデーションルール

| 項目 | バリデーション内容 | エラーメッセージ |
| :--- | :--- | :--- |
| `status` | TaskStatus enum に含まれる値であること（TODO/IN_PROGRESS/DONE） | Spring が自動的にバインドエラーとして処理 |
| `priority` | TaskPriority enum に含まれる値であること（HIGH/MEDIUM/LOW） | Spring が自動的にバインドエラーとして処理 |
| `categoryId` | 数値型であること | Spring が自動的にバインドエラーとして処理 |

---

## 4. レスポンス仕様

### 4.1 正常時レスポンス

* **HTTPステータスコード**: `200 OK`
* **Content-Type**: `text/html` (Thymeleafテンプレート)

**レンダリングされるビュー**: `task/list.html`

**モデル属性**:

| 属性名 | 型 | 必須 | 説明 | 例 |
| :--- | :--- | :--- | :--- | :--- |
| `tasks` | `List<TaskDto>` | ✔ | タスクDTOのリスト | - |
| `statuses` | `TaskStatus[]` | ✔ | ステータスenum全値 | `[TODO, IN_PROGRESS, DONE]` |
| `priorities` | `TaskPriority[]` | ✔ | 優先度enum全値 | `[HIGH, MEDIUM, LOW]` |
| `categories` | `List<TaskCategory>` | ✔ | カテゴリーマスタ全件 | - |
| `selectedStatus` | `TaskStatus` | - | 選択中のステータス | `TODO` |
| `selectedPriority` | `TaskPriority` | - | 選択中の優先度 | `HIGH` |
| `selectedCategoryId` | `Long` | - | 選択中のカテゴリーID | `1` |
| `successMessage` | `String` | - | 成功メッセージ（フラッシュ属性） | `"タスクを削除しました"` |

### 4.2 一覧取得時のレスポンス

**TaskDto のフィールド定義**:

| フィールド名 | 型 | 必須 | 説明 | 例 |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `Long` | ✔ | タスクID | `1` |
| `title` | `String` | ✔ | タスクタイトル | `"レポート作成"` |
| `description` | `String` | - | タスク説明 | `"月次レポートを作成する"` |
| `status` | `TaskStatus` | ✔ | ステータス | `TODO` |
| `priority` | `TaskPriority` | ✔ | 優先度 | `HIGH` |
| `categoryId` | `Long` | - | カテゴリーID | `1` |
| `categoryName` | `String` | - | カテゴリー名 | `"仕事"` |
| `categoryColor` | `String` | - | カテゴリー色 | `"#007bff"` |
| `dueDate` | `LocalDate` | - | 期限日 | `2025-12-31` |
| `createdAt` | `LocalDateTime` | ✔ | 作成日時 | `2025-01-15T10:30:00` |
| `updatedAt` | `LocalDateTime` | ✔ | 更新日時 | `2025-01-15T10:30:00` |

---

## 5. 処理詳細

### 5.1 処理ステップ

| ステップ | 処理内容 | 備考 |
| :--- | :--- | :--- |
| 1 | クエリパラメータ（status, priority, categoryId）の存在確認 | - |
| 2 | パラメータあり: TaskService.findByCondition() を呼び出し条件検索 | SQL1 |
| 3 | パラメータなし: TaskService.findAll() を呼び出し全件取得 | SQL2 |
| 4 | TaskService.findAllCategories() を呼び出しカテゴリーマスタ取得 | SQL3 |
| 5 | モデルに取得データと検索条件を設定 | - |
| 6 | ビュー `task/list.html` をレンダリング | - |

### 5.2 使用サービス

| サービスクラス | メソッド | 役割 |
| :--- | :--- | :--- |
| `TaskService` | `findByCondition(status, priority, categoryId)` | 条件に一致するタスクを検索 |
| `TaskService` | `findAll()` | 全タスクを取得 |
| `TaskService` | `findAllCategories()` | 全カテゴリーを取得 |

### 5.3 使用テーブル

| テーブル名 | 物理名 | 操作 | 用途 |
| :--- | :--- | :--- | :--- |
| タスク | `tasks` | `SELECT` | タスク情報の取得 |
| カテゴリー | `task_categories` | `SELECT` | カテゴリー情報の取得 |

### 5.4 SQL詳細

#### SQL1: 条件検索（カテゴリー情報含む）

**目的**: ステータス、優先度、カテゴリーIDのいずれか、または複数の条件でタスクを絞り込み検索する

**Mapper**: `TaskMapper.selectByConditionWithCategory`

```sql
SELECT
    t.id,
    t.title,
    t.description,
    t.status,
    t.priority,
    t.category_id,
    c.name AS category_name,
    c.color AS category_color,
    t.due_date,
    t.created_at,
    t.updated_at
FROM tasks t
LEFT JOIN task_categories c ON t.category_id = c.id
WHERE
    <if test="status != null and status != ''">
        AND t.status = #{status}
    </if>
    <if test="priority != null and priority != ''">
        AND t.priority = #{priority}
    </if>
    <if test="categoryId != null">
        AND t.category_id = #{categoryId}
    </if>
ORDER BY t.created_at DESC
```

**パラメータ**:

| パラメータ名 | 型 | 説明 | 例 |
| :--- | :--- | :--- | :--- |
| `#{status}` | `String` | ステータス（任意） | `"TODO"` |
| `#{priority}` | `String` | 優先度（任意） | `"HIGH"` |
| `#{categoryId}` | `Long` | カテゴリーID（任意） | `1` |

#### SQL2: 全件取得（カテゴリー情報含む）

**目的**: 全タスクをカテゴリー情報付きで取得する

**Mapper**: `TaskMapper.selectAllWithCategory`

```sql
SELECT
    t.id,
    t.title,
    t.description,
    t.status,
    t.priority,
    t.category_id,
    c.name AS category_name,
    c.color AS category_color,
    t.due_date,
    t.created_at,
    t.updated_at
FROM tasks t
LEFT JOIN task_categories c ON t.category_id = c.id
ORDER BY t.created_at DESC
```

**パラメータ**: なし

#### SQL3: カテゴリーマスタ取得

**目的**: 全カテゴリーを表示順で取得する

**Mapper**: `TaskCategoryMapper.selectAll`

```sql
SELECT id, name, description, color, display_order, created_at, updated_at
FROM task_categories
ORDER BY display_order ASC
```

**パラメータ**: なし

---

## 6. ビジネスロジック

該当なし

---

## 7. トランザクション制御

該当なし（参照のみ）

---

## 8. エラーハンドリング

| エラー条件 | HTTPステータス | エラーコード | 処理内容 |
| :--- | :--- | :--- | :--- |
| DB接続エラー | 500 | INTERNAL_SERVER_ERROR | エラー画面表示、ログ出力 |
| 不正なパラメータ型 | 400 | BAD_REQUEST | Spring が自動的にエラーレスポンスを返却 |

---

## 9. 外部連携

該当なし

---

## 10. 関連API

| API名 | エンドポイント | 関係性 |
| :--- | :--- | :--- |
| タスク詳細取得 | `/tasks/{id}` | 一覧からの詳細遷移 |
| タスク登録フォーム表示 | `/tasks/new` | 一覧からの新規登録遷移 |
| タスク編集フォーム表示 | `/tasks/{id}/edit` | 一覧からの編集遷移 |

---

## 11. テストケース

### 11.1 正常系

| No. | テストケース | 入力値 | 期待結果 |
| :--- | :--- | :--- | :--- |
| 1 | 全件取得（検索条件なし） | クエリパラメータなし | 200 OK、全タスクを取得 |
| 2 | ステータスで絞り込み | `status=TODO` | 200 OK、ステータスがTODOのタスクのみ取得 |
| 3 | 優先度で絞り込み | `priority=HIGH` | 200 OK、優先度がHIGHのタスクのみ取得 |
| 4 | カテゴリーで絞り込み | `categoryId=1` | 200 OK、カテゴリーID=1のタスクのみ取得 |
| 5 | 複数条件で絞り込み | `status=TODO&priority=HIGH&categoryId=1` | 200 OK、全条件に一致するタスクのみ取得 |
| 6 | タスク0件の場合 | 該当データなし | 200 OK、空リストを返却 |

### 11.2 異常系

| No. | テストケース | 入力値 | 期待結果 |
| :--- | :--- | :--- | :--- |
| 1 | 不正なステータス値 | `status=INVALID` | 400 BAD_REQUEST |
| 2 | 不正な優先度値 | `priority=INVALID` | 400 BAD_REQUEST |
| 3 | 不正なカテゴリーID | `categoryId=abc` | 400 BAD_REQUEST |

### 11.3 境界値

| No. | テストケース | 入力値 | 期待結果 |
| :--- | :--- | :--- | :--- |
| 1 | カテゴリーID=0 | `categoryId=0` | 200 OK、該当データなし（空リスト） |
| 2 | カテゴリーID最大値 | `categoryId=9223372036854775807` | 200 OK、該当データなし（存在しない場合） |

---

## 12. 備考

- カテゴリー情報はLEFT JOINで取得するため、カテゴリー未設定のタスクも表示される
- 検索結果は作成日時の降順（新しい順）でソートされる
- 画面表示用にTaskStatusとTaskPriorityはenumの表示名メソッド（getDisplayName）を使用
- MyBatisマッパーは `TaskMapper` (カスタムマッパー) を使用

---

## 13. 変更履歴

| バージョン | 変更日 | 変更者 | 変更内容 |
| :--- | :--- | :--- | :--- |
| 1.0.0 | 2025-12-09 | API設計エージェント | 新規作成 |
