/**
 *
 */
package com.yishuifengxiao.common.tool.text;

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
     * 高性能版本的SQL注释移除方法（使用StringBuilder）
     * 适合处理大文本，性能最优
     * 该方法会移除以"--"开头的行注释以及行内的"--"注释
     *
     * @param text 输入文本
     * @return 处理后的文本，已移除SQL风格的注释
     */
    public static String removeComments(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        StringBuilder result = new StringBuilder(text.length());
        StringBuilder currentLine = new StringBuilder();

        // 逐字符遍历输入文本，按行处理注释
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            if (c == '\n' || c == '\r') {
                // 处理换行
                processLine(currentLine, result);
                result.append(c);
                currentLine.setLength(0);
            } else {
                currentLine.append(c);
            }
        }

        // 处理最后一行（如果没有换行符结尾）
        if (currentLine.length() > 0) {
            processLine(currentLine, result);
        }

        return result.toString();
    }


    /**
     * 处理单行文本注释
     * 该方法用于移除SQL风格的注释（以"--"开头的注释）
     *
     * @param line   需要处理的行文本内容
     * @param result 存储处理后结果的StringBuilder对象
     */
    private static void processLine(StringBuilder line, StringBuilder result) {
        String lineStr = line.toString();

        // 检查是否以--开头（允许前面有空格）
        int firstNonSpace = -1;
        for (int i = 0; i < lineStr.length(); i++) {
            if (!Character.isWhitespace(lineStr.charAt(i))) {
                firstNonSpace = i;
                break;
            }
        }

        if (firstNonSpace >= 0 &&
                firstNonSpace + 1 < lineStr.length() &&
                lineStr.charAt(firstNonSpace) == '-' &&
                lineStr.charAt(firstNonSpace + 1) == '-') {
            // 整行注释，跳过
            return;
        }

        // 处理行内注释，需要找到不在引号内的注释符号
        int commentIndex = findCommentPosition(lineStr);
        if (commentIndex >= 0) {
            result.append(lineStr.substring(0, commentIndex));
        } else {
            result.append(lineStr);
        }
    }

    /**
     * 查找行中真正的注释位置，避免在字符串引号内误判
     *
     * @param lineStr 行字符串
     * @return 注释符号"--"的位置，如果未找到则返回-1
     */
    private static int findCommentPosition(String lineStr) {
        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;
        
        for (int i = 0; i < lineStr.length() - 1; i++) {
            char c = lineStr.charAt(i);
            char next = lineStr.charAt(i + 1);
            
            // 处理转义字符
            if (c == '\\' && i + 1 < lineStr.length()) {
                i++; // 跳过下一个字符
                continue;
            }
            
            // 切换引号状态
            if (c == '\'' && !inDoubleQuote) {
                inSingleQuote = !inSingleQuote;
            } else if (c == '"' && !inSingleQuote) {
                inDoubleQuote = !inDoubleQuote;
            }
            
            // 如果当前不在引号内，且遇到注释符号，则返回位置
            if (!inSingleQuote && !inDoubleQuote && c == '-' && next == '-') {
                return i;
            }
        }
        
        return -1;
    }


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
