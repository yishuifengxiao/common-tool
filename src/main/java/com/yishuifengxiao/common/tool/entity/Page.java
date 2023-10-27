/**
 *
 */
package com.yishuifengxiao.common.tool.entity;

import com.yishuifengxiao.common.tool.collections.CollectionUtil;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 通用分页对象
 * </p>
 * 统一分页数据的返回形式
 *
 * @param <S> 分页里数据的数据类型
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
@ApiModel(value = " 通用分页对象", description = "用于所有接口的通用返回数据")
@Builder
public class Page<S> implements Serializable {
    /**
     * 默认的当前页的页码
     */
    public static final int DEFAULT_PAGE_NUM = 1;
    /**
     * 默认的最小页的页码
     */
    public static final int DEFAULT_PAGE_SIZE = 10;
    /**
     *
     */
    private static final long serialVersionUID = 1466782682580092020L;
    /**
     * 当前分页里的数据
     */
    @ApiModelProperty("当前分页里的数据")
    protected List<S> data;

    /**
     * 总的记录数
     */
    @ApiModelProperty("总的记录数")
    protected Number total;

    /**
     * 总的分页数
     */
    @ApiModelProperty("总的分页数")
    protected Number pages;

    /**
     * 分页大小
     */
    @ApiModelProperty("分页大小")
    protected Number pageSize;

    /**
     * 当前页页码(从1开始)
     */
    @ApiModelProperty("当前页页码(从1开始)")
    protected Number pageNum;

    /**
     * 全参构造函数
     *
     * @param data     当前页的数据
     * @param total    记录总数
     * @param pages    总的分页数
     * @param pageSize 分页大小
     * @param pageNum  当前页页码，从1开始
     */
    public Page(List<S> data, Number total, Number pages, Number pageSize, Number pageNum) {
        this.pageSize = pageSize;
        this.pageNum = pageNum;
        this.data = data;
        this.pages = pages;
        this.total = total;
    }

    /**
     * 无参构造函数
     */
    public Page() {

    }

    /**
     * <p>
     * 构造一个空的分页对象
     * </p>
     * <p>
     * 该分页对象的属性为:
     * <ol>
     * <li>分页大小:0</li>
     * <li>当前页页码:1</li>
     * <li>总的分页数:0</li>
     * <li>记录总数:0</li>
     * </ol>
     *
     * @param <S> 分页对象里包含的数据的类型
     * @return 分页对象
     */
    public static <S> Page<S> ofEmpty() {
        return new Page<>(new ArrayList<>(), 0L, 0L, DEFAULT_PAGE_SIZE, DEFAULT_PAGE_NUM);
    }

    /**
     * <p>
     * 构造一个空的分页对象
     * </p>
     * <p>
     * 该分页对象的属性为:
     * <ol>
     * <li>分页大小:输入的pageSize的值</li>
     * <li>当前页页码:1</li>
     * <li>总的分页数:0</li>
     * <li>记录总数:0</li>
     * </ol>
     *
     * @param <S>      分页对象里包含的数据的类型
     * @param pageSize 分页大小
     * @return 分页对象
     */
    public static <S> Page<S> ofEmpty(Number pageSize) {
        return new Page<>(new ArrayList<>(), 0L, 0L, pageSize, DEFAULT_PAGE_NUM);
    }

    /**
     * <p>
     * 根据数据构造当前页为1，分页大小为数据大小的分页对象
     * </p>
     * <p>
     * 该分页对象的属性为:
     * <ol>
     * <li>分页大小:data.size()</li>
     * <li>当前页页码:1</li>
     * <li>总的分页数:1</li>
     * <li>记录总数:data.size()</li>
     * </ol>
     *
     * @param <S>  分页对象里包含的数据的类型
     * @param data 当前页的数据
     * @return 分页对象
     */
    public static <S> Page<S> toPage(List<S> data) {
        data = data == null ? new ArrayList<>() : data;

        return Page.of(data, data.size(), data.size(), DEFAULT_PAGE_NUM);
    }

    /**
     * 从总的数据中根据分页参数获取一个分页对象
     *
     * @param <S>      分页对象里包含的数据的类型
     * @param list     传入的总数据
     * @param pageSize 分页大小
     * @param pageNum  当前页页码
     * @return 分页对象
     */
    public static <S> Page<S> toPage(List<S> list, Number pageSize, Number pageNum) {
        if (CollectionUtil.isEmpty(list)) {
            return Page.ofEmpty(pageSize);
        }
        if (pageSize.longValue() <= 0) {
            pageSize = DEFAULT_PAGE_SIZE;
        }
        if (pageNum.longValue() <= 0) {
            pageNum = DEFAULT_PAGE_NUM;
        }
        // 总的数据量
        long totalNum = list.size();
        // 起始量
        long startNum = (pageNum.longValue() - 1) * pageSize.longValue();
        // 结束量
        long endNum = pageNum.longValue() * pageSize.longValue();
        // 当前页的数据
        List<S> data = new ArrayList<>();

        if (endNum <= list.size()) {
            // 正常情况下
            data = list.stream().skip(startNum).limit(endNum).collect(Collectors.toList());
        }

        if (startNum > list.size()) {
            // 起始页面过大
            data = new ArrayList<>();
        }

        if (startNum < list.size() && list.size() < endNum) {
            // 最后一页，且最后一页的数量不够
            data = list.stream().skip(startNum).limit(list.size()).collect(Collectors.toList());
        }

        return Page.of(data, totalNum, pageSize, pageNum);
    }

