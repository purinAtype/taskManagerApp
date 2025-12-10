# API設計書

> **出力先**: `docs/api/API設計書/`
> **ファイル名**: `API-TASK-006_タスク更新.md`

## 1. 文書情報

| 項目 | 内容 |
| :--- | :--- |
| **API ID** | API-TASK-006 |
| **API名** | タスク更新 |
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
| **エンドポイント** | `/tasks/{id}` |
| **HTTPメソッド** | `POST` |
| **機能概要** | 既存タスクの情報を更新する。バリデーションを実施し、成功時は詳細画面へリダイレクトする。 |
| **処理種別** | `データ更新` |
| **トランザクション** | `あり` |
| **認証** | `不要` |
| **必要権限** | `なし` |

### 使用場面

- タスク編集画面（SCR-TASK-004）での更新ボタン押下時

---

## 3. リクエスト仕様

### 3.1 パスパラメータ

| 名前 | 型 | 必須 | 説明 | 例 |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `Long` | ✔ | 更新対象のタスクID | `1` |

### 3.2 クエリパラメータ

なし

### 3.3 リクエストボディ

**Content-Type**: `application/x-www-form-urlencoded` (フォーム送信)

**フィールド定義**:

| フィールド名 | 型 | 必須 | 説明 | 制約 |
| :--- | :--- | :--- | :--- | :--- |
| `title` | `String` | ✔ | タスクタイトル | 最大100文字、空白のみ不可 |
| `description` | `String` | - | タスク説明 | 最大1000文字 |
| `status` | `String` | ✔ | ステータス | TODO/IN_PROGRESS/DONE |
| `priority` | `String` | ✔ | 優先度 | HIGH/MEDIUM/LOW |
| `categoryId` | `Long` | - | カテゴリーID | - |
| `dueDate` | `String` | - | 期限日 | yyyy-MM-dd形式 |

### 3.4 バリデーションルール

| 項目 | バリデーション内容 | エラーメッセージ |
| :--- | :--- | :--- |
| `title` | 必須、1～100文字、空白のみ不可 | "タイトルを入力してください" |
| `title` | 最大100文字 | "タイトルは100文字以内で入力してください" |
| `description` | 最大1000文字 | "説明は1000文字以内で入力してください" |
| `status` | 必須 | "ステータスを選択してください" |
| `priority` | 必須 | "優先度を選択してください" |

**バリデーションアノテーション**:
- `@NotBlank`: title
- `@Size(max=100)`: title
- `@Size(max=1000)`: description
- `@NotNull`: status, priority
- `@DateTimeFormat(pattern="yyyy-MM-dd")`: dueDate

---

## 4. レスポンス仕様

### 4.1 正常時レスポンス

* **HTTPステータスコード**: `302 Found` (リダイレクト)
* **リダイレクト先**: `/tasks/{id}` (更新されたタスクの詳細画面)
* **フラッシュ属性**: `successMessage = "タスクを更新しました"`

### 4.2 バリデーションエラー時レスポンス

* **HTTPステータスコード**: `200 OK`
* **Content-Type**: `text/html` (Thymeleafテンプレート)
* **レンダリングされるビュー**: `task/edit.html`

**モデル属性**:

| 属性名 | 型 | 必須 | 説明 |
| :--- | :--- | :--- | :--- |
| `taskForm` | `TaskForm` | ✔ | 入力済みデータを保持したフォーム |
| `taskId` | `Long` | ✔ | 更新対象のタスクID |
| `statuses` | `TaskStatus[]` | ✔ | ステータスenum全値 |
| `priorities` | `TaskPriority[]` | ✔ | 優先度enum全値 |
| `categories` | `List<TaskCategory>` | ✔ | カテゴリーマスタ全件 |
| `org.springframework.validation.BindingResult.taskForm` | `BindingResult` | ✔ | バリデーションエラー情報 |

---

## 5. 処理詳細

### 5.1 処理ステップ

