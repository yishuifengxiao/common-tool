package com.yishuifengxiao.common.tool.jdbc;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringJoiner;

/**
 * MySQL JDBC URL 解析与构建工具。
 * <p>
 * 支持从 jdbc:mysql://host:port/database?param1=value1&param2=value2 格式的URL中提取参数，
 * 并支持将提取后的参数反向构建为等价的JDBC URL，参数值会自动进行URL编码/解码以兼容特殊字符。
 */
public class JdbcUrlHelper {

    private static final String JDBC_MYSQL_PREFIX = "jdbc:mysql://";  // MySQL JDBC URL前缀
    private static final String DEFAULT_PROTOCOL = "mysql";            // 默认协议
    private static final int DEFAULT_PORT = 3306;                     // 默认端口
    private static final String CHARSET = StandardCharsets.UTF_8.name(); // 字符集编码

    /**
     * JDBC连接信息实体，包含协议、主机、端口、数据库名和参数映射。
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Accessors(chain = true)
    public static class ConnInfo implements Serializable {
        private String protocol;       // 例如 "mysql"
        private String host;
        private int port;
        private String database;
        private Map<String, String> parameters;  // 有序，保留原始参数顺序

        /**
         * 启用批量处理功能的方法
         * 此方法会设置参数以启用SQL语句的批量重写功能，可以提高批量操作的效率
         *
         * @return 返回当前ConnInfo对象，支持链式调用
         */
        public synchronized ConnInfo enableBatch() {
            // 返回当前对象实例，支持链式调用
            addParameter("rewriteBatchedStatements", "true");
            return this;
        }

        /**
         * 使用SSL连接
         * <p>useSSL：控制客户端与 MySQL 服务器之间的通信是否加密。
         * <p>
         * useSSL=true：会尝试建立加密连接（如果服务器支持的话）。
         * <p>
         * useSSL=false：明确禁用加密，通常用于开发或测试环境，以轻微提升性能</p>
         *
         * @param useSSL 是否使用SSL连接
         * @return 返回当前ConnInfo对象，支持链式调用
         */
        public synchronized ConnInfo useSSL(boolean useSSL) {
            // 添加SSL参数
            addParameter("useSSL", String.valueOf(useSSL));
            return this;
        }

        /**
         * 使用字符集
         * 该方法为同步方法，用于设置数据库连接参数中的字符集为指定值。
         * <p>
         * charset=utf8mb4：将字符集设置为UTF-8多字节字符集，支持所有UTF-8字符。
         * </p>
         * <p>useUnicode & characterEncoding：这两个参数通常需配合使用，主要用于解决中文字符等非英文字符的乱码问题。
         * <p>
         * useUnicode=true 是启用 Unicode 编码支持的总开关。
         * <p>
         * characterEncoding 则指定具体的字符编码集，例如 utf8 或 utf8mb4。
         * <p>
         * 注意事项：如果不设置 characterEncoding，驱动会使用系统默认编码，容易出现乱码</p>
         *
         * @param charset 字符集名称，例如 "utf8mb4"
         * @return 返回当前ConnInfo对象，支持链式调用
         */
        public synchronized ConnInfo useCharset(String charset) {
            if (StringUtils.isBlank(charset)) {
                charset = "utf8mb4";
            }
            charset = charset.trim();
            // 添加字符集参数
            addParameter("useUnicode", String.valueOf(true));
            // 添加字符集参数
            addParameter("characterEncoding", charset);
            return this;
        }

        /**
         * 设置连接排序规则的同步方法
         * <p>connectionCollation：连接使用的排序规则（默认值由服务器决定）。当设置了 characterEncoding=utf8mb4 时，推荐同时设置此参数为 utf8mb4_unicode_ci</p>
         *
         * @param collation 指定的连接排序规则字符串
         * @return 返回当前ConnInfo对象，支持链式调用
         */
        public synchronized ConnInfo connectionCollation(String collation) {
            // 添加排序规则参数
            addParameter("connectionCollation", collation);
            // 返回当前对象实例
            return this;
        }

