package com.yishuifengxiao.common.tool.image;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Base64Utils;

import com.yishuifengxiao.common.tool.exception.CustomException;

/**
 * base64与图片转换类
 * 
 * @author yishui
 * @date 2018年7月27日
 * @Version 0.0.1
 */
public final class Base64ToImage {
	/**
	 * 将 本地图片转换成base64字符串
	 * 
	 * @param imgFile 本地图片的地址
	 * @return
	 * @throws CustomException
	 */
	public static synchronized String imageToBase64ByLocal(String imgFile) throws CustomException {

		if (StringUtils.isBlank(imgFile)) {
			throw new CustomException("本地图片的地址不能为空");
		}

		if (!new File(imgFile).exists()) {
			throw new CustomException("本地图片不存在");
		}

		byte[] data = null;
		// 读取图片字节数组
		try {
			InputStream inputStream = new FileInputStream(imgFile);
			data = new byte[inputStream.available()];
			inputStream.read(data);
			inputStream.close();
			inputStream = null;
		} catch (IOException e) {
			throw new CustomException(e.getMessage());
		}
		// 返回Base64编码过的字节数组字符串
		return Base64Utils.encodeToString(data);
	}

	/**
	 * 将base64字符串转换成图片
	 * 
	 * @param imgBase64Str 图片 的base64字符串
	 * @param imagePath    图片存放路径
	 * @throws CustomException
	 */
	public static synchronized void base64ToImage(String imgBase64Str, String imagePath) throws CustomException {
		if (!StringUtils.isNoneBlank(imgBase64Str, imagePath)) {
			throw new CustomException("输入参数错误");
		}
		try {
			// Base64解码
			byte[] b = Base64Utils.decodeFromString(imgBase64Str);
			for (int i = 0; i < b.length; ++i) {
				if (b[i] < 0) {
					// 调整异常数据
					b[i] += 256;
				}
			}
			OutputStream out = new FileOutputStream(imagePath);
			out.write(b);
			out.flush();
			out.close();
			out = null;
		} catch (Exception e) {
			throw new CustomException(e.getMessage());
		}
	}

}
