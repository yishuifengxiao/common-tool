/**
 *
 */
package com.yishuifengxiao.common.tool.collections;

import com.yishuifengxiao.common.tool.exception.UncheckedException;
import com.yishuifengxiao.common.tool.exception.constant.ErrorCode;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>
 * 集合元素处理工具
 * </p>
 *
 * 该工具的主要目标是在不发生NPE的前提下对集合以及集合里的元素进行操作，其具备以下的几项功能
 * <ol>
 * <li>将集合转换成java8中的串行流或并行流</li>
 * <li>将数组转换成List或Set，而从避免Arrays.asList()转换后存在的问题</li>
 * <li>安全地获取集合里的第一个元素</li>
 * </ol>
 *
 * <p>
 * <strong>该工具是一个非线程安全类的工具</strong>
 * </p>
 *
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
public final class DataUtil {

    /**
     * 将数据安全地转换成串行流Stream
     *
     * @param <T>  集合里数据的类型
     * @param data 需要转换的集合数据
     * @return 串行流Stream
     */
    public static <T> Stream<T> stream(Collection<T> data) {
        if (null == data) {
            data = new ArrayList<>();
        }
        return data.stream();
    }

    /**
     * 将数据安全地转换成串行流Stream,并检查输入的数据，若输入的数据源为空就抛出异常
     *
     * @param <T>  集合里数据的类型
     * @param data 需要转换的集合数据
     * @param msg  异常提示信息
     * @return 串行流Stream
     */
    public static <T> Stream<T> stream(Collection<T> data, String msg) {
        if (null == data) {
            throw new UncheckedException(ErrorCode.DATA_ERROR, msg);
        }
        return data.stream();
    }

    /**
     * 将数据安全地转换成串行流Stream
     *
     * @param <T>  集合里数据的类型
     * @param data 需要转换的集合数据
     * @return 串行流Stream
     */
    public static <T> Stream<T> stream(T[] data) {
        return stream(toList(data));
    }

    /**
     * 将数据安全地转换成串行流Stream,并检查输入的数据，若输入的数据源为空就抛出异常
     *
     * @param <T>  集合里数据的类型
     * @param data 需要转换的集合数据
     * @param msg  异常提示信息
     * @return 串行流Stream
     */
    public static <T> Stream<T> stream(T[] data, String msg) {
        if (null == data) {
            throw new UncheckedException(ErrorCode.DATA_ERROR, msg);
        }
        return stream(toList(data));
    }

    /**
     * 将数据安全地转换成并行流Stream
     *
     * @param <T>  集合里数据的类型
     * @param data 需要转换的集合数据
     * @return 并行流Stream
     */
    public static <T> Stream<T> parallelStream(Collection<T> data) {
        if (null == data) {
            data = new ArrayList<>();
        }
        return data.parallelStream();
    }

    /**
     * 将数据安全地转换成并行流Stream,并检查输入的数据，若输入的数据源为空就抛出异常
     *
     * @param <T>  集合里数据的类型
     * @param data 需要转换的集合数据
     * @param msg  异常提示信息
     * @return 并行流Stream
     */
    public static <T> Stream<T> parallelStream(Collection<T> data, String msg) {
        if (null == data) {
            throw new UncheckedException(ErrorCode.DATA_ERROR, msg);
        }
        return data.parallelStream();
    }

    /**
     * 将数据安全地转换成并行流Stream
     *
     * @param <T>  集合里数据的类型
     * @param data 需要转换的集合数据
     * @return 并行流Stream
     */
    public static <T> Stream<T> parallelStream(T[] data) {
        return parallelStream(toList(data));
    }

    /**
     * 将数据安全地转换成并行流Stream,并检查输入的数据，若输入的数据源为空就抛出异常
     *
     * @param <T>  集合里数据的类型
     * @param data 需要转换的集合数据
     * @param msg  异常提示信息
     * @return 并行流Stream
     */
    public static <T> Stream<T> parallelStream(T[] data, String msg) {
        if (null == data) {
            throw new UncheckedException(ErrorCode.DATA_ERROR, msg);
        }
        return parallelStream(toList(data));
    }

