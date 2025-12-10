# API設計書

> **出力先**: `docs/api/API設計書/`
> **ファイル名**: `API-CAT-002_カテゴリー詳細取得.md`

## 1. 文書情報

| 項目 | 内容 |
| :--- | :--- |
| **API ID** | API-CAT-002 |
| **API名** | カテゴリー詳細取得 |
| **カテゴリ** | カテゴリー管理 |
| **バージョン** | 1.0.0 |
| **作成日** | 2025-12-10 |
| **作成者** | API設計エージェント |
| **最終更新日** | 2025-12-10 |
| **最終更新者** | API設計エージェント |

---

## 2. API概要

| 項目 | 内容 |
| :--- | :--- |
| **エンドポイント** | `/categories/{id}` |
| **HTTPメソッド** | `GET` |
| **機能概要** | 指定されたIDのカテゴリー詳細情報と、そのカテゴリーに属するタスク一覧を取得する。 |
| **処理種別** | `データ取得` |
| **トランザクション** | `なし` |
| **認証** | `不要` |
| **必要権限** | `なし` |

### 使用場面

- カテゴリー詳細画面の表示
- カテゴリー一覧画面からの詳細遷移

---

## 3. リクエスト仕様

### 3.1 パスパラメータ

| 名前 | 型 | 必須 | 説明 | 例 |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `Long` | ✔ | カテゴリーID | `1` |

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

**レンダリングされるビュー**: `category/detail.html`

**モデル属性**:

| 属性名 | 型 | 必須 | 説明 | 例 |
| :--- | :--- | :--- | :--- | :--- |
| `category` | `CategoryDto` | ✔ | カテゴリーDTO | - |
| `tasks` | `List<TaskDto>` | ✔ | このカテゴリーに属するタスクのリスト | - |
| `successMessage` | `String` | - | 成功メッセージ（フラッシュ属性） | `"カテゴリーを更新しました"` |

**CategoryDto のフィールド定義**:

| フィールド名 | 型 | 必須 | 説明 | 例 |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `Long` | ✔ | カテゴリーID | `1` |
| `name` | `String` | ✔ | カテゴリー名 | `"仕事"` |
| `description` | `String` | - | 説明 | `"仕事関連のタスク"` |
| `color` | `String` | ✔ | カラーコード | `"#007bff"` |
| `displayOrder` | `Integer` | ✔ | 表示順 | `1` |
| `taskCount` | `Integer` | ✔ | このカテゴリーに属するタスク数 | `5` |
| `createdAt` | `LocalDateTime` | ✔ | 作成日時 | `2025-01-15T10:30:00` |
| `updatedAt` | `LocalDateTime` | ✔ | 更新日時 | `2025-01-15T10:30:00` |

**TaskDto のフィールド定義**:

| フィールド名 | 型 | 必須 | 説明 | 例 |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `Long` | ✔ | タスクID | `1` |
| `title` | `String` | ✔ | タスクタイトル | `"レポート作成"` |
| `status` | `TaskStatus` | ✔ | ステータス | `TODO` |
| `priority` | `TaskPriority` | ✔ | 優先度 | `HIGH` |
| `dueDate` | `LocalDate` | - | 期限日 | `2025-12-31` |

---

## 5. 処理詳細

### 5.1 処理ステップ

| ステップ | 処理内容 | 備考 |
| :--- | :--- | :--- |
| 1 | CategoryService.findById(id) を呼び出し | SQL1 |
| 2 | カテゴリーが存在しない場合、NotFoundException をスロー | 404エラー |
| 3 | TaskService.findByCategoryId(id) を呼び出し | SQL2 |
| 4 | モデルに取得データを設定 | - |
| 5 | ビュー `category/detail.html` をレンダリング | - |

### 5.2 使用サービス

| サービスクラス | メソッド | 役割 |
| :--- | :--- | :--- |
| `CategoryService` | `findById(id)` | 指定IDのカテゴリーを取得 |
| `TaskService` | `findByCategoryId(categoryId)` | 指定カテゴリーに属するタスクを取得 |

### 5.3 使用テーブル

