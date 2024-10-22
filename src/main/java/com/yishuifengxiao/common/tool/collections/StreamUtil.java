package com.yishuifengxiao.common.tool.collections;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>数据流工具</p>
 * <p>
 * <strong>该工具是一个线程安全类的工具</strong>
 * </p>
 *
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
public class StreamUtil {

    /**
     * <p>从数据流获取一个符合条件的非空数据</p>
     * <p><strong>注意：会排除掉null数据</strong></p>
     *
     * @param stream    输入的数据流
     * @param predicate 判断条件,不能为空null
     * @param <T>       数据类型
     * @return 任意一个满足条件的非空数据
     */
    public synchronized static <T> T findAny(Stream<T> stream, Predicate<? super T> predicate) {
        if (null == stream) {
            return null;
        }
        return stream.filter(Objects::nonNull).filter(predicate).findAny().orElse(null);
    }

    /**
     * <p>从List获取一个符合条件的非空数据</p>
     * <p><strong>注意：会排除掉null数据</strong></p>
     *
     * @param list      输入的数据集合
     * @param predicate 判断条件,不能为空null
     * @param <T>       数据类型
     * @return 任意一个满足条件的非空数据
     */
    public synchronized static <T> T findAny(List<T> list, Predicate<? super T> predicate) {
        if (null == list) {
            return null;
        }
        return list.parallelStream().filter(Objects::nonNull).filter(predicate).findAny().orElse(null);
    }

    /**
     * <p>从Set获取一个符合条件的非空数据</p>
     * <p><strong>注意：会排除掉null数据</strong></p>
     *
     * @param set       输入的数据集合
     * @param predicate 判断条件,不能为空null
     * @param <T>       数据类型
     * @return 任意一个满足条件的非空数据
     */
    public synchronized static <T> T findAny(Set<T> set, Predicate<? super T> predicate) {
        if (null == set) {
            return null;
        }
        return set.parallelStream().filter(Objects::nonNull).filter(predicate).findAny().orElse(null);
    }

    /**
     * 判断集合中是否存在符合指定条件的元素
     *
     * @param list      待匹配的集合
     * @param predicate 集合条件
     * @param <T>       数据类型
     * @return 存在返回为true，否则为false
     */
    public synchronized static <T> boolean contain(List<T> list, Predicate<? super T> predicate) {
        return null != findAny(list, predicate);
    }

    /**
     * 判断集合中是否存在符合指定条件的元素
     *
     * @param set       待匹配的集合
     * @param predicate 集合条件
     * @param <T>       数据类型
     * @return 存在返回为true，否则为false
     */
    public synchronized static <T> boolean contain(Set<T> set, Predicate<? super T> predicate) {
        return null != findAny(set, predicate);
    }

    /**
     * <p>在集合中根据条件删除一个元素</p>
     * <p><strong>注意：会排除掉null数据</strong></p>
     *
     * @param list      原始集合
     * @param predicate 删除条件,不能为空null
     * @param <T>       数据类型
     * @return 删除元素之后的集合
     */
    public synchronized static <T> List<T> remove(List<T> list, Predicate<? super T> predicate) {
        if (null == list) {
            list = new ArrayList<>();
        }
        List<T> collect =
                list.parallelStream().filter(Objects::nonNull).filter(predicate.negate()).collect(Collectors.toList());
        return collect;
    }


    /**
     * <p> 在集合中根据条件删除一个元素</p>
     * <p><strong>注意：会排除掉null数据</strong></p>
     *
     * @param set       原始集合
     * @param predicate 删除条件,不能为空null
     * @param <T>       数据类型
     * @return 删除元素之后的集合
     */
    public synchronized static <T> Set<T> remove(Set<T> set, Predicate<? super T> predicate) {
        if (null == set) {
            set = new HashSet<>();
        }
        Set<T> collect =
                set.parallelStream().filter(Objects::nonNull).filter(predicate.negate()).collect(Collectors.toSet());
        return collect;
    }


    /**
     * <p>在集合中替换并添加一个元素</p>
     *
     * @param list      原始集合
     * @param t         待添加的元素
     * @param predicate 替换条件,不能为空null
     * @param <T>       数据类型
     * @return 替换元素之后的集合
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public synchronized static <T> List<T> replace(List<T> list, T t, Predicate<? super T> predicate) {
        if (null == list) {
            list = new ArrayList<>();
        }
        List collect = remove(list, predicate);
        collect.add(t);
        return collect;
    }

    /**
     * <p>在集合中替换并添加一个元素</p>
     *
     * @param set       原始集合
     * @param t         待添加的元素
     * @param predicate 替换条件,不能为空null
     * @param <T>       数据类型
     * @return 替换元素之后的集合
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public synchronized static <T> Set<T> replace(Set<T> set, T t, Predicate<? super T> predicate) {
        if (null == set) {
            set = new HashSet<>();
        }
        Set collect = remove(set, predicate);
        collect.add(t);
        return collect;
    }

    /**
     * <p> 先判断集合中是否存在此元素，若不存在则添加，存在该元素则跳过</p>
     *
     * @param list      原始集合
     * @param t         待添加的元素
     * @param predicate 存在条件判断,不能为空null
     * @param <T>       数据类型
     * @return 处理之后的元素集合
     */
    public synchronized static <T> List<T> addIfNoPresent(List<T> list, T t, Predicate<? super T> predicate) {
        if (null == list) {
            list = new ArrayList<>();
        }
        Optional<T> optional = list.parallelStream().filter(Objects::nonNull).filter(predicate).findAny();
        if (!optional.isPresent()) {
            list.add(t);
        }
        return list;
    }

}
