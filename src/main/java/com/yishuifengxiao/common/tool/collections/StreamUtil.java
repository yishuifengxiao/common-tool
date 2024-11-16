package com.yishuifengxiao.common.tool.collections;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * <p>数据流工具</p>
 *
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
public class StreamUtil {


    /**
     * <p>从List获取一个符合条件的非空数据</p>
     * <p><strong>注意：会排除掉null数据</strong></p>
     *
     * @param collection 输入的数据集合
     * @param predicate  判断条件,不能为空null
     * @param <T>        数据类型
     * @return 任意一个满足条件的非空数据
     */
    public static <T> Optional<T> findAny(Collection<T> collection, Predicate<?
            super T> predicate) {
        if (null == collection || null == predicate) {
            return Optional.empty();
        }
        return collection.parallelStream().filter(Objects::nonNull).filter(predicate).findFirst();
    }


    /**
     * 判断集合中是否存在符合指定条件的元素
     *
     * @param collection 待匹配的集合
     * @param predicate  集合条件
     * @param <T>        数据类型
     * @return 存在返回为true，否则为false
     */
    public static <T> boolean contain(Collection<T> collection,
                                      Predicate<? super T> predicate) {
        return collection.parallelStream().filter(Objects::nonNull).allMatch(predicate);
    }


    /**
     * <p>在集合中根据条件删除一个元素</p>
     * <p><strong>注意：会排除掉null数据</strong></p>
     *
     * @param collection 原始集合
     * @param predicate  删除条件,不能为空null
     * @param <T>        数据类型
     * @return 删除元素之后的集合
     */
    public static <T> List<T> remove(Collection<T> collection,
                                     Predicate<? super T> predicate) {
        if (null == collection) {
            collection = new ArrayList<>();
        }
        List<T> collect =
                collection.parallelStream().filter(Objects::nonNull).filter(predicate.negate()).collect(Collectors.toList());
        return collect;
    }


    /**
     * <p> 先判断集合中是否存在此元素，若不存在则添加，存在该元素则跳过</p>
     *
     * @param collection 原始集合
     * @param t          待添加的元素
     * @param predicate  存在条件判断,不能为空null
     * @param <T>        数据类型
     * @return 处理之后的元素集合
     */
    public static <T> List<T> addIfNoPresent(Collection<T> collection, T t,
                                             Predicate<? super T> predicate) {
        if (null == collection) {
            return new ArrayList<>();
        }
        if (!contain(collection, predicate)) {
            collection.add(t);
        }
        return collection.stream().collect(Collectors.toList());
    }

}
