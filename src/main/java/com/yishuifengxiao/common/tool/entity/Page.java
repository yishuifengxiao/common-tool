/**
 *
 */
package com.yishuifengxiao.common.tool.entity;

import com.yishuifengxiao.common.tool.collections.CollUtil;

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
public class Page<S> extends Slice implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1466782682580092020L;
    /**
     * 当前分页里的数据
     */
    protected List<S> data;

    /**
     * 总的记录数
     */
    protected Number total;

    /**
     * 总的分页数
     */
    protected Number pages;


    /**
     * 全参构造函数
     *
     * @param data  当前页的数据
     * @param total 记录总数
     * @param pages 总的分页数
     * @param size  分页大小
     * @param num   当前页页码，从1开始
     */
    public Page(List<S> data, Number total, Number pages, Number size, Number num) {
        this.size = size;
        this.num = num;
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
     * <li>分页大小:输入的size的值</li>
     * <li>当前页页码:1</li>
     * <li>总的分页数:0</li>
     * <li>记录总数:0</li>
     * </ol>
     *
     * @param <S>  分页对象里包含的数据的类型
     * @param size 分页大小
     * @return 分页对象
     */
    public static <S> Page<S> ofEmpty(Number size) {
        return new Page<>(new ArrayList<>(), 0L, 0L, size, DEFAULT_PAGE_NUM);
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
     * @param <S>  分页对象里包含的数据的类型
     * @param list 传入的总数据
     * @param size 分页大小
     * @param num  当前页页码
     * @return 分页对象
     */
    public static <S> Page<S> toPage(List<S> list, Number size, Number num) {
        if (CollUtil.isEmpty(list)) {
            return Page.ofEmpty(size);
        }
        if (size.longValue() <= 0) {
            size = DEFAULT_PAGE_SIZE;
        }
        if (num.longValue() <= 0) {
            num = DEFAULT_PAGE_NUM;
        }
        // 起始量
        long startNum = (num.longValue() - 1) * size.longValue();

        List<S> data = null == list ? Collections.emptyList() :
                list.stream().skip(startNum).limit(size.longValue()).collect(Collectors.toList());

        return Page.of(data, list.size(), size, num);
    }

    /**
     * 生成分页对象
     *
     * @param <S>   分页对象里包含的数据的类型
     * @param data  当前页数据
     * @param total 总的记录数
     * @param size  分页大小
     * @param num   当前页页码，从1开始
     * @return 分页对象
     */
    public static <S> Page<S> of(List<S> data, Number total, Number size, Number num) {
        if (null == size || size.longValue() <= 0) {
            size = DEFAULT_PAGE_SIZE;
        }
        if (null == num || num.longValue() <= 0) {
            num = DEFAULT_PAGE_NUM;
        }

        if (null == total || total.longValue() <= 0) {
            total = 0L;
        }


        return new Page<>(data, total, page(total, size), size, num);
    }

    /**
     * 构建一个分页对象
     *
     * @param data  当前页数据
     * @param total 总的记录数
     * @param slice 分页对象
     * @param <S>   当前页数据的数据类型
     * @return 分页对象
     */
    public static <S> Page<S> of(List<S> data, Number total, Slice slice) {
        return of(data, total, slice.size(), slice.num());
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
        return total.longValue() % size.longValue() == 0 ? total.longValue() / size.longValue() : (total.longValue() / size.longValue() + 1);
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
            return Page.of(Collections.emptyList(), this.total, this.size, this.num);
        }
        return Page.of(this.data.stream().map(converter::map).collect(Collectors.toList()), this.total, this.size, this.num);
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
            return Page.of(Collections.emptyList(), this.total, this.size, this.num);
        }
        return Page.of(converter.map(this.data), this.total, this.size, this.num);
    }


    /**
     * 设置分页大小
     *
     * @param size 分页大小
     * @return 当前分页对象
     */
    public Page<S> size(Number size) {
        this.size = size;
        return this;
    }


    /**
     * 设置当前页页码
     *
     * @param num 当前页页码
     * @return 当前分页对象
     */
    public Page<S> num(Number num) {
        this.num = num;
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

    /**
     * 是否为空的分页
     *
     * @return true表示为空的分页，false不是为非空分页对象
     */

    public boolean isEmpty() {
        return null == this.data || this.data.isEmpty();
    }

    @Override
    public String toString() {
        String builder = "Page [size=" + size + ", num=" + num + ", data=" + data + ", pages=" + pages + ", " + "total=" + total + "]";
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
