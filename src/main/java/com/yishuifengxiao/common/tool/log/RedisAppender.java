package com.yishuifengxiao.common.tool.log;

import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import ch.qos.logback.core.spi.DeferredProcessingAware;
import ch.qos.logback.core.status.ErrorStatus;
import com.yishuifengxiao.common.tool.collections.JsonUtil;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.RedisURI.Builder;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.InetAddress;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.concurrent.locks.ReentrantLock;

/**
 * <p>
 * 自定义日志输出器
 * </p>
 * <p>
 * 该工具是logback日志里的一个自定义日志输出器，主要是利用redis的订阅发布功能， 将日志信息通过redis发布出来，以便其他终端能够接收到日志信息
 * </p>
 * 使用方法如下:
 * 
 * <pre>
 * {@code
 * <appender name="redis" class=com.yishuifengxiao.common.tool.log.RedisAppender"> 
 * 	<host>Redis服务器地址</host>
 * 	<port>Redis服务器端口号</port> 
 * 	<password>redis连接密码</password>
 * 	<application>项目名字，可选，如 测试项目</application> 
 * 	<channel>发送的订阅通道的名字，选填，默认为application的值</channel> 
 * 	<extra>附加数据，会被加载在日志中</extra> 
 * </appender>
 * 
 * }
 * </pre>
 * 
 * 
 * 
 * 
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 * @param <E> 数据类型
 */
public class RedisAppender<E> extends UnsynchronizedAppenderBase<E> {

	/**
	 * 默认的端口 6379
	 */
	private static final int DEFAULT_PORT = 6379;

	/**
	 * 默认的超时时间，180秒
	 */
	private static final int DEFAULT_TIMEOUT_SECOND = 180;

	/**
	 * All synchronization in this class is done via the lock object.
	 */
	protected final ReentrantLock lock = new ReentrantLock(false);

	private RedisClient redisClient;

	private StatefulRedisConnection<String, String> connection;

	private RedisAsyncCommands<String, String> async;

	/**
	 * Redis 数据库地址
	 */
	private String host;

	/**
	 * Redis 数据库密码
	 */
	private String password;

	/**
	 * Redis 数据库端口，默认为6379
	 */
	private Integer port;

	/**
	 * Redis 订阅通道名字
	 */
	private String channel;

	/**
	 * Redis 连接过期时间
	 */
	private Integer timeoutInSecond;

	/**
	 * 应用名称
	 */
	private String application;

	/**
	 * 附加信息
	 */
	private String extra;

	@Override
	protected void append(E eventObject) {
		if (!isStarted()) {
			return;
		}
		subAppend(eventObject);
	}

	/**
	 * 获取连接的端口
	 * 
	 * @return 连接的端口
	 */
	protected int port() {
		if (null == this.port) {
			return DEFAULT_PORT;
		}
		return this.port;
	}

	/**
	 * 获取连接的超时时间，单位为秒
	 * 
	 * @return 连接的超时时间，单位为秒
	 */
	protected int timeout() {
		if (null == this.timeoutInSecond) {
			return DEFAULT_TIMEOUT_SECOND;
		}
		return this.timeoutInSecond;
	}

	/**
	 * Redis 订阅通道名字
	 * 
	 * @return Redis 订阅通道名字
	 */
	protected String channel() {
		if (null == this.channel || "".equals(this.channel.trim())) {
			return this.application();
		}
		return this.channel.trim();
	}

	/**
	 * 获取应用名字，如果应用名字为空就使用当前的主机名
	 * 
	 * @return 应用名字
	 */
	protected String application() {
		if (StringUtils.isBlank(this.application)) {
			try {
				return InetAddress.getLocalHost().getHostName();
			} catch (Exception e) {
			}
		}

		return this.application.trim();
	}

	/**
	 * Actual writing occurs here.
	 * <p>
	 * Most subclasses of <code>WriterAppender</code> will need to override this
	 * method.
	 * 
	 * @param event 日志事件
	 */
	protected void subAppend(E event) {
		if (!isStarted()) {
			return;
		}
		try {
			// this step avoids LBCLASSIC-139
			if (event instanceof DeferredProcessingAware) {
				((DeferredProcessingAware) event).prepareForDeferredProcessing();
			}
			// the synchronization prevents the OutputStream from being closed while we
			// are writing. It also prevents multiple threads from entering the same
			// converter. Converters assume that they are in a synchronized block.
			// lock.lock();

			if (event instanceof LoggingEvent) {
				LoggingEvent e = (LoggingEvent) event;
				// 提取出需要输出的信息
				LogInfo logInfo = LogInfo.builder()
						// 应用名字
						.application(this.application())
						// 附加信息
						.extra(this.extra)
						// 线程名字
						.threadName(e.getThreadName())
						// 时间戳
						.timeStamp(e.getTimeStamp())
						// 解析后的时间
						.date(new Date(e.getTimeStamp()))
						// logger 名字
						.loggerName(e.getLoggerName())
						// 日志级别
						.level(e.getLevel().toString())
						// 日志消息
						.message(e.getFormattedMessage())
						// 构建
						.build();
				writeBytes(logInfo);
			}

		} catch (Exception ioe) {
			// as soon as an exception occurs, move to non-started state
			// and add a single ErrorStatus to the SM.
			this.started = false;
			addStatus(new ErrorStatus("IO failure in appender", this, ioe));
		}
	}

