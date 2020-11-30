/**
 * 
 */
package com.yishuifengxiao.common.tool.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import com.yishuifengxiao.common.tool.constant.ErrorCode;
import com.yishuifengxiao.common.tool.exception.DataException;
import com.yishuifengxiao.common.tool.exception.ValidateException;

/**
 * 集合元素获取工具类
 * 
 * @author yishui
 * @date 2018年10月17日
 * @Version 0.0.1
 */
public final class DataUtil {

	/**
	 * 将数据安全地转换成串行流Stream<br/>
	 * 该操作不会引起NPE
	 * 
	 * @param <T>
	 * @param list 需要转换的数据
	 * @return 串行流Stream
	 */
	public static <T> Stream<T> stream(List<T> list) {
		if (EmptyUtil.isEmpty(list)) {
			list = new ArrayList<>();
		}
		return list.stream();
	}

	/**
	 * 将数据转换成串行流Stream，如果传入的数据为null则抛出异常
	 * 
	 * 
	 * @param <T>
	 * @param list 需要转换的数据
	 * @param msg  提示信息
	 * @return 串行流Stream
	 * @throws ValidateException
	 */
	public static <T> Stream<T> stream(List<T> list, String msg) throws ValidateException {
		if (EmptyUtil.isEmpty(list)) {
			throw new ValidateException(ErrorCode.DATA_ERROR, msg);
		}
		return list.stream();
	}

	/**
	 * 将数据安全地转换成串行流Stream<br/>
	 * 该操作不会引起NPE
	 * 
	 * @param <T>
	 * @param list 需要转换的数据
	 * @return 串行流Stream
	 */
	public static <T> Stream<T> stream(Set<T> set) {
		if (EmptyUtil.isEmpty(set)) {
			set = new HashSet<>();
		}
		return set.stream();
	}

	/**
	 * 将数据转换成串行流Stream<br/>
	 * 该操作不会引起NPE
	 * 
	 * @param <T>
	 * @param list 需要转换的数据
	 * @param msg  提示信息
	 * @return 串行流Stream
	 * @throws ValidateException
	 */
	public static <T> Stream<T> stream(Set<T> set, String msg) throws ValidateException {
		if (EmptyUtil.isEmpty(set)) {
			throw new ValidateException(ErrorCode.DATA_ERROR, msg);
		}
		return set.stream();
	}

	/**
	 * 将数据安全地转换成串行流Stream<br/>
	 * 该操作不会引起NPE
	 * 
	 * @param <T>
	 * @param list 需要转换的数据
	 * @return 串行流Stream
	 */
	public static <T> Stream<T> stream(T[] data) {
		return stream(toList(data));
	}

	/**
	 * 将数据转换成串行流Stream,如果传入的数据为null则抛出异常
	 * 
	 * @param <T>
	 * @param list 需要转换的数据
	 * @param msg  提示信息
	 * @return 串行流Stream
	 * @throws ValidateException
	 */
	public static <T> Stream<T> stream(T[] data, String msg) throws ValidateException {
		if (null == data) {
			throw new ValidateException(ErrorCode.DATA_ERROR, msg);
		}
		return stream(toList(data));
	}

	/**
	 * 将数据安全地转换成并行流ParallelStream<br/>
	 * 该操作不会引起NPE
	 * 
	 * @param <T>
	 * @param list 需要转换的数据
	 * @return 并行流Stream
	 */
	public static <T> Stream<T> parallelStream(List<T> list) {
		if (EmptyUtil.isEmpty(list)) {
			list = new ArrayList<>();
		}
		return list.parallelStream();
	}

	/**
	 * 将数据转换成并行流ParallelStream，如果传入的数据为null则抛出异常
	 * 
	 * 
	 * @param <T>
	 * @param list 需要转换的数据
	 * @param msg  提示信息
	 * @return 并行流ParallelStream
	 * @throws ValidateException
	 */
	public static <T> Stream<T> parallelStream(List<T> list, String msg) throws ValidateException {
		if (EmptyUtil.isEmpty(list)) {
			throw new ValidateException(ErrorCode.DATA_ERROR, msg);
		}
		return list.parallelStream();
	}

	/**
	 * 将数据安全地转换成并行流ParallelStream<br/>
	 * 该操作不会引起NPE
	 * 
	 * @param <T>
	 * @param list 需要转换的数据
	 * @return 并行流ParallelStream
	 */
	public static <T> Stream<T> parallelStream(Set<T> set) {
		if (EmptyUtil.isEmpty(set)) {
			set = new HashSet<>();
		}
		return set.parallelStream();
	}

	/**
	 * 将数据转换成并行流ParallelStream,如果传入的数据为null则抛出异常
	 * 
	 * @param <T>
	 * @param list 需要转换的数据
	 * @param msg  提示信息
	 * @return 并行流ParallelStream
	 * @throws ValidateException
	 */
	public static <T> Stream<T> parallelStream(Set<T> set, String msg) throws ValidateException {
		if (EmptyUtil.isEmpty(set)) {
			throw new ValidateException(ErrorCode.DATA_ERROR, msg);
		}
		return set.parallelStream();
	}

