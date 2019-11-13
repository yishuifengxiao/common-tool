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
 * @param <T>
 */
@ApiModel(value = "mybatis通用分页实体类", description = "用于所有接口的基于mybatis分页对象的通用返回数据")
public class TkPage<T> extends Page<T> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 257040631674056497L;
	/**
	 * 判断一个分页对象是否为空
	 * @param <T>
	 * @param pages
	 * @return 如果为空则返回true，否则为false
	 */
	public static <T> boolean isEmpty(PageInfo<T> pages) {

		if (pages == null) {
			return true;
		}
		if (pages.getList() == null || pages.getList().size() == 0) {
			return true;
		}
		return false;

	}
	
	

	/**
	 * 判断是否为一个非空的分页对象
	 * 
	 * @param pages
	 *            分页对象
	 * @return 如果是空返回为false，否则为true
	 */
	public static <T> boolean notEmpty(PageInfo<T> pages) {
		return !isEmpty(pages);
	}
	

	/**
	 * 根据mybatis的分页对象构建一个的自定义分页对象
	 * 
	 * @param page mybatis的分页对象
	 * @return 自定义分页对象
	 */
	public synchronized static <T> Page<T> of(PageInfo<T> page) {
		page = page == null ? PageInfo.of(new ArrayList<>()) : page;
		return new Page<>(page.getPageSize() + 0L, page.getPageNum() + 0L, page.getList(), page.getPages() + 0L,
				page.getTotal());

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
