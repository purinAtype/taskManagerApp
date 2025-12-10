# API設計書

> **出力先**: `docs/api/API設計書/`
> **ファイル名**: `API-TASK-004_タスク登録.md`

## 1. 文書情報

| 項目 | 内容 |
| :--- | :--- |
| **API ID** | API-TASK-004 |
| **API名** | タスク登録 |
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
| **HTTPメソッド** | `POST` |
| **機能概要** | 新規タスクをデータベースに登録する。バリデーションを実施し、成功時は詳細画面へリダイレクトする。 |
| **処理種別** | `データ登録` |
| **トランザクション** | `あり` |
| **認証** | `不要` |
| **必要権限** | `なし` |

### 使用場面

- タスク登録画面（SCR-TASK-002）での登録ボタン押下時

---

## 3. リクエスト仕様

### 3.1 パスパラメータ

なし

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
* **リダイレクト先**: `/tasks/{id}` (登録されたタスクの詳細画面)
* **フラッシュ属性**: `successMessage = "タスクを登録しました"`

### 4.2 バリデーションエラー時レスポンス

* **HTTPステータスコード**: `200 OK`
* **Content-Type**: `text/html` (Thymeleafテンプレート)
* **レンダリングされるビュー**: `task/form.html`

**モデル属性**:

| 属性名 | 型 | 必須 | 説明 |
| :--- | :--- | :--- | :--- |
| `taskForm` | `TaskForm` | ✔ | 入力済みデータを保持したフォーム |
| `statuses` | `TaskStatus[]` | ✔ | ステータスenum全値 |
| `priorities` | `TaskPriority[]` | ✔ | 優先度enum全値 |
| `categories` | `List<TaskCategory>` | ✔ | カテゴリーマスタ全件 |
| `org.springframework.validation.BindingResult.taskForm` | `BindingResult` | ✔ | バリデーションエラー情報 |

---

## 5. 処理詳細

### 5.1 処理ステップ

| ステップ | 処理内容 | 備考 |
| :--- | :--- | :--- |
| 1 | リクエストボディから TaskForm を取得 | Spring が自動的にバインド |
| 2 | @Valid によりバリデーション実施 | - |
| 3 | バリデーションエラーがある場合、ステップ9へ | - |
| 4 | TaskService.create(taskForm) を呼び出し | トランザクション開始 |
| 5 | TaskConverter でTaskFormをTaskエンティティに変換 | - |
| 6 | TaskMapper.insert(task) でデータベースに登録（SQL1） | - |
| 7 | 登録されたタスクをTaskDtoに変換して返却 | トランザクションコミット |
| 8 | フラッシュ属性に成功メッセージを設定し、詳細画面へリダイレクト | 正常終了 |
| 9 | マスタデータ（statuses, priorities, categories）を再設定 | - |
| 10 | ビュー `task/form.html` を再表示 | エラー終了 |

### 5.2 使用サービス

| サービスクラス | メソッド | 役割 |
| :--- | :--- | :--- |
| `TaskService` | `create(taskForm)` | タスクを登録 |
| `TaskService` | `findAllCategories()` | 全カテゴリーを取得（エラー時の再表示用） |

### 5.3 使用テーブル

| テーブル名 | 物理名 | 操作 | 用途 |
| :--- | :--- | :--- | :--- |
| タスク | `tasks` | `INSERT` | タスク情報の登録 |
| カテゴリー | `task_categories` | `SELECT` | カテゴリーマスタの取得（エラー時） |

### 5.4 SQL詳細

#### SQL1: タスク登録

**目的**: 新規タスクをデータベースに登録する

**Mapper**: `TaskMapper.insert`

```sql
INSERT INTO tasks (title, description, status, priority, category_id, due_date, created_at, updated_at)
VALUES (#{title}, #{description}, #{status}, #{priority}, #{categoryId}, #{dueDate}, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
```

**パラメータ**:

| パラメータ名 | 型 | 説明 | 例 |
| :--- | :--- | :--- | :--- |
| `#{title}` | `String` | タスクタイトル | `"レポート作成"` |
| `#{description}` | `String` | タスク説明 | `"月次レポートを作成する"` |
| `#{status}` | `String` | ステータス | `"TODO"` |
| `#{priority}` | `String` | 優先度 | `"HIGH"` |
| `#{categoryId}` | `Long` | カテゴリーID | `1` |
| `#{dueDate}` | `LocalDate` | 期限日 | `2025-12-31` |

