/**
 * 
 */
package com.yishuifengxiao.common.tool.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.yishuifengxiao.common.tool.collections.EmptyUtil;
import com.yishuifengxiao.common.tool.constant.ErrorCode;
import com.yishuifengxiao.common.tool.converter.PageConverter;
import com.yishuifengxiao.common.tool.exception.CustomException;

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
public class Page<S> implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1466782682580092020L;
	/**
	 * 默认的当前页的页码
	 */
	public static final int DEFAULT_PAGE_NUM = 1;
	/**
	 * 默认的最小页的页码
	 */
	public static final int DEFAULT_PAGE_SIZE = 1;

	@ApiModelProperty("当前分页里的数据")
	private List<S> data;

	@ApiModelProperty("总的记录数")
	private Long total;

	@ApiModelProperty("总的分页数")
	private Long pages;

	@ApiModelProperty("分页大小")
	private Integer pageSize;

	@ApiModelProperty("当前页页码(从1开始)")
	private Integer pageNum;

	/**
	 *<p> 将一种类型数据的分页对象转换成另一种数据类型的分页对象</p>

	 * 【注意】 如果被转换后的数据为null,在转换后的分页对象的数据集里依然会包含null数据
	 * 
	 * @param <T>
	 * @param converter 数据转换器
	 * @return
	 * @throws CustomException
	 */
	public <T> Page<T> convert(PageConverter<S, T> converter) throws CustomException {
		return this.convert(converter, false);
	}

	/**
	 *<p> 将一种类型数据的分页对象转换成另一种数据类型的分页对象</p>
	 * 【注意】 如果进行过滤，转换后的数据里的数据条目可能少于原始的数据条目
	 * 
	 * @param <T>
	 * @param converter    数据转换器
	 * @param isFilterNull 是否对转换后的数据进行null值过滤，
	 * @return
	 * @throws CustomException
	 */
	public <T> Page<T> convert(PageConverter<S, T> converter, boolean isFilterNull) throws CustomException {
		if (null == converter) {
			throw new CustomException(ErrorCode.PARAM_NULL, "转换器不能为空");
		}
		if (null == this.data) {
			this.data = new ArrayList<>();
		}
		List<T> list = new ArrayList<>();
		for (S s : this.data) {
			T t = converter.convert(s);
			if (isFilterNull) {
				if (null != t) {
					list.add(t);
				}
			} else {
				list.add(t);
			}
		}
		return Page.of(list, total, pageSize, pageNum);
	}

	/**
	 *<p> 构造一个空的分页对象</p>

	 * 该分页对象的属性为:
	 * <ol>
	 * <li>分页大小:0</li>
	 * <li>当前页页码:1</li>
	 * <li>总的分页数:0</li>
	 * <li>记录总数:0</li>
	 * </ol>
	 * 
	 * @param t 分页数据里的数据类型
	 * @return
	 */
	public static <S> Page<S> ofEmpty() {
		return new Page<>(new ArrayList<>(), 0L, 0L, 0, DEFAULT_PAGE_NUM);
	}

	/**
	 * <p>构造一个空的分页对象</p>

	 * 该分页对象的属性为:
	 * <ol>
	 * <li>分页大小:输入的pageSize的值</li>
	 * <li>当前页页码:1</li>
	 * <li>总的分页数:0</li>
	 * <li>记录总数:0</li>
	 * </ol>
	 * 
	 * @param t 分页数据里的数据类型
	 * @param t 分页数据里的数据类型
	 * @return
	 */
	public static <S> Page<S> ofEmpty(int pageSize) {
		return new Page<>(new ArrayList<>(), 0L, 0L, pageSize, DEFAULT_PAGE_NUM);
	}

	/**
	 *<p> 根据数据构造当前页为1，分页大小为数据大小的分页对象</p>

	 * 该分页对象的属性为:
	 * <ol>
	 * <li>分页大小:data.size()</li>
	 * <li>当前页页码:1</li>
	 * <li>总的分页数:1</li>
	 * <li>记录总数:data.size()</li>
	 * </ol>
	 * 
	 * @param data 传入的数据
	 * @return
	 */
	public static <S> Page<S> toPage(List<S> data) {
		data = data == null ? new ArrayList<>() : data;

		return Page.of(data, data.size() + 0L, data.size(), DEFAULT_PAGE_NUM);
	}

	/**
	 * <p>从总的数据中根据分页参数获取一个分页对象</p>
	 *
	 * @param list     总的分页数据数据集
	 * @param pageSize 分页大小
	 * @param pageNum  当前页页码，从1开始
	 * @return 当前分页数据集
	 */
	public static <S> Page<S> toPage(List<S> list, int pageSize, int pageNum) {
		if (EmptyUtil.isEmpty(list)) {
			return Page.ofEmpty(pageSize);
		}
		if (pageSize <= 0) {
			pageSize = DEFAULT_PAGE_SIZE;
		}
		if (pageNum <= 0) {
			pageNum = DEFAULT_PAGE_NUM;
		}
		// 总的数据量
		int totalNum = list.size();
		// 起始量
		int startNum = (pageNum - 1) * pageSize;
		// 结束量
		int endNum = pageNum * pageSize;
		// 当前页的数据
		List<S> data = new ArrayList<>();

		if (endNum <= list.size()) {
			// 正常情况下
			data = list.subList(startNum, endNum);
		}

		if (startNum > list.size()) {
			// 起始页面过大
			data = new ArrayList<>();
		}

		if (startNum < list.size() && list.size() < endNum) {
			// 最后一页，且最后一页的数量不够
			data = list.subList(startNum, list.size());
		}

		return Page.of(data, totalNum + 0L, pageSize, pageNum);
	}

	/**
	 * 生成分页对象
	 * 
	 * @param data     当前页数据
	 * @param total    总的记录数
	 * @param pageSize 分页大小
	 * @param pageNum  当前页页码，从1开始
	 * @return
	 */
	public static <S> Page<S> of(List<S> data, long total, int pageSize, int pageNum) {
		if (pageSize <= 0) {
			pageSize = DEFAULT_PAGE_SIZE;
		}
		if (pageNum <= 0) {
			pageNum = DEFAULT_PAGE_NUM;
		}

		if (total <= 0) {
			total = 0L;
		}
		long totalPage = (total % pageSize == 0) ? (total / pageSize) : (total / pageSize + 1);

		return new Page<>(data, total, totalPage, pageSize, pageNum);
	}

	/**
	 * 根据分页信息来源对象生成分页对象
	 * 
	 * @param data   当前页数据
	 * @param source 分页信息来源对象
	 * @return
	 */
	public static <S, U> Page<S> of(Page<U> source, List<S> data) {
		if (null == source) {
			return Page.ofEmpty();
		}
		return Page.of(data, source.getTotal(), source.getPageSize(), source.getPageNum());
	}

	public Integer getPageSize() {
		return pageSize;
	}

	public Page<S> setPageSize(int pageSize) {
		this.pageSize = pageSize;
		return this;
	}

	public Integer getPageNum() {
		return pageNum;
	}

	public Page<S> setPageNum(int pageNum) {
		this.pageNum = pageNum;
		return this;
	}

	public Long getTotal() {
		return total;
	}

	public Page<S> setTotal(long total) {
		this.total = total;
		return this;
	}

	public List<S> getData() {
		return data;
	}

	public Page<S> setData(List<S> data) {
		this.data = data;
		return this;
	}

	public Long getPages() {
		return pages;
	}

	public void setPages(long pages) {
		this.pages = pages;
	}

	/**
	 * 
	 * @param data     当前页的数据
	 * @param total    记录总数
	 * @param pages    总的分页数
	 * @param pageSize 分页大小
	 * @param pageNum  当前页页码，从1开始
	 */
	public Page(List<S> data, long total, long pages, int pageSize, int pageNum) {
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
