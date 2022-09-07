/**
 *
 */
package com.yishuifengxiao.common.tool.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * 回调工具类
 * </p>
 *
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
public final class CallbackUtil {

	/**
	 * 简单线程工程
	 */
	private final static ThreadFactory SIMPLE_THREAD_FACTORY = new SimpleThreadFactory();

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
	public static ExecutorService getPool() {
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
	public static synchronized void execute(Runnable runnable, ExcuteComplete complete) {
		execute(runnable, complete, null);
	}

	/**
	 * 执行任务
	 *
	 * @param runnable 待执行的任务
	 * @param error    执行失败后触发的动作
	 */
	public static synchronized void execute(Runnable runnable, ExcuteError error) {
		execute(runnable, null, error);
	}

	/**
	 * 执行任务
	 *
	 * @param runnable 待执行的任务
	 * @param complete 执行完成后的回调(不论成功还是失败都会触发)
	 * @param error    执行失败后触发的动作
	 */
	public static synchronized void execute(Runnable runnable, ExcuteComplete complete, ExcuteError error) {
		Assert.notNull("待执行的任务不能为空", runnable);
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
	private static class SimpleThreadFactory implements ThreadFactory {

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
	public static interface ExcuteError {

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
	public static interface ExcuteComplete {

		/**
		 * 在执行完成时触发
		 */
		void onComplete();

	}

}