| ステップ | 処理内容 | 備考 |
| :--- | :--- | :--- |
| 1 | パスパラメータから `id` を取得 | - |
| 2 | リクエストボディから TaskForm を取得 | Spring が自動的にバインド |
| 3 | @Valid によりバリデーション実施 | - |
| 4 | バリデーションエラーがある場合、ステップ11へ | - |
| 5 | TaskService.update(id, taskForm) を呼び出し | トランザクション開始 |
| 6 | TaskService.findById(id) で更新対象の存在確認（SQL1） | - |
| 7 | タスクが存在しない場合、TaskNotFoundExceptionをスロー | ステップ13へ |
| 8 | TaskConverter でTaskFormをTaskエンティティに変換 | - |
| 9 | Taskエンティティに id を設定 | - |
| 10 | TaskMapper.update(task) でデータベースを更新（SQL2） | トランザクションコミット |
| 11 | フラッシュ属性に成功メッセージを設定し、詳細画面へリダイレクト | 正常終了 |
| 12 | taskId とマスタデータ（statuses, priorities, categories）を再設定 | - |
| 13 | ビュー `task/edit.html` を再表示 | エラー終了 |
| 14 | GlobalExceptionHandler が例外を捕捉し、404エラー画面へ遷移 | エラー終了 |

### 5.2 使用サービス

| サービスクラス | メソッド | 役割 |
| :--- | :--- | :--- |
| `TaskService` | `update(id, taskForm)` | タスクを更新 |
| `TaskService` | `findById(id)` | 更新対象タスクの存在確認 |
| `TaskService` | `findAllCategories()` | 全カテゴリーを取得（エラー時の再表示用） |

### 5.3 使用テーブル

| テーブル名 | 物理名 | 操作 | 用途 |
| :--- | :--- | :--- | :--- |
| タスク | `tasks` | `SELECT` | 更新対象タスクの存在確認 |
| タスク | `tasks` | `UPDATE` | タスク情報の更新 |
| カテゴリー | `task_categories` | `SELECT` | カテゴリーマスタの取得（エラー時） |

### 5.4 SQL詳細

#### SQL1: ID検索（存在確認）

**目的**: 更新対象のタスクが存在するか確認する

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

#### SQL2: タスク更新

**目的**: タスク情報を更新する

**Mapper**: `TaskMapper.update`

```sql
UPDATE tasks
SET title = #{title},
    description = #{description},
    status = #{status},
    priority = #{priority},
    category_id = #{categoryId},
    due_date = #{dueDate},
    updated_at = CURRENT_TIMESTAMP
WHERE id = #{id}
```

**パラメータ**:

| パラメータ名 | 型 | 説明 | 例 |
| :--- | :--- | :--- | :--- |
| `#{id}` | `Long` | タスクID | `1` |
| `#{title}` | `String` | タスクタイトル | `"レポート作成（更新）"` |
| `#{description}` | `String` | タスク説明 | `"月次レポートを作成する（更新）"` |
| `#{status}` | `String` | ステータス | `"IN_PROGRESS"` |
| `#{priority}` | `String` | 優先度 | `"HIGH"` |
| `#{categoryId}` | `Long` | カテゴリーID | `2` |
| `#{dueDate}` | `LocalDate` | 期限日 | `2025-12-31` |

**自動更新**:
- `updated_at`: CURRENT_TIMESTAMP

---

## 6. ビジネスロジック

### 6.1 データ変換ロジック

**TaskForm → Task エンティティ変換**:
- TaskConverter (MapStruct) を使用
- enum型（TaskStatus, TaskPriority）は自動的に文字列に変換される
- categoryIdがnullの場合、そのままnullとして保存される（カテゴリー未設定）

### 6.2 更新時の注意事項

- `created_at` は変更しない（既存の作成日時を保持）
- `updated_at` のみ CURRENT_TIMESTAMP で更新される

---

## 7. トランザクション制御

| 項目 | 内容 |
| :--- | :--- |
| **トランザクション境界** | TaskService.update() メソッド全体 |
| **コミット条件** | タスク更新SQL正常実行時 |
| **ロールバック条件** | SQL実行エラー、データベース制約違反時、タスク未存在時 |
| **トランザクション管理** | Spring の @Transactional アノテーション |

---

## 8. エラーハンドリング

