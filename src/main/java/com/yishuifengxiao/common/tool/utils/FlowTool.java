package com.yishuifengxiao.common.tool.utils;

import com.yishuifengxiao.common.tool.collections.DataUtil;
import com.yishuifengxiao.common.tool.collections.EmptyUtil;
import com.yishuifengxiao.common.tool.random.IdWorker;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * 并行执行工具
 *
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
public final class FlowTool {

	/**
	 * 工作队列
	 */
	@SuppressWarnings("rawtypes")
	private final List<List<Worker>> workers = new ArrayList<>();

	/**
	 * 默认的异常处理函数
	 */
	private ErrorHandler errorHandler = (e, w) -> {
	};

	/**
	 * 默认的完成处理函数
	 */
	private SuccessHandler successHandler = (s, c) -> {
	};

	/**
	 * 默认的过滤器
	 */
	private Filter filter = (w, d) -> true;

	/**
	 * 初始化一个并行处理工具
	 *
	 * @param workers 工作队列
	 * @return 并行处理工具实例
	 */
	@SuppressWarnings("rawtypes")
	public static FlowTool init(Worker... workers) {
		FlowTool tool = new FlowTool();
		tool.workers.clear();
		addWorkers(tool, workers);
		return tool;
	}

	/**
	 * 为并行处理工具实例添加工作队列
	 *
	 * @param tool    工具实例
	 * @param workers 工作队列
	 */
	@SuppressWarnings("rawtypes")
	private static void addWorkers(FlowTool tool, Worker[] workers) {
		if (null != workers) {
			List<Worker> queues = DataUtil.parallelStream(workers).filter(Objects::nonNull)
					.collect(Collectors.toList());
			if (EmptyUtil.notEmpty(queues)) {
				tool.workers.add(queues);
			}
		}
	}

	/**
	 * 启动并行处理工具
	 */
	public void start() {

		final AtomicBoolean error = new AtomicBoolean(false);
		Context result = null;
		for (int i = 0; i < this.workers.size(); i++) {
			if (error.get()) {
				break;
			}
			final Context context = new Context();
			this.workers.get(i).parallelStream().filter(Objects::nonNull).forEach(v -> {
				try {
					Object returnData = v.action(context);
					error.set(!filter.doWork(v, returnData));
					context.put(v, returnData);
				} catch (Throwable e) {
					errorHandler.catchError(e, v);
					error.set(true);
				}
			});
			result = context;
		}
		successHandler.complete(!error.get(), result);

	}

	/**
	 * 为并行处理工具添加工作队列
	 *
	 * @param workers 工作队列
	 * @return 当前处理工具实例
	 */
	@SuppressWarnings("rawtypes")
	public FlowTool then(Worker... workers) {
		addWorkers(this, workers);
		return this;
	}

	/**
	 * 为并行处理工具添加异常处理机制
	 *
	 * @param handler 异常处理机制
	 * @return 当前处理工具实例
	 */
	public FlowTool error(ErrorHandler handler) {
		if (null != handler) {
			this.errorHandler = handler;
		}
		return this;
	}

	/**
	 * 为并行处理工具添加任务完成处理机制
	 *
	 * @param handler 任务完成机制
	 * @return 当前处理工具实例
	 */
	public FlowTool success(SuccessHandler handler) {
		if (null != handler) {
			this.successHandler = handler;
		}
		return this;
	}

	/**
	 * 为并行处理工具添加任务过滤器
	 *
	 * @param filter 任务过滤器
	 * @return 当前处理工具实例
	 */
	public FlowTool filter(Filter filter) {
		if (null != filter) {
			this.filter = filter;
		}
		return this;
	}

	/**
	 * 工作对象
	 *
	 * @param <T> 数据类型
	 */
	public interface Worker<T> {

		/**
		 * 执行任务
		 *
		 * @param context 任务上下文
		 * @return 任务结果
		 */
		T action(final Context context);

		/**
		 * 获取任务名称
		 *
		 * @return 任务名称
		 */
		default String getName() {
			return IdWorker.snowflakeStringId();
		}
	}

	/**
	 * 应用上下文
	 */
	public class Context {

		private Map<String, Object> cache = new HashMap<>();
		@SuppressWarnings("rawtypes")
		private List<Worker> list = new ArrayList<>();

		@SuppressWarnings("rawtypes")
		private synchronized void put(Worker v, Object data) {
			cache.put(v.getName(), data);
			list.add(v);
		}

		/**
		 * 获取上游任务的当前任务对象的输出数据
		 *
		 * @param v 当前任务对象
		 * @return 当前任务对象的输出数据
		 */
		@SuppressWarnings("rawtypes")
		public synchronized Object get(Worker v) {
			return cache.get(v.getName());
		}

		/**
		 * 获取上游任务的全部的任务对象
		 *
		 * @return 全部的任务对象
		 */
		@SuppressWarnings("rawtypes")
		public List<Worker> workers() {
			return list;
		}

		/**
		 * 获取上游任务的全部的输出数据
		 *
		 * @return 全部的输出数据
		 */
		public List<Object> values() {
			return cache.values().parallelStream().collect(Collectors.toList());
		}
	}

	/**
	 * 任务过滤器
	 */
	public interface Filter {
		/**
		 * 是否执行下游任务
		 *
		 * @param worker 工作对象
		 * @param data   该工作对象的生产数据
		 * @return 是否执行下游任务，true表示执行，false不执行
		 */
		@SuppressWarnings("rawtypes")
		boolean doWork(Worker worker, Object data);
	}

	/**
	 * 异常处理
	 */
	public interface ErrorHandler {
		/**
		 * 在执行任务队列时出现问题时
		 *
		 * @param e      出现的问题
		 * @param worker 工作对象
		 */
		@SuppressWarnings("rawtypes")
		void catchError(Throwable e, Worker worker);
	}

	/**
	 * 成功处理
	 */
	public interface SuccessHandler {
		/**
		 * 任务完成后执行
		 *
		 * @param suc     true表示任务全部完成,false表示任务有失败
		 * @param context 上下文
		 */
		void complete(boolean suc, Context context);
	}
}
