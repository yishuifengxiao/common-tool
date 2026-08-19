package com.yishuifengxiao.common.tool.log;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

import com.yishuifengxiao.common.tool.collections.CollUtil;

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

        // 校验日志级别是否合法
        try {
            Level.valueOf(logLevel.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            if (log.isWarnEnabled()) {
                log.warn("Invalid log level provided: {}, loggerName={}", logLevel, loggerName);
            }
            return false;
        }

        try {
            LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
            final List<Logger> loggers = loggerContext.getLoggerList();

            // 使用串行流替代并行流保证线程安全
            final Set<Logger> matchedLoggers = loggers.stream()
                    .filter(v -> Objects.equals(v.getName(), loggerName))
                    .collect(Collectors.toSet());

            if (CollUtil.isEmpty(matchedLoggers)) {
                return false;
            }

            matchedLoggers.forEach(logger -> logger.setLevel(Level.valueOf(logLevel.trim().toUpperCase())));

        } catch (Throwable e) {
            if (log.isWarnEnabled()) {
                log.warn("There is a problem when dynamically modifying the log level. loggerName={}, logLevel={}",
                        loggerName, logLevel, e);
            }
            return false;
        }

        return true;
    }
}
