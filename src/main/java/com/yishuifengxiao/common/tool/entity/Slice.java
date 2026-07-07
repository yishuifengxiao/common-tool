package com.yishuifengxiao.common.tool.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>基础分页对象</p>
 * <p>提供分页参数的基础实现，包括分页大小和当前页码。</p>
 * <p>特性：</p>
 * <ul>
 * <li>默认分页大小为10</li>
 * <li>默认页码从1开始</li>
 * <li>支持计算分页偏移量</li>
 * <li>提供空值安全的参数获取方法</li>
 * </ul>
 *
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class Slice implements Serializable {
    public static final Slice DEFAULT_SLICE = Slice.of(10, 1);

    /**
     *
     */
    private static final long serialVersionUID = 1L;


    /**
     * 默认的分页大小
     */
    public final static int DEFAULT_PAGE_SIZE = 10;

    /**
     * 默认的当前页页码
     */
    public final static int DEFAULT_PAGE_NUM = 1;

    /**
     * 分页大小
     */
    protected Number size;

    /**
     * 当前页页码
     */
    protected Number current;


    /**
     * <p>获取分页大小</p>
     * <p>若分页大小为null或者&#60;=0则返回默认值 10</p>
     *
     * @return 分页大小
     */
    @JsonIgnore
    public Number size() {
        if (null == this.size || this.size.longValue() <= 0) {
            return DEFAULT_PAGE_SIZE;
        }

        return this.size;
    }

    /**
     * <p>获取当前页页码</p>
     * <p>若分页大小为null或者&#60;=0则返回默认值 1</p>
     *
     * @return 当前页页码
     */
    @JsonIgnore
    public Number current() {
        if (null == this.current || this.current.longValue() <= 0) {
            return DEFAULT_PAGE_NUM;
        }

        return this.current;
    }

    /**
     * 构建一个基础的分页对象
     *
     * @param size    分页大小
     * @param current 当前页页码
     * @return 基础分页对象
     */
    public static Slice of(Number size, Number current) {
        size = null == size || size.longValue() <= 0 ? DEFAULT_PAGE_SIZE : size;
        current = null == current || current.longValue() <= 0 ? DEFAULT_PAGE_NUM : current;
        return new Slice(size, current);
    }

    /**
     * 分页的起始偏移量
     *
     * @return 起始偏移量
     */
    public Number startOffset() {
        return (this.current().longValue() - 1) * this.size().longValue();
    }

    /**
     * 分页的结束偏移量
     *
     * @return 结束偏移量
     */
    public Number endOffset() {
        return this.current().longValue() * this.size().longValue();
    }

    /**
     * <p>获取当前页页码</p>
     * <p>若分页大小为null或者&#60;=0则返回默认值 1</p>
     *
     * @return 当前页页码
     */
    @JsonIgnore
    public Number num() {


        return this.current();
    }
}
