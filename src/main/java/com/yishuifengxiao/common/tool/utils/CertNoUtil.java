package com.yishuifengxiao.common.tool.utils;

import java.time.LocalDate;

import org.apache.commons.lang3.StringUtils;

import com.yishuifengxiao.common.tool.exception.ValidateException;

import lombok.extern.slf4j.Slf4j;

/**
 * 身份证号校验工具<br/>
 * 18位身份证号码：第7、8、9、10位为出生年份(四位数)，第11、第12位为出生月份，第13、14位代表出生日期，第17位代表性别，奇数为男，偶数为女。
 * 
 * @author yishui
 * @date 2018年12月11日
 * @Version 0.0.1
 */
@Slf4j
public class CertNoUtil {

	// 每位加权因子
	private final static int POWER[] = { 7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2 };
	/**
	 * 二代身份证号的长度
	 */
	private final static int LENGTH_LONG_IDCARD = 18;

	/**
	 * 校验身份证号的合法性<br/>
	 * 非完全校验，未校验数据内容是否正确
	 * 
	 * @param idcard 身份证号
	 * @return true表示合法，false不合法
	 */
	public synchronized static boolean isValid(String idcard) { // 非18位为假
		// 判断出生日期是否正确
		try {
			getBirthday(idcard);
		} catch (ValidateException e) {
			return false;
		}
		// 获取前17位
		String idcard17 = idcard.substring(0, 17);
		// 获取第18位
		String idcard18Code = idcard.substring(17, 18);

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
	 * 从身份证号里提取出出生日期
	 * 
	 * @param str 身份证号
	 * @return 出生日期
	 * @throws ValidateException
	 */
	public synchronized static LocalDate getBirthday(String str) throws ValidateException {
		if (StringUtils.length(str) != LENGTH_LONG_IDCARD) {
			throw new ValidateException("身份证号格式不正确");
		}
		LocalDate localDate = null;
		try {
			String year = StringUtils.substring(str, 6, 10);
			String month = StringUtils.substring(str, 10, 12);
			String day = StringUtils.substring(str, 12, 14);
			localDate = LocalDate.of(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day));
		} catch (Exception e) {
			log.info("从身份证号{}中提取出生日期时出现异常，出现异常的原因为 {}", str, e.getMessage());
			throw new ValidateException("身份证号出生日期格式不正确");
		}
		return localDate;
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
	 * @param checkCode
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
	 * @throws NumberFormatException
	 */
	private static int[] converCharToInt(char[] c) throws NumberFormatException {
		int[] a = new int[c.length];
		int k = 0;
		for (char temp : c) {
			a[k++] = Integer.parseInt(String.valueOf(temp));
		}
		return a;
	}

	public static void main(String[] args) throws ValidateException {
		System.out.println(isValid("110101199003078275"));
		System.out.println(isValid("110101199003075453"));
		System.out.println(isValid("11010119900307301X"));
		System.out.println(isValid("110101199003073011"));
		System.out.println(isValid("42010319900307291X"));

		System.out.println(getBirthday("42010319900307291X"));
	}
}