        /**
         * 使用utf8mb4字符集的方法
         * 这是一个同步方法，用于设置数据库连接的字符集为utf8mb4
         *
         * @return 返回当前对象实例，支持链式调用
         */
        public synchronized ConnInfo useUtf8mb4() {
            // 设置字符集为utf8mb4
            useCharset("utf8mb4");
            // 设置连接排序规则为utf8mb4_unicode_ci
            connectionCollation("utf8mb4_unicode_ci");
            // 返回当前对象实例
            return this;
        }

        /**
         * 设置连接时区参数的方法
         *
         * @param connectionTimeZone 连接时区字符串
         * @return 返回当前对象实例，支持链式调用
         */
        public synchronized ConnInfo connectionTimeZone(String connectionTimeZone) {
            // 添加连接时区参数到参数集合中
            addParameter("connectionTimeZone", connectionTimeZone);
            // 返回当前对象实例，以便支持链式调用
            return this;
        }

        /**
         * 使用服务器端预处理语句的方法
         * 该方法同步执行，用于启用服务器端预处理语句功能
         * <p>useServerPrepStmts：是否使用服务器端预处理（默认值false）。对于执行频繁的SQL，开启可降低解析开销</p>
         * <p>cachePrepStmts：是否缓存预处理语句（默认值false）。建议与 useServerPrepStmts 一同开启，提升性能</p>
         *
         * @return 返回当前ConnInfo对象，支持链式调用
         */
        public synchronized ConnInfo useServerPrepStmts() {
            // 添加字符集参数，设置serverPrepStmts为true
            addParameter("serverPrepStmts", String.valueOf(true));
            addParameter("cachePrepStmts", String.valueOf(true));
            return this;  // 返回当前对象实例，以便支持链式调用
        }

        /**
         * 允许多重查询设置方法
         * 该方法用于设置是否允许在一个查询中执行多个SQL语句
         * <p>allowMultiQueries：是否允许在一次请求中执行多条SQL语句（默认值false）。有SQL注入风险，尽量在代码中控制而非依赖于此参数，可设置参数allowMultiQueries=true</p>
         *
         * @param allowMultiQueries 布尔值，true表示允许多重查询，false表示不允许
         * @return 返回当前ConnInfo对象，支持链式调用
         */
        public synchronized ConnInfo allowMultiQueries(boolean allowMultiQueries) {
            addParameter("allowMultiQueries", String.valueOf(allowMultiQueries));
            return this;
        }

        /**
         * 向连接信息对象中添加参数的方法
         * 该方法是同步的，确保在多线程环境下安全操作
         *
         * @param key   参数的键名
         * @param value 参数的值
         * @return 返回当前ConnInfo对象，支持链式调用
         */
        public synchronized ConnInfo addParameter(String key, String value) {
            // 如果参数集合为空，则初始化一个新的LinkedHashMap
            // 使用LinkedHashMap可以保持参数的插入顺序
            if (this.parameters == null) {
                this.parameters = new LinkedHashMap<>();
            }
            if (StringUtils.isAnyBlank(key, value)) {
                return this;
            }
            // 将键值对添加到参数集合中
            this.parameters.put(key.trim(), value.trim());
            // 返回当前对象实例，支持链式调用
            return this;
        }


        /**
         * 使用北京时区配置方法
         * 该方法为同步方法，用于设置数据库连接参数中的服务器时区为北京时区（Asia/Shanghai）
         *
         * @return 返回当前ConnInfo对象实例，支持链式调用
         */
        public synchronized ConnInfo useBeijingTimeZone() {
            // 添加服务器时区参数，设置为亚洲/上海时区（对应北京时间）
            addParameter("serverTimezone", "Asia/Shanghai");
            addParameter("connectionTimeZone", "Asia/Shanghai");
            // 返回当前对象实例，以便支持链式调用
            return this;
        }

