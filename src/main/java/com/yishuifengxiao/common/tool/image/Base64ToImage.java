package com.yishuifengxiao.common.tool.image;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Base64Utils;

import com.yishuifengxiao.common.tool.constant.ErrorCode;
import com.yishuifengxiao.common.tool.exception.CustomException;
import com.yishuifengxiao.common.tool.io.CloseUtil;

/**
 * base64与图片转换工具
 * 
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
public final class Base64ToImage {
	/**
	 * 将 本地图片转换成base64字符串
	 * 
	 * @param imgFile 本地图片的地址
	 * @return base64字符串
	 * @throws CustomException 转换时出现问题
	 */
	public static synchronized String imageToBase64ByLocal(String imgFile) throws CustomException {

		if (StringUtils.isBlank(imgFile)) {
			throw new CustomException(ErrorCode.PARAM_NULL, "本地图片的地址不能为空");
		}

		if (!new File(imgFile).exists()) {
			throw new CustomException(ErrorCode.PARAM_NULL, "本地图片不存在");
		}
		InputStream inputStream = null;
		byte[] data = null;
		// 读取图片字节数组
		try {
			inputStream = new FileInputStream(imgFile);
			data = new byte[inputStream.available()];
			inputStream.read(data);
		} catch (IOException e) {
			throw new CustomException(ErrorCode.PARSE_ERROR, e.getMessage());
		} finally {
			CloseUtil.close(inputStream);
		}
		// 返回Base64编码过的字节数组字符串
		return Base64Utils.encodeToString(data);
	}

	/**
	 * 将base64字符串转换成图片
	 * 
	 * @param imgBase64Str 图片 的base64字符串
	 * @param imagePath    图片存放路径
	 * @throws CustomException 转换时出现问题
	 */
	public static synchronized void base64ToImage(String imgBase64Str, String imagePath) throws CustomException {
		if (!StringUtils.isNoneBlank(imgBase64Str, imagePath)) {
			throw new CustomException(ErrorCode.PARAM_NULL, "输入参数错误");
		}
		OutputStream out = null;
		try {
			// Base64解码
			byte[] b = Base64Utils.decodeFromString(imgBase64Str);
			for (int i = 0; i < b.length; ++i) {
				if (b[i] < 0) {
					// 调整异常数据
					b[i] += 256;
				}
			}
			out = new FileOutputStream(imagePath);
			out.write(b);
			out.flush();

		} catch (Exception e) {
			throw new CustomException(ErrorCode.PARSE_ERROR, e.getMessage());
		} finally {
			CloseUtil.close(out);
		}
	}

}
