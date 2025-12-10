# API設計書

> **出力先**: `docs/api/API設計書/`
> **ファイル名**: `API-CAT-004_カテゴリー更新.md`

## 1. 文書情報

| 項目 | 内容 |
| :--- | :--- |
| **API ID** | API-CAT-004 |
| **API名** | カテゴリー更新 |
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
| **HTTPメソッド** | `POST` |
| **機能概要** | 既存カテゴリー情報を更新する。バリデーションを実施し、成功時は詳細画面へリダイレクトする。 |
| **処理種別** | `データ更新` |
| **トランザクション** | `あり` |
| **認証** | `不要` |
| **必要権限** | `なし` |

### 使用場面

- カテゴリー編集画面での更新ボタン押下時

---

## 3. リクエスト仕様

### 3.1 パスパラメータ

| 名前 | 型 | 必須 | 説明 | 例 |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `Long` | ✔ | カテゴリーID | `1` |

### 3.2 クエリパラメータ

なし

### 3.3 リクエストボディ

**Content-Type**: `application/x-www-form-urlencoded` (フォーム送信)

**フィールド定義**:

| フィールド名 | 型 | 必須 | 説明 | 制約 |
| :--- | :--- | :--- | :--- | :--- |
| `name` | `String` | ✔ | カテゴリー名 | 最大50文字、空白のみ不可 |
| `description` | `String` | - | 説明 | 最大200文字 |
| `color` | `String` | ✔ | カラーコード | 7文字（#RRGGBB形式） |
| `displayOrder` | `Integer` | ✔ | 表示順 | 0以上の整数 |

### 3.4 バリデーションルール

| 項目 | バリデーション内容 | エラーメッセージ |
| :--- | :--- | :--- |
| `id` | 数値型であること | Spring が自動的にバインドエラーとして処理 |
| `name` | 必須、1～50文字、空白のみ不可 | "カテゴリー名を入力してください" |
| `name` | 最大50文字 | "カテゴリー名は50文字以内で入力してください" |
| `description` | 最大200文字 | "説明は200文字以内で入力してください" |
| `color` | 必須 | "カラーコードを入力してください" |
| `color` | #RRGGBB形式（正規表現: ^#[0-9A-Fa-f]{6}$） | "カラーコードの形式が不正です（例: #FF5733）" |
| `displayOrder` | 必須 | "表示順を入力してください" |
| `displayOrder` | 0以上の整数 | "表示順は0以上の整数を入力してください" |

**バリデーションアノテーション**:
- `@NotBlank`: name
- `@Size(max=50)`: name
- `@Size(max=200)`: description
- `@NotBlank`: color
- `@Pattern(regexp="^#[0-9A-Fa-f]{6}$")`: color
- `@NotNull`: displayOrder
- `@Min(0)`: displayOrder

---

## 4. レスポンス仕様

### 4.1 正常時レスポンス

* **HTTPステータスコード**: `302 Found` (リダイレクト)
* **リダイレクト先**: `/categories` (カテゴリー一覧画面)
* **フラッシュ属性**: `successMessage = "カテゴリーを更新しました"`

### 4.2 バリデーションエラー時レスポンス

* **HTTPステータスコード**: `200 OK`
* **Content-Type**: `text/html` (Thymeleafテンプレート)
* **レンダリングされるビュー**: `category/edit.html`

**モデル属性**:

| 属性名 | 型 | 必須 | 説明 |
| :--- | :--- | :--- | :--- |
| `categoryForm` | `CategoryForm` | ✔ | 入力済みデータを保持したフォーム |
| `org.springframework.validation.BindingResult.categoryForm` | `BindingResult` | ✔ | バリデーションエラー情報 |

---

## 5. 処理詳細

### 5.1 処理ステップ

| ステップ | 処理内容 | 備考 |
| :--- | :--- | :--- |
| 1 | パスパラメータから id を取得 | - |
| 2 | リクエストボディから CategoryForm を取得 | Spring が自動的にバインド |
| 3 | @Valid によりバリデーション実施 | - |
| 4 | バリデーションエラーがある場合、ステップ10へ | - |
| 5 | CategoryService.update(id, categoryForm) を呼び出し | トランザクション開始 |
| 6 | CategoryService.findById(id) でカテゴリー存在確認（SQL1） | - |
| 7 | カテゴリーが存在しない場合、NotFoundException をスロー | 404エラー |
| 8 | CategoryConverter でCategoryFormをCategoryエンティティに変換 | - |
| 9 | CategoryMapper.update(category) でデータベースを更新（SQL2） | - |
| 10 | トランザクションコミット、フラッシュ属性に成功メッセージを設定し、一覧画面へリダイレクト | 正常終了 |
| 11 | ビュー `category/edit.html` を再表示 | エラー終了 |

### 5.2 使用サービス

| サービスクラス | メソッド | 役割 |
| :--- | :--- | :--- |
| `CategoryService` | `update(id, categoryForm)` | カテゴリーを更新 |
| `CategoryService` | `findById(id)` | カテゴリー存在確認 |

### 5.3 使用テーブル

| テーブル名 | 物理名 | 操作 | 用途 |
| :--- | :--- | :--- | :--- |
| カテゴリー | `task_categories` | `SELECT` | カテゴリー存在確認 |
| カテゴリー | `task_categories` | `UPDATE` | カテゴリー情報の更新 |

### 5.4 SQL詳細

#### SQL1: カテゴリー存在確認

**目的**: 指定されたIDのカテゴリーが存在するか確認する

**Mapper**: `CategoryMapper.selectById`

