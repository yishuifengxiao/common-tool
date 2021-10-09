/**
 * 
 */
package com.yishuifengxiao.common.tool.io;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.MessageDigest;
import java.util.Base64;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;

import com.yishuifengxiao.common.tool.exception.CustomException;
import com.yishuifengxiao.common.tool.exception.constant.ErrorCode;
import com.yishuifengxiao.common.tool.random.UID;
import com.yishuifengxiao.common.tool.utils.Assert;

import lombok.extern.slf4j.Slf4j;

/**
 * 文件处理工具
 * 
 * <p>
 * <strong>该工具是一个线程安全类的工具</strong>
 * </p>
 * 
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
public class FileUtil {

	/**
	 * 将base64字符串转换成文件
	 *
	 * @param base64File base64格式的文件
	 * @return 转换后的文件
	 * @throws CustomException 转换出错
	 */
	public synchronized static File base64ToFile(String base64File) throws CustomException {
		if (StringUtils.isBlank(base64File)) {
			throw new CustomException(ErrorCode.PARAM_NULL, "上传的文件内容不能为空");
		}
		File file = new File(UID.uuid());
		// 创建文件目录
		BufferedOutputStream bos = null;
		FileOutputStream fos = null;
		try {
			byte[] bytes = Base64.getDecoder().decode(base64File);
			fos = new FileOutputStream(file);
			bos = new BufferedOutputStream(fos);
			bos.write(bytes);
			bos.flush();
		} catch (Exception e) {
			throw new CustomException(ErrorCode.DATA_CONVERT_ERROR, "文件保存失败");
		} finally {
			CloseUtil.close(bos, fos);
		}
		return file;
	}

	/**
	 * 将文件转换成base64字符串
	 * 
	 * @param file 待转换的文件
	 * @return 转换后的base64字符串
	 * @throws CustomException 转换出错
	 */
	public synchronized static String file2Base64(File file) throws CustomException {
		Assert.assertNotNull("待转换的文件不能为空", file);

		try (FileInputStream inputFile = new FileInputStream(file)) {
			byte[] buffer = new byte[(int) file.length()];
			inputFile.read(buffer);
			inputFile.close();
			return Base64.getEncoder().encodeToString(buffer);
		} catch (Exception e) {
			throw new CustomException(ErrorCode.DATA_CONVERT_ERROR, "文件转换失败");
		}

	}

	/**
	 * 计算一个文件的MD5值
	 * 
	 * @param file 待计算的文件
	 * @return 文件的MD5值(32位小写)
	 */
	public synchronized static String getMd5(File file) {
		if (null == file) {
			return null;
		}
		FileInputStream inputStream = null;
		try {
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			inputStream = new FileInputStream(file);
			byte[] buffer = new byte[8192];
			int length;
			while ((length = inputStream.read(buffer)) != -1) {
				md5.update(buffer, 0, length);
			}
			return new String(Hex.encodeHex(md5.digest()));
		} catch (Exception e) {
			log.info("计算文件{}的md5值时出现问题{}", file, e.getMessage());
			return null;
		} finally {
			CloseUtil.close(inputStream);
		}
	}

}
