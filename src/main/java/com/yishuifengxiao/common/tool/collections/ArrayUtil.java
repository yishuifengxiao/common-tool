package com.yishuifengxiao.common.tool.collections;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

/**
 * <p>集合比较工具</p>
 * <p>主要功能如下：</p>
 * <ul>
 *     <li>获取两个集合之间的交集</li>
 *     <li>获取两个集合之间的并集</li>
 *     <li>获取两个集合之间的差集</li>
 * </ul>
 *
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
public class ArrayUtil {

    /**
     * 获取两个集合的交集
     *
     * @param src       源集合
     * @param target    目标集合
     * @param predicate 元素匹配条件
     * @param <T>       元素类型
     * @return 两个集合的交集
     */
    public static <T> Collection<T> intersection(Collection<T> src, Collection<T> target, BiPredicate<? super T, ?
            super T> predicate) {

        if (null == src) {
            return new HashSet<>();
        }
        if (null == target) {
            return src;
        }

        return src.stream().filter(Objects::nonNull).filter(v -> target.stream().anyMatch(s -> predicate.test(v, s))).collect(Collectors.toSet());

    }

    /**
     * 获取两个集合的并集
     *
     * @param src       源集合
     * @param target    目标集合
     * @param predicate 元素匹配条件
     * @param <T>       元素类型
     * @return 两个集合的并集
     */
    public static <T> Collection<T> union(Collection<T> src, Collection<T> target,
                                          BiPredicate<? super T, ? super T> predicate) {
        Set<T> sources = null == src ? new HashSet<>() :
                src.stream().filter(Objects::nonNull).collect(Collectors.toSet());
        Collection<T> difference = difference(sources, target, predicate);
        sources.addAll(difference);
        return sources;
    }

    /**
     * 获取两个集合的差集
     *
     * @param src       源集合
     * @param target    目标集合
     * @param predicate 元素匹配条件
     * @param <T>       元素类型
     * @return 两个集合的差集
     */
    public static <T> Collection<T> difference(Collection<T> src, Collection<T> target, BiPredicate<? super T, ?
            super T> predicate) {
        if (null == src) {
            return null == target ? new HashSet<>() : target;
        }
        if (null == target) {
            return new HashSet<>();
        }
        // 差集
        Collection<T> source = src.stream().filter(Objects::nonNull).collect(Collectors.toSet());
        return target.stream().filter(Objects::nonNull).filter(v -> !source.stream().anyMatch(s -> predicate.test(s,
                v))).collect(Collectors.toSet());
    }

    /**
     * 获取两个集合的交集
     *
     * @param src       源集合
     * @param target    目标集合
     * @param predicate 元素匹配条件
     * @param <T>       元素类型
     * @return 两个集合的交集
     */
    public static <T> Collection<T> intersection(T[] src, T[] target, BiPredicate<? super T, ? super T> predicate) {


        return intersection(null == src ? new HashSet<>() : Arrays.asList(src), null == target ? new HashSet<>() :
                Arrays.asList(target), predicate);

    }

    /**
     * 获取两个集合的并集
     *
     * @param src       源集合
     * @param target    目标集合
     * @param predicate 元素匹配条件
     * @param <T>       元素类型
     * @return 两个集合的并集
     */
    public static <T> Collection<T> union(T[] src, T[] target, BiPredicate<? super T, ? super T> predicate) {
        return union(null == src ? new HashSet<>() : Arrays.asList(src), null == target ? new HashSet<>() :
                Arrays.asList(target), predicate);
    }

    /**
     * 获取两个集合的差集
     *
     * @param src       源集合
     * @param target    目标集合
     * @param predicate 元素匹配条件
     * @param <T>       元素类型
     * @return 两个集合的差集
     */
    public static <T> Collection<T> difference(T[] src, T[] target, BiPredicate<? super T, ? super T> predicate) {
        return difference(null == src ? new HashSet<>() : Arrays.asList(src), null == target ? new HashSet<>() :
                Arrays.asList(target), predicate);
    }

}
