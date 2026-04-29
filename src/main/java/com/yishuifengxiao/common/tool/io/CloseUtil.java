package com.yishuifengxiao.common.tool.io;

import java.io.Closeable;
import java.io.Flushable;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>IO流关闭工具类</p>
 * <p>优雅地关闭各种IO流，屏蔽关闭时的异常捕获代码，提升代码整洁性。</p>
 * <p>特性：</p>
 * <ul>
 * <li>支持批量关闭多个Closeable对象</li>
 * <li>支持在关闭前自动执行flush操作</li>
 * <li>异常静默处理，不影响主流程</li>
 * </ul>
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
