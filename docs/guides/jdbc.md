# JDBC 工具类使用说明

## 1. 模块概述

`com.yishuifengxiao.common.tool.jdbc` 提供基于 Spring JDBC 的轻量数据访问工具，核心能力：

- **JdbcHelper**：以实体对象为参数生成 SQL，完成单表增删改查、条件查询、分页、批量写入和动态 SQL 执行；自动识别主键、表名、列名，并在初始化时探测数据库时区。
- **JdbcUrlHelper**：MySQL JDBC URL 的解析、重建、克隆与常见连接参数配置（字符集、SSL、时区、批量重写等）。
- **ZoneIdDetector**：从数据库连接中探测服务端时区。
- **SimpleRowMapper**：`RowMapper` 实现，按列名/列标签自动映射到实体字段，支持日期时间类型与时区转换。
- **FieldExtractor / FieldValue**：实体字段元数据提取（表名、列名、主键、SQL 类型）。
- **PojoGenerator**：读取数据库元数据，生成带 Lombok / JPA / OpenAPI 3 注解的实体类源码。

**适用场景**：中小项目的单表 CRUD、快速查询、代码生成、连接串拼装。

**不适用场景**：多表关联映射、复杂 ORM 关系（一对多/多对多）、多主键实体的更新、跨数据库方言适配、分布式事务。这些场景请使用 JPA/MyBatis 等方案。

## 2. 引入方式

本模块属于项目内部的公共工具包，直接使用源码包中的类：

```java
import com.yishuifengxiao.common.tool.jdbc.JdbcHelper;
import com.yishuifengxiao.common.tool.jdbc.JdbcUrlHelper;
import com.yishuifengxiao.common.tool.jdbc.SimpleRowMapper;
import com.yishuifengxiao.common.tool.jdbc.FieldExtractor;
import com.yishuifengxiao.common.tool.jdbc.FieldValue;
import com.yishuifengxiao.common.tool.jdbc.ZoneIdDetector;
import com.yishuifengxiao.common.tool.jdbc.PojoGenerator;
```

依赖要求：

- Spring JDBC（`JdbcTemplate`、`NamedParameterJdbcTemplate`、`RowMapper`）；
- `jakarta.persistence-api`（实体使用 `@Table`、`@Column`、`@Id`、`@Transient` 时需要）；
- Jackson、Lombok、Swagger 注解仅在 `PojoGenerator` 按配置生成时才会出现在产物中。

## 3. 核心约定

### 3.1 实体与表名

- 表名解析顺序：`@Table(name = "...")` → `@Entity(name = "...")` → 类名转下划线（`UserInfo` → `user_info`）。
- 列名解析顺序：`@Column(name = "...")` → 字段名转下划线（`createTime` → `create_time`）。
- 被 `static`、`final`、`transient` 修饰或标注 `@Transient` 的字段不参与 SQL。

### 3.2 主键识别

满足任一条件即视为主键字段：

1. 标注 `@Id`；
2. `@Column(name = "id")`；
3. 字段名（忽略大小写）为 `id`。

一个实体只使用**第一个**主键字段；复合主键不在支持范围内。

### 3.3 时区

`JdbcHelper` 初始化时通过 `ZoneIdDetector` 探测数据库时区，并在 `LocalDateTime` 等类型映射时使用；探测失败时回退到 JVM 默认时区。MySQL 连接字符串中的 `serverTimezone`（旧驱动）与 `connectionTimeZone`（新驱动）均已被识别。`ZoneIdDetector` 仅内置 MySQL / PostgreSQL / Oracle / SQL Server 的时区查询，**其他数据库（如 H2）按契约安全返回 `null`**，不会抛出异常。

## 4. API 说明

### 4.1 JdbcHelper

#### 构造与初始化

| 方法 | 说明 |
|---|---|
| `JdbcHelper()` | 空实例，需随后调用 `setJdbcTemplate(JdbcTemplate)` 才能执行 SQL |
| `JdbcHelper(JdbcTemplate)` | 使用默认批量大小（500）初始化 |
| `JdbcHelper(DataSource)` | 内部创建 `JdbcTemplate` |
| `JdbcHelper(JdbcTemplate, int batchSize)` | 指定批量大小 |

