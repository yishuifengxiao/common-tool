package com.yishuifengxiao.common.tool.image;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Base64;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Base64Utils;

import com.yishuifengxiao.common.tool.constant.ErrorCode;
import com.yishuifengxiao.common.tool.exception.CustomException;
import com.yishuifengxiao.common.tool.io.CloseUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * base64与图片转换工具
 * </p>
 * <p>
 * 该工具的主要作用是实现图片与base64字符串之间的互相转换.
 * </p>
 * <strong>该工具是一个线程安全类的工具。</strong>
 * 
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
public final class ImageUtil {

	/**
	 * png格式图片base64编码之后的前缀
	 */
	public final static String BASE64_PNG_PREFIX = "data:image/png;base64,";

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

	/**
	 * 将图片转换成base64字符串
	 * 
	 * @param image 需要转换的图片
	 * @return 转换后的字符串
	 */
	public static synchronized String image2Base64(BufferedImage image) {
		if (null == image) {
			return null;
		}
		String base64 = null;
		try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
			// 输出流
			ImageIO.write(image, "png", stream);
			base64 = Base64.getEncoder().encodeToString(stream.toByteArray());
			stream.flush();

		} catch (Exception e) {
			log.info("将图片转换成base64时出现问题，出现问题的原因为 {}", e.getMessage());
		}
		return base64;

	}

	/**
	 * 将图片转换成包含png格式信息的base64字符串
	 * 
	 * @param image 需要转换的图片
	 * @return 转换后的字符串
	 */
	public static synchronized String image2Base64Png(BufferedImage image) {
		if (null == image) {
			return null;
		}

		return new StringBuffer(BASE64_PNG_PREFIX).append(image2Base64(image)).toString();

	}

}
