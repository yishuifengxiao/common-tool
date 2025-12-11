package com.yishuifengxiao.common.tool.text;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * TextUtil工具类的单元测试
 */
public class TextUtil_removeComments_Test {

    /**
     * 测试正常情况下的注释移除功能
     */
    @Test
    public void testRemoveComments_NormalCase() {
        String input = "SELECT * FROM users; -- 查询所有用户\n" +
                      "WHERE id = 1; -- 根据ID查询";
        String expected = "SELECT * FROM users; \n" +
                         "WHERE id = 1; ";
        String actual = TextUtil.removeComments(input);
        assertEquals(expected, actual);
    }

    /**
     * 测试整行注释的情况
     */
    @Test
    public void testRemoveComments_FullLineComment() {
        String input = "-- 这是一整行注释\n" +
                      "SELECT * FROM users;\n" +
                      "  -- 这也是一整行注释（前面有空格）";
        String expected = "\n" +
                         "SELECT * FROM users;\n";
        String actual = TextUtil.removeComments(input);
        assertEquals(expected, actual);
    }

    /**
     * 测试空字符串和null值
     */
    @Test
    public void testRemoveComments_EmptyAndNull() {
        // 测试null值
        assertNull(TextUtil.removeComments(null));
        
        // 测试空字符串
        assertEquals("", TextUtil.removeComments(""));
    }

    /**
     * 测试没有注释的文本
     */
    @Test
    public void testRemoveComments_NoComments() {
        String input = "SELECT * FROM users;\n" +
                      "WHERE id = 1;";
        String expected = "SELECT * FROM users;\n" +
                         "WHERE id = 1;";
        String actual = TextUtil.removeComments(input);
        assertEquals(expected, actual);
    }

    /**
     * 测试只有注释的文本
     */
    @Test
    public void testRemoveComments_OnlyComments() {
        String input = "-- 只有注释\n" +
                      "  -- 只有注释（带空格）\n" +
                      "-- another comment";
        String expected = "\n" +
                         "\n";
        String actual = TextUtil.removeComments(input);
        assertEquals(expected, actual);
    }

    /**
     * 测试多种换行符
     */
    @Test
    public void testRemoveComments_DifferentLineBreaks() {
        String input = "SELECT * FROM users; -- comment\r\n" +
                      "WHERE id = 1; -- comment\n" +
                      "-- full line comment\r" +
                      "END; -- end comment";
        String expected = "SELECT * FROM users; \r\n" +
                         "WHERE id = 1; \n" +
                         "\r" +
                         "END; ";
        String actual = TextUtil.removeComments(input);
        assertEquals(expected, actual);
    }

    /**
     * 测试最后一行没有换行符的情况
     */
    @Test
    public void testRemoveComments_LastLineWithoutBreak() {
        String input = "SELECT * FROM users; -- comment\n" +
                      "WHERE id = 1; -- comment\n" +
                      "END; -- final comment";
        String expected = "SELECT * FROM users; \n" +
                         "WHERE id = 1; \n" +
                         "END; ";
        String actual = TextUtil.removeComments(input);
        assertEquals(expected, actual);
    }

    /**
     * 测试多个连续的注释符号
     */
    @Test
    public void testRemoveComments_MultipleDashes() {
        String input = "SELECT '--test' AS value; -- 这是注释\n" +
                      "---- 这也是注释";
        String expected = "SELECT '--test' AS value; \n";
        String actual = TextUtil.removeComments(input);
        assertEquals(expected, actual);
    }
}