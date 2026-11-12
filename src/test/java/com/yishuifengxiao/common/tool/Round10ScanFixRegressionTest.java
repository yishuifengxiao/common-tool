package com.yishuifengxiao.common.tool;

import com.yishuifengxiao.common.tool.codec.ECC;
import com.yishuifengxiao.common.tool.http.UrlUtil;
import com.yishuifengxiao.common.tool.random.RandomUtil;
import com.yishuifengxiao.common.tool.utils.GpsUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.interfaces.ECPrivateKey;
import java.util.Locale;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 第 10 轮回归测试：扫描问题修复验证
 *
 * <p>覆盖内容：</p>
 * <ul>
 * <li>GpsUtil 千米结果四舍五入，不再因截断使 1 千米内恒为 0</li>
 * <li>RandomUtil 复用共享随机源后输出格式与长度不变，非法入参仍抛异常</li>
 * <li>UrlUtil.extractProtocol 在空串、无协议时返回 null，正常 URL 返回协议名</li>
 * <li>ECC 对大小写混用的 PEM 头尾仍可识别与解析，且在土耳其语 Locale 下不受影响</li>
 * </ul>
 *
 * @author qingteng
 * @version 1.0.0
 */
@DisplayName("第10轮：扫描问题修复回归测试")
public class Round10ScanFixRegressionTest {

    /**
     * 土耳其语 Locale，用于验证大小写归一化未受默认 Locale 影响
     */
    private static final Locale TURKISH = Locale.forLanguageTag("tr-TR");

    /**
     * 在土耳其语默认 Locale 下执行给定逻辑，执行完毕后恢复原 Locale
     *
     * @param supplier 待执行的逻辑
     * @param <T>      返回类型
     * @return 逻辑执行结果
     */
    private <T> T underTurkishLocale(Supplier<T> supplier) {
        Locale original = Locale.getDefault();
        try {
            Locale.setDefault(TURKISH);
            return supplier.get();
        } finally {
            Locale.setDefault(original);
        }
    }

    /**
     * 0.0045 度纬度约 0.5009 千米：修复前截断为 0，修复后应四舍五入为 1 千米
     */
    @Test
    @DisplayName("GpsUtil.distanceKm 对 1 千米内距离四舍五入")
    void gpsDistanceKmRoundsHalfKilometerUp() {
        assertEquals(1L, GpsUtil.distanceKm(0d, 0d, 0d, 0.0045d));
        assertEquals(501L, GpsUtil.distance(0d, 0d, 0d, 0.0045d));
    }

    /**
     * 同经度 1 度纬度差约为 111.319 千米（地球半径 6378.137 千米）
     */
    @Test
    @DisplayName("GpsUtil 已知距离换算符合预期")
    void gpsKnownDistance() {
        assertEquals(111319L, GpsUtil.distance(0d, 0d, 0d, 1d));
        assertEquals(111L, GpsUtil.distanceKm(0d, 0d, 0d, 1d));
    }

    /**
     * 随机十六进制字符串长度应为 2 * numBytes，且仅包含小写十六进制字符
     */
    @Test
    @DisplayName("RandomUtil.generateRandomHexString 输出长度与字符集不变")
    void randomHexStringFormat() {
        String hex = RandomUtil.generateRandomHexString(8);
        assertEquals(16, hex.length());
        assertTrue(hex.matches("[0-9a-f]{16}"));
        assertThrows(IllegalArgumentException.class, () -> RandomUtil.generateRandomHexString(0));
    }

    /**
     * 汉字文本长度应为请求的字符数
     */
    @Test
    @DisplayName("RandomUtil.generateChineseText 长度不变")
    void randomChineseTextLength() {
        assertEquals(5, RandomUtil.generateChineseText(5).length());
    }

    /**
     * 协议提取：空值/空白/无协议返回 null，正常 URL 返回协议名
     */
    @Test
    @DisplayName("UrlUtil.extractProtocol 边界与正常场景")
    void urlExtractProtocol() {
        assertNull(UrlUtil.extractProtocol(null));
        assertNull(UrlUtil.extractProtocol("   "));
        assertNull(UrlUtil.extractProtocol("www.example.com/path"));
        assertEquals("https", UrlUtil.extractProtocol("https://example.com/path"));
        assertEquals("http", UrlUtil.extractProtocol("http://example.com"));
    }

    /**
     * 小写 PEM 头尾也应被识别为 EC 私钥格式
     */
    @Test
    @DisplayName("ECC.detectKeyFormat 忽略 PEM 头大小写")
    void eccDetectKeyFormatIgnoresCase() {
        String lowerPem = "-----begin ec private key-----\nAAAA\n-----end ec private key-----";
        assertEquals(ECC.KeyFormat.PEM_EC, ECC.detectKeyFormat(lowerPem));
        assertEquals(ECC.KeyFormat.PEM_EC, underTurkishLocale(() -> ECC.detectKeyFormat(lowerPem)));
    }

    /**
     * 小写 PEM 头包装的私钥应能正常解析，且 D 值与原始私钥一致
     *
     * @throws Exception 密钥生成或解析失败时抛出
     */
    @Test
    @DisplayName("ECC.parsePEMPrivateKey 支持小写 PEM 头")
    void eccParsePrivateKeyWithLowercasePemHeader() throws Exception {
        KeyPair keyPair = ECC.generateECCKeyPair();
        String pem = ECC.ecPrivateKeyToPem((ECPrivateKey) keyPair.getPrivate());
        String lowerPem = pem.replace("-----BEGIN EC PRIVATE KEY-----", "-----begin ec private key-----")
                .replace("-----END EC PRIVATE KEY-----", "-----end ec private key-----");

        ECPrivateKey parsed = ECC.parsePEMPrivateKey(lowerPem);
        assertNotNull(parsed);
        assertEquals(((ECPrivateKey) keyPair.getPrivate()).getS(), parsed.getS());
    }

    /**
     * 小写 PEM 头包装的公钥应能正常解析（剥离标记不影响 Base64 正文大小写）
     *
     * @throws Exception 密钥生成或解析失败时抛出
     */
    @Test
    @DisplayName("ECC.parsePEMPublicKey 支持小写 PEM 头")
    void eccParsePublicKeyWithLowercasePemHeader() throws Exception {
        KeyPair keyPair = ECC.generateECCKeyPair();
        String pem = ECC.convertECPublicKeyToPem(
                com.yishuifengxiao.common.tool.lang.Hex.bytesToHex(keyPair.getPublic().getEncoded()));
        String lowerPem = pem.replace("-----BEGIN PUBLIC KEY-----", "-----begin public key-----")
                .replace("-----END PUBLIC KEY-----", "-----end public key-----");

        assertNotNull(ECC.parsePEMPublicKey(lowerPem));
    }
}
