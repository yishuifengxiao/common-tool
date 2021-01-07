package com.yishuifengxiao.common.tool.converter;

import com.yishuifengxiao.common.tool.exception.CustomException;

/**
 * <p>分页数据转换器</p>
 * 将分页对象的里元数据转换成目标数据
 * 
 * @author yishui
 * @date 2019年11月13日
 * @version 1.0.0
 * @param <S> 源数据
 * @param <T> 目标数据
 */
public interface PageConverter<S, T> {

	/**
	 * 将源数据转换成目标数据
	 * 
	 * @param s 源数据
	 * @return 目标数据
	 * @throws CustomException
	 */
	T convert(S s) throws CustomException;
}
