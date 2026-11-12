# 通用工具类使用说明（行为变更与注意事项）

> 本文档随代码演进更新，按模块组织；变更历史见 `docs/changelogs/`。

## 1. 概述

本说明覆盖 `com.yishuifengxiao.common.tool` 下的通用工具：AES 加解密、密钥对、IO、时间、缓存、反射、HTTP、分页、ASN.1、脱敏与枚举校验。重点说明近期修复带来的行为变更与使用边界。

不适用范围：jdbc 包（单独见 [jdbc.md](./jdbc.md)）。

## 2. AES 加解密（`codec.AesCbc`）

### 能力与约束

- 算法固定为 `AES/CBC/NoPadding`，**不提供 GCM/AEAD**。
- 无填充模式下，明文长度必须是 16 字节的倍数；如长度不固定，请先用 `padData` 填充。
- 为兼容历史密文，**IV 为 null 或空数组时按 16 字节全零 IV 处理**，加密和解密行为一致。
- 长度为 1~15 的 IV 会抛 `IllegalArgumentException`，不会静默补齐。
- 已移除硬编码的 `AesCbc.key` / `AesCbc.iv` 默认字段，密钥与 IV 必须由调用方显式传入。

### 示例

```java
byte[] key = "0123456789ABCDEF".getBytes(StandardCharsets.UTF_8);
byte[] data = "0123456789ABCDEF".getBytes(StandardCharsets.UTF_8);

// 空 IV：兼容历史密文
byte[] cipher = AesCbc.encrypt(data, key, null);
byte[] plain = AesCbc.decrypt(cipher, key, null);
// plain 与 data 相同

// 非法 IV 长度：快速失败
assertThrows(IllegalArgumentException.class, () -> AesCbc.encrypt(data, key, new byte[8]));
```

`unpadData`（宽松模式）对 null、空数组、填充值大于数据长度的情况均返回安全结果，不会越界；填充不合法时**按无填充处理并返回原数据**。需要校验密文完整性时使用 `unpadDataStrict`，填充长度越界或填充字节不一致会抛 `IllegalArgumentException`。

```java
byte[] padded = AesCbc.padData("hello".getBytes(StandardCharsets.UTF_8));
byte[] plain = AesCbc.unpadDataStrict(padded);                    // "hello"
assertThrows(IllegalArgumentException.class,
        () -> AesCbc.unpadDataStrict(new byte[]{1, 2, 3, 4, 4, 4, 4, 9}));
```

## 3. 密钥对（`codec.KeyPairHelper`）

- ECC 公私钥十六进制长度按曲线字段长度计算：P-256=32 字节、P-384=48 字节、P-521=66 字节；公钥格式为 `04 + X + Y`。
- 数值超出目标长度时抛 `IllegalArgumentException`，不会截断（截断会产生错误密钥）。
- `generateKeyPair` 的参数类型错误直接抛 `IllegalArgumentException`，不会被包装为 `RuntimeException`。
- `printKeyInfo` / `printKeyDetails` / `printKeyPairDetails` 改为日志输出，且**不输出私钥内容**。

### 示例

```java
KeyPair keyPair = KeyPairHelper.generateECCKeyPair("secp384r1");
String publicHex = KeyPairHelper.getPublicKeyHex(keyPair);   // 长度 194
String privateHex = KeyPairHelper.getPrivateKeyDHex(keyPair); // 长度 96
```

## 4. IO（`io.IoUtil`）

- `readFileToBase64(File)` 一次性读取完整文件，不再存在单次 `read` 截断风险。
- `readResourceAsString` 在资源不存在时抛 `UncheckedException("资源不存在: ...")`，默认使用 UTF-8 编码。
- `inputStreamToString` 保留原始换行符（不再用 `lines()` 重新拼接）；无 charset 参数的重载固定使用 UTF-8。
- `copy(InputStream, OutputStream)` **会关闭入参输入流和输出流**；使用固定 4096 字节缓冲区，不依赖 `available()` 的返回值。
- `writeToFile(String, File)` 固定使用 UTF-8 写入，不再依赖平台默认字符集。

```java
String base64 = IoUtil.readFileToBase64(new File("data.bin"));
String text = IoUtil.inputStreamToString(inputStream, StandardCharsets.UTF_8.name());
```

## 5. 时间（`datetime.*`）

