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
    public static <T> Collection<T> intersection(Collection<T> src, Collection<T> target, BiPredicate<? super T, ? super T> predicate) {

        if (null == src || null == target) {
            return new HashSet<>();
        }

        if (predicate == null) {
            throw new IllegalArgumentException("predicate cannot be null");
        }

        if (src.isEmpty() || target.isEmpty()) {
            return new HashSet<>();
        }

        Set<T> targetSet = target.stream().filter(Objects::nonNull).collect(Collectors.toSet());

        if (targetSet.isEmpty()) {
            return new HashSet<>();
        }

        return src.stream().filter(Objects::nonNull).filter(v -> targetSet.stream().anyMatch(s -> predicate.test(v, s))).collect(Collectors.toSet());
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
    public static <T> Collection<T> union(Collection<T> src, Collection<T> target, BiPredicate<? super T, ? super T> predicate) {
        if (predicate == null) {
            throw new IllegalArgumentException("predicate cannot be null");
        }

        Set<T> sources = new HashSet<>();
        if (src != null) {
            sources.addAll(src);
        }
        if (target != null) {
            sources.addAll(target);
        }
        return sources;
    }


    /**
     * 获取两个集合的差集
     *
     * @param src       源集合
     * @param target    目标集合
     * @param predicate 元素匹配条件
     * @param <T>       元素类型
     * @return target中不存在于src中的元素集合
     */
    public static <T> Collection<T> difference(Collection<T> src, Collection<T> target, BiPredicate<? super T, ? super T> predicate) {
        if (null == src) {
            return null == target ? new HashSet<>() : target;
        }
        if (null == target) {
            return new HashSet<>();
        }

        // 预先过滤并收集source集合中的非空元素
        Set<T> sourceSet = src.stream().filter(Objects::nonNull).collect(Collectors.toSet());

        // 如果source为空，则直接返回target中非空元素
        if (sourceSet.isEmpty()) {
            return target.stream().filter(Objects::nonNull).collect(Collectors.toSet());
        }

        // 将source转换为列表以支持索引访问，提高匹配效率
        List<T> sourceList = new ArrayList<>(sourceSet);

        return target.stream().filter(Objects::nonNull).filter(v -> {
            for (T s : sourceList) {
                if (predicate.test(s, v)) {
                    return false;
                }
            }
            return true;
        }).collect(Collectors.toSet());
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
        if (predicate == null) {
            throw new IllegalArgumentException("predicate cannot be null");
        }

        List<T> srcList = (src == null) ? Collections.emptyList() : Arrays.asList(src);
        List<T> targetList = (target == null) ? Collections.emptyList() : Arrays.asList(target);

        return intersection(srcList, targetList, predicate);
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
        if (predicate == null) {
            throw new IllegalArgumentException("Predicate cannot be null");
        }

        List<T> srcList = src == null ? Collections.emptyList() : Arrays.asList(src);
        List<T> targetList = target == null ? Collections.emptyList() : Arrays.asList(target);

        return union(srcList, targetList, predicate);
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
        List<T> srcList = (src == null) ? Collections.emptyList() : Arrays.asList(src);
        List<T> targetList = (target == null) ? Collections.emptyList() : Arrays.asList(target);
        return difference(srcList, targetList, predicate);
    }


}
