package com.yishuifengxiao.common.tool.lang;
import org.junit.Test;
import static org.junit.Assert.*;

public class TextUtil_underscoreName_Test {

    /**
     * 测试正常场景：单个大写字母的驼峰命名转换
     * 验证方法能够正确将单个大写字母转换为小写
     */
    @Test
    public void testUnderscoreName_SingleUpperCase() {
        String input = "A";
        String result = TextUtil.underscoreName(input);
        assertEquals("a", result);
    }

    /**
     * 测试正常场景：单个小写字母的驼峰命名转换
     * 验证方法能够正确处理单个小写字母（保持不变）
     */
    @Test
    public void testUnderscoreName_SingleLowerCase() {
        String input = "a";
        String result = TextUtil.underscoreName(input);
        assertEquals("a", result);
    }

    /**
     * 测试正常场景：简单驼峰命名转换
     * 验证方法能够正确将驼峰命名转换为下划线命名
     */
    @Test
    public void testUnderscoreName_SimpleCamelCase() {
        String input = "CallBack";
        String result = TextUtil.underscoreName(input);
        assertEquals("call_back", result);
    }

    /**
     * 测试正常场景：连续大写字母的驼峰命名转换
     * 验证方法能够正确处理连续大写字母的情况
     */
    @Test
    public void testUnderscoreName_ConsecutiveUpperCase() {
        String input = "XMLHttpRequest";
        String result = TextUtil.underscoreName(input);
        assertEquals("x_m_l_http_request", result);
    }

    /**
     * 测试边界场景：空字符串输入
     * 验证方法能够正确处理空字符串输入（返回空字符串）
     */
    @Test
    public void testUnderscoreName_EmptyString() {
        String input = "";
        String result = TextUtil.underscoreName(input);
        assertEquals("", result);
    }

    /**
     * 测试边界场景：null输入
     * 验证方法能够正确处理null输入（返回null）
     */
    @Test
    public void testUnderscoreName_NullInput() {
        String input = null;
        String result = TextUtil.underscoreName(input);
        assertNull(result);
    }

    /**
     * 测试边界场景：全小写字符串输入
     * 验证方法能够正确处理全小写字符串（保持不变）
     */
    @Test
    public void testUnderscoreName_AllLowerCase() {
        String input = "lowercase";
        String result = TextUtil.underscoreName(input);
        assertEquals("lowercase", result);
    }

    /**
     * 测试边界场景：全大写字符串输入
     * 验证方法能够正确处理全大写字符串（转换为全小写并添加下划线）
     */
    @Test
    public void testUnderscoreName_AllUpperCase() {
        String input = "UPPERCASE";
        String result = TextUtil.underscoreName(input);
        assertEquals("u_p_p_e_r_c_a_s_e", result);
    }
}
