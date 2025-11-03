package com.yishuifengxiao.common.tool.lang;
import org.junit.Test;
import static org.junit.Assert.*;

public class TextUtil_toLowerCaseFirstOne_Test {

    /**
     * 测试正常场景：字符串首字母为大写
     * 预期结果：返回首字母变为小写的字符串
     */
    @Test
    public void testToLowerCaseFirstOne_UpperCaseFirstLetter() {
        String input = "Hello";
        String result = TextUtil.toLowerCaseFirstOne(input);
        assertEquals("hello", result);
    }

    /**
     * 测试正常场景：字符串首字母已经为小写
     * 预期结果：返回原字符串不变
     */
    @Test
    public void testToLowerCaseFirstOne_LowerCaseFirstLetter() {
        String input = "hello";
        String result = TextUtil.toLowerCaseFirstOne(input);
        assertEquals("hello", result);
    }

    /**
     * 测试边界场景：空字符串
     * 预期结果：返回空字符串
     */
    @Test
    public void testToLowerCaseFirstOne_EmptyString() {
        String input = "";
        String result = TextUtil.toLowerCaseFirstOne(input);
        assertEquals("", result);
    }

    /**
     * 测试边界场景：null输入
     * 预期结果：返回null
     */
    @Test
    public void testToLowerCaseFirstOne_NullInput() {
        String input = null;
        String result = TextUtil.toLowerCaseFirstOne(input);
        assertNull(result);
    }

    /**
     * 测试边界场景：单字符字符串且为大写
     * 预期结果：返回变为小写的单字符字符串
     */
    @Test
    public void testToLowerCaseFirstOne_SingleUpperCaseChar() {
        String input = "H";
        String result = TextUtil.toLowerCaseFirstOne(input);
        assertEquals("h", result);
    }

    /**
     * 测试边界场景：单字符字符串且为小写
     * 预期结果：返回原字符串不变
     */
    @Test
    public void testToLowerCaseFirstOne_SingleLowerCaseChar() {
        String input = "h";
        String result = TextUtil.toLowerCaseFirstOne(input);
        assertEquals("h", result);
    }
}
