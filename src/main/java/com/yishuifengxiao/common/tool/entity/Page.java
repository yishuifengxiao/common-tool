/**
 * 
 */
package com.yishuifengxiao.common.tool.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.yishuifengxiao.common.tool.collections.EmptyUtil;
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

	@ApiModelProperty("当前分页里的数据")
	private List<S> data;

	@ApiModelProperty("总的记录数")
	private Long total;

	@ApiModelProperty("总的分页数")
	private Long pages;

	@ApiModelProperty("分页大小")
	private Long pageSize;

	@ApiModelProperty("当前页页码(从1开始)")
	private Long pageNum;

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
	 * 将一种类型数据的分页对象转换成另一种数据类型的分页对象<br/>
	 * <br/>
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
	 * 将一种类型数据的分页对象转换成另一种数据类型的分页对象<br/>
	 * <br/>
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
			throw new CustomException("转换器不能为空");
		}
		if (null == this.data) {
			this.data = new ArrayList<>();
		}
		List<T> list = new ArrayList<>();
		for (S t : this.data) {
			T s = converter.convert(t);
			if (isFilterNull) {
				if (null != s) {
					list.add(s);
				}
			} else {
				list.add(s);
			}
		}
		return Page.of(list, total, pageSize, pageNum);
	}

	/**
	 * 构造一个空的分页对象<br/>
	 * <br/>
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
	public static <S> Page<S> ofEmpty(S t) {
		return new Page<>(new ArrayList<>(), 0L, 0L, 0L, 1L);
	}

	/**
	 * 根据数据构造当前页为1，分页大小为数据大小的分页对象<br/>
	 * <br/>
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

		return Page.of(data, data.size() + 0L, data.size() + 0L, 1L);
	}

	/**
	 * 从总的数据中根据分页参数获取分页对象<br/>
	 *
	 * @param list     总的分页数据数据集
	 * @param pageSize 分页大小
	 * @param pageNum  当前页页码
	 * @return 当前分页数据集
	 */
	public static <S> Page<S> toPage(List<S> list, int pageSize, int pageNum) {
		if (EmptyUtil.isEmpty(list)) {
			return null;
		}
		if (pageSize <= 0) {
			pageSize = 1;
		}
		if (pageNum <= 0) {
			pageNum = 1;
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

		return Page.of(data, totalNum + 0L, pageSize + 0L, pageNum + 0L);
	}

	/**
	 * 生成分页对象
	 * 
	 * @param data     当前页数据
	 * @param total    总的记录数
	 * @param pageSize 分页大小
	 * @param pageNum  当前页页码
	 * @return
	 */
	public static <S> Page<S> of(List<S> data, long total, long pageSize, long pageNum) {
		if (pageSize <= 0) {
			pageSize = 1L;
		}
		if (pageNum <= 0) {
			pageNum = 1L;
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
			return null;
		}
		return Page.of(data, source.getTotal(), source.getPageSize(), source.getPageNum());
	}

	public Long getPageSize() {
		return pageSize;
	}

	public Page<S> setPageSize(Long pageSize) {
		this.pageSize = pageSize;
		return this;
	}

	public Long getPageNum() {
		return pageNum;
	}

	public Page<S> setPageNum(Long pageNum) {
		this.pageNum = pageNum;
		return this;
	}

	public Long getTotal() {
		return total;
	}

	public Page<S> setTotal(Long total) {
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

	public void setPages(Long pages) {
		this.pages = pages;
	}

	/**
	 * 
	 * @param data     当前页的数据
	 * @param total    记录总数
	 * @param pages    总的分页数
	 * @param pageSize 分页大小
	 * @param pageNum  当前页页码
	 */
	public Page(List<S> data, Long total, Long pages, Long pageSize, Long pageNum) {
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
