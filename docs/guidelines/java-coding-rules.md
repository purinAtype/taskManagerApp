# Javaコーディングルール

## 概要

タスク管理アプリケーション（task-manager-app）のJavaコーディング規約を定義します。

---

## 基本方針

1. **可読性**: コードは書くよりも読まれる時間が長い
2. **一貫性**: プロジェクト全体で統一されたスタイル
3. **保守性**: 将来の変更に強いコード
4. **簡潔性**: 複雑さを避ける（KISS原則）

---

## 命名規則

### クラス名

- **PascalCase**（各単語の頭文字を大文字）
- 名詞または名詞句
- 具体的で意味のある名前

```java
// ✅ 良い例
public class TaskService { }
public class TaskController { }
public class TaskMapper { }

// ❌ 悪い例
public class taskservice { }        // 小文字
public class TS { }                  // 略称
public class Manager { }             // 不明確
```

### インターフェース名

- 実装を示す接尾辞を避ける（`Impl`は実装クラスのみ）
- 能力を示す場合は`-able`接尾辞

```java
// ✅ 良い例
public interface TaskService { }
public interface Serializable { }

// ❌ 悪い例
public interface ITaskService { }    // I接頭辞は不要
```

### メソッド名

- **camelCase**
- 動詞または動詞句
- 意図を明確に表現

```java
// ✅ 良い例
public TaskDto findById(Long id) { }
public List<TaskDto> findAll() { }
public boolean isExpired() { }
public void updateTask() { }

// ❌ 悪い例
public TaskDto task(Long id) { }     // 動詞が不明確
public List<TaskDto> tasks() { }     // 動詞なし
public boolean expired() { }         // is抜け
```

### 変数名

- **camelCase**
- 意味のある名前
- 略称を避ける

```java
// ✅ 良い例
private Long taskId;
private String title;
private LocalDateTime createdAt;
private List<TaskDto> tasks;

// ❌ 悪い例
private Long id;           // 何のID?
private String t;          // 1文字
private LocalDateTime dt;  // 略称
```

### 定数名

- **UPPER_SNAKE_CASE**（全て大文字、アンダースコア区切り）
- `static final`で定義

```java
// ✅ 良い例
public static final int MAX_TITLE_LENGTH = 100;
public static final String DEFAULT_STATUS = "TODO";

// ❌ 悪い例
public static final int maxTitleLength = 100;
```

### パッケージ名

- **全て小文字**
- ドット区切り

```java
// ✅ 良い例
package com.example.taskmanager.service;
package com.example.taskmanager.controller;

// ❌ 悪い例
package com.example.taskmanager.Service;  // 大文字
```

---

## フォーマット

### インデント

- **スペース4つ**
- タブは使用しない

```java
public class Example {
    public void method() {
        if (condition) {
            doSomething();
        }
    }
}
```

### 括弧の位置

- **K&Rスタイル**（開き括弧は同じ行）

```java
// ✅ 良い例
public class Example {
    public void method() {
        if (condition) {
            doSomething();
        }
    }
}

// ❌ 悪い例（Allmanスタイル）
public class Example
{
    public void method()
    {
    }
}
```

### 行の長さ

- **最大120文字**
- 超える場合は改行

```java
// ✅ 良い例
public ResponseEntity<TaskDto> updateTask(
        @PathVariable Long id,
        @Valid @ModelAttribute TaskForm form) {
    TaskDto result = taskService.update(id, form);
    return ResponseEntity.ok(result);
}
```

### 空行

- メソッド間に1行
- 論理的なブロック間に1行

```java
public class TaskService {

    private final TaskMapper taskMapper;

    public TaskDto findById(Long id) {
        Task task = taskMapper.selectById(id);

        if (task == null) {
            throw new TaskNotFoundException(id);
        }

        return convertToDto(task);
    }

    public List<TaskDto> findAll() {
        List<Task> tasks = taskMapper.selectAll();
        return tasks.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
}
```

---

## Lombok使用規約

### 推奨アノテーション

```java
// ✅ Entity/DTO
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Task {
    private Long id;
    private String title;
}

// ✅ Service（コンストラクタインジェクション）
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {
    private final TaskMapper taskMapper;
    private final TaskConverter taskConverter;
}

// ✅ Logging
@Slf4j
public class TaskService {
    public void method() {
        log.info("Processing task: {}", taskId);
        log.debug("Task details: {}", task);
        log.error("Task not found: {}", taskId);
    }
}
```

### アノテーション使用基準

| クラス種別 | アノテーション |
|:---|:---|
| Entity | `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor` |
| DTO | `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor` |
| Form | `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor` |
| Service | `@Slf4j`, `@Service`, `@RequiredArgsConstructor` |
| Controller | `@Slf4j`, `@Controller`, `@RequiredArgsConstructor` |

---

## コメント

### JavaDoc

- **public** メソッドには必須（Service層）
- パラメータと戻り値を記載

```java
/**
 * タスクIDからタスク情報を取得します
 *
 * @param id タスクID
 * @return タスク情報DTO
 * @throws TaskNotFoundException タスクが見つからない場合
 */
public TaskDto findById(Long id) {
    // 実装
}
```

### インラインコメント

- **なぜ**を説明（何をしているかはコードで表現）
- 複雑なロジックのみコメント

```java
// ✅ 良い例
// H2データベースはAUTO_INCREMENTでID生成されるため、
// insertで返されたEntityにIDが設定されている
Task task = taskMapper.insert(newTask);

// ❌ 悪い例
// タスクを取得
Task task = taskMapper.selectById(id);  // 自明
```

---

## 例外ハンドリング

### カスタム例外

