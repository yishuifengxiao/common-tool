/**
 *
 */
package com.yishuifengxiao.common.tool.io;

import com.yishuifengxiao.common.tool.exception.UncheckedException;
import com.yishuifengxiao.common.tool.exception.constant.ErrorCode;
import com.yishuifengxiao.common.tool.random.UID;
import com.yishuifengxiao.common.tool.utils.Assert;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.StreamUtils;

import javax.validation.constraints.NotNull;
import java.io.*;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.stream.Collectors;

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
public class IoUtil {

    /**
     * 获取文件的后缀名
     *
     * @param file 文件
     * @return 文件后缀名
     */
    public static String suffix(File file) {
        return null == file ? null : suffix(file.getName());
    }

    /**
     * 根据文件名获取文件的后缀名
     *
     * @param fileName 文件名
     * @return 文件后缀名 (小写格式)
     */
    public static String suffix(String fileName) {
        if (StringUtils.isBlank(fileName)) {
            return null;
        }
        String suffix = StringUtils.substringAfterLast(fileName, ".");
        return StringUtils.isNotBlank(suffix) ? suffix.toLowerCase().trim() : null;
    }

    /**
     * <p>
     * 将输入流转换为字节数组
     * </p>
     * <strong>线程安全</strong>
     *
     * @param inputStream 输入流
     * @return 转换后的字节数组
     */
    public synchronized static byte[] copyToByteArray(InputStream inputStream) {
        if (inputStream == null) {
            return new byte[0];
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream(StreamUtils.BUFFER_SIZE);
        copy(inputStream, out);
        return out.toByteArray();
    }

    /**
     * <p>
     * Copy the contents of the given InputStream to the given OutputStream. Closes
     * both streams when done.
     * </p>
     * <strong>线程安全</strong>
     *
     * @param in  the stream to copy from
     * @param out the stream to copy to
     * @return the number of bytes copied
     */
    public synchronized static int copy(InputStream in, OutputStream out) {
        try {
            int byteCount = 0;
            byte[] buffer = new byte[4096];
            int bytesRead = -1;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
                byteCount += bytesRead;
            }
            out.flush();
            return byteCount;
        } catch (Throwable e) {
            throw new UncheckedException("复制文件流时出现问题 " + e);
        } finally {
            CloseUtil.close(out, in);
        }
    }

    /**
     * <p>使用默认的编码将输入流转为字符串</p>
     * <p>默认的编码格式由<code>Charset.defaultCharset()</code>确定</p>
     * @param in 输入流
     * @param charsetName 编码格式
     * @return 转换后的字符串
     * @throws IOException 转换中发生异常
     */
    public synchronized static String inputStream2String(InputStream in, String charsetName) throws IOException {
        try (InputStreamReader reader = new InputStreamReader(in, charsetName)) {
            String result = new BufferedReader(reader).lines().collect(Collectors.joining(System.lineSeparator()));
            return result;
        }

    }

    /**
     * <p>使用默认的编码将输入流转为字符串</p>
     * <p>默认的编码格式由<code>Charset.defaultCharset()</code>确定</p>
     * @param in 输入流
     * @return 转换后的字符串
     * @throws IOException 转换中发生异常
     */
    public synchronized static String inputStream2String(InputStream in) throws IOException {
        return inputStream2String(in, Charset.defaultCharset().name());
    }

    /**
     * 将文件复制到输出流中
     * @param file 待复制的文件
     * @param out 输出流
     * @return 复制失败返回为-1
     */
    public synchronized static int copy(File file, OutputStream out) {
        if (null == file || null == out) {
            return -1;
        }
        try {
            return copy(new FileInputStream(file), out);
        } catch (Throwable e) {
            return -1;
        }

    }

    /**
     * <p>
     * 将文件输入流保存为文件
     * </p>
     * <strong>线程安全</strong>
     *
     * @param inputStream 文件输入流
     * @param file        待保存的目标文件
     * @return 保存后的文件
     */
    public synchronized static File stream2File(@NotNull InputStream inputStream, @NotNull File file) {
        try {
            copy(inputStream, new FileOutputStream(file));
        } catch (Exception e) {
            throw new UncheckedException("文件转换失败，失败的原因为 " + e);
        }
        return file;
    }

    /**
     * <p>
     * 将字符串复制到指定的文件中
     * </p>
     * <strong>线程安全</strong>
     *
     * @param text 待输入的字符串
     * @param file 目标文件
     * @return 目标文件
     */
    public synchronized static File string2File(String text, @NotNull File file) {
        if (StringUtils.isBlank(text) || null == file) {
            return file;
        }
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            bw.write(text);
            bw.flush();
        } catch (Exception e) {
            throw new UncheckedException("文件转换失败，失败的原因为 " + e);
        }
        return file;
    }

    /**
     * <p>
     * 将base64字符串转换成文件
     * </p>
     * <strong>线程安全</strong>
     *
     * @param base64File base64格式的文件
     * @return 转换后的文件
     */
    public synchronized static File base64ToFile(String base64File) {
        if (StringUtils.isBlank(base64File)) {
            throw new UncheckedException(ErrorCode.PARAM_NULL, "上传的文件内容不能为空");
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
            throw new UncheckedException("文件转换失败，失败的原因为 " + e);
        } finally {
            CloseUtil.close(bos, fos);
        }
        return file;
    }

    /**
     * <p>
     * 将文件转换成base64字符串
     * </p>
     * <strong>线程安全</strong>
     *
     * @param file 待转换的文件
     * @return 转换后的base64字符串
     */
    public synchronized static String file2Base64(File file) {
        Assert.notNull("待转换的文件不能为空", file);

        try (FileInputStream inputFile = new FileInputStream(file)) {
            byte[] buffer = new byte[(int) file.length()];
            inputFile.read(buffer);
            inputFile.close();
            return Base64.getEncoder().encodeToString(buffer);
        } catch (Exception e) {
            throw new UncheckedException(ErrorCode.DATA_CONVERT_ERROR, "文件转换失败");
        }

    }

    /**
     * <p>
     * 计算一个文件的MD5值
     * </p>
     * <strong>线程安全</strong>
     *
     * @param file 待计算的文件
     * @return 文件的MD5值(32位小写)
     */
    public synchronized static String md5(File file) {
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
