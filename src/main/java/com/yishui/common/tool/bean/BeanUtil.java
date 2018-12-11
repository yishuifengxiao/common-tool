/**
 * 
 */
package com.yishui.common.tool.bean;

import org.springframework.cglib.beans.BeanCopier;

/**
 * 对象转换类
 * 
 * @author yishui
 * @date 2018年12月6日
 * @Version 0.0.1
 */
public final class BeanUtil {
	/**
	 * 将源对象里属性值复制给目标对象
	 * 
	 * @param source 源对象
	 * @param target 目标对象
	 * @param converter 目标对象
	 * @return
	 */
	public static <S, T> T copy(S source, T target) {
		BeanCopier.create(source.getClass(), target.getClass(), false).copy(source, target, null);
		return target;

	}

}
