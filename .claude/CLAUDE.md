# タスク管理アプリケーション (task-manager-app)

## プロジェクト概要

マルチエージェントによる自動化デモを目的としたタスク管理アプリケーション。

## 技術スタック

| カテゴリ | 技術 | バージョン |
|:---|:---|:---|
| 言語 | Java | 21 |
| フレームワーク | Spring Boot | 3.4.9 |
| ORM | MyBatis | 3.0.3 |
| DB | H2 Database | (embedded) |
| テンプレート | Thymeleaf | 3.x |
| マッピング | MapStruct | 1.5.5 |
| ユーティリティ | Lombok | 1.18.x |
| UI | Bootstrap | 5.3.2 |
| アイコン | Bootstrap Icons | 1.11.1 |
| テスト | JUnit 5 | 5.x |
| ビルド | Maven | 3.x |

---

## ⚠️ 最重要: エージェント起動ルール

**このセクションは全リクエストで最初に確認すること**

### 基本原則
- 以下のキーワードを含むリクエストでは、**Taskツールを必ず使用**してエージェントを起動
- キーワードが含まれていれば、単純な質問でもエージェントを起動
- ユーザーが明示的に「エージェント不要」と言わない限り直接対応は禁止

### Skill優先ルール
**Skills > Agents**: 特定タスクに特化したSkillが存在する場合、エージェントより優先して使用

| タスク | 使用するSkill | 説明 |
|:---|:---|:---|
| テーブル定義書生成 | `table-definition-generator` | schema.sqlからテーブル定義書を自動生成 |
| ER図生成 | `er-diagram-generator` | schema.sqlからER図を自動生成 |
| 画面画像生成 | `screen-image-generator` | HTMLモックからPNG画像を自動生成 |

> **重要**: 上記タスクではエージェントを起動せず、必ずSkillを使用すること

### エージェント一覧

| 優先度 | Agent | 起動キーワード | 役割 | 除外条件 |
|:---|:---|:---|:---|:---|
| 1 | `architecture-specialist` | アーキテクチャ, 技術選定, DB設計, ER図 | システム設計・技術選定 | ER図生成単体は`er-diagram-generator` Skill使用 |
| 2 | `screen-designer` | 画面設計書, 画面遷移, モック, Thymeleaf | 画面設計書・モックアップ作成 | - |
| 3 | `api-designer` | API設計書, エンドポイント, Controller | API設計書作成 | - |
| 4 | `database-specialist` | テーブル変更, カラム追加, スキーマ変更, DDL | データベーススキーマ変更・関連ファイル一括更新 | テーブル定義書生成は`table-definition-generator` Skill使用 |
| 5 | `implementation-specialist` | 実装, コーディング, 機能追加 | バックエンド・フロントエンド実装 | - |
| 6 | `code-reviewer` | コードレビュー, 品質確認, リファクタリング | コード品質チェック | - |
| 7 | `test-specialist` | テスト, JUnit, カバレッジ | テスト作成・実行 | - |
| 8 | `quality-analyst` | 品質分析, メトリクス, 改善提案 | テスト品質分析 | - |
| 9 | `design-reviewer` | 設計書レビュー, 設計書チェック, テンプレート準拠確認 | 設計書品質レビュー（テンプレート準拠・規約整合性・統一性） | - |
| 10 | `consistency-checker` | 整合性チェック, モック確認, 三者比較, 画面整合性 | モック・設計書・実装の三者間整合性チェック | - |
| 11 | `code-health-analyzer` | 未使用ファイル, デッドコード, 循環依存, 技術的負債 | コードベース健全性分析 | - |
| 12 | `pm-agent` | PM, 進捗管理, 全体調整 | プロジェクト管理・タスク振り分け | - |

### 並列起動ルール

| パターン | 並列起動 | 説明 |
|:---|:---|:---|
| screen-designer + api-designer | ✅ 可 | 画面とAPI同時設計 |
| implementation-specialist + test-specialist | ✅ 可 | 実装とテスト同時進行 |
| code-reviewer + quality-analyst | ✅ 可 | コードと品質の同時レビュー |
| consistency-checker + design-reviewer | ✅ 可 | 三者整合性と設計書整合性の同時チェック |
| pm-agent | ❌ 単独推奨 | 全体調整は単独で実行 |

---

## 1. プロジェクト構成

