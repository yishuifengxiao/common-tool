package com.yishuifengxiao.common.tool.entity;

import java.util.ArrayList;

import org.springframework.data.domain.PageImpl;

import io.swagger.annotations.ApiModel;

/**
 * jpa分页对象处理
 * 
 * @author yishui
 * @date 2019年11月13日
 * @version 1.0.0
 * @param <S>
 */
@ApiModel(value = "JPA通用分页实体类", description = "用于所有接口的基于jpa分页对象的通用返回数据")
public class JpaPage<S> extends Page<S> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 671498569643800274L;

	/**
	 * 根据spring data的分页对象构造一个分页对象
	 * 
	 * @param page spring data的分页对象
	 * @return 自定义分页对象
	 */
	public static synchronized <S> Page<S> of(org.springframework.data.domain.Page<S> page) {
		page = page == null ? new PageImpl<>(new ArrayList<>()) : page;

		return Page.of(page.getContent(), page.getTotalElements(), page.getSize() + 0L, page.getNumber() + 1L);
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
