-- 初期カテゴリーデータ
INSERT INTO task_categories (name, description, color, display_order) VALUES
('仕事', '業務関連のタスク', '#0d6efd', 1),
('プライベート', '個人的なタスク', '#198754', 2),
('学習', '勉強・スキルアップ関連', '#ffc107', 3),
('その他', '分類できないタスク', '#6c757d', 99);

-- 初期タスクデータ
INSERT INTO tasks (title, description, status, priority, category_id, due_date) VALUES
('プロジェクト計画書作成', 'Q1のプロジェクト計画書を作成する', 'TODO', 'HIGH', 1, '2025-12-31'),
('コードレビュー', '新機能のプルリクエストをレビューする', 'IN_PROGRESS', 'MEDIUM', 1, '2025-12-15'),
('ドキュメント更新', 'API仕様書を最新版に更新', 'DONE', 'LOW', 1, '2025-12-01'),
('バグ修正 #123', 'ログイン画面のバリデーションエラーを修正', 'TODO', 'HIGH', 1, '2025-12-20'),
('ユニットテスト作成', 'サービス層のテストカバレッジを向上', 'IN_PROGRESS', 'MEDIUM', 3, '2025-12-25');
