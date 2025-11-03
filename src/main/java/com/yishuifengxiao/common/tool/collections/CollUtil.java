/**
 *
 */
package com.yishuifengxiao.common.tool.collections;

import com.yishuifengxiao.common.tool.entity.Page;

import java.util.*;
import java.util.function.BiConsumer;
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
     * 将可变参数转换为Map对象
     * 参数必须成对出现，每对参数中第一个作为key，第二个作为value
     *
     * @param vals 可变参数列表，必须为偶数个，格式为[key1, value1, key2, value2, ...]
     * @return 包含所有键值对的HashMap对象
     * @throws IllegalArgumentException 当参数个数为奇数时抛出异常
     */
    public static Map toMap(Object... vals) {
        if (null == vals || vals.length == 0) {
            return new HashMap<>();
        }
        if (vals.length % 2 != 0) {
            throw new IllegalArgumentException("vals must be an even number, but got length: " + vals.length);
        }
        // 计算初始容量，避免频繁扩容
        int capacity = (int) Math.ceil(vals.length / 2.0 * 1.5);
        Map<Object, Object> map = new LinkedHashMap<>(capacity);
        int len = vals.length;
        // 遍历参数数组，每隔两个元素组成一个键值对
        for (int i = 0; i < len; i += 2) {
            map.put(vals[i], vals[i + 1]);
        }
        return map;
    }


    /**
     * 创建一个安全的Stream，如果输入为null则返回空Stream
     *
     * @param data 输入的Stream数据
     * @param <T>  Stream元素类型
     * @return 如果data不为null，返回原Stream；否则返回空Stream
     */
    public static <T> Stream<T> stream(Stream<T> data) {
        if (null == data) {
            return Stream.empty();
        }
        return data;
    }


    /**
     * 将数据安全地转换成串行流Stream
     *
     * @param <T>  集合里数据的类型
     * @param data 需要转换的集合数据
     * @return 串行流Stream
     */
    public static <T> Stream<T> stream(Collection<T> data) {
        if (null == data) {
            return Stream.empty();
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
            return Stream.empty();
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
        return new ArrayList<>(Arrays.asList(objs));
    }


        /**
     * 将集合转换为数组
     *
     * @param vals 要转换的集合，可以为null
     * @return 转换后的数组，如果输入为null则返回空数组
     */
    public static <T> T[] toArray(Collection<T> vals) {
        // 处理null输入情况，返回空数组
        if (null == vals) {
            return (T[]) new Object[0];
        }
        // 使用集合的toArray方法转换为数组
        return vals.toArray((T[]) new Object[0]);
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
        return new HashSet<>(Arrays.asList(objs));
    }


    /**
     * 取出数组的第一个元素, 若数组为空则返回null
     *
     * @param <T>  数组里数据的类型
     * @param data 数据源
     * @return 数组的第一个元素
     */
    public static <T> Optional<T> first(T[] data) {
        // 检查数组是否为空或长度为0
        if (data == null || data.length == 0) {
            return Optional.empty();
        }
        // 返回数组第一个元素的Optional包装
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
        if (data == null || data.isEmpty()) {
            return Optional.empty();
        }
        Iterator<T> it = data.iterator();
        return Optional.ofNullable(it.next());
    }


    /**
     * 取出List中的最后一个元素,如果List为空则返回empty Optional
     *
     * @param <T>  List里的数据的类型
     * @param data 链表
     * @return List中的最后一个元素的Optional包装
     */
    public static <T> Optional<T> last(List<T> data) {
        if (data == null || data.isEmpty()) {
            return Optional.empty();
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
    public static <T> List<T> asList(T... a) {
        if (null == a || a.length == 0) {
            return new ArrayList<>();
        }
        return new ArrayList<>(Arrays.asList(a));
    }


    /**
     * 将不定参数转为数组
     *
     * @param vals 不定参数，不能为null
     * @param <T>  参数类型
     * @return 数组，返回的数组是传入参数的直接引用，修改数组会影响原始参数
     * @throws IllegalArgumentException if vals is null
     */
    public static <T> T[] asArray(T... vals) {
        if (vals == null) {
            throw new IllegalArgumentException("参数vals不能为null");
        }
        return vals;
    }


    /**
     * 将多个链表合并成一个链表
     *
     * @param collections 待合并的链表
     * @param <T>         数据类型
     * @return 合并后的链表
     */
    public static <T> List<T> merge(Collection<T>... collections) {
        if (null == collections) {
            return Collections.emptyList();
        }

        // 预估初始容量以减少扩容
        int initialCapacity = 0;
        for (Collection<T> collection : collections) {
            if (collection != null) {
                initialCapacity += collection.size();
            }
        }

        List<T> result = new ArrayList<>(initialCapacity > 0 ? initialCapacity : 10);
        for (Collection<T> collection : collections) {
            if (null == collection) {
                continue;
            }
            result.addAll(collection);
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
        return new HashSet<>(Arrays.asList(a));
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
        if (null == data || index < 0 || index >= data.size()) {
            return Optional.empty();
        }
        return Optional.of(data.get(index));
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
        if (null == data || index < 0 || data.length <= index) {
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
        long index = 0L;
        for (T item : collection) {
            consumer.accept(index++, item);
        }
    }


    /**
     * 判断分页对象是否为空
     *
     * @param <T>  分页对象里元素的数据类型
     * @param page 分页对象
     * @return 如果是空返回为true，否则为false
     */
    public static <T> boolean isEmpty(Page<T> page) {
        return page == null || page.getData() == null || page.getData().isEmpty();
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
    public static boolean isAnyEmpty(Collection... collections) {
        if (null == collections) {
            return true;
        }
        return Arrays.stream(collections).anyMatch(CollUtil::isEmpty);
    }


    /**
     * 是否所有的集合均不为空或者null
     *
     * @param collections 待判断的集合
     * @return 只要有一个集合为空或者null则返回为false, 否则为true
     */
    @SuppressWarnings({"rawtypes"})
    public static boolean isNoneEmpty(Collection... collections) {
        if (null == collections) {
            return false;
        }

        for (Collection collection : collections) {
            if (collection == null || collection.isEmpty()) {
                return false;
            }
        }
        return true;
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