未初始化就执行 SQL 时抛出 `UncheckedException`，错误码为 `JdbcError.SQL_HELPER_INIT_ERROR(6007)`，消息中包含“SQL执行器未初始化”。

#### 单表 CRUD（实体驱动）

| 方法 | 说明 |
|---|---|
| `<T> T findByPrimaryKey(Class<T> clazz, Object primaryKey)` | 按主键查询，主键为 null 时返回 null |
| `<T> Long count(T t, boolean likeMode)` | 按实体非空字段统计数量；`likeMode` 为 true 时字符串字段用 `LIKE '%值%'` |
| `<T> T findOne(T t, boolean likeMode, Order... orders)` | 查询第一条，自动 `LIMIT 1` |
| `<T> List<T> find(T t, boolean likeMode, Order... orders)` | 条件查询 |
| `<T> Page<T> findPage(PageQuery<T> pageQuery, boolean likeMode, Order... orders)` | 实体条件 + 分页 |
| `<T> Page<T> findPage(T t, boolean likeMode, Slice slice, Order... orders)` | 实体条件 + 分页参数 |
| `<T> Result insert(T t)` | 插入；**值为 null 的主键字段不参与 SQL**，交由数据库自增 |
| `<T> Result updateByPrimaryKey(T t)` | 按主键全量更新（SET 子句**不含主键**） |
| `<T> Result updateByPrimaryKeySelective(T t)` | 按主键选择性更新（SET 子句不含主键、不含 null 字段） |
| `<T> Result deleteByPrimaryKey(Class<T> clazz, Object... primaryKeys)` | 按主键批量删除；null/空白主键会被过滤 |
| `<T> Result deleteByPrimaryKeys(Class<T> clazz, List<?> primaryKeys)` | 同上，集合入参 |
| `<T> Result saveOrUpdate(T t)` | 先按主键更新，影响行数为 0 时执行插入 |
| `<T> Result saveAll(List<T> list)` | 批量插入，按批量大小分批执行 |

更新/删除时若没有可写入的字段（例如实体只有主键），直接返回 `affectedRows = 0`，不执行 SQL。

#### 原生/动态 SQL

| 方法 | 说明 |
|---|---|
| `<T> T findOne(Class<T> clazz, String sql, SqlParameterSource params)` | 查询单条 |
| `<T> T findOne(Class<T> clazz, Function<MapSqlParameterSource, String> function)` | 在 Lambda 中构建 SQL 并填充参数 |
| `<T> List<T> find(Class<T> clazz, String sql, SqlParameterSource params)` | 查询列表 |
| `<T> List<T> find(Class<T> clazz, String sql, Map<String, Object> map)` | Map 参数版 |
| `<T> List<T> find(Class<T> clazz, Function<MapSqlParameterSource, String> function)` | Lambda 构建版 |
| `<T> Page<T> findPage(Class<T> clazz, Slice slice, String sql, SqlParameterSource params)` | 分页查询（自动生成 count 语句） |
| `<T> Page<T> findPage(Class<T> clazz, Slice slice, String sql, Map<String, Object> map)` | 分页查询 Map 参数版 |
| `<T> Page<T> findPage(Class<T> clazz, Slice slice, Function<MapSqlParameterSource, String> function)` | 分页查询 Lambda 版 |
| `<T> Result update(String sql, SqlParameterSource params)` | 执行更新类语句，返回 `Result` |
| `<T> Result update(Function<MapSqlParameterSource, String> function)` | Lambda 构建更新语句 |
| `<T> Result batchUpdate(String sql, SqlParameterSource[] params)` | 批量更新，按批量大小分批提交 |

> `clazz` 为基本类型（`Long`、`String`、`Integer` 等）时按标量结果返回；否则按实体映射返回。

#### 排序 Order

| 方法 | 说明 |
|---|---|
| `Order.asc(String orderName)` | 升序 |
| `Order.desc(String orderName)` | 降序 |
| `Order.of(String orderName, Direction direction)` | 指定方向 |
| `order.asc()` / `order.desc()` | 实例方法，可链式修改方向 |

排序字段会做标识符校验，仅允许 `字段名` 或 `表名.字段名`（字母、数字、下划线），非法值抛出 `JdbcError.INVALID_IDENTIFIER(6008)`。

#### Result

