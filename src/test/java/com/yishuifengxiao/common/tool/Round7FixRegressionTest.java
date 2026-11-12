package com.yishuifengxiao.common.tool;

import com.yishuifengxiao.common.tool.asn1.Asn1Util;
import com.yishuifengxiao.common.tool.bean.JsonUtil;
import com.yishuifengxiao.common.tool.codec.RSA;
import com.yishuifengxiao.common.tool.exception.CustomException;
import com.yishuifengxiao.common.tool.exception.UncheckedException;
import com.yishuifengxiao.common.tool.lang.Hex;
import com.yishuifengxiao.common.tool.text.HtmlExtract;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 第七轮修复回归测试，覆盖 Hex 校验、HtmlExtract XXE、Luhn 校验、RSA 显式填充、异常类型统一、JsonUtil 契约等场景。
 */
class Round7FixRegressionTest {

    // ==================== Hex ====================

    @Test
    void hexToUtf8ShouldRejectOddLength() {
        assertThrows(IllegalArgumentException.class, () -> Hex.hexToUtf8("abc"));
    }

    @Test
    void hexToUtf8ShouldRejectInvalidChar() {
        assertThrows(IllegalArgumentException.class, () -> Hex.hexToUtf8("GG"));
    }

    @Test
    void hexToUtf8ShouldStripWhitespaceAndDecode() {
        // "E4 B8 AD" = UTF-8 for 中
        assertEquals("中", Hex.hexToUtf8("E4 B8 AD"));
    }

    @Test
    void hexToUtf8NullReturnsNull() {
        assertNull(Hex.hexToUtf8(null));
    }

    @Test
    void hexToUtf8EmptyReturnsEmpty() {
        assertEquals("", Hex.hexToUtf8(""));
    }

    @Test
    void hexToAsciiShouldCoverExtendedRange() {
        // 0xE9 (233) is beyond US_ASCII (0-127); ISO-8859-1 preserves it as é
        String result = Hex.hexToAscii("E9");
        assertEquals("\u00E9", result);
    }

    @Test
    void hexToAsciiRoundTripExtended() {
        String original = "\u00E9\u00FF"; // é ÿ
        String hex = Hex.asciiToHex(original);
        assertEquals(original, Hex.hexToAscii(hex));
    }

    // ==================== HtmlExtract XXE ====================

    @Test
    void htmlExtractShouldRejectDoctype() {
        // 包含 DOCTYPE 的 XHTML 应被 disallow-doctype-decl 拒绝，element 返回 null
        String xhtml = "<!DOCTYPE foo [<!ENTITY xxe SYSTEM \"file:///etc/passwd\">]>\n<root>&xxe;</root>";
        assertNull(HtmlExtract.element(xhtml));
    }

    @Test
    void htmlExtractShouldParseNormalXml() {
        String xml = "<root><child>hello</child></root>";
        assertNotNull(HtmlExtract.element(xml));
    }

    // ==================== Asn1Util.generateLuhn ====================

    @Test
    void generateLuhnShouldRejectNonDigit() {
        assertThrows(UncheckedException.class, () -> Asn1Util.generateLuhn("12a45"));
    }

    @Test
    void generateLuhnShouldRejectNull() {
        assertThrows(UncheckedException.class, () -> Asn1Util.generateLuhn(null));
    }

    @Test
    void generateLuhnShouldRejectEmpty() {
        assertThrows(UncheckedException.class, () -> Asn1Util.generateLuhn(""));
    }

    @Test
    void generateLuhnKnownCheckDigit() {
        // 经典 Luhn 测试："7992739871" 的校验位为 3
        assertEquals("3", Asn1Util.generateLuhn("7992739871"));
    }

    // ==================== RSA 显式填充 ====================

    @Test
    void rsaEncryptDecryptRoundTrip() {
        KeyPair keyPair = RSA.generateKeyPair(2048);
        String plain = "hello-rsa-round7";
        String encrypted = RSA.encrypt(plain, keyPair.getPublic());
        String decrypted = RSA.decrypt(encrypted, keyPair.getPrivate());
        assertEquals(plain, decrypted);
    }

    // ==================== 异常 code 类型统一 ====================

    @Test
    void uncheckedExceptionSetCodeAcceptsObject() {
        UncheckedException ex = new UncheckedException("msg");
        ex.setCode("CODE-001");
        assertEquals("CODE-001", ex.getCode());
    }

    @Test
    void uncheckedExceptionOfAcceptsObjectCode() {
        // 用 Long 避免与 of(String, Object) 重载歧义
        UncheckedException ex = UncheckedException.of(42L, "boom");
        assertEquals(42L, ex.getCode());
        assertEquals("boom", ex.getMessage());
    }

    @Test
    void uncheckedExceptionOfAcceptsIntegerCode() {
        // 历史调用方仍可传 Integer
        UncheckedException ex = UncheckedException.of(500, "server error");
        assertEquals(500, ex.getCode());
    }

    @Test
    void customExceptionConstructorAcceptsObjectCode() {
        CustomException ex = new CustomException("C-1", "custom");
        assertEquals("C-1", ex.getCode());
        assertEquals("custom", ex.getMessage());
    }

    @Test
    void customExceptionStringConstructorDefaultsCodeToZero() {
        CustomException ex = new CustomException("no-code");
        assertEquals(0, ex.getCode());
    }

    // ==================== JsonUtil 契约 ====================

    @Data
    public static class SimpleBean {
        private String name;
        private int age;
    }

    @Test
    void strToBeanTwoArgShouldNotFailOnUnknownProperties() {
        // 共享 mapper 配置 FAIL_ON_UNKNOWN_PROPERTIES=false，2 参数版本应同样宽松
        String json = "{\"name\":\"alice\",\"age\":30,\"extra\":\"ignored\"}";
        SimpleBean bean = JsonUtil.strToBean(json, SimpleBean.class);
        assertNotNull(bean);
        assertEquals("alice", bean.getName());
        assertEquals(30, bean.getAge());
    }

    @Test
    void strToBeanThreeArgFailOnUnknown() {
        String json = "{\"name\":\"alice\",\"age\":30,\"extra\":\"x\"}";
        // failOnUnknownProperties=true 时应返回 null（解析失败）
        SimpleBean bean = JsonUtil.strToBean(json, SimpleBean.class, true);
        assertNull(bean);
    }

    @Test
    void strToListShouldReturnEmptyForNullJson() {
        List<SimpleBean> list = JsonUtil.strToList(null, SimpleBean.class);
        assertNotNull(list);
        assertTrue(list.isEmpty());
    }

    @Test
    void strToBeanNullReturnsNull() {
        assertNull(JsonUtil.strToBean(null, SimpleBean.class));
    }

    @Test
    void strToBeanBlankReturnsNull() {
        assertNull(JsonUtil.strToBean("   ", SimpleBean.class));
    }
}
