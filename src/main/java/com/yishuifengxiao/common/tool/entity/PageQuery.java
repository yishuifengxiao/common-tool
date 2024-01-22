package com.yishuifengxiao.common.tool.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Optional;

/**
 * 基础分页查询参数
 *
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class PageQuery<T> extends Slice implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;


    protected T query;


    /**
     * 获取查询参数
     *
     * @return 查询参数
     */
    @JsonIgnore
    public Optional<T> query() {
        return Optional.ofNullable(this.query);
    }

}