- 所有"当前时间"、`Calendar`、`Date` 格式化、`Date` 与 `LocalDateTime` 互转均使用 JVM 默认时区（`ZoneId.systemDefault()` / `Calendar.getInstance()`），不再硬编码任何固定时区。
- `OsUtils` 已移除 `DEFAULT_ZONE`、`ZONEID_OF_CHINA`；`DateTimeUtil` 已移除 `ZONEID_OF_CHINA`，如外部仍引用需改为 `ZoneId.systemDefault()`。
- `JsonUtil` 的 `default_mapper` 与 `none_null_mapper` 均在类初始化时以 `TimeZone.getDefault()` 统一配置 JVM 默认时区；初始化失败时保留兜底实例，`none_null_mapper` 不再为 null。`Response.responseTime` 移除了 `@JsonFormat(timezone = "GMT+8")`，JSON 中的 `Date` 输出随 JVM 默认时区变化。
- `DateTimeUtil.format` 不再使用 `synchronized`（每次创建 formatter，无共享状态），并显式使用 `Locale.getDefault()`。

```java
// 结果随 JVM 默认时区变化，不做任何固定时区转换
LocalDateTime ldt = LocalDateTime.of(2024, 1, 1, 12, 30);
Date date = DateTimeUtil.localDateTime2Date(ldt);
assertEquals(ldt, DateTimeUtil.date2LocalDateTime(date)); // 同一时区往返一致
assertEquals("2024-01-01 12:30", DateTimeUtil.format(date, "yyyy-MM-dd HH:mm"));
```

## 6. 本地缓存（`context.LocalCache`）

- 线程安全，容量上限 1024，按访问顺序进行 LRU 淘汰。
- `put`/`get`/`remove`/`containsKey` 对 null 或空白 key 安全：写入和删除被忽略，读取返回 null。
- `get(key, supplier)` 在 key 为空时直接返回 `supplier` 结果，不写缓存。
- `get(key, supplier)` 命中缓存时直接返回缓存值；泛型在运行期被擦除，返回值的类型不匹配由调用方的隐式强转抛 `ClassCastException`，方法内部不会抛。
- `keys()` 返回快照，遍历时不会被并发修改影响。

## 7. 反射与 JSON（`bean.ClassUtil` / `bean.BeanUtil` / `bean.JsonUtil`）

- 字段缓存使用 `ClassValue`，按类隔离，不会强引用 Class 导致类加载器泄漏。
- `findField(clazz, name)` 找不到字段返回 null（此前可能抛 NPE），支持父类字段。
- `getValue` / `BeanUtil.copy` 会设置 `setAccessible(true)` 后不再恢复，避免并发访问异常。
- `BeanUtil.setFieldsFromMap` 支持给父类字段赋值。
- `JsonUtil.strToBean(json, clazz)`（2 参数）默认 `failOnUnknownProperties=false`，与共享 `default_mapper` 的宽松策略一致；如需严格模式请调用 3 参数版本并传 `true`。
- `JsonUtil.mapper()` 返回进程内共享的只读配置实例（`default_mapper` 与 `none_null_mapper` 均为 `private static final`）；不要在调用方修改其配置，否则会影响所有使用者。

## 8. HTTP（`http.HttpClient`）

- **默认不信任所有 HTTPS 证书**；如需访问自签名服务，显式调用 `trustAllCertificates(true)`。
- `execute()` / `executeAsString()` 失败时抛 `UncheckedException`，异常信息包含 method 与 url，不再返回 null。
- 未知 HTTP 方法抛 `IllegalArgumentException`；支持 GET/POST/PUT/DELETE/HEAD/OPTIONS/TRACE/PATCH。
- `maxBodySize(int)` 可限制响应体大小，默认 0 表示不限制；默认不限制是为了兼容大文件下载，读取不受信任的服务端响应时建议显式设置上限，避免超大响应耗尽内存。
- 请求执行前会把实例请求头**复制为局部副本**再补充 `Content-Type`/`Content-Length` 等字段，执行过程不会修改或污染 `HttpClient` 实例上已设置的请求头，可安全复用同一实例。

```java
String body = HttpClient.instance()
        .url("https://example.com/api")
        .get()
        .executeAsString(); // 失败抛 UncheckedException
```

## 9. 分页（`entity.Page`）

