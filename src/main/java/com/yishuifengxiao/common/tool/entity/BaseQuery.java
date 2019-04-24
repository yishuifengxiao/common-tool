package com.yishuifengxiao.common.tool.entity;

import java.io.Serializable;

/**
 * 顶级查询参数类
 * 
 * @author yishui
 * @Date 2019年4月24日
 * @version 1.0.0
 */
public class BaseQuery implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 分页大小
	 */
	private Integer pageSize;

	/**
	 * 当前页页码
	 */
	private Integer pageNum;

	/**
	 * 获取分页大小
	 * 
	 * @return
	 */
	public Integer getPageSize() {
		return pageSize;
	}

	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
	}

	/**
	 * 获取当前页页码
	 * 
	 * @return
	 */
	public Integer getPageNum() {
		return pageNum;
	}

	public void setPageNum(Integer pageNum) {
		this.pageNum = pageNum;
	}

	/**
	 * 
	 * @param pageSize
	 *            分页大小
	 * @param pageNum
	 *            当前页页码
	 */
	public BaseQuery(Integer pageSize, Integer pageNum) {
		this.pageSize = pageSize;
		this.pageNum = pageNum;
	}

	public BaseQuery() {

	}

}
