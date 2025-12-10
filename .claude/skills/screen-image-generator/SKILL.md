---
name: screen-image-generator
description: |
  HTMLモックアップからPNG画像を生成するスキル。
  Playwrightを使用して高品質なスクリーンショットを作成し、画面設計書用の画像を自動生成します。
allowed-tools: Read, Glob, Bash, Write
---
# Screen Image Generator

HTMLモックアップをPNG画像に変換し、画面設計書で参照する画像を自動生成するスキル。

## 概要

| 項目 | 内容 |
|:---|:---|
| **入力** | `docs/mockups/*.html` |
| **出力** | `docs/screen/images/{画面ID}.png` |
| **ツール** | Playwright (Chromium) |
| **解像度** | 1280×800（デフォルト） |

## 処理フロー

1. **依存関係確認**: `node_modules/playwright` の存在を確認
2. **インストール**: 未インストールの場合、`npm install` を実行
3. **モックアップ検索**: `docs/mockups/*.html` を検索
4. **ファイル名変換**: モックファイル名 → 画面ID に変換
5. **スクリーンショット生成**: Playwrightでキャプチャ
6. **出力**: `docs/screen/images/` に保存

## ファイル名変換ルール

| モックファイル名 | 画面ID | 出力ファイル |
|:---|:---|:---|
| `01_task_list.html` | `SCR-TASK-001` | `SCR-TASK-001.png` |
| `02_task_new.html` | `SCR-TASK-002` | `SCR-TASK-002.png` |
| `03_task_detail.html` | `SCR-TASK-003` | `SCR-TASK-003.png` |
| `04_task_edit.html` | `SCR-TASK-004` | `SCR-TASK-004.png` |
| `05_error_404.html` | `SCR-CMN-001` | `SCR-CMN-001.png` |
| `06_error_500.html` | `SCR-CMN-002` | `SCR-CMN-002.png` |

### 変換ロジック

```
番号_画面名.html → SCR-{機能コード}-{連番}.png

機能コードの判定:
- task_* → TASK
- category_* → CAT
- error_* → CMN
- その他 → CMN
```

## 実行方法

### 全画面を生成
```bash
cd .claude/skills/screen-image-generator
node generate.js
```

### 特定画面のみ生成
```bash
node generate.js --file 01_task_list.html
```

### オプション

| オプション | 説明 | デフォルト |
|:---|:---|:---|
| `--file <filename>` | 特定ファイルのみ処理 | 全ファイル |
| `--width <px>` | ビューポート幅 | 1280 |
| `--height <px>` | ビューポート高さ | 800 |
| `--scale <factor>` | デバイススケール（Retina対応） | 1 |
| `--fullpage` | ページ全体をキャプチャ | false |

## 設定

### デフォルト設定

```javascript
const config = {
  viewport: { width: 1280, height: 800 },
  deviceScaleFactor: 1,
  fullPage: false,
  format: 'png'
};
```

### Retina品質で生成する場合

```bash
node generate.js --scale 2
```

## 前提条件

1. **Node.js**: v18以上
2. **Playwright**: 自動インストール（初回実行時）

## 出力例

```
生成開始...
[1/6] 01_task_list.html → SCR-TASK-001.png ✓
[2/6] 02_task_new.html → SCR-TASK-002.png ✓
[3/6] 03_task_detail.html → SCR-TASK-003.png ✓
[4/6] 04_task_edit.html → SCR-TASK-004.png ✓
[5/6] 05_error_404.html → SCR-CMN-001.png ✓
[6/6] 06_error_500.html → SCR-CMN-002.png ✓
生成完了: 6ファイル
出力先: docs/screen/images/
```

## トラブルシューティング

| 問題 | 原因 | 解決策 |
|:---|:---|:---|
| `Chromium not found` | ブラウザ未インストール | `npx playwright install chromium` |
| 画像が真っ白 | ローカルリソース読み込み失敗 | HTMLの相対パスを確認 |
| 日本語が文字化け | フォント未インストール | システムに日本語フォントをインストール |

## 関連ファイル

| ファイル | 説明 |
|:---|:---|
| `SKILL.md` | 本ファイル（スキル定義） |
| `generate.js` | Playwrightスクリプト |
| `package.json` | Node.js依存関係 |