```java
// ✅ ビジネス例外を定義
public class TaskNotFoundException extends RuntimeException {
    public TaskNotFoundException(Long id) {
        super("タスクが見つかりません: ID=" + id);
    }
}
```

### 例外処理パターン

```java
// ✅ Service層で例外をスロー
public TaskDto findById(Long id) {
    Task task = taskMapper.selectById(id);

    if (task == null) {
        throw new TaskNotFoundException(id);
    }

    return taskConverter.toDto(task);
}

// ✅ GlobalExceptionHandlerで一元管理
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TaskNotFoundException.class)
    public String handleTaskNotFound(TaskNotFoundException ex, Model model) {
        log.warn("Task not found: {}", ex.getMessage());
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/404";
    }
}
```

---

## null安全

### 早期リターン

```java
// ✅ 早期リターンでnullチェック
public TaskDto findById(Long id) {
    if (id == null) {
        throw new IllegalArgumentException("ID must not be null");
    }

    Task task = taskMapper.selectById(id);

    if (task == null) {
        throw new TaskNotFoundException(id);
    }

    return taskConverter.toDto(task);
}
```

### Optional使用（必要な場合のみ）

```java
// ✅ Optional返却
public Optional<Task> findTaskById(Long id) {
    Task task = taskMapper.selectById(id);
    return Optional.ofNullable(task);
}

// 呼び出し側
Task task = findTaskById(id)
    .orElseThrow(() -> new TaskNotFoundException(id));
```

---

## Stream API

### 適切な使用

```java
// ✅ シンプルな変換
List<TaskDto> taskDtos = tasks.stream()
    .map(taskConverter::toDto)
    .collect(Collectors.toList());

// ✅ フィルタリング
List<TaskDto> todoTasks = tasks.stream()
    .filter(task -> task.getStatus() == TaskStatus.TODO)
    .map(taskConverter::toDto)
    .collect(Collectors.toList());

// ✅ 複雑な場合は従来のforループ
List<TaskDto> result = new ArrayList<>();
for (Task task : tasks) {
    if (isValidTask(task)) {
        TaskDto dto = taskConverter.toDto(task);
        enrichDto(dto);
        result.add(dto);
    }
}
```

---

## バリデーション

### Bean Validationアノテーション

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskForm {

    @NotBlank(message = "タイトルを入力してください")
    @Size(max = 100, message = "タイトルは100文字以内で入力してください")
    private String title;

    @Size(max = 1000, message = "説明は1000文字以内で入力してください")
    private String description;

    @NotNull(message = "ステータスを選択してください")
    private TaskStatus status;

    @NotNull(message = "優先度を選択してください")
    private TaskPriority priority;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dueDate;
}
```

### Controller側のバリデーション

```java
@PostMapping
public String create(@Valid @ModelAttribute TaskForm form,
                     BindingResult bindingResult,
                     Model model) {
    if (bindingResult.hasErrors()) {
        // バリデーションエラー時の処理
        return "task/form";
    }

    taskService.create(form);
    return "redirect:/tasks";
}
```

---

## DTO変換規約

### MapStruct使用

```java
@Mapper(componentModel = "spring")
public interface TaskConverter {

    TaskDto toDto(Task entity);

    List<TaskDto> toDtoList(List<Task> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Task toEntity(TaskForm form);

    TaskForm toForm(TaskDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(TaskForm form, @MappingTarget Task entity);
}
```

---

## トランザクション管理

### @Transactionalの使用

```java
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional  // クラスレベルでデフォルト設定
public class TaskServiceImpl implements TaskService {

    private final TaskMapper taskMapper;
    private final TaskConverter taskConverter;

    @Override
    @Transactional(readOnly = true)  // 参照系はreadOnly
    public List<TaskDto> findAll() {
        List<Task> tasks = taskMapper.selectAll();
        return taskConverter.toDtoList(tasks);
    }

    @Override
    // 更新系はデフォルト（readOnly = false）
    public TaskDto create(TaskForm form) {
        Task task = taskConverter.toEntity(form);
        taskMapper.insert(task);
        return taskConverter.toDto(task);
    }
}
```

---

## セキュリティ

### SQLインジェクション対策

```java
// ✅ MyBatis パラメータバインディング（#{}）
<select id="selectById" resultMap="taskResultMap">
    SELECT * FROM tasks WHERE id = #{id}
</select>

// ❌ 文字列結合（禁止）
<select id="selectById">
    SELECT * FROM tasks WHERE id = ${id}
</select>
```

### XSS対策

```java
// ✅ Thymeleafのth:text使用（自動エスケープ）
<td th:text="${task.title}"></td>

// ❌ th:utext（エスケープなし）は避ける
<td th:utext="${task.title}"></td>
```

---

## チェックリスト

コードレビュー時の確認項目：

### 基本項目
- [ ] 命名規則に準拠
- [ ] 適切なインデント・フォーマット
- [ ] publicメソッドにJavaDoc記載
- [ ] 例外ハンドリング実装
- [ ] null安全性確保
- [ ] Lombok適切に使用
- [ ] ログ出力適切

### バリデーション
- [ ] FormクラスにBean Validationアノテーション使用
- [ ] Controllerで@Validアノテーション使用
- [ ] 適切なエラーメッセージを定義

### DTO変換
- [ ] MapStructを使用
- [ ] 自動生成フィールド（id, createdAt等）はignore

### トランザクション
- [ ] Service層に@Transactional
- [ ] 参照系はreadOnly = true

### セキュリティ
- [ ] SQLインジェクション対策（#{}使用）
- [ ] XSS対策（th:text使用）

---

## 参考資料

- [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- [Spring Boot Best Practices](https://spring.io/guides)
- [Lombok Documentation](https://projectlombok.org/)
- [MapStruct Documentation](https://mapstruct.org/)
