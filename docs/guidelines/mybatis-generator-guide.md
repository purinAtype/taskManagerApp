# MyBatis Generator 使用ガイド

## 概要

MyBatis Generatorを使用して、H2データベースのテーブルからEntity、Mapper、XMLファイルを自動生成する手順を説明します。

---

## 前提条件

- Java 21
- Maven 3.x
- H2 Database

---

## ファイル構成

```
taskManagerApp/
├── src/main/resources/
│   └── generatorConfig.xml        # MyBatis Generator設定ファイル
├── scripts/
│   └── init-h2-for-generator.sql  # H2初期化スクリプト
└── data/                          # H2ファイルDB格納先（自動生成）
    └── taskdb.mv.db
```

---

## 実行手順

### 1. H2データベースの準備

MyBatis Generatorはファイルベースのデータベースが必要です。

#### 方法A: H2 Shell使用

```bash
# H2 Shellを起動
java -cp ~/.m2/repository/com/h2database/h2/2.2.224/h2-2.2.224.jar org.h2.tools.Shell

# 接続情報を入力
URL: jdbc:h2:file:./data/taskdb
User: sa
Password: (空Enter)

# SQLスクリプトを実行
sql> RUNSCRIPT FROM 'scripts/init-h2-for-generator.sql';
sql> exit
```

#### 方法B: H2コンソール使用

1. アプリケーション起動: `mvn spring-boot:run`
2. ブラウザで http://localhost:8080/h2-console にアクセス
3. 接続URL変更: `jdbc:h2:file:./data/taskdb`
4. `scripts/init-h2-for-generator.sql` のSQLを実行

### 2. MyBatis Generator実行

```bash
# プロジェクトルートで実行
mvn mybatis-generator:generate
```

### 3. 生成されるファイル

| 種類 | 出力先 | ファイル名 |
|:---|:---|:---|
| Entity | `src/main/java/.../common/entity/` | `TaskGenerated.java` |
| Mapper Interface | `src/main/java/.../common/mapper/` | `TaskGeneratedMapper.java` |
| Mapper XML | `src/main/resources/mapper/` | `TaskGeneratedMapper.xml` |

---

## 自動生成されるメソッド

### Mapperインターフェース

```java
public interface TaskGeneratedMapper {
    // INSERT
    int insert(TaskGenerated record);
    int insertSelective(TaskGenerated record);

    // SELECT
    TaskGenerated selectByPrimaryKey(Long id);

    // UPDATE
    int updateByPrimaryKey(TaskGenerated record);
    int updateByPrimaryKeySelective(TaskGenerated record);

    // DELETE
    int deleteByPrimaryKey(Long id);
}
```

### メソッド説明

| メソッド | 用途 |
|:---|:---|
| `insert` | 全フィールドをINSERT（NULLも含む） |
| `insertSelective` | NULL以外のフィールドのみINSERT |
| `selectByPrimaryKey` | 主キーで1件取得 |
| `updateByPrimaryKey` | 全フィールドをUPDATE |
| `updateByPrimaryKeySelective` | NULL以外のフィールドのみUPDATE |
| `deleteByPrimaryKey` | 主キーで物理削除 |

---

## 設定ファイル解説

### generatorConfig.xml

```xml
<!-- H2接続設定 -->
<jdbcConnection
    driverClass="org.h2.Driver"
    connectionURL="jdbc:h2:file:./data/taskdb;AUTO_SERVER=TRUE"
    userId="sa"
    password="">
</jdbcConnection>

<!-- Java 8日付型（LocalDate, LocalDateTime）を使用 -->
<javaTypeResolver>
    <property name="useJSR310Types" value="true"/>
</javaTypeResolver>

<!-- テーブル設定 -->
<table tableName="tasks" domainObjectName="TaskGenerated">
    <!-- AUTO_INCREMENT主キー設定 -->
    <generatedKey column="id" sqlStatement="JDBC" identity="true"/>
</table>
```

---

## 既存コードとの統合

### 推奨構成

```
src/main/java/com/example/taskmanager/
├── entity/
│   └── Task.java                    # 手動作成（既存・カスタム）
├── common/
│   ├── entity/                      # 自動生成Entity
│   │   └── TaskGenerated.java
│   └── mapper/                      # 自動生成Mapper
│       └── TaskGeneratedMapper.java
├── mapper/
│   └── TaskMapper.java              # カスタムMapper（手動作成）
```

```
src/main/resources/mapper/
├── TaskMapper.xml                   # カスタムMapper XML（手動作成）
└── TaskGeneratedMapper.xml          # 自動生成Mapper XML
```

### 自動生成コードの使用方法

```java
@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    // 自動生成Mapper
    private final TaskGeneratedMapper taskGeneratedMapper;

    // カスタムMapper
    private final TaskMapper taskMapper;

    @Override
    @Transactional
    public TaskDto create(TaskForm form) {
        TaskGenerated entity = new TaskGenerated();
        entity.setTitle(form.getTitle());
        entity.setDescription(form.getDescription());
        entity.setStatus("TODO");
        entity.setPriority(form.getPriority().name());
        entity.setDueDate(form.getDueDate());

        // 自動生成メソッドでINSERT
        taskGeneratedMapper.insertSelective(entity);

        return convertToDto(entity);
    }

    @Override
    @Transactional
    public TaskDto update(Long id, TaskForm form) {
        TaskGenerated entity = taskGeneratedMapper.selectByPrimaryKey(id);
        if (entity == null) {
            throw new TaskNotFoundException(id);
        }

        entity.setTitle(form.getTitle());
        entity.setDescription(form.getDescription());
        entity.setStatus(form.getStatus().name());

        // 自動生成メソッドでUPDATE
        taskGeneratedMapper.updateByPrimaryKeySelective(entity);

        return convertToDto(entity);
    }

    @Override
    public List<TaskDto> findByCondition(TaskSearchCondition condition) {
        // 複雑な検索はカスタムMapperを使用
        return taskMapper.selectByCondition(condition);
    }
}
```

---

## 注意事項

### 自動生成ファイルの編集禁止

```
WARNING: common/entity/ および common/mapper/ 内のファイルは直接編集しないでください。
再生成時に上書きされます。

カスタマイズが必要な場合:
- entity/ パッケージにカスタムEntityを作成
- mapper/ パッケージにカスタムMapperを作成
```

### テーブル追加時

1. `scripts/init-h2-for-generator.sql` にCREATE TABLE追加
2. `generatorConfig.xml` に`<table>`要素追加
3. `mvn mybatis-generator:generate` 実行

### 再生成時の動作

- `overwrite=true` 設定により既存ファイルは上書き
- カスタムコードは別ファイルに分離して保護

---

## トラブルシューティング

### エラー: テーブルが見つからない

```
原因: H2データベースにテーブルが作成されていない
対処: init-h2-for-generator.sql を実行してテーブルを作成
```

### エラー: 接続できない

```
原因: H2ファイルDBが他プロセスでロックされている
対処: AUTO_SERVER=TRUE を接続URLに追加、または他プロセスを停止
```

### 生成されたコードがコンパイルエラー

```
原因: Java型マッピングの問題
対処: generatorConfig.xmlの<columnOverride>で型を明示指定
```

---

## 参考資料

- [MyBatis Generator公式ドキュメント](https://mybatis.org/generator/)
- [H2 Database公式ドキュメント](https://h2database.com/html/main.html)
