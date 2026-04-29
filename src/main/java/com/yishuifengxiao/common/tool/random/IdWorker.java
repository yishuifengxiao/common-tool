package com.yishuifengxiao.common.tool.random;

import java.util.UUID;

/**
 * <p>雪花算法(SnowFlake)ID生成器</p>
 * <p>分布式系统中生成唯一ID的算法，具有高并发、高性能、全局唯一的特点。</p>
 * <p>SnowFlake ID结构（64位Long型）：</p>
 * <ul>
 * <li>1位符号位：固定为0（正数）</li>
 * <li>41位时间戳：毫秒级，可使用约69年</li>
 * <li>5位数据中心ID：支持32个数据中心</li>
 * <li>5位工作机器ID：支持32个节点</li>
 * <li>12位序列号：毫秒内可生成4096个ID</li>
 * </ul>
 * <p>优点：</p>
 * <ul>
 * <li>ID按时间自增排序</li>
 * <li>分布式系统内无ID碰撞</li>
 * <li>高性能，每秒可生成约26万ID</li>
 * </ul>
 *
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
public class IdWorker {

    // ==============================Fields===========================================
    /**
     * 开始时间截 (2015-01-01)
     */
    private final long twepoch = 1420041600000L;

    /**
     * 机器id所占的位数
     */
    private final long workerIdBits = 5L;

    /**
     * 数据标识id所占的位数
     */
    private final long datacenterIdBits = 5L;

    /**
     * 支持的最大机器id，结果是31 (这个移位算法可以很快的计算出几位二进制数所能表示的最大十进制数)
     */
    private final long maxWorkerId = -1L ^ (-1L << workerIdBits);

    /**
     * 支持的最大数据标识id，结果是31
     */
    private final long maxDatacenterId = -1L ^ (-1L << datacenterIdBits);

    /**
     * 序列在id中占的位数
     */
    private final long sequenceBits = 12L;

    /**
     * 机器ID向左移12位
     */
    private final long workerIdShift = sequenceBits;

    /**
     * 数据标识id向左移17位(12+5)
     */
    private final long datacenterIdShift = sequenceBits + workerIdBits;

    /**
     * 时间截向左移22位(5+5+12)
     */
    private final long timestampLeftShift = sequenceBits + workerIdBits + datacenterIdBits;

    /**
     * 生成序列的掩码，这里为4095 (0b111111111111=0xfff=4095)
     */
    private final long sequenceMask = -1L ^ (-1L << sequenceBits);

    /**
     * 工作机器ID(0~31)
     */
    private long workerId;

    /**
     * 数据中心ID(0~31)
     */
    private long datacenterId;

    /**
     * 毫秒内序列(0~4095)
     */
    private long sequence = 0L;

    /**
     * 上次生成ID的时间截
     */
    private long lastTimestamp = -1L;

    /**
     * 默认的生成器
     */
    private final static IdWorker WORKER = new IdWorker(30, 30);

    // ==============================Constructors=====================================

    /**
     * 构造函数
     *
     * @param workerId     工作ID (0~31)
     * @param datacenterId 数据中心ID (0~31)
     */
    public IdWorker(long workerId, long datacenterId) {
        if (workerId > maxWorkerId || workerId < 0) {
            throw new IllegalArgumentException(
                    String.format("worker Id can't be greater than %d or less than 0",
                            maxWorkerId));
        }
        if (datacenterId > maxDatacenterId || datacenterId < 0) {
            throw new IllegalArgumentException(
                    String.format("datacenter Id can't be greater than %d or less than 0",
                            maxDatacenterId));
        }
        this.workerId = workerId;
        this.datacenterId = datacenterId;
    }

    // ==============================Methods==========================================

    /**
     * <p>
     * 获得下一个ID (该方法是线程安全的)
     * </p>
     * <p>
     * <strong>线程安全</strong>
     * </p>
     *
     * @return SnowflakeId
     */
    public synchronized long nextId() {
        long timestamp = timeGen();

        // 如果当前时间小于上一次ID生成的时间戳，说明系统时钟回退过这个时候应当抛出异常
        if (timestamp < lastTimestamp) {
            throw new RuntimeException(String.format(
                    "Clock moved backwards.  Refusing to generate id for %d milliseconds",
                    lastTimestamp - timestamp));
        }

        // 如果是同一时间生成的，则进行毫秒内序列
        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & sequenceMask;
            // 毫秒内序列溢出
            if (sequence == 0) {
                // 阻塞到下一个毫秒,获得新的时间戳
                timestamp = tilNextMillis(lastTimestamp);
            }
        }
        // 时间戳改变，毫秒内序列重置
        else {
            sequence = 0L;
        }

        // 上次生成ID的时间截
        lastTimestamp = timestamp;

        // 移位并通过或运算拼到一起组成64位的ID
        return ((timestamp - twepoch) << timestampLeftShift) | (datacenterId << datacenterIdShift)
                | (workerId << workerIdShift) | sequence;
    }

    /**
     * <p>
     * 使用默认的参数生成一个雪花id
     * </p>
     * <p>
     * 生成的雪花id的形式为 922847180031385619
     * </p>
     * <p>
     * <strong>线程安全</strong>
     * </p>
     *
     * @return 雪花id
     */
    public static long snowflakeId() {
        return WORKER.nextId();
    }

    /**
     * <p>
     * 使用默认的参数生成一个雪花id
     * </p>
     * <p>
     * 生成的雪花id的形式为 922847180031385619
     * </p>
     * <p>
     * <strong>线程安全</strong>
     * </p>
     *
     * @return 雪花id
     */
    public static String snowflakeStringId() {
        return WORKER.nextId() + "";
    }

    /**
     * 阻塞到下一个毫秒，直到获得新的时间戳
     *
     * @param lastTimestamp 上次生成ID的时间截
     * @return 当前时间戳
     */
    protected long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    /**
     * 返回以毫秒为单位的当前时间
     *
     * @return 当前时间(毫秒)
     */
    protected long timeGen() {
        return System.currentTimeMillis();
    }

    /**
     * <p>生成UUID</p>
     *
     * @return 去掉-后的UUID
     */
    public synchronized static String uuid() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }
}