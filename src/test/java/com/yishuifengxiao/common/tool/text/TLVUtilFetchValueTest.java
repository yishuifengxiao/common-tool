package com.yishuifengxiao.common.tool.text;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * TLVUtil.fetchValueFromTlv 方法的单元测试类
 */
public class TLVUtilFetchValueTest {

    /**
     * TC01: 正常路径，目标 Tag 存在于顶层 TLV 中
     */
    @Test
    public void testFetchValueFromTlv_TopLevelTag() {
        String tag = "9F01";
        String tlv = "9F0103AABBCC";
        String expected = "AABBCC";
        assertEquals(expected, TLVUtil.fetchValueFromTlv(tag, tlv));
    }

    /**
     * TC02: 正常路径，目标 Tag 存在于嵌套 TLV 中
     */
    @Test
    public void testFetchValueFromTlv_NestedTag() {
        String tag = "9F02";
        String tlv = "9F010BAABBCC9F0203DDEEFF";
        String expected = "DDEEFF";
        String actual = TLVUtil.fetchValueFromTlv(tag, tlv);
        assertEquals(expected, actual);
    }

    /**
     * TC03: 目标 Tag 不存在
     */
    @Test
    public void testFetchValueFromTlv_TagNotFound() {
        String tag = "9F03";
        String tlv = "9F0103AABBCC";
        String expected = "";
        assertEquals(expected, TLVUtil.fetchValueFromTlv(tag, tlv));
    }

    /**
     * TC04: TLV 字符串格式错误（Tag 解析失败）
     */
    @Test
    public void testFetchValueFromTlv_InvalidTagFormat() {
        String tag = "9F01";
        String tlv = "GG0103AABBCC"; // 非法十六进制字符
        String expected = "";
        assertEquals(expected, TLVUtil.fetchValueFromTlv(tag, tlv));
    }

    /**
     * TC05: TLV 字符串格式错误（Length 解析失败）
     */
    @Test
    public void testFetchValueFromTlv_InvalidLengthFormat() {
        String tag = "9F01";
        String tlv = "9F01GG03AABBCC"; // 非法长度字段
        String expected = "";
        assertEquals(expected, TLVUtil.fetchValueFromTlv(tag, tlv));
    }

    /**
     * TC06: TLV 字符串不完整（Value 不足）
     */
    @Test
    public void testFetchValueFromTlv_IncompleteValue() {
        String tag = "9F01";
        String tlv = "9F0103AABB"; // Value 长度不足
        String expected = "";
        assertEquals(expected, TLVUtil.fetchValueFromTlv(tag, tlv));
    }

    /**
     * TC07: 空输入
     */
    @Test
    public void testFetchValueFromTlv_EmptyInput() {
        String tag = "9F01";
        String tlv = "";
        String expected = "";
        assertEquals(expected, TLVUtil.fetchValueFromTlv(tag, tlv));
    }

    /**
     * TC08: 多层嵌套 TLV 查找
     */
    @Test
    public void testFetchValueFromTlv_MultiNestedTag() {
        String tag = "9F04";
        String tlv = "9F010B9F02059F0403AABBCC"; // 三层嵌套
        String expected = "AABBCC";
        assertEquals(expected, TLVUtil.fetchValueFromTlv(tag, tlv));
    }
}