| 方法 | 说明 |
|---|---|
| `int getAffectedRows()` | 受影响行数 |
| `int[] getBatchAffectedRows()` | 批量执行时各批次影响行数 |
| `KeyHolder getKeyHolder()` | 生成键持有器，永不为 null |
| `KeyHolder keyHolder()` | 与 `getKeyHolder()` 等价 |
| `<T> T getKeyAs(Class<T> keyType)` | 按类型取第一个生成键，类型不匹配返回 null |
| `Object getKey()` | 取第一个生成键 |
| `String getKeyAsString()` | 生成键转字符串 |
| `Long getKeyAsLong()` | 生成键转 Long，无法转换返回 null |

### 4.2 JdbcUrlHelper

| 方法 | 说明 |
|---|---|
| `ConnInfo parseJdbcUrl(String jdbcUrl)` | 解析 MySQL JDBC URL；参数值会 URL 解码 |
| `String buildJdbcUrl(ConnInfo info)` | 由 `ConnInfo` 重建 URL；空字符串参数值会被保留 |
| `String getJdbcUrl(DataSource ds)` | 从 `DataSource` 获取连接后读取 `DatabaseMetaData.getURL()`，连接使用后自动关闭 |
| `String cloneJdbcUrl(String jdbcUrl)` | 解析后应用默认配置（utf8mb4、批量重写、北京时间）并重建 URL |
| `String cloneJdbcUrl(DataSource ds)` | 同上，URL 来自 `DataSource` |

`ConnInfo` 字段：`protocol`、`host`、`port`、`database`、`parameters`（有序 Map），并提供链式配置方法：

```java
ConnInfo info = new JdbcUrlHelper.ConnInfo("mysql", "127.0.0.1", 3306, "demo", new LinkedHashMap<>())
        .useUtf8mb4()          // useUnicode=true & characterEncoding=utf8mb4
        .useSSL(false)         // useSSL=false
        .enableBatch()         // rewriteBatchedStatements=true
        .useBeijingTimeZone(); // serverTimezone=Asia/Shanghai（URL 编码后写入）
```

其他可用方法：`addParameter`、`useCharset`、`connectionCollation`、`connectionTimeZone`、`useServerPrepStmts`、`allowMultiQueries`、`requireSSL`、`autoReconnect`、`useUTCTimeZone`、`useDefaultTimeZone`。

**不支持的 URL 形式**：`jdbc:mysql:loadbalance://`、`jdbc:mysql:replication://` 及多主机地址（如 `host1:3306,host2:3306`），解析时抛出 `IllegalArgumentException` 并说明原因。

### 4.3 ZoneIdDetector

| 方法 | 说明 |
|---|---|
| `static ZoneId detectDatabaseTimezone(Connection connection)` | 读取数据库时区；无法识别时返回 JVM 默认时区，不抛异常 |

### 4.4 SimpleRowMapper

| 方法 | 说明 |
|---|---|
| `SimpleRowMapper(Class<T> targetClass)` | 使用 JVM 默认时区 |
| `SimpleRowMapper(Class<T> targetClass, ZoneId databaseZoneId)` | 指定数据库时区，用于 `LocalDateTime` 等转换 |
| `T mapRow(ResultSet rs, int rowNum)` | 按列标签优先、列名兜底匹配实体字段 |

支持的目标字段类型：基本类型及包装类、`String`、`BigDecimal`、`Date`、`java.sql.Date`、`Time`、`Timestamp`、`LocalDate`、`LocalTime`、`LocalDateTime`、`Instant`、`ZonedDateTime`、`OffsetDateTime`。

### 4.5 FieldExtractor / FieldValue

| 方法 | 说明 |
|---|---|
| `static <T> List<FieldValue> extractField(Class<T> clazz)` | 提取字段定义（含主键标识），结果缓存 |
| `static <T> List<FieldValue> extractFieldValue(T t)` | 提取字段定义与实际值 |
| `static <T> String extractTableName(Class<T> clazz)` | 提取表名，`clazz` 为 null 时返回 null |
| `static <T> FieldValue extractPrimaryField(Class<T> clazz)` | 提取第一个主键字段，无主键返回 null |
| `static boolean isPrimary(Field field)` | 判断字段是否主键 |
| `static <T> boolean isBasicResult(Class<T> clazz)` | 判断是否为可直接作为标量结果返回的类型 |

