/**
 * Screen Image Generator
 *
 * HTMLモックアップからPNG画像を生成するスクリプト。
 * Playwrightを使用してスクリーンショットを作成します。
 *
 * @usage
 *   node generate.js                    # 全ファイル生成
 *   node generate.js --file 01_task_list.html  # 特定ファイルのみ
 *   node generate.js --scale 2          # Retina品質
 *   node generate.js --fullpage         # ページ全体
 */

const { chromium } = require('playwright');
const path = require('path');
const fs = require('fs');

// プロジェクトルートからの相対パス
const PROJECT_ROOT = path.resolve(__dirname, '../../..');
const MOCKUPS_DIR = path.join(PROJECT_ROOT, 'docs/mockups');
const OUTPUT_DIR = path.join(PROJECT_ROOT, 'docs/screen/images');

/**
 * ファイル名から画面IDへの変換マッピング
 */
const FILE_TO_SCREEN_ID = {
  '01_task_list.html': 'SCR-TASK-001',
  '02_task_new.html': 'SCR-TASK-002',
  '03_task_detail.html': 'SCR-TASK-003',
  '04_task_edit.html': 'SCR-TASK-004',
  '05_error_404.html': 'SCR-CMN-001',
  '06_error_500.html': 'SCR-CMN-002',
};

/**
 * ファイル名から画面IDを取得
 * マッピングにない場合は自動生成
 *
 * @param {string} filename - HTMLファイル名
 * @returns {string} 画面ID
 */
function getScreenId(filename) {
  // マッピングに存在する場合はそれを使用
  if (FILE_TO_SCREEN_ID[filename]) {
    return FILE_TO_SCREEN_ID[filename];
  }

  // 自動生成: 番号_画面名.html → SCR-{機能}-{連番}
  const match = filename.match(/^(\d+)_(.+)\.html$/);
  if (!match) {
    // フォーマットに合わない場合はファイル名をそのまま使用
    return filename.replace('.html', '');
  }

  const [, num, name] = match;
  const paddedNum = num.padStart(3, '0');

  // 機能コードの判定
  let funcCode = 'CMN';
  if (name.startsWith('task_') || name.startsWith('task')) {
    funcCode = 'TASK';
  } else if (name.startsWith('category_') || name.startsWith('cat')) {
    funcCode = 'CAT';
  } else if (name.startsWith('error_') || name.startsWith('error')) {
    funcCode = 'CMN';
  }

  return `SCR-${funcCode}-${paddedNum}`;
}

/**
 * コマンドライン引数をパース
 *
 * @param {string[]} args - process.argv
 * @returns {Object} パースされたオプション
 */
function parseArgs(args) {
  const options = {
    file: null,
    width: 1280,
    height: 800,
    scale: 1,
    fullPage: false,
  };

  for (let i = 2; i < args.length; i++) {
    switch (args[i]) {
      case '--file':
        options.file = args[++i];
        break;
      case '--width':
        options.width = parseInt(args[++i], 10);
        break;
      case '--height':
        options.height = parseInt(args[++i], 10);
        break;
      case '--scale':
        options.scale = parseFloat(args[++i]);
        break;
      case '--fullpage':
        options.fullPage = true;
        break;
      case '--help':
        console.log(`
Usage: node generate.js [options]

Options:
  --file <filename>   特定ファイルのみ処理
  --width <px>        ビューポート幅 (default: 1280)
  --height <px>       ビューポート高さ (default: 800)
  --scale <factor>    デバイススケール (default: 1)
  --fullpage          ページ全体をキャプチャ
  --help              ヘルプを表示
        `);
        process.exit(0);
    }
  }

  return options;
}

/**
 * スクリーンショットを生成
 *
 * @param {Object} options - 生成オプション
 */
async function generateScreenshots(options) {
  // 出力ディレクトリの作成
  if (!fs.existsSync(OUTPUT_DIR)) {
    fs.mkdirSync(OUTPUT_DIR, { recursive: true });
    console.log(`出力ディレクトリを作成: ${OUTPUT_DIR}`);
  }

  // 対象ファイルの取得
  let files;
  if (options.file) {
    const filePath = path.join(MOCKUPS_DIR, options.file);
    if (!fs.existsSync(filePath)) {
      console.error(`エラー: ファイルが見つかりません: ${options.file}`);
      process.exit(1);
    }
    files = [options.file];
  } else {
    files = fs.readdirSync(MOCKUPS_DIR)
      .filter(f => f.endsWith('.html'))
      .sort();
  }

  if (files.length === 0) {
    console.log('処理対象のHTMLファイルがありません。');
    return;
  }

  console.log('生成開始...');
  console.log(`ビューポート: ${options.width}x${options.height}`);
  console.log(`スケール: ${options.scale}`);
  console.log(`全ページ: ${options.fullPage}`);
  console.log('---');

  // Playwrightブラウザの起動
  const browser = await chromium.launch();
  const context = await browser.newContext({
    viewport: { width: options.width, height: options.height },
    deviceScaleFactor: options.scale,
  });
  const page = await context.newPage();

  let successCount = 0;
  let errorCount = 0;

  for (let i = 0; i < files.length; i++) {
    const filename = files[i];
    const screenId = getScreenId(filename);
    const inputPath = path.join(MOCKUPS_DIR, filename);
    const outputPath = path.join(OUTPUT_DIR, `${screenId}.png`);

    try {
      // ファイルURLでページを開く
      await page.goto(`file://${inputPath}`, {
        waitUntil: 'networkidle',
        timeout: 30000,
      });

      // 少し待機してレンダリングを完了させる
      await page.waitForTimeout(500);

      // スクリーンショットを撮影
      await page.screenshot({
        path: outputPath,
        fullPage: options.fullPage,
      });

      console.log(`[${i + 1}/${files.length}] ${filename} → ${screenId}.png ✓`);
      successCount++;
    } catch (error) {
      console.error(`[${i + 1}/${files.length}] ${filename} → エラー: ${error.message}`);
      errorCount++;
    }
  }

  await browser.close();

  console.log('---');
  console.log(`生成完了: ${successCount}ファイル${errorCount > 0 ? `, エラー: ${errorCount}ファイル` : ''}`);
  console.log(`出力先: ${OUTPUT_DIR}`);
}

// メイン処理
const options = parseArgs(process.argv);
generateScreenshots(options).catch(error => {
  console.error('予期しないエラーが発生しました:', error);
  process.exit(1);
});
