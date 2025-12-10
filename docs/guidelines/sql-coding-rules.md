# SQLコーディングルール（MyBatis XML）

## 概要

タスク管理アプリケーション（task-manager-app）のMyBatis XMLマッパーファイルにおけるSQL記述規約を定義します。

---

## 基本方針

1. **可読性**: SQLは読みやすくフォーマットする
2. **保守性**: 複雑なクエリは適切にコメント
3. **セキュリティ**: パラメータバインディングを使用（SQLインジェクション対策）
4. **パフォーマンス**: 効率的なクエリを記述
5. **自動生成活用**: INSERT/UPDATEはMyBatis Generator自動生成メソッドを使用

---

## ファイル構成

### ディレクトリ構造

```
src/main/resources/
├── mapper/                    # MyBatis XMLマッパー
│   ├── common/                # MyBatis Generator自動生成（編集禁止）
│   │   └── TaskMapper.xml
│   └── custom/                # カスタムマッパー（手書き）
│       └── TaskCustomMapper.xml
├── schema.sql                 # DDL（テーブル定義）
└── data.sql                   # 初期データ
```

### ファイル命名規則

| 種類 | 命名規則 | 例 |
|------|---------|-----|
| **自動生成** | `{Entity}Mapper.xml` | `TaskMapper.xml` |
| **カスタム** | `{Feature}Mapper.xml` | `TaskCustomMapper.xml` |

---

## XMLマッパー基本構造

### ファイルヘッダー

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
    "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.example.taskmanager.mapper.TaskMapper">

    <!-- クエリ定義 -->

</mapper>
```

### namespace命名規則

- **完全修飾クラス名**を使用
- Mapperインターフェースのパッケージと一致させる

```xml
<!-- ✅ 良い例 -->
<mapper namespace="com.example.taskmanager.mapper.TaskMapper">

<!-- ❌ 悪い例 -->
<mapper namespace="TaskMapper">
```

---

## ResultMap定義

### 基本的なResultMap

```xml
<!-- ✅ 良い例: 明確な命名と適切なマッピング -->
<resultMap id="taskResultMap" type="com.example.taskmanager.entity.Task">
    <!-- 主キー -->
    <id property="id" column="id"/>

    <!-- 基本フィールド -->
    <result property="title" column="title"/>
    <result property="description" column="description"/>
    <result property="status" column="status"/>
    <result property="priority" column="priority"/>

    <!-- 日付型 -->
    <result property="dueDate" column="due_date"/>
    <result property="createdAt" column="created_at"/>
    <result property="updatedAt" column="updated_at"/>
</resultMap>
```

### Association（1対1の関連）

```xml
<resultMap id="taskDetailResultMap" type="com.example.taskmanager.entity.Task">
    <id property="id" column="id"/>
    <result property="title" column="title"/>

    <!-- 関連エンティティ（1対1） -->
    <association property="category" javaType="com.example.taskmanager.entity.Category">
        <id property="id" column="category_id"/>
        <result property="categoryName" column="category_name"/>
    </association>
</resultMap>
```

### Collection（1対多の関連）

```xml
<resultMap id="taskWithCommentsResultMap" type="com.example.taskmanager.entity.Task">
    <id property="id" column="id"/>
    <result property="title" column="title"/>

    <!-- 1対多の関連 -->
    <collection property="comments"
                ofType="com.example.taskmanager.entity.Comment">
        <id property="id" column="comment_id"/>
        <result property="content" column="comment_content"/>
        <result property="createdAt" column="comment_created_at"/>
    </collection>
</resultMap>
```

### ResultMap命名規則

| パターン | 例 |
|:---|:---|
| 基本 | `taskResultMap` |
| 一覧用 | `taskListResultMap` |
| 詳細用 | `taskDetailResultMap` |

---

## SQL記述規約

### SELECT文

```xml
<!-- ✅ 良い例: 構造化されたフォーマット -->
<select id="selectAll" resultMap="taskResultMap">
    SELECT
        id,
        title,
        description,
        status,
        priority,
        due_date,
        created_at,
        updated_at
    FROM tasks
    ORDER BY created_at DESC
</select>

<!-- ❌ 悪い例: 1行で記述 -->
<select id="selectAll" resultMap="taskResultMap">
    SELECT id,title,description,status,priority,due_date,created_at,updated_at FROM tasks ORDER BY created_at DESC
