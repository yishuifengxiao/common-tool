# 快速启动
本工具包主要集成了目前在项目开发过程中个人经常会使用到的一些工具类，对工具类进行了一下简单的封装。该工具常用的工具有：

| 工具类路径 | 作用说明 |
| ------------------------------------------------------------ | :--------------------: |
| [com.yishuifengxiao.common.tool.bean](https://apidoc.gitee.com/zhiyubujian/tool/com/yishuifengxiao/common/tool/bean/package-summary.html) |                        java bean 操作工具|
| [com.yishuifengxiao.common.tool.collections](https://apidoc.gitee.com/zhiyubujian/tool/com/yishuifengxiao/common/tool/collections/package-summary.html) | java 集合操作工具      |
| [com.yishuifengxiao.common.tool.context](https://apidoc.gitee.com/zhiyubujian/tool/com/yishuifengxiao/common/tool/context/package-summary.html) | 数据存储工具           |
| [com.yishuifengxiao.common.tool.converter](https://apidoc.gitee.com/zhiyubujian/tool/com/yishuifengxiao/common/tool/converter/package-summary.html) | 数据转换工具           |
| [com.yishuifengxiao.common.tool.datetime](https://apidoc.gitee.com/zhiyubujian/tool/com/yishuifengxiao/common/tool/datetime/package-summary.html) | 日期时间工具           |
| [com.yishuifengxiao.common.tool.encoder](https://apidoc.gitee.com/zhiyubujian/tool/com/yishuifengxiao/common/tool/encoder/package-summary.html) | 加解密工具             |
| [com.yishuifengxiao.common.tool.entity](https://apidoc.gitee.com/zhiyubujian/tool/com/yishuifengxiao/common/tool/entity/package-summary.html) | 基础通用对象           |
| [com.yishuifengxiao.common.tool.exception](https://apidoc.gitee.com/zhiyubujian/tool/com/yishuifengxiao/common/tool/exception/package-summary.html) | 自定义异常             |
| [com.yishuifengxiao.common.tool.exception.constant](https://apidoc.gitee.com/zhiyubujian/tool/com/yishuifengxiao/common/tool/exception/constant/package-summary.html) | 异常错误码常量         |
| [com.yishuifengxiao.common.tool.http](https://apidoc.gitee.com/zhiyubujian/tool/com/yishuifengxiao/common/tool/http/package-summary.html) | HTTP操作工具           |
| [com.yishuifengxiao.common.tool.io](https://apidoc.gitee.com/zhiyubujian/tool/com/yishuifengxiao/common/tool/io/package-summary.html) | IO流和文件操作工具     |
| [com.yishuifengxiao.common.tool.lang](https://apidoc.gitee.com/zhiyubujian/tool/com/yishuifengxiao/common/tool/lang/package-summary.html) | 常见数据类型的封装工具 |
| [com.yishuifengxiao.common.tool.log](https://apidoc.gitee.com/zhiyubujian/tool/com/yishuifengxiao/common/tool/log/package-summary.html) | 日志工具               |
| [com.yishuifengxiao.common.tool.random](https://apidoc.gitee.com/zhiyubujian/tool/com/yishuifengxiao/common/tool/random/package-summary.html) | 随机工具               |
| [com.yishuifengxiao.common.tool.sensitive](https://apidoc.gitee.com/zhiyubujian/tool/com/yishuifengxiao/common/tool/sensitive/package-summary.html) | 脱敏工具               |
| [com.yishuifengxiao.common.tool.utils](https://apidoc.gitee.com/zhiyubujian/tool/com/yishuifengxiao/common/tool/utils/package-summary.html) | 自定义工具             |
| [com.yishuifengxiao.common.tool.validate](https://apidoc.gitee.com/zhiyubujian/tool/com/yishuifengxiao/common/tool/validate/package-summary.html) | 自定义校验工具         |

工具包已经发布到maven中央仓库，使用方法如下：

```xml
<dependency>
	<groupId>com.yishuifengxiao.common</groupId>
	<artifactId>common-tool</artifactId>
	<version>4.3.0</version>
</dependency>
```

最新版的版本号参见 [https://mvnrepository.com/artifact/com.yishuifengxiao.common/common-tool](https://mvnrepository.com/artifact/com.yishuifengxiao.common/common-tool)

工具类说明文档的地址为 [https://apidoc.gitee.com/zhiyubujian/tool/](https://apidoc.gitee.com/zhiyubujian/tool/)

# 一 Bean操作工具

## 1.1 对象转换工具

该工具的主要目的是将源对象转换为目标对象，其主要功能如下:

- 将源对象里属性值复制给目标对象
- 将Java对象序列化为二进制数据
- 将序列化化后的二进制数据反序列化为对象

工具路径:

 ```java
  com.yishuifengxiao.common.tool.bean.BeanUtil
 ```

  使用示例:

```java

CustomException e = new CustomException();
DataException ex = new DataException();

//将CustomException复制为DataException
// 在使用此方法时，属性名一致的将会被复制，该方法是一个线程安全类的
// 第一个参数为源对象，第二个参数为目标对象
DataException copy = BeanUtil.copy(e, ex);

//将CustomException序列化为二进制数组
byte[] bytes = BeanUtil.objectToByte(e);

// 将转换后的二进制数据反序列化为对象
Object object = BeanUtil.byteToObject(bytes);

// 将转换后的二进制数据反序列化为指定的对象
CustomException exception = BeanUtil.byteToObject(bytes, CustomException.class);
```
# 二 集合操作工具

## 2.1 集合元素处理工具

该工具的主要目的是对集合进行处理，让用户在无须考虑NPE的情况下安全地操作集合。主要作用如下：

- 将集合转换成java8中的stream流
- 获取集合中的第一个元素
- 将数据转换成集合
- 安全地创建集合

> 该工具是线程安全类的

工具路径:

```java
com.yishuifengxiao.common.tool.collections.DataUtil
```

  使用示例:

```java
// 安全地创建集合，该方法与Arrays.asList不同，
// 创建出来的是ArrayList，可以放心地对创建出来的list进行各种操作
//List<String> list = Arrays.asList("a", "b", "c", "d");
List<String> list = DataUtil.asList("a", "b", "c", "d");

//将集合转换成并行流
Stream<String> parallelStream = DataUtil.parallelStream(list);

//将集合转换成串行流
Stream<String> stream = DataUtil.stream(list);

// 获取集合的第一个元素
String first = DataUtil.first(list);

//将数组转换成集合
String[] strs = {"a", "b", "c", "d"};
List<String> asList = DataUtil.asList(strs);
```

## 2.2 空集合判断工具

该工具的主要目的在于快速地判断一个集合是否为空集合或者为NULL。其主要作用如下：

- 判断集合是否为空
- 判断分页对象是否为空
- 判断集合是否仅有一个元素

工具路径:

```java
com.yishuifengxiao.common.tool.collections.SizeUtil
```

  使用示例:

```java
//判断改分页对象是否为空或者null
Page<FileRecord> page = Page.empty();
boolean empty = EmptyUtil.isEmpty(page);

//判断该集合是否为空或者null
List<String> list = new ArrayList<>();
boolean empty1 = EmptyUtil.isEmpty(list);

//判断该集合是否仅有一个元素
boolean onlyOneElement = EmptyUtil.onlyOneElement(list);
```

## 2.3 字典链式构建工具

该工具的主要目的是能通过链式方法快速地构建一个字典对象。

工具路径:

```java
com.yishuifengxiao.common.tool.collections.MapUtil
```

  使用示例:

```java
Map<String, Object> map = MapUtil.instance().put("k1", "v1").
        put("k2", "v2").put("k3", "v3").build();
```
# 三 日期时间工具

## 3.1 Date时间工具

该工具主要是基于`java.util.Date`实现的日期时间获取工具，其主要作用如下:

- 获取今天的开始时间点(00:00:00)
- 获取昨天的开始时间点(00:00:00)和结束时间点(23:59:59)
- 获取前天的开始时间点(00:00:00)
- 获取7天前的那个时间的开始时间点(00:00:00)
- 获取14天前的那个时间的开始时间点(00:00:00)
- 获取本周一的那个时间的开始时间点(00:00:00)
- 获取上周一的那个时间的开始时间点(00:00:00)
- 获取过去指定时间的那个时间的开始时间点(00:00:00)
- 获取本月1号的那个时间的开始时间点(00:00:00)
- 获取过去指定月份的那个月份的1号的开始时间点(00:00:00)
- 获取过去指定年份的那个时间的1月1号的那个时间的开始时间点(00:00:00)

> 该工具是线程安全类的

工具路径:

```java
com.yishuifengxiao.common.tool.datetime.DateOffsetUtil
```

  使用示例:

```java
//今天的开始时间(今天的00:00:00)
Date todayStart = DateOffsetUtil.todayStart();
//获取昨天的开始时间(昨天的00:00:00)
Date yesterdayStart = DateOffsetUtil.yesterdayStart();
//获取昨天的结束时间(昨天的23:59:59)
Date yesterdayEnd = DateOffsetUtil.yesterdayEnd();
//获取前天的开始时间(前天的00:00:00)
Date last2DayStart = DateOffsetUtil.last2DayStart();
//获取7天前那一天0时0分0秒这个时间(七天前的00:00:00)
Date last7DayStart = DateOffsetUtil.last7DayStart();
//获取本周一的开始时间(本周一的00:00:00)
Date mondayStart = DateOffsetUtil.mondayStart();
//获取本月的开始时间(本月1号的00:00:00)
Date monthStart = DateOffsetUtil.monthStart();
//获取1个月前的那个月的开始时间(1个月前的那个月的00:00:00)
Date monthStart1 = DateOffsetUtil.monthStart(1);
```

## 3.2 LocalDateTime时间工具

该工具主要是基于`java.time.LocalDateTime`实现的日期时间获取工具，其主要作用如下:

- 获取今天的开始时间点(00:00:00)
- 获取昨天的开始时间点(00:00:00)和结束时间点(23:59:59)
- 获取前天的开始时间点(00:00:00)
- 获取7天前的那个时间的开始时间点(00:00:00)
- 获取14天前的那个时间的开始时间点(00:00:00)
- 获取本周一的那个时间的开始时间点(00:00:00)
- 获取上周一的那个时间的开始时间点(00:00:00)
- 获取过去指定时间的那个时间的开始时间点(00:00:00)
- 获取本月1号的那个时间的开始时间点(00:00:00)
- 获取过去指定月份的那个月份的1号的开始时间点(00:00:00)
- 获取过去指定年份的那个时间的1月1号的那个时间的开始时间点(00:00:00)

> 该工具是线程安全类的

工具路径:

```java
com.yishuifengxiao.common.tool.datetime.TemporalUtil
```

  使用示例:

```java
//今天的开始时间(今天的00:00:00)
LocalDateTime todayStart = TemporalUtil.todayStart();
//获取昨天的开始时间(昨天的00:00:00)
LocalDateTime yesterdayStart = TemporalUtil.yesterdayStart();
//获取昨天的结束时间(昨天的23:59:59)
LocalDateTime yesterdayEnd = TemporalUtil.yesterdayEnd();
//获取前天的开始时间(前天的00:00:00)
LocalDateTime last2DayStart = TemporalUtil.last2DayStart();
//获取7天前那一天0时0分0秒这个时间(七天前的00:00:00)
LocalDateTime last7DayStart = TemporalUtil.last7DayStart();
//获取本周一的开始时间(本周一的00:00:00)
LocalDateTime mondayStart = TemporalUtil.mondayStart();
//获取本月的开始时间(本月1号的00:00:00)
LocalDateTime monthStart = TemporalUtil.monthStart();
//获取1个月前的那个月的开始时间(1个月前的那个月的00:00:00)
LocalDateTime monthStart1 = TemporalUtil.monthStart(1);
```

## 3.3 时间解析与格式化工具

该工具的主要目的是实现字符串与时间的转换以及不同格式时间的转换，主要功能如下

- 获取中国时区
- LocalDateTime与Date相互转换
- 将时间转换成毫秒数
- 将字符串解析为时间
- 将时间格式化为字符串

> 该工具是线程安全类的

工具路径:

```java
com.yishuifengxiao.common.tool.datetime.DateTimeUtil
```

  使用示例:

```java
// 获取中国时区
ZoneId china = DateTimeUtil.zoneIdOfChina();

// 将 Date 转换成 LocalDateTime
LocalDateTime localDateTime = DateTimeUtil.date2LocalDateTime(new Date());

// 将 LocalDateTime 转换成 Date
Date date = DateTimeUtil.localDateTime2Date(LocalDateTime.now());

//将时间转换成毫秒数
Long time = DateTimeUtil.getTime(date);
Long time1 = DateTimeUtil.getTime(localDateTime);


//将时间格式化为字符串
String format = DateTimeUtil.format(date);
String format1 = DateTimeUtil.format(localDateTime);

//按照指定格式将时间格式化为字符串
String format3 = DateTimeUtil.format(date, "yyyy-MM-dd HH:mm:ss");
String format4 = DateTimeUtil.format(localDateTime, "yyyy-MM-dd HH:mm:ss");

//将字符串转换成时间
LocalDateTime parse = DateTimeUtil.parse("2021-10-10 12:12:12");
//按照指定格式将字符串转换成时间
LocalDateTime parse1 = DateTimeUtil.parse("2021-10-10 12:12:12", "yyyy-MM-dd HH:mm:ss");
```
# 四 加解密工具

## 4.1 AES加解密工具

该工具是基于AES算法实现的加解密工具。其主要作用如下：

- 实现AES算法加密
- 实现AES算法解密

工具路径:

```java
com.yishuifengxiao.common.tool.encoder.AES
```

  使用示例:

```java
//使用指定的秘钥对数据进行加密
String encrypt = AES.encrypt("秘钥", "待加密的数据");
// 使用指定的秘钥对加密后的数据进行解密，若待解密的数据为空或解密出现问题时返回为null
AES.decrypt("秘钥", encrypt);
```

## 4.2 DES加解密工具

该工具是基于DES算法实现的加解密工具。其主要作用如下：

- 实现DES算法加密
- 实现DES算法解密

工具路径:

```java
com.yishuifengxiao.common.tool.encoder.DES
```

  使用示例:

```java
//使用指定的秘钥对数据进行加密
String encrypt = DES.encrypt("秘钥", "待加密的数据");
// 使用指定的秘钥对加密后的数据进行解密，若待解密的数据为空或解密出现问题时返回为null
DES.decrypt("秘钥", encrypt);
```

## 4.3 MD5加密工具

该工具主要是使用MD5算法对字符串进行加密操作。

工具路径:

```java
com.yishuifengxiao.common.tool.encoder.Md5
```

  使用示例:

```java
//对字符串进行MD5加密，输入32位小写的MD5值
String md5 = Md5.md5("待加密码的数据");
//对字符串进行MD5加密，输入16位小写的MD5值
String md5Short = Md5.md5Short("待加密码的数据");
```
# 五 通用实体类

## 5.1 通用响应对象

该对象的主要目的是在进行后端开发时统一响应数据的格式，使得全体应用中接口的返回数据的格式能够保持一致性，方便前端开发处理的同时让全局风格保持一定的规范性。

通用响应对象的属性及定义如下：

| 修饰符| 数据类型   | 属性及含义 |
| ------------------| ------------------------------  | ------------------------------------------------------------ |
| `protected`|    ` int` | `code`请求的响应码 |
| `protected `  |  `T`   | `data`响应数据，在基本基本信息无法满足时会出现此信息,一般情况下无此信息 |
| `protected `|   `Date` | `date`响应时间                                               |
| `protected `|`String`  | `id`请求ID,用于请求追踪 .无论调用接口成功与否,都会返回请求 ID,该序列号全局唯一且随机 |
| `protected `|`String`  | `msg`响应提示信息,一般与响应码的状态对应,对响应结果进行简单地描述 |

### 5.1.1 赋值说明

- 响应码`code` : 在默认情况下借鉴了HttpStatus的响应值和含义，其定义可参见 [https://developer.mozilla.org/en-US/docs/Web/HTTP/Statu]( https://developer.mozilla.org/en-US/docs/Web/HTTP/Statu)
- 响应数据`data` : 在定义中该属性是一个泛型，用户可以传输各种必需的响应数据。如果用户不需要传输数据仅仅通过响应码表达请求操作结果时，该属性可以置空或使用默认值
- 响应信息 `msg` : 该属性在一般情况用于辅助描述响应码希望表达的含义

### 5.1.2 常用创建方法

|修饰符及响应 | `方法使用及说明  |
| ------------------------- | ------------------------------------------------------------ |
| `static Response<Object>` | `badParam()`生成一个默认的表示参数有误的响应对象(响应码400)  |
| `static Response<Object>` | `badParam(String msg)`根据响应提示信息生成一个表示参数有误的响应对象(响应码400) |
| `static <T> Response<T>`  | `badParam(String msg, T data)`根据响应提示信息和响应数据生成一个表示参数有误的响应对象(响应码400) |
| `static Response<Object>` | `error()`生成一个默认表示请求业务未完成的响应对象(500响应码) |
| `static Response<Object>` | `error(String msg)`根据响应提示信息生成一个表示服务器内部异常500时的返回信息 |
| `static <T> Response<T>`  | `error(String msg, T data)`根据响应提示信息和响应数据生成表示服务器内部异常500时的返回信息 |
| `static <T> Response<T>`  | `error(T data)`根据响应数据生成表示服务器内部异常500时的返回信息 |
| `static <T> Response<T>`  | `errorData(T data)`生成一个默认表示请求业务未完成的响应对象(500响应码) |
| `static Response<Object>` | `notAllow()`生成一个默认的表示资源不可用的响应对象(403响应码) |
| `static Response<Object>` | `notAllow(String msg)`根据响应提示信息生成表示资源不可用的响应对象(403响应码) |
| `static Response<Object>` | `notFoundt()`生成一个默认的表示资源不存在的响应对象(404响应码) |
| `static <T> Response<T>`  | `of(int code, String msg, T data)`构建一个通用的响应对象     |
| `static Response<Object>` | `suc()`生成一个默认的一个表示成功的响应对象                  |
| `static Response<Object>` | `suc(String msg)`根据响应提示信息生成一个表示成功的响应对象  |
| `static <T> Response<T>`  | `suc(String msg, T data)`根据响应提示信息和响应数据生成一个表示成功的响应对象 |
| `static <T> Response<T>`  | `suc(T data)`根据响应数据生成一个表示成功的响应对象          |
| `static <T> Response<T>`  | `sucData(T data)`根据响应提示信息生成一个表示成功的响应对象  |
| `static Response<Object>` | `unAuth()`生成一个默认的表示资源未授权的响应对象(401响应码)  |
| `static Response<Object>` | `unAuth(String msg)`根据响应提示信息生成一个表示资源未授权的响应对象(401响应码) |
| `static <T> Response<T>`  | `unAuth(String msg, T data)`根据响应提示信息和响应数据生成一个表示资源未授权的响应对象(401响应码) |

### 5.1.3  示例代码

工具路径:

```java
com.yishuifengxiao.common.tool.entity.Response
```
使用示例:
```java
//默认的请求成功响应，响应码为200
Response<Object> suc = Response.suc();
//默认的请求成功响应，响应码为200,其中:响应描述信息修改为 响应描述信息：成功
Response<Object> suc1 = Response.suc("响应描述信息：成功");
//默认的请求成功响应，响应码为200,其中:响应具体信息修改成功 响应具体信息:成功
Response<String> sucData = Response.sucData("响应具体信息:成功");
//构建一个请求成功响应，响应码为200，其中: 响应描述信息修改为 响应描述信息：成功 ，响应具体信息修改成功 响应具体信息:成功
Response<String> suc2 = Response.suc("响应描述信息：成功", "响应具体信息:成功");


//默认的请求失败响应，响应码为500
Response<Object> error = Response.error();
//默认的请求失败响应，响应码为500,其中：响应描述信息修改为 响应描述信息：失败
Response<Object> error1 = Response.error("响应描述信息：失败");
//默认的请求失败响应，响应码为500,其中：响应具体信息修改成功 响应具体信息:失败
Response<String> errorData = Response.errorData("响应具体信息:失败");
//默认的请求失败响应，响应码为500，其中: 响应描述信息修改为 响应描述信息：失败 ，响应具体信息修改成功 响应具体信息:失败
Response<String> error2 = Response.error("响应描述信息：失败", "响应具体信息:失败");

//通用的构建方法
Response<Object> response = Response.of(响应码, "响应描述信息", 响应具体信息);
```

## 5.2 通用分页对象

本对象主要是解决后端开发时在使用不同的分页插件时造成分页对象不同从而导致数据结构不一致的问题，本工具主要是适配了jpa里的`org.springframework.data.domain.Page`和pagehelper里的`com.github.pagehelper.PageInfo`这两个分页对象。

该工具主要作用如下：

- 适配不同类型的分页对象
- 转换分页对象里的数据

> 该分页对象中的当前页页码从1开始，默认的分页大小为20

工具路径:

```java
//创建一个默认的空的分页对象
Page<Object> empty = Page.ofEmpty();

// 转换 jpa 里的分页对象
Page<Object> page = JpaPage.of(org.springframework.data.domain.Page.empty());

//转换 pagehelper 的分页对象
Page<Object> page1 = TkPage.of(PageInfo.of(new ArrayList<>()));


// 转换分页对象里的数据
Page<Object> convert = page.convert(原始数据 -> {
    
    return 转换后的数据
});
```

## 5.3 自定义异常

该工具的主要目的是统一项目中使用的各种异常，其定义如下:

![image-20211011144459610](https://zhiyubujian.oss-cn-hangzhou.aliyuncs.com/blog/image-20211011144459610.png)
# 六 IO流和文件操作工具

## 6.1 IO流关闭工具

该工具的主要目的是在安全地关闭各种Closeable实例。

> 该工具是一个线程安全类的工具

工具路径:

```java
com.yishuifengxiao.common.tool.io.CloseUtil
```

使用示例

```java
InputStream inputStream = new FileInputStream("input");

OutputStream outputStream = new FileOutputStream("out");

CloseUtil.close(inputStream, outputStream);
```

## 6.2 文件处理工具

该工具的主要目的是进行文件和base64字符串之间进行互相转换和获取文件的MD5值。

> 该工具是一个线程安全类的工具

工具路径:

```java
com.yishuifengxiao.common.tool.io.IoUtil
```
示例代码:
```java
File file = new File("待处理的文件");

// 将文件转换成base64格式的字符串
String base64Str = FileUtil.file2Base64(file);

//将base64格式的字符串转换成文件
File toFile = FileUtil.base64ToFile(base64Str);

//获取文件的MD5值(32位小写)
String md5 = FileUtil.getMd5(file);
```

## 6.3 base64与图片转换工具

该工具的主要目的是进行图片和base64字符串之间进行互相转换

> 该工具是一个线程安全类的工具

工具路径:

```java
com.yishuifengxiao.common.tool.io.IoUtil
```

代码示例:

```java
//base64字符串转换成图片
ImageUtil.base64ToImage("图片转成base64格式后的字符串", "目标图片的地址");

//BufferedImage格式的图片转换为base64字符串，结果不包含base64信息头
String image2Base64 = ImageUtil.image2Base64(
    new BufferedImage(255, 255, BufferedImage.TYPE_INT_RGB));

//BufferedImage格式的图片转换为base64字符串，结果包含png格式的base64信息头
String _image2Base64 = ImageUtil.image2Base64Png(
    new BufferedImage(255, 255, BufferedImage.TYPE_INT_RGB));

//base64字符串转换为ufferedImage格式的图片
ImageUtil.base64ToImage(image2Base64, "待保存的图片的地址");
```
# 七 缓存工具

## 7.1 全局存储工具

该工具的主要目的是构建一个基于内存的全局字典缓存工具。在使用该工具时注意防止内存泄露。

> 该工具是一个线程安全类的工具

工具路径:

```java
com.yishuifengxiao.common.tool.context.LocalCache
```

示例代码:

```java
        //存放一个数据
        LocalStorage.put(new FileRecord());

        //使用指定的索引存放一个数据
        LocalStorage.put("k1", new FileRecord());

        //根据索引获取一个数据
        Object object = LocalStorage.get("k1");

        //根据数据类型获取一个数据
        FileRecord record = LocalStorage.get(FileRecord.class);

        //根据索引从缓存中删除一个数据
        LocalStorage.remove("k1");

        //根据索引获取一个数据,然后从缓存中删除该数据
        Object obj = LocalStorage.pop("k1");

        //清除缓存中所有的数据
        LocalStorage.clear();
```

## 7.2 上下文存储工具

该工具与全局存储工具基本类似，但不同的是，该工具是基于当前线程实现的，在当前线程中存储的数据不能再其他线程中访问。在使用该工具时注意防止内存泄露。

> 该工具是一个线程安全类的工具

工具路径:

```java
com.yishuifengxiao.common.tool.context.SessionStorage
```

示例代码:

```java
//存放一个数据
SessionStorage.put(new FileRecord());

//使用指定的索引存放一个数据
SessionStorage.put("k1", new FileRecord());

//根据索引获取一个数据
Object object = SessionStorage.get("k1");

//根据数据类型获取一个数据
FileRecord record = SessionStorage.get(FileRecord.class);

//根据索引从缓存中删除一个数据
SessionStorage.remove("k1");

//根据索引获取一个数据,然后从缓存中删除该数据
Object obj = SessionStorage.pop("k1");

//清除缓存中所有的数据
SessionStorage.clear();
```

# 八 常见数据封装工具

该工具的主要目的是对常见的基础数据类型的数据进行处理，实现数值大小比较和字符串处理等功能。

该工具包的功能如下:

| 工具名称 | 功能说明        |
| :----------------------------------------------------------: | :--------------------: |
| [BetweenUtil](https://apidoc.gitee.com/zhiyubujian/tool/com/yishuifengxiao/common/tool/lang/BetweenUtil.html) | 比较工具工具类         |
| [HumpUtil](https://apidoc.gitee.com/zhiyubujian/tool/com/yishuifengxiao/common/tool/lang/HumpUtil.html) | 下划线与驼峰互转工具   |
| [NumberUtil](https://apidoc.gitee.com/zhiyubujian/tool/com/yishuifengxiao/common/tool/lang/NumberUtil.html) | 数字转换与操作比较工具 |
| [ObjUtil](https://apidoc.gitee.com/zhiyubujian/tool/com/yishuifengxiao/common/tool/lang/ObjUtil.html) | 集合对象判断工具       |
| [RegexUtil](https://apidoc.gitee.com/zhiyubujian/tool/com/yishuifengxiao/common/tool/lang/RegexUtil.html) | 正则工具               |
| [StringUtil](https://apidoc.gitee.com/zhiyubujian/tool/com/yishuifengxiao/common/tool/lang/StringUtil.html) | 字符串工具             |

## 8.1 下划线与驼峰互转工具

该工具主要目的是实现下划线与驼峰互相转换。

工具路径:

```java
com.yishuifengxiao.common.tool.lang.HumpUtil
```

示例代码:

```java
//转换为驼峰,例如:将call_back转换成 CallBack
String camelCaseName = HumpUtil.camelCaseName("call_back");
System.out.println(camelCaseName);
//转换为下划线,例如:将CallBack转换成 call_back
String underscoreName = HumpUtil.underscoreName("camelCaseName");
System.out.println(underscoreName);
```

## 8.2 数字转换与操作比较工具

该工具的主要目的是实现字符串与数字之间互相转换以及进行数据大小比较

工具路径

```java
com.yishuifengxiao.common.tool.lang.NumberUtil
```

示例代码:

```java
//将字符串转换成数字
Integer integer = NumberUtil.parseInt("11");
//判断数字是否小于0
boolean lessZero = NumberUtil.lessZero(integer);
//判断数字是否小于或等于0
boolean lessEqualZero = NumberUtil.lessEqualZero(integer);
//判断数字是否大于0
boolean greaterZero = NumberUtil.greaterZero(integer);
//判断数字是否大于或等于0
boolean greaterEqualZero = NumberUtil.greaterEqualZero(integer);
//判断两个数字是否相等
boolean equals = NumberUtil.equals(Integer.parseInt("2322"), Integer.parseInt("5865"));
//将数字转成Boolean值,如果数字为null，返回为false,数字小于或等于0返回为false,数字大于0返回为true
Boolean num2Bool = NumberUtil.num2Bool(1);
//将boolean值转换成数字,value为true时返回1，否则为0
int bool2Int = NumberUtil.bool2Int(true);
```
# 九 自定义工具

## 9.1 回调工具类

该工具类的主要目的是使用内置线程池执行一个回调操作，节省创建线程和线程管理所需的资源。

工具路径:

```java
com.yishuifengxiao.common.tool.utils.ExecuteUtil
```

示例代码:

```java
ExecuteUtil.execute(() -> {
    System.out.println("-------- 回调");
});
```

## 9.2 身份证操作工具

该工具的主要目的是对字符串格式的身份证号进行判断。主要功能如下：

- 判断该字符串是否为一个合法的身份证号
- 从字符串格式的身份证号里提取出出生日期

> 该工具是一个线程安全类的工具

工具路径:

```java
com.yishuifengxiao.common.tool.utils.CertNoUtil
```

示例代码:

```java
//判断该身份证号是否为一个正确的身份证号
boolean valid = CertNoUtil.isValid("421111198705164213");
System.out.println(valid);
//从身份证号中提取出出生日期
LocalDate birthday = CertNoUtil.extractBirthday("421111198705164213");
System.out.println(birthday);
```

## 9.3 经纬度距离计算工具

该工具的主要目的是计算两个经纬度之间距离。

> 该工具是一个线程安全类的工具

工具路径:

```java
com.yishuifengxiao.common.tool.utils.GpsUtil
```

示例代码:

```java
//通过经纬度计算出来的结果单位为米
long distance = GpsUtil.distance(111, 30, 156, 35);
```
