/**
 * 
 */
package com.yishuifengxiao.common.tool.utils;

/**
 * 对象工具类
 * 
 * @author qingteng
 * @date 2020年11月25日
 * @version 1.0.0
 */
public final class ObjUtil {

	/**
	 * 判断输入的数据是否全部不为null，如果所有的数据都不包含null则返回为true,否则返回为false<br/>
	 * 注意：输入参数个数为0时返回为true
	 * 
	 * @param objs 需要判断的数据
	 * @return 判断输入的数据是否全部不为null，如果所有的数据都不包含null则返回为true,否则返回为false
	 */
	public static boolean isNoneNull(Object... objs) {
		if (null != objs) {
			for (Object obj : objs) {
				if (null == obj) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * 判断输入的数据是否全部为null，如果是则返回为true，否则为false<br/>
	 * 注意：输入参数个数为0时返回为true
	 * 
	 * @param objs 需要判断的数据
	 * @return 数据是否全部为null，如果是则返回为true，否则为false
	 */
	public static boolean isAllNull(Object... objs) {
		if (null != objs) {
			for (Object obj : objs) {
				if (null != obj) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * 检查输入的数据是否包含null值，只要一个值为null就返回true,否则为false<br/>
	 * 注意：输入参数个数为0时返回为false
	 * 
	 * @param objs 需要判断的数据
	 * @return 数据是否包含null值，只要一个值为null就返回true,否则为false
	 */
	public static boolean hasNull(Object... objs) {
		if (null != objs) {
			for (Object obj : objs) {
				if (null == obj) {
					return true;
				}
			}
		}
		return false;
	}

}