`FieldValue` 常用方法：`getField()`、`getColumnName()`、`isPrimary()`、`getValue()`、`setValue(Object)`、`isNullVal()`、`isNotNullVal()`、`sqlType()`。

内部使用 `ClassValue` 缓存，随类卸载自动释放，无需手动清理。

### 4.6 PojoGenerator

| 方法 | 说明 |
|---|---|
| `void generateAllPojos(GeneratorConfig config)` | 连接数据库并逐表生成实体类；单表失败不影响其他表 |

`GeneratorConfig` 主要配置：

| 配置 | 默认值 | 说明 |
|---|---|---|
| `driver` | `com.mysql.cj.jdbc.Driver` | 驱动类名 |
| `url` / `username` / `password` | 无 | 连接信息 |
| `packageName` | `com.example.entity` | 生成类的包名 |
| `outputPath` | `src/main/java/com/example/entity` | 输出目录 |
| `tablePrefixes` | 空 | 生成类名时去掉的表前缀 |
| `includeTables` / `excludeTables` | 空 | 表过滤 |
| `useLombok` | `true` | 生成 `@Data` 等注解 |
| `useJpa` | `true` | 生成 `@Table`/`@Column`/`@Id` |
| `useSwagger` | `false` | 生成 OpenAPI 3 `@Schema` |
| `idType` | `Long` | 主键 Java 类型 |
| `useJava8Date` | `true` | 日期映射为 `java.time` 类型 |
| `generateColumnDefinition` | `true` | 生成 `columnDefinition`、长度、注释 |

类型映射要点：`BLOB`/`BINARY` → `byte[]`；`tinyint(1)` → `Boolean`；`POINT`、`INTERVAL` 等非整数类型 → `String` 或对应类型，不会因类型名包含 “int” 被误判为 `Integer`。

## 5. 代码示例

### 示例 1：初始化 JdbcHelper 并完成 CRUD

```java
JdbcHelper helper = new JdbcHelper(dataSource);

User user = new User();
user.setName("张三");          // id 为 null，由数据库自增
JdbcHelper.Result inserted = helper.insert(user);
Long id = inserted.getKeyAsLong();
// 预期：inserted.getAffectedRows() == 1，id 为数据库生成的主键

User found = helper.findByPrimaryKey(User.class, id);
// 预期：found.getName() 等于 "张三"

found.setName("李四");
helper.updateByPrimaryKey(found);
// 预期：SQL 形如 UPDATE `user` SET `name` = :name WHERE `id` = :id

helper.deleteByPrimaryKey(User.class, id);
// 预期：affectedRows == 1；传入 null 或空白主键时不会生成 DELETE 语句
```

### 示例 2：条件查询、模糊查询与排序

```java
User condition = new User();
condition.setStatus(1);

List<User> users = helper.find(condition, false, JdbcHelper.Order.desc("create_time"));
// 预期：status = 1 的记录，按 create_time 降序

List<User> fuzzy = helper.find(condition, true);
// 预期：status = 1，且字符串字段使用 LIKE '%值%'
```

### 示例 3：分页查询

```java
User condition = new User();
condition.setStatus(1);

PageQuery<User> pageQuery = PageQuery.of(condition, 10, 1);
Page<User> page = helper.findPage(pageQuery, false, JdbcHelper.Order.asc("id"));

List<User> data = page.getData();
Number total = page.getTotal();
// 预期：data 最多 10 条，total 为符合条件的总数
```

### 示例 4：动态 SQL

```java
List<User> users = helper.find(User.class, params -> {
    params.addValue("status", 1);
    params.addValue("keyword", "%张%");
    return "SELECT * FROM user WHERE status = :status AND name LIKE :keyword";
});
// 预期：返回 status=1 且 name 包含“张”的记录
```

### 示例 5：分页动态 SQL

```java
Page<User> page = helper.findPage(User.class,
        Slice.of(10, 2),
        "SELECT * FROM user WHERE status = :status",
        new MapSqlParameterSource("status", 1));
// 预期：第 2 页数据，每页 10 条
```

### 示例 6：批量写入

```java
List<User> batch = List.of(new User("A"), new User("B"));
JdbcHelper.Result result = helper.saveAll(batch);
// 预期：affectedRows == 2，getBatchAffectedRows() 返回各批次影响行数
```

