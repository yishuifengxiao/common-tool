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
    @ApiModelProperty(value = "分页大小", example = "10")
    protected Number size;

    /**
     * 当前页页码
     */
    @ApiModelProperty(value = "当前页页码,从1开始", example = "1")
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
}
