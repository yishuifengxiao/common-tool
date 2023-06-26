package com.yishuifengxiao.common.tool.log;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import com.yishuifengxiao.common.tool.collections.SizeUtil;
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
    public   static boolean setLevel(String loggerName, String logLevel) {
        if (StringUtils.isAnyBlank(loggerName, logLevel)) {
            return false;
        }
        try {
            LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
            final List<Logger> loggers = loggerContext.getLoggerList();
            final Set<Logger> sets = loggers.parallelStream().filter(v -> StringUtils.equalsIgnoreCase(v.getName(), loggerName)).collect(Collectors.toSet());
            if (SizeUtil.isEmpty(sets)) {
                return false;
            }
            sets.stream().forEach(logger -> {
                logger.setLevel(Level.valueOf(logLevel.trim()));
            });

        } catch (Throwable e) {
            log.warn("动态修改日志级别 loggerName ={} ， logLevel ={} 时出现问题 {} ", loggerName, logLevel, e);
            return false;
        }
        return true;

    }

}