**自動生成**:
- `id`: AUTO_INCREMENT
- `created_at`: CURRENT_TIMESTAMP
- `updated_at`: CURRENT_TIMESTAMP

---

## 6. ビジネスロジック

### 6.1 データ変換ロジック

**TaskForm → Task エンティティ変換**:
- TaskConverter (MapStruct) を使用
- enum型（TaskStatus, TaskPriority）は自動的に文字列に変換される
- categoryIdがnullの場合、そのままnullとして保存される

---

## 7. トランザクション制御

| 項目 | 内容 |
| :--- | :--- |
| **トランザクション境界** | TaskService.create() メソッド全体 |
| **コミット条件** | タスク登録SQL正常実行時 |
| **ロールバック条件** | SQL実行エラー、データベース制約違反時 |
| **トランザクション管理** | Spring の @Transactional アノテーション |

---

## 8. エラーハンドリング

| エラー条件 | HTTPステータス | エラーコード | 処理内容 |
| :--- | :--- | :--- | :--- |
| バリデーションエラー | 200 | - | フォーム再表示、エラーメッセージ表示 |
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
| タスク登録フォーム表示 | `GET /tasks/new` | フォーム表示元 |
| タスク詳細取得 | `GET /tasks/{id}` | 登録成功時のリダイレクト先 |

---

## 11. テストケース

### 11.1 正常系

| No. | テストケース | 入力値 | 期待結果 |
| :--- | :--- | :--- | :--- |
| 1 | 全項目指定で登録 | title="レポート作成", description="詳細", status=TODO, priority=HIGH, categoryId=1, dueDate="2025-12-31" | 302リダイレクト、成功メッセージ表示 |
| 2 | 必須項目のみで登録 | title="タスク", status=TODO, priority=MEDIUM | 302リダイレクト、descriptionとcategoryIdとdueDateはnull |
| 3 | カテゴリー未選択で登録 | title="タスク", status=TODO, priority=LOW, categoryId=null | 302リダイレクト、正常登録 |

### 11.2 異常系

| No. | テストケース | 入力値 | 期待結果 |
| :--- | :--- | :--- | :--- |
| 1 | タイトル未入力 | title=null | 200、"タイトルを入力してください"エラー表示 |
| 2 | タイトル空白のみ | title="   " | 200、"タイトルを入力してください"エラー表示 |
| 3 | ステータス未選択 | status=null | 200、"ステータスを選択してください"エラー表示 |
| 4 | 優先度未選択 | priority=null | 200、"優先度を選択してください"エラー表示 |
| 5 | 存在しないcategoryId | categoryId=99999 | 500 INTERNAL_SERVER_ERROR（外部キー制約違反） |

### 11.3 境界値

| No. | テストケース | 入力値 | 期待結果 |
| :--- | :--- | :--- | :--- |
| 1 | タイトル最大文字数（100文字） | title="a" × 100 | 302リダイレクト、正常登録 |
| 2 | タイトル文字数超過（101文字） | title="a" × 101 | 200、"タイトルは100文字以内で入力してください"エラー表示 |
| 3 | 説明最大文字数（1000文字） | description="a" × 1000 | 302リダイレクト、正常登録 |
| 4 | 説明文字数超過（1001文字） | description="a" × 1001 | 200、"説明は1000文字以内で入力してください"エラー表示 |

---

## 12. 備考

- Spring の @Valid アノテーションによりサーバーサイドバリデーションを実施
- バリデーションエラー時は入力済みデータを保持してフォームを再表示
- created_at と updated_at は自動設定（CURRENT_TIMESTAMP）
- MyBatisマッパーは `TaskMapper` (カスタムマッパー) を使用
- useGeneratedKeys=true により登録後の自動採番IDを取得可能

---

## 13. 変更履歴

| バージョン | 変更日 | 変更者 | 変更内容 |
| :--- | :--- | :--- | :--- |
| 1.0.0 | 2025-12-09 | API設計エージェント | 新規作成 |
