package com.yishuifengxiao.common.tool.lang;
import com.yishuifengxiao.common.tool.text.TextUtil;
import org.junit.Test;
import static org.junit.Assert.*;

public class TextUtil_camelCaseName_Test {

    /**
     * 测试正常场景：标准下划线命名转换为驼峰命名
     * 输入：call_back
     * 预期输出：CallBack
     */
    @Test
    public void testCamelCaseName_StandardUnderscore() {
        String input = "call_back";
        String expected = "CallBack";
        String result = TextUtil.camelCaseName(input);
        assertEquals(expected, result);
    }

    /**
     * 测试边界场景：连续多个下划线的情况
     * 输入：call___back
     * 预期输出：CallBack
     */
    @Test
    public void testCamelCaseName_MultipleUnderscores() {
        String input = "call___back";
        String expected = "CallBack";
        String result = TextUtil.camelCaseName(input);
        assertEquals(expected, result);
    }

    /**
     * 测试边界场景：以下划线开头的情况
     * 输入：_call_back
     * 预期输出：CallBack
     */
    @Test
    public void testCamelCaseName_LeadingUnderscore() {
        String input = "_call_back";
        String expected = "CallBack";
        String result = TextUtil.camelCaseName(input);
        assertEquals(expected, result);
    }

    /**
     * 测试边界场景：以下划线结尾的情况
     * 输入：call_back_
     * 预期输出：CallBack
     */
    @Test
    public void testCamelCaseName_TrailingUnderscore() {
        String input = "call_back_";
        String expected = "CallBack";
        String result = TextUtil.camelCaseName(input);
        assertEquals(expected, result);
    }

    /**
     * 测试边界场景：空字符串输入
     * 输入：""
     * 预期输出：""
     */
    @Test
    public void testCamelCaseName_EmptyString() {
        String input = "";
        String expected = "";
        String result = TextUtil.camelCaseName(input);
        assertEquals(expected, result);
    }

    /**
     * 测试异常场景：null输入
     * 输入：null
     * 预期输出：null
     */
    @Test
    public void testCamelCaseName_NullInput() {
        String input = null;
        String expected = null;
        String result = TextUtil.camelCaseName(input);
        assertEquals(expected, result);
    }

    /**
     * 测试正常场景：单个单词的情况
     * 输入：call
     * 预期输出：Call
     */
    @Test
    public void testCamelCaseName_SingleWord() {
        String input = "call";
        String expected = "Call";
        String result = TextUtil.camelCaseName(input);
        assertEquals(expected, result);
    }

    /**
     * 测试正常场景：已经是大驼峰的情况
     * 输入：CallBack
     * 预期输出：CallBack
     */
    @Test
    public void testCamelCaseName_AlreadyCamelCase() {
        String input = "CallBack";
        String expected = "CallBack";
        String result = TextUtil.camelCaseName(input);
        assertEquals(expected, result);
    }
}