	/**
	 * 将数据安全地转换成并行流ParallelStream<br/>
	 * 该操作不会引起NPE
	 * 
	 * @param <T>
	 * @param list 需要转换的数据
	 * @return 并行流ParallelStream
	 */
	public static <T> Stream<T> parallelStream(T[] data) {
		return parallelStream(toList(data));
	}

	/**
	 * 将数据安全地转换成并行流ParallelStream<br/>
	 * 该操作不会引起NPE
	 * 
	 * @param <T>
	 * @param list 需要转换的数据
	 * @param msg  提示信息
	 * @return 并行流ParallelStream
	 * @throws ValidateException
	 */
	public static <T> Stream<T> parallelStream(T[] data, String msg) throws ValidateException {
		if (null == data) {
			throw new ValidateException(ErrorCode.DATA_ERROR, msg);
		}
		return parallelStream(toList(data));
	}

	/**
	 * 将数组转为list
	 * 
	 * @param objs 需要转换的数组
	 * @return 转换后的list
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
	 * 将数组转换为set
	 * 
	 * @param objs 需要转换的数组
	 * @return 转换后的set
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
	 * 取出链表的第一个元素,如果链表为空则返回null
	 * 
	 * @param data
	 * @return
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
	 * @param data
	 * @param defaultValue 给定的缺省值
	 * @return
	 */
	public synchronized static <T> T first(List<T> data, T defaultValue) {
		T t = first(data);
		return null == t ? defaultValue : t;
	}

	/**
	 * 取出链表的第一个元素，若链表为空则抛出异常
	 * 
	 * @param <T>
	 * @param data     链表数据
	 * @param errorMsg 异常提示信息
	 * @return
	 * @throws DataException
	 */
	public synchronized static <T> T first(List<T> data, String errorMsg) throws DataException {
		T t = first(data);
		if (null == t) {
			throw new DataException(ErrorCode.DATA_ERROR, errorMsg);
		}
		return t;
	}

	/**
	 * 获取set集合的第一个元素,如果set为空则返回null
	 * 
	 * @param data
	 * @return
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
	 * @param data
	 * @param defaultValue 给定的缺省值
	 * @return
	 */
	public synchronized static <T> T first(Set<T> data, T defaultValue) {
		T t = first(data);
		return null == t ? defaultValue : t;
	}

	/**
	 * 从set里取出第一个元素，若set集合为空则抛出异常
	 * 
	 * @param <T>
	 * @param data
	 * @param errorMsg 异常提示信息
	 * @return
	 * @throws DataException
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
	 * @param data
	 * @return
	 */
	public synchronized static <T> T first(T[] data) {
		if (data == null || data.length == 0) {
			return null;
		}
		return data[0];
	}

	/**
	 * 取出数组的第一个元素，若数组为空则返回给定的缺省值
	 * 
	 * @param data
	 * @param defaultValue 给定的缺省值
	 * @return
	 */
	public synchronized static <T> T first(T[] data, T defaultValue) {
		T t = first(data);
		return null == t ? defaultValue : t;
	}

	/**
	 * 从数组里取出第一个元素，如果数组为空，则抛出异常
	 * 
	 * @param <T>
	 * @param data
	 * @param errorMsg 异常提示信息
	 * @return
	 * @throws DataException
	 */
	public synchronized static <T> T first(T[] data, String errorMsg) throws DataException {
		T t = first(data);
		if (null == t) {
			throw new DataException(ErrorCode.DATA_ERROR, errorMsg);
		}
		return t;
	}

	/**
	 * 取出集合的第一个元素
	 * 
	 * @param data
	 * @return
	 */
	public synchronized static <T> T first(Collection<T> data) {
		if (data == null || data.size() == 0) {
			return null;
		}
		Iterator<T> it = data.iterator();
		return it.next();
	}

	/**
	 * 取出集合的第一个元素，若链表为空则返回给定的缺省值
	 * 
	 * @param data
	 * @param defaultValue 给定的缺省值
	 * @return
	 */
	public synchronized static <T> T first(Collection<T> data, T defaultValue) {
		T t = first(data);
		return null == t ? defaultValue : t;
	}

	/**
	 * 取出集合的第一个元素，如果集合为空，则抛出异常
	 * 
	 * @param <T>
	 * @param data
	 * @param errorMsg 异常提示信息
	 * @return
	 * @throws DataException
	 */
	public synchronized static <T> T first(Collection<T> data, String errorMsg) throws DataException {
		T t = first(data);
		if (null == t) {
			throw new DataException(ErrorCode.DATA_ERROR, errorMsg);
		}
		return t;
	}

}
