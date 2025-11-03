/**
 *
 */
package com.yishuifengxiao.common.tool.lang;

/**
 * <p>
 * 文本工具
 * </p>
 * 该工具主要用于将字符串的首字母进行大小写转换
 *
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
public class TextUtil {

    /**
     * 将对象转换为字符串
     *
     * @param val 待转换的字符串
     * @return 转换后的字符串
     */
    public static String toString(Object val) {
        return null == val ? null : val.toString();
    }

    /**
     * 将字符串的首字母变为小写的
     *
     * @param s 字符串
     * @return 转换之后的字符串
     */
    public static String toLowerCaseFirstOne(String s) {
        if (s == null || s.isEmpty()) {
            return s;
        }
        if (Character.isLowerCase(s.charAt(0))) {
            return s;
        }
        return new StringBuilder()
                .append(Character.toLowerCase(s.charAt(0)))
                .append(s.substring(1))
                .toString();
    }

    /**
     * 将字符串的首字母变为大写的
     *
     * @param s 字符串
     * @return 转换之后的字符串
     */
    public static String toUpperCaseFirstOne(String s) {
        if (s == null || s.isEmpty()) {
            return s;
        }
        if (Character.isUpperCase(s.charAt(0))) {
            return s;
        }
        return new StringBuilder()
                .append(Character.toUpperCase(s.charAt(0)))
                .append(s.substring(1))
                .toString();
    }


    /**
     * 转换为下划线,例如:将CallBack转换成 call_back
     *
     * @param camelCaseName 输入的数据
     * @return 转换后的数据
     */
    public static String underscoreName(String camelCaseName) {
        if (camelCaseName == null || camelCaseName.isEmpty()) {
            return camelCaseName;
        }

        StringBuilder result = new StringBuilder();
        result.append(Character.toLowerCase(camelCaseName.charAt(0)));

        for (int i = 1; i < camelCaseName.length(); i++) {
            char ch = camelCaseName.charAt(i);
            if (Character.isUpperCase(ch)) {
                result.append('_');
                result.append(Character.toLowerCase(ch));
            } else {
                result.append(ch);
            }
        }

        return result.toString();
    }

    /**
     * 转换为驼峰,例如:将call_back转换成 CallBack
     *
     * @param underscoreName 输入的数据
     * @return 转换后的数据
     */
    public static String camelCaseName(String underscoreName) {
        if (underscoreName == null || underscoreName.isEmpty()) {
            return underscoreName;
        }

        StringBuilder result = new StringBuilder();
        boolean nextUpper = false;
        boolean firstChar = true;

        for (int i = 0; i < underscoreName.length(); i++) {
            char ch = underscoreName.charAt(i);
            if (ch == '_') {
                nextUpper = true;
            } else {
                if (nextUpper || firstChar) {
                    result.append(Character.toUpperCase(ch));
                    nextUpper = false;
                    firstChar = false;
                } else {
                    result.append(ch);
                }
            }
        }

        return result.toString();
    }
}
