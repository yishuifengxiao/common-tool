package com.yishuifengxiao.common.tool.entity;

import java.util.ArrayList;

import com.github.pagehelper.PageInfo;

import io.swagger.annotations.ApiModel;

/**
 * 基于mybatis分页对象的分页对象
 * 
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 * @param <S> 分页对象里的数据的类型
 */
@ApiModel(value = "mybatis通用分页实体类", description = "用于所有接口的基于mybatis分页对象的通用返回数据")
public class TkPage<S> extends Page<S> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 257040631674056497L;

	/**
	 * 根据mybatis的分页对象构建一个的通用分页对象
	 * 
	 * @param <S>  分页对象里的数据的类型
	 * @param page 分页对象
	 * @return 通用分页对象
	 */
	public static synchronized <S> Page<S> of(PageInfo<S> page) {
		page = page == null ? PageInfo.of(new ArrayList<>()) : page;
		return Page.of(page.getList(), page.getTotal(), page.getSize(), page.getPageNum());

	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TkPage [getPageSize()=");
		builder.append(getPageSize());
		builder.append(", getPageNum()=");
		builder.append(getPageNum());
		builder.append(", getTotal()=");
		builder.append(getTotal());
		builder.append(", getData()=");
		builder.append(getData());
		builder.append(", getPages()=");
		builder.append(getPages());
		builder.append(", toString()=");
		builder.append(super.toString());
		builder.append(", getClass()=");
		builder.append(getClass());
		builder.append(", hashCode()=");
		builder.append(hashCode());
		builder.append("]");
		return builder.toString();
	}

}
