package com.yishuifengxiao.common.tool.utils;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>Logback日志级别动态修改工具类</p>
 * <p>提供在运行时动态修改Logback日志级别的功能。</p>
 * <p>特性：</p>
 * <ul>
 * <li>支持按Logger名称动态修改日志级别</li>
 * <li>日志级别合法性校验</li>
 * <li>线程安全的操作</li>
 * </ul>
 *
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
public class LogLevelUtil {

    /**
     * 动态修改logback日志的级别
     *
     * @param loggerName Logger的名字，例如 "org.springframework"
     * @param logLevel   日志级别 ，例如 info
     * @return 是否修改成功
     */
    public static boolean setLevel(String loggerName, String logLevel) {
        if (StringUtils.isAnyBlank(loggerName, logLevel)) {
            return false;
        }

        String targetName = loggerName.trim();
        Level level;
        // 校验日志级别是否合法
        try {
            level = Level.valueOf(logLevel.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            if (log.isWarnEnabled()) {
                log.warn("Invalid log level provided: {}, loggerName={}", logLevel, loggerName);
            }
            return false;
        }

        try {
            LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
            // 直接获取（必要时创建）目标 Logger，避免因目标 Logger 尚未实例化而无法匹配
            // 设置到 Logger 实例上的级别会持久保存，待该 Logger 被创建后即生效
            Logger logger = loggerContext.getLogger(targetName);
            if (logger == null) {
                return false;
            }
            logger.setLevel(level);

        } catch (Exception e) {
            if (log.isWarnEnabled()) {
                log.warn("There is a problem when dynamically modifying the log level. loggerName={}, logLevel={}",
                        loggerName, logLevel, e);
            }
            return false;
        }

        return true;
    }
}
