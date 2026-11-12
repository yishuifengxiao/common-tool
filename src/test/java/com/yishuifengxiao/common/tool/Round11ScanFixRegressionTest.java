package com.yishuifengxiao.common.tool;

import com.yishuifengxiao.common.tool.codec.X509Helper;
import com.yishuifengxiao.common.tool.context.LocalCache;
import com.yishuifengxiao.common.tool.exception.UncheckedException;
import com.yishuifengxiao.common.tool.http.UrlUtil;
import com.yishuifengxiao.common.tool.lang.Hex;
import com.yishuifengxiao.common.tool.lang.TLV;
import com.yishuifengxiao.common.tool.lang.TLVFormatter;
import com.yishuifengxiao.common.tool.utils.CertNoUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 第 11 轮回归测试：扫描问题修复验证
 *
 * <p>覆盖内容：</p>
 * <ul>
 * <li>TLVFormatter 对带时区偏移的 UTCTime/GeneralizedTime 不再丢弃偏移</li>
 * <li>TLVFormatter 按 RFC 5280 将 UTCTime 的 50-99 年份映射到 1950-1999</li>
 * <li>X509Helper 支持小写 PEM 头，且不受默认 Locale 影响</li>
 * <li>CertNoUtil 校验码通过后仍会校验出生日期是否真实存在、是否晚于今天</li>
 * <li>UrlUtil.extractProtocol 支持 localhost、IP 等无点主机</li>
 * <li>TLV 标签大小写归一化在土耳其语 Locale 下保持一致</li>
 * <li>LocalCache.get(key, supplier) 命中缓存时不再调用 supplier</li>
 * </ul>
 *
 * @author qingteng
 * @version 1.0.0
 */
@DisplayName("第11轮：扫描问题修复回归测试")
public class Round11ScanFixRegressionTest {

    /**
     * 土耳其语 Locale，用于验证大小写归一化未受默认 Locale 影响
     */
    private static final Locale TURKISH = Locale.forLanguageTag("tr-TR");