</select>
```

### INSERT文

```xml
<!-- ✅ 良い例: useGeneratedKeysでID取得 -->
<insert id="insert" useGeneratedKeys="true" keyProperty="id">
    INSERT INTO tasks (
        title,
        description,
        status,
        priority,
        due_date,
        created_at,
        updated_at
    ) VALUES (
        #{title},
        #{description},
        #{status},
        #{priority},
        #{dueDate},
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    )
</insert>
```

### UPDATE文

```xml
<!-- ✅ 良い例: 更新日時を自動設定 -->
<update id="update">
    UPDATE tasks
    SET
        title = #{title},
        description = #{description},
        status = #{status},
        priority = #{priority},
        due_date = #{dueDate},
        updated_at = CURRENT_TIMESTAMP
    WHERE id = #{id}
</update>
```

### DELETE文

```xml
<!-- ✅ 良い例: 主キーで削除 -->
<delete id="deleteById">
    DELETE FROM tasks
    WHERE id = #{id}
</delete>
```

---

## データ操作（INSERT/UPDATE/DELETE）

### 基本原則

**重要**: INSERT、UPDATE、DELETEの基本操作は、**MyBatis Generator自動生成メソッド**を使用してください。XMLファイルに手動でSQL文を記述する必要はありません。

### MyBatis自動生成メソッド

MyBatis Generatorは以下のメソッドを自動生成します：

#### INSERT操作

```java
// ✅ 良い例: 自動生成メソッドを使用
@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskMapper taskMapper;

    @Transactional
    public TaskDto createTask(TaskForm form) {
        // エンティティ作成
        Task task = new Task();
        task.setTitle(form.getTitle());
        task.setDescription(form.getDescription());
        task.setStatus(TaskStatus.TODO);
        task.setPriority(form.getPriority());
        task.setDueDate(form.getDueDate());
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());

        // 自動生成メソッドでINSERT
        taskMapper.insert(task);

        return taskConverter.toDto(task);
    }
}

// ❌ 悪い例: XMLに手動でINSERT文を記述
// TaskMapper.xmlにinsertTaskメソッドを手書きで定義する必要なし
```

#### UPDATE操作

```java
// ✅ 良い例: 全フィールド更新
@Transactional
public TaskDto updateTask(Long taskId, TaskForm form) {
    // 既存データ取得
    Task task = taskMapper.selectByPrimaryKey(taskId);
    if (task == null) {
        throw new TaskNotFoundException(taskId);
    }

    // 更新
    task.setTitle(form.getTitle());
    task.setDescription(form.getDescription());
    task.setStatus(form.getStatus());
    task.setPriority(form.getPriority());
    task.setDueDate(form.getDueDate());
    task.setUpdatedAt(LocalDateTime.now());

    // 自動生成メソッドで全フィールド更新
    taskMapper.updateByPrimaryKey(task);

    return taskConverter.toDto(task);
}

// ✅ 良い例: NULL以外のフィールドのみ更新
@Transactional
public TaskDto updateTaskPartial(Long taskId, TaskForm form) {
    // 更新対象のフィールドのみセット
    Task task = new Task();
    task.setId(taskId);

    if (form.getTitle() != null) {
        task.setTitle(form.getTitle());
    }
    if (form.getDescription() != null) {
        task.setDescription(form.getDescription());
    }

    task.setUpdatedAt(LocalDateTime.now());

    // NULL以外のフィールドのみ更新
    taskMapper.updateByPrimaryKeySelective(task);

    return taskConverter.toDto(task);
}

// ❌ 悪い例: XMLに手動でUPDATE文を記述
// TaskMapper.xmlにupdateTaskメソッドを手書きで定義する必要なし
```

#### DELETE操作

```java
// ✅ 良い例: 論理削除（UPDATE使用）
@Transactional
public void deleteTask(Long taskId) {
    Task task = taskMapper.selectByPrimaryKey(taskId);
    if (task == null) {
        throw new TaskNotFoundException(taskId);
    }

    // 論理削除フラグ設定
    task.setDeleted(true);
    task.setDeletedAt(LocalDateTime.now());
    task.setUpdatedAt(LocalDateTime.now());

    // 自動生成メソッドで更新
    taskMapper.updateByPrimaryKeySelective(task);
}

