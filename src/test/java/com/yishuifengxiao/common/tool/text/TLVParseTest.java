package com.yishuifengxiao.common.tool.text;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TLV#parse(String tag) 的单元测试类
 */
public class TLVParseTest {

    /**
     * TC01: 正常解析带tag的数据
     */
    @Test
    public void testParse_NormalCaseWithTag() {
        String hexData = "5F370101";
        TLV tlv = TLV.of(hexData).parse("5F37");

        assertTrue(tlv.isSuccess(), "应解析成功");
        assertEquals("", tlv.getError(), "不应出现错误");
        assertEquals("5F37", tlv.getTag(), "tag 应为 '5F37'");
        assertEquals("01", tlv.getValue(), "value 应为 '01'");
        assertEquals(1, tlv.getValueLength(), "valueLength 应为 1");
        assertEquals("", tlv.getRemainingData(), "剩余数据应为 ''");
    }

    /**
     * TC02: tag为空时也能解析
     */
    @Test
    public void testParse_TagIsNull() {
        String hexData = "5F370101";
        TLV tlv = TLV.of(hexData).parse(null);

        assertFalse(tlv.isSuccess(), "数据长度不足");
        assertEquals("解析错误: 数据长度不足", tlv.getError(), "数据长度不足");
        assertEquals("", tlv.getTag(), "tag 应为空");
        assertEquals("", tlv.getValue(), "value 应为 ''");
        assertEquals(0, tlv.getValueLength(), "valueLength 应为 0");
        assertEquals("", tlv.getRemainingData(), "剩余数据应为 ''");
    }

    /**
     * TC03: tag不匹配导致失败
     */
    @Test
    public void testParse_TagMismatch() {
        String hexData = "5F370101";
        TLV tlv = TLV.of(hexData).parse("6F37");

        assertFalse(tlv.isSuccess(), "应解析失败");
        assertEquals("数据不以指定标签开头", tlv.getError(), "错误信息应为 '数据不以指定标签开头'");
    }

    /**
     * TC04: 输入非合法Hex数据
     */
    @Test
    public void testParse_InvalidHexData() {
        String invalidHex = "XYZZ";
        TLV tlv = TLV.of(invalidHex).parse("");

        assertFalse(tlv.isSuccess(), "应解析失败");
        assertEquals("无效的Hex数据", tlv.getError(), "错误信息应为 '无效的Hex数据'");
    }

    /**
     * TC05: 输入空数据
     */
    @Test
    public void testParse_EmptyData() {
        String emptyData = "";
        TLV tlv = TLV.of(emptyData).parse("5F37");

        assertFalse(tlv.isSuccess(), "应解析失败");
        assertEquals("数据为空", tlv.getError(), "错误信息应为 '数据为空'");
    }

    /**
     * TC06: 解析过程抛出异常（如长度字段不足）
     */
    @Test
    public void testParse_ParseException() {
        String shortData = "5F37"; // 缺少长度和值字段
        TLV tlv = TLV.of(shortData).parse("5F37");

        assertFalse(tlv.isSuccess(), "应解析失败");
        assertTrue(tlv.getError().startsWith("解析错误:"), "应报告解析错误");
    }

    /**
     * TC07: 第二次解析剩余数据
     */
    @Test
    public void testParse_SecondParseWithRemainingData() {
        String multiTlvData = "5F3701016F380102";
        TLV firstTlv = TLV.of(multiTlvData).parse("5F37");

        assertTrue(firstTlv.isSuccess(), "第一次应解析成功");
        assertEquals("01", firstTlv.getValue(), "第一次value应为'01'");
        assertEquals("6F380102", firstTlv.getRemainingData(), "剩余数据应为第二段");

        // 再次解析剩余数据
        TLV secondTlv = firstTlv.parse("6F38");
        assertTrue(secondTlv.isSuccess(), "第二次也应解析成功");
        assertEquals("02", secondTlv.getValue(), "第二次value应为'02'");
        assertEquals("", secondTlv.getRemainingData(), "最后无剩余数据");
    }
}
