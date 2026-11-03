# ExcelReader & ExcelWriter 使用手册

> 一套企业级通用 Excel 读写工具。**核心特性**：格式自动识别、双语表头、同步/异步流式读取、非空校验、动态列宽、文本溢出处理、全字符串写入。
>
> **关键设计**：
> - 正文始终**逐行流式回调**，每读一行立即释放内存——适合大文件。
> - 同步模式：`parse(...)` 返回时回调**已全部执行完**。
> - 异步模式：表头通过后 **`parse(...)` 立即返回**，正文在后台**流式回调**。
> - 两种模式下返回的 `ParseResult` 结构、方法、行为**完全相同**。

---

## 目录

- [一、依赖](#一依赖)
- [二、ExcelReader](#二excelreader)
  - [2.1 一分钟上手](#21-一分钟上手)
  - [2.2 核心类型总览](#22-核心类型总览)
  - [2.3 输入源与生命周期](#23-输入源与生命周期)
  - [2.4 列定义 ColumnDef](#24-列定义-columndef)
  - [2.5 配置 HeaderConfig](#25-配置-headerconfig)
  - [2.6 回调契约 RowCallback](#26-回调契约-rowcallback)
  - [2.7 单元格 CellData](#27-单元格-celldata)
  - [2.8 结果 ParseResult](#28-结果-parseresult)
  - [2.9 同步与异步](#29-同步与异步)
  - [2.10 语言模式](#210-语言模式)
  - [2.11 表头校验规则](#211-表头校验规则)
  - [2.12 非空校验](#212-非空校验)
  - [2.13 行数统计](#213-行数统计)
  - [2.14 完整示例集](#214-完整示例集)
- [三、ExcelWriter](#三excelwriter)
  - [3.1 一分钟上手](#31-一分钟上手)
  - [3.2 输出格式与目标](#32-输出格式与目标)
  - [3.3 配置 WriteConfig](#33-配置-writeconfig)
  - [3.4 数据源](#34-数据源)
  - [3.5 表头与数据列数不匹配](#35-表头与数据列数不匹配)
  - [3.6 列宽与文本溢出](#36-列宽与文本溢出)
  - [3.7 全字符串模式](#37-全字符串模式)
  - [3.8 完整示例集](#38-完整示例集)
- [四、综合实战](#四综合实战)
- [五、FAQ](#五faq)
- [附录：API 速查表](#附录api-速查表)

---

## 一、依赖

```xml
<dependencies>
    <!-- Apache POI -->
    <dependency>
        <groupId>org.apache.poi</groupId>
        <artifactId>poi</artifactId>
        <version>5.2.5</version>
    </dependency>
    <dependency>
        <groupId>org.apache.poi</groupId>
        <artifactId>poi-ooxml</artifactId>
        <version>5.2.5</version>
    </dependency>

    <!-- Lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <version>1.18.46</version>
        <scope>provided</scope>
    </dependency>

    <!-- 测试 -->
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter</artifactId>
        <version>5.10.0</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```

**环境要求**：JDK 21+。

---

## 二、ExcelReader

### 2.1 一分钟上手

**同步读取**：

```java
List<ColumnDef> columns = Arrays.asList(
        ColumnDef.required("姓名", "Name"),
        ColumnDef.required("年龄", "Age"),
        ColumnDef.nullable("部门", "Department"));

AtomicInteger rowCount = new AtomicInteger(0);

ParseResult r = ExcelReader.parse(new File("data.xlsx"),
        new HeaderConfig().setColumns(columns),
        (rowIndex, rowData) -> {
            rowCount.incrementAndGet();
            for (CellData cell : rowData.values()) {
                if (!cell.isValid()) {
                    System.err.println(cell.getValidationErrorZh());
                    continue;
                }
                System.out.printf("%s=%s%n",
                        cell.getColumn().getChineseName(), cell.getValue());
            }
        });

if (r.isSuccess()) {
    System.out.printf("行数: %d, 语言: %s%n",
            rowCount.get(), r.getLanguage().getDesc());
} else {
    System.err.println(r.getMessageZhShort());
}
```

**异步读取（大文件推荐）**：

```java
CountDownLatch allDone = new CountDownLatch(1);
AtomicInteger rowCount = new AtomicInteger(0);

ParseResult r = ExcelReader.parse(new File("big-data.xlsx"),
        new HeaderConfig().setColumns(columns).setAsync(true),
        (rowIndex, rowData) -> {
            rowCount.incrementAndGet();
            // 每行处理完立即释放，不缓存
        });

// 表头通过后 parse 立即返回，正文还在后台处理中
if (r.isFailed()) {
    System.err.println(r.getMessageZhShort());
    return;
}
System.out.println("表头已通过，正文在后台处理中...");
// 主线程可继续做其他事，比如更新 UI

// 如需感知完成，用 latch 在回调中控制（见 2.13）
```

### 2.2 核心类型总览

| 类型 | 作用 |
|------|------|
| `ExcelReader` | 工具类入口，静态方法 `parse(...)` |
| `HeaderConfig` | 解析配置（列定义、校验规则、异步开关等） |
| `ColumnDef` | 单列期望定义（中英文名 + 是否必填） |
| `RowCallback` | 逐行流式回调 |
| `CellData` | 单元格数据 + 列定义 + 非空校验结果 + 中英双语错误消息 |
| `ParseResult` | 解析结果（成功/失败 + 命中语言 + 表头映射 + 简略/详细消息） |
| `HeaderCellInfo` | 表头映射（columns 下标 ↔ Excel 物理列索引） |
| `HeaderLanguage` | 命中语言枚举（`ZH` / `EN`） |

### 2.3 输入源与生命周期

**4 种输入源**，均支持 `.xls` / `.xlsx` 自动识别：

```java
// ① File
ExcelReader.parse(new File("data.xlsx"), config, callback);

// ② byte[]
byte[] bytes = Files.readAllBytes(Paths.get("data.xlsx"));
ExcelReader.parse(bytes, config, callback);

// ③ InputStream（流由调用方关闭；异步模式需等正文读完再关）
try (InputStream in = new FileInputStream("data.xlsx")) {
    ExcelReader.parse(in, config, callback);
}

// ④ Workbook（由调用方负责关闭）
try (Workbook wb = WorkbookFactory.create(new FileInputStream("data.xlsx"))) {
    ExcelReader.parse(wb, config, callback);
}
```

**Workbook 生命周期表**：

| 输入方式 | 谁负责关闭 Workbook | 谁负责关闭输入流 | 何时可关闭（异步模式） |
|---------|-----------------|-----------------|---------------------|
| `File` / `byte[]` | **工具类** | **工具类**（正文读完或提交失败后） | 工具类自动在正文读完时关闭 |
| `InputStream` | **工具类** | **调用方** | Workbook 由工具类在正文读完时关闭；**流需调用方等正文读完后再关闭** |
| `Workbook` | **调用方** | 不适用 | **必须等正文读完**（可用 `onBodyComplete` 或 CountDownLatch 感知） |

> **异步模式 + Workbook 输入**示例：
> ```java
> try (Workbook wb = new XSSFWorkbook(...)) {
>     CountDownLatch done = new CountDownLatch(1);
>     ExcelReader.parse(wb,
>             new HeaderConfig().setColumns(columns).setAsync(true),
>             (idx, rowData) -> {
>                 // 处理最后一行时 countDown
>                 if (isLastRow) done.countDown();
>             });
>     done.await(30, TimeUnit.SECONDS);   // 等待正文读完才能关闭
> }
> ```

### 2.4 列定义 ColumnDef

**9 个快捷工厂**：

```java
// 中英双语
ColumnDef.required("姓名", "Name");         // 必填
ColumnDef.nullable("部门", "Department");    // 可空
ColumnDef.of("备注", "Remark", true);        // 通用

// 只有中文
ColumnDef.requiredZh("姓名");
ColumnDef.nullableZh("部门");
ColumnDef.ofZh("备注", true);

// 只有英文
ColumnDef.requiredEn("Name");
ColumnDef.nullableEn("Department");
ColumnDef.ofEn("Remark", true);
```

**常用方法**：

```java
col.getChineseName();           // "姓名"
col.getEnglishName();           // "Name"
col.isNullable();               // false
col.name(HeaderLanguage.EN);    // "Name"
```

**规则**：中英文名**至少填一个**；都为空 → 构造抛 `IllegalArgumentException`。

### 2.5 配置 HeaderConfig

| 参数 | 默认 | 说明 |
|------|------|------|
| `columns` | 空 | 列定义列表，**必填** |
| `ignoreBlankColumns` | `true` | 表头行中间空白单元格是否忽略 |
| `orderRequired` | `true` | 表头顺序是否必须与 columns 完全一致 |
| `allowExtraHeaders` | `false` | 是否允许 columns 之外的额外表头 |
| `ignoreCase` | `true` | 表头比对是否忽略大小写 |
| `ignoreInvisibleChars` | `true` | 表头比对是否忽略空白/不可见字符 |
| `ignoredChars` | 空 | 额外忽略的字符串列表（整体子串，长的优先） |
| `headerRowIndex` | `0` | 表头所在行（0 开始） |
| `sheetIndex` | `0` | 工作表索引（0 开始，超出实际数量时解析失败） |
| `async` | `false` | 正文读取模式（见 [2.9](#29-同步与异步)） |
| `executor` | null | 异步线程池，默认使用工具类内置的守护线程池（线程数 + 队列容量均有上限，队列满时拒绝任务） |
| `onBodyComplete` | null | 异步正文完成回调 `(success, error) -> {}`；正文结束或提交失败时触发（仅异步模式生效） |

**完整配置示例**：

```java
HeaderConfig config = new HeaderConfig()
        .setColumns(columns)
        .setOrderRequired(false)                          // 表头可乱序
        .setAllowExtraHeaders(true)                       // 允许额外列
        .setIgnoreBlankColumns(true)                      // 忽略空白列
        .setIgnoreCase(true)                              // 忽略大小写
        .setIgnoreInvisibleChars(true)                    // 忽略空白/不可见字符
        .setIgnoredChars(Arrays.asList("*", "（必填）"))   // 忽略 * 与"（必填）"
        .setSheetIndex(0)                                 // 第 1 个工作表
        .setHeaderRowIndex(0)                             // 表头在第 1 行
        .setAsync(false);                                 // 同步模式
```

> **列定义不可变**：`getColumns()` 返回不可变视图，修改请重新调用 `setColumns(...)`；`setColumns(List)` 与 `setColumns(ColumnDef...)` 都会做防御性拷贝。

### 2.6 回调契约 RowCallback

**4 条核心约定**：

1. `rowData` 是 `LinkedHashMap<Integer, CellData>`
2. **`key` = 列在 `columns` 中的下标（0..N-1），迭代顺序与 `columns` 完全一致**
3. **`CellData.getExcelIdx()` = 该列在 Excel 中的物理列索引**
4. **`rowIndex` = Excel 物理行号（0 开始）**：从表头行下一行起按物理位置递增，空白行（含未创建的间隙行）不跳过

**举例**：`columns = [姓名, 年龄, 部门]`，Excel 表头为 `[D:部门, A:姓名, C:年龄]`：

```java
ExcelReader.parse(file, config.setOrderRequired(false), (rowIndex, rowData) -> {
    // rowData.keySet() 迭代顺序永远是 0, 1, 2（= columns 顺序）
    // rowData.get(0) → 姓名，excelIdx = 0（Excel A 列）
    // rowData.get(1) → 年龄，excelIdx = 2（Excel C 列）
    // rowData.get(2) → 部门，excelIdx = 3（Excel D 列）
});
```

**触发时机**：

- 仅在**表头校验通过**后触发
- 表头失败 → **一次都不会触发**
- 空白行（整行无内容，甚至是未创建的空白行）**不会跳过**：照常回调，必填列的 `isValid()` 为 `false`
- **流式**：每读一行 → 立即回调 → 方法返回后 rowData 可被 GC

**执行线程**：

- 同步模式：调用方线程
- 异步模式：`executor` 指定线程池

### 2.7 单元格 CellData

| 方法 | 返回 | 说明 |
|------|------|------|
| `getValue()` | `String` | 单元格文本（已 trim 不可见字符，空值为 `""`；公式单元格返回**计算结果**） |
| `getColumn()` | `ColumnDef` | 列定义（永不为 null） |
| `getExcelIdx()` | `int` | Excel 物理列索引（0 开始） |
| `isValid()` | `boolean` | 非空校验结果（必填为空或 Excel 错误值 `#DIV/0!` 等均为 `false`） |
| `getValidationErrorZh()` | `String` | 校验失败时中文消息，通过时为 `null` |
| `getValidationErrorEn()` | `String` | 校验失败时英文消息，通过时为 `null` |
| `isMissingRequired()` | `boolean` | 等价于 `!isValid()` |

**判断整行是否有效**：

```java
boolean rowOk = rowData.values().stream().allMatch(CellData::isValid);
```

### 2.8 结果 ParseResult

| 方法 | 返回 | 说明 |
|------|------|------|
| `isSuccess()` / `isFailed()` | `boolean` | 是否成功（异步模式下仅代表表头） |
| `getLanguage()` | `HeaderLanguage` | 命中语言（**任何失败场景均为 `null`**） |
| `getHeaders()` | `List<HeaderCellInfo>` | 按 columns 顺序的表头映射（失败时为该阶段已解析到的表头） |
| `getMessageZh()` / `getMessageEn()` | `String` | **详细版**错误消息 |
| `getMessageZhShort()` / `getMessageEnShort()` | `String` | **简写版**错误消息 |

> **注意**：`ParseResult` **不含行数**。行数由调用方在回调中自行统计（见 [2.13](#213-行数统计)）。

**简写 vs 详细示例**（Excel 第 2 列写成了"性别"，期望是"年龄"）：

```java
System.out.println(r.getMessageZhShort());
// 表头校验失败：第 2 个表头（Excel 列索引 1）期望为 [年龄]，实际为 [性别]

System.out.println(r.getMessageZh());
// 表头校验失败：第 2 个表头（Excel 列索引 1）期望为 [年龄]，实际为 [性别]；
// 期望表头顺序为 [姓名, 年龄, 部门]，实际表头顺序为 [姓名, 性别, 部门]
```

> **建议**：面向用户展示用**简写版**，日志/排查用**详细版**。

### 2.9 同步与异步

**这是本工具的核心设计**。请仔细阅读。

#### 两种模式对比

| 项目 | `async = false`（默认） | `async = true` |
|------|------------------------|----------------|
| 表头校验 | 当前线程同步 | 当前线程同步 |
| 表头失败 | 立即返回 `ParseResult.fail(...)` | 立即返回 `ParseResult.fail(...)` |
| 表头失败时回调 | 一次都不触发 | 一次都不触发 |
| 表头失败时提交任务 | 不提交 | 不提交 |
| 正文读取线程 | 调用方线程 | `executor` 线程池 |
| 回调方式 | **流式逐行回调** | **流式逐行回调** |
| `parse(...)` 返回时机 | **回调全部执行完后** | **表头通过后立即返回** |
| `parse` 返回时数据状态 | 全部处理完 | **可能还在处理中** |
| 正文异常感知 | 通过 `ParseResult.fail` 返回 | 通过 `onBodyComplete` 回调通知（同时打印到 stderr） |
| 返回的 `ParseResult` | 结构、方法、行为**完全相同** | 结构、方法、行为**完全相同** |

> **默认线程池**：未设置 `executor` 时使用工具类内置的守护线程池（线程数与队列容量均有上限，队列满时拒绝任务），不会占用 `ForkJoinPool.commonPool()`。

#### 场景 1：普通读取（同步，推荐）

```java
AtomicInteger rowCount = new AtomicInteger(0);

ParseResult r = ExcelReader.parse(file,
        new HeaderConfig().setColumns(columns),
        (idx, rowData) -> rowCount.incrementAndGet());

if (r.isFailed()) {
    System.err.println(r.getMessageZhShort());     // 表头或正文异常都能拿到
} else {
    System.out.println("全部完成，行数: " + rowCount.get());
}
// 此时数据已全部处理完
```

**适用场景**：小型/中型文件、需要知道处理结果、需要感知异常。

#### 场景 2：大文件、UI 不阻塞（异步，推荐）

```java
AtomicInteger rowCount = new AtomicInteger(0);

ParseResult r = ExcelReader.parse(file,
        new HeaderConfig().setColumns(columns).setAsync(true),
        (idx, rowData) -> {
            rowCount.incrementAndGet();
            // 逐行处理，内存占用恒定（每行处理完即释放）
        });

// 表头通过后立即返回，UI 可继续交互
if (r.isFailed()) {
    System.err.println(r.getMessageZhShort());     // 仅表头异常能拿到
    return;
}
System.out.println("表头已通过，正文在后台处理中...");
// 主线程可继续做事
```

**适用场景**：大文件、UI 不阻塞、后台任务、批量并行处理。

#### 场景 3：需要感知异步完成 / 异常

由于异步 `parse` 立即返回，完成与异常感知需要 `onBodyComplete`：

```java
CountDownLatch done = new CountDownLatch(1);
AtomicBoolean success = new AtomicBoolean(false);
AtomicReference<Throwable> error = new AtomicReference<>();

ExcelReader.parse(file,
        new HeaderConfig()
                .setColumns(columns)
                .setAsync(true)
                .setOnBodyComplete((ok, err) -> {
                    success.set(ok);
                    error.set(err);
                    done.countDown();
                }),
        (idx, rowData) -> {
            // 逐行处理
        });

done.await(30, TimeUnit.SECONDS);
if (!success.get()) {
    System.err.println("正文读取失败: " + error.get().getMessage());
}
```

回调在**正文结束时恰好触发一次**；若任务提交失败（线程池关闭/队列满），也会以 `(false, error)` 触发一次。`callback == null` 时不会启动正文任务，也不会触发回调。回调内抛出的异常只会打印到 stderr，不影响正文流程。

> **建议**：如果只需要"精确知道何时完成"，**直接用同步模式**——`parse` 返回时就是完成时。

#### 场景 4：自定义线程池

```java
ExecutorService excelPool = Executors.newFixedThreadPool(4);
try {
    ExcelReader.parse(file,
            new HeaderConfig()
                    .setColumns(columns)
                    .setAsync(true)
                    .setExecutor(excelPool),
            callback);
} finally {
    excelPool.shutdown();
}
```

#### 异步异常处理（重要）

异步模式下**正文异常无法通过返回值传播**（`parse` 已经返回）。发生异常时工具类会：

1. 打印到 stderr（含堆栈）：

   ```
   [ExcelReader] 异步正文读取异常: ...
   java.lang.RuntimeException: ...
   ```

2. 若设置了 `onBodyComplete`，调用一次 `onComplete(false, error)`；
3. 在 `finally` 中关闭 Workbook 与工具类打开的文件流（输入源为 `File` / `byte[]`；`InputStream` 由调用方关闭）。

**需要程序化感知正文异常 → 设置 `onBodyComplete`，或改用同步模式**。

> **任务提交失败**：若 `executor` 已关闭或队列已满（默认池队列容量 64），`parse(...)` 不抛异常，而是返回 `ParseResult.isFailed()`（消息以"异步任务提交失败"开头），`language` 为 `null`，Workbook 与内部文件流会被立即释放。

### 2.10 语言模式

工具类会根据 `ColumnDef` 中**用户实际提供**的语言自动判断校验模式：

| 场景 | 判定条件 | 校验行为 | `getLanguage()` |
|------|---------|---------|----------------|
| 全双语 | 每列都提供中英名 | 中英双套校验，任一通过即成功 | 命中哪套就哪套 |
| 全中文 | 所有列都有中文名 | 只做中文校验 | 固定 `ZH` |
| 全英文 | 所有列都有英文名 | 只做英文校验 | 固定 `EN` |
| 混合 | 部分只中文、部分只英文 | **抛 `IllegalArgumentException`** | — |

**示例**：

```java
// 全中文
List<ColumnDef> zhOnly = Arrays.asList(
        ColumnDef.requiredZh("姓名"),
        ColumnDef.requiredZh("年龄"));
// language = ZH

// 全英文
List<ColumnDef> enOnly = Arrays.asList(
        ColumnDef.requiredEn("Name"),
        ColumnDef.requiredEn("Age"));
// language = EN

// 部分双语（都有中文名，走 ZH_ONLY）
List<ColumnDef> partial = Arrays.asList(
        ColumnDef.required("姓名", "Name"),   // 双语
        ColumnDef.requiredZh("年龄"));         // 只中文
// language = ZH

// 混合（抛异常）
List<ColumnDef> mixed = Arrays.asList(
        ColumnDef.requiredZh("姓名"),
        ColumnDef.requiredEn("Age"));   // 只英文 —— 混合！
// 抛 IllegalArgumentException
```

### 2.11 表头校验规则

**四组正交参数**：

**① `orderRequired`（顺序）**

```java
// true（默认）：必须与 columns 顺序完全一致
// false：顺序随意，但必须全部包含
new HeaderConfig().setOrderRequired(false);
```

**② `allowExtraHeaders`（额外列）**

```java
// false（默认）：不允许 columns 之外的列
// true：允许额外列，额外列被忽略（不进入 rowData）
new HeaderConfig().setAllowExtraHeaders(true);
```

**③ `ignoreBlankColumns`（空白列）**

```java
// true（默认）：表头行中间空白单元格被跳过
// false：空白单元格作为"空白表头"参与校验
//   - 顺序模式：与对应位置比对（必然不等，失败）
//   - 乱序模式：视为额外列（受 allowExtraHeaders 约束）
new HeaderConfig().setIgnoreBlankColumns(false);
```

**④ 归一化规则**

```java
new HeaderConfig()
        .setIgnoreCase(true)                          // "NAME" == "name"
        .setIgnoreInvisibleChars(true)                // 忽略空格、全角空格、NBSP、BOM、零宽字符
        .setIgnoredChars(Arrays.asList("*", "（必填）")); // 整体子串移除，长的优先
```

**归一化顺序**：先去不可见字符 → 再移除自定义字符串 → 再按大小写规则比对。

**举例**：

```
Excel 表头："姓名* "（带 * 和尾部空格）
期望："姓名"
配置：setIgnoreInvisibleChars(true).setIgnoredChars(Arrays.asList("*"))
结果：通过 ✅
```

### 2.12 非空校验

**规则**：

- `ColumnDef.nullable = true`：任意值都通过
- `ColumnDef.nullable = false`：值为空 → `CellData.valid = false`，填充双语错误消息
- **不中断解析**：仅标记，业务自行决定是否报错

**示例**：

```java
List<ColumnDef> columns = Arrays.asList(
        ColumnDef.required("姓名", "Name"),
        ColumnDef.nullable("部门", "Department"));

ExcelReader.parse(file, new HeaderConfig().setColumns(columns),
        (rowIndex, rowData) -> {
            for (CellData cell : rowData.values()) {
                if (!cell.isValid()) {
                    System.out.println(cell.getValidationErrorZh());
                    // 第 5 行，列 [姓名/Name]（Excel 列索引 0）为必填列，但内容为空
                    System.out.println(cell.getValidationErrorEn());
                    // Row 5, column [姓名/Name] (Excel column index 0) is required but empty
                }
            }
        });
```

### 2.13 行数统计

`ParseResult` 不含行数，由调用方在回调中自行统计。**推荐方式**：

```java
// 方式 1：AtomicInteger（推荐，并发安全）
AtomicInteger rowCount = new AtomicInteger(0);
ExcelReader.parse(file, config, (idx, rowData) -> {
    rowCount.incrementAndGet();
    // 处理每行
});
System.out.println("行数: " + rowCount.get());

// 方式 2：List 收集（同时保存数据）
List<List<String>> rows = new ArrayList<>();
ExcelReader.parse(file, config, (idx, rowData) -> {
    List<String> row = new ArrayList<>();
    for (int i = 0; i < columns.size(); i++) {
        row.add(rowData.get(i).getValue());
    }
    rows.add(row);
});
System.out.println("行数: " + rows.size());

// 方式 3：只需要知道"是否有数据"
AtomicBoolean hasData = new AtomicBoolean(false);
ExcelReader.parse(file, config, (idx, rowData) -> hasData.set(true));
```

**异步模式下感知"全部完成"**：

使用 `onBodyComplete`（见 2.9），无需依赖总行数：

```java
CountDownLatch allDone = new CountDownLatch(1);

ExcelReader.parse(file, config.setAsync(true)
        .setOnBodyComplete((ok, err) -> allDone.countDown()),
        (idx, rowData) -> {
            // 逐行处理
        });

allDone.await(60, TimeUnit.SECONDS);
```

> 本工具**不扫描正文统计行数**，`callback == null` 只校验表头；如确需总数，请在回调中自行计数或从业务侧获取。

### 2.14 完整示例集

#### 示例 1：基础读取

```java
List<ColumnDef> columns = Arrays.asList(
        ColumnDef.required("姓名", "Name"),
        ColumnDef.required("年龄", "Age"),
        ColumnDef.nullable("部门", "Department"));

AtomicInteger rowCount = new AtomicInteger(0);
ParseResult r = ExcelReader.parse(new File("data.xlsx"),
        new HeaderConfig().setColumns(columns),
        (rowIndex, rowData) -> {
            rowCount.incrementAndGet();
            for (CellData cell : rowData.values()) {
                if (cell.isValid()) {
                    System.out.printf("%s=%s%n",
                            cell.getColumn().getChineseName(), cell.getValue());
                }
            }
        });

if (r.isSuccess()) {
    System.out.printf("行数: %d, 语言: %s%n",
            rowCount.get(), r.getLanguage().getDesc());
}
```

#### 示例 2：乱序 + 允许额外列

```java
ExcelReader.parse(file,
        new HeaderConfig()
                .setColumns(columns)
                .setOrderRequired(false)
                .setAllowExtraHeaders(true),
        callback);
```

#### 示例 3：只中文 / 只英文

```java
// 只中文
List<ColumnDef> zhOnly = Arrays.asList(
        ColumnDef.requiredZh("姓名"),
        ColumnDef.requiredZh("年龄"));

// 只英文
List<ColumnDef> enOnly = Arrays.asList(
        ColumnDef.requiredEn("Name"),
        ColumnDef.requiredEn("Age"));
```

#### 示例 4：忽略自定义符号

```java
// Excel 表头："姓名*"、"年龄（必填）"、"（部门）"
ExcelReader.parse(file,
        new HeaderConfig()
                .setColumns(columns)
                .setIgnoredChars(Arrays.asList("*", "（必填）", "（", "）")),
        callback);
```

#### 示例 5：大文件异步流式读取

```java
AtomicInteger rowCount = new AtomicInteger(0);
AtomicReference<String> firstRow = new AtomicReference<>();

ParseResult r = ExcelReader.parse(new File("huge.xlsx"),
        new HeaderConfig()
                .setColumns(columns)
                .setAsync(true),
        (idx, rowData) -> {
            int c = rowCount.incrementAndGet();
            if (c == 1) {
                firstRow.set(rowData.get(0).getValue());
            }
            // 逐行处理，不缓存 → 内存恒定
            processRow(rowData);
        });

if (r.isFailed()) {
    System.err.println(r.getMessageZhShort());
    return;
}
System.out.println("表头通过，后台处理中...");
// UI 可继续交互
```

#### 示例 6：只校验表头

```java
ParseResult r = ExcelReader.parse(file,
        new HeaderConfig().setColumns(columns),
        null);

System.out.println(r.isSuccess() ? "表头通过" : r.getMessageZhShort());
```

#### 示例 7：批量处理多个文件

```java
List<File> files = getExcelFiles();
Map<File, ParseResult> results = new LinkedHashMap<>();

for (File f : files) {
    try {
        ParseResult r = ExcelReader.parse(f,
                new HeaderConfig().setColumns(columns),
                callback);
        results.put(f, r);
    } catch (IOException e) {
        System.err.println("读取失败: " + f.getName() + " - " + e.getMessage());
    }
}

long okCount = results.values().stream().filter(ParseResult::isSuccess).count();
System.out.printf("成功 %d / %d%n", okCount, results.size());
```

---

## 三、ExcelWriter

### 3.1 一分钟上手

```java
ExcelWriter.write(new File("out.xlsx"), new ExcelWriter.WriteConfig()
        .setHeaders("姓名", "年龄", "部门")
        .setListData(Arrays.asList(
                Arrays.asList("张三", "28", "研发部"),
                Arrays.asList("李四", "30", "市场部"))));
```

### 3.2 输出格式与目标

| 输出方式 | 格式 | 说明 |
|---------|------|------|
| `write(File, config)` | **根据后缀自动识别** | `.xls` → HSSF；`.xlsx` → XSSF；其他 → 抛异常 |
| `write(OutputStream, config)` | 默认 `.xlsx` | 便利重载 |
| `write(OutputStream, config, format)` | 显式指定 | |
| `writeToBytes(config)` | 默认 `.xlsx` | |
| `writeToBytes(config, format)` | 显式指定 | |
| `write(Workbook, config)` | — | 向已有 Workbook 追加 Sheet |

**示例**：

```java
WriteConfig cfg = new WriteConfig()
        .setHeaders("姓名", "年龄")
        .setListData(Arrays.asList(Arrays.asList("张三", "28")));

// 自动识别格式
ExcelWriter.write(new File("out.xlsx"), cfg);   // XSSF
ExcelWriter.write(new File("out.xls"), cfg);    // HSSF

// 显式指定格式
try (OutputStream out = new FileOutputStream("data.bin")) {
    ExcelWriter.write(out, cfg, ExcelFormat.XLS);
}

// 输出字节数组
byte[] xlsxBytes = ExcelWriter.writeToBytes(cfg);
byte[] xlsBytes = ExcelWriter.writeToBytes(cfg, ExcelFormat.XLS);
```

**格式识别规则**（`ExcelFormat.detect`）：

```java
ExcelFormat.detect("a.xlsx");   // XLSX
ExcelFormat.detect("a.XLSX");   // XLSX（大小写不敏感）
ExcelFormat.detect("a.xls");    // XLS
ExcelFormat.detect("a.csv");    // null
```

> **HSSF 边界保护**：`.xls` 最多 65536 行 × 256 列，超出时提前抛 `IllegalArgumentException`。

> **写入安全**：`write(File, config)` 先在同目录写临时文件（`.tmp` 后缀，崩溃残留不会被误认为 Excel 文件），`fsync` 后原子替换目标文件；参数校验或写入失败时，**已存在的目标文件内容不会被破坏**。清理临时文件失败会被忽略，进程被强杀时也可能残留 `.tmp`，可直接删除。
>
> - 目标为符号链接时，写入链接指向的真实文件，链接本身保留；**层级超过 8 层或存在环时报 `IOException`**。
> - 格式按**真实目标文件**的后缀判断（链接名与真实后缀不一致时，以真实目标为准）。
> - **目标已存在且不可写（如只读文件）时拒绝覆盖**并抛 `IOException`；该检查为尽力而为，root/管理员身份下仍可能写入。
> - POSIX 平台保留目标文件原有权限；新建文件不硬编码权限，沿用平台默认权限（受 umask 影响，owner/ACL 不保留）。
> - 仅对临时文件执行 `fsync`，**不对父目录 fsync**：原子替换保证"旧文件或新文件之一可见"，但不保证断电后更新一定持久化。
> - `write(Workbook, config)` 遇到同名 Sheet 时抛 `IllegalArgumentException`，不会静默覆盖。

### 3.3 配置 WriteConfig

| 参数 | 默认 | 说明 |
|------|------|------|
| `sheetName` | `"Sheet1"` | 工作表名称 |
| `headers` | 空 | 表头列表 |
| `listData` | 空 | List 形式数据（与 mapData 互斥） |
| `mapData` | 空 | Map 形式数据（与 listData 互斥） |
| `truncateExtraData` | `true` | `true`：有表头时数据超表头忽略；`false`：表头补空 |
| `textOverflow` | `WRAP` | `WRAP` 自动换行；`HIDE` 隐藏 |
| `allAsText` | `true` | `true`：全部按文本写入 |
| `autoColumnWidth` | `true` | 是否自动计算列宽 |
| `minColumnWidth` | `8` | 列宽下限（字符数） |
| `maxColumnWidth` | `60` | 列宽上限（字符数） |
| `freezeHeader` | `true` | 是否冻结首行 |

> **参数校验**：`sheetName` 必须非空、不超过 31 个字符且不含 Excel 禁用字符（`: \ / ? * [ ]`）；`autoColumnWidth = true` 时要求 `1 <= minColumnWidth <= maxColumnWidth <= 255`，否则在写文件之前即抛 `IllegalArgumentException`。

**链式调用示例**：

```java
WriteConfig cfg = new WriteConfig()
        .setSheetName("员工表")
        .setHeaders("姓名", "年龄", "部门")
        .setListData(data)
        .setTruncateExtraData(true)
        .setTextOverflow(TextOverflow.WRAP)
        .setAllAsText(true)
        .setAutoColumnWidth(true)
        .setMinColumnWidth(8)
        .setMaxColumnWidth(60)
        .setFreezeHeader(true);
```

### 3.4 数据源

**方式一：`List<List<String>>`**

```java
WriteConfig cfg = new WriteConfig()
        .setHeaders("姓名", "年龄", "部门")
        .setListData(Arrays.asList(
                Arrays.asList("张三", "28", "研发部"),
                Arrays.asList("李四", "30", "市场部")));
```

**方式二：`List<Map<String, String>>`**

```java
Map<String, String> row = new LinkedHashMap<>();
row.put("姓名", "张三");
row.put("年龄", "28");
row.put("部门", "研发部");

WriteConfig cfg = new WriteConfig()
        .setHeaders("姓名", "年龄", "部门")   // Map 模式按此顺序取值
        .setMapData(Arrays.asList(row));
```

> **互斥**：调用 `setListData(...)` 会清空 `mapData`，反之亦然。
> **Map 模式取值**：按 `headers` 顺序逐列取 `map.get(header)`，缺失取 `""`；集合中的 `null` 行会被跳过，不产生空白数据行。

### 3.5 表头与数据列数不匹配

**`truncateExtraData = true`（默认）**：

```java
// headers = ["A", "B"]，data = ["a1", "b1", "c1", "d1"]
// 结果：只写 A/B 两列，c1 / d1 被忽略
new WriteConfig()
        .setHeaders("A", "B")
        .setListData(Collections.singletonList(
                Arrays.asList("a1", "b1", "c1", "d1")))
        .setTruncateExtraData(true);
```

**`truncateExtraData = false`**：

```java
// headers = ["A", "B"]，data = ["a1", "b1", "c1", "d1"]
// 结果：表头补成 ["A", "B", "", ""]，c1 / d1 完整写入
new WriteConfig()
        .setHeaders("A", "B")
        .setListData(Collections.singletonList(
                Arrays.asList("a1", "b1", "c1", "d1")))
        .setTruncateExtraData(false);
```

> **Map 模式额外说明**：`truncateExtraData = false` 或**未设置表头**时，Map 中出现过、headers 中未声明的 key 会被作为表头（保序、去重）。
>
> **未设置表头时**：List 数据从第 0 行开始、**不写空表头行**；Map 数据以 Map 的 key 作为表头（见上一条）。列数按数据最大列数展开（避免数据被整体丢弃）。

### 3.6 列宽与文本溢出

**列宽自适应**：

```java
new WriteConfig()
        .setHeaders("短", "长")
        .setListData(Collections.singletonList(
                Arrays.asList("x", "很长很长".repeat(50))))
        .setMinColumnWidth(8)
        .setMaxColumnWidth(60);
```

**列宽计算规则**：

1. 遍历所有单元格，计算显示宽度（ASCII = 1，中文 = 2）
2. 加上 padding（+2）
3. clamp 到 `[minColumnWidth, maxColumnWidth]`

**文本溢出**：

```java
// WRAP：自动换行 + 自动调行高
new WriteConfig().setTextOverflow(TextOverflow.WRAP);

// HIDE：不换行，超出被截断显示
new WriteConfig().setTextOverflow(TextOverflow.HIDE);
```

**行高计算**（仅 WRAP 模式）：

- **表头行与数据行**都会按实际列宽估算行高
- 按**实际列宽**（clamp 之后）估算行数
- 每行 15pt，上限 409pt（POI 单行限制）

### 3.7 全字符串模式

```java
// allAsText = true（默认）：所有内容都按文本写入
// 效果："001234" 保留前导 0，不会变成 1234
new WriteConfig()
        .setHeaders("编号")
        .setListData(Collections.singletonList(Collections.singletonList("001234")))
        .setAllAsText(true);

// allAsText = false：自动识别
// "1234" → 数字 1234
// "true" → 布尔 true
// 其他 → 文本
new WriteConfig()
        .setHeaders("值")
        .setListData(Collections.singletonList(Collections.singletonList("1234")))
        .setAllAsText(false);
```

> **建议**：默认保持 `true`，除非明确要写数字/布尔类型（例如用于 Excel 公式计算）。
>
> **长整型保护**：`allAsText = false` 时，绝对值超过 `2^53` 的整数（如雪花 ID、订单号）**自动按文本写入**，避免 double 精度丢失；`2^53` 以内仍写为数字。

### 3.8 完整示例集

#### 示例 1：List 数据基础写入

```java
ExcelWriter.write(new File("out.xlsx"), new WriteConfig()
        .setHeaders("姓名", "年龄", "部门")
        .setListData(Arrays.asList(
                Arrays.asList("张三", "28", "研发部"),
                Arrays.asList("李四", "30", "市场部"))));
```

#### 示例 2：Map 数据 + 表头过滤

```java
List<Map<String, String>> data = new ArrayList<>();
for (User user : userList) {
    Map<String, String> row = new LinkedHashMap<>();
    row.put("姓名", user.getName());
    row.put("年龄", String.valueOf(user.getAge()));
    row.put("部门", user.getDepartment());
    data.add(row);
}

ExcelWriter.write(new File("out.xlsx"), new WriteConfig()
        .setHeaders("姓名", "年龄", "部门")   // 只输出这三列
        .setMapData(data));
```

#### 示例 3：大段文本自动换行

```java
WriteConfig cfg = new WriteConfig()
        .setHeaders("标题", "内容")
        .setListData(Collections.singletonList(
                Arrays.asList("技术文档", "很长的描述...".repeat(20))))
        .setTextOverflow(TextOverflow.WRAP)
        .setMinColumnWidth(10)
        .setMaxColumnWidth(60);

ExcelWriter.write(new File("doc.xlsx"), cfg);
```

#### 示例 4：输出到 HTTP 响应

```java
@GetMapping("/export")
public void export(HttpServletResponse response) throws IOException {
    response.setContentType(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    response.setHeader("Content-Disposition",
            "attachment; filename=\"export.xlsx\"");

    ExcelWriter.write(response.getOutputStream(), new WriteConfig()
            .setHeaders("姓名", "年龄", "部门")
            .setListData(data));
}
```

#### 示例 5：多 Sheet 输出

```java
try (Workbook wb = new XSSFWorkbook()) {
    ExcelWriter.write(wb, new WriteConfig()
            .setSheetName("员工")
            .setHeaders("姓名", "年龄")
            .setListData(employeeData));

    ExcelWriter.write(wb, new WriteConfig()
            .setSheetName("部门")
            .setHeaders("部门名称", "人数")
            .setListData(deptData));

    try (OutputStream out = new FileOutputStream("multi.xlsx")) {
        wb.write(out);
    }
}
```

---

## 四、综合实战

### 场景 1：读入 A → 校验 → 转换 → 输出 B

```java
public class DataTransformDemo {
    public static void main(String[] args) throws Exception {
        List<ColumnDef> columns = Arrays.asList(
                ColumnDef.required("姓名", "Name"),
                ColumnDef.required("年龄", "Age"),
                ColumnDef.nullable("部门", "Department"));

        List<List<String>> outputRows = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        // 读取（同步）
        ParseResult readResult = ExcelReader.parse(new File("source.xlsx"),
                new HeaderConfig()
                        .setColumns(columns)
                        .setOrderRequired(false)
                        .setIgnoreInvisibleChars(true)
                        .setIgnoredChars(Arrays.asList("*", "（必填）")),
                (rowIndex, rowData) -> {
                    // 校验：任意必填列为空则跳过该行
                    boolean rowValid = rowData.values().stream()
                            .allMatch(CellData::isValid);
                    if (!rowValid) {
                        rowData.values().stream()
                                .filter(c -> !c.isValid())
                                .forEach(c -> errors.add(c.getValidationErrorZh()));
                        return;
                    }
                    // 提取：按 columns 顺序收集
                    List<String> row = new ArrayList<>();
                    for (int i = 0; i < columns.size(); i++) {
                        row.add(rowData.get(i).getValue());
                    }
                    outputRows.add(row);
                });

        if (readResult.isFailed()) {
            System.err.println("表头校验失败: " + readResult.getMessageZhShort());
            return;
        }
        System.out.printf("成功读取 %d 行，%d 行校验失败%n",
                outputRows.size(), errors.size());
        errors.forEach(System.err::println);

        // 写出
        ExcelWriter.write(new File("dest.xlsx"), new ExcelWriter.WriteConfig()
                .setSheetName("校验通过数据")
                .setHeaders("姓名", "年龄", "部门")
                .setListData(outputRows)
                .setTextOverflow(ExcelWriter.TextOverflow.WRAP)
                .setMinColumnWidth(10)
                .setMaxColumnWidth(40)
                .setAllAsText(true));

        System.out.println("输出完成: dest.xlsx");
    }
}
```

### 场景 2：大文件异步导入数据库（UI 不阻塞）

```java
public class AsyncImportDemo {

    private final JdbcTemplate jdbc;
    private final ExecutorService pool = Executors.newFixedThreadPool(4);

    public void importFile(File file) throws IOException {
        List<ColumnDef> columns = Arrays.asList(
                ColumnDef.requiredZh("姓名"),
                ColumnDef.requiredZh("年龄"),
                ColumnDef.nullableZh("部门"));

        List<Object[]> batch = Collections.synchronizedList(new ArrayList<>());

        ExcelReader.ParseResult r = ExcelReader.parse(file,
                new HeaderConfig()
                        .setColumns(columns)
                        .setAsync(true)                       // 异步
                        .setExecutor(pool),
                (rowIndex, rowData) -> {
                    // 在后台线程逐行处理
                    if (rowData.values().stream().allMatch(CellData::isValid)) {
                        batch.add(new Object[]{
                                rowData.get(0).getValue(),
                                Integer.parseInt(rowData.get(1).getValue()),
                                rowData.get(2).getValue()
                        });
                    }
                });

        if (r.isFailed()) {
            System.err.println("表头不合规: " + r.getMessageZhShort());
            return;
        }
        // parse 已返回，UI 可继续；正文在后台进行
        System.out.println("表头已通过，导入进行中...");
    }

    @PreDestroy
    public void shutdown() {
        pool.shutdown();
    }
}
```

---

## 五、FAQ

### Q1：如何获取读取到的行数？

`ParseResult` **不含行数**，请在回调中自行统计：

```java
AtomicInteger count = new AtomicInteger(0);
ExcelReader.parse(file, config, (idx, rowData) -> count.incrementAndGet());
System.out.println("行数: " + count.get());
```

### Q2：同步和异步到底有什么区别？

**唯一区别**：

- **同步**：`parse(...)` **等待回调全部执行完**才返回；
- **异步**：`parse(...)` **表头通过后立即返回**，回调在后台流式执行。

返回的 `ParseResult` 结构完全一致。回调都是**流式逐行**的，内存占用相同。

### Q3：异步模式下如何知道"处理完成"？

三种方案：

1. **改用同步模式**（推荐）——`parse` 返回时就是完成时；
2. **在回调中计数 + Latch**：

```java
AtomicInteger count = new AtomicInteger(0);
int expected = 1000;
CountDownLatch done = new CountDownLatch(1);
ExcelReader.parse(file, config.setAsync(true), (idx, rowData) -> {
    if (count.incrementAndGet() == expected) done.countDown();
});
done.await(60, TimeUnit.SECONDS);
```

3. **包一层**：

```java
CompletableFuture.runAsync(() -> {
    ExcelReader.parse(file, config, callback);   // 同步模式，在 future 中跑
});
```

### Q4：异步模式下正文异常怎么办？

**异步正文异常无法传播**，工具类会**打印到 stderr** 并终止后台任务：

```
[ExcelReader] 异步正文读取异常: ...
```

**如果必须感知异常，请使用同步模式**。

### Q5：如何处理大文件（百万行以上）？

当前实现基于 POI `WorkbookFactory`，会把整表加载到内存。适用场景：

- 文件 < 100MB
- 行数 < 100 万

**超过此规模**的建议：

1. 改用 POI 的 `XSSFReader` 事件模型（真正流式读取）
2. 或使用 EasyExcel 等流式库
3. 或先切分为小文件

### Q6：如何处理表头带 `*` 或 `（必填）`？

```java
new HeaderConfig()
        .setColumns(columns)
        .setIgnoredChars(Arrays.asList("*", "（必填）", "（", "）"));
```

每个元素按"整体子串"处理，支持单字符与多字符；内部按长度降序移除。

### Q7：表头校验失败后如何拿到实际表头内容？

```java
if (r.isFailed()) {
    for (HeaderCellInfo h : r.getHeaders()) {
        System.out.println("Excel 列 " + h.getColumnIndex() + ": " + h.getValue());
    }
}
```

### Q8：如何避免 `001234` 被 Excel 变成 `1234`？

- **ExcelReader 无此问题**：值以字符串形式返回；
- **ExcelWriter**：保持默认 `allAsText = true` 即可。

### Q9：如何处理合并单元格？

**目前不支持**。合并单元格中只有左上角有值，其他为空。可在回调中实现"向下填充"：

```java
Map<Integer, String> lastValues = new HashMap<>();
ExcelReader.parse(file, config, (rowIndex, rowData) -> {
    for (Map.Entry<Integer, CellData> e : rowData.entrySet()) {
        String v = e.getValue().getValue();
        if (v.isEmpty() && lastValues.containsKey(e.getKey())) {
            v = lastValues.get(e.getKey());
        } else if (!v.isEmpty()) {
            lastValues.put(e.getKey(), v);
        }
        // 使用 v ...
    }
});
```

### Q10：如何把校验失败的行单独输出？

```java
List<List<String>> invalidRows = new ArrayList<>();
ExcelReader.parse(file, config, (rowIndex, rowData) -> {
    if (rowData.values().stream().anyMatch(c -> !c.isValid())) {
        List<String> row = new ArrayList<>();
        for (int i = 0; i < columns.size(); i++) {
            row.add(rowData.get(i).getValue());
        }
        invalidRows.add(row);
    }
});

if (!invalidRows.isEmpty()) {
    ExcelWriter.write(new File("invalid.xlsx"), new ExcelWriter.WriteConfig()
            .setSheetName("校验失败")
            .setHeaders("姓名", "年龄", "部门")
            .setListData(invalidRows));
}
```

### Q11：异步模式下传入 Workbook 需要注意什么？

`parse(Workbook, ...)` 时，**调用方必须等正文读完后再关闭 Workbook**：

```java
try (Workbook wb = new XSSFWorkbook(...)) {
    CountDownLatch done = new CountDownLatch(1);
    ExcelReader.parse(wb,
            new HeaderConfig().setColumns(columns).setAsync(true),
            (idx, rowData) -> {
                // 处理最后一行时 countDown
                if (isLastRow) done.countDown();
            });
    done.await(60, TimeUnit.SECONDS);
}   // 此时才关闭
```

**更简单**：用同步模式，`parse` 返回后直接关闭。

---

## 附录：API 速查表

### ExcelReader.HeaderConfig

| Setter | 类型 | 默认 | 说明 |
|--------|------|------|------|
| `setColumns` | `List<ColumnDef>` / `ColumnDef...` | 空 | 列定义（必填） |
| `setOrderRequired` | `boolean` | `true` | 表头顺序严格度 |
| `setAllowExtraHeaders` | `boolean` | `false` | 是否允许额外列 |
| `setIgnoreBlankColumns` | `boolean` | `true` | 忽略空白列 |
| `setIgnoreCase` | `boolean` | `true` | 忽略大小写 |
| `setIgnoreInvisibleChars` | `boolean` | `true` | 忽略不可见字符 |
| `setIgnoredChars` | `List<String>` | 空 | 额外忽略字符串 |
| `setHeaderRowIndex` | `int` | `0` | 表头行索引 |
| `setSheetIndex` | `int` | `0` | 工作表索引 |
| `setAsync` | `boolean` | `false` | **正文读取模式** |
| `setExecutor` | `Executor` | null | 异步线程池 |

### ExcelReader.ParseResult

| 方法 | 返回 | 说明 |
|------|------|------|
| `isSuccess()` / `isFailed()` | `boolean` | 是否成功 |
| `getLanguage()` | `HeaderLanguage` | 命中语言（可能 null） |
| `getHeaders()` | `List<HeaderCellInfo>` | 表头映射 |
| `getMessageZh()` / `getMessageEn()` | `String` | 详细错误消息 |
| `getMessageZhShort()` / `getMessageEnShort()` | `String` | 简写错误消息 |

### ExcelReader.CellData

| 方法 | 返回 |
|------|------|
| `getValue()` | `String` |
| `getColumn()` | `ColumnDef` |
| `getExcelIdx()` | `int` |
| `isValid()` | `boolean` |
| `getValidationErrorZh()` / `getValidationErrorEn()` | `String` |
| `isMissingRequired()` | `boolean` |

### ExcelReader.ColumnDef 快捷工厂

| 分类 | 工厂 |
|------|------|
| 双语 | `required(zh, en)` / `nullable(zh, en)` / `of(zh, en, nullable)` |
| 只中文 | `requiredZh(zh)` / `nullableZh(zh)` / `ofZh(zh, nullable)` |
| 只英文 | `requiredEn(en)` / `nullableEn(en)` / `ofEn(en, nullable)` |

### ExcelWriter.WriteConfig

| Setter | 类型 | 默认 |
|--------|------|------|
| `setSheetName` | `String` | `"Sheet1"` |
| `setHeaders` | `List<String>` / `String...` | 空 |
| `setListData` | `List<List<String>>` | 空 |
| `setMapData` | `List<Map<String, String>>` | 空 |
| `setTruncateExtraData` | `boolean` | `true` |
| `setTextOverflow` | `TextOverflow` | `WRAP` |
| `setAllAsText` | `boolean` | `true` |
| `setAutoColumnWidth` | `boolean` | `true` |
| `setMinColumnWidth` | `int` | `8` |
| `setMaxColumnWidth` | `int` | `60` |
| `setFreezeHeader` | `boolean` | `true` |

### ExcelWriter 静态方法

| 方法 | 说明 |
|------|------|
| `write(File, WriteConfig)` | 后缀自动识别格式 |
| `write(OutputStream, WriteConfig)` | 默认 xlsx |
| `write(OutputStream, WriteConfig, ExcelFormat)` | 显式指定格式 |
| `writeToBytes(WriteConfig)` | 默认 xlsx |
| `writeToBytes(WriteConfig, ExcelFormat)` | 显式指定格式 |
| `write(Workbook, WriteConfig)` | 追加 Sheet |

### ExcelWriter.ExcelFormat

| 枚举值 | 后缀 | 行×列上限 |
|--------|------|----------|
| `XLS` | `.xls` | 65536 × 256 |
| `XLSX` | `.xlsx` | 1048576 × 16384 |

`detect(String fileName)` → 根据后缀返回 `XLS` / `XLSX` / `null`（大小写不敏感）。

---

**文档版本**：v5.0
- **`async` 语义**：`async = true` 时 `parse(...)` 表头通过后**立即返回**，正文在后台**流式回调**
- **`ParseResult` 结构**：两种模式完全一致，无任何 async 专属字段
- **行数统计**：由调用方在回调中自行统计（`AtomicInteger` / `List` / `CountDownLatch`）
- **回调内存模型**：两种模式都是**逐行流式**，每行处理完立即释放，内存恒定
- **异步异常**：无法传播给调用方，打印到 stderr；需感知异常请用同步模式