// ⚠️ 物理削除が必要な場合のみ使用
@Transactional
public void physicalDeleteTask(Long taskId) {
    // 自動生成メソッドで物理削除
    taskMapper.deleteByPrimaryKey(taskId);
}
```

### 自動生成メソッド一覧

| メソッド名 | 用途 | 備考 |
|-----------|------|------|
| `insert(Entity)` | 全フィールドINSERT | NULLも含めて全て挿入 |
| `insertSelective(Entity)` | NULL以外INSERT | NULLフィールドは挿入しない |
| `selectByPrimaryKey(Long)` | 主キーでSELECT | 1件取得 |
| `updateByPrimaryKey(Entity)` | 全フィールドUPDATE | 主キー指定、全フィールド更新 |
| `updateByPrimaryKeySelective(Entity)` | NULL以外UPDATE | 主キー指定、NULL以外のみ更新 |
| `deleteByPrimaryKey(Long)` | 物理DELETE | 主キー指定で削除 |

### 一括操作が必要な場合

一括INSERT、一括UPDATEなどの複雑な操作が必要な場合のみ、カスタムマッパーXMLに記述します。

```xml
<!-- カスタムマッパー: 一括INSERT -->
<insert id="batchInsertTasks">
    INSERT INTO tasks (
        title,
        description,
        status,
        priority,
        due_date,
        created_at,
        updated_at
    ) VALUES
    <foreach collection="tasks" item="task" separator=",">
        (
            #{task.title},
            #{task.description},
            #{task.status},
            #{task.priority},
            #{task.dueDate},
            CURRENT_TIMESTAMP,
            CURRENT_TIMESTAMP
        )
    </foreach>
</insert>
```

---

## 命名規則

### SQL文のID

| 操作 | 命名パターン | 例 |
|:---|:---|:---|
| 全件取得 | `selectAll` | `selectAll` |
| 主キー取得 | `selectById` | `selectById` |
| 条件検索 | `selectBy{条件}` | `selectByCondition` |
| 件数取得 | `count` | `count` |
| 登録 | `insert` | `insert` |
| 更新 | `update` | `update` |
| 削除 | `deleteById` | `deleteById` |

```xml
<!-- ✅ 良い例 -->
<select id="selectAll">...</select>
<select id="selectById">...</select>
<select id="selectByCondition">...</select>
<insert id="insert">...</insert>
<update id="update">...</update>
<delete id="deleteById">...</delete>

<!-- ❌ 悪い例 -->
<select id="getList">...</select>
<select id="query1">...</select>
<select id="findAll">...</select>
```

### テーブルエイリアス

```xml
<!-- ✅ 良い例: 意味のある略称（複数テーブル時） -->
SELECT
    t.id,
    t.title,
    c.category_name
FROM tasks t
LEFT JOIN categories c ON t.category_id = c.id

<!-- ❌ 悪い例: 無意味な略称 -->
SELECT
    t1.id,
    t1.title,
    t2.category_name
FROM tasks t1
LEFT JOIN categories t2 ON t1.category_id = t2.id
```

---

## キーワードの大文字化

```xml
<!-- ✅ 良い例: SQLキーワードは大文字 -->
SELECT id, title, status
FROM tasks
WHERE status = #{status}
ORDER BY created_at DESC

<!-- ❌ 悪い例: 小文字 -->
select id, title, status
from tasks
where status = #{status}
order by created_at desc
```

---

## パラメータバインディング

### 基本的なバインディング

```xml
<!-- ✅ 良い例: #{} 使用（プリペアドステートメント） -->
<select id="selectById" resultMap="taskResultMap">
    SELECT *
    FROM tasks
    WHERE id = #{id}
</select>

<!-- ❌ 悪い例: ${} 使用（SQLインジェクションの危険） -->
<select id="selectById" resultMap="taskResultMap">
    SELECT *
    FROM tasks
    WHERE id = ${id}
</select>
```

### ${} の使用が許可されるケース

**ORDER BY句の動的カラム指定**（要バリデーション）

```xml
<!-- ⚠️ 注意: ${}使用（要バリデーション） -->
<select id="selectAllSorted" resultMap="taskResultMap">
    SELECT *
    FROM tasks
    ORDER BY ${sortColumn} ${sortDirection}
</select>
```

**Java側でバリデーション必須:**
```java
private static final Set<String> ALLOWED_SORT_COLUMNS =
    Set.of("id", "title", "created_at", "due_date");

