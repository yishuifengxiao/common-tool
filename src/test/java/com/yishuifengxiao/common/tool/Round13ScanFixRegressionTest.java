package com.yishuifengxiao.common.tool;

import com.yishuifengxiao.common.tool.asn1.ASNValueReader;
import com.yishuifengxiao.common.tool.bean.JsonUtil;
import com.yishuifengxiao.common.tool.codec.EccKeyAgreement;
import com.yishuifengxiao.common.tool.http.UrlUtil;
import com.yishuifengxiao.common.tool.jdbc.JdbcUrlHelper;
import com.yishuifengxiao.common.tool.jdbc.SimpleRowMapper;
import com.yishuifengxiao.common.tool.jdbc.ZoneIdDetector;
import com.yishuifengxiao.common.tool.lang.NumberUtil;
import com.yishuifengxiao.common.tool.random.IdWorker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Round13 全局扫描问题修复回归测试
 * <p>
 * 覆盖 ZoneIdDetector 空 URL/时区参数大小写与字面量加号、UrlUtil 非法转义兼容、
 * JdbcUrlHelper 参数编解码可逆、NumberUtil 溢出处理、EccKeyAgreement 参数与共享秘密校验、
 * ASNValueReader HEX 校验、SimpleRowMapper 日期时间回退转换、IdWorker 无同步 UUID。
 * </p>
 *
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
@DisplayName("Round13 扫描修复回归测试")
class Round13ScanFixRegressionTest {

    /**
     * Instant 目标字段测试实体
     */
    public static class InstantEntity {
        Instant eventTime;

        public InstantEntity() {
        }
    }

    /**
     * LocalDate 目标字段测试实体
     */
    public static class LocalDateEntity {
        LocalDate eventDate;

        public LocalDateEntity() {
        }
    }

    /**
     * 含 null 字段的测试实体
     */
    public static class NullableFieldBean {
        public int a = 1;
        public String b;

        public NullableFieldBean() {
        }
    }

    @Mock
    private Connection connection;

    @Mock
    private DatabaseMetaData databaseMetaData;

    @Mock
    private Statement statement;

    @Mock
    private ResultSet resultSet;

    @Mock
    private ResultSetMetaData metaData;

    @BeforeEach
    void setUp() throws SQLException {
        MockitoAnnotations.openMocks(this);
        when(connection.getMetaData()).thenReturn(databaseMetaData);
    }

    @Test
    @DisplayName("ZoneIdDetector 元数据 URL 为 null 时不抛 NPE")
    void detectDatabaseTimezoneHandlesNullUrl() throws SQLException {
        when(databaseMetaData.getURL()).thenReturn(null);
        when(connection.createStatement()).thenThrow(new SQLException("unsupported"));

        assertNull(ZoneIdDetector.detectDatabaseTimezone(connection));
    }

    @Test
    @DisplayName("ZoneIdDetector 时区参数名大小写不敏感")
    void detectDatabaseTimezoneIgnoresParameterCase() throws SQLException {
        when(databaseMetaData.getURL())
                .thenReturn("jdbc:mysql://localhost:3306/db?SERVERTIMEZONE=Asia/Shanghai&useSSL=false");

        assertEquals(ZoneId.of("Asia/Shanghai"), ZoneIdDetector.detectDatabaseTimezone(connection));
    }

    @Test
    @DisplayName("ZoneIdDetector 支持 Etc/GMT+8 且 + 按字面量处理")
    void detectDatabaseTimezoneSupportsEtcGmtPlusOffset() throws SQLException {
        when(databaseMetaData.getURL())
                .thenReturn("jdbc:mysql://localhost:3306/db?serverTimezone=Etc/GMT+8");

        assertEquals(ZoneId.of("Etc/GMT+8"), ZoneIdDetector.detectDatabaseTimezone(connection));
    }

    @Test
    @DisplayName("UrlUtil.queryStringToMap 对非法转义的 key 回退原始值")
    void queryStringToMapToleratesInvalidEscape() {
        Map<String, String> result = UrlUtil.queryStringToMap("a%=1&b=2");

        assertEquals("1", result.get("a%"));
        assertEquals("2", result.get("b"));
    }

