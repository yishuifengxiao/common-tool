package com.yishuifengxiao.common.tool.entity;

import java.util.ArrayList;

import org.springframework.data.domain.PageImpl;

import io.swagger.annotations.ApiModel;

/**
 * 基于JPA分页对象的分页对象
 * 
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 * @param <S> 分页里数据的数据类型
 */
@ApiModel(value = "基于JPA分页对象的分页对象", description = "用于所有接口的基于jpa分页对象的通用返回数据")
public class JpaPage<S> extends Page<S> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 671498569643800274L;

	/**
	 * 根据spring data的分页对象构造一个分页对象
	 * 
	 * @param <S>  分页里数据的数据类型
	 * @param page spring data的分页对象
	 * @return 分页对象
	 */
	public static synchronized <S> Page<S> of(org.springframework.data.domain.Page<S> page) {
		page = page == null ? new PageImpl<>(new ArrayList<>()) : page;

		return Page.of(page.getContent(), page.getTotalElements(), page.getSize(), page.getNumber() + 1);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("JpaPage [getPageSize()=");
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
