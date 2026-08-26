package com.yishuifengxiao.common.tool.jdbc;

import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.DateTimeException;
import java.time.ZoneId;
import java.util.Map;

/**
 * 时区检测器，用于检测数据库服务器的时区
 *
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
public class ZoneIdDetector {
    /** JDBC URL中的时区参数标识 */
    private static final String TIMEZONE_PARAM = "serverTimezone=";
    
    /** 不同数据库的主时区查询语句映射表 */
    private static final Map<String, String> DB_TIMEZONE_QUERIES = Map.of(
            "mysql", "SELECT @@session.time_zone",
            "postgresql", "SHOW TIMEZONE",
            "oracle", "SELECT DBTIMEZONE FROM DUAL",
            "microsoft sql server", "SELECT CURRENT_TIMEZONE_ID()"
    );
    
    /** 不同数据库的备用时区查询语句映射表，用于主查询返回SYSTEM时的补充查询 */
    private static final Map<String, String> DB_BACKUP_TIMEZONE_QUERIES = Map.of(
            "mysql", "SELECT @@global.time_zone, @@system_time_zone",
            "postgresql", "SELECT current_setting('TIMEZONE')",
            "oracle", "SELECT SESSIONTIMEZONE FROM DUAL",
            "microsoft sql server", "SELECT @@DATEFIRST, @@LANGUAGE"
    );

    /**
     * 检测数据库时区
     *
     * @param connection 数据库连接
     * @return 数据库时区，如果无法检测则返回 null
     * @throws SQLException SQL 异常
     */
    public static ZoneId detectDatabaseTimezone(Connection connection) throws SQLException {
        try {
            String timezone = tryGetTimezoneFromUrl(connection);
            if (timezone == null) {
                timezone = tryGetTimezoneFromDatabase(connection);
            }
            return parseTimezone(timezone);
        } catch (SQLException e) {
            log.warn("无法获取数据库时区信息，使用系统默认时区", e);
            return null;
        }
    }

    /**
     * 尝试从JDBC连接URL中提取时区配置信息
     * 优先检查URL中是否包含serverTimezone参数
     *
     * @param connection 数据库连接，用于获取JDBC URL
     * @return 从URL中提取的时区字符串，如果URL中未配置则返回null
     * @throws SQLException 获取数据库元数据失败时抛出异常
     */
    private static String tryGetTimezoneFromUrl(Connection connection) throws SQLException {
        try {
            String url = connection.getMetaData().getURL();
            return url.contains("serverTimezone") ? extractTimezoneFromUrl(url) : null;
        } catch (SQLException e) {
            log.warn("从URL获取时区信息失败", e);
            return null;
        }
    }

    /**
     * 通过执行数据库查询获取时区信息
     * 使用主时区查询语句，如果返回SYSTEM则尝试备用查询获取实际时区
     *
     * @param connection 数据库连接，用于执行时区查询语句
     * @return 数据库时区字符串，查询失败或无结果时返回null
     */
    private static String tryGetTimezoneFromDatabase(Connection connection) {
        try (Statement stmt = connection.createStatement();
             ResultSet timezoneRs = stmt.executeQuery(getTimezoneQuery(connection))) {
            if (timezoneRs.next()) {
                String timezone = timezoneRs.getString(1);
                if ("SYSTEM".equalsIgnoreCase(timezone)) {
                    log.trace("主时区查询返回SYSTEM，尝试使用备用查询获取实际时区");
                    timezone = tryGetBackupTimezoneFromDatabase(connection);
                }
                return timezone;
            }
            return null;
        } catch (SQLException e) {
            log.debug("从数据库查询时区信息失败", e);
            return null;
        }
    }

    /**
     * 当主时区查询返回SYSTEM时，使用备用查询获取实际时区配置
     * 针对不同数据库类型采用不同的解析策略：
     * - MySQL: 检查全局时区和系统时区，处理编码问题
     * - PostgreSQL/Oracle: 直接获取会话时区
     * - SQL Server: 根据语言设置推断时区
     *
     * @param connection 数据库连接，用于执行备用时区查询
     * @return 解析后的时区字符串，无法获取时返回"SYSTEM"
     */
    private static String tryGetBackupTimezoneFromDatabase(Connection connection) {
        try {
            String backupQuery = getBackupTimezoneQuery(connection);
            if (backupQuery == null) {
                log.debug("未找到备用时区查询语句，使用SYSTEM");
                return "SYSTEM";
            }

            try (Statement stmt = connection.createStatement();
                 ResultSet backupRs = stmt.executeQuery(backupQuery)) {
                if (backupRs.next()) {
                    String databaseProductName = connection.getMetaData().getDatabaseProductName().toLowerCase();
                    String result;
                switch (databaseProductName) {
                    case "mysql":
                        // MySQL备用查询返回全局时区和系统时区两个字段，需要处理SYSTEM情况和编码问题
                            String globalTimezone = backupRs.getString(1);
                            if ("SYSTEM".equalsIgnoreCase(globalTimezone)) {
                                String systemTimezone = backupRs.getString(2);
                                result = fixTimezoneEncoding(systemTimezone);
                            } else {
                                result = globalTimezone;
                            }
                            break;
                        case "postgresql":
                            // PostgreSQL备用查询直接返回当前时区设置
                            result = backupRs.getString(1);
                            break;
                        case "oracle":
                            // Oracle备用查询返回会话级别的时区
                            result = backupRs.getString(1);
                            break;
                        case "microsoft sql server":
                            // SQL Server通过日期起始日和语言设置推断时区
                            int dateFirst = backupRs.getInt(1);
                            String language = backupRs.getString(2);
                            result = inferSqlServerTimezone(dateFirst, language);
                            break;
                        default:
                            // 不支持的数据库类型，默认返回SYSTEM
                            log.debug("不支持的数据库类型备用查询: {}", databaseProductName);
                            result = "SYSTEM";
                            break;
                    }
                    return result;
                }
            }
        } catch (SQLException e) {
            log.debug("备用时区查询失败，使用SYSTEM", e);
        }
        return "SYSTEM";
    }

    /**
     * 修复时区字符串的编码问题，特别处理中文乱码场景
     * 修复流程：
     * 1. 检查是否为有效时区格式，如果是则直接返回
     * 2. 尝试多种字符编码转换（UTF-8、ISO-8859-1、GBK等）
     * 3. 基于乱码特征推断时区（如检测中文字节模式）
     * 4. 所有修复尝试失败后返回SYSTEM
     *
     * @param timezone 可能存在编码问题的时区字符串
     * @return 修复后的有效时区字符串，无法修复时返回"SYSTEM"
     */
    private static String fixTimezoneEncoding(String timezone) {
        if (timezone == null || timezone.trim().isEmpty()) {
            return "SYSTEM";
        }
        // 尝试验证时区格式的有效性，支持多种标准格式
        String trimmed = timezone.trim();
        if (isValidTimezoneFormat(trimmed)) {
            return trimmed;
        }

        // 尝试多种字符编码进行转换修复，常见于MySQL返回的SYSTEM时区乱码问题
        String[] encodings = {"UTF-8", "ISO-8859-1", "GBK", "GB2312", "Windows-1252"};
        for (String encoding : encodings) {
            try {
                byte[] bytes = timezone.getBytes(encoding);
                String fixedTimezone = new String(bytes, StandardCharsets.UTF_8);
                if (isValidTimezoneFormat(fixedTimezone)) {
                    log.debug("成功修复时区编码: 从 {} 编码转换为有效时区: {}", encoding, fixedTimezone);
                    return fixedTimezone;
                }
            } catch (Exception e) {
                log.trace("编码 {} 转换失败", encoding);
            }
        }

        // 编码转换均失败后，尝试基于乱码特征智能推断时区
        String inferredTimezone = inferSystemTimezone(timezone);
        if (inferredTimezone != null) {
            log.trace("基于乱码字符串推断时区: {} -> {}", timezone, inferredTimezone);
            return inferredTimezone;
        }

        log.trace("无法修复时区编码: {}, 使用SYSTEM", timezone);
        return "SYSTEM";
    }

    /**
     * 验证时区字符串格式是否符合标准规范
     * 支持的格式包括：
     * - IANA时区格式：Asia/Shanghai、America/New_York等
     * - UTC/GMT偏移格式：UTC+8、GMT-5等
     * - 缩写格式：CST、PST、EST等（2-4个大写字母）
     * - 数字偏移格式：+08:00、-05:00等
     * - 特殊关键字：SYSTEM、LOCAL
     *
     * @param timezone 待验证的时区字符串
     * @return 格式有效返回true，否则返回false
     */
    private static boolean isValidTimezoneFormat(String timezone) {
        if (timezone == null || timezone.trim().isEmpty()) {
            return false;
        }
        // 依次匹配各种标准时区格式模式
        String trimmed = timezone.trim();
        if (trimmed.matches("[a-zA-Z]+/[a-zA-Z_]+")) {
            return true;
        }
        if (trimmed.matches("(UTC|GMT)[+-]\\d+")) {
            return true;
        }
        if (trimmed.matches("[A-Z]{2,4}")) {
            return true;
        }
        if (trimmed.matches("[+-]\\d{1,2}:\\d{2}")) {
            return true;
        }
        if ("SYSTEM".equalsIgnoreCase(trimmed) || "LOCAL".equalsIgnoreCase(trimmed)) {
            return true;
        }
        return false;
    }

    /**
     * 基于乱码字符串的特征推断原始时区
     * 推断策略：
     * 1. 检测字节数组中是否包含负值字节（中文字符特征），推断为亚洲/上海时区
     * 2. 对于短字符串（&lt;=10字符），尝试匹配常见时区缩写或城市名
     * 3. 其他情况返回null表示无法推断
     *
     * @param garbledTimezone 存在编码问题的乱码时区字符串
     * @return 推断出的时区字符串，无法推断时返回null
     */
    private static String inferSystemTimezone(String garbledTimezone) {
        if (garbledTimezone == null) {
            return null;
        }
        byte[] bytes = garbledTimezone.getBytes();
        if (containsChineseTimezonePattern(bytes)) {
            return "Asia/Shanghai";
        }
        if (garbledTimezone.length() <= 10) {
            return inferFromShortString(garbledTimezone);
        }
        return null;
    }

    /**
     * 检测字节数组中是否包含中文字符的特征模式
     * 中文字符在UTF-8或其他多字节编码中通常表现为负值字节
     *
     * @param bytes 待检测的字节数组
     * @return 检测到中文特征返回true，否则返回false
     */
    private static boolean containsChineseTimezonePattern(byte[] bytes) {
        if (bytes == null || bytes.length < 2) {
            return false;
        }
        for (byte b : bytes) {
            if (b < 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * 从简短的时区字符串推断完整的时区标识符
     * 支持识别以下模式：
     * - 常见时区缩写：CST、PST、EST、GMT、JST等
     * - 城市/国家名称：CHINA、BEIJING、TOKYO、LONDON等
     * - 纯数字偏移量：8、9、-5等，转换为对应时区
     *
     * @param shortString 简短的时区字符串（通常&lt;=10字符）
     * @return 推断出的完整时区标识符，无法识别时返回null
     */
    private static String inferFromShortString(String shortString) {
        String upper = shortString.toUpperCase();
        if ("CST".equals(upper) || "CHINA".equals(upper) || "BEIJING".equals(upper)) {
            return "Asia/Shanghai";
        }
        // 匹配常见时区缩写和城市名称
        if ("PST".equals(upper) || "PDT".equals(upper) || "LOS ANGELES".equals(upper)) {
            return "America/Los_Angeles";
        }
        if ("EST".equals(upper) || "EDT".equals(upper) || "NEW YORK".equals(upper)) {
            return "America/New_York";
        }
        if ("GMT".equals(upper) || "UTC".equals(upper) || "LONDON".equals(upper)) {
            return "Europe/London";
        }
        if ("JST".equals(upper) || "TOKYO".equals(upper)) {
            return "Asia/Tokyo";
        }
        // 如果字符串是纯数字，将其解释为时区偏移量并转换为对应时区
        if (shortString.matches("[+-]?\\d+")) {
            int offset = Integer.parseInt(shortString.replace("+", ""));
            return convertOffsetToTimezone(offset);
        }
        return null;
    }

    /**
     * 将数字偏移量转换为对应的标准时区标识符
     * 内置了常见偏移量到主要城市时区的映射关系：
     * +8: 北京/上海, +9: 东京, 0: UTC, -5: 纽约, -8: 洛杉矶, +1: 巴黎
     * 对于未内置的偏移量，生成UTC±N格式的时区字符串
     *
     * @param offset 时区偏移量（小时数），如8表示东八区，-5表示西五区
     * @return 对应的时区标识符字符串
     */
    private static String convertOffsetToTimezone(int offset) {
        String result;
        switch (offset) {
            case 8:
                result = "Asia/Shanghai";
                break;
            case 9:
                result = "Asia/Tokyo";
                break;
            case 0:
                result = "UTC";
                break;
            case -5:
                result = "America/New_York";
                break;
            case -8:
                result = "America/Los_Angeles";
                break;
            case 1:
                result = "Europe/Paris";
                break;
            default:
                result = String.format("UTC%+d", offset);
                break;
        }
        return result;
    }

    /**
     * 根据数据库连接获取对应的备用时区查询语句
     * 从DB_BACKUP_TIMEZONE_QUERIES映射表中查找指定数据库类型的查询语句
     *
     * @param connection 数据库连接，用于获取数据库产品类型
     * @return 备用时区查询SQL语句，不支持的数据库类型返回null
     * @throws SQLException 获取数据库元数据失败时抛出异常
     */
    private static String getBackupTimezoneQuery(Connection connection) throws SQLException {
        String databaseProductName = connection.getMetaData().getDatabaseProductName().toLowerCase();
        return DB_BACKUP_TIMEZONE_QUERIES.get(databaseProductName);
    }

    /**
     * 根据SQL Server的语言设置和日期起始日推断时区
     * 主要通过language参数中的区域信息进行判断：
     * - 中文（简体/繁体）-> 亚洲/上海
     * - 日文 -> 亚洲/东京
     * - 英文（美国）-> 美洲/纽约
     * - 英文（英国）-> 欧洲/伦敦
     * 无法推断时默认返回UTC
     *
     * @param dateFirst 日期起始日参数（1-7，暂未使用）
     * @param language SQL Server的语言设置字符串，如"简体中文"、"us_english"等
     * @return 推断出的时区标识符，无法推断时返回UTC
     */
    private static String inferSqlServerTimezone(int dateFirst, String language) {
        if (language != null) {
            String lowerLanguage = language.toLowerCase();
            if (lowerLanguage.contains("chinese") || lowerLanguage.contains("zh-cn") || lowerLanguage.contains("zh-tw")) {
                return "Asia/Shanghai";
            } else if (lowerLanguage.contains("japanese")) {
                return "Asia/Tokyo";
            } else if (lowerLanguage.contains("english") && lowerLanguage.contains("us")) {
                return "America/New_York";
            } else if (lowerLanguage.contains("english") && lowerLanguage.contains("uk")) {
                return "Europe/London";
            }
        }
        return "UTC";
    }

    /**
     * 解析时区字符串为ZoneId对象
     * 处理逻辑：
     * 1. 空值或空白字符串返回null
     * 2. SYSTEM关键字表示使用系统默认时区，返回null
     * 3. 尝试使用ZoneId.of()解析标准时区格式
     * 4. 解析失败返回null，降级使用系统默认时区
     *
     * @param timezone 待解析的时区字符串
     * @return 解析后的ZoneId对象，无法解析或为SYSTEM时返回null
     */
    private static ZoneId parseTimezone(String timezone) {
        if (timezone == null || timezone.trim().isEmpty()) {
            return null;
        }
        String trimmedTimezone = timezone.trim();
        if ("SYSTEM".equalsIgnoreCase(trimmedTimezone)) {
            log.debug("数据库时区设置为SYSTEM，使用系统默认时区: {}", ZoneId.systemDefault());
            return null;
        }
        try {
            return ZoneId.of(trimmedTimezone);
        } catch (DateTimeException e) {
            log.debug("无法解析时区字符串: {}, 使用系统默认时区", trimmedTimezone);
            return null;
        }
    }

    /**
     * 根据数据库类型获取对应的时区查询SQL语句
     * 从DB_TIMEZONE_QUERIES映射表中查找，不支持的数据库类型会抛出异常
     *
     * @param connection 数据库连接，用于获取数据库产品名称
     * @return 时区查询SQL语句
     * @throws SQLException 当数据库类型不在支持列表中时抛出异常
     */
    private static String getTimezoneQuery(Connection connection) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        String databaseProductName = metaData.getDatabaseProductName().toLowerCase();
        String query = DB_TIMEZONE_QUERIES.get(databaseProductName);
        if (query != null) {
            return query;
        }
        throw new SQLException("不支持的数据库类型: " + databaseProductName);
    }

    /**
     * 从JDBC URL中提取serverTimezone参数的值
     * 支持处理URL中存在多个参数的场景，正确截取时区参数值到下一个&amp;符号之前
     *
     * @param url JDBC连接URL字符串，如jdbc:mysql://localhost:3306/db?serverTimezone=Asia/Shanghai&amp;useSSL=false
     * @return 提取的时区参数值，URL为空或未包含时区参数时返回null
     */
    private static String extractTimezoneFromUrl(String url) {
        if (url == null) {
            return null;
        }
        int timezoneIndex = url.indexOf(TIMEZONE_PARAM);
        if (timezoneIndex == -1) {
            return null;
        }
        int startIndex = timezoneIndex + TIMEZONE_PARAM.length();
        int endIndex = url.indexOf('&', startIndex);
        if (endIndex == -1) {
            return url.substring(startIndex);
        }
        return url.substring(startIndex, endIndex);
    }
}