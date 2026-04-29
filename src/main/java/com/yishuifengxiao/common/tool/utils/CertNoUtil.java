package com.yishuifengxiao.common.tool.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.yishuifengxiao.common.tool.exception.UncheckedException;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>身份证号校验工具类</p>
 * <p>提供18位身份证号的合法性校验和信息提取功能。</p>
 * <p>18位身份证号码结构：</p>
 * <ul>
 * <li>第1-6位：行政区划代码</li>
 * <li>第7-14位：出生年月日（YYYYMMDD）</li>
 * <li>第15-17位：顺序码（第17位奇数为男，偶数为女）</li>
 * <li>第18位：校验码</li>
 * </ul>
 * <p>特性：</p>
 * <ul>
 * <li>校验18位身份证号的合法性（格式验证+校验码验证）</li>
 * <li>从身份证号中提取出生日期</li>
 * </ul>
 *
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
public final class CertNoUtil {
    /**
     * 18位身份证正则表达式
     */
    private static final Pattern ID_CARD_18_PATTERN = Pattern.compile("^[1-9]\\d{5}(18|19|([23]\\d))\\d{2}((0[1-9])|" +
            "(10|11|12))(([0-2][1-9])|10|20|30|31)\\d{3}[0-9Xx]$");

    /**
     * 每位加权因子
     */
    private static final int[] WEIGHT_FACTORS = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};

    /**
     * 校验码映射表
     */
    private static final String[] CHECK_CODE_MAP = {"1", "0", "X", "9", "8", "7", "6", "5", "4", "3", "2"};

    /**
     * <p>
     * 校验18位身份证号的合法性
     * </p>
     *
     * @param idcard 身份证号
     * @return true表示合法，false不合法
     */
    public static boolean isValid(String idcard) {
        if (StringUtils.isBlank(idcard)) {
            return false;
        }
        idcard = idcard.trim();
        if (!ID_CARD_18_PATTERN.matcher(idcard).matches()) {
            return false;
        }

        String idcard17 = idcard.substring(0, 17);
        String idcard18Code = idcard.substring(17, 18);

        if (!isNumeric(idcard17)) {
            return false;
        }

        char[] chars = idcard17.toCharArray();
        int[] digits = convertCharsToInts(chars);
        int sum = calculateWeightedSum(digits);
        String checkCode = getCheckCodeBySum(sum);

        if (checkCode == null || !idcard18Code.equalsIgnoreCase(checkCode)) {
            return false;
        }

        return true;
    }

    /**
     * <p>
     * 从身份证号里提取出出生日期
     * </p>
     *
     * @param idcard 身份证号
     * @return 出生日期
     */
    public static LocalDate extractBirthday(String idcard) {
        if (!isValid(idcard)) {
            throw new UncheckedException("身份证号格式不正确").setContext(idcard);
        }
        try {
            String dateStr = StringUtils.substring(idcard.trim(), 6, 14);
            return LocalDate.parse(dateStr, DateTimeFormatter.BASIC_ISO_DATE);
        } catch (Exception e) {
            if (log.isInfoEnabled()) {
                log.info("An exception occurred while extracting the birth date from ID number {}. The reason for the" +
                        " " +
                        "exception is {}", idcard.trim(), e.getMessage());
            }

            throw new UncheckedException("身份证号出生日期格式不正确").setContext(idcard);
        }
    }

    /**
     * 判断输入的字符串是否为纯数字
     *
     * @param str 输入的字符串
     * @return true表示为纯数字，false表示包含非数字字符或为空
     */
    private static boolean isNumeric(String str) {
        return str != null && !str.isEmpty() && str.matches("^[0-9]+$");
    }

    /**
     * 将身份证前17位每位数字和对应位的加权因子相乘后求和
     *
     * @param digits 身份证前17位数字数组
     * @return 加权求和结果
     */
    private static int calculateWeightedSum(int[] digits) {
        int sum = 0;
        for (int i = 0; i < digits.length; i++) {
            sum += digits[i] * WEIGHT_FACTORS[i];
        }
        return sum;
    }

    /**
     * 将加权和与11取模得到余数，通过余数获取校验码
     *
     * @param weightedSum 加权和
     * @return 校验码，如果余数超出范围则返回null
     */
    private static String getCheckCodeBySum(int weightedSum) {
        int remainder = weightedSum % 11;
        if (remainder >= 0 && remainder < CHECK_CODE_MAP.length) {
            return CHECK_CODE_MAP[remainder];
        }
        return null;
    }

    /**
     * 将字符数组转换为整型数组
     *
     * @param chars 字符数组，每个元素应为'0'-'9'的字符
     * @return 转换后的整型数组
     */
    private static int[] convertCharsToInts(char[] chars) {
        int[] result = new int[chars.length];
        for (int i = 0; i < chars.length; i++) {
            result[i] = chars[i] - '0';
        }
        return result;
    }

}