| テーブル名 | 物理名 | 操作 | 用途 |
| :--- | :--- | :--- | :--- |
| カテゴリー | `task_categories` | `SELECT` | カテゴリー情報の取得 |
| タスク | `tasks` | `SELECT` | カテゴリーに属するタスクの取得 |

### 5.4 SQL詳細

#### SQL1: カテゴリー詳細取得（タスク数付き）

**目的**: 指定されたIDのカテゴリー情報とタスク数を取得する

**Mapper**: `CategoryMapper.selectByIdWithTaskCount`

```sql
SELECT
    c.id,
    c.name,
    c.description,
    c.color,
    c.display_order,
    c.created_at,
    c.updated_at,
    COUNT(t.id) AS task_count
FROM task_categories c
LEFT JOIN tasks t ON c.id = t.category_id
WHERE c.id = #{id}
GROUP BY c.id, c.name, c.description, c.color, c.display_order, c.created_at, c.updated_at
```

**パラメータ**:

| パラメータ名 | 型 | 説明 | 例 |
| :--- | :--- | :--- | :--- |
| `#{id}` | `Long` | カテゴリーID | `1` |

#### SQL2: カテゴリーに属するタスク一覧取得

**目的**: 指定されたカテゴリーIDに属する全タスクを取得する

**Mapper**: `TaskMapper.selectByCategoryId`

```sql
SELECT
    id,
    title,
    description,
    status,
    priority,
    category_id,
    due_date,
    created_at,
    updated_at
FROM tasks
WHERE category_id = #{categoryId}
ORDER BY created_at DESC
```

**パラメータ**:

| パラメータ名 | 型 | 説明 | 例 |
| :--- | :--- | :--- | :--- |
| `#{categoryId}` | `Long` | カテゴリーID | `1` |

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
| カテゴリー未存在 | 404 | NOT_FOUND | 404エラー画面（SCR-CMN-001）を表示 |
| 不正なID形式 | 400 | BAD_REQUEST | Spring が自動的にエラーレスポンスを返却 |
| DB接続エラー | 500 | INTERNAL_SERVER_ERROR | 500エラー画面（SCR-CMN-002）を表示、ログ出力 |

---

## 9. 外部連携

該当なし

---

## 10. 関連API

| API名 | エンドポイント | 関係性 |
| :--- | :--- | :--- |
| カテゴリー一覧取得 | `/categories` | 詳細から一覧への戻り |
| カテゴリー編集フォーム表示 | `/categories/{id}/edit` | 詳細から編集遷移 |
| カテゴリー削除 | `POST /categories/{id}/delete` | 詳細画面からの削除 |

---

## 11. テストケース

### 11.1 正常系

| No. | テストケース | 入力値 | 期待結果 |
| :--- | :--- | :--- | :--- |
| 1 | 存在するカテゴリーを取得 | `id=1` | 200 OK、カテゴリー情報とタスク一覧を取得 |
| 2 | タスク0件のカテゴリー | `id=2`（タスクなし） | 200 OK、tasks=空リスト |

### 11.2 異常系

| No. | テストケース | 入力値 | 期待結果 |
| :--- | :--- | :--- | :--- |
| 1 | 存在しないカテゴリー | `id=99999` | 404 NOT_FOUND |
| 2 | 不正なID形式 | `id=abc` | 400 BAD_REQUEST |

### 11.3 境界値

| No. | テストケース | 入力値 | 期待結果 |
| :--- | :--- | :--- | :--- |
| 1 | ID=0 | `id=0` | 404 NOT_FOUND（存在しないため） |
| 2 | ID最大値 | `id=9223372036854775807` | 404 NOT_FOUND（存在しない場合） |

---

## 12. 備考

- タスク数はLEFT JOINとCOUNT集計で算出
- タスクが紐づいていないカテゴリーもtask_count=0として表示
- タスク一覧は作成日時の降順（新しい順）でソート
- MyBatisマッパーは `CategoryMapper` と `TaskMapper` (カスタムマッパー) を使用

---

## 13. 変更履歴

| バージョン | 変更日 | 変更者 | 変更内容 |
| :--- | :--- | :--- | :--- |
| 1.0.0 | 2025-12-10 | API設計エージェント | 新規作成 |
