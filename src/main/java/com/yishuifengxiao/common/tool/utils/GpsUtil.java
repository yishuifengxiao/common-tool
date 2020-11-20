package com.yishuifengxiao.common.tool.utils;

/**
 * 经纬度距离计算工具<br/><br/>
 * 根据两个坐标值计算出两个坐标点之间的距离
 * <br/>
 * 算法来源 https://www.cnblogs.com/zhoug2020/p/8993750.html
 * 
 * @author yishui
 * @version 1.0.0
 * @date 2020-10-23
 */
public class GpsUtil {
	/**
	 * 地球半径，单位千米
	 */
	private static double EARTH_RADIUS = 6378.137;

	/**
	 * Lat1 Lung1 表示A点经纬度，Lat2 Lung2 表示B点经纬度； a=Lat1 – Lat2 为两点纬度之差 b=Lung1 -Lung2
	 * 为两点经度之差； 6378.137为地球半径，单位为千米； 计算出来的结果单位为千米。 通过经纬度获取距离(单位：千米)
	 *
	 * @param lng1 A点经度
	 * @param lat1 A点纬度
	 * @param lng2 B点经度
	 * @param lat2 B点纬度
	 * @return 通过经纬度获取的距离(单位 ： 千米)
	 */
	public synchronized static Double getDistance(double lng1, double lat1, double lng2, double lat2) {
		double radLat1 = rad(lat1);
		double radLat2 = rad(lat2);
		double a = radLat1 - radLat2;
		double b = rad(lng1) - rad(lng2);
		double s = 2 * Math.asin(Math.sqrt(
				Math.pow(Math.sin(a / 2), 2) + Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(b / 2), 2)));
		s = s * EARTH_RADIUS;
		s = Math.round(s * 10000d) / 10000d;
		// s = s*1000; 乘以1000是换算成米
		return new Double(s);
	}

	private static double rad(double d) {
		return d * Math.PI / 180.0;
	}
}
