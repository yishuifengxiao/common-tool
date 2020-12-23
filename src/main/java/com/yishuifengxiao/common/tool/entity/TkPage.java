package com.yishuifengxiao.common.tool.entity;

import java.util.ArrayList;

import com.github.pagehelper.PageInfo;

import io.swagger.annotations.ApiModel;

/**
 * mybatis分页对象处理
 * 
 * @author yishui
 * @date 2019年11月13日
 * @version 1.0.0
 * @param <S>
 */
@ApiModel(value = "mybatis通用分页实体类", description = "用于所有接口的基于mybatis分页对象的通用返回数据")
public class TkPage<S> extends Page<S> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 257040631674056497L;

	/**
	 * 根据mybatis的分页对象构建一个的自定义分页对象
	 * 
	 * @param page mybatis的分页对象
	 * @return 自定义分页对象
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
