package com.yishuifengxiao.common.tool.lang;
import org.junit.Test;
import static org.junit.Assert.*;

public class TextUtil_toUpperCaseFirstOne_Test {

    /**
     * 测试正常场景：字符串首字母为小写
     * 预期结果：将首字母转换为大写，其余字母保持不变
     */
    @Test
    public void testToUpperCaseFirstOne_LowercaseFirstLetter() {
        String input = "hello";
        String result = TextUtil.toUpperCaseFirstOne(input);
        assertEquals("Hello", result);
    }

    /**
     * 测试正常场景：字符串首字母已为大写
     * 预期结果：保持原字符串不变
     */
    @Test
    public void testToUpperCaseFirstOne_AlreadyUppercase() {
        String input = "Hello";
        String result = TextUtil.toUpperCaseFirstOne(input);
        assertEquals("Hello", result);
    }

    /**
     * 测试边界场景：空字符串
     * 预期结果：返回空字符串
     */
    @Test
    public void testToUpperCaseFirstOne_EmptyString() {
        String input = "";
        String result = TextUtil.toUpperCaseFirstOne(input);
        assertEquals("", result);
    }

    /**
     * 测试边界场景：单字符字符串且为小写
     * 预期结果：将单个字符转换为大写
     */
    @Test
    public void testToUpperCaseFirstOne_SingleLowercaseCharacter() {
        String input = "h";
        String result = TextUtil.toUpperCaseFirstOne(input);
        assertEquals("H", result);
    }

    /**
     * 测试边界场景：单字符字符串且为大写
     * 预期结果：保持原字符不变
     */
    @Test
    public void testToUpperCaseFirstOne_SingleUppercaseCharacter() {
        String input = "H";
        String result = TextUtil.toUpperCaseFirstOne(input);
        assertEquals("H", result);
    }

    /**
     * 测试异常场景：输入为null
     * 预期结果：返回null
     */
    @Test
    public void testToUpperCaseFirstOne_NullInput() {
        String input = null;
        String result = TextUtil.toUpperCaseFirstOne(input);
        assertNull(result);
    }

    /**
     * 测试边界场景：字符串包含非字母字符作为首字符
     * 预期结果：保持原字符串不变
     */
    @Test
    public void testToUpperCaseFirstOne_NonLetterFirstCharacter() {
        String input = "1hello";
        String result = TextUtil.toUpperCaseFirstOne(input);
        assertEquals("1hello", result);
    }
}
