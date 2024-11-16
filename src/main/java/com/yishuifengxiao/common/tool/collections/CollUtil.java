/**
 *
 */
package com.yishuifengxiao.common.tool.collections;

import com.yishuifengxiao.common.tool.entity.Page;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>
 * 集合增强工具类
 * </p>
 * <p>
 * 该工具的主要目标是快速地判断集合是否为空，其具备以下的几项功能 </p>
 * <ol>
 * <li>判断集合是否为空或者空元素的集合</li>
 * <li>判断集合是否仅有一个元素</li>
 * <li>将集合转换成java8中的串行流或并行流</li>
 * <li>将数组转换成List或Set，而从避免Arrays.asList()转换后存在的问题</li>
 * <li>安全地获取集合或数组里的第一个元素</li>
 * <li>安全地获取集合或数组里的元素的数量</li>
 * </ol>
 *
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
public final class CollUtil {
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
     * 将数据安全地转换成串行流Stream
     *
     * @param <T>  集合里数据的类型
     * @param data 需要转换的集合数据
     * @return 串行流Stream
     */
    public static <T> Stream<T> stream(T[] data) {
        if (null == data) {
            return new ArrayList<T>().stream();
        }
        return Arrays.stream(data);
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
        return Arrays.asList(objs).stream().collect(Collectors.toList());
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
        return Arrays.asList(objs).stream().collect(Collectors.toSet());
    }

    /**
     * 取出数组的第一个元素, 若数组为空则返回null
     *
     * @param <T>  数组里数据的类型
     * @param data 数据源
     * @return 数组的第一个元素
     */
    public static <T> Optional<T> first(T[] data) {
        if (data == null || data.length == 0) {
            return Optional.empty();
        }
        return Optional.ofNullable(data[0]);
    }


    /**
     * 取出集合里的第一个元素，若集合为空则返回为null
     *
     * @param <T>  集合里元素的类型
     * @param data 数据源
     * @return 集合里的第一个元素
     */
    public static <T> Optional<T> first(Collection<T> data) {
        if (data == null || data.size() == 0) {
            return Optional.empty();
        }
        Iterator<T> it = data.iterator();
        return Optional.ofNullable(it.next());
    }