    /**
     * 测试用 X.509 证书（Microsoft 公开证书，仅用于解析验证）
     */
    private static final String TEST_CERT_PEM = "-----BEGIN CERTIFICATE-----\n" +
            "MIIIbjCCBlagAwIBAgITMwHJ91v1GJSZPhZsYQAAAcn3WzANBgkqhkiG9w0BAQwF\n" +
            "ADBdMQswCQYDVQQGEwJVUzEeMBwGA1UEChMVTWljcm9zb2Z0IENvcnBvcmF0aW9u\n" +
            "MS4wLAYDVQQDEyVNaWNyb3NvZnQgQXp1cmUgUlNBIFRMUyBJc3N1aW5nIENBIDA0\n" +
            "MB4XDTI1MDIxOTA5MjYyNVoXDTI1MDgxODA5MjYyNVowbDELMAkGA1UEBhMCVVMx\n" +
            "CzAJBgNVBAgTAldBMRAwDgYDVQQHEwdSZWRtb25kMR4wHAYDVQQKExVNaWNyb3Nv\n" +
            "ZnQgQ29ycG9yYXRpb24xHjAcBgNVBAMTFXN1cHBvcnQubWljcm9zb2Z0LmNvbTCC\n" +
            "ASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBALNESH3H68B2clANWgXgabo3\n" +
            "wUOGQUK0vg65MtsKq1WcmfzGl/ZH+PEMkHkMQDsq4nOrgwczW4aSge98DEm8C0K/\n" +
            "gG6vBxcFE4iBpiJShWyBHdCy2CSUCJMjIzAIjV6GTK5IQtN0FbhR5P7qdUUstvID\n" +
            "U52x3C+v6HwQRr8h3dYgSNWQF1l+5jOe0rQnQ0Tr7AUDQNYMD4MDlWJpv03J8/ly\n" +
            "C68a7vMlyFSCxn2quzzfAJnfSTxVTPdFZ097iE3OEvYngJfNlXt9DQqfMGyTV0pJ\n" +
            "PFRXMVC1xt9N6dkTuSxQShsi57hB8FHRbfD9wIkJlVr9lDFguvHK+3LJnD8xZJUC\n" +
            "AwEAAaOCBBYwggQSMIIBfQYKKwYBBAHWeQIEAgSCAW0EggFpAWcAdQDd3Mo0ldfh\n" +
            "FgXnlTL6x5/4PRxQ39sAOhQSdgosrLvIKgAAAZUdkN40AAAEAwBGMEQCIGvE+UeV\n" +
            "cQqrypOZLfYo1wfBmdfDcADrxP4nv3POxA9sAiArx+yI42O6FpvgzjXW8mSC3uic\n" +
            "dFWYAgGH9Gj6GpGKKwB2AH1ZHhLheCp7HGFnfF79+NCHXBSgTpWeuQMv2Q6MLnm4\n" +
            "AAABlR2Q3e8AAAQDAEcwRQIgcofJyjlG+7A4dozJ/RqyOvJ3KU18ucwrvIgqzFvv\n" +
            "yBgCIQCXgWp1NYmnCmnMKFp6bNdhRvG4oEe45WDn9oPOsRboDQB2ABoE/0nQVB1A\n" +
            "r/agw7/x2MRnL07s7iNAaJhrF0Au3Il9AAABlR2Q3lIAAAQDAEcwRQIgCBqTTttc\n" +
            "KS9Qmx8ClKHRlBBfVJzpyf6RmehtQpC6LoMCIQC3M35TBeXPtdhvDUR66se4yPSV\n" +
            "Q8MI1OmboX3fI5/2RDAnBgkrBgEEAYI3FQoEGjAYMAoGCCsGAQUFBwMCMAoGCCsG\n" +
            "AQUFBwMBMDwGCSsGAQQBgjcVBwQvMC0GJSsGAQQBgjcVCIe91xuB5+tGgoGdLo7Q\n" +
            "DIfw2h1dgqvnMIft8R8CAWQCAS0wgbQGCCsGAQUFBwEBBIGnMIGkMHMGCCsGAQUF\n" +
            "BzAChmdodHRwOi8vd3d3Lm1pY3Jvc29mdC5jb20vcGtpb3BzL2NlcnRzL01pY3Jv\n" +
            "c29mdCUyMEF6dXJlJTIwUlNBJTIwVExTJTIwSXNzdWluZyUyMENBJTIwMDQlMjAt\n" +
            "JTIweHNpZ24uY3J0MC0GCCsGAQUFBzABhiFodHRwOi8vb25lb2NzcC5taWNyb3Nv\n" +
            "ZnQuY29tL29jc3AwHQYDVR0OBBYEFDp8HLn3sazI8pH6hLT4bV1fBkzzMA4GA1Ud\n" +
            "DwEB/wQEAwIFoDAgBgNVHREEGTAXghVzdXBwb3J0Lm1pY3Jvc29mdC5jb20wDAYD\n" +
            "VR0TAQH/BAIwADBqBgNVHR8EYzBhMF+gXaBbhllodHRwOi8vd3d3Lm1pY3Jvc29m\n" +
            "dC5jb20vcGtpb3BzL2NybC9NaWNyb3NvZnQlMjBBenVyZSUyMFJTQSUyMFRMUyUy\n" +
            "MElzc3VpbmclMjBDQSUyMDA0LmNybDBmBgNVHSAEXzBdMFEGDCsGAQQBgjdMg30B\n" +
            "ATBBMD8GCCsGAQUFBwIBFjNodHRwOi8vd3d3Lm1pY3Jvc29mdC5jb20vcGtpb3Bz\n" +
            "L0RvY3MvUmVwb3NpdG9yeS5odG0wCAYGZ4EMAQICMB8GA1UdIwQYMBaAFDtw0VPp\n" +
            "diWdYKjKZg/Gm65vVBZqMB0GA1UdJQQWMBQGCCsGAQUFBwMCBggrBgEFBQcDATAN\n" +
            "BgkqhkiG9w0BAQwFAAOCAgEAVxbVtMrdQAImLPf/Rne2GCmPyzhY4oeZD5miXcez\n" +
            "QVMAxjlx66aKV1SaBh/+MxbzLUF3qEJlPzFDnon1W/VJ6wuBCf9l7T7bKa+jFoio\n" +
            "7UqR3fVLLV3ySNBUgMHVL6e8h5djxVsmhNwW59xM6p0BacJGvyKnMIZ+eSHdqfkH\n" +
            "RunxjkAjByzAOjLHy+o2o/N5+pjxQp/uBgfQD4h3hMIb5ujA6Sn+S5gmrHGn7DPk\n" +
            "3wJcN+E964E+iB31KvKwcAv/FC01/cuv61T1w5viIqDxVXZlq5fvsFzcMj2XLS8x\n" +
            "NIaksMloC9jmN55ZGDTbaFhW5NMcoFHO9sJtBydOX13j5/lFRGxu3x6M5PFlPekb\n" +
            "A34UzttweG210y64nqaGPLAxkUK5BFd0WfIMHKfOU7DgaizyueTlJLYtF7SPSLq2\n" +
            "oCMggXR6TwUhL8xaWnNv/gxP42v2ZULV/7RYUPkcIndDqCfpqTVh6KSCFOzqqWnH\n" +
            "f9Y+4hLkFNXtGvYp9Irr1xyNxNIt6URwqKgp3d0aRLRwtoqAgLN7y3QeUIv1NJYj\n" +
            "6BilL9Nt/Xvcbk2BiC/USybXm6RIxKx7yAdFckup1LChsIPCpxd/3cgbwCoqRgrj\n" +
            "8KWLhnemJS7rY8HN7N+YCehQvg7RWLf+IKGCQkooPlGunLHkqHhD5YxMAWQA78Hd\n" +
            "/y8=\n" +
            "-----END CERTIFICATE-----";

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
     * 构造只包含一个 UTCTime（tag 0x17）的 TLV 十六进制串
     *
     * @param timeText UTCTime 的时间文本，如 {@code 250219092625Z}
     * @return TLV 十六进制串
     */
    private static String utcTimeTlv(String timeText) {
        byte[] value = timeText.getBytes(StandardCharsets.US_ASCII);
        return "17" + String.format("%02X", value.length) + Hex.bytesToHex(value);
    }

