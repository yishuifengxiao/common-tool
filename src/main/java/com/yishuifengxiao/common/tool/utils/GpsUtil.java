package com.yishuifengxiao.common.tool.utils;

/**
 * <p>
 * 经纬度距离计算工具
 * </p>
 *
 * <p>
 * 根据两个坐标值计算出两个坐标点之间的距离
 * </p>
 * <p>
 * 算法来源 https://www.cnblogs.com/zhoug2020/p/8993750.html
 * </p>
 * <p>
 * <strong>该工具是一个线程安全类的工具。</strong>
 * </p>
 *
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
public final class GpsUtil {
	/**
	 * 地球半径，6378.137千米
	 */
	private static final double EARTH_RADIUS = 6378.137;

	/**
	 * <p>
	 * lng1 lat1 表示A点经纬度，lng2 lat2 表示B点经纬度
	 * </p>
	 * <p>
	 * a=lat1 – lat2 为两点纬度之差
	 * </p>
	 * <p>
	 * b=lng1 -lng2 为两点经度之差
	 * </p>
	 * <p>
	 * 地球半径值这里取值为 6378.137千米；
	 * </p>
	 * <p>
	 * 通过经纬度计算出来的结果单位为米
	 *
	 * @param lng1 A点经度
	 * @param lat1 A点纬度
	 * @param lng2 B点经度
	 * @param lat2 B点纬度
	 * @return 通过经纬度获取的距离(单位 ： 米)
	 */
	public static synchronized long distance(double lng1, double lat1, double lng2, double lat2) {
		return getDistance(lng1, lat1, lng2, lat2, EARTH_RADIUS * 1000);
	}

	/**
	 * <p>
	 * lng1 lat1 表示A点经纬度，lng2 lat2 表示B点经纬度
	 * </p>
	 * <p>
	 * a=lat1 – lat2 为两点纬度之差
	 * </p>
	 * <p>
	 * b=lng1 -lng2 为两点经度之差
	 * </p>
	 * <p>
	 * 地球半径值这里取值为 6378.137千米；
	 * </p>
	 * 通过经纬度计算出来的结果单位为千米
	 *
	 * @param lng1 A点经度
	 * @param lat1 A点纬度
	 * @param lng2 B点经度
	 * @param lat2 B点纬度
	 * @return 通过经纬度获取的距离(单位 ： 千米)
	 */
	public static synchronized long distanceKm(double lng1, double lat1, double lng2, double lat2) {

		return getDistance(lng1, lat1, lng2, lat2, EARTH_RADIUS);
	}

	/**
	 * <p>
	 * lng1 lat1 表示A点经纬度，lng2 lat2 表示B点经纬度
	 * </p>
	 * <p>
	 * a=lat1 – lat2 为两点纬度之差
	 * </p>
	 * <p>
	 * b=lng1 -lng2 为两点经度之差
	 * </p>
	 * <p>
	 * 地球半径值这里取值为 6378.137千米；
	 * </p>
	 * <p>
	 * 通过经纬度计算出来的结果单位为米
	 *
	 * @param lng1 A点经度
	 * @param lat1 A点纬度
	 * @param lng2 B点经度
	 * @param lat2 B点纬度
	 * @return 通过经纬度获取的距离(单位 ： 米)
	 */
	@SuppressWarnings("removal")
	private static long getDistance(double lng1, double lat1, double lng2, double lat2, double earthRadius) {
		double radLat1 = rad(lat1);
		double radLat2 = rad(lat2);
		double a = radLat1 - radLat2;
		double b = rad(lng1) - rad(lng2);
		double s = 2 * Math.asin(Math.sqrt(
				Math.pow(Math.sin(a / 2), 2) + Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(b / 2), 2)));
		s = s * earthRadius;
		s = Math.round(s * 10000d) / 10000d;
		// s = s*1000; 乘以1000是换算成米
		return new Double(s).longValue();
	}

	private static double rad(double d) {
		return d * Math.PI / 180.0;
	}
}
