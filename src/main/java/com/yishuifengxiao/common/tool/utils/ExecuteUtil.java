package com.yishuifengxiao.common.tool.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * 执行工具
 *
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
public class ExecuteUtil {
	/**
	 * 简单线程工程
	 */
	public final static ThreadFactory SIMPLE_THREAD_FACTORY = new SimpleThreadFactory();

	/**
	 * 线程池初始化
	 */
	private final static ExecutorService POOL = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(),
			Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), SIMPLE_THREAD_FACTORY,
			new ThreadPoolExecutor.AbortPolicy());

	/**
	 * 获取线程池
	 *
	 * @return 内置线程池
	 */
	public static ExecutorService pool() {
		return POOL;
	}

	/**
	 * 执行任务
	 *
	 * @param runnable 待执行的任务
	 */
	public static synchronized void execute(Runnable runnable) {
		execute(runnable, null, null);
	}

	/**
	 * 执行任务
	 *
	 * @param runnable 待执行的任务
	 * @param complete 执行完成后的回调(不论成功还是失败都会触发)
	 */
	public static synchronized void execute(Runnable runnable, ExecuteComplete complete) {
		execute(runnable, complete, null);
	}

	/**
	 * 执行任务
	 *
	 * @param runnable 待执行的任务
	 * @param error    执行失败后触发的动作
	 */
	public static synchronized void execute(Runnable runnable, ExecuteError error) {
		execute(runnable, null, error);
	}

	/**
	 * 执行任务
	 *
	 * @param runnable 待执行的任务
	 * @param complete 执行完成后的回调(不论成功还是失败都会触发)
	 * @param error    执行失败后触发的动作
	 */
	public static synchronized void execute(Runnable runnable, ExecuteComplete complete, ExecuteError error) {	
		POOL.execute(() -> {
			try {
				runnable.run();
			} catch (Throwable e) {
				log.warn("执行回调任务 {} 时出现问题，出现的问题为{}", runnable, e.getMessage());
				if (null != error) {
					error.onError(e);
				}
			} finally {
				if (null != complete) {
					complete.onComplete();
				}
			}
		});

	}

	/**
	 * 简单线程工程
	 *
	 * @author yishui
	 * @version 1.0.0
	 * @since 1.0.0
	 */
	public static class SimpleThreadFactory implements ThreadFactory {

		@Override
		public Thread newThread(Runnable r) {
			Thread thread = new Thread(r);
			thread.setName(new StringBuilder("callback-").append(System.currentTimeMillis()).toString());
			return thread;
		}

	}

	/**
	 * 执行失败时的回调
	 * <p>
	 * 仅仅在执行失败时会触发
	 * </p>
	 *
	 * @author yishui
	 * @version 1.0.0
	 * @since 1.0.0
	 */
	@FunctionalInterface
	public interface ExecuteError {

		/**
		 * 在执行失败时调用
		 *
		 * @param e 执行失败时触发的错误
		 */
		void onError(Throwable e);

	}

	/**
	 * 执行完成时的回调
	 * <p>
	 * 无论是否执行成功均会触发
	 * </p>
	 *
	 * @author yishui
	 * @version 1.0.0
	 * @since 1.0.0
	 */
	@FunctionalInterface
	public interface ExecuteComplete {

		/**
		 * 在执行完成时触发
		 */
		void onComplete();

	}

	/**
	 * <p>
	 * 流程串行执行
	 * </p>
	 * <p>
	 * 当前一个流程的输出结果不满足要求时继续执行下一个流程，直到执行完成,若流程的输出结果满足要求则停止后续流程的执行，直接返回结果
	 * </p>
	 *
	 * @param match     匹配条件，若满足此条件则直接返回结果
	 * @param suppliers 流程执行
	 * @param <T>       输出数据类型
	 * @return 输出数据
	 */
	@SuppressWarnings("unchecked")
	public synchronized static <T> T execute(Predicate<T> match, Supplier<T>... suppliers) {
		if (null == suppliers) {
			return null;
		}
		return Arrays.stream(suppliers).filter(Objects::nonNull).map(v -> v.get()).filter(v -> match.test(v))
				.findFirst().orElse(null);
	}
}