    /**
     * 将数组转换成List
     *
     * @param <T>  数组里元素的类型
     * @param objs 需要转换的数据
     * @return 转换后的List数据
     */
    public static <T> List<T> toList(T[] objs) {
        if (null == objs) {
            return new ArrayList<>();
        }
        List<T> list = new ArrayList<>(objs.length);
        for (T t : objs) {
            list.add(t);
        }
        return list;
    }

    /**
     * 将数组转换成Set
     *
     * @param <T>  数组里元素的类型
     * @param objs 需要转换的数据
     * @return 转换后的Set数据
     */
    public static <T> Set<T> toSet(T[] objs) {
        if (null == objs) {
            return new HashSet<>();
        }
        Set<T> set = new HashSet<>(objs.length);
        for (T t : objs) {
            set.add(t);
        }
        return set;
    }

    /**
     * 取出Stream中的第一个非空元素,如果Stream为空则返回null
     *
     * @param <T>  数据流里的数据的类型
     * @param data 数据流
     * @return Stream中的第一个非空元素
     */
    public static <T> T first(Stream<T> data) {
        if (data == null) {
            return null;
        }

        return data.filter(Objects::nonNull).findFirst().orElse(null);
    }

    /**
     * 取出Stream中的第一个非空元素,如果Stream为空则返回 缺省值
     *
     * @param <T>          数据流里的数据的类型
     * @param data         数据流
     * @param defaultValue 缺省值
     * @return Stream中的第一个非空元素
     */
    public static <T> T first(Stream<T> data, T defaultValue) {
        if (data == null) {
            return null;
        }

        return data.filter(Objects::nonNull).findFirst().orElse(defaultValue);
    }

    /**
     * 取出数组的第一个元素, 若数组为空则返回null
     *
     * @param <T>  数组里数据的类型
     * @param data 数据源
     * @return 数组的第一个元素
     */
    public static <T> T first(T[] data) {
        if (data == null || data.length == 0) {
            return null;
        }
        return data[0];
    }

    /**
     * 取出数组的第一个元素, 若数组为空则返回给定的缺省值
     *
     * @param <T>          数组里的数据的类型
     * @param data         数据源
     * @param defaultValue 缺省值
     * @return 数组里的第一个元素
     */
    public static <T> T first(T[] data, T defaultValue) {
        T t = first(data);
        return null == t ? defaultValue : t;
    }

    /**
     * 取出数组的第一个元素，若数组为空则抛出异常
     *
     * @param <T>      数组里的数据的类型
     * @param data     数据源
     * @param errorMsg 异常提示信息
     * @return 数组的第一个元素
     */
    public static <T> T first(T[] data, String errorMsg) {
        T t = first(data);
        if (null == t) {
            throw new UncheckedException(ErrorCode.DATA_ERROR, errorMsg);
        }
        return t;
    }

    /**
     * 取出集合里的第一个元素，若集合为空则返回为null
     *
     * @param <T>  集合里元素的类型
     * @param data 数据源
     * @return 集合里的第一个元素
     */
    public static <T> T first(Collection<T> data) {
        if (data == null || data.size() == 0) {
            return null;
        }
        Iterator<T> it = data.iterator();
        return it.next();
    }

    /**
     * 取出集合的第一个元素, 若集合为空则返回给定的缺省值
     *
     * @param <T>          集合里的数据的类型
     * @param data         数据源
     * @param defaultValue 缺省值
     * @return 集合里的第一个元素
     */
    public static <T> T first(Collection<T> data, T defaultValue) {
        T t = first(data);
        return null == t ? defaultValue : t;
    }

    /**
     * 取出集合的第一个元素，若集合为空则抛出异常
     *
     * @param <T>      集合里的数据的类型
     * @param data     数据源
     * @param errorMsg 异常提示信息
     * @return 集合里的第一个元素
     */
    public static <T> T first(Collection<T> data, String errorMsg) {
        T t = first(data);
        if (null == t) {
            throw new UncheckedException(ErrorCode.DATA_ERROR, errorMsg);
        }
        return t;
    }

    /**
     * 取出List中的最后一个非空元素,如果Stream为空则返回null
     *
     * @param <T>  List里的数据的类型
     * @param data 链表
     * @return List中的第一个非空元素
     */
    public static <T> T last(List<T> data) {
        if (data == null || data.size() == 0) {
            return null;
        }
        return data.get(data.size() - 1);
    }