- `Page.toPage(list, size, current)`：size/current 为 null 或 ≤0 时归一化为 10/1。
- `Page.page(total, size)`：size 为 null 或 ≤0 时按 10 计算，避免除零；`total` 为 null 或 ≤0 时统一返回 0 页，与 `Page.of` 对负总数的归一化保持一致（不再出现负总数得到 1 页）。
- `Page.of(data, total, slice)`：slice 为 null 时使用默认分页对象。

## 10. ASN.1（`asn1.Asn1Util`）

- 反射缓存改为 `ClassValue`，避免类加载器泄漏。
- `writePdu` 查找排除静态方法，避免误匹配。
- `toObject(clazz, hex)` 对 null 类型、null/空十六进制抛 `UncheckedException`（与 Javadoc 一致）。
- `generateLuhn(String)` 对 null/空或非纯数字输入抛 `UncheckedException`，避免静默产生错误校验位。
- `BERTag(byte[])` 对 null/空标签数组抛 `IllegalArgumentException`；标签号改用 `long` 累加并显式判溢出，超过 `Integer.MAX_VALUE` 抛 `RuntimeException`，不再溢出成负数。
- `BERTag.getByteList()` 对负数标签号（未设置或溢出）立即抛 `RuntimeException`，避免位移循环永不终止导致 OOM。
- `BERReader.readTag()` 对固定 10 字节标签缓冲区显式限界，超长畸形标签抛 `IOException` 而不是 `ArrayIndexOutOfBoundsException`。
- `BERReader.readOctetString(int)` / `readRestrictedCharacterString(int)` 对负长度（含 BER 不定长 `0x80` 得到的 `-1`）抛 `IOException`，并改为分块读取，避免按外部声明长度一次性分配巨量内存。
- `BERWriter.writeRelativeOID(long[])` 对 null、空数组与负数 arc 抛 `RuntimeException`，与 `writeObjectIdentifier` 的校验保持一致；合法输入仍按 Base-128 编码（如 `{128}` 编码为 `81 00`）。
- `BERReader.readRelativeOID(int)` 对 `nBytes < 1`（空 RELATIVE-OID）与最后一字节仍带延续位的截断编码抛 `RuntimeException`；修复前空输入会返回只含哨兵值 `-1` 的数组。

## 11. 脱敏与枚举校验

- `SensitiveUtil.idCard`：严格合法的身份证做中间脱敏；非严格合法但长度 ≥ 8 的历史数据同样按"前三后四"兜底脱敏。
- `SensitiveUtil.phone`：先剥离 `+86`、空格、连字符等非数字字符，再取末 11 位按"前三后四"脱敏；剥离后长度不足 11 位时原样返回，不改动无法识别的输入。
- `InStringValidator`：`nullable=true` 只放行 null，空白字符串仍需在 `value()` 集合中才能通过。
- `CertNoUtil.isValid`：正则与校验码通过后，还会校验出生日期是否为真实日历日期（如 `20230231` 判定为非法）且不晚于今天；`extractBirthday` 与 `isValid` 契约一致，非法号码抛 `UncheckedException`。

## 12. 线程池（`utils.ExecuteUtil`）

- 内置线程为守护线程，不会阻止 JVM 退出。
- 应用关闭时可调用 `shutdown()` 或 `shutdownNow()`。
- `waitForAll` 等待失败时保留中断标志。

## 13. ECC 签名与密钥协商（`codec.ECC` / `codec.EccKeyAgreement`）

- `ECC.sign` 返回的 R||S 定长签名长度随曲线坐标长度变化：P-256=64 字节、P-384=96 字节、P-521=132 字节；不再固定 64 字节。
- `ECC.verifySignature` 依据公钥曲线判断输入是 R||S 还是 DER 格式，P-256 之外曲线的定长签名可正常验签。
- `parsePublicKeyFromHex` 期望长度为 `坐标字节数 * 4` 个十六进制字符，与曲线匹配；长度不符抛 `IllegalArgumentException`。
- `padBytes` 仅去除 `BigInteger` 的前导符号位零，去掉后仍超长才抛异常，不会静默截断签名值。
- 私钥 D 值提取长度同样随曲线变化，且不再把私钥内容打印到控制台。
- PEM 头尾标记的识别与剥离统一忽略大小写（`-----begin ec private key-----` 等小写写法同样可用），且只剥离标记、不改动 Base64 正文的大小写；协议、域名等匹配仍使用 `Locale.ROOT`，不受 JVM 默认 Locale 影响。
- `EccKeyAgreement`：公钥长度必须为 `1 + 2 * 坐标字节数`；十六进制入参为奇数长度或含非法字符时抛 `IllegalArgumentException`；私钥 D 值必须在 `[1, n-1]` 内；派生长度 `iKeyLen <= 0` 抛 `IllegalArgumentException`；共享秘密为全零时抛 `IllegalArgumentException`（拒绝无效公钥/小子群攻击）；`sShareInfo` 为 null 时按空共享信息处理。

