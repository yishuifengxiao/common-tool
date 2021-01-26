package com.yishuifengxiao.common.tool.log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Date;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.yishuifengxiao.common.tool.encoder.DES;

import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import ch.qos.logback.core.spi.DeferredProcessingAware;
import ch.qos.logback.core.status.ErrorStatus;

/**
 * <p>
 * 自定义组播日志输出器
 * </p>
 * <p>
 * 该工具是logback日志里的一个自定义日志输出器，主要是利用组播将加密后的日志信息发送到当前网络中，以便其他终端能够接收到日志信息
 * </p>
 * 使用方法如下:
 * 
 * <pre>
 * {@code
 * <appender name="redis" class=com.yishuifengxiao.common.tool.log.MulticastSocketAppender"> 
 * 	<host>组播地址,必须为一个D类网络地址(224.0.0.1—239.255.255.254),默认地址为 228.125.125.125</host>
 * 	<port>组播端口，默认为60379</port> 
 *	<secure>是否对组播的日志内容进行加密，true表示加密，false不加密</secure>
 * 	<password>加密时的加密密钥</password>
 * 	<application>项目名字，可选，如 测试项目</application> 
 *	<extra>附加数据，会被加载在日志中</extra> 
 * </appender>
 * 
 * }
 * </pre>
 * 
 * 接收组播的方法如下:
 * 
 * <pre>
 * <code>
 * 	
 * 
 * 	// 接受组播和发送组播的数据报服务都要把组播地址添加进来
 * 	String host = "225.0.0.1";// 多播地址
 * 	int port = 9998;
 * 	int length = 1024;
 * 	byte[] buf = new byte[length];
 * 	MulticastSocket ms = null;
 * 	DatagramPacket dp = null;
 * 	StringBuffer sbuf = new StringBuffer();
 * 
 * 	ms = new MulticastSocket(port);
 * 	dp = new DatagramPacket(buf, length);
 * 
 * 	// 加入多播地址
 * 	InetAddress group = InetAddress.getByName(host);
 * 	ms.joinGroup(group);
 * 
 * 	System.out.println("监听多播端口打开：");
 * 
 * </code>
 * </pre>
 * 
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 * @param <E> 数据类型
 */
public class MulticastSocketAppender<E> extends UnsynchronizedAppenderBase<E> {

	/**
	 * 默认的端口 60379
	 */
	public static final int DEFAULT_PORT = 60379;

	/**
	 * 默认的组播地址
	 */
	public static final String DEFAULT_HOST = "228.125.125.125";

	/**
	 * All synchronization in this class is done via the lock object.
	 */
	protected final ReentrantLock lock = new ReentrantLock(false);

	private MulticastSocket multicastSocket;

	private InetAddress group;

	/**
	 * 组播的地址
	 */
	private String host;

	/**
	 * 组播的内容的加密密钥
	 */
	private String password;

	/**
	 * 组播端口，默认为60379
	 */
	private Integer port;

	/**
	 * 应用名称
	 */
	private String application;

	/**
	 * 附加信息
	 */
	private String extra;

	/**
	 * 是否以加密方式组播，默认为true
	 */
	private Boolean secure;

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
	 * 是否以加密方式组播，默认为true
	 * 
	 * @return true表示对内容进行加密
	 */
	protected boolean secure() {

		return BooleanUtils.isNotFalse(this.secure);

	}

	/**
	 * 获取组播地址
	 * 
	 * @return 组播地址
	 */
	protected String host() {
		if (StringUtils.isNotBlank(this.host)) {
			return this.host.trim();

		}
		return DEFAULT_HOST;

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
			String msg = JSONObject.toJSONString(logInfo);
			if (this.secure()) {
				msg = DES.encrypt(this.password, msg);
			}
			DatagramPacket dp = new DatagramPacket(msg.getBytes(), msg.length(), this.group, this.port());
			this.multicastSocket.send(dp);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void start() {
		int errors = 0;
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
	 * 
	 * @throws IOException
	 */
	private void init() throws IOException {

		InetAddress group = InetAddress.getByName(this.host());
		MulticastSocket multicastSocket = new MulticastSocket();
		// 加入多播组
		multicastSocket.joinGroup(group);
		this.group = group;
		this.multicastSocket = multicastSocket;
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
		try {
			if (null != this.multicastSocket) {
				this.multicastSocket.close();
				this.multicastSocket = null;
			}
		} catch (Exception e) {
			addStatus(new ErrorStatus("IO failure in appender", this, e));
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

	public String getApplication() {
		return application;
	}

	public void setApplication(String application) {
		this.application = application;
	}

	public Boolean getSecure() {
		return secure;
	}

	public void setSecure(Boolean secure) {
		this.secure = secure;
	}

	public String getExtra() {
		return extra;
	}

	public void setExtra(String extra) {
		this.extra = extra;
	}

}