    /**
     * 取出Stream中的最后一个非空元素,如果Stream为空则返回null
     *
     * @param <T>  数据流里的数据的类型
     * @param data 数据流
     * @return Stream中的第一个非空元素
     */
    public static <T> T last(Stream<T> data) {
        if (data == null) {
            return null;
        }

        return last(data.filter(Objects::nonNull).collect(Collectors.toList()));
    }

    /**
     * 将指定的数据转换成list
     *
     * @param <T> 数据类型
     * @param a   需要转换的数据
     * @return 转换后的list
     */
    @SafeVarargs
    public static <T> List<T> asList(T... a) {
        if (null == a || a.length == 0) {
            return new ArrayList<>();
        }
        List<T> list = new ArrayList<>(a.length);
        for (int i = 0; i < a.length; i++) {
            list.add(a[i]);
        }
        return list;
    }

    /**
     * 将多个链表合并成一个链表
     * @param list 待合并的链表
     * @param <T> 数据类型
     * @return 合并后的链表
     */
    @SuppressWarnings("unchecked")
	public static <T> List<T> merge(List<T>... list) {
        if (null == list) {
            return Collections.emptyList();
        }
        List<T> result = new ArrayList<>();
        for (List<T> data : list) {
            if (null == data) {
                continue;
            }
            result.addAll(data);
        }
        return result;
    }

    /**
     * 将多个Set合并成一个Set
     * @param sets 待合并的Set
     * @param <T> 数据类型
     * @return 合并后的Set
     */
    @SuppressWarnings({ "unchecked" })
	public static <T> Set<T> merge(Set<T>... sets) {
        if (null == sets) {
            return Collections.emptySet();
        }
        Set<T> result = new HashSet<>();
        for (Set<T> data : sets) {
            if (null == data) {
                continue;
            }
            result.addAll(data);
        }
        return result;
    }


    /**
     * 将指定的数据转换成Set
     *
     * @param <T> 数据类型
     * @param a   需要转换的数据
     * @return 转换后的Set
     */
    @SafeVarargs
    public static <T> Set<T> asSet(T... a) {
        if (null == a || a.length == 0) {
            return new HashSet<>();
        }
        Set<T> list = new HashSet<>(a.length);
        for (int i = 0; i < a.length; i++) {
            list.add(a[i]);
        }
        return list;
    }

    /**
     * 获取集合里的元素的数量
     *
     * @param <T>  数据类型
     * @param data 待判断的集合
     * @return 集合里的元素的数量
     */
    public static <T> long size(Collection<T> data) {
        return null == data ? 0L : data.size();
    }

    /**
     * 获取数组里的元素的数量
     *
     * @param <T>  数据类型
     * @param data 待判断的数组
     * @return 数组里的元素的数量
     */
    public static <T> long size(T[] data) {
        return null == data ? 0L : data.length;
    }

    /**
     * 生成一个从0开始到指定数值的链表
     * @param end 截止数值(不包含该值)
     * @return 0开始到指定数值的链表
     */
    public static List<Integer> intList(int end) {
        return intList(0, end);
    }

    /**
     * 生成一个从开始数值到结束数值的链表
     * @param start 开始数值(包含该值)
     * @param end 截止数值(不包含该值)
     * @return 0开始到指定数值的链表
     */
    public static List<Integer> intList(int start, int end) {
        List<Integer> list = new ArrayList<>(end - start);
        for (int i = 0; i < end; i++) {
            list.add(i);
        }
        return list;
    }

    /**
     * 获取链表里的第index个元素
     * @param data 链表
     * @param index 序号
     * @param <T> 数据类型
     * @return 链表为空或index超过容量时返回为null
     */
    public static <T> T get(List<T> data, int index) {
        if (null == data || data.size() <= index) {
            return null;
        }
        return data.get(index);
    }

    /**
     * 获取数组里的第index个元素
     * @param data 链表
     * @param index 序号
     * @param <T> 数据类型
     * @return 数组为空或index超过容量时返回为null
     */
    public static <T> T get(T[] data, int index) {
        if (null == data || data.length <= index) {
            return null;
        }
        return data[index];
    }
}
