/**
 * 
 */
package com.yishuifengxiao.common.tool.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.yishuifengxiao.common.tool.constant.ErrorCode;
import com.yishuifengxiao.common.tool.exception.DataException;
import com.yishuifengxiao.common.tool.exception.ValidateException;

/**
 * <p>
 * 集合元素提取工具
 * </p>
 * 
 * 该工具的主要目标是在不发生NPE的前提下对集合以及集合里的元素进行操作，其具备以下的几项功能
 * <ol>
 * <li>将集合转换成java8中的串行流或并行流</li>
 * <li>将数组转换成List或Set，而从避免Arrays.asList()转换后存在的问题</li>
 * <li>安全地获取集合里的第一个元素</li>
 * </ol>
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
	 * @param list 需要转换的集合数据
	 * @return 串行流Stream
	 */
	public static <T> Stream<T> stream(List<T> list) {
		if (EmptyUtil.isEmpty(list)) {
			list = new ArrayList<>();
		}
		return list.stream();
	}

	/**
	 * 将数据安全地转换成串行流Stream,并检查输入的数据，若输入的数据源为空就抛出异常
	 * 
	 * @param <T>  集合里数据的类型
	 * @param list 需要转换的集合数据
	 * @param msg  异常提示信息
	 * @return 串行流Stream
	 * @throws ValidateException 输入的数据源为空
	 */
	public static <T> Stream<T> stream(List<T> list, String msg) throws ValidateException {
		if (EmptyUtil.isEmpty(list)) {
			throw new ValidateException(ErrorCode.DATA_ERROR, msg);
		}
		return list.stream();
	}

	/**
	 * 将数据安全地转换成串行流Stream
	 * 
	 * @param <T> 集合里数据的类型
	 * @param set 需要转换的集合数据
	 * @return 串行流Stream
	 */
	public static <T> Stream<T> stream(Set<T> set) {
		if (EmptyUtil.isEmpty(set)) {
			set = new HashSet<>();
		}
		return set.stream();
	}

	/**
	 * 将数据安全地转换成串行流Stream,并检查输入的数据，若输入的数据源为空就抛出异常
	 * 
	 * @param <T> 集合里数据的类型
	 * @param set 需要转换的集合数据
	 * @param msg 异常提示信息
	 * @return 串行流Stream
	 * @throws ValidateException 输入的数据源为空
	 */
	public static <T> Stream<T> stream(Set<T> set, String msg) throws ValidateException {
		if (EmptyUtil.isEmpty(set)) {
			throw new ValidateException(ErrorCode.DATA_ERROR, msg);
		}
		return set.stream();
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
	 * @throws ValidateException 输入的数据源为空
	 */
	public static <T> Stream<T> stream(T[] data, String msg) throws ValidateException {
		if (null == data) {
			throw new ValidateException(ErrorCode.DATA_ERROR, msg);
		}
		return stream(toList(data));
	}

	/**
	 * 将数据安全地转换成并行流Stream
	 * 
	 * @param <T>  集合里数据的类型
	 * @param list 需要转换的集合数据
	 * @return 并行流Stream
	 */
	public static <T> Stream<T> parallelStream(List<T> list) {
		if (EmptyUtil.isEmpty(list)) {
			list = new ArrayList<>();
		}
		return list.parallelStream();
	}

	/**
	 * 将数据安全地转换成并行流Stream,并检查输入的数据，若输入的数据源为空就抛出异常
	 * 
	 * @param <T>  集合里数据的类型
	 * @param list 需要转换的集合数据
	 * @param msg  异常提示信息
	 * @return 并行流Stream
	 * @throws ValidateException 输入的数据源为空
	 */
	public static <T> Stream<T> parallelStream(List<T> list, String msg) throws ValidateException {
		if (EmptyUtil.isEmpty(list)) {
			throw new ValidateException(ErrorCode.DATA_ERROR, msg);
		}
		return list.parallelStream();
	}

	/**
	 * 将数据安全地转换成并行流Stream
	 * 
	 * @param <T> 集合里数据的类型
	 * @param set 需要转换的集合数据
	 * @return 并行流Stream
	 */
	public static <T> Stream<T> parallelStream(Set<T> set) {
		if (EmptyUtil.isEmpty(set)) {
			set = new HashSet<>();
		}
		return set.parallelStream();
	}

	/**
	 * 将数据安全地转换成并行流Stream,并检查输入的数据，若输入的数据源为空就抛出异常
	 * 
	 * @param <T> 集合里数据的类型
	 * @param set 需要转换的集合数据
	 * @param msg 异常提示信息
	 * @return 并行流Stream
	 * @throws ValidateException 输入的数据源为空
	 */
	public static <T> Stream<T> parallelStream(Set<T> set, String msg) throws ValidateException {
		if (EmptyUtil.isEmpty(set)) {
			throw new ValidateException(ErrorCode.DATA_ERROR, msg);
		}
		return set.parallelStream();
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
	 * @throws ValidateException 输入的数据源为空
	 */
	public static <T> Stream<T> parallelStream(T[] data, String msg) throws ValidateException {
		if (null == data) {
			throw new ValidateException(ErrorCode.DATA_ERROR, msg);
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
	public synchronized static <T> List<T> toList(T[] objs) {
		if (EmptyUtil.isEmpty(objs)) {
			return new ArrayList<>();
		}
		List<T> list = new ArrayList<>();
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
	public synchronized static <T> Set<T> toSet(T[] objs) {
		if (EmptyUtil.isEmpty(objs)) {
			return new HashSet<>();
		}
		Set<T> set = new HashSet<>();
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
	public synchronized static <T> T first(Stream<T> data) {
		if (data == null) {
			return null;
		}

		return first(data.filter(Objects::nonNull).collect(Collectors.toList()));
	}

	/**
	 * 取出List中的第一个非空元素,如果Stream为空则返回null
	 * 
	 * @param <T>  List里的数据的类型
	 * @param data 链表
	 * @return List中的第一个非空元素
	 */
	public synchronized static <T> T first(List<T> data) {
		if (data == null || data.size() == 0) {
			return null;
		}
		return data.get(0);
	}

	/**
	 * 取出链表的第一个元素, 若链表为空则返回给定的缺省值
	 * 
	 * @param <T>          List里的数据的类型
	 * @param data         链表
	 * @param defaultValue 缺省值
	 * @return 链表的第一个元素
	 */
	public synchronized static <T> T first(List<T> data, T defaultValue) {
		T t = first(data);
		return null == t ? defaultValue : t;
	}

	/**
	 * 取出链表的第一个元素，若链表为空则抛出异常
	 * 
	 * @param <T>      List里的数据的类型
	 * @param data     链表数据
	 * @param errorMsg 异常提示信息
	 * @return 链表的第一个元素
	 * @throws DataException 链表为空
	 */
	public synchronized static <T> T first(List<T> data, String errorMsg) throws DataException {
		T t = first(data);
		if (null == t) {
			throw new DataException(ErrorCode.DATA_ERROR, errorMsg);
		}
		return t;
	}

	/**
	 * 取出Set中的第一个非空元素,如果Stream为空则返回null
	 * 
	 * @param <T>  Set里的数据的类型
	 * @param data 输入的数据
	 * @return Set中的第一个非空元素
	 */
	public synchronized static <T> T first(Set<T> data) {
		if (data == null || data.size() == 0) {
			return null;
		}
		Iterator<T> it = data.iterator();
		return it.next();
	}

	/**
	 * 取出Set的第一个元素, 若Set为空则返回给定的缺省值
	 * 
	 * @param <T>          List里的数据的类型
	 * @param data         数据源
	 * @param defaultValue 缺省值
	 * @return Set里的第一个元素
	 */
	public synchronized static <T> T first(Set<T> data, T defaultValue) {
		T t = first(data);
		return null == t ? defaultValue : t;
	}

	/**
	 * 取出Set的第一个元素，若Set为空则抛出异常
	 * 
	 * @param <T>      Set里的数据的类型
	 * @param data     数据源
	 * @param errorMsg 异常提示信息
	 * @return Set的第一个元素
	 * @throws DataException Set为空
	 */
	public synchronized static <T> T first(Set<T> data, String errorMsg) throws DataException {
		T t = first(data);
		if (null == t) {
			throw new DataException(ErrorCode.DATA_ERROR, errorMsg);
		}
		return t;
	}

	/**
	 * 取出数组的第一个元素, 若数组为空则返回null
	 * 
	 * @param <T>  数组里数据的类型
	 * @param data 数据源
	 * @return 数组的第一个元素
	 */
	public synchronized static <T> T first(T[] data) {
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
	public synchronized static <T> T first(T[] data, T defaultValue) {
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
	 * @throws DataException 数组为空
	 */
	public synchronized static <T> T first(T[] data, String errorMsg) throws DataException {
		T t = first(data);
		if (null == t) {
			throw new DataException(ErrorCode.DATA_ERROR, errorMsg);
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
	public synchronized static <T> T first(Collection<T> data) {
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
	public synchronized static <T> T first(Collection<T> data, T defaultValue) {
		T t = first(data);
		return null == t ? defaultValue : t;
	}

	/**
	 * 取出集合的第一个元素，若集合为空则抛出异常
	 * 
	 * @param <T>      集合里的数据的类型
	 * @param data     数据源
	 * @param errorMsg 异常提示信息
	 * @return 集合的第一个元素
	 * @throws DataException 集合为空
	 */
	public synchronized static <T> T first(Collection<T> data, String errorMsg) throws DataException {
		T t = first(data);
		if (null == t) {
			throw new DataException(ErrorCode.DATA_ERROR, errorMsg);
		}
		return t;
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

}
