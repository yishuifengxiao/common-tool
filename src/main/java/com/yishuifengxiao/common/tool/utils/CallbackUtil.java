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
 * 回调工具类
 * 
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
public class CallbackUtil {

	/**
	 * 简单线程工程
	 */
	private final static ThreadFactory SIMPLE_THREAD_FACTORY = new SimpleThreadFactory();

	/**
	 * 线程池初始化
	 */
	private final static ExecutorService POOL = new ThreadPoolExecutor(1, Runtime.getRuntime().availableProcessors(),
			0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(1024), SIMPLE_THREAD_FACTORY,
			new ThreadPoolExecutor.AbortPolicy());

	/**
	 * 执行任务
	 * 
	 * @param runnable 待执行的任务
	 */
	public static void execute(Runnable runnable) {
		try {
			POOL.execute(runnable);
		} catch (Exception e) {
			log.info("执行回调任务 {} 时出现问题，出现的问题为{}", runnable, e.getMessage());
		}
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

}
