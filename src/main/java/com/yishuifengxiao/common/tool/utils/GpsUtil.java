package com.yishuifengxiao.common.tool.utils;

/**
 * <p>GPS经纬度距离计算工具类</p>
 * <p>根据两个坐标点的经纬度计算它们之间的距离，采用Haversine公式。</p>
 * <p>特性：</p>
 * <ul>
 * <li>支持米和千米两种单位</li>
 * <li>地球半径取值为6378.137千米</li>
 * <li>返回值精确到整数</li>
 * </ul>
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
    public static long distance(double lng1, double lat1, double lng2, double lat2) {
        // 米制结果同样按四舍五入取整，避免直接截断造成 1 米内的偏差
        return Math.round(distanceRaw(lng1, lat1, lng2, lat2, EARTH_RADIUS * 1000));
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
    public static long distanceKm(double lng1, double lat1, double lng2, double lat2) {
        // 千米制结果四舍五入到整千米：不足 0.5 千米记 0，0.5 千米及以上记 1
        return Math.round(distanceRaw(lng1, lat1, lng2, lat2, EARTH_RADIUS));
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
     * 地球半径值由调用方传入：米制传 6378.137 * 1000，千米制传 6378.137
     * </p>
     *
     * @param lng1        A点经度
     * @param lat1        A点纬度
     * @param lng2        B点经度
     * @param lat2        B点纬度
     * @param earthRadius 地球半径（单位与期望的返回结果一致）
     * @return 保留 4 位小数的距离原始值（单位由 earthRadius 决定）
     */
    private static double distanceRaw(double lng1, double lat1, double lng2, double lat2, double earthRadius) {
        // 非法坐标（含 NaN/Infinity）会让结果被 Math.round 静默转为 0，必须在计算前拦截
        if (!isValidLongitude(lng1) || !isValidLatitude(lat1)
                || !isValidLongitude(lng2) || !isValidLatitude(lat2)) {
            throw new IllegalArgumentException(
                    "非法的经纬度坐标: lng1=" + lng1 + ", lat1=" + lat1 + ", lng2=" + lng2 + ", lat2=" + lat2);
        }
        double radLat1 = rad(lat1);
        double radLat2 = rad(lat2);
        double a = radLat1 - radLat2;
        double b = rad(lng1) - rad(lng2);
        double s = 2 * Math.asin(Math.sqrt(
                Math.pow(Math.sin(a / 2), 2) + Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(b / 2), 2)));
        s = s * earthRadius;
        return Math.round(s * 10000d) / 10000d;
    }

    private static double rad(double d) {
        return d * Math.PI / 180.0;
    }

    /**
     * 校验经度是否在合法范围内
     *
     * @param lng 经度
     * @return 合法返回true
     */
    private static boolean isValidLongitude(double lng) {
        return Double.isFinite(lng) && lng >= -180.0 && lng <= 180.0;
    }

    /**
     * 校验纬度是否在合法范围内
     *
     * @param lat 纬度
     * @return 合法返回true
     */
    private static boolean isValidLatitude(double lat) {
        return Double.isFinite(lat) && lat >= -90.0 && lat <= 90.0;
    }
}