	private void writeBytes(LogInfo logInfo) throws IOException {
		if (null == logInfo) {
			return;
		}

		lock.lock();
		try {
			this.async.publish(this.channel(), JsonUtil.toJSONString(logInfo));
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void start() {
		int errors = 0;
		if (null == this.host || "".equals(this.host.trim())) {
			addStatus(new ErrorStatus("The address of redis database cannot be empty", this));
			errors++;
		}
		try {
			this.init();
		} catch (Exception e) {
			addStatus(new ErrorStatus("Failed to initialize log output. The reason for failure is " + e.getMessage(),
					this));
			errors++;
		}
		// only error free appenders should be activated
		if (errors == 0) {
			super.start();
		}

	}

	/**
	 * 初始化
	 */
	@SuppressWarnings("deprecation")
	private void init() {
		// <1> 创建单机连接的连接信息
		Builder builder = RedisURI.builder().withHost(this.host.trim()).withPort(this.port())
				.withTimeout(Duration.of(this.timeout(), ChronoUnit.SECONDS));
		// 设置密码
		if (StringUtils.isNotBlank(this.password)) {
			builder = builder.withPassword(this.password.trim());
		}
		// <2> 创建客户端
		this.redisClient = RedisClient.create(builder.build());
		// <3> 创建线程安全的连接
		this.connection = redisClient.connect();
		// <4> 创建同步命令
		this.async = connection.async();
	}

	@Override
	public void stop() {

		lock.lock();
		try {
			close();
			super.stop();
		} finally {
			lock.unlock();
		}

	}

	private void close() {
		try { // ... 其他操作
			if (null != this.connection) {
				connection.close(); // <5> 关闭连接
			}

			if (null != this.redisClient) {
				redisClient.shutdown(); // <6> 关闭客户端}
			}
		} catch (Exception e) {
			addStatus(new ErrorStatus(
					"Failed to close the redis connection. The reason for the failure is " + e.getMessage(), this, e));
		}

	}

	/**
	 * 设置Redis 数据库地址
	 * 
	 * @return Redis 数据库地址
	 */
	public String getHost() {
		return host;
	}

	/**
	 * 设置 Redis 数据库地址
	 * 
	 * @param host Redis 数据库地址
	 */
	public void setHost(String host) {
		this.host = host;
	}

	/**
	 * 获取设置的 Redis 数据库密码
	 * 
	 * @return Redis 数据库密码
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * 设置 Redis 数据库密码，如果redis数据没有连接密码可以不用设置
	 * 
	 * @param password Redis 数据库密码
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * 获取 Redis 数据库端口
	 * 
	 * @return Redis 数据库端口
	 */
	public Integer getPort() {
		return port;
	}

	/**
	 * 设置 Redis 数据库端口
	 * 
	 * @param port Redis 数据库端口,默认为6379
	 */
	public void setPort(Integer port) {
		this.port = port;
	}

	/**
	 * 获取Redis 连接过期时间
	 * 
	 * @return Redis 连接过期时间
	 */
	public Integer getTimeoutInSecond() {
		return timeoutInSecond;
	}

	/**
	 * 设置Redis 连接过期时间
	 * 
	 * @param timeoutInSecond Redis 连接过期时间，单位为秒
	 */
	public void setTimeoutInSecond(Integer timeoutInSecond) {
		this.timeoutInSecond = timeoutInSecond;
	}

	/**
	 * 获取Redis 订阅通道名字
	 * 
	 * @return Redis 订阅通道名字
	 */
	public String getChannel() {
		return channel;
	}

	/**
	 * 设置 Redis 订阅通道
	 * 
	 * @param channel Redis 订阅通道
	 */
	public void setChannel(String channel) {
		this.channel = channel;
	}

	/**
	 * 获取应用名称
	 * 
	 * @return 应用名称
	 */
	public String getApplication() {
		return application;
	}

	/**
	 * 设置 应用名称
	 * 
	 * @param application 应用名称
	 */
	public void setApplication(String application) {
		this.application = application;
	}

	/**
	 * 获取附加信息
	 * 
	 * @return 附加信息
	 */
	public String getExtra() {
		return extra;
	}

	/**
	 * 设置附加信息
	 * 
	 * @param extra 附加信息
	 */
	public void setExtra(String extra) {
		this.extra = extra;
	}

}
