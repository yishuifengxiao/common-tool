package com.yishuifengxiao.common.tool.jdbc;

import com.yishuifengxiao.common.tool.entity.RootEnum;

/**
 * JDBC 错误枚举
 *
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
public enum JdbcError implements RootEnum {
    NO_PRIMARY_KEY(6001, "无数据库主键"),
    NULL_PRIMARY_KEY(6002, "数据库主键为空"),
    DELETE_PARAMS_IS_ALL_NULL(6003, "删除条件为空"),
    PARAMS_IS_ALL_NULL(6004, "参数不能全为null"),
    MULTIPLE_PRIMARY_KEYS(6005, "多个主键属性"),
    SQL_IS_NULL(6006, "SQL语句不能为空"),
    SQL_HELPER_INIT_ERROR(6007, "SQL执行器初始化失败");

    private final int code;
    private final String message;

    JdbcError(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public Integer code() {
        return this.code;
    }
}