        /**
         * 使用UTC时区配置方法
         * 该方法为同步方法，用于设置数据库连接参数中的服务器时区为UTC时区（UTC）
         * <p>serverTimezone：用于覆盖服务器端的时区设置。这在 MySQL 8.0 及更高版本中非常关键，如果未正确设置，可能导致连接报错。
         * <p>
         * serverTimezone=UTC：将时区设置为协调世界时（UTC）。
         * <p>
         * serverTimezone=Asia/Shanghai：将时区设置为上海时间。
         * <p>
         * serverTimezone=GMT%2B8：通过 URL 编码指定东八区时间（GMT+8）</p>
         *
         * @return 返回当前ConnInfo对象实例，支持链式调用
         */
        public synchronized ConnInfo useUTCTimeZone() {
            // 添加服务器时区参数，设置为UTC时区（UTC）
            addParameter("serverTimezone", "UTC");
            // 返回当前对象实例，以便支持链式调用
            return this;
        }

        /**
         * 设置是否需要SSL连接
         * <p>requireSSL：是否强制要求SSL连接（默认值false）。若设置为true，服务器不支持SSL时会连接失败，强化了安全性</p>
         *
         * @param requireSSL 是否需要SSL连接，true表示需要，false表示不需要
         * @return 返回当前ConnInfo对象，支持链式调用
         */
        public synchronized ConnInfo requireSSL(boolean requireSSL) {
            // 添加requireSSL参数，将boolean值转换为字符串形式
            addParameter("requireSSL", String.valueOf(requireSSL));
            // 返回当前对象实例
            return this;
        }

        /**
         * 自动重新连接方法
         * 该方法使用synchronized关键字确保线程安全
         * 设置自动重连相关参数并返回当前连接信息对象
         * <p>autoReconnect：连接异常中断时是否自动重连（默认值false）。使用连接池时建议开启，并配合 failOverReadOnly=false</p>
         *
         * @return ConnInfo 返回当前连接信息对象，支持链式调用
         */
        public synchronized ConnInfo autoReconnect() {
            // 添加autoReconnect参数，将boolean值转换为字符串形式
            addParameter("autoReconnect", String.valueOf(true));
            // 添加failOverReadOnly参数，将boolean值转换为字符串形式
            addParameter("failOverReadOnly", String.valueOf(false));
            return this;
        }

        /**
         * 使用默认时区设置连接信息
         * 该方法是线程安全的，通过synchronized关键字保证
         *
         * @return 返回当前ConnInfo对象实例，支持链式调用
         */
        public synchronized ConnInfo useDefaultTimeZone() {
            // 添加服务器时区参数，设置为默认时区（根据JVM时区）
            addParameter("serverTimezone", "useTimezone=true");
            // 返回当前对象实例，以便支持链式调用
            return this;
        }

