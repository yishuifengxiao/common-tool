/**
 *
 */
package com.yishuifengxiao.common.tool.text;

import org.apache.commons.lang3.StringUtils;

/**
 * <p>文本工具类</p>
 * <p>提供字符串处理、格式转换等文本操作功能。</p>
 * <p>特性：</p>
 * <ul>
 * <li>字符串首字母大小写转换</li>
 * <li>驼峰命名与下划线命名互转</li>
 * <li>SQL注释移除</li>
 * <li>对象转字符串</li>
 * </ul>
 *
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
public class TextUtil {

    /**
     * 移除字符串中的所有空白字符和不可见字符
     * <p>
     * 该方法使用Unicode正则表达式匹配并删除以下字符：
     * <ul>
     * <li>\p{Z} - 所有Unicode空白字符（包括空格、不间断空格、制表符、换行符、回车符等）</li>
     * <li>\p{C} - 所有不可见控制字符（如\u0000-\u001F、\u007F、\u0080-\u009F、零宽字符等）</li>
     * </ul>
     * </p>
     *
     * @param str 待处理的字符串
     * @return 移除空白和不可见字符后的字符串；如果输入为null则返回null
     */
    public static String removeWhitespaceAndInvisible(String str) {
        if (str == null) return null;
        return str.replaceAll("[\\p{C}\\p{Z}]", "").trim();
    }

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

    /**
     * 在字符串前端填充指定字符至规定长度，如果已有字符串超过这个长度则返回原字符串
     *
     * <p>处理逻辑：
     * <ul>
     *   <li>将参数委托给padString方法，设置前置填充标志为true</li>
     *   <li>从左侧（前端）开始填充指定字符，直到达到目标长度</li>
     * </ul>
     *
     * <p>典型应用场景：
     * <ul>
     *   <li>数字字符串补零：将"123"补零为"00123"</li>
     *   <li>十六进制数据对齐：确保数据块具有统一的起始位置</li>
     *   <li>固定宽度字段格式化：如数据库字段、报表列对齐等</li>
     * </ul>
     *
     * <p>示例：
     * <ul>
     *   <li>输入 "123", '0', 5 → 输出 "00123"(左侧补零)</li>
     *   <li>输入 "ABC", ' ', 6 → 输出 "   ABC"(左侧补空格)</li>
     *   <li>输入 "ABCDEFGH", '0', 5 → 输出 "ABCDEFGH"(超长不处理)</li>
     *   <li>输入 "", '*', 3 → 输出 "***"(空字符串全部填充)</li>
     * </ul>
     *
     * @param originalString 被填充的原始字符串，不能为null
     * @param paddingChar    用于填充的字符，可以是任意有效字符（如'0'、' '、'*'等）
     * @param targetLength   填充后的目标总长度，必须为非负整数
     * @return 前端填充后的字符串，若原字符串长度已超过目标长度则返回原字符串
     * @since 3.1.2
     */
    public static String padLeft(String originalString, char paddingChar, int targetLength) {
        return padString(originalString, paddingChar, targetLength, true);
    }

    /**
     * 在字符串后端填充指定字符至规定长度，如果已有字符串超过这个长度则返回原字符串
     *
     * <p>处理逻辑：
     * <ul>
     *   <li>将参数委托给padString方法，设置前置填充标志为false</li>
     *   <li>从右侧（后端）开始填充指定字符，直到达到目标长度</li>
     * </ul>
     *
     * <p>典型应用场景：
     * <ul>
     *   <li>文本右对齐填充：在文本后添加空格或特殊字符</li>
     *   <li>数据块补齐：确保数据段达到固定长度要求</li>
     *   <li>协议报文构造：填充报文尾部以满足格式规范</li>
     * </ul>
     *
     * <p>示例：
     * <ul>
     *   <li>输入 "123", '0', 5 → 输出 "12300"(右侧补零)</li>
     *   <li>输入 "ABC", ' ', 6 → 输出 "ABC   "(右侧补空格)</li>
     *   <li>输入 "ABCDEFGH", '0', 5 → 输出 "ABCDEFGH"(超长不处理)</li>
     *   <li>输入 "", '*', 3 → 输出 "***"(空字符串全部填充)</li>
     * </ul>
     *
     * @param originalString 被填充的原始字符串，不能为null
     * @param paddingChar    用于填充的字符，可以是任意有效字符（如'0'、' '、'*'等）
     * @param targetLength   填充后的目标总长度，必须为非负整数
     * @return 后端填充后的字符串，若原字符串长度已超过目标长度则返回原字符串
     * @since 3.1.2
     */
    public static String padRight(String originalString, char paddingChar, int targetLength) {
        return padString(originalString, paddingChar, targetLength, false);
    }


    /**
     * 将已有字符串填充为规定长度，如果已有字符串超过这个长度则返回这个字符串
     *
     * <p>处理逻辑：
     * <ul>
     *   <li>首先获取原始字符串的长度</li>
     *   <li>若原始字符串长度已超过目标长度，直接返回原字符串不进行填充</li>
     *   <li>计算需要填充的字符数量(目标长度减去原始长度)</li>
     *   <li>使用指定字符生成填充字符串</li>
     *   <li>根据paddingPosition参数决定填充位置：true则填充在前，false则填充在后</li>
     * </ul>
     *
     * <p>典型应用场景：
     * 用于固定长度字段的格式化，如十六进制字符串补齐、数据对齐等场景
     *
     * <p>示例：
     * <ul>
     *   <li>输入 "ABC", '0', 5, true → 输出 "00ABC"(前填充)</li>
     *   <li>输入 "ABC", '0', 5, false → 输出 "ABC00"(后填充)</li>
     *   <li>输入 "ABCDEFG", '0', 5, true → 输出 "ABCDEFG"(超长不填充)</li>
     *   <li>输入 "", 'F', 4, false → 输出 "FFFF"(空字符串填充)</li>
     *   </ul>
     *
     * @param originalString  被填充的原始字符串，不能为null
     * @param paddingChar     填充使用的字符，可以是任意有效字符
     * @param targetLength    填充后的目标长度，必须为非负数
     * @param shouldPadBefore 是否在前端填充，true表示前填充，false表示后填充
     * @return 填充后的字符串，若原字符串长度超过目标长度则返回原字符串
     * @since 3.1.2
     */
    public static String padString(String originalString, char paddingChar, int targetLength, boolean shouldPadBefore) {
        final int originalLength = originalString.length();
        if (originalLength > targetLength) {
            return originalString;
        }

        String paddingContent = StringUtils.repeat(paddingChar, targetLength - originalLength);
        return shouldPadBefore ? paddingContent.concat(originalString) : originalString.concat(paddingContent);
    }
}