    /**
     * 生成分页对象
     *
     * @param <S>      分页对象里包含的数据的类型
     * @param data     当前页数据
     * @param total    总的记录数
     * @param pageSize 分页大小
     * @param pageNum  当前页页码，从1开始
     * @return 分页对象
     */
    public static <S> Page<S> of(List<S> data, Number total, Number pageSize, Number pageNum) {
        if (pageSize.longValue() <= 0) {
            pageSize = DEFAULT_PAGE_SIZE;
        }
        if (pageNum.longValue() <= 0) {
            pageNum = DEFAULT_PAGE_NUM;
        }

        if (total.longValue() <= 0) {
            total = 0L;
        }


        return new Page<>(data, total, page(total, pageSize), pageSize, pageNum);
    }

    /**
     * 计算分页大小
     *
     * @param total 全部数据数量
     * @param size  分页大小，若为空则默认为10
     * @return 分页数量
     */
    public static Number page(Number total, Number size) {
        if (null == total) {
            total = 0;
        }
        if (null == size) {
            size = DEFAULT_PAGE_SIZE;
        }
        return total.longValue() % size.longValue() == 0 ? total.longValue() / size.longValue() :
                (total.longValue() / size.longValue() + 1);
    }

    /**
     * <p>将一种类型数据的分页对象转换成另一种数据类型的分页对象</p>
     *
     * @param <T>       分页元素转换工具的类型
     * @param converter 分页元素转换工具
     * @return 另一种数据类型的分页对象
     */
    public <T> Page<T> map(DataConverter<S, T> converter) {
        if (null == this.data) {
            return Page.of(Collections.emptyList(), this.total, this.pageSize, this.pageNum);
        }
        return Page.of(this.data.stream().map(converter::map).collect(Collectors.toList()), this.total, this.pageSize
                , this.pageNum);
    }

    /**
     * <p>将一种类型数据的分页对象转换成另一种数据类型的分页对象</p>
     *
     * @param <T>       分页元素转换工具的类型
     * @param converter 分页元素转换工具
     * @return 另一种数据类型的分页对象
     */
    public <T> Page<T> maps(ListConverter<S, T> converter) {
        if (null == this.data) {
            return Page.of(Collections.emptyList(), this.total, this.pageSize, this.pageNum);
        }
        return Page.of(converter.map(this.data), this.total, this.pageSize, this.pageNum);
    }

    /**
     * 获取分页大小
     *
     * @return 分页大小
     */
    public Number getPageSize() {
        return this.pageSize;
    }

    /**
     * 设置分页大小
     *
     * @param pageSize 分页大小
     * @return 当前分页对象
     */
    public Page<S> setPageSize(Number pageSize) {
        this.pageSize = pageSize;
        return this;
    }

    /**
     * 获取当前页页码
     *
     * @return 当前页页码
     */
    public Number getPageNum() {
        return this.pageNum;
    }

    /**
     * 设置当前页页码
     *
     * @param pageNum 当前页页码
     * @return 当前分页对象
     */
    public Page<S> setPageNum(Number pageNum) {
        this.pageNum = pageNum;
        return this;
    }

    /**
     * 获取总的记录的数量
     *
     * @return 总的记录的数量
     */
    public Number getTotal() {
        return this.total;
    }

    /**
     * 设置总的总的记录的数量
     *
     * @param total 总的记录的数量
     * @return 当前分页对象
     */
    public Page<S> setTotal(Number total) {
        this.total = total;
        return this;
    }

    /**
     * 获取当前页里包含的数据
     *
     * @return 当前页里包含的数据
     */
    public List<S> getData() {
        return this.data;
    }

    /**
     * 设置当前页里包含的数据
     *
     * @param data 当前页里包含的数据
     * @return 当前分页对象
     */
    public Page<S> setData(List<S> data) {
        this.data = data;
        return this;
    }

    /***
     * 获取分页的数量
     *
     * @return 分页的数量
     */
    public Number getPages() {
        return this.pages;
    }

    /**
     * 设置分页的数量
     *
     * @param pages 分页的数量
     * @return 当前分页对象
     */
    public Page<S> setPages(Number pages) {
        this.pages = pages;
        return this;
    }

    @Override
    public String toString() {
        String builder =
                "Page [pageSize=" + pageSize + ", pageNum=" + pageNum + ", data=" + data + ", pages=" + pages + ", " +
                        "total=" + total + "]";
        return builder;
    }

    /**
     * <p>
     * 分页数据转换器
     * </p>
     * 将分页对象里的源数据转换成目标数据
     *
     * @param <S> 源数据
     * @param <T> 目标数据
     * @author yishui
     * @version 1.0.0
     * @since 1.0.0
     */
    @FunctionalInterface
    public interface DataConverter<S, T> {

        /**
         * 将源数据转换成目标数据
         *
         * @param s 源数据
         * @return 目标数据
         */
        T map(S s);
    }

    /**
     * <p>
     * 分页数据转换器
     * </p>
     * 将分页对象里的源数据转换成目标数据
     *
     * @param <S> 源数据
     * @param <T> 目标数据
     * @author yishui
     * @version 1.0.0
     * @since 1.0.0
     */
    @FunctionalInterface
    public interface ListConverter<S, T> {

        /**
         * 将源数据转换成目标数据
         *
         * @param s 源数据
         * @return 目标数据
         */
        List<T> map(List<S> s);
    }

}
