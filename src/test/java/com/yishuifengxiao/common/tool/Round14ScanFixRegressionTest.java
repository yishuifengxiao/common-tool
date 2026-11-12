package com.yishuifengxiao.common.tool;

import com.yishuifengxiao.common.tool.codec.AES;
import com.yishuifengxiao.common.tool.codec.AesCbc;
import com.yishuifengxiao.common.tool.codec.SHA256;
import com.yishuifengxiao.common.tool.entity.Page;
import com.yishuifengxiao.common.tool.lang.OID;
import com.yishuifengxiao.common.tool.sensitive.SensitiveUtil;
import com.yishuifengxiao.common.tool.text.PlaceholderExtractor;
import com.yishuifengxiao.common.tool.utils.GpsUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Round14 全局扫描问题修复回归测试
 * <p>
 * 覆盖 AES 密钥确定性派生、OID 节点范围校验、经纬度校验、手机号脱敏归一化、
 * 分页负数边界、AesCbc 严格去填充、SHA256 空值、占位符空值处理。
 * </p>
 *
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
@DisplayName("Round14 扫描修复回归测试")
class Round14ScanFixRegressionTest {

    @Test
    @DisplayName("AES 使用同一密钥可正确解密，且同一明文多次加密结果一致")
    void aesRoundTripIsDeterministic() {
        String cipherText = AES.encrypt("my-secret-key", "hello world");

        assertNotNull(cipherText);
        assertEquals("hello world", AES.decrypt("my-secret-key", cipherText));
        assertEquals(cipherText, AES.encrypt("my-secret-key", "hello world"));
        assertNull(AES.decrypt("other-key", cipherText));
    }

    @Test
    @DisplayName("AES 默认密钥同样支持加解密往返")
    void aesDefaultKeyRoundTrip() {
        String cipherText = AES.encrypt("yishui-default-data");

        assertNotNull(cipherText);
        assertEquals("yishui-default-data", AES.decrypt(cipherText));
    }

    @Test
    @DisplayName("OID 节点超出 2^32-1 时抛出非法参数异常")
    void oidRejectsOutOfRangeComponent() {
        assertNotNull(OID.dotNotationToHex("2.4294967295"));
        assertThrows(IllegalArgumentException.class, () -> OID.dotNotationToHex("2.4294967296"));
        assertThrows(IllegalArgumentException.class, () -> OID.dotNotationToHex("1.40.4294967296"));
    }

    @Test
    @DisplayName("Page.page 对非正总量返回 0")
    void pageReturnsZeroForNonPositiveTotal() {
        assertEquals(0, Page.page(-5, 10).intValue());
        assertEquals(0, Page.page(0, 10).intValue());
        assertEquals(3, Page.page(25, 10).intValue());
    }

    @Test
    @DisplayName("GpsUtil 拒绝非法经纬度与 NaN")
    void gpsRejectsInvalidCoordinates() {
        assertThrows(IllegalArgumentException.class, () -> GpsUtil.distance(0, 91, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> GpsUtil.distanceKm(181, 0, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> GpsUtil.distance(Double.NaN, 0, 0, 0));
        assertEquals(0L, GpsUtil.distance(120.0, 30.0, 120.0, 30.0));
    }

    @Test
    @DisplayName("手机号脱敏兼容 +86、空格与连字符格式")
    void phoneMaskingNormalizesFormat() {
        assertEquals("138****5678", SensitiveUtil.phone("13812345678"));
        assertEquals("138****5678", SensitiveUtil.phone("+86 138-1234-5678"));
        assertEquals("123", SensitiveUtil.phone("123"));
        assertNull(SensitiveUtil.phone(null));
    }

    @Test
    @DisplayName("AesCbc 严格去填充在填充非法时抛出异常")
    void aesCbcStrictUnpadRejectsInvalidPadding() {
        byte[] padded = AesCbc.padData(new byte[]{1, 2, 3});
        assertArrayEquals(new byte[]{1, 2, 3}, AesCbc.unpadDataStrict(padded));
        assertThrows(IllegalArgumentException.class,
                () -> AesCbc.unpadDataStrict(new byte[]{1, 2, 3, 4, 4, 4, 4, 9}));
    }

    @Test
    @DisplayName("SHA256 对 null 输入返回 null")
    void sha256HandlesNull() {
        assertNull(SHA256.calculateSHA256((String) null));
        assertNull(SHA256.calculateSHA256((byte[]) null));
        // Hex.bytesToHex 输出大写十六进制，此处固定既有契约
        assertEquals("BA7816BF8F01CFEA414140DE5DAE2223B00361A396177A9CB410FF61F20015AD",
                SHA256.calculateSHA256("abc"));
    }

    @Test
    @DisplayName("PlaceholderExtractor 空白输入原样返回且允许空替换映射")
    void placeholderHandlesBlankAndNullMap() {
        assertEquals(" ", PlaceholderExtractor.replacePlaceholders(" ", null));
        assertEquals("${a}", PlaceholderExtractor.replacePlaceholders("${a}", null));
        assertEquals("1", PlaceholderExtractor.replacePlaceholders("${a}", Map.of("a", "1")));
    }
}