    @Test
    @DisplayName("JdbcUrlHelper.useDefaultTimeZone 在参数容器为空时可正常调用")
    void useDefaultTimeZoneInitializesParameters() {
        JdbcUrlHelper.ConnInfo info = new JdbcUrlHelper.ConnInfo().useDefaultTimeZone();

        assertNotNull(info.getParameters());
        assertEquals("true", info.getParameters().get("useTimezone"));
    }

    @Test
    @DisplayName("JdbcUrlHelper 解析时 + 按字面量保留并可再次构建")
    void parseAndBuildKeepsLiteralPlus() {
        JdbcUrlHelper.ConnInfo info = JdbcUrlHelper.parseJdbcUrl(
                "jdbc:mysql://localhost:3306/db?serverTimezone=GMT+8");

        assertEquals("GMT+8", info.getParameters().get("serverTimezone"));
        assertTrue(JdbcUrlHelper.buildJdbcUrl(info).contains("serverTimezone=GMT%2B8"));
    }

    @Test
    @DisplayName("JdbcUrlHelper 解析已编码的 %2B 得到字面量加号")
    void parseDecodesEncodedPlus() {
        JdbcUrlHelper.ConnInfo info = JdbcUrlHelper.parseJdbcUrl(
                "jdbc:mysql://localhost:3306/db?serverTimezone=GMT%2B8");

        assertEquals("GMT+8", info.getParameters().get("serverTimezone"));
    }

    @Test
    @DisplayName("NumberUtil 解析溢出时返回 null 而不是静默回绕")
    void numberUtilRejectsOverflow() {
        assertNull(NumberUtil.parseInt("99999999999"));
        assertNull(NumberUtil.parseLong("99999999999999999999"));
        assertEquals(Integer.valueOf(123), NumberUtil.parseInt("123.9"));
        assertEquals(Long.valueOf(123L), NumberUtil.parseLong("123.9"));
    }

    @Test
    @DisplayName("EccKeyAgreement 拒绝非正派生长度")
    void eccRejectsNonPositiveKeyLength() {
        assertThrows(IllegalArgumentException.class,
                () -> EccKeyAgreement.eccKeyAgreement("1.2.840.10045.3.1.7", "04", "01", "", 0, true));
    }

    @Test
    @DisplayName("EccKeyAgreement 双方协商结果一致且 null 共享信息等价空串")
    void eccKeyAgreementMatchesBetweenParties() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("EC");
        generator.initialize(new ECGenParameterSpec("secp256r1"));
        KeyPair alice = generator.generateKeyPair();
        KeyPair bob = generator.generateKeyPair();

        String curveOid = "1.2.840.10045.3.1.7";
        String alicePrivate = toHex(((ECPrivateKey) alice.getPrivate()).getS().toByteArray());
        String bobPrivate = toHex(((ECPrivateKey) bob.getPrivate()).getS().toByteArray());
        String alicePublic = encodeUncompressed((ECPublicKey) alice.getPublic(), 32);
        String bobPublic = encodeUncompressed((ECPublicKey) bob.getPublic(), 32);

        String aliceSecret = EccKeyAgreement.eccKeyAgreement(curveOid, bobPublic, alicePrivate, "", 32, true);
        String bobSecret = EccKeyAgreement.eccKeyAgreement(curveOid, alicePublic, bobPrivate, "", 32, true);
        String nullShareInfo = EccKeyAgreement.eccKeyAgreement(curveOid, bobPublic, alicePrivate, null, 32, true);

