package com.yishuifengxiao.common.tool.io;

import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.io.Flushable;

/**
 * <p>
 * IO流关闭工具
 * </p>
 * 该工具主要目的是优雅地关闭掉各种IO流，从而屏蔽掉因为关闭IO时强制异常捕获代码造成代码优雅性的降低
 *
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
public class CloseUtil {

    /**
     * <p>批量关闭IO流</p>
     * <p>注意：如果被关闭的IO是Flushable的实例，则进行flush操作</p>
     *
     * @param closeable 需要关闭的IO流
     */
    public static void close(Closeable... closeable) {
        close(true, closeable);
    }

    /**
     * 批量关闭IO流
     *
     * @param flush     如果被关闭的IO是Flushable的实例，是否先进行flush操作，true表示进行，false表示不进行
     * @param closeable 待管理的io流
     */
    public static void close(boolean flush, Closeable... closeable) {
        if (null == closeable || closeable.length == 0) {
            return;
        }
        for (Closeable close : closeable) {
            if (null == close) {
                continue;
            }
            try {
                // 先执行 flush 操作（如果需要）
                if (flush && close instanceof Flushable) {
                    flushSafely((Flushable) close);
                }
                // 再执行 close 操作
                close.close();
            } catch (Exception e) {
                log.warn("Failed to close stream: {}", close.getClass().getName(), e);
            }
        }
    }

    /**
     * 安全地执行 flush 操作并处理可能出现的异常
     *
     * @param flushable 可刷新的对象
     */
    private static void flushSafely(Flushable flushable) {
        try {
            flushable.flush();
        } catch (Exception e) {
            log.warn("Failed to flush stream: {}", flushable.getClass().getName(), e);
        }
    }
}
