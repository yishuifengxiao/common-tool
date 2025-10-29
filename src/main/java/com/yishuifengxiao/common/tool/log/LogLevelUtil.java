package com.yishuifengxiao.common.tool.log;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import com.yishuifengxiao.common.tool.collections.CollUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 动态修改logback日志级别
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
                    .filter(v -> StringUtils.equals(v.getName(), loggerName))
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