```sql
SELECT id, name, description, color, display_order, created_at, updated_at
FROM task_categories
WHERE id = #{id}
```

**パラメータ**:

| パラメータ名 | 型 | 説明 | 例 |
| :--- | :--- | :--- | :--- |
| `#{id}` | `Long` | カテゴリーID | `1` |

#### SQL2: カテゴリー更新

**目的**: 既存カテゴリー情報を更新する

**Mapper**: `CategoryMapper.update`

```sql
UPDATE task_categories
SET
    name = #{name},
    description = #{description},
    color = #{color},
    display_order = #{displayOrder},
    updated_at = CURRENT_TIMESTAMP
WHERE id = #{id}
```

**パラメータ**:

| パラメータ名 | 型 | 説明 | 例 |
| :--- | :--- | :--- | :--- |
| `#{id}` | `Long` | カテゴリーID | `1` |
| `#{name}` | `String` | カテゴリー名 | `"仕事"` |
| `#{description}` | `String` | 説明 | `"仕事関連のタスク"` |
| `#{color}` | `String` | カラーコード | `"#007bff"` |
| `#{displayOrder}` | `Integer` | 表示順 | `1` |

**自動更新**:
- `updated_at`: CURRENT_TIMESTAMP

---

## 6. ビジネスロジック

### 6.1 データ変換ロジック

**CategoryForm → Category エンティティ変換**:
- CategoryConverter (MapStruct) を使用
- idは更新対象を特定するために使用
- created_atは更新しない（既存値を保持）

---

## 7. トランザクション制御

| 項目 | 内容 |
| :--- | :--- |
| **トランザクション境界** | CategoryService.update() メソッド全体 |
| **コミット条件** | カテゴリー更新SQL正常実行時 |
| **ロールバック条件** | SQL実行エラー、データベース制約違反時、カテゴリー未存在時 |
| **トランザクション管理** | Spring の @Transactional アノテーション |

---

## 8. エラーハンドリング

| エラー条件 | HTTPステータス | エラーコード | 処理内容 |
| :--- | :--- | :--- | :--- |
| カテゴリー未存在 | 404 | NOT_FOUND | 404エラー画面（SCR-CMN-001）を表示 |
| バリデーションエラー | 200 | - | フォーム再表示、エラーメッセージ表示 |
| DB制約違反 | 500 | INTERNAL_SERVER_ERROR | 500エラー画面（SCR-CMN-002）を表示、ログ出力 |
| DB接続エラー | 500 | INTERNAL_SERVER_ERROR | 500エラー画面（SCR-CMN-002）を表示、ログ出力 |

---

## 9. 外部連携

該当なし

---

## 10. 関連API

| API名 | エンドポイント | 関係性 |
| :--- | :--- | :--- |
| カテゴリー編集フォーム表示 | `GET /categories/{id}/edit` | フォーム表示元 |
| カテゴリー一覧取得 | `GET /categories` | 更新成功時のリダイレクト先 |

---

## 11. テストケース

### 11.1 正常系

| No. | テストケース | 入力値 | 期待結果 |
| :--- | :--- | :--- | :--- |
| 1 | 全項目更新 | id=1, name="仕事(更新)", description="更新後の説明", color="#FF5733", displayOrder=5 | 302リダイレクト、成功メッセージ表示 |
| 2 | 必須項目のみ更新 | id=1, name="個人", color="#28a745", displayOrder=2, description=null | 302リダイレクト、descriptionはnull |

### 11.2 異常系

| No. | テストケース | 入力値 | 期待結果 |
| :--- | :--- | :--- | :--- |
| 1 | 存在しないカテゴリー | id=99999 | 404 NOT_FOUND |
| 2 | カテゴリー名未入力 | name=null | 200、"カテゴリー名を入力してください"エラー表示 |
| 3 | カテゴリー名空白のみ | name="   " | 200、"カテゴリー名を入力してください"エラー表示 |
| 4 | カラーコード未入力 | color=null | 200、"カラーコードを入力してください"エラー表示 |
| 5 | カラーコード形式不正 | color="007bff"（#なし） | 200、"カラーコードの形式が不正です"エラー表示 |
| 6 | 表示順負の数 | displayOrder=-1 | 200、"表示順は0以上の整数を入力してください"エラー表示 |

### 11.3 境界値

| No. | テストケース | 入力値 | 期待結果 |
| :--- | :--- | :--- | :--- |
| 1 | カテゴリー名最大文字数（50文字） | name="a" × 50 | 302リダイレクト、正常更新 |
| 2 | カテゴリー名文字数超過（51文字） | name="a" × 51 | 200、"カテゴリー名は50文字以内で入力してください"エラー表示 |
| 3 | 説明最大文字数（200文字） | description="a" × 200 | 302リダイレクト、正常更新 |
| 4 | 説明文字数超過（201文字） | description="a" × 201 | 200、"説明は200文字以内で入力してください"エラー表示 |
| 5 | 表示順=0 | displayOrder=0 | 302リダイレクト、正常更新 |

---

## 12. 備考

- Spring の @Valid アノテーションによりサーバーサイドバリデーションを実施
- バリデーションエラー時は入力済みデータを保持してフォームを再表示
- updated_at は自動更新（CURRENT_TIMESTAMP）
- created_at は更新しない
- MyBatisマッパーは `CategoryMapper` (カスタムマッパー) を使用

---

## 13. 変更履歴

| バージョン | 変更日 | 変更者 | 変更内容 |
| :--- | :--- | :--- | :--- |
| 1.0.0 | 2025-12-10 | API設計エージェント | 新規作成 |
