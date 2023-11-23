package com.yishuifengxiao.common.tool.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 基础分页查询参数
 *
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
@ApiModel(value = "基础分页对象")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class Slice implements Serializable {

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
    @ApiModelProperty(value = "分页大小", example = "10", dataType = "java.lang.Long")
    protected Number size;

    /**
     * 当前页页码
     */
    @ApiModelProperty(value = "当前页页码,从1开始", example = "1", dataType = "java.lang.Long")
    protected Number num;


    /**
     * <p>获取分页大小</p>
     * <p>若分页大小为null或者<=0则返回默认值 10</p>
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
     * <p>若分页大小为null或者<=0则返回默认值 1</p>
     *
     * @return 当前页页码
     */
    @JsonIgnore
    public Number num() {
        if (null == this.num || this.num.longValue() <= 0) {
            return DEFAULT_PAGE_NUM;
        }

        return this.num;
    }

    /**
     * 构建一个基础的分页对象
     *
     * @param size 分页大小
     * @param num  当前页页码
     * @return 基础分页对象
     */
    public static Slice of(Number size, Number num) {
        size = null == size || size.longValue() <= 0 ? DEFAULT_PAGE_SIZE : size;
        num = null == num || num.longValue() <= 0 ? DEFAULT_PAGE_NUM : num;
        return new Slice(size, num);
    }

    /**
     * 分页的起始偏移量
     *
     * @return 起始偏移量
     */
    public Number startOffset() {
        return (this.num().longValue() - 1) * this.size().longValue();
    }

    /**
     * 分页的结束偏移量
     *
     * @return 结束偏移量
     */
    public Number endOffset() {
        return this.num().longValue() * this.size().longValue();
    }
}