        /**
         * 构建JDBC连接URL的方法
         * 该方法是同步方法，确保在多线程环境下安全构建URL
         *
         * @return 返回构建好的JDBC连接URL字符串
         */
        public synchronized String buildJdbcUrl() {
            // 调用JdbcUrlHelper类的静态方法buildJdbcUrl
            // 传入当前ConnInfo对象作为参数来构建URL
            return JdbcUrlHelper.buildJdbcUrl(ConnInfo.this);
        }
    }

    /**
     * 解析JDBC URL，提取连接信息。
     *
     * @param jdbcUrl 以 "jdbc:mysql://" 开头的URL字符串
     * @return 解析得到的连接信息对象
     * @throws IllegalArgumentException 如果URL格式无效或解析失败
     */
    public static ConnInfo parseJdbcUrl(String jdbcUrl) {
        if (jdbcUrl == null || !jdbcUrl.startsWith(JDBC_MYSQL_PREFIX)) {
            throw new IllegalArgumentException("URL必须以 " + JDBC_MYSQL_PREFIX + " 开头");
        }

        // 去掉 "jdbc:" 前缀，得到类似 "mysql://host:port/db?params" 的标准URI
        String uriPart = jdbcUrl.substring(5); // 移除 "jdbc:"
        URI uri;
        try {
            uri = new URI(uriPart);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("URL格式错误: " + jdbcUrl, e);
        }

        String protocol = uri.getScheme();     // 如 "mysql"
        if (protocol == null) {
            protocol = DEFAULT_PROTOCOL;
        }
        String host = uri.getHost();
        if (host == null || host.isEmpty()) {
            throw new IllegalArgumentException("URL中未包含主机名");
        }
        int port = uri.getPort();
        if (port == -1) {
            port = DEFAULT_PORT;
        }

        // 解析数据库名 (path部分，例如 "/personkit" -> "personkit")
        String path = uri.getPath();
        String database = null;
        if (path != null && path.length() > 1) {
            // 去除开头的 '/'
            String rawDb = path.substring(1);
            // 数据库名可能需要解码 (如果包含%xx编码)
            database = decodeValue(rawDb);
        }

        // 解析查询参数
        String query = uri.getQuery();
        Map<String, String> parameters = new LinkedHashMap<>(); // 保持顺序
        if (query != null && !query.isEmpty()) {
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                int eqIdx = pair.indexOf('=');
                String key;
                String value;
                if (eqIdx > 0) {
                    key = pair.substring(0, eqIdx);
                    value = pair.substring(eqIdx + 1);
                } else {
                    // 没有等号的情况（参数名存在但无值，少见），视作key，值为空串
                    key = pair;
                    value = "";
                }
                // 参数值需要URL解码
                parameters.put(key, decodeValue(value));
            }
        }

        return new ConnInfo(protocol, host, port, database, parameters);
    }

    /**
     * 根据连接信息构建完整的JDBC URL。
     * 参数值会被URL编码，以兼容特殊字符（如空格、中文等）。
     *
     * @param info 连接信息对象
     * @return 完整的JDBC URL字符串
     */
    public static String buildJdbcUrl(ConnInfo info) {
        // 构建协议和主机端口部分
        StringBuilder url = new StringBuilder(JDBC_MYSQL_PREFIX);
        url.append(info.getHost());
        url.append(':').append(info.getPort());

        // 数据库名（需要URL编码）
        if (info.getDatabase() != null && !info.getDatabase().isEmpty()) {
            url.append('/').append(encodeValue(info.getDatabase()));
        } else {
            url.append('/'); // 即使没有数据库名，保留斜杠
        }

        // 查询参数
        Map<String, String> params = info.getParameters();
        if (params != null && !params.isEmpty()) {
            StringJoiner paramJoiner = new StringJoiner("&");
            for (Map.Entry<String, String> entry : params.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                // 忽略值为null或空字符串的参数
                if (StringUtils.isBlank(value)) {
                    continue;
                }
                // 参数键一般无需编码，但值必须编码
                String encodedValue = encodeValue(value);
                paramJoiner.add(key + "=" + encodedValue);
            }
            // 只有存在有效参数时才添加问号
            if (paramJoiner.length() > 0) {
                url.append('?').append(paramJoiner);
            }
        }

        return url.toString();
    }

    /**
     * 对参数值进行URL解码（UTF-8）。
     * 自动处理 '+' 为空格。
     */
    private static String decodeValue(String value) {
        if (value == null) {
            return null;
        }
        try {
            // URLDecoder 会将 '+' 转换为空格
            return URLDecoder.decode(value, CHARSET);
        } catch (UnsupportedEncodingException e) {
            // 理论上UTF-8始终可用
            throw new RuntimeException("不支持的字符集: " + CHARSET, e);
        }
    }

    /**
     * 对参数值进行URL编码（UTF-8）。
     * 注意：空格会被编码为 '+'，这是标准的 application/x-www-form-urlencoded 行为。
     * 如果需要严格还原为 '%20'，可在编码后替换，但MySQL驱动同时接受两种形式，此处保持简洁。
     */
    private static String encodeValue(String value) {
        if (value == null) {
            return "";
        }
        try {
            return URLEncoder.encode(value, CHARSET);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("不支持的字符集: " + CHARSET, e);
        }
    }

}