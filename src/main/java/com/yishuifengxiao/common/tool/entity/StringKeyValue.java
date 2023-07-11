package com.yishuifengxiao.common.tool.entity;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

/**
 * 键为字符串类型的键值对数据
 *
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
@SuppressWarnings("rawtypes")
public class StringKeyValue<T> extends KeyValue implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -5795495851726526435L;

    /**
     * 全参构造函数
     *
     * @param key 键值对的key
     * @param val 键值对的值
     */
    @SuppressWarnings("unchecked")
    public StringKeyValue(String key, T val) {
        super(key, val);
    }

    /**
     * 无参构造函数
     */
    public StringKeyValue() {
    }

    @Override
    public String getKey() {
        return null != super.getKey() ? super.getKey().toString() : null;
    }

    /**
     * 设置键值对的键
     *
     * @param key 键值对的键
     * @return 当前对象实例
     */
    @SuppressWarnings("unchecked")
    public StringKeyValue<T> setKey(String key) {
        super.setKey(key);
        return this;
    }

    /**
     * 判断该实例的键是否为null或空字符串
     *
     * @return 若该实例的键为null或空字符串即返回为true, 否则为false
     */
    public boolean blankKey() {
        return StringUtils.isBlank(getKey());
    }

    /**
     * 判断该实例的键是否不为null或空字符串且值不为null
     *
     * @return 该实例的键是否不为null或空字符串且值不为null即返回为true, 否则为false
     */
    public boolean notBlank() {
        return !blankKey() && !nullVal();
    }

    /**
     * 判断该实例的键是否为null或空字符串且值为null
     *
     * @return 该实例的键是否为null或空字符串且值为null即返回为true, 否则为false
     */
    public boolean isBlank() {
        return blankKey() && nullVal();
    }

}