```java
KeyPair p384 = ECC.generateECCKeyPair("secp384r1");
byte[] sig = ECC.sign(p384.getPrivate(), data);          // 96 字节
assertTrue(ECC.verifySignature(p384.getPublic(), data, sig));
```

## 13.1 位置距离与随机工具（`utils.GpsUtil` / `random.RandomUtil`）

- `GpsUtil.distance` 返回米制距离，`GpsUtil.distanceKm` 返回千米制距离，二者均按 `Math.round` 四舍五入取整。
- 千米制不再对小数直接截断：不足 0.5 千米返回 0，0.5 千米及以上返回 1，避免 1 千米内的距离恒为 0。
- 地球半径固定为 `6378.137` 千米，计算采用 Haversine 公式，不依赖系统时区与 Locale。
- 经纬度超出合法范围（经度 ±180、纬度 ±90）或为 `NaN`/`Infinity` 时抛 `IllegalArgumentException`，不再让非法坐标被 `Math.round` 静默转成 0 距离。

```java
long meters = GpsUtil.distance(0, 0, 0, 1);      // 111319（米）
long km = GpsUtil.distanceKm(0, 0, 0, 0.0045);   // 1（千米，四舍五入）
```

- `RandomUtil.generateChineseChar` / `generateChineseText` / `generateTimestamp*` 保留 `synchronized`，并发调用安全。
- 内部复用静态的 `Random` 与 `SecureRandom` 实例，不再每次调用新建随机源；`generateRandomHexString(numBytes)` 输出长度为 `numBytes * 2` 的小写十六进制字符串，`numBytes <= 0` 抛 `IllegalArgumentException`。

```java
String hex = RandomUtil.generateRandomHexString(8); // 16 位小写十六进制
String text = RandomUtil.generateChineseText(5);    // 5 个汉字
```

## 14. 对称加密（`codec.AES` / `codec.DES` / `codec.TripleDES`）

- `AES` / `DES` 显式声明转换方式为 `AES/ECB/PKCS5Padding`、`DES/ECB/PKCS5Padding`，字节结果与 JCE 默认值一致；失败日志不再输出明文或密文。
- `AES` 的密钥由传入字符串经 **SHA-256 派生后截取前 16 字节**作为 AES-128 密钥：同一 key 的加解密可稳定复现。不再使用 `SecureRandom(seed)` 派生密钥——该构造只把种子当作额外熵，同一 key 每次生成的密钥不同，会导致加密与解密使用的密钥不一致。
- `DES` 的密钥统一为"不足 8 个字符补 `0`、超过 8 个字符截断为前 8 个字符"；`encrypt/decrypt/encryptData/decryptData/mac` 全部走同一规则，历史密文兼容（DES 本身只用前 8 字节）。
- `TripleDES.decrypt` 对长度 ≤ 8 字节（不含有效 IV）的密文抛 `IllegalArgumentException`，不再出现负数长度导致的数组越界。

```java
String key = TripleDES.generate3DESKey();
String cipher = TripleDES.encrypt(key, "hello");
assertEquals("hello", TripleDES.decrypt(key, cipher));
```

## 14.1 摘要与消息认证（`codec.RSA` / `codec.SHA256` / `codec.CMAC`）

- `RSA.encrypt/decrypt(String, ...)` 固定使用 UTF-8，中文明文在 Windows/Linux 下结果一致；`Cipher` 显式使用 `RSA/ECB/PKCS1Padding`，与 SunJCE 默认一致，避免跨 JVM 隐式填充差异。
- `SHA256.calculateSHA256(String)` 固定使用 UTF-8，输出为大写十六进制；`calculateSHA256(String)` 与 `calculateSHA256(byte[])` 输入为 null 时返回 null；`calculateSHA256FromHex` 对 null 或非法十六进制抛 `IllegalArgumentException`。
- `CMAC.calculate` 按 RFC 4493 实现：消息长度为 16 字节整数倍时最后一块不填充并使用 K1，其余情况填充 `0x80` 后使用 K2；空消息（`""`）按空数据计算。非法十六进制密钥/数据返回 null。
- `Md5.md5(File)` 不再使用 `synchronized`：每次调用在方法内创建 `MessageDigest`，不同文件可并行计算，不存在共享状态的竞态；文件流仍在 `finally` 中关闭。

