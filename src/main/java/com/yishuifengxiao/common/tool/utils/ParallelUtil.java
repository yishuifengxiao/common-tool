package com.yishuifengxiao.common.tool.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CountDownLatch;

/**
 * <p>多线程并行执行工具</p>
 * <p>注意这里是直接new Thread方式开启新线程的，应注意有可能造成系统创建大量同类线程而导致消耗完内存或者“过度切换”的问题</p>
 *
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
public class ParallelUtil {


    private CountDownLatch cdl;

    @SuppressWarnings("unused")
    private ParallelUtil() {
    }

    /**
     * 构造函数
     *
     * @param taskNum 并行任务的数量
     */
    public ParallelUtil(int taskNum) {
        this.cdl = new CountDownLatch(taskNum);
    }

    /**
     * 创建一个实例
     *
     * @param taskNum 并行任务的数量
     * @return 实例
     */
    public static ParallelUtil instance(int taskNum) {
        return new ParallelUtil(taskNum);
    }

    /**
     * 执行一个任务
     *
     * @param runnable 待执行的任务
     */
    public ParallelUtil execute(Runnable runnable) {

        CallbackUtil.execute(() -> {
            try {
                runnable.run();
            } catch (Throwable e) {
                log.info("执行任务 {} 时出现问题 {} ", runnable, e);
            } finally {
                cdl.countDown();
            }
        });
        return this;
    }

    /**
     * 等待所有的任务都执行完成。在执行完成之前，都处于阻塞状态。
     */
    public void waitComplete() {
        try {
            cdl.await();
        } catch (InterruptedException e) {
            log.warn("等待任务执行过程中出现问题{}", e);
        }
    }
}
