package com.yishuifengxiao.common.tool.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * 网络工具类
 *
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
public class NetUtil {

    /**
     * 获取网络接口信息列表
     * 该方法遍历所有网络接口，收集每个活动接口的网络信息，
     * 包括网卡名称、索引、MAC地址、IP地址等信息
     *
     * @return 网络接口信息列表，包含所有活动的非回环网络接口信息
     */
    public static List<NetworkInfo> getNetworkInfo() {
        List<NetworkInfo> list = new ArrayList<>();
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

            // 遍历所有网络接口
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();

                // 跳过非活动接口
                if (!networkInterface.isUp()) {
                    continue;
                }
                String macAddress = getMacAddress(networkInterface);
                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                boolean hasNonLoopback = false;

                // 遍历当前网络接口的所有IP地址
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();

                    if (!address.isLoopbackAddress()) {
                        if (!hasNonLoopback) {
                            hasNonLoopback = true;
                        }
                        list.add(new NetworkInfo(networkInterface.getDisplayName(), networkInterface.getIndex(), macAddress, address.getHostAddress(), (address instanceof Inet4Address)));
                    }
                }
            }
        } catch (SocketException e) {
            log.warn("获取网卡信息失败", e);
        }
        return list;
    }


    /**
     * 获取网络接口的MAC地址
     * 该方法通过NetworkInterface对象获取对应网卡的硬件地址(MAC地址)，
     * 并将其格式化为标准的十六进制字符串表示形式(如: 08:00:27:DC:1F:46)
     *
     * @param networkInterface 网络接口对象
     * @return 格式化的MAC地址字符串，格式为"XX:XX:XX:XX:XX:XX"，获取失败时返回空字符串
     */
    public static String getMacAddress(NetworkInterface networkInterface) {
        try {
            byte[] mac = networkInterface.getHardwareAddress();
            if (mac == null) return "";

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < mac.length; i++) {
                sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? ":" : ""));
            }
            return sb.toString();
        } catch (SocketException e) {
            log.warn("获取网卡信息失败", e);
            return "";
        }
    }

    /**
     * 获取外网IP地址
     * 该方法通过访问亚马逊AWS提供的IP查询服务(http://checkip.amazonaws.com)来获取当前设备的公网IP地址
     *
     * @return 外网IP地址字符串，获取失败时返回空字符串
     */
    public static String getOutIp() {
        try {
            URL url = new URL("https://checkip.amazonaws.com");
            try (BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()))) {
                String ip = in.readLine();
                return ip;
            }
        } catch (Exception e) {
            // 记录异常信息，便于问题排查
            log.warn("获取外网IP地址失败", e);
        }
        return "";
    }


    public static void main(String[] args) {
        List<NetworkInfo> networkInfo = getNetworkInfo();
        System.out.println(networkInfo);
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Accessors(chain = true)
    public static class NetworkInfo implements Serializable {

        /**
         * 网卡名称
         */
        private String displayname;
        /**
         * 网卡索引
         */
        private Integer index;
        /**
         * MAC地址
         */
        private String macAddress;

        /**
         * IP地址
         */
        private String ip;
        /**
         * 是否为IPv4地址
         */
        private boolean isIpv4;
    }
}