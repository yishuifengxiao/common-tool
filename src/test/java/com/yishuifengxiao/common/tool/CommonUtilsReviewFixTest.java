package com.yishuifengxiao.common.tool;

import com.yishuifengxiao.common.tool.asn1.Asn1Util;
import com.yishuifengxiao.common.tool.bean.ClassUtil;
import com.yishuifengxiao.common.tool.codec.DES;
import com.yishuifengxiao.common.tool.codec.ECC;
import com.yishuifengxiao.common.tool.codec.EccKeyAgreement;
import com.yishuifengxiao.common.tool.codec.RSA;
import com.yishuifengxiao.common.tool.codec.SHA256;
import com.yishuifengxiao.common.tool.codec.TripleDES;
import com.yishuifengxiao.common.tool.collections.ArrayUtil;
import com.yishuifengxiao.common.tool.collections.CollUtil;
import com.yishuifengxiao.common.tool.context.LocalCache;
import com.yishuifengxiao.common.tool.datetime.DateTimeUtil;
import com.yishuifengxiao.common.tool.entity.Page;
import com.yishuifengxiao.common.tool.exception.UncheckedException;
import com.yishuifengxiao.common.tool.io.ImageUtil;
import com.yishuifengxiao.common.tool.io.IoUtil;
import com.yishuifengxiao.common.tool.lang.Hex;
import com.yishuifengxiao.common.tool.lang.OID;
import com.yishuifengxiao.common.tool.lang.TLVFormatter;
import com.yishuifengxiao.common.tool.sensitive.SensitiveUtil;
import com.yishuifengxiao.common.tool.text.RegexUtil;
import com.yishuifengxiao.common.tool.validate.InString;
import com.yishuifengxiao.common.tool.validate.validator.InStringValidator;
import jakarta.validation.Payload;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.lang.annotation.Annotation;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 通用工具类审计问题修复回归测试
 * <p>
 * 覆盖 IO、本地缓存、分页、时区、反射缓存、ASN.1、脱敏与枚举校验等修复点。
 * </p>
 *
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
class CommonUtilsReviewFixTest {

    /**
     * 读取大文件为 Base64 时不应被截断
     *
     * @throws Exception IO异常
     */
    @Test
    void readFileToBase64ReadsWholeFile() throws Exception {
        byte[] content = new byte[9000];
        for (int i = 0; i < content.length; i++) {
            content[i] = (byte) (i % 251);
        }
        Path path = Files.createTempFile("io-util-", ".bin");
        try {
            Files.write(path, content);
            String base64 = IoUtil.readFileToBase64(path.toFile());
            assertArrayEquals(content, Base64.getDecoder().decode(base64));
        } finally {
            Files.deleteIfExists(path);
        }
    }

    /**
     * 输入流转字符串时应保留原始换行符
     *
     * @throws Exception IO异常
     */
    @Test
    void inputStreamToStringKeepsLineBreaks() throws Exception {
        byte[] raw = "第一行\r\n第二行\n第三行".getBytes(StandardCharsets.UTF_8);
        String text = IoUtil.inputStreamToString(new ByteArrayInputStream(raw), StandardCharsets.UTF_8.name());
        assertEquals("第一行\r\n第二行\n第三行", text);
    }

    /**
     * 空白 key 不应触发空指针，且直接返回 supplier 结果
     */
    @Test
    void localCacheBlankKeyUsesSupplier() {
        assertEquals("fallback", LocalCache.get("   ", () -> "fallback"));
        assertNull(LocalCache.get(null, null));
        assertNull(LocalCache.get("   ", null));
    }

    /**
     * 本地缓存应有容量上限，避免无界增长
     */
    @Test
    void localCacheIsBounded() {
        for (int i = 0; i < 1500; i++) {
            LocalCache.put("bounded-" + i, i);
        }
        assertTrue(LocalCache.keys().size() <= 1024, "缓存条目数不应超过容量上限");
        for (int i = 0; i < 1500; i++) {
            LocalCache.remove("bounded-" + i);
        }
    }

    /**
     * 分页参数为 null 或非正数时应归一化，不应抛异常
     */
    @Test
    void pageHandlesNullAndZeroArguments() {
        List<Integer> data = new ArrayList<>();
        for (int i = 0; i < 25; i++) {
            data.add(i);
        }
        Page<Integer> page = Page.toPage(data, null, null);
        assertEquals(10, page.getSize().intValue());
        assertEquals(1, page.getCurrent().intValue());
        assertEquals(10, page.getData().size());

        assertEquals(1L, Page.page(10, 0).longValue());
        assertEquals(1L, Page.page(10, null).longValue());
        assertEquals(3L, Page.page(25, 10).longValue());
    }

    /**
     * Date 与 LocalDateTime 互转必须跟随 JVM 默认时区，不得硬编码固定时区
     */
    @Test
    void dateTimeConversionUsesDefaultZone() {
        TimeZone original = TimeZone.getDefault();
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("America/New_York"));
            LocalDateTime localDateTime = LocalDateTime.of(2024, 1, 1, 12, 30);
            Date date = DateTimeUtil.localDateTime2Date(localDateTime);
            assertEquals(localDateTime, DateTimeUtil.date2LocalDateTime(date));
            assertEquals("2024-01-01 12:30", DateTimeUtil.format(date, "yyyy-MM-dd HH:mm"));
            assertEquals(LocalDateTime.now().withNano(0), DateTimeUtil.now().withNano(0));
        } finally {
            TimeZone.setDefault(original);
        }
    }

    /**
     * 查找不存在的字段应返回 null，而不是抛出空指针
     */
    @Test
    void findFieldReturnsNullForMissingField() {
        assertNull(ClassUtil.findField(SampleBean.class, "notExists"));
        assertNull(ClassUtil.findField(null, "name"));
        assertNotNull(ClassUtil.findField(SampleBean.class, "name"));
    }

    /**
     * ASN.1 转换在入参为空时应抛出统一异常（与 Javadoc 一致）
     */
    @Test
    void asn1ToObjectRejectsNullArguments() {
        assertThrows(UncheckedException.class, () -> Asn1Util.toObject(null, "00"));
        assertThrows(UncheckedException.class, () -> Asn1Util.toObject(SampleBean.class, null));
        assertThrows(UncheckedException.class, () -> Asn1Util.toObject(SampleBean.class, ""));
    }

    /**
     * 非严格合法的历史身份证号也应做脱敏，不能原样返回
     */
    @Test
    void sensitiveIdCardMasksLegacyValue() {
        String legacy = "11010119900307";
        String masked = SensitiveUtil.idCard(legacy);
        assertEquals("110*******0307", masked);
        assertFalse(masked.contains("19900307"));
    }

    /**
     * nullable=true 只放行 null，空白串仍需参与集合匹配
     */
    @Test
    void inStringValidatorOnlyIgnoresNull() {
        InStringValidator validator = new InStringValidator();
        validator.initialize(inString(new String[]{"A"}, true, true));

        assertTrue(validator.isValid(null, null));
        assertFalse(validator.isValid("", null));
        assertFalse(validator.isValid("   ", null));
        assertTrue(validator.isValid("A", null));
    }

    /**
     * 构造用于测试的 InString 注解实例
     *
     * @param values   允许的字符串集合
     * @param nullable 是否允许 null
     * @param sensitive 是否大小写敏感
     * @return 注解实例
     */
    private static InString inString(String[] values, boolean nullable, boolean sensitive) {
        return new InString() {
            @Override
            public String message() {
                return "";
            }

            @Override
            public boolean nullable() {
                return nullable;
            }

            @Override
            public boolean sensitive() {
                return sensitive;
            }

            @Override
            public String[] value() {
                return values;
            }

            @Override
            public Class<?>[] groups() {
                return new Class<?>[0];
            }

            @Override
            public Class<? extends Payload>[] payload() {
                return new Class[0];
            }

            @Override
            public Class<? extends Annotation> annotationType() {
                return InString.class;
            }
        };
    }

    /**
     * ECC签名长度应随曲线坐标长度变化，且签名可被同曲线公钥验证
     *
     * @throws Exception 密钥生成或签名验签异常
     */
    @Test
    void eccSignatureUsesCurveCoordinateLength() throws Exception {
        byte[] data = "ecc-signature".getBytes(StandardCharsets.UTF_8);

        KeyPair p256 = ECC.generateECCKeyPair("secp256r1");
        byte[] signature256 = ECC.sign(p256.getPrivate(), data);
        assertEquals(64, signature256.length);
        assertTrue(ECC.verifySignature(p256.getPublic(), data, signature256));

        KeyPair p384 = ECC.generateECCKeyPair("secp384r1");
        byte[] signature384 = ECC.sign(p384.getPrivate(), data);
        assertEquals(96, signature384.length);
        assertTrue(ECC.verifySignature(p384.getPublic(), data, signature384));
        assertFalse(ECC.verifySignature(p384.getPublic(),
                "other".getBytes(StandardCharsets.UTF_8), signature384));
    }

    /**
     * 3DES 加解密往返应一致
     *
     * @throws Exception 加解密异常
     */
    @Test
    void tripleDesRoundTrip() throws Exception {
        String key = TripleDES.generate3DESKey();
        String text = "3DES 测试内容";
        assertEquals(text, TripleDES.decrypt(key, TripleDES.encrypt(key, text)));
    }

    /**
     * 3DES 解密时密文长度不足应抛出明确异常，而不是数组越界
     *
     * @throws Exception 密钥生成异常
     */
    @Test
    void tripleDesDecryptRejectsInvalidCipherText() throws Exception {
        String key = TripleDES.generate3DESKey();
        assertThrows(IllegalArgumentException.class,
                () -> TripleDES.decrypt(key, Base64.getEncoder().encodeToString(new byte[8])));
        assertThrows(IllegalArgumentException.class, () -> TripleDES.decrypt(key, ""));
    }

    /**
     * ECC密钥协商遇到非法十六进制入参时应抛出明确异常
     */
    @Test
    void eccKeyAgreementRejectsMalformedHex() {
        assertThrows(IllegalArgumentException.class,
                () -> EccKeyAgreement.eccKeyAgreement("1.2.840.10045.3.1.7", "04ABC", "01", "00", 16));
        assertThrows(IllegalArgumentException.class,
                () -> EccKeyAgreement.eccKeyAgreement("1.2.840.10045.3.1.7", "04ZZ", "01", "00", 16));
    }

    /**
     * Hex 对非法十六进制字符串保持返回 null 的既有契约
     */
    @Test
    void hexToBytesRejectsInvalidInput() {
        assertNull(Hex.hexToBytes("ABC"));
        assertNull(Hex.hexToBytes("ZZ"));
        assertArrayEquals(new byte[]{0x01, 0x02}, Hex.hexToBytes("0102"));
    }

    /**
     * 正则Pattern缓存应复用同一个Pattern实例，并对空白正则抛出明确异常
     */
    @Test
    void regexPatternCacheReusesCompiledPattern() {
        assertSame(RegexUtil.pattern("a+b"), RegexUtil.pattern("a+b"));
        assertThrows(IllegalArgumentException.class, () -> RegexUtil.pattern(" "));
    }

    /**
     * DES 密钥超过8个字符时只使用前8个字符，保证加解密一致
     */
    @Test
    void desKeyLongerThanEightCharsUsesFirstEight() {
        String data = "des-key-length";
        assertEquals(DES.encrypt("12345678", data), DES.encrypt("123456789", data));
        assertEquals(data, DES.decrypt("123456789", DES.encrypt("12345678", data)));
    }

    /**
     * 图片文件转Base64应读取完整文件内容，不能因 available() 被截断
     *
     * @throws Exception IO异常
     */
    @Test
    void imageFileToBase64ReadsWholeFile() throws Exception {
        byte[] content = new byte[12000];
        for (int i = 0; i < content.length; i++) {
            content[i] = (byte) (i % 253);
        }
        Path path = Files.createTempFile("image-util-", ".bin");
        try {
            Files.write(path, content);
            assertEquals(Base64.getEncoder().encodeToString(content),
                    ImageUtil.imageFileToBase64(path.toFile()));
        } finally {
            Files.deleteIfExists(path);
        }
    }

    /**
     * RSA 中文明文加解密应使用 UTF-8，往返结果与原文一致
     */
    @Test
    void rsaRoundTripUsesUtf8() {
        KeyPair keyPair = RSA.generateKeyPair(2048);
        String data = "RSA中文加密测试";
        assertEquals(data, RSA.decrypt(RSA.encrypt(data, keyPair.getPublic()), keyPair.getPrivate()));
    }

    /**
     * SHA-256 字符串摘要应使用 UTF-8，非法十六进制应抛明确异常
     */
    @Test
    void sha256UsesUtf8AndRejectsInvalidHex() {
        assertEquals("72726D8818F693066CEB69AFA364218B692E62EA92B385782363780F47529C21",
                SHA256.calculateSHA256("中文"));
        assertThrows(IllegalArgumentException.class, () -> SHA256.calculateSHA256FromHex("ABC"));
    }

    /**
     * TLVFormatter 对 null 入参应抛 IllegalArgumentException 而非 NPE
     */
    @Test
    void tlvFormatterRejectsNullInput() {
        assertThrows(IllegalArgumentException.class, () -> TLVFormatter.formatTLVStr(false, null));
    }

    /**
     * OID 首段 base128 编码不完整时应抛异常，合法输入仍能正确解析
     */
    @Test
    void oidRejectsIncompleteBase128() {
        assertThrows(IllegalArgumentException.class, () -> OID.hexToDotNotation("81"));
        assertEquals("1.2", OID.hexToDotNotation("2A"));
    }

    /**
     * CollUtil.get 读取到 null 元素时应返回空 Optional，而不是抛 NPE
     */
    @Test
    void collUtilGetReturnsEmptyForNullElement() {
        List<String> list = new ArrayList<>();
        list.add(null);
        assertTrue(CollUtil.get(list, 0).isEmpty());
    }

    /**
     * ArrayUtil.union 应按 predicate 判定重复，而不是按 equals
     */
    @Test
    void arrayUtilUnionHonorsPredicate() {
        List<String> src = new ArrayList<>();
        src.add("x1a");
        src.add("y2a");
        List<String> target = new ArrayList<>();
        target.add("x1b");
        target.add("z3a");

        Collection<String> union = ArrayUtil.union(src, target,
                (a, b) -> a.substring(0, 2).equals(b.substring(0, 2)));

        assertEquals(3, union.size());
        assertTrue(union.contains("x1a"));
        assertFalse(union.contains("x1b"));
        assertTrue(union.contains("y2a"));
        assertTrue(union.contains("z3a"));
    }

    /**
     * 测试用简单对象
     */
    private static class SampleBean {
        private String name;
    }
}
