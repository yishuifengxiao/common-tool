package com.yishuifengxiao.common.tool.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.yishuifengxiao.common.tool.exception.UncheckedException;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * 身份证号校验工具
 * </p>
 * <p>
 * 该工具主要是为了校验当前数据是否为一个合法的18位身份证号。
 * 18位身份证号码的含义如下：第7、8、9、10位为出生年份(四位数)，第11、第12位为出生月份，
 * 第13、14位代表出生日期，第17位代表性别，奇数为男，偶数为女。
 * </p>
 * <ol>
 * <li>判断给定的字符串是否为一个合法的18位身份证号</li>
 * <li>从合法的身份证号中提取出当前身份证里的出生日期</li>
 * </ol>
 * <strong>该工具是一个线程安全类的工具。</strong>
 *
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
public final class CertNoUtil {
	/**
	 * 18位身份证正则表示
	 */
	private static final Pattern REGEX_18_CARD = Pattern.compile(
			"^[1-9]\\d{5}(18|19|([23]\\d))\\d{2}((0[1-9])|(10|11|12))(([0-2][1-9])|10|20|30|31)\\d{3}[0-9Xx]$");

	/**
	 * 每位加权因子
	 */
	private static final int[] POWER = { 7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2 };

	/**
	 * <p>
	 * 校验18位身份证号的合法性
	 * </p>
	 * 
	 * <p>
	 * <strong>线程安全</strong>
	 * </p>
	 *
	 * @param idcard 身份证号
	 * @return true表示合法，false不合法
	 */
	public static synchronized boolean isValid(String idcard) { // 非18位为假
		// 判断出生日期是否正确
		if (StringUtils.isBlank(idcard)) {
			return false;
		}
		if (!REGEX_18_CARD.matcher(idcard.trim()).matches()) {
			return false;
		}
		// 获取前17位
		String idcard17 = idcard.trim().substring(0, 17);
		// 获取第18位
		String idcard18Code = idcard.trim().substring(17, 18);

		// 是否都为数字
		if (!isDigital(idcard17)) {
			return false;
		}

		char[] c = idcard17.toCharArray();

		int[] bit = converCharToInt(c);

		int sum17 = getPowerSum(bit);

		// 将和值与11取模得到余数进行校验码判断
		String checkCode = getCheckCodeBySum(sum17);
		if (null == checkCode) {
			return false;
		}
		// 将身份证的第18位与算出来的校码进行匹配，不相等就为假
		if (!idcard18Code.equalsIgnoreCase(checkCode)) {
			return false;
		}

		return true;
	}

	/**
	 * <p>
	 * 从身份证号里提取出出生日期
	 * </p>
	 * <p>
	 * <strong>线程安全</strong>
	 * </p>
	 * 
	 * @param idcard 身份证号
	 * @return 出生日期
	 */
	public static synchronized LocalDate extractBirthday(String idcard) {
		if (!isValid(idcard)) {
			throw new UncheckedException("身份证号格式不正确");
		}
		try {
			String dateStr = StringUtils.substring(idcard.trim(), 6, 14);
			return LocalDate.parse(dateStr, DateTimeFormatter.BASIC_ISO_DATE);
		} catch (Exception e) {
			log.info("【易水工具】从身份证号{}中提取出生日期时出现异常，出现异常的原因为 {}", idcard.trim(), e.getMessage());
			throw new UncheckedException("身份证号出生日期格式不正确");
		}
	}

	/**
	 * 判断输入的参数是否为纯数字
	 *
	 * @param str 输入的参数
	 * @return true表示为纯数字
	 */
	private static boolean isDigital(String str) {
		return str == null || "".equals(str) ? false : str.matches("^[0-9]*$");
	}

	/**
	 * 将身份证的每位和对应位的加权因子相乘之后，再得到和值
	 *
	 * @param bit
	 * @return
	 */
	private static int getPowerSum(int[] bit) {

		int sum = 0;

		if (POWER.length != bit.length) {
			return sum;
		}

		for (int i = 0; i < bit.length; i++) {
			for (int j = 0; j < POWER.length; j++) {
				if (i == j) {
					sum = sum + bit[i] * POWER[j];
				}
			}
		}
		return sum;
	}

	/**
	 * 将和值与11取模得到余数进行校验码判断
	 *
	 * @param sum17
	 * @return 校验位
	 */
	private static String getCheckCodeBySum(int sum17) {
		String checkCode = null;
		switch (sum17 % 11) {
		case 10:
			checkCode = "2";
			break;
		case 9:
			checkCode = "3";
			break;
		case 8:
			checkCode = "4";
			break;
		case 7:
			checkCode = "5";
			break;
		case 6:
			checkCode = "6";
			break;
		case 5:
			checkCode = "7";
			break;
		case 4:
			checkCode = "8";
			break;
		case 3:
			checkCode = "9";
			break;
		case 2:
			checkCode = "x";
			break;
		case 1:
			checkCode = "0";
			break;
		case 0:
			checkCode = "1";
			break;
		default:
			break;
		}
		return checkCode;
	}

	/**
	 * 将字符数组转为整型数组
	 *
	 * @param c
	 * @return
	 */
	private static int[] converCharToInt(char[] c) {
		int[] a = new int[c.length];
		int k = 0;
		for (char temp : c) {
			a[k++] = Integer.parseInt(String.valueOf(temp));
		}
		return a;
	}

}
