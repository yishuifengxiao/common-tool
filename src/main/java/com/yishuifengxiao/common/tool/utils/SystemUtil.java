package com.yishuifengxiao.common.tool.utils;

/**
 * 系统工具类
 *
 * @author qingteng
 * @version 1.0.0
 * @since 1.0.0
 */
public final class SystemUtil {

    /**
     * 休眠指定的时间
     *
     * @param millis 休眠时间，单位毫秒
     * @return 是否执行成功
     */
    public static boolean sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}