## 15. 文本与网络工具（`RegexUtil` / `Hex` / `NetUtil` / `ImageUtil`）

- `RegexUtil` 的 Pattern 缓存改为上限 256 的有界 LRU（`LinkedHashMap` + 同步），空白正则抛 `IllegalArgumentException`。
- `Hex.hexToBytes` 对奇数长度或非法字符记录 WARN 并返回 null（既有契约不变）。
- `Hex.hexToUtf8` 对奇数长度或非法十六进制字符抛 `IllegalArgumentException`；自动去除空白和不可见字符；空串返回 `""`，null 返回 null。
- `Hex.hexToAscii` 使用 ISO-8859-1（Latin-1）解码，覆盖 0x00~0xFF 全字节范围（扩展 ASCII），奇数长度或非法字符抛 `IllegalArgumentException`。
- `Hex.asciiToHex` 不再对输入做 `trim`，首尾空格会被编码为 `20`，与 `hexToAscii` 保持可往返（`hexToAscii(asciiToHex(" A "))` 仍为 `" A "`）。
- `Hex.numberToHexString(number, byteNum)` 在 `byteNum >= 8` 时不再做无符号扩展掩码：`(1L << 64)` 按 64 取模等于 1，修复前掩码为 0，会把所有值转成 0（如 `numberToHexString(5, 8)` 曾返回全 0）。`byteNum < 8` 的既有行为不变；int 负值仍输出完整 4 字节（文档约定输出长度允许超过 `2 * byteNum`）。
- `Hex.padHexLeft(hex, targetBytes)` 恢复对 `0x`/`0X` 前缀与内部空白的支持（此前先调用 `isHex` 校验，导致前缀/空白处理分支不可达而一律返回 null）；剥离前缀后仍需偶数长度与合法十六进制，否则返回 null；返回时保留前缀的原始大小写。
- `HtmlExtract` 解析 XHTML/XML 时禁用 DOCTYPE 声明与外部实体（`disallow-doctype-decl=true`），防止 XXE 攻击。
- `TextExtract.getText` 对相邻块预读做越界保护（越界位置按 0 处理），不再在正文块列表末尾触发 `IndexOutOfBoundsException` 导致 `extract` 静默返回 null。
- `TextExtract` 的连续链接块匹配模式改为 `[^<]{0,30}` 且不再启用 `DOTALL`，避免超大 HTML 上因 `.*` 跨行匹配产生严重回溯；`FREQUENT_URL` 为常量，正则对象预编译为 `static final`。
- `PlaceholderExtractor.replacePlaceholders(input, replacements)`：空白 `input` 原样返回；`replacements` 为 null 时按空映射处理，未匹配到的占位符保持原样（如 `${a}` 不变）。
- `NetUtil.getOutIp()` 设置 3 秒连接/读取超时并使用 UTF-8，避免线程被永久阻塞；已移除调试用 `main` 方法。
- `UrlUtil.urlComplete(referrer, url)` 改为基于 `java.net.URI.resolve` 按 RFC 3986 解析相对地址，正确处理 `/`、`../`、`.` 与协议相对地址（如 `//cdn.com/a.js`）；referrer 非法或 url 为空返回 null。
- `UrlUtil.extractProtocol(url)` 基于 `java.net.URI` 解析并返回**小写**协议名（`HTTP://Example.com` → `http`），支持域名、IP、`localhost` 及任意合法 scheme；空值、空白或无协议时返回 null。
- `UrlUtil.keyword(url)` 取域名末尾两级标签做**精确后缀比较**判断 `com.cn`/`gov.cn`/`edu.cn`，不再用子串 `contains`；`www.acom.cn` 返回 `acom`，`www.mygov.cn` 返回 `mygov`，`com.cn` 这类无主域名的输入返回 null。
- `X509Helper.parseCert` 的 PEM 头尾标记比较忽略大小写（`-----begin certificate-----` 同样可解析），Base64 正文保持大小写敏感；头标记归一化使用 `Locale.ROOT`，不受默认 Locale 影响。
- `HexBitset.bitStringToHex("")` 返回 `"00"`（空 BIT STRING 只有 unused-bits 字节，无数据字节）。
- `HexBitset.hexToBitString(hex)` 返回数据部分的完整二进制字符串，**包含末尾填充位**（`"0780"` → `"10000000"`）；需要精确位长时由调用方按 `nbits` 截断。
- `ValidateUtils` 内部校验在提示信息既非 `RootEnum` 也非 `String`（含 null）时抛 `UncheckedException`，不再静默忽略验证失败。
- `ImageUtil.imageFileToBase64` 使用 `Files.readAllBytes` 读取完整文件，大文件不再被 `available()` 截断。
- `ExcelWriter` 在渲染 Sheet 失败时会关闭已创建的 `Workbook`，避免 XSSF 临时文件与内存泄漏。
- 所有用于协议、标识符、类型名、扩展名、域名、日志级别匹配的大小写归一化统一使用 `Locale.ROOT`（`toLowerCase(Locale.ROOT)` / `toUpperCase(Locale.ROOT)`），不受 JVM 默认 Locale 影响。此前在土耳其语（tr-TR）环境下，`"SQLite"`、`"INI"`、`"info"`、含 `I` 的域名与列名会因 `I -> ı`、`i -> İ` 映射导致识别或匹配失败；`lang.Hex` 的大小写转换只作用于 `0-9A-F`，与 Locale 无关，保持原样。
- `ExcelReader.RowCallback.onRow(int, LinkedHashMap, boolean)` 的 `finished` 参数已恢复：末行回调时置为 `true`；无数据行时也会以空 `rowData` 回调一次并置 `finished=true`。修复前该参数被移除但 `ExcelReaderTest` 仍按三参数契约编写，导致测试源码无法编译。

