# API設計書

> **出力先**: `docs/api/API設計書/`
> **ファイル名**: `API-TASK-005_タスク編集フォーム表示.md`

## 1. 文書情報

| 項目 | 内容 |
| :--- | :--- |
| **API ID** | API-TASK-005 |
| **API名** | タスク編集フォーム表示 |
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
| **エンドポイント** | `/tasks/{id}/edit` |
| **HTTPメソッド** | `GET` |
| **機能概要** | 指定されたIDのタスクの編集フォームを表示する。既存データをTaskFormに変換し、マスタデータと共に提供する。 |
| **処理種別** | `データ取得` |
| **トランザクション** | `なし` |
| **認証** | `不要` |
| **必要権限** | `なし` |

### 使用場面

- タスク編集画面（SCR-TASK-004）の初期表示
- タスク一覧画面からの「編集」ボタン押下時
- タスク詳細画面からの「編集」ボタン押下時

---

## 3. リクエスト仕様

### 3.1 パスパラメータ

| 名前 | 型 | 必須 | 説明 | 例 |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `Long` | ✔ | 編集対象のタスクID | `1` |

### 3.2 クエリパラメータ

なし

### 3.3 リクエストボディ

なし

### 3.4 バリデーションルール

| 項目 | バリデーション内容 | エラーメッセージ |
| :--- | :--- | :--- |
| `id` | 数値型であること | Spring が自動的にバインドエラーとして処理 |

---

## 4. レスポンス仕様

### 4.1 正常時レスポンス

* **HTTPステータスコード**: `200 OK`
* **Content-Type**: `text/html` (Thymeleafテンプレート)

**レンダリングされるビュー**: `task/edit.html`

**モデル属性**:

| 属性名 | 型 | 必須 | 説明 | 例 |
| :--- | :--- | :--- | :--- | :--- |
| `taskForm` | `TaskForm` | ✔ | 既存タスク情報を設定したフォーム | - |
| `taskId` | `Long` | ✔ | 編集対象のタスクID | `1` |
| `statuses` | `TaskStatus[]` | ✔ | ステータスenum全値 | `[TODO, IN_PROGRESS, DONE]` |
| `priorities` | `TaskPriority[]` | ✔ | 優先度enum全値 | `[HIGH, MEDIUM, LOW]` |
| `categories` | `List<TaskCategory>` | ✔ | カテゴリーマスタ全件 | - |

**TaskForm のフィールド（既存データ設定済み）**:

| フィールド名 | 型 | 説明 | 例 |
| :--- | :--- | :--- | :--- |
| `title` | `String` | タスクタイトル | `"レポート作成"` |
| `description` | `String` | タスク説明 | `"月次レポートを作成する"` |
| `status` | `TaskStatus` | ステータス | `TODO` |
| `priority` | `TaskPriority` | 優先度 | `HIGH` |
| `categoryId` | `Long` | カテゴリーID | `1` |
| `dueDate` | `LocalDate` | 期限日 | `2025-12-31` |

---

## 5. 処理詳細

### 5.1 処理ステップ

| ステップ | 処理内容 | 備考 |
| :--- | :--- | :--- |
| 1 | パスパラメータから `id` を取得 | - |
| 2 | TaskService.findById(id) を呼び出し、タスク情報を取得（SQL1） | - |
| 3 | タスクが存在しない場合、TaskNotFoundExceptionをスロー | ステップ8へ |
| 4 | TaskConverter.toForm(taskDto) でTaskDtoをTaskFormに変換 | - |
| 5 | TaskService.findAllCategories() を呼び出しカテゴリーマスタ取得（SQL2） | - |
| 6 | モデルに taskForm, taskId, statuses, priorities, categories を設定 | - |
| 7 | ビュー `task/edit.html` をレンダリング | 正常終了 |
| 8 | GlobalExceptionHandler が例外を捕捉し、404エラー画面へ遷移 | エラー終了 |

### 5.2 使用サービス

| サービスクラス | メソッド | 役割 |
| :--- | :--- | :--- |
| `TaskService` | `findById(id)` | 指定IDのタスクを取得 |
| `TaskService` | `findAllCategories()` | 全カテゴリーを取得 |