public List<Task> findAllSorted(String sortColumn, String sortDirection) {
    if (!ALLOWED_SORT_COLUMNS.contains(sortColumn)) {
        throw new IllegalArgumentException("Invalid sort column");
    }
    if (!"ASC".equals(sortDirection) && !"DESC".equals(sortDirection)) {
        throw new IllegalArgumentException("Invalid sort direction");
    }
    return taskMapper.selectAllSorted(sortColumn, sortDirection);
}
```

### パラメータ型の明示

```xml
<!-- ✅ 型を明示 -->
<select id="selectByDueDate">
    SELECT *
    FROM tasks
    WHERE due_date = #{dueDate, jdbcType=DATE}
</select>

<!-- NULLを許可する場合 -->
<insert id="insert">
    INSERT INTO tasks (title, description, due_date)
    VALUES (
        #{title, jdbcType=VARCHAR},
        #{description, jdbcType=VARCHAR, javaType=String},
        #{dueDate, jdbcType=DATE}
    )
</insert>
```

---

## 動的SQL

### where タグ

```xml
<!-- ✅ 良い例: <where>タグで自動的にANDを調整 -->
<select id="selectByCondition" resultMap="taskResultMap">
    SELECT
        id, title, description, status, priority, due_date, created_at, updated_at
    FROM tasks
    <where>
        <if test="status != null and status != ''">
            AND status = #{status}
        </if>
        <if test="priority != null and priority != ''">
            AND priority = #{priority}
        </if>
    </where>
    ORDER BY created_at DESC