## 15.1 TLV 与 OID（`lang.TLV` / `lang.TLVFormatter` / `lang.OID` / `lang.TLVUnit`）

- `TLV.toTLV(tag, value)` 的 tag 必须是非空且长度为偶数的十六进制字符串，否则抛 `IllegalArgumentException`；value 可为空，非空时必须是合法十六进制。无标签的 LV 场景请使用新增的 `TLV.toLV(value)`。
- `TLV.toLV(value)` / `buildLengthAndValue` 的 value 字节长度上限为 16777215（BER 长格式 3 字节长度），超出抛 `UncheckedException`，避免产生 `83` 前缀后跟 4 字节长度的畸形报文。
- `TLVUnit.toTLV(value)` 生成 LV 格式（长度 + 值），不再产生 `"null..."` 前缀。
- `TLVFormatter.formatTLVStr` 对 null 入参抛 `IllegalArgumentException`；解析非法十六进制抛 `IllegalArgumentException`；OID 首段按标准规则拆分（`<40 → 0.x`、`<80 → 1.(v-40)`、否则 `2.(v-80)`）。
- `TLVFormatter` 解析 UTCTime/GeneralizedTime 时：带 `<+hhmm>`/`<-hhmm>` 偏移的格式使用 `OffsetDateTime` 保留偏移（输出形如 `2025-02-19 09:26:25 +08:00`），不再被错误标注为 UTC；UTCTime 按 RFC 5280 将 `50-99` 映射到 `1950-1999`、`00-49` 映射到 `2000-2049`。
- `TLV` 的标签/值大小写归一化统一使用 `Locale.ROOT`，标签查找与存储结果在任意默认 Locale 下保持一致。
- `OID.hexToDotNotation` 对首段 base128 编码不完整（如 `"81"`）抛 `IllegalArgumentException`。
- `OID.dotNotationToHex` 对负数节点与超过 `4294967295`（2^32-1，ASN.1 单节点上限）的节点抛 `IllegalArgumentException`，避免 `first*40+second` 溢出或 base128 静默产出空编码。
- `TLVUnit.print()` 改为日志输出，不再写标准输出。
- `TLVFormatter` 解析 REAL 时按 X.690 修正：首字节 `0x40`/`0x41`/`0x42` 分别为 `+INF`/`-INF`/`NaN`；`bit8=1` 按二进制编码、`bit8=0` 按十进制编码（此前判断取反）；二进制 REAL 的指数长度格式取 `bits2-1`、scale factor 取 `bits4-3` 并参与数值计算。
- `TLVFormatter` 的 TLV 长度校验改为 `length < 0 || length > maxPosition - position`：长度字段来自外部输入，修复前 `position + length` 会整数溢出后绕过校验，`0x84 7F FF FF FF` 这类畸形输入会尝试分配约 2GB 数组；现在统一抛 `IllegalArgumentException`。

