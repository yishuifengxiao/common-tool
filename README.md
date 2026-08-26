# common-tool

`common-tool` 是一套面向 Java 业务开发的通用工具库，覆盖 Bean/JSON、集合、时间、加解密与证书、HTTP、IO、JDBC、文本解析、ASN.1/TLV、智能卡、校验与脱敏等常见场景。多数 API 对 `null` 做了空安全处理，可直接在 Spring Boot / 普通 Java 项目中引用。

- **当前版本**：`9.0.3`
- **JDK**：21
- **坐标**：`com.yishuifengxiao.common:common-tool`
- **仓库**：[Gitee](https://gitee.com/zhiyubujian/tool)
- **协议**：Apache License 2.0

部分能力依赖可选组件（未引入时对应模块不可用）：

| 能力 | 依赖 | Maven 作用域 |
|------|------|----------------|
| Bean Validation 分组校验 | `hibernate-validator` | optional |
| JDBC 实体映射 / `JdbcHelper` | `spring-jdbc`、`jakarta.persistence-api` | optional |

核心依赖（会随本库引入）：Jackson、Apache Commons Lang3、Jsoup、dom4j、json-path、SLF4J / Logback。

## 快速开始

```xml
<dependency>
    <groupId>com.yishuifengxiao.common</groupId>
    <artifactId>common-tool</artifactId>
    <version>9.0.3</version>
</dependency>
```

最新版本见 [Maven Central](https://central.sonatype.com/artifact/com.yishuifengxiao.common/common-tool)。

---

## 模块总览

根包：`com.yishuifengxiao.common.tool`

| 模块 | 包 | 作用 |
|------|----|------|
| Bean | `bean` | 属性拷贝、Map/Bean 互转、JSON、反射读字段、源码编译 |
| 集合 | `collections` | 空安全集合操作、交并差 |
| 缓存 | `context` | 进程内 ConcurrentHashMap 缓存 |
| 时间 | `datetime` | Date / LocalDateTime 偏移、解析与格式化 |
| 加解密 | `codec` | AES/DES/3DES、RSA/ECC、摘要、CMAC、X.509、密钥对 |
| 实体 | `entity` | 统一响应、分页、键值对、布尔三态、枚举基接口 |
| 异常 | `exception` | 受检 / 非受检业务异常 |
| HTTP | `http` | 基于 Jsoup 的客户端、URL 解析、User-Agent |
| IO | `io` | 文件与流、Base64、图片、安静关闭 |
| JDBC | `jdbc` | 基于 JPA 注解的简易 CRUD、URL 组装、POJO 生成 |
| 语言 | `lang` | 布尔/数字、Hex、OID、TLV |
| 随机 | `random` | 雪花 ID、UUID、随机中文/十六进制 |
| 脱敏 | `sensitive` | 姓名/手机/身份证/密码脱敏与 Jackson 注解 |
| 智能卡 | `smartcard` | PC/SC 读卡器与 APDU |
| 文本 | `text` | 命名转换、正则抽取、HTML/正文、`${}` 占位符 |
| 杂项 | `utils` | 断言、校验抛错、身份证、线程池、GPS、网卡、OS |
| 校验 | `validate` | Bean Validation 工具与取值范围注解 |
| ASN.1 | `asn1` | BER 读写与对象编解码 |

---

## 1. Bean 操作（`bean`）

### API 列表

**`BeanUtil`**

| 方法 | 说明 |
|------|------|
| `copy(source, target)` | 按同名、兼容类型字段把源对象属性拷到目标对象 |
| `objectToByte(obj)` / `byteToObject(...)` | Java 序列化与反序列化 |
| `mapToBean(map, clazz)` / `beanToMap(data)` | Map 与 Bean 互转 |
| `cloneVal(val)` / `deepClone(val)` | 浅克隆 / JSON 深克隆 |

**`JsonUtil`**

| 方法 | 说明 |
|------|------|
| `mapper()` | 获取内部 `ObjectMapper` |
| `strToBean` / `strToList` | JSON 转对象或列表 |
| `extract` / `extractList` | 用 JsonPath 抽取字段 |
| `jsonToMap` | JSON 对象转 Map |
| `isJSON` / `isJSONObject` / `isJSONArray` | 判断 JSON 形态 |
| `toJSONString` / `prettyPrinter` | 序列化（可控制是否输出 null） |
| `deepClone(val)` | JSON 深拷贝 |

**`ClassUtil`**：`fields`、`extractValue`、`extractNestedValue`（如 `a.b.c`）、`getValue`、`findField`。

**`CustomStringJavaCompiler`**：把 Java 源码字符串编译为字节码。

### 示例

```java
User src = new User();
src.setName("yi");
User dest = BeanUtil.copy(src, new User());

Map<String, Object> map = BeanUtil.beanToMap(src);
User fromMap = BeanUtil.mapToBean(map, User.class);

String json = JsonUtil.toJSONString(src);
User bean = JsonUtil.strToBean(json, User.class);
String name = JsonUtil.extract("{\"user\":{\"name\":\"yi\"}}", "$.user.name", String.class);
```

---

## 2. 集合（`collections`）

### API 列表

**`CollUtil`**

| 方法 | 说明 |
|------|------|
| `toMap(k1, v1, k2, v2, ...)` | 偶数个参数构建 LinkedHashMap，奇数个抛 `IllegalArgumentException` |
| `stream(...)` | Collection / 数组 / Stream 转串行流，`null` 得到空流 |
| `toList` / `toArray` / `toSet` | 数组与集合互转 |
| `first` / `last` / `get` | 安全取值，返回 `Optional` |
| `asList` / `asSet` / `asArray` | 可变参数创建可变集合（非 `Arrays.asList` 固定长度） |
| `merge(collections...)` | 合并多个集合 |
| `forEach(collection, (index, item) -> ...)` | 带下标遍历 |
| `isEmpty` / `isNotEmpty` | 支持 Collection、数组、Map、`Page` |
| `isAllEmpty` / `isAnyEmpty` / `isNoneEmpty` | 多集合空判断 |
| `isOnlyOneElement` / `gteOneElement` / `gtOneElement` / `ltOneElement` / `lteOneElement` | 元素个数判断 |
| `size` | 空安全 size |

**`ArrayUtil`**：`intersection` / `union` / `difference`，集合或数组，比较规则由 `BiPredicate` 指定。

### 示例

```java
List<String> list = CollUtil.asList("a", "b", "c");
CollUtil.stream(list).forEach(System.out::println);
String first = CollUtil.first(list).orElse(null);

Map map = CollUtil.toMap("k1", "v1", "k2", 2);

Collection<String> inter = ArrayUtil.intersection(
        CollUtil.asList("a", "b"),
        CollUtil.asList("b", "c"),
        Objects::equals);
```

---

## 3. 本地缓存（`context`）

**`LocalCache`**：进程内 `ConcurrentHashMap`。

| 方法 | 说明 |
|------|------|
| `put(value)` | key 为 `value.getClass().getName()` |
| `put(key, value)` / `get(key)` | 显式 key |
| `get(key, supplier)` | 不存在时用 Supplier 填充 |
| `get(Class)` / `remove(Class)` | 按类型名存取 |
| `keys` / `containsKey` / `clear` | 键集合与清理 |

```java
LocalCache.put("user", user);
User cached = (User) LocalCache.get("user");
Config cfg = LocalCache.get("config", Config::loadDefault);
```

---

## 4. 日期时间（`datetime`）

**`DateOffsetUtil`**（`java.util.Date`）与 **`LocalDateTimeUtil`**（`java.time.LocalDateTime`）能力对齐：

| 方法 | 含义 |
|------|------|
| `todayStart` / `yesterdayStart` / `yesterdayEnd` | 今天 00:00:00、昨天起止 |
| `last2DayStart` / `last7DayStart` / `last14DayStart` | 前天 / 7 天前 / 14 天前 0 点 |
| `mondayStart` / `lastMondayStart` / `last2MondayStart` / `mondayStart(offsetWeeks)` | 周一 0 点 |
| `getMonday` / `getMondayStart` | 指定日期所在周一 |
| `dayStart` / `dayEnd` | 相对今天偏移的日界 |
| `monthStart` / `monthStart(offset)` / `lastMonthStart` / `last2MonthStart` | 月初 |
| `getDayStart` / `getDayEnd` / `getMonthStart` / `getYearStart` | 任意时刻的日/月/年起点 |
| `LocalDateTimeUtil.parse(timeStr, patterns...)` | 按候选格式解析 |

**`DateTimeUtil`**：`now`、`date2LocalDateTime`、`localDateTime2Date`、`getTime`、`parse` / `parseDate`、`format` / `formatDate`。

```java
Date today = DateOffsetUtil.todayStart();
LocalDateTime weekStart = LocalDateTimeUtil.mondayStart();

LocalDateTime ldt = DateTimeUtil.date2LocalDateTime(new Date());
String s = DateTimeUtil.format(ldt, "yyyy-MM-dd HH:mm:ss");
LocalDateTime parsed = DateTimeUtil.parse("2026-08-24 21:00:00", "yyyy-MM-dd HH:mm:ss");
```

---

## 5. 加解密与证书（`codec`）

| 类 | 主要 API | 说明 |
|----|----------|------|
| `AES` | `encrypt` / `decrypt`（可省略 key） | AES，结果 Base64；无 key 用内置默认密钥 |
| `AesCbc` | `encrypt` / `decrypt` / `generateIV` / `padData` | AES-CBC，十六进制或字节 |
| `DES` | `encrypt` / `decrypt`、`encryptData` / `decryptData`、`mac` | DES；`*Data` 失败抛异常，其余失败常返回 null |
| `TripleDES` | `generate3DESKey`、`encrypt`、`decrypt` | 3DES，密钥为 Base64 |
| `Md5` | `md5` / `md5Short`（字符串或 File） | 32 / 16 位小写 MD5 |
| `SHA256` | `calculateSHA256` / `calculateSHA256FromHex` | SHA-256 |
| `CMAC` | `calculate(key, data)` | AES-CMAC |
| `RSA` | `generateKeyPair`、加解密、密钥字符串互转 | RSA |
| `ECC` | 密钥生成、签名验签、PEM/Hex 解析、ECDH、证书匹配 | 椭圆曲线，默认曲线 secp256r1 |
| `EccKeyAgreement` | `eccKeyAgreement(...)` | ECDH 共享密钥 |
| `KeyPairHelper` | RSA/DSA/ECC 密钥对生成与 Hex 导出 | 密钥工厂 |
| `X509Helper` | `parseCert`、公钥/曲线 OID/SKID 抽取、链校验、PEM/Hex 转换 | X.509 |

```java
String cipher = AES.encrypt("my-secret-key", "hello");
String plain = AES.decrypt("my-secret-key", cipher);

String md5 = Md5.md5("payload");
String sha = SHA256.calculateSHA256("payload");

KeyPair rsa = RSA.generateKeyPair(2048);
String enc = RSA.encrypt("hello", rsa.getPublic());
String dec = RSA.decrypt(enc, rsa.getPrivate());

X509Certificate cert = X509Helper.parseCert(pemOrBase64);
String pubHex = X509Helper.extractSubjectPublicKeyHex(cert);
```

---

## 6. 通用实体（`entity`）

| 类型 | 说明 |
|------|------|
| `Response<T>` | 统一接口响应：`code`、`msg`、`data` |
| `Slice` | 分页参数：默认 `size=10`、`current=1`，`startOffset` / `endOffset` |
| `Page<S>` | 分页结果：`data`、`total`、`pages`，`map` 转换元素类型 |
| `PageQuery<T>` | `Slice` + 查询条件 `query` |
| `KeyValue<K,V>` / `StringKeyValue<T>` | 键值对 |
| `BoolStat` | 布尔三态：TRUE / FALSE / UNKNOWN |
| `RootEnum<T>` | 枚举基接口，`code()`、`equalCode`、`RootEnum.of(clazz, code)` |

**`Response` 工厂方法**

| 方法 | HTTP 语义 |
|------|-----------|
| `suc()` / `suc(data)` / `suc(msg, data)` | 200 |
| `badParam()` / `badParam(msg)` / `badParam(msg, data)` | 400 |
| `unAuth()` / `unAuth(msg)` / `unAuth(msg, data)` | 401 |
| `notAllow()` / `notAllow(msg)` | 403 |
| `notFound()` | 404 |
| `error()` / `error(msg)` / `error(msg, data)` | 500 |
| `of(code, msg, data)` | 自定义 |

常量见 `Response.Const`（如 `CODE_OK = 200`）。

```java
return Response.suc(user);
return Response.badParam("name 不能为空");

Page<User> page = Page.of(users, 100, 10, 1);
Page<UserVO> voPage = page.map(this::toVo);

PageQuery<User> q = PageQuery.of(new User().setName("yi"), 10, 1);
```

---

## 7. 异常（`exception`）

| 类 | 类型 | 工厂 |
|----|------|------|
| `CustomException` | 受检异常 | `of(msg)` / `of(msg, context)` |
| `UncheckedException` | 运行时异常 | `of(msg)` / `of(code, msg)` / `of(msg, context)` |

二者均可设置 `code`、`context`。`Assert`、`ValidateUtils` 失败时抛出 `UncheckedException`。

---

## 8. HTTP（`http`）

**`HttpClient`**（Jsoup）：链式构建请求；HTTPS 使用信任全部证书的 SSLContext（仅适合明确信任的内网/测试环境）。

| 方法 | 说明 |
|------|------|
| `instance()` | 新建客户端 |
| `url` / `get` / `post` / `put` / `delete` / `method` | 地址与动词 |
| `form()` / `json()` | Content-Type |
| `data(Map)` / `data(String body)` | 表单或原始 body |
| `addHeader` / `setHeaders` / `cookies` / `timeout` / `userAgent` | 头、Cookie、超时 |
| `execute()` / `executeAsString()` | 执行 |
| 静态 `get` / `postForm` / `postJson` / `execute` | 一次性调用 |

**`UrlUtil`**：`extractProtocolAndHost`、`extractDomain`、`extractProtocol`、`matchHttpRequest`、`urlComplete`、`queryStringToMap`、`keyword`。

**`UserAgent`**：枚举常见 UA，`autoUserAgent()` 随机一个。

```java
String html = HttpClient.get("https://example.com");

String body = HttpClient.instance()
        .url("https://api.example.com/users")
        .json()
        .addHeader("Authorization", "Bearer token")
        .data("{\"name\":\"yi\"}")
        .post()
        .executeAsString();

Map<String, String> qs = UrlUtil.queryStringToMap("a=1&b=2");
```

---

## 9. IO（`io`）

**`IoUtil`**：`suffix`、`fileToByteArray`、`inputStreamToByteArray` / `inputStreamToString`、`inputStreamToFile`、`readResourceAsString`、`copy`、`writeToFile`、`readFileAsString`、`base64ToFile`、`readFileToBase64`。

**`ImageUtil`**：图片文件 / `BufferedImage` 与 Base64 互转。

**`CloseUtil`**：`close(closeables...)`，可选择先 `flush`。

```java
String text = IoUtil.readFileAsString(new File("data.txt"));
IoUtil.writeToFile("hello", new File("out.txt"));
String b64 = ImageUtil.imageFileToBase64("avatar.png");
CloseUtil.close(in, out);
```

---

## 10. JDBC（`jdbc`，需 `spring-jdbc`）

实体建议使用 JPA 注解：`@Entity` / `@Table`、`@Id`、`@Column`。无注解时按类名、字段名映射。

**`JdbcHelper`**（基于 `NamedParameterJdbcTemplate`）

| 方法 | 说明 |
|------|------|
| `findByPrimaryKey` | 按主键查 |
| `count` / `findOne` / `find` | 按非空字段拼条件，`likeMode` 控制模糊匹配 |
| `findPage` | 分页查询 |
| `insert` / `saveOrUpdate` / `saveAll` | 插入、存在则更新、批量 |
| `updateByPrimaryKey` / `updateByPrimaryKeySelective` | 全量 / 非空字段更新 |
| `deleteByPrimaryKey` / `deleteByPrimaryKeys` | 按主键删 |
| `find` / `findOne` / `findPage` / `update` / `batchUpdate` | 自定义命名参数 SQL |
| `Order.asc("col")` / `Order.desc("col")` | 排序 |

`Result`：`getKey()` / `getKeyAsLong()` / `keyHolder()`。

其它类：`FieldExtractor`、`SimpleRowMapper`、`JdbcUrlHelper`（链式拼 JDBC URL）、`ZoneIdDetector`、`PojoGenerator`（从表结构生成实体源码）、`JdbcError`、`FieldValue`。

```java
JdbcHelper helper = new JdbcHelper(jdbcTemplate);

User u = helper.findByPrimaryKey(User.class, 1L);
List<User> list = helper.find(new User().setName("yi"), true, JdbcHelper.Order.desc("id"));
Page<User> page = helper.findPage(new User(), false, Slice.of(10, 1));

helper.insert(new User().setName("yi"));
helper.updateByPrimaryKeySelective(u);
helper.deleteByPrimaryKey(User.class, 1L);

List<User> custom = helper.find(User.class,
        "SELECT * FROM sys_user WHERE age > :age",
        new MapSqlParameterSource("age", 18));

String url = JdbcUrlHelper.parseJdbcUrl(rawUrl)
        .useUtf8mb4()
        .useBeijingTimeZone()
        .enableBatch()
        .buildJdbcUrl();
```

---

## 11. 语言工具（`lang`）

| 类 | 主要 API |
|----|----------|
| `BoolUtil` | `parse`、`isTrueText` / `isFalseText`、`boolToInt`、`twoStateTrue` / `twoStateFalse` |
| `NumberUtil` | `parseInt` / `parseLong` / `parseDouble` / `parseFloat` / `parseToBigDecimal` / `parseHex`；`gt`/`gte`/`lt`/`lte`/`equals`；`gtZero` 等；`containsAny` |
| `Hex` | 十六进制与 UTF-8/字节/Base64/二进制互转、补齐、异或 |
| `HexBitset` | Hex 与 `BitSet` / 比特串互转 |
| `OID` | OID 点分与 Hex 互转 |
| `TLV` / `TLVUnit` / `TLVFormatter` | BER-TLV 抽取、组装、格式化 |

```java
Integer n = NumberUtil.parseInt("12", 0);
boolean ok = NumberUtil.gtZero(n);

String hex = Hex.utf8ToHex("abc");
String text = Hex.hexToUtf8(hex);

String tlv = TLV.toTLV("80", "010203");
String val = TLV.extractVal("80", tlv);
```

---

## 12. 日志（`log`）

**`LogLevelUtil.setLevel(loggerName, logLevel)`**：运行时改 Logback 级别（如 `info`、`debug`）。

**`LogInfo`**：可序列化的日志信息载体。

```java
LogLevelUtil.setLevel("com.yishuifengxiao", "debug");
```

---

## 13. 随机与 ID（`random`）

**`IdWorker`**：`snowflakeId()`、`snowflakeStringId()`、`uuid()`；也可 `new IdWorker(workerId, datacenterId).nextId()`。

**`RandomUtil`**：`generateChineseChar`、`generateChineseText(len)`、`generateTimestamp`、`generateTimestampWithPrefix`、`generateTimestampWithRandom`、`generateRandomHexString(numBytes)`。

```java
long id = IdWorker.snowflakeId();
String uuid = IdWorker.uuid();
String hex = RandomUtil.generateRandomHexString(16);
```

---

## 14. 脱敏（`sensitive`）

**`SensitiveUtil`**：`name`、`idCard`、`phone`、`password`。

**`@Sensitive(SensitiveEnum.xxx)`**：Jackson 序列化时自动脱敏。枚举：`NAME`、`MOBILE_PHONE`、`ID_CARD`、`PASSWORD`。实现类：`SensitiveSerialize`。

```java
SensitiveUtil.phone("13812345678"); // 138****5678

public class UserVO {
    @Sensitive(SensitiveEnum.MOBILE_PHONE)
    private String phone;
}
```

---

## 15. 智能卡（`smartcard`）

**`SmartCard`**：列举读卡器、连接/断开、逻辑通道、发送 APDU（十六进制）、SW=0x61 时自动 GET RESPONSE、81E2 通道、读取 eID。

```java
SmartCard card = new SmartCard();
List<String> terminals = card.getCardTerminalNames();
card.connect(terminals.get(0));
SmartCard.ApduResult r = card.transmit("00A4040000", true);
card.disconnect();
```

---

## 16. 文本（`text`）

| 类 | 主要 API |
|----|----------|
| `TextUtil` | 去空白与不可见字符、去注释、首字母大小写、驼峰/下划线、左右填充 |
| `RegexUtil` | 编译缓存、`match`/`find`/`extract`/`extractAll`，中文/数字/日期/IPv4/URL 抽取 |
| `HtmlExtract` | CSS / XPath 抽 HTML 或 XML |
| `TextExtract` | 从 HTML 抽正文 |
| `PlaceholderExtractor` | `${name}` 提取与替换 |

```java
String col = TextUtil.underscoreName("userName"); // user_name
List<String> urls = RegexUtil.extractAllUrls(html);
String title = HtmlExtract.extractAnyTextByCss(html, "h1");

List<String> keys = PlaceholderExtractor.extractPlaceholders("Hello ${name}");
String out = PlaceholderExtractor.replacePlaceholders("Hello ${name}", Map.of("name", "yi"));
```

---

## 17. 杂项工具（`utils`）

| 类 | 作用 |
|----|------|
| `Assert` | 断言失败抛 `UncheckedException`（数字与 0 比较、null/blank、集合空、仅一个元素等） |
| `ValidateUtils` | 布尔断言、`orElseThrow`、从异常抽取信息、按 `RootEnum` 抛错 |
| `CertNoUtil` | 身份证校验、提取生日 |
| `ExecuteUtil` | 公共线程池、异步执行、`executeFirstMatch`、`waitForAll` |
| `GpsUtil` | 两点距离（米 / 千米） |
| `NetUtil` | 网卡信息、MAC、公网 IP |
| `OsUtils` | 操作系统判断、临时目录、工作目录、`Platform` |

```java
Assert.isNotBlank("name 不能为空", name);
Assert.gtZero("id 必须大于 0", id);

ValidateUtils.isTrue(user != null, "用户不存在");

boolean valid = CertNoUtil.isValid("110101199001011234");
long meters = GpsUtil.distance(116.4, 39.9, 121.4, 31.2);

ExecuteUtil.execute(() -> doWork());
```

---

## 18. 参数校验（`validate`）

**`BeanValidator`**：`validate(bean)` / `validate(bean, group)` 返回违例集合；`validateResult` 只返回第一条错误文案。

**`Group`**：`Create`、`Update`、`Delete`、`Query`、`All`。

**约束注解**（Jakarta Validation）

| 注解 | 目标类型 | 含义 |
|------|----------|------|
| `@InInt` | Integer | 必须在 `value` 数组中，`nullable` 默认 true |
| `@InLong` | Long | 同上 |
| `@InString` | String | 必须在给定字符串集合中 |
| `@InBool` | Integer | 必须是合法布尔码（配合 `BoolStat`） |

```java
public class CreateUser {
    @NotBlank(groups = Group.Create.class)
    private String name;

    @InInt(value = {1, 2, 3}, message = "非法状态")
    private Integer status;
}

Set<ConstraintViolation<CreateUser>> vs = BeanValidator.validate(cmd, Group.Create.class);
String msg = BeanValidator.validateResult(cmd);
```

---

## 19. ASN.1（`asn1`）

| 类 | 作用 |
|----|------|
| `Asn1Util` | Hex/UTF-8 转 `BERReader`；对象 `writePdu`/`readPdu` 编解码；Luhn、ICCID、MCC/MNC |
| `BERReader` / `BERWriter` | BER 标签、长度与基础类型读写 |
| `ASNValueReader` / `ASNValueWriter` | 高层 ASN.1 值读写 |
| `BERTag` | BER 标签 |

```java
BERReader reader = Asn1Util.hexToBERReader(hexPdu);
String hex = Asn1Util.toHexString(pduObject);
MyPdu obj = Asn1Util.toObject(MyPdu.class, hex);
```

`toObject` / `toHexString` 要求目标类型提供约定的 `readPdu` / `writePdu` 方法。

---

## 环境要求

- JDK 21+
- 仅使用 Bean/集合/时间等核心工具时，不必引入 Spring
- 使用 `JdbcHelper` 时需要可用的 `DataSource` 或 `JdbcTemplate`
- 使用 `BeanValidator` / `@InInt` 等时需要 Hibernate Validator 实现