### 示例 7：解析并重建 JDBC URL

```java
String url = "jdbc:mysql://127.0.0.1:3306/demo?useUnicode=true&characterEncoding=utf8mb4&serverTimezone=Asia%2FShanghai";
JdbcUrlHelper.ConnInfo info = JdbcUrlHelper.parseJdbcUrl(url);

info.useDefaultTimeZone();
String rebuilt = JdbcUrlHelper.buildJdbcUrl(info);
// 预期：serverTimezone 被移除，新增 useTimezone=true，其余参数保持原顺序
```

### 示例 8：使用简单映射器

```java
List<User> users = jdbcTemplate.query(
        "SELECT id, user_name AS name, create_time FROM user",
        new SimpleRowMapper<>(User.class, ZoneId.of("Asia/Shanghai")));
// 预期：name 从别名列映射；create_time 按 Asia/Shanghai 转为 LocalDateTime
```

### 示例 9：提取实体元数据

```java
String table = FieldExtractor.extractTableName(User.class);
FieldValue pk = FieldExtractor.extractPrimaryField(User.class);
List<FieldValue> fields = FieldExtractor.extractField(User.class);
// 预期：table = "user"，pk.getColumnName() = "id"，fields 不含 static/final/transient/@Transient 字段
```

### 示例 10：生成实体类

```java
PojoGenerator.GeneratorConfig config =
        new PojoGenerator.GeneratorConfig(url, username, password)
                .addTablePrefix("t_")
                .addExcludeTable("sys_log");
config.setPackageName("com.example.entity")
      .setOutputPath("src/main/java/com/example/entity")
      .setUseJpa(true)
      .setUseSwagger(true);

new PojoGenerator().generateAllPojos(config);
// 预期：为每张业务表生成一个 .java 文件，使用 UTF-8 写入；失败表会被跳过并记录日志
```

## 6. 注意事项与边界

1. **主键限制**：更新、删除只使用第一个主键字段，复合主键实体不支持。
2. **insert 与 saveAll 的差异**：`insert` 会排除值为 null 的主键字段；`saveAll` 以首个实体的字段集构建 SQL，不排除 null 主键（MySQL 下自增列写入 NULL 仍可自动生成）。
3. **更新/删除的“空条件”保护**：更新无 SET 字段、删除无有效主键时返回 `affectedRows = 0`，不执行 SQL，不会误更新全表。
4. **排序字段安全**：`Order` 的字段名经过严格校验，禁止传入任意 SQL 片段。
5. **tinyint(1) 判定**：`PojoGenerator` 依据列显示宽度判定 Boolean。MySQL 8.0.19+ 已废弃显示宽度，若驱动不再返回宽度 1，则生成 `Integer`，需按实际元数据确认。
6. **URL 支持范围**：仅支持单主机 MySQL URL；负载均衡、主从复制、多主机格式会明确报错。
7. **线程安全**：`JdbcHelper` 自身无可变业务状态，可复用；`ConnInfo` 的单个配置方法为 `synchronized`，但对象整体仍不建议多线程共享修改。
8. **异常约定**：参数/状态错误统一抛 `UncheckedException`，错误码见 `JdbcError`（6001–6008）。
9. **缓存**：`FieldExtractor`、`SimpleRowMapper` 使用 `ClassValue` 缓存字段映射；运行时修改注解不会自动刷新，需重新加载类。
10. **PojoGenerator 生成产物**：`useSwagger=true` 生成的是 OpenAPI 3（`io.swagger.v3.oas.annotations.media.Schema`）注解，需引入 springdoc/swagger-core v3 依赖。

## 7. 相关链接

- 源码目录：[src/main/java/com/yishuifengxiao/common/tool/jdbc](../../src/main/java/com/yishuifengxiao/common/tool/jdbc)
- 单元测试：[src/test/java/com/yishuifengxiao/common/tool/jdbc](../../src/test/java/com/yishuifengxiao/common/tool/jdbc)
- P2 修复变更记录：[202611112330-fix-jdbc-p2.md](../changelogs/202611112330-fix-jdbc-p2.md)
- P0/P1 修复变更记录：[202611112317-fix-jdbc-p0-p1.md](../changelogs/202611112317-fix-jdbc-p0-p1.md)
