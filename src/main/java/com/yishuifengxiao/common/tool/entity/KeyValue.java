package com.yishuifengxiao.common.tool.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;

/**
 * 键值对数据
 *
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
public class KeyValue<K, V> implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 6141567080605609052L;
	/**
     * 数据的键
     */
    private K key;
    /**
     * 数据的值
     */
    private V val;

    /**
     * 全参构造函数
     *
     * @param key 键值对的key
     * @param val 键值对的值
     */
    public KeyValue(K key, V val) {
        this.key = key;
        this.val = val;
    }

    /**
     * 无参构造函数
     */
    public KeyValue() {
    }

    /**
     * 获取键值对数据的key
     *
     * @return 键值对数据的key
     */
    public K getKey() {
        return key;
    }

    /**
     * 设置键值对数据的key
     *
     * @param key 键值对数据的key
     * @return 当前实例
     */
    public KeyValue<K, V> setKey(K key) {
        this.key = key;
        return this;
    }

    /**
     * 获取键值对数据的值
     *
     * @return 键值对数据的值
     */
    public V getVal() {
        return val;
    }

    /**
     * 设置键值对数据的值
     *
     * @param val 键值对数据的值
     * @return 当前实例
     */
    public KeyValue<K, V> setVal(V val) {
        this.val = val;
        return this;
    }

    /**
     * 判断该实例的键和值均不为null
     *
     * @return 若该实例的键和值均不为null即返回为true, 否则为false
     */
    @JsonIgnore
    public boolean notNull() {
        return null != this.key && null != this.val;
    }

    /**
     * 判断该实例的键和值是否均为null
     *
     * @return 若该该实例的键和值是否均为null即返回为true, 否则为false
     */
    @JsonIgnore
    public boolean isNull() {
        return null == this.key && null == this.val;
    }

    /**
     * 判断该实例的键是否为null
     *
     * @return 若该实例的键为null即返回为true, 否则为false
     */
    @JsonIgnore
    public boolean nullKey() {
        return null == this.key;
    }

    /**
     * 判断该实例的值是否为null
     *
     * @return 若该实例的值为null即返回为true, 否则为false
     */
    @JsonIgnore
    public boolean nullVal() {
        return null == this.val;
    }
}
