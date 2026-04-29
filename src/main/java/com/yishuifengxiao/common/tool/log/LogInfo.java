package com.yishuifengxiao.common.tool.log;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * <p>自定义日志内容对象</p>
 * <p>用于封装日志信息，支持通过Redis订阅发布功能传输日志内容。</p>
 * <p>包含字段：</p>
 * <ul>
 * <li>date - 日志时间</li>
 * <li>threadName - 线程名称</li>
 * <li>loggerName - Logger名称</li>
 * <li>level - 日志级别</li>
 * <li>message - 日志消息</li>
 * <li>application - 应用名称</li>
 * <li>extra - 附加信息</li>
 * </ul>
 *
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@Builder
public class LogInfo implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 5380449689877786879L;

    /**
     * 日志的时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS", timezone = "GMT+8")
    private Date date;

    /**
     * 线程名字
     */
    private String threadName;

    /**
     * logger 的名字
     */
    private String loggerName;

    /**
     * 日志级别
     */
    private String level;

    /**
     * 原始消息【格式化之后的消息】
     */
    private String message;

    /**
     * 应用名称
     */
    private String application;

    /**
     * 附加信息
     */
    private String extra;

    /**
     * 发送时间
     */
    private Long timeStamp;

}
