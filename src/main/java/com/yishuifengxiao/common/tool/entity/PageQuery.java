package com.yishuifengxiao.common.tool.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Optional;


/**
 * 基础分页查询参数
 *
 * @param <T> 查询参数的类型
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
@Schema(name = "基础分页查询对象")
@Data
@Accessors(chain = true)
public class PageQuery<T> extends Slice implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    /**
     * 查询参数
     */
    @Schema(title = "查询参数")
    protected T query;

    /**
     * 带分页参数和查询条件的构造函数
     *
     * @param size  每页显示数量
     * @param num   当前页码
     * @param query 查询条件对象
     */
    public PageQuery(Number size, Number num, T query) {
        super(size, num);  // 调用父类Slice的构造函数初始化分页参数
        this.query = query;  // 设置当前查询条件
    }

    /**
     * 仅包含查询条件的构造函数
     *
     * @param query 查询条件对象，将使用默认分页参数(DEFAULT_PAGE_SIZE和DEFAULT_PAGE_NUM)
     */
    public PageQuery(T query) {
        this.query = query;
    }

    /**
     * 仅包含分页参数的构造函数
     *
     * @param size 每页显示数量
     * @param num  当前页码
     */
    public PageQuery(Number size, Number num) {
        super(size, num);
    }

    /**
     * 默认构造函数
     * <p>创建一个不带查询条件和分页参数的分页查询对象</p>
     * <p>分页参数将使用父类Slice中定义的默认值(DEFAULT_PAGE_SIZE和DEFAULT_PAGE_NUM)</p>
     */
    public PageQuery() {
    }

    /**
     * 获取查询参数
     *
     * @return 查询参数
     */
    @JsonIgnore
    public Optional<T> query() {
        return Optional.ofNullable(this.query);
    }

    /**
     * 创建一个带有查询条件和分页参数的分页查询对象
     *
     * @param <T>   查询参数的类型
     * @param query 查询条件对象
     * @param size  每页显示数量
     * @param num   当前页码
     * @return 包含指定查询条件和分页参数的分页查询对象
     */
    public static <T> PageQuery<T> of(T query, Number size, Number num) {
        return new PageQuery<>(size, num, query);
    }

    /**
     * 创建一个带有查询条件和默认分页参数的分页查询对象
     * <p>使用父类Slice中定义的默认分页参数(DEFAULT_PAGE_SIZE和DEFAULT_PAGE_NUM)</p>
     *
     * @param <T> 查询参数的类型
     * @param query 查询条件对象
     * @return 包含指定查询条件和默认分页参数的分页查询对象
     */
    public static <T> PageQuery<T> of(T query) {
        return new PageQuery<>(DEFAULT_PAGE_SIZE, DEFAULT_PAGE_NUM, query);
    }
}