        assertEquals(aliceSecret, bobSecret);
        assertEquals(aliceSecret, nullShareInfo);
    }

    @Test
    @DisplayName("ASNValueReader.hexStringToByteArray 拒绝奇数长度与非法字符")
    void asnHexRejectsMalformedInput() throws Exception {
        Method method = ASNValueReader.class.getDeclaredMethod("hexStringToByteArray", String.class);
        method.setAccessible(true);

        assertArrayEquals(new byte[]{0x0A, 0x0B}, (byte[]) method.invoke(null, "0A0B"));
        InvocationTargetException oddLength = assertThrows(InvocationTargetException.class,
                () -> method.invoke(null, "0A0"));
        assertInstanceOf(IllegalArgumentException.class, oddLength.getCause());
        InvocationTargetException illegalChar = assertThrows(InvocationTargetException.class,
                () -> method.invoke(null, "0G"));
        assertInstanceOf(IllegalArgumentException.class, illegalChar.getCause());
    }

    @Test
    @DisplayName("SimpleRowMapper 字符串回退按数据库时区转换 Instant")
    void mapRowConvertsStringToInstantWithDatabaseZone() throws SQLException {
        when(resultSet.getMetaData()).thenReturn(metaData);
        when(metaData.getColumnCount()).thenReturn(1);
        when(metaData.getColumnLabel(1)).thenReturn(null);
        when(metaData.getColumnName(1)).thenReturn("event_time");
        when(resultSet.getObject(1)).thenReturn("2025-01-01 12:00:00");
        when(resultSet.getTimestamp("event_time")).thenThrow(new SQLException("unsupported"));
        when(resultSet.getObject("event_time")).thenReturn("2025-01-01 12:00:00");

        ZoneId zone = ZoneId.of("Asia/Shanghai");
        SimpleRowMapper<InstantEntity> mapper = new SimpleRowMapper<>(InstantEntity.class, zone);
        InstantEntity entity = mapper.mapRow(resultSet, 1);

        assertEquals(LocalDateTime.of(2025, 1, 1, 12, 0, 0).atZone(zone).toInstant(), entity.eventTime);
    }

    @Test
    @DisplayName("SimpleRowMapper 原始 LocalDateTime 可转换为 LocalDate")
    void mapRowConvertsLocalDateTimeToLocalDate() throws SQLException {
        LocalDateTime rawValue = LocalDateTime.of(2025, 1, 1, 12, 0, 0);
        when(resultSet.getMetaData()).thenReturn(metaData);
        when(metaData.getColumnCount()).thenReturn(1);
        when(metaData.getColumnLabel(1)).thenReturn(null);
        when(metaData.getColumnName(1)).thenReturn("event_date");
        when(resultSet.getObject(1)).thenReturn(rawValue);
        when(resultSet.getDate("event_date")).thenThrow(new SQLException("unsupported"));
        when(resultSet.getObject("event_date")).thenReturn(rawValue);

        SimpleRowMapper<LocalDateEntity> mapper = new SimpleRowMapper<>(LocalDateEntity.class);
        LocalDateEntity entity = mapper.mapRow(resultSet, 1);

        assertEquals(LocalDate.of(2025, 1, 1), entity.eventDate);
    }

    @Test
    @DisplayName("IdWorker.uuid 无同步也能保证唯一性")
    void idWorkerUuidIsUniqueUnderParallelCalls() {
        Set<String> values = IntStream.range(0, 2000)
                .parallel()
                .mapToObj(i -> IdWorker.uuid())
                .collect(Collectors.toSet());

        assertEquals(2000, values.size());
    }

    @Test
    @DisplayName("JsonUtil.toJSONString 不包含 null 字段")
    void jsonUtilExcludesNullFields() {
        String json = JsonUtil.toJSONString(false, new NullableFieldBean());

        assertNotNull(json);
        assertFalse(json.contains("\"b\""));
    }

    private static String encodeUncompressed(ECPublicKey publicKey, int fieldSize) {
        byte[] x = toFixedLength(publicKey.getW().getAffineX(), fieldSize);
        byte[] y = toFixedLength(publicKey.getW().getAffineY(), fieldSize);
        byte[] encoded = new byte[1 + x.length + y.length];
        encoded[0] = 0x04;
        System.arraycopy(x, 0, encoded, 1, x.length);
        System.arraycopy(y, 0, encoded, 1 + x.length, y.length);
        return toHex(encoded);
    }

    private static byte[] toFixedLength(BigInteger value, int length) {
        byte[] bytes = value.toByteArray();
        if (bytes.length == length) {
            return bytes;
        }
        byte[] fixed = new byte[length];
        if (bytes.length > length) {
            System.arraycopy(bytes, bytes.length - length, fixed, 0, length);
        } else {
            System.arraycopy(bytes, 0, fixed, length - bytes.length, bytes.length);
        }
        return fixed;
    }

    private static String toHex(byte[] bytes) {
        StringBuilder builder = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            builder.append(Character.forDigit((b >> 4) & 0x0F, 16));
            builder.append(Character.forDigit(b & 0x0F, 16));
        }
        return builder.toString();
    }
}