## 15.2 JDBC 行映射（`jdbc.SimpleRowMapper`）

- 字符串形式的日期时间在 `getTimestamp`/`getDate`/`getTime` 不可用时走回退解析：`LocalDateTime` 支持 ISO、`yyyy-MM-dd HH:mm:ss`、`yyyy-MM-dd HH:mm` 与纯日期（按当天 0 点），`OffsetDateTime`/`ZonedDateTime` 支持对应的 ISO 带偏移格式；解析失败记录 WARN 并返回 null，不再静默丢值。
- 数值转 `BigDecimal` 通过 `new BigDecimal(number.toString())` 构造，避免 `long` 超过 2^53 时经 `double` 中转丢失精度（如 `9007199254740993`）。

```java
// "2025-01-01 12:00:00" 回退解析为 LocalDateTime
assertEquals(LocalDateTime.of(2025, 1, 1, 12, 0, 0), mapper.mapRow(rs, 1).eventTime);
```

## 15.3 时区、URL 参数与数字/日期转换（Round13 扫描修复）

- `ZoneIdDetector.detectDatabaseTimezone`：元数据 `getURL()` 返回 null 时不再抛 NPE；`serverTimezone` 参数名大小写不敏感，且要求以 `?` 或 `&` 作为参数边界；时区值中的 `+` 按字面量保留（如 `GMT+8`）；`Etc/GMT+8`、`America/Argentina/Buenos_Aires` 等合法 IANA 时区优先交由 `ZoneId.of` 校验。
- `UrlUtil.queryStringToMap`：key 含不完整转义序列（如 `a%=1`）时按原始值保留，不再因单个参数导致整体解析失败；空 key 跳过。
- `JdbcUrlHelper`：`ConnInfo.useDefaultTimeZone()` 在参数容器为空时惰性初始化；解析与构建对参数 key/value 使用同一规则编解码；`+` 按字面量处理，空格编码为 `%20`（不再编码为 `+`），解析 `GMT+8` 与 `GMT%2B8` 均得到 `GMT+8`，构建结果为 `GMT%2B8`。
- `NumberUtil.parseInt` / `parseLong`：先截断小数部分保持原有行为，再用 `intValueExact`/`longValueExact` 拒绝溢出，超范围返回 null，不再静默回绕（如 `parseInt("99999999999") == null`）。
- `NumberUtil.gt/gte/lt/lte/equals` 对 `Double.NaN`、`Float.NaN` 与 `Infinity` 不再抛未捕获 `NumberFormatException`：NaN 与任何值（包括自身）比较均返回 false（无数学意义）；`Infinity` 回退到原始 `doubleValue()` 比较，语义与 `Double.compare` 一致。
- `SimpleRowMapper`：日期时间回退支持字符串按数据库时区（缺省系统时区）转换为 `Instant`；驱动直接返回 `LocalDateTime`/`LocalDate`/`Timestamp` 等对象时，可按目标类型互转，避免反射赋值类型不匹配被丢弃。
- `IdWorker.uuid()` 无共享状态，已移除 `synchronized`；`ASNValueReader` 的 HEX 解析对奇数长度与非法字符抛 `IllegalArgumentException`。

```java
assertNull(NumberUtil.parseInt("99999999999"));                    // 溢出返回 null
assertEquals(ZoneId.of("Asia/Shanghai"),
        ZoneIdDetector.detectDatabaseTimezone(connection));        // 大写参数名可识别
assertEquals("GMT+8", JdbcUrlHelper.parseJdbcUrl(
        "jdbc:mysql://h:3306/db?serverTimezone=GMT+8").getParameters().get("serverTimezone"));
```

## 16. 集合工具（`collections.CollUtil` / `collections.ArrayUtil`）

- `CollUtil.get(List, int)` 对 null 元素返回 `Optional.empty()`，与数组版本一致。
- `CollUtil.toArray(Collection)` 返回运行时类型为 `Object[]` 的数组，不做运行时类型转换；需要具体类型数组请直接调用 `Collection.toArray(T[])`。
- `ArrayUtil.union(src, target, predicate)` 严格按传入的 `predicate` 判重（保留首次出现的元素顺序），不再使用默认 `equals`。
- `ArrayUtil.difference(src, target, predicate)` 要求 `predicate` 非空，否则抛 `IllegalArgumentException`；`src` 为 null 时返回 `target` 中过滤 null 后的新集合。
- `CollUtil.asArray(T... vals)` 在显式传入 null 时抛 `IllegalArgumentException`，不再返回 `Object[]` 以避免调用点 `ClassCastException`。

## 16.1 日末时间、图片 Base64 与数字提取（`datetime.*` / `io.ImageUtil` / `text.RegexUtil`）

- `DateOffsetUtil.getDayEnd(Date)` 和 `LocalDateTimeUtil.getDayEnd(LocalDateTime)` 统一返回日末 `23:59:59.999`，不截断到秒；日期边界使用 JVM 默认时区。
- `ImageUtil.base64ToBufferedImage(String)` 会移除字符串开头的 `data:image/xxx;base64,` Data URI 前缀，可与 `imageToBase64WithPngHeader` 的输出直接组合。
- `ImageUtil.imageToBase64WithPngHeader(BufferedImage)` 在转换失败时返回 null，不再拼出 `data:image/png;base64,null`。
- `RegexUtil.extractAllNumber(String)` 先按完整数字 token 切分，再丢弃含多个小数点的非法 token；`1.2.3` 不再被拆成 `1.2` 和 `0.3`。

```java
// 日末毫秒固定为 999
LocalDateTime dayEnd = LocalDateTimeUtil.getDayEnd(LocalDateTime.now());
assertEquals(999_999_000, dayEnd.getNano());

// 图片 Base64 Data URI 往返
BufferedImage image = ImageUtil.base64ToBufferedImage(ImageUtil.imageToBase64WithPngHeader(source));
assertNotNull(image);

// 多数值点 token 被整体丢弃
assertTrue(RegexUtil.extractAllNumber("1.2.3").isEmpty());
```

## 16.2 智能卡（`smartcard.SmartCard`）

- `connect(String)` 在已连接状态下切换到其他读卡器时，会先 `disconnect()` 旧连接再建立新连接，避免 APDU 仍发往旧卡片而 `terminalName` 已变更。
- 指定终端不存在时先释放已有物理连接再抛 `UncheckedException`，不再把 `card` 置空造成连接泄漏。
- 重复 `connect` 同一读卡器为空操作，直接返回当前实例。

```java
SmartCard card = new SmartCard().connect("Reader A");
card.connect("Reader B");   // 先断开 Reader A，再连接 Reader B
```

## 17. 相关链接
- 源码：`src/main/java/com/yishuifengxiao/common/tool/`
- 回归测试：`src/test/java/com/yishuifengxiao/common/tool/Round6FixRegressionTest.java`、`src/test/java/com/yishuifengxiao/common/tool/Round11ScanFixRegressionTest.java`、`src/test/java/com/yishuifengxiao/common/tool/Round12ScanFixRegressionTest.java`、`src/test/java/com/yishuifengxiao/common/tool/Round13ScanFixRegressionTest.java`、`src/test/java/com/yishuifengxiao/common/tool/Round14ScanFixRegressionTest.java`、`src/test/java/com/yishuifengxiao/common/tool/Round15ScanFixRegressionTest.java`、`src/test/java/com/yishuifengxiao/common/tool/Round16ScanFixRegressionTest.java`、`src/test/java/com/yishuifengxiao/common/tool/Round17ConvergentScanRegressionTest.java`、`src/test/java/com/yishuifengxiao/common/tool/Round18ScanFixRegressionTest.java`、`src/test/java/com/yishuifengxiao/common/tool/codec/CodecCompatibilityTest.java`、`src/test/java/com/yishuifengxiao/common/tool/CommonUtilsReviewFixTest.java`
- 变更记录：`docs/changelogs/202611121957-fix-round18-scan-fixes.md`
- 历史变更记录：`docs/changelogs/202611121412-fix-round17-convergent-scan.md`、`docs/changelogs/202611121400-fix-round16-scan-fixes.md`
