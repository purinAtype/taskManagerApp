# アーキテクチャ設計書

## 1. 概要

タスク管理アプリケーションのアーキテクチャ設計書。
レイヤードアーキテクチャを採用し、各層の責務を明確に分離する。

## 2. 技術スタック

| カテゴリ | 技術 | バージョン |
|---------|------|-----------|
| 言語 | Java | 17+ |
| フレームワーク | Spring Boot | 3.2.x |
| ORM | MyBatis | 3.5.x |
| データベース | H2 Database | 2.2.x |
| テンプレート | Thymeleaf | 3.1.x |
| マッピング | MapStruct | 1.5.x |
| ユーティリティ | Lombok | 1.18.x |
| テスト | JUnit 5 | 5.10.x |

## 3. レイヤードアーキテクチャ

```
┌─────────────────────────────────────────┐
│           Presentation Layer            │
│         (Controller + View)             │
├─────────────────────────────────────────┤
│           Application Layer             │
│              (Service)                  │
├─────────────────────────────────────────┤
│            Domain Layer                 │
│         (Entity + DTO)                  │
├─────────────────────────────────────────┤
│         Infrastructure Layer            │
│        (Repository/Mapper)              │
└─────────────────────────────────────────┘
```

## 4. 各レイヤーの責務

### 4.1 Presentation Layer (プレゼンテーション層)
- **責務**: ユーザーインターフェースの提供、リクエスト/レスポンスの処理
- **構成要素**:
  - Controller: HTTPリクエストの受付とレスポンス返却
  - View (Thymeleaf): HTMLテンプレートのレンダリング
  - Form: 画面入力データのバインディング

### 4.2 Application Layer (アプリケーション層)
- **責務**: ビジネスロジックの実装、トランザクション管理
- **構成要素**:
  - Service: ビジネスロジックの実装
  - Mapper (MapStruct): Entity-DTO変換

### 4.3 Domain Layer (ドメイン層)
- **責務**: ドメインモデルの定義
- **構成要素**:
  - Entity: データベーステーブルに対応するオブジェクト
  - DTO: データ転送オブジェクト
  - Enum: ステータス、優先度などの列挙型

### 4.4 Infrastructure Layer (インフラストラクチャ層)
- **責務**: データアクセス、外部システム連携
- **構成要素**:
  - Mapper (MyBatis): SQLマッピング
  - Repository: データアクセスの抽象化

## 5. パッケージ構成

```
com.example.taskmanager
├── config/                    # 設定クラス
│   ├── WebConfig.java
│   └── MyBatisConfig.java
├── controller/                # コントローラー
│   └── TaskController.java
├── service/                   # サービス
│   ├── TaskService.java
│   └── impl/
│       └── TaskServiceImpl.java
├── mapper/                    # MyBatis Mapper
│   └── TaskMapper.java
├── dto/                       # DTO
│   ├── TaskDto.java
│   ├── TaskCreateRequest.java
│   └── TaskUpdateRequest.java
├── entity/                    # エンティティ
│   └── Task.java
├── form/                      # フォーム
│   └── TaskForm.java
├── converter/                 # MapStruct Converter
│   └── TaskConverter.java
├── enums/                     # 列挙型
│   ├── TaskStatus.java
│   └── TaskPriority.java
├── exception/                 # 例外
│   ├── TaskNotFoundException.java
│   └── GlobalExceptionHandler.java
└── TaskManagerApplication.java
```

## 6. 依存関係図

```
Controller
    │
    ↓
Service (Interface)
    │
    ↓
ServiceImpl ──→ TaskConverter (MapStruct)
    │
    ↓
Mapper (MyBatis)
    │
    ↓
H2 Database
```

## 7. データフロー

### 7.1 画面表示（GET）
```
Browser → Controller → Service → Mapper → DB
                ↓
Browser ← View (Thymeleaf) ← DTO ← Entity
```

### 7.2 データ登録（POST）
```
Browser → Form → Controller → Service → Mapper → DB
                      ↓
                 Validation
                      ↓
                 Entity変換
```

## 8. 設計原則

### 8.1 単一責任の原則 (SRP)
- 各クラスは単一の責務のみを持つ
- Controller: リクエスト処理のみ
- Service: ビジネスロジックのみ
- Mapper: データアクセスのみ

### 8.2 依存性逆転の原則 (DIP)
- 上位レイヤーは下位レイヤーの抽象に依存
- ServiceはMapperインターフェースに依存

### 8.3 インターフェース分離の原則 (ISP)
- ServiceはInterfaceを定義し、Implで実装

## 9. エラーハンドリング

- GlobalExceptionHandlerで一元管理
- 業務例外とシステム例外を分離
- 適切なHTTPステータスコードを返却

## 10. トランザクション管理

- Service層で@Transactionalアノテーションを使用
- デフォルトはREQUIRED
- 読み取り専用操作にはreadOnly=trueを指定
