---
description: コンパイル検証（テストなし・高速）
allowed-tools: Bash
---

# コンパイル検証

プロジェクトディレクトリ: C:\Users\ka2mi\OneDrive\develop\source\app\taskManagerApp

## 実行手順

1. Mavenコンパイルを実行（テストはスキップ）:
   ```bash
   cmd /c "cd /d C:\Users\ka2mi\OneDrive\develop\source\app\taskManagerApp && mvn clean compile -q"
   ```

2. 結果を報告:
   - 成功時: 「コンパイル成功」と表示
   - 失敗時: エラー内容を詳細表示し、修正箇所を特定

## 出力フォーマット

### 成功時
```
## コンパイル結果: OK

ビルド成功。コンパイルエラーはありません。
```

### 失敗時
```
## コンパイル結果: NG

### エラー内容
- ファイル: XxxClass.java
- 行番号: XX
- エラー: [エラーメッセージ]

### 修正提案
[具体的な修正方法]
```
