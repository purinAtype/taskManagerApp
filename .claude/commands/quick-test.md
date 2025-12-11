---
description: Mavenテスト実行と結果サマリー表示
allowed-tools: Bash, Read, Glob, Grep
---

# クイックテスト実行

プロジェクトディレクトリ: C:\Users\ka2mi\OneDrive\develop\source\app\taskManagerApp

## 実行手順

1. Mavenテストを実行:
   ```bash
   cmd /c "cd /d C:\Users\ka2mi\OneDrive\develop\source\app\taskManagerApp && mvn clean test -q"
   ```

2. テスト結果サマリーを表示:
   - `target/surefire-reports/*.txt` からテスト統計を抽出
   - 成功/失敗数、実行時間を報告

3. 失敗がある場合:
   - 失敗したテストクラス名とメソッド名を表示
   - エラーメッセージの要約を提示
   - 修正のヒントを提案

## 出力フォーマット

```
## テスト結果サマリー

| 項目 | 結果 |
|:---|:---|
| 総テスト数 | X |
| 成功 | X |
| 失敗 | X |
| スキップ | X |

### 失敗テスト（ある場合）
- TestClass#testMethod: エラー内容
```