```
taskManagerApp/
├── .claude/
│   ├── CLAUDE.md              # 本ファイル
│   ├── agents/                # エージェント定義
│   │   ├── architecture-specialist.md
│   │   ├── screen-designer.md
│   │   ├── api-designer.md
│   │   ├── database-specialist.md
│   │   ├── implementation-specialist.md
│   │   ├── code-reviewer.md
│   │   ├── test-specialist.md
│   │   ├── quality-analyst.md
│   │   ├── design-reviewer.md
│   │   ├── consistency-checker.md
│   │   ├── code-health-analyzer.md
│   │   └── pm-agent.md
│   └── skills/                # スキル定義
│       ├── table-definition-generator/
│       ├── er-diagram-generator/
│       └── screen-image-generator/
├── docs/
│   ├── architecture/          # アーキテクチャ設計書
│   │   └── architecture.md
│   ├── screen/                # 画面設計書
│   │   ├── 画面一覧.md
│   │   ├── 画面遷移図.md
│   │   └── 画面設計書テンプレート.md
│   ├── api/                   # API設計書
│   │   ├── API一覧.md
│   │   └── API設計書テンプレート.md
│   ├── guidelines/            # コーディング規約
│   │   ├── java-coding-rules.md
│   │   ├── sql-coding-rules.md
│   │   └── mybatis-generator-guide.md
│   └── mockups/               # HTMLモックアップ
│       ├── 01_task_list.html
│       ├── 02_task_new.html
│       ├── 03_task_detail.html
│       ├── 04_task_edit.html
│       ├── 05_error_404.html
│       └── 06_error_500.html
├── src/
│   ├── main/
│   │   ├── java/com/example/taskmanager/
│   │   │   ├── common/            # 共通モジュール
│   │   │   │   ├── controller/    # 共通コントローラー
│   │   │   │   ├── entity/        # エンティティ（自動生成）
│   │   │   │   ├── enums/         # 列挙型
│   │   │   │   ├── exception/     # 例外処理
│   │   │   │   └── mapper/        # MyBatis Mapper（自動生成）
│   │   │   ├── task/              # タスク機能モジュール
│   │   │   │   ├── controller/    # タスクコントローラー
│   │   │   │   ├── converter/     # MapStruct変換
│   │   │   │   ├── dto/           # DTO
│   │   │   │   ├── form/          # フォーム
│   │   │   │   ├── mapper/        # カスタムMapper
│   │   │   │   ├── model/         # モデル
│   │   │   │   └── service/       # サービス層
│   │   │   └── TaskManagerApplication.java
│   │   └── resources/
│   │       ├── mapper/            # MyBatis XML
│   │       ├── templates/         # Thymeleafテンプレート
│   │       │   ├── fragments/     # 共通フラグメント
│   │       │   ├── task/          # タスク画面
│   │       │   └── error/         # エラー画面
│   │       ├── application.yml
│   │       ├── schema.sql
│   │       └── data.sql
│   └── test/
│       └── java/com/example/taskmanager/
│           ├── controller/        # コントローラーテスト
│           ├── service/           # サービステスト
│           └── TaskManagerApplicationTests.java
├── pom.xml
└── progress.md                    # 進捗管理
```

---

## 2. 画面ID・ファイル命名規則

### 画面ID

画面IDの詳細は画面一覧を参照: @docs/screen/画面一覧.md

### ファイル命名規則

| 対象 | フォーマット | 出力先 | 例 |
|:---|:---|:---|:---|
| 画面設計書 | `SCR-{機能}-{連番}_{画面名}.md` | `docs/screen/画面設計書/` | `SCR-TASK-001_タスク一覧.md` |
| API設計書 | `API-{機能}-{連番}_{API名}.md` | `docs/api/API設計書/` | `API-TASK-001_タスク一覧取得.md` |
| モックアップ | `{番号}_{画面名_snake}.html` | `docs/mockups/` | `01_task_list.html` |

### 機能コード一覧

| 機能コード | 機能名 | 説明 |
|:---|:---|:---|
| TASK | タスク管理 | タスクのCRUD操作 |
| CAT | カテゴリー管理 | カテゴリーのCRUD操作 |
| AUTH | 認証 | ログイン・ログアウト |
| CMN | 共通 | 共通機能・エラー画面等 |

---

## 3. 共通禁止事項

1. **推測記述の禁止**: 実装コードにない仕様を勝手に創作しない
2. **テンプレート改変の禁止**: 指定されたMarkdownテンプレートの構造を崩さない
3. **機密情報の記載禁止**: パスワード、APIキー、個人情報は絶対に記載しない
4. **自動生成ファイルの直接編集禁止**: MapStruct生成コード等は編集しない

---

## 4. バージョン管理ルール

設計書修正時は以下のロジックでヘッダーを更新する。

| 条件 | バージョン | 更新日 |
|:---|:---|:---|
| 最終更新日 = 今日 | **変更しない** | 今日に更新 |
| 最終更新日 < 今日 | **マイナーバージョンUP** (1.0.0 → 1.1.0) | 今日に更新 |
| 新規作成 | 1.0.0 | 作成日 = 今日 |

---

## 5. 開発コマンド

```bash
# ビルド
mvn clean compile

# テスト実行
mvn test

# アプリケーション起動
mvn spring-boot:run

# パッケージング
mvn clean package
```

---

## 6. 参照ドキュメント

| ドキュメント | パス | 説明 |
|:---|:---|:---|
| 進捗管理 | `progress.md` | フェーズ別進捗・エージェント実行履歴 |
| アーキテクチャ | `docs/architecture/architecture.md` | システム設計 |
| 画面一覧 | `docs/screen/画面一覧.md` | 画面ID管理 |
| 画面遷移図 | `docs/screen/画面遷移図.md` | 画面遷移フロー |
| API一覧 | `docs/api/API一覧.md` | エンドポイント定義 |
| Javaコーディング規約 | `docs/guidelines/java-coding-rules.md` | Java記述ルール |
| SQLコーディング規約 | `docs/guidelines/sql-coding-rules.md` | SQL記述ルール |
| MyBatis Generatorガイド | `docs/guidelines/mybatis-generator-guide.md` | 自動生成設定 |

> **Note**: 作業開始時は `progress.md` で現在のフェーズと進捗を確認してください。

---

## 7. コーディングルール

Javaのコーディング規約は以下を参照: @docs/guidelines/java-coding-rules.md

SQLのコーディング規約は以下を参照: @docs/guidelines/sql-coding-rules.md
