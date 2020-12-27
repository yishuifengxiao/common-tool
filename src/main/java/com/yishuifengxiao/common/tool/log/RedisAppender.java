package com.yishuifengxiao.common.tool.log;

import java.io.IOException;
import java.io.OutputStream;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang3.StringUtils;

import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import ch.qos.logback.core.encoder.Encoder;
import ch.qos.logback.core.spi.DeferredProcessingAware;
import ch.qos.logback.core.status.ErrorStatus;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.RedisURI.Builder;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;

/**
 * 自定义日志输出器<br/>
 * 利用redis的订阅发布功能，将信息发布到redis中
 * 
 * @author qingteng
 * @date 2020年12月27日
 * @version 1.0.0
 * @param <E>
 */
public class RedisAppender<E> extends UnsynchronizedAppenderBase<E> {

	/**
	 * 默认的端口 6379
	 */
	private static final int DEFAULT_PORT = 6379;

	/**
	 * 默认的发布的通道的名字 logback_publish_yishuifengxiao
	 */
	private static final String DEFAULT_CHANNEL = "logback_publish_yishuifengxiao";

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
	 * It is the encoder which is ultimately responsible for writing the event to an
	 * {@link OutputStream}.
	 */
	protected Encoder<E> encoder;

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

	@Override
	protected void append(E eventObject) {
		if (!isStarted()) {
			return;
		}
		subAppend(eventObject);
	}

	@SuppressWarnings("unchecked")
	private synchronized Encoder<E> encoder() {
		if (null == this.encoder) {
			this.encoder = (Encoder<E>) new PatternLayoutEncoder();
		}
		return this.encoder;
	}

	/**
	 * 获取连接的端口
	 * 
	 * @return
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
	 * @return
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
	 * @return
	 */
	protected String channel() {
		if (null == this.channel || "".equals(this.channel.trim())) {
			return DEFAULT_CHANNEL;
		}
		return this.channel.trim();
	}

	/**
	 * Actual writing occurs here.
	 * <p>
	 * Most subclasses of <code>WriterAppender</code> will need to override this
	 * method.
	 * 
	 * @since 0.9.0
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

			byte[] byteArray = this.encoder().encode(event);
			writeBytes(byteArray);

		} catch (IOException ioe) {
			// as soon as an exception occurs, move to non-started state
			// and add a single ErrorStatus to the SM.
			this.started = false;
			addStatus(new ErrorStatus("IO failure in appender", this, ioe));
		}
	}

	private void writeBytes(byte[] byteArray) throws IOException {
		if (byteArray == null || byteArray.length == 0)
			return;

		lock.lock();
		try {
			this.async.publish(this.channel(), new String(byteArray, "utf-8"));
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void start() {
		try {
			int errors = 0;

			if (null == this.host || "".equals(this.host.trim())) {
				addStatus(new ErrorStatus("The address of redis database cannot be empty", this));
				errors++;
			}
			this.init();
			// only error free appenders should be activated
			if (errors == 0) {
				super.start();
			}
		} catch (Exception e) {
			addStatus(new ErrorStatus("Failed to initialize log output. The reason for failure is " + e.getMessage(),
					this));
		}

	}

	/**
	 * 初始化
	 */
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
			addStatus(new ErrorStatus("Could not close output stream for OutputStreamAppender.", this, e));
		}

	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public Integer getTimeoutInSecond() {
		return timeoutInSecond;
	}

	public void setTimeoutInSecond(Integer timeoutInSecond) {
		this.timeoutInSecond = timeoutInSecond;
	}

	public Encoder<E> getEncoder() {
		return encoder;
	}

	public void setEncoder(Encoder<E> encoder) {
		this.encoder = encoder;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

}
