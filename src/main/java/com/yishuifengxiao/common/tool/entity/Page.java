/**
 * 
 */
package com.yishuifengxiao.common.tool.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.PageImpl;

import com.github.pagehelper.PageInfo;

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
	private Long totalPage;

	@ApiModelProperty("总的记录数")
	private Long total;

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
	 * 根据mybatis的分页对象构建一个的自定义分页对象
	 * 
	 * @param page
	 *            mybatis的分页对象
	 * @return 自定义分页对象
	 */
	public synchronized static <T> Page<T> of(PageInfo<T> page) {
		page = page == null ? PageInfo.of(new ArrayList<>()) : page;
		return new Page<>(page.getPageSize() + 0L, page.getPageNum() + 0L, page.getList(), page.getPages() + 0L,
				page.getTotal());

	}

	/**
	 * 根据spring data的分页对象构造一个分页对象
	 * 
	 * @param page
	 *            spring data的分页对象
	 * @return 自定义分页对象
	 */
	public synchronized static <T> Page<T> of(org.springframework.data.domain.Page<T> page) {
		page = page == null ? new PageImpl<>(new ArrayList<>()) : page;
		return new Page<>(page.getSize() + 0L, page.getNumber() + 1L, page.getContent(), page.getTotalPages() + 0L,
				page.getTotalElements());
	}

	/**
	 * 生成分页对象
	 * 
	 * @param pageSize
	 *            分页大小
	 * @param pageNum
	 *            当前页页码
	 * @param data
	 *            当前页数据
	 * @param totalPage
	 *            总的页码数
	 * @param total
	 *            总的记录数
	 * @return
	 */
	public static <T> Page<T> of(Long pageSize, Long pageNum, List<T> data, Long totalPage, Long total) {

		return new Page<>(pageSize, pageNum, data, totalPage, total);
	}

	/**
	 * 生成分页对象
	 * 
	 * @param data
	 *            当前页数据
	 * @param pageSize
	 *            分页大小
	 * @param pageNum
	 *            当前页页码
	 * @param total
	 *            总的记录数
	 * @return
	 */
	public static <T> Page<T> of(List<T> data, Long pageSize, Long pageNum, Long total) {
		long totalPage = (total % pageSize == 0) ? (total / pageSize) : (total / pageSize + 1);
		return new Page<>(pageSize, pageNum, data, totalPage, total);
	}

	/**
	 * 根据分页信息来源对象生成分页对象
	 * 
	 * @param data
	 *            当前页数据
	 * @param source
	 *            分页信息来源对象
	 * @return
	 */
	public static <T, U> Page<T> of(Page<U> source,List<T> data) {
		return new Page<>(source.getPageSize(), source.getPageNum(), data, source.getTotalPage(), source.getTotal());
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

	public Long getTotalPage() {
		return totalPage;
	}

	public void setTotalPage(Long totalPage) {
		this.totalPage = totalPage;
	}

	public Page(Long pageSize, Long pageNum, List<T> data, Long totalPage, Long total) {
		this.pageSize = pageSize;
		this.pageNum = pageNum;
		this.data = data;
		this.totalPage = totalPage;
		this.total = total;
	}

	public Page() {

	}

	@Override
	public String toString() {
		return "Page [pageSize=" + pageSize + ", pageNum=" + pageNum + ", data=" + data + ", totalPage=" + totalPage
				+ ", total=" + total + "]";
	}

}
