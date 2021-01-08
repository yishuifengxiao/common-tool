package com.yishuifengxiao.common.tool.entity;

import java.io.Serializable;

/**
 * 顶级查询参数类
 * 
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
public class BaseQuery implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 分页大小
	 */
	protected Integer pageSize;

	/**
	 * 当前页页码
	 */
	protected Integer pageNum;

	/**
	 * 获取分页大小
	 * 
	 * @return 分页大小
	 */
	public Integer getPageSize() {
		return pageSize;
	}

	/**
	 * 设置分页发小
	 * 
	 * @param pageSize 分页大小
	 */
	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
	}

	/**
	 * 获取当前页页码
	 * 
	 * @return 当前页页码
	 */
	public Integer getPageNum() {
		return pageNum;
	}

	/**
	 * 设置当前页页码
	 * 
	 * @param pageNum 当前页页码
	 */
	public void setPageNum(Integer pageNum) {
		this.pageNum = pageNum;
	}

	/**
	 * 全参构造函数
	 * 
	 * @param pageSize 分页大小
	 * @param pageNum  当前页页码
	 */
	public BaseQuery(Integer pageSize, Integer pageNum) {
		this.pageSize = pageSize;
		this.pageNum = pageNum;
	}

	/**
	 * 无参构造函数
	 */
	public BaseQuery() {

	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("BaseQuery [pageSize=");
		builder.append(pageSize);
		builder.append(", pageNum=");
		builder.append(pageNum);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((pageNum == null) ? 0 : pageNum.hashCode());
		result = prime * result + ((pageSize == null) ? 0 : pageSize.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		BaseQuery other = (BaseQuery) obj;
		if (pageNum == null) {
			if (other.pageNum != null) {
				return false;
			}
		} else if (!pageNum.equals(other.pageNum)) {
			return false;
		}
		if (pageSize == null) {
			if (other.pageSize != null) {
				return false;
			}
		} else if (!pageSize.equals(other.pageSize)) {
			return false;
		}
		return true;
	}

}