    /**
     * 构造只包含一个 GeneralizedTime（tag 0x18）的 TLV 十六进制串
     *
     * @param timeText GeneralizedTime 的时间文本，如 {@code 20250219092625Z}
     * @return TLV 十六进制串
     */
    private static String generalizedTimeTlv(String timeText) {
        byte[] value = timeText.getBytes(StandardCharsets.US_ASCII);
        return "18" + String.format("%02X", value.length) + Hex.bytesToHex(value);
    }

    /**
     * 按 GB 11643-1999 计算 18 位身份证号第 18 位校验码
     *
     * @param first17 前 17 位数字
     * @return 补上校验码后的 18 位号码
     */
    private static String withCheckDigit(String first17) {
        int[] weights = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};
        String[] codes = {"1", "0", "X", "9", "8", "7", "6", "5", "4", "3", "2"};
        int sum = 0;
        for (int i = 0; i < first17.length(); i++) {
            sum += (first17.charAt(i) - '0') * weights[i];
        }
        return first17 + codes[sum % 11];
    }

    /**
     * 解析证书，将受检异常转为运行时异常，便于在 Lambda 中使用
     *
     * @param pem PEM 格式证书文本
     * @return 解析得到的证书
     */
    private static X509Certificate parseCert(String pem) {
        try {
            return X509Helper.parseCert(pem);
        } catch (CertificateException e) {
            throw new UncheckedException(e);
        }
    }

    /**
     * 带 +08:00 偏移的 UTCTime 应保留偏移，而不是被当作 UTC
     */
    @Test
    @DisplayName("TLVFormatter 保留 UTCTime 的时区偏移")
    void utcTimeKeepsOffset() {
        String output = TLVFormatter.formatTLVStr(true, utcTimeTlv("250219092625+0800"));
        assertTrue(output.contains("2025-02-19 09:26:25 +08:00"), output);
        assertFalse(output.contains("2025-02-19 09:26:25 UTC"), output);
    }

    /**
     * RFC 5280：UTCTime 的 50-99 表示 1950-1999，00-49 表示 2000-2049
     */
    @Test
    @DisplayName("TLVFormatter 按 RFC 5280 修正 UTCTime 世纪")
    void utcTimeCenturyWindow() {
        assertTrue(TLVFormatter.formatTLVStr(true, utcTimeTlv("500101000000Z"))
                .contains("1950-01-01 00:00:00 UTC"));
        assertTrue(TLVFormatter.formatTLVStr(true, utcTimeTlv("490101000000Z"))
                .contains("2049-01-01 00:00:00 UTC"));
    }

    /**
     * 带 +08:00 偏移的 GeneralizedTime 应保留偏移
     */
    @Test
    @DisplayName("TLVFormatter 保留 GeneralizedTime 的时区偏移")
    void generalizedTimeKeepsOffset() {
        String output = TLVFormatter.formatTLVStr(true, generalizedTimeTlv("20250219092625+0800"));
        assertTrue(output.contains("2025-02-19 09:26:25.000 +08:00"), output);
        assertFalse(output.contains("UTC"), output);
    }

    /**
     * 小写 PEM 头尾应能正常解析，且不受默认 Locale 影响
     */
    @Test
    @DisplayName("X509Helper.parseCert 支持小写 PEM 头")
    void x509ParsesLowercasePemHeader() {
        String lowerPem = TEST_CERT_PEM
                .replace("-----BEGIN CERTIFICATE-----", "-----begin certificate-----")
                .replace("-----END CERTIFICATE-----", "-----end certificate-----");

        X509Certificate certificate = parseCert(lowerPem);
        assertNotNull(certificate);
        assertTrue(certificate.getSubjectX500Principal().getName().contains("CN=support.microsoft.com"));

        X509Certificate turkish = underTurkishLocale(() -> parseCert(lowerPem));
        assertNotNull(turkish);
        assertEquals(certificate.getSerialNumber(), turkish.getSerialNumber());
    }

    /**
     * 校验码正确但出生日期不存在（2023-02-31）时判为非法
     */
    @Test
    @DisplayName("CertNoUtil 拒绝不存在的出生日期")
    void certNoRejectsNonExistentDate() {
        String idCard = withCheckDigit("11010520230231001");
        assertEquals("31", idCard.substring(12, 14));
        assertFalse(CertNoUtil.isValid(idCard));
        assertThrows(UncheckedException.class, () -> CertNoUtil.extractBirthday(idCard));
    }

    /**
     * 出生日期晚于今天的号码判为非法
     */
    @Test
    @DisplayName("CertNoUtil 拒绝未来出生日期")
    void certNoRejectsFutureDate() {
        String idCard = withCheckDigit("11010520991231001");
        assertFalse(CertNoUtil.isValid(idCard));
    }

    /**
     * 真实存在的合法号码仍判定为合法，作为正向对照
     */
    @Test
    @DisplayName("CertNoUtil 仍接受合法号码")
    void certNoAcceptsValidId() {
        String idCard = "11010519491231002X";
        assertEquals(idCard, withCheckDigit("11010519491231002"));
        assertTrue(CertNoUtil.isValid(idCard));
    }

    /**
     * 无点主机（localhost）与 IP 主机也能提取协议
     */
    @Test
    @DisplayName("UrlUtil.extractProtocol 支持无点主机")
    void urlExtractProtocolHandlesHostWithoutDot() {
        assertEquals("http", UrlUtil.extractProtocol("http://localhost:8080/a"));
        assertEquals("https", UrlUtil.extractProtocol("https://127.0.0.1/x"));
        assertEquals("http", UrlUtil.extractProtocol("http://[::1]:8080/"));
    }

    /**
     * TLV 标签大小写归一化在土耳其语 Locale 下保持一致
     */
    @Test
    @DisplayName("TLV.extract 大小写归一化不受 Locale 影响")
    void tlvExtractConsistentUnderTurkishLocale() {
        TLV.TlvResult result = underTurkishLocale(() -> TLV.extract("ab", "ab0101"));
        assertTrue(result.isSuccess());
        assertEquals("01", result.getVal("ab"));
        assertEquals("01", result.getVal("AB"));
    }

    /**
     * LocalCache.get(key, supplier) 命中缓存时不再调用 supplier，未命中时回填
     */
    @Test
    @DisplayName("LocalCache.get(key, supplier) 命中后不再调用 supplier")
    void localCacheGetWithSupplier() {
        String key = "round11.localcache.hit";
        String missingKey = "round11.localcache.missing";
        LocalCache.put(key, "cache-value");
        LocalCache.remove(missingKey);

        AtomicInteger counter = new AtomicInteger();
        Supplier<String> supplier = () -> {
            counter.incrementAndGet();
            return "supplier-value";
        };

        assertEquals("cache-value", LocalCache.get(key, supplier));
        assertEquals(0, counter.get());

        assertEquals("supplier-value", LocalCache.get(missingKey, supplier));
        assertEquals(1, counter.get());
        assertEquals("supplier-value", LocalCache.get(missingKey, supplier));
        assertEquals(1, counter.get());
    }
}