### 5.3 使用テーブル

| テーブル名 | 物理名 | 操作 | 用途 |
| :--- | :--- | :--- | :--- |
| タスク | `tasks` | `SELECT` | タスク情報の取得 |
| カテゴリー | `task_categories` | `SELECT` | カテゴリー情報の取得（JOIN、マスタ取得） |

### 5.4 SQL詳細

#### SQL1: ID検索（カテゴリー情報含む）

**目的**: 指定されたIDのタスクをカテゴリー情報付きで取得する

**Mapper**: `TaskMapper.selectByIdWithCategory`

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
WHERE t.id = #{id}
```

**パラメータ**:

| パラメータ名 | 型 | 説明 | 例 |
| :--- | :--- | :--- | :--- |
| `#{id}` | `Long` | タスクID | `1` |

#### SQL2: カテゴリーマスタ取得

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

### 6.1 データ変換ロジック

**TaskDto → TaskForm 変換**:
- TaskConverter (MapStruct) を使用
- カテゴリー関連フィールド（categoryName, categoryColor）は変換対象外
- enum型（TaskStatus, TaskPriority）は自動的に適切な型に変換される

---

## 7. トランザクション制御

該当なし（参照のみ）

---

## 8. エラーハンドリング

| エラー条件 | HTTPステータス | エラーコード | 処理内容 |
| :--- | :--- | :--- | :--- |
| タスク未存在 | 404 | NOT_FOUND | 404エラー画面（SCR-CMN-001）を表示 |
| 不正なID形式 | 400 | BAD_REQUEST | Spring が自動的にエラーレスポンスを返却 |
| DB接続エラー | 500 | INTERNAL_SERVER_ERROR | 500エラー画面（SCR-CMN-002）を表示、ログ出力 |

---

## 9. 外部連携

該当なし

---

## 10. 関連API

| API名 | エンドポイント | 関係性 |
| :--- | :--- | :--- |
| タスク更新 | `POST /tasks/{id}` | フォーム送信先 |
| タスク詳細取得 | `GET /tasks/{id}` | キャンセル時の遷移先 |

---

## 11. テストケース

### 11.1 正常系

| No. | テストケース | 入力値 | 期待結果 |
| :--- | :--- | :--- | :--- |
| 1 | 存在するタスクの編集フォーム表示 | `id=1` | 200 OK、既存データが設定されたフォームを表示 |
| 2 | カテゴリー未設定タスクの編集 | `id=2` (category_id=null) | 200 OK、categoryIdがnullのフォームを表示 |
| 3 | 説明未設定タスクの編集 | `id=3` (description=null) | 200 OK、descriptionがnullのフォームを表示 |

### 11.2 異常系

| No. | テストケース | 入力値 | 期待結果 |
| :--- | :--- | :--- | :--- |
| 1 | 存在しないタスクID | `id=99999` | 404 NOT_FOUND、エラー画面表示 |
| 2 | 不正なID形式 | `id=abc` | 400 BAD_REQUEST |
| 3 | 負の数のID | `id=-1` | 404 NOT_FOUND（該当データなし） |

### 11.3 境界値

| No. | テストケース | 入力値 | 期待結果 |
| :--- | :--- | :--- | :--- |
| 1 | ID=0 | `id=0` | 404 NOT_FOUND |
| 2 | ID最大値 | `id=9223372036854775807` | 404 NOT_FOUND（存在しない場合） |

---

## 12. 備考

- TaskConverterはMapStructにより自動生成される変換ロジック
- TaskNotFoundExceptionはGlobalExceptionHandlerで捕捉され、404エラー画面へ遷移する
- taskIdはフォーム送信時の更新先IDとして使用される
- MyBatisマッパーは `TaskMapper` (カスタムマッパー) を使用

---

## 13. 変更履歴

| バージョン | 変更日 | 変更者 | 変更内容 |
| :--- | :--- | :--- | :--- |
| 1.0.0 | 2025-12-09 | API設計エージェント | 新規作成 |
