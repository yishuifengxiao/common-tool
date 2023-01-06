package com.yishuifengxiao.common.tool.log;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;

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
     */
    public synchronized static void setLevel(String loggerName, String logLevel) {
        if (StringUtils.isAnyBlank(loggerName, logLevel)) {
            return;
        }
        try {
            LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

            Logger logger = loggerContext.getLogger(loggerName.trim());

            if (null == logger) {
                return;
            }

            logger.setLevel(Level.valueOf(logLevel.trim()));
        } catch (Throwable e) {
            log.warn("动态修改日志级别 loggerName ={} ， logLevel ={} 时出现问题 {} ", loggerName, logLevel, e);
        }


    }

}
