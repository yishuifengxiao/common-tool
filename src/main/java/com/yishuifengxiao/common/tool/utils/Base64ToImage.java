package com.yishuifengxiao.common.tool.utils;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Base64Utils;

/**
 * base64与图片转换类
 * 
 * @author yishui
 * @date 2018年7月27日
 * @Version 0.0.1
 */
public final class Base64ToImage {
	private final static Logger log = LoggerFactory.getLogger(Base64ToImage.class);

	/**
	 * 本地图片转换成base64字符串
	 * 
	 * @param imgFile
	 *            图片本地路径
	 * @return
	 * 
	 * @author ZHANGJL
	 * @dateTime 2018-02-23 14:40:46
	 */
	public static String imageToBase64ByLocal(String imgFile) {// 将图片文件转化为字节数组字符串，并对其进行Base64编码处理

		InputStream in = null;
		byte[] data = null;

		// 读取图片字节数组
		try {
			in = new FileInputStream(imgFile);

			data = new byte[in.available()];
			in.read(data);
			in.close();
		} catch (IOException e) {
			log.info("本地图片转换成base64字符串发生问题，出现问题的原因为 {}", e.getMessage());
		}
		// 对字节数组Base64编码

		// 返回Base64编码过的字节数组字符串
		return Base64Utils.encodeToString(data);
	}

	/**
	 * 在线图片转换成base64字符串
	 * 
	 * @param imgURL
	 *            图片线上路径
	 * @return
	 * 
	 * @author ZHANGJL
	 * @dateTime 2018-02-23 14:43:18
	 */
	public static String imageToBase64ByOnline(String imgUrl) {
		ByteArrayOutputStream data = new ByteArrayOutputStream();
		try {
			// 创建URL
			URL url = new URL(imgUrl);
			byte[] by = new byte[1024];
			// 创建链接
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setConnectTimeout(5000);
			InputStream is = conn.getInputStream();
			// 将内容读取内存中
			int len = -1;
			while ((len = is.read(by)) != -1) {
				data.write(by, 0, len);
			}
			// 关闭流
			is.close();
		} catch (IOException e) {
			log.info("在线图片转换成base64字符串发生问题，出现问题的原因为 {}", e.getMessage());
		}
		// 对字节数组Base64编码
		return Base64Utils.encodeToString(data.toByteArray());
	}

	/**
	 * base64字符串转换成图片
	 * 
	 * @param imgStr
	 *            base64字符串
	 * @param imgFilePath
	 *            图片存放路径
	 * @return
	 * 
	 * @author ZHANGJL
	 * @dateTime 2018-02-23 14:42:17
	 */
	public static synchronized boolean base64ToImage(String imgStr, String imgFilePath) { // 对字节数组字符串进行Base64解码并生成图片

		if (StringUtils.isEmpty(imgStr)) {
			// 图像数据为空
			return false;
		}
		try {
			// Base64解码
			byte[] b = Base64Utils.decodeFromString(imgStr);
			for (int i = 0; i < b.length; ++i) {
				if (b[i] < 0) {
					// 调整异常数据
					b[i] += 256;
				}
			}

			OutputStream out = new FileOutputStream(imgFilePath);
			out.write(b);
			out.flush();
			out.close();

			return true;
		} catch (Exception e) {
			log.info("在base64字符串转换成图片发生问题，出现问题的原因为 {}", e.getMessage());
			return false;
		}

	}

}