</select>
```

### if タグ

```xml
<!-- ✅ 良い例: 条件付きWHERE句 -->
<select id="searchTasks" resultMap="taskResultMap">
    SELECT *
    FROM tasks
    <where>
        <if test="title != null and title != ''">
            AND title LIKE CONCAT('%', #{title}, '%')
        </if>
        <if test="status != null">
            AND status = #{status}
        </if>
        <if test="priority != null">
            AND priority = #{priority}
        </if>
    </where>
</select>
```

### choose/when/otherwise タグ

```xml
<!-- ✅ 良い例: 条件分岐 -->
<select id="selectByStatus" resultMap="taskResultMap">
    SELECT *
    FROM tasks
    <where>
        <choose>
            <when test="status == 'TODO'">
                AND status = 'TODO'
            </when>
            <when test="status == 'IN_PROGRESS'">
                AND status = 'IN_PROGRESS'
            </when>
            <when test="status == 'DONE'">
                AND status = 'DONE'
            </when>
            <otherwise>
                <!-- 全件取得 -->
            </otherwise>
        </choose>
    </where>
</select>
```

### foreach タグ

```xml
<!-- ✅ 良い例: IN句 -->
<select id="selectByIds" resultMap="taskResultMap">
    SELECT *
    FROM tasks
    WHERE id IN
    <foreach collection="ids" item="id" open="(" separator="," close=")">
        #{id}
    </foreach>
</select>

<!-- ✅ 良い例: 一括INSERT -->
<insert id="batchInsert">
    INSERT INTO tasks (title, description, status, priority, due_date, created_at, updated_at)
    VALUES
    <foreach collection="tasks" item="task" separator=",">
        (
            #{task.title},
            #{task.description},
            #{task.status},
            #{task.priority},
            #{task.dueDate},
            CURRENT_TIMESTAMP,
            CURRENT_TIMESTAMP
        )
    </foreach>
</insert>
```

### set タグ

```xml
<!-- ✅ 良い例: 動的UPDATE -->
<update id="updateSelective">
    UPDATE tasks
    <set>
        <if test="title != null">
            title = #{title},
        </if>
        <if test="description != null">
            description = #{description},
        </if>
        <if test="status != null">
            status = #{status},
        </if>
        <if test="priority != null">
            priority = #{priority},
        </if>
        <if test="dueDate != null">
            due_date = #{dueDate},
        </if>
        updated_at = CURRENT_TIMESTAMP
    </set>
    WHERE id = #{id}
</update>
```

---

## JOIN

### テーブル結合

```xml
<!-- ✅ 良い例: 明示的なJOIN -->
<select id="selectWithCategory" resultMap="taskDetailResultMap">
    SELECT
        t.id,
        t.title,
        t.description,
        c.id AS category_id,
        c.category_name
    FROM tasks t
    LEFT JOIN categories c ON t.category_id = c.id
    WHERE t.id = #{id}
</select>

<!-- ❌ 悪い例: カンマ区切りのJOIN -->
<select id="selectWithCategory">
    SELECT *
    FROM tasks t, categories c
    WHERE t.category_id = c.id
</select>
```

---

## サブクエリ

### スカラーサブクエリ

```xml
<!-- ✅ 良い例: サブクエリで集計値を取得 -->
<select id="selectTasksWithStats" resultMap="taskResultMap">
    SELECT
        t.id,
        t.title,

        -- サブクエリ: 同一ステータスのタスク数
        (SELECT COUNT(*)
         FROM tasks
         WHERE status = t.status
        ) AS same_status_count

    FROM tasks t
    WHERE t.id = #{id}
</select>
```

### EXISTS句

```xml
<!-- ✅ 良い例: EXISTS使用 -->
<select id="selectTasksWithComments" resultMap="taskResultMap">
    SELECT *
    FROM tasks t
    WHERE EXISTS (
        SELECT 1
        FROM comments c
        WHERE c.task_id = t.id
    )
</select>

<!-- ✅ 良い例: NOT EXISTS使用 -->
<select id="selectTasksWithoutComments" resultMap="taskResultMap">
    SELECT *
    FROM tasks t
    WHERE NOT EXISTS (
        SELECT 1
        FROM comments c
        WHERE c.task_id = t.id
    )
</select>
```

---

## パフォーマンス考慮

### インデックスを意識したクエリ

```xml
<!-- ✅ 良い例: インデックスカラムを使用 -->
<select id="selectByStatus">
    SELECT *
    FROM tasks
    WHERE status = #{status}  -- statusにインデックスあり
</select>

<!-- ❌ 悪い例: 関数適用でインデックス無効化 -->
<select id="selectByTitle">
    SELECT *
    FROM tasks
    WHERE LOWER(title) = LOWER(#{title})  -- インデックスが使えない
</select>
```

### SELECT * の回避

```xml
<!-- ✅ 良い例: 必要なカラムのみ取得 -->
<select id="selectTitles" resultType="string">
    SELECT title
    FROM tasks
    WHERE status = 'TODO'
</select>

<!-- ❌ 悪い例: 全カラム取得 -->
<select id="selectTitles" resultMap="taskResultMap">
    SELECT *
    FROM tasks
    WHERE status = 'TODO'
</select>
```

### LIMIT/OFFSET（ページネーション）

```xml
<!-- ✅ 良い例: ページネーション -->
<select id="selectPaged" resultMap="taskResultMap">
    SELECT *
    FROM tasks
    WHERE status = #{status}
    ORDER BY created_at DESC
    LIMIT #{limit} OFFSET #{offset}
</select>
```

---

## コメント

### SQLコメント

```xml
<select id="selectWithStats" resultMap="taskResultMap">
    SELECT
        t.id,
        t.title,

        -- サブクエリ: 完了タスク数
        (SELECT COUNT(*)
         FROM tasks
         WHERE status = 'DONE'
        ) AS done_count

    FROM tasks t
    WHERE t.status = #{status}
</select>
```

### XMLコメント

```xml
<!--
  タスク一覧取得（条件検索）

  パラメータ:
  - status: ステータス（TODO/IN_PROGRESS/DONE）
  - priority: 優先度（LOW/MEDIUM/HIGH）
  - limit: 取得件数
  - offset: オフセット
-->
<select id="selectByCondition" resultMap="taskResultMap">
    ...
</select>
```

---

## セキュリティ

### SQLインジェクション対策

```xml
<!-- ✅ 良い例: パラメータバインディング -->
<select id="selectByTitle">
    SELECT *
    FROM tasks
    WHERE title = #{title}
</select>

<!-- ❌ 悪い例: 文字列連結 -->
<select id="selectByTitle">
    SELECT *
    FROM tasks
    WHERE title = '${title}'  -- SQLインジェクションの危険
</select>
```

### LIKE句

```xml
<!-- ✅ 良い例: CONCAT使用 -->
<select id="searchByTitle">
    SELECT *
    FROM tasks
    WHERE title LIKE CONCAT('%', #{keyword}, '%')
</select>

<!-- H2データベースの場合 -->
<select id="searchByTitle">
    SELECT *
    FROM tasks
    WHERE title LIKE '%' || #{keyword} || '%'
</select>
```

---

## DDL（schema.sql）

### テーブル定義

```sql
-- ✅ 良い例: コメント付き、適切な制約
-- タスク管理テーブル
CREATE TABLE IF NOT EXISTS tasks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'TODO',
    priority VARCHAR(10) NOT NULL DEFAULT 'MEDIUM',
    due_date DATE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- インデックス作成
CREATE INDEX IF NOT EXISTS idx_tasks_status ON tasks(status);
CREATE INDEX IF NOT EXISTS idx_tasks_due_date ON tasks(due_date);
```

### 命名規則

| 対象 | 規則 | 例 |
|:---|:---|:---|
| テーブル名 | 複数形、snake_case | `tasks`, `task_categories` |
| カラム名 | snake_case | `due_date`, `created_at` |
| 主キー | `id` | `id` |
| 外部キー | `{テーブル}_id` | `category_id` |
| インデックス | `idx_{テーブル}_{カラム}` | `idx_tasks_status` |

---

## 初期データ（data.sql）

```sql
-- ✅ 良い例: 明確なINSERT文
INSERT INTO tasks (title, description, status, priority, due_date) VALUES
('サンプルタスク1', 'タスクの説明文です', 'TODO', 'HIGH', '2025-12-31'),
('サンプルタスク2', 'もう一つのタスク', 'IN_PROGRESS', 'MEDIUM', '2025-12-15'),
('完了タスク', '完了済みのタスク', 'DONE', 'LOW', NULL);
```

---

## 自動生成ファイルの扱い

### MyBatis Generator自動生成

```xml
<!--
  WARNING: 自動生成ファイルは直接編集禁止

  以下のファイルはMyBatis Generatorで自動生成されます:
  - src/main/resources/mapper/common/{Entity}Mapper.xml
  - src/main/java/com/example/taskmanager/entity/{Entity}.java
  - src/main/java/com/example/taskmanager/mapper/{Entity}Mapper.java

  カスタムクエリが必要な場合:
  - src/main/resources/mapper/custom/ 配下に別ファイルを作成
-->
```

### カスタムマッパーの作成

```xml
<!--
  カスタムマッパー: TaskCustomMapper.xml
  src/main/resources/mapper/custom/TaskCustomMapper.xml
-->
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
    "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.example.taskmanager.mapper.custom.TaskCustomMapper">

    <!-- カスタムクエリ -->
    <select id="selectTasksWithDetails" resultMap="taskDetailResultMap">
        ...
    </select>

</mapper>
```

---

## チェックリスト

### データ操作時の確認項目

- [ ] **INSERT/UPDATE/DELETEは自動生成メソッドを使用**（XMLに手書きしない）
- [ ] 単一レコード挿入は`insert()`または`insertSelective()`を使用
- [ ] 単一レコード更新は`updateByPrimaryKey()`または`updateByPrimaryKeySelective()`を使用
- [ ] 削除は`deleteByPrimaryKey()`を使用（論理削除は`updateByPrimaryKeySelective()`で対応）
- [ ] 一括操作が必要な場合のみカスタムXMLに記述

### MyBatis XMLマッパー作成時

- [ ] namespaceが正しいMapperインターフェースを指定
- [ ] ResultMapのtype属性が完全修飾クラス名
- [ ] パラメータバインディングに`#{}`を使用（`${}`は原則禁止）
- [ ] SQLキーワードを大文字で記述
- [ ] テーブルエイリアスが意味のある略称
- [ ] WHERE句で動的条件を`<where>`タグで囲む
- [ ] 日付型にjdbcTypeを指定
- [ ] SELECT *を避け必要なカラムのみ取得
- [ ] INDEXを意識したクエリ
- [ ] 複雑なクエリにコメント記載

### DDL/データ作成時

- [ ] テーブル名・カラム名がsnake_case
- [ ] 適切な制約（NOT NULL、DEFAULT）
- [ ] 検索対象カラムにインデックス
- [ ] 外部キーの命名規則準拠

---

## 参考資料

- [MyBatis Documentation](https://mybatis.org/mybatis-3/)
- [MyBatis Generator Documentation](https://mybatis.org/generator/)
- [MyBatis Dynamic SQL](https://mybatis.org/mybatis-dynamic-sql/)
- [H2 Database Documentation](https://h2database.com/html/main.html)
- [SQL Style Guide](https://www.sqlstyle.guide/)
