/**
 * 
 */
package com.yishuifengxiao.common.tool.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 分页对象类
 * 
 * @author yishui
 * @Date 2019年3月15日
 * @version 1.0.0
 */
@ApiModel(value = "通用分页实体类", description = "用于所有接口的通用返回数据")
public class Page<T> implements Serializable {
	/**
	 * 默认的当前页的页码
	 */
	public final static int DEFAULT_PAGE_NUM = 0;
	/**
	 * 默认的最小页的页码
	 */
	public final static int MIN_PAGE_NUM = 1;
	/**
	 * 默认的第一个元素的索引
	 */
	public final static int FIRST_ELEMENT_INDEX = 0;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1466782682580092020L;

	@ApiModelProperty("分页大小")
	private Long pageSize;

	@ApiModelProperty("当前页页码(从1开始)")
	private Long pageNum;

	@ApiModelProperty("当前分页里的数据")
	private List<T> data;

	@ApiModelProperty("总的分页数")
	private Long pages;

	@ApiModelProperty("总的记录数")
	private Long total;

	/**
	 * 对页码进行减一转换
	 * 
	 * @param pageSize 页码
	 * @return 当页码数小于1时，返回为0，否则为原始值减1
	 */
	public static int reduce(Integer pageSize) {
		if (pageSize == null || pageSize < MIN_PAGE_NUM) {
			return DEFAULT_PAGE_NUM;
		}
		return pageSize > DEFAULT_PAGE_NUM ? pageSize - MIN_PAGE_NUM : pageSize;
	}

	/**
	 * 根据数据构造当前页为1，分页大小为数据大小的分页对象
	 * 
	 * @param data
	 * @return
	 */
	public static <T> Page<T> of(List<T> data) {
		data = data == null ? new ArrayList<>() : data;
		return new Page<>(data.size() + 0L, 1L, data, 1L, data.size() + 0L);
	}

	/**
	 * 生成分页对象
	 * 
	 * @param pageSize  分页大小
	 * @param pageNum   当前页页码
	 * @param data      当前页数据
	 * @param totalPage 总的页码数
	 * @param total     总的记录数
	 * @return
	 */
	public static <T> Page<T> of(Long pageSize, Long pageNum, List<T> data, Long totalPage, Long total) {

		return new Page<>(pageSize, pageNum, data, totalPage, total);
	}

	/**
	 * 生成分页对象
	 * 
	 * @param data     当前页数据
	 * @param pageSize 分页大小
	 * @param pageNum  当前页页码
	 * @param total    总的记录数
	 * @return
	 */
	public static <T> Page<T> of(List<T> data, Long pageSize, Long pageNum, Long total) {
		long totalPage = (total % pageSize == 0) ? (total / pageSize) : (total / pageSize + 1);
		return new Page<>(pageSize, pageNum, data, totalPage, total);
	}

	/**
	 * 根据分页信息来源对象生成分页对象
	 * 
	 * @param data   当前页数据
	 * @param source 分页信息来源对象
	 * @return
	 */
	public static <T, U> Page<T> of(Page<U> source, List<T> data) {
		return new Page<>(source.getPageSize(), source.getPageNum(), data, source.getPages(), source.getTotal());
	}

	public Long getPageSize() {
		return pageSize;
	}

	public Page<T> setPageSize(Long pageSize) {
		this.pageSize = pageSize;
		return this;
	}

	public Long getPageNum() {
		return pageNum;
	}

	public Page<T> setPageNum(Long pageNum) {
		this.pageNum = pageNum;
		return this;
	}

	public Long getTotal() {
		return total;
	}

	public Page<T> setTotal(Long total) {
		this.total = total;
		return this;
	}

	public List<T> getData() {
		return data;
	}

	public Page<T> setData(List<T> data) {
		this.data = data;
		return this;
	}

	public Long getPages() {
		return pages;
	}

	public void setPages(Long pages) {
		this.pages = pages;
	}

	public Page(Long pageSize, Long pageNum, List<T> data, Long pages, Long total) {
		this.pageSize = pageSize;
		this.pageNum = pageNum;
		this.data = data;
		this.pages = pages;
		this.total = total;
	}

	public Page() {

	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Page [pageSize=");
		builder.append(pageSize);
		builder.append(", pageNum=");
		builder.append(pageNum);
		builder.append(", data=");
		builder.append(data);
		builder.append(", pages=");
		builder.append(pages);
		builder.append(", total=");
		builder.append(total);
		builder.append("]");
		return builder.toString();
	}

}