| エラー条件 | HTTPステータス | エラーコード | 処理内容 |
| :--- | :--- | :--- | :--- |
| バリデーションエラー | 200 | - | フォーム再表示、エラーメッセージ表示 |
| タスク未存在 | 404 | NOT_FOUND | 404エラー画面（SCR-CMN-001）を表示 |
| DB制約違反 | 500 | INTERNAL_SERVER_ERROR | 500エラー画面（SCR-CMN-002）を表示、ログ出力 |
| DB接続エラー | 500 | INTERNAL_SERVER_ERROR | 500エラー画面（SCR-CMN-002）を表示、ログ出力 |
| 外部キー制約違反（不正なcategoryId） | 500 | INTERNAL_SERVER_ERROR | 500エラー画面（SCR-CMN-002）を表示、ログ出力 |

---

## 9. 外部連携

該当なし

---

## 10. 関連API

| API名 | エンドポイント | 関係性 |
| :--- | :--- | :--- |
| タスク編集フォーム表示 | `GET /tasks/{id}/edit` | フォーム表示元 |
| タスク詳細取得 | `GET /tasks/{id}` | 更新成功時のリダイレクト先 |

---

## 11. テストケース

### 11.1 正常系

| No. | テストケース | 入力値 | 期待結果 |
| :--- | :--- | :--- | :--- |
| 1 | 全項目更新 | id=1, title="更新", description="更新詳細", status=IN_PROGRESS, priority=LOW, categoryId=2, dueDate="2025-12-31" | 302リダイレクト、成功メッセージ表示 |
| 2 | 必須項目のみ更新 | id=1, title="更新", status=DONE, priority=HIGH | 302リダイレクト、descriptionとcategoryIdとdueDateはnull |
| 3 | カテゴリー削除（null設定） | id=1, title="タスク", status=TODO, priority=MEDIUM, categoryId=null | 302リダイレクト、正常更新 |
| 4 | ステータスをDONEに変更 | id=1, status=DONE | 302リダイレクト、ステータス更新 |

### 11.2 異常系

| No. | テストケース | 入力値 | 期待結果 |
| :--- | :--- | :--- | :--- |
| 1 | タイトル未入力 | title=null | 200、"タイトルを入力してください"エラー表示 |
| 2 | タイトル空白のみ | title="   " | 200、"タイトルを入力してください"エラー表示 |
| 3 | ステータス未選択 | status=null | 200、"ステータスを選択してください"エラー表示 |
| 4 | 優先度未選択 | priority=null | 200、"優先度を選択してください"エラー表示 |
| 5 | 存在しないタスクIDで更新 | id=99999 | 404 NOT_FOUND、エラー画面表示 |
| 6 | 存在しないcategoryId | categoryId=99999 | 500 INTERNAL_SERVER_ERROR（外部キー制約違反） |

### 11.3 境界値

| No. | テストケース | 入力値 | 期待結果 |
| :--- | :--- | :--- | :--- |
| 1 | タイトル最大文字数（100文字） | title="a" × 100 | 302リダイレクト、正常更新 |
| 2 | タイトル文字数超過（101文字） | title="a" × 101 | 200、"タイトルは100文字以内で入力してください"エラー表示 |
| 3 | 説明最大文字数（1000文字） | description="a" × 1000 | 302リダイレクト、正常更新 |
| 4 | 説明文字数超過（1001文字） | description="a" × 1001 | 200、"説明は1000文字以内で入力してください"エラー表示 |

---

## 12. 備考

- Spring の @Valid アノテーションによりサーバーサイドバリデーションを実施
- バリデーションエラー時は入力済みデータを保持してフォームを再表示
- updated_at は自動更新（CURRENT_TIMESTAMP）、created_at は保持
- 更新前に必ずタスクの存在確認を実施
- MyBatisマッパーは `TaskMapper` (カスタムマッパー) を使用

---

## 13. 変更履歴

| バージョン | 変更日 | 変更者 | 変更内容 |
| :--- | :--- | :--- | :--- |
| 1.0.0 | 2025-12-09 | API設計エージェント | 新規作成 |
