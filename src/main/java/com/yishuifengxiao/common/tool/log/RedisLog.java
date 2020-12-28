package com.yishuifengxiao.common.tool.log;

import java.io.Serializable;
import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 自定义日志的内容<br/>
 * 利用redis的订阅发布功能，将信息发布到redis中
 * 
 * @author qingteng
 * @date 2020年12月27日
 * @version 1.0.0
 * @param <E>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@Builder
public class RedisLog implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5380449689877786879L;

	/**
	 * 时间
	 */
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS", timezone = "GMT+8")
	@JSONField(format = "yyyy-MM-dd HH:mm:ss.SSS")
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