    /**
     * 取出List中的最后一个非空元素,如果Stream为空则返回null
     *
     * @param <T>  List里的数据的类型
     * @param data 链表
     * @return List中的第一个非空元素
     */
    public static <T> Optional<T> last(List<T> data) {
        if (data == null || data.size() == 0) {
            return null;
        }
        return Optional.ofNullable(data.get(data.size() - 1));
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
     * 将不定参数转为数组
     *
     * @param vals 不定参数
     * @param <T>  参数类型
     * @return 数组
     */
    public static <T> T[] asArray(T... vals) {
        return vals;
    }

    /**
     * 将多个链表合并成一个链表
     *
     * @param list 待合并的链表
     * @param <T>  数据类型
     * @return 合并后的链表
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> merge(Collection<T>... list) {
        if (null == list) {
            return Collections.emptyList();
        }
        List<T> result = new ArrayList<>();
        for (Collection<T> data : list) {
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
     * 获取链表里的第index个元素
     *
     * @param data  链表
     * @param index 序号
     * @param <T>   数据类型
     * @return 链表为空或index超过容量时返回为null
     */
    public static <T> Optional<T> get(List<T> data, int index) {
        if (null == data || data.size() <= index) {
            return Optional.empty();
        }
        return Optional.ofNullable(data.get(index));
    }

    /**
     * 获取数组里的第index个元素
     *
     * @param data  链表
     * @param index 序号
     * @param <T>   数据类型
     * @return 数组为空或index超过容量时返回为null
     */
    public static <T> Optional<T> get(T[] data, int index) {
        if (null == data || data.length <= index) {
            return Optional.empty();
        }
        return Optional.ofNullable(data[index]);
    }

    /**
     * 遍历一个集合
     *
     * @param collection 待遍历的集合
     * @param consumer   消费者，第一个参数为元素的序号，第二个参数为被遍历到的元素
     * @param <T>        数据类型
     */
    public static <T> void forEach(Collection<T> collection, BiConsumer<Long, T> consumer) {
        if (null == collection || collection.isEmpty()) {
            return;
        }
        AtomicLong atomic = new AtomicLong(0L);
        collection.stream().forEach(v -> consumer.accept(atomic.getAndIncrement(), v));
    }

    /**
     * 判断分页对象是否为空
     *
     * @param <T>  分页对象里元素的数据类型
     * @param page 分页对象
     * @return 如果是空返回为true，否则为false
     */
    public static <T> boolean isEmpty(Page<T> page) {

        if (page == null) {
            return true;
        }
        if (page.getData() == null || page.getData().size() == 0) {
            return true;
        }
        return false;

    }

    /**
     * 判断分页对象是否不为空
     *
     * @param <T>  分页对象里元素的数据类型
     * @param page 分页对象
     * @return 如果不是空返回为true，否则为false
     */
    public static <T> boolean isNotEmpty(Page<T> page) {
        return !isEmpty(page);
    }


    /**
     * 判断数组是否为空
     *
     * @param <T>  数组里元素的类型
     * @param data 数据源
     * @return 如果为空则返回true，否则为false
     */
    public static <T> boolean isEmpty(T[] data) {
        return data == null || data.length == 0;
    }

    /**
     * 判断数组是否不为空
     *
     * @param <T>  数组里元素的类型
     * @param data 数据源
     * @return 如果不为空则返回true，否则为false
     */
    public static <T> boolean isNotEmpty(T[] data) {
        return !isEmpty(data);
    }

    /**
     * 判断Map是否为空
     *
     * @param map 数据源
     * @return 如果为空则返回true，否则为false
     */
    public static boolean isEmpty(Map map) {
        return map == null || map.isEmpty();
    }

    /**
     * 判断Map是否不为空
     *
     * @param map 数据源
     * @return 如果不为空则返回true，否则为false
     */
    public static boolean isNotEmpty(Map map) {
        return null != map && !map.isEmpty();
    }

    /**
     * 判断集合是否为空
     *
     * @param <T>  集合里元素的类型
     * @param data 数据源
     * @return 如果为空则返回true，否则为false
     */
    public static <T> boolean isEmpty(Collection<T> data) {
        if (data == null) {
            return true;
        }
        if (data.size() == 0) {
            return true;
        }
        return false;
    }

    /**
     * 是否全部为空集合或null
     *
     * @param collections 待判断的集合
     * @return 若全部为空集合或者null则返回为true, 否则为false
     */
    @SuppressWarnings({"rawtypes"})
    public static boolean isAllEmpty(Collection... collections) {
        if (null == collections) {
            return true;
        }
        return Arrays.stream(collections).allMatch(CollUtil::isEmpty);
    }

    /**
     * 是否有一个集合为空或null
     *
     * @param collections 待判断的集合
     * @return 只要有一个集合为空或者null则返回为true, 否则为false
     */
    @SuppressWarnings({"rawtypes"})
    public static boolean isAnyEmpty(Collection... collections) {
        if (null == collections) {
            return false;
        }
        return Arrays.stream(collections).anyMatch(CollUtil::isEmpty);
    }

    /**
     * 是否所有的集合均不为空或者null
     *
     * @param collections 待判断的集合
     * @return 只要有一个集合为空或者null则返回为true, 否则为false
     */
    @SuppressWarnings({"rawtypes"})
    public static boolean isNoneEmpty(Collection... collections) {
        if (null == collections) {
            return false;
        }
        return !isAnyEmpty(collections);
    }

    /**
     * 判断集合是否不为空
     *
     * @param <T>  集合里元素的类型
     * @param data 数据源
     * @return 如果不为空则返回true，否则为false
     */
    public static <T> boolean isNotEmpty(Collection<T> data) {
        return !isEmpty(data);
    }

    /**
     * 判断 集合是否只有一个元素
     *
     * @param <T>  集合里元素的类型
     * @param data 数据源
     * @return 如果只有一个元素返回为true，否则为false
     */
    public static <T> boolean isOnlyOneElement(Collection<T> data) {
        if (null == data) {
            return false;
        }
        return data.size() == 1;
    }

    /**
     * 判断集合里的元素的数量是否大于或等于一个
     *
     * @param <T>  集合里元素的类型
     * @param data 数据源
     * @return 如果元素的数量是否大于或等于一个返回为true，否则为false
     */
    public static <T> boolean gteOneElement(Collection<T> data) {
        if (null == data) {
            return false;
        }
        return data.size() >= 1;
    }

    /**
     * 判断集合里元素数量是否大于一个
     *
     * @param <T>  集合里元素的类型
     * @param data 数据源
     * @return 若集合里元素数量大于1则返回为true, 否则为false
     */
    public static <T> boolean gtOneElement(Collection<T> data) {
        if (null == data) {
            return false;
        }
        return data.size() > 1;
    }

    /**
     * <p>
     * 判断集合里元素数量是否小于一个
     * </p>
     * <p>
     * <strong>注意：若集合为null则依旧返回为true</strong>
     * </p>
     *
     * @param <T>  集合里元素的类型
     * @param data 数据源
     * @return 若集合里元素数量小于1则返回为true, 否则为false
     */
    public static <T> boolean ltOneElement(Collection<T> data) {
        if (null == data) {
            return true;
        }
        return data.size() < 1;
    }

    /**
     * <p>
     * 判断集合里元素数量是否小于或等于一个
     * </p>
     * <p>
     * <strong>注意：若集合为null则依旧返回为true</strong>
     * </p>
     *
     * @param <T>  集合里元素的类型
     * @param data 数据源
     * @return 若集合里元素数量小于或等于一个则返回为true, 否则为false
     */
    public static <T> boolean lteOneElement(Collection<T> data) {
        if (null == data) {
            return true;
        }
        return data.size() <= 1;
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
}
