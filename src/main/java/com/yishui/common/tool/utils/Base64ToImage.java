/**
 * 
 */
package com.yishui.common.tool.utils;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Base64Utils;

import sun.misc.BASE64Decoder;


/**
 * base64与图片转换类
 * @author yishui
 * @date 2018年7月27日
 * @Version 0.0.1
 */
public class Base64ToImage {


	public static void main(String[] args) throws Exception {
          String str="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";

          Base64ToImage.base64ToImage(str, "D:/test1.jpg");

		
	}

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
			e.printStackTrace();
		}
		// 对字节数组Base64编码


		return Base64Utils.encodeToString(data);// 返回Base64编码过的字节数组字符串
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
	public static String imageToBase64ByOnline(String imgURL) {
		ByteArrayOutputStream data = new ByteArrayOutputStream();
		try {
			// 创建URL
			URL url = new URL(imgURL);
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
			e.printStackTrace();
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

		if (StringUtils.isEmpty(imgStr)) {// 图像数据为空
			return false;
		}
		BASE64Decoder decoder = new BASE64Decoder();
		try {
			// Base64解码
			byte[] b = decoder.decodeBuffer(imgStr);
			for (int i = 0; i < b.length; ++i) {
				if (b[i] < 0) {// 调整异常数据
					b[i] += 256;
				}
			}

			OutputStream out = new FileOutputStream(imgFilePath);
			out.write(b);
			out.flush();
			out.close();

			return true;
		} catch (Exception e) {
			return false;
		}

	}


}
