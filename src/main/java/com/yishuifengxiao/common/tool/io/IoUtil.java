/**
 *
 */
package com.yishuifengxiao.common.tool.io;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Base64;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;

import com.yishuifengxiao.common.tool.exception.UncheckedException;
import com.yishuifengxiao.common.tool.random.IdWorker;

import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>文件处理工具类</p>
 * <p>提供文件读写、流操作、Base64转换等IO相关功能。</p>
 * <p>特性：</p>
 * <ul>
 * <li>文件与字节数组、字符串之间的转换</li>
 * <li>输入流与输出流的复制操作</li>
 * <li>classpath资源读取</li>
 * <li>Base64编码与文件的互相转换</li>
 * </ul>
 *
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
public class IoUtil {
    private static final int DEFAULT_BUFFER_SIZE = 4096;

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
        return StringUtils.isNotBlank(suffix) ? suffix.toLowerCase(Locale.ROOT).trim() : null;
    }

    /**
     * <p>
     * 将文件转换为字节数组
     * </p>
     *
     * @param file 待转换的文件
     * @return 转换后的字节数组
     * @throws IOException 转换时出现问题
     */
    public static byte[] fileToByteArray(File file) throws IOException {
        try (FileInputStream inputStream = new FileInputStream(file)) {
            return inputStreamToByteArray(inputStream);
        }
    }

    /**
     * <p>
     * 将输入流转换为字节数组
     * </p>
     *
     * @param inputStream 输入流
     * @return 转换后的字节数组
     * @throws IOException 转换中发生异常
     */
    public static byte[] inputStreamToByteArray(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            return new byte[0];
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream(DEFAULT_BUFFER_SIZE);
        copy(inputStream, out);
        return out.toByteArray();
    }

    /**
     * <p>
     * 使用指定的编码将输入流转为字符串
     * </p>
     *
     * @param in          输入流
     * @param charsetName 编码格式
     * @return 转换后的字符串
     * @throws IOException 转换中发生异常
     */
    public static String inputStreamToString(InputStream in, String charsetName) throws IOException {
        if (null == in) {
            return null;
        }
        try {
            // 一次性读取全部字节，避免 BufferedReader.lines() 改写原始换行符
            byte[] bytes = in.readAllBytes();
            return new String(bytes, Charset.forName(charsetName));
        } finally {
            CloseUtil.close(in);
        }
    }

    /**
     * <p>
     * 使用UTF-8编码将输入流转为字符串
     * </p>
     *
     * @param in 输入流
     * @return 转换后的字符串
     * @throws IOException 转换中发生异常
     */
    public static String inputStreamToString(InputStream in) throws IOException {
        return inputStreamToString(in, StandardCharsets.UTF_8.name());
    }

    /**
     * <p>
     * 将文件输入流保存为文件
     * </p>
     *
     * @param inputStream 文件输入流
     * @param file        待保存的目标文件
     * @return 保存后的文件
     * @throws IOException 转换时出现问题
     */
    public static File inputStreamToFile(@NotNull InputStream inputStream, @NotNull File file) throws IOException {
        copy(inputStream, new FileOutputStream(file));
        return file;
    }

    /**
     * <p>
     * 读取classpath目录下的资源文本
     * </p>
     * <p style="color:yellow">
     * 这里使用了UTF-8编码
     * </p>
     * <p>
     * 返回用于读取指定资源的输入流文本,getResource（String）文档中描述了搜索顺序。
     * 命名模块中的资源受Module.getResourceAsStream指定的封装规则约束。此外，除了资源的名称以“.class”结尾的特殊情况外，此方法仅在无条件打开包时在命名模块的包中查找资源。
     * </p>
     *
     * @param clazz 命名模块中的资源所在的类
     * @param name  The resource name
     * @param <T>   元素类型
     * @return 对应的编码文本
     * @throws IOException 读取时出现问题
     */
    public static <T> String readResourceAsString(Class<T> clazz, String name) throws IOException {
        return readResourceAsString(clazz, name, StandardCharsets.UTF_8.name());
    }

    /**
     * <p>
     * 读取classpath目录下的资源文本
     * </p>
     * <p>
     * 返回用于读取指定资源的输入流文本,getResource（String）文档中描述了搜索顺序。
     * 命名模块中的资源受Module.getResourceAsStream指定的封装规则约束。此外，除了资源的名称以“.class”结尾的特殊情况外，此方法仅在无条件打开包时在命名模块的包中查找资源。
     * </p>
     *
     * @param clazz       命名模块中的资源所在的类
     * @param name        The resource name
     * @param charsetName 字符串编码
     * @param <T>         元素类型
     * @return 对应的编码文本
     * @throws IOException 读取时出现问题
     */
    public static <T> String readResourceAsString(Class<T> clazz, String name, String charsetName) throws IOException {
        InputStream inputStream = clazz.getClassLoader().getResourceAsStream(name);
        if (null == inputStream) {
            throw new UncheckedException("资源不存在: " + name);
        }
        return inputStreamToString(inputStream, charsetName);
    }

    /**
     * 将文件复制到输出流中
     *
     * @param file 待复制的文件
     * @param out  输出流
     * @return 复制失败返回为-1
     */
    public static long copy(File file, OutputStream out) {
        if (null == file || null == out) {
            return -1;
        }
        try {
            return copy(new FileInputStream(file), out);
        } catch (Throwable e) {
            log.warn("复制文件到输出流失败，文件：{}", file.getAbsolutePath(), e);
            return -1;
        }

    }


    /**
     * 将输入流的数据复制到输出流中
     * <p>
     * 注意：复制完成后会关闭入参输入流和输出流。
     * </p>
     *
     * @param in  输入流，提供要复制的数据
     * @param out 输出流，接收复制的数据
     * @return 返回复制的字节总数
     * @throws IOException 当读取或写入过程中发生I/O错误时抛出
     */
    public static long copy(InputStream in, OutputStream out) throws IOException {
        try {
            long byteCount = 0;
            // 使用固定缓冲区大小，避免 available() 在部分流上返回0或不可靠值
            byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
            int bytesRead = -1;
            // 循环读取输入流数据并写入输出流，直到读取完毕
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
                byteCount += bytesRead;
            }
            out.flush();
            return byteCount;
        } finally {
            CloseUtil.close(out, in);
        }
    }


    /**
     * 将文本内容写入指定文件
     *
     * @param text 要写入的文本内容，如果为空或空白字符则不执行写入操作
     * @param file 目标文件对象，不能为null
     * @return 返回写入的文件对象
     * @throws IOException 当文件写入过程中发生IO异常时抛出
     */
    public static File writeToFile(String text, @NotNull File file) throws IOException {
        if (StringUtils.isBlank(text) || null == file) {
            return file;
        }
        // 使用UTF-8编码的缓冲写入器将文本写入文件，避免依赖平台默认字符集
        try (BufferedWriter bw = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
            bw.write(text);
            bw.flush();
        }
        return file;
    }


    /**
     * 将指定文件的内容读取为字符串
     *
     * @param file 要读取的文件，不能为空
     * @return 文件内容的字符串表示
     * @throws IOException 当文件读取过程中发生IO异常时抛出
     */
    public static String readFileAsString(@NotNull File file) throws IOException {
        return inputStreamToString(new FileInputStream(file), StandardCharsets.UTF_8.name());
    }


    /**
     * 将文件内容读取为字符串
     *
     * @param file        文件对象，不能为空
     * @param charsetName 字符集名称，用于指定文件内容的编码格式
     * @return 返回文件内容的字符串表示
     * @throws IOException 当文件读取过程中发生IO异常时抛出
     */
    public static String readFileAsString(@NotNull File file, String charsetName) throws IOException {
        return inputStreamToString(new FileInputStream(file), charsetName);
    }


    /**
     * 将Base64编码的文件数据转换为临时文件
     *
     * @param base64File Base64编码的文件数据字符串
     * @return 转换后的文件对象
     */
    public static File base64ToFile(String base64File) {
        File file;
        try {
            file = File.createTempFile("tool-base64-", ".tmp");
            file.deleteOnExit();
        } catch (IOException e) {
            throw new UncheckedException(e);
        }
        // 创建文件目录
        BufferedOutputStream bos = null;
        FileOutputStream fos = null;
        try {
            // 解码Base64数据并写入文件
            byte[] bytes = Base64.getDecoder().decode(base64File);
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            bos.write(bytes);
            bos.flush();
        } catch (Exception e) {
            throw new UncheckedException(e);
        } finally {
            CloseUtil.close(bos, fos);
        }
        return file;
    }



    /**
     * 将指定文件读取并转换为Base64编码字符串
     *
     * @param file 要读取的文件对象
     * @return 文件内容的Base64编码字符串
     * @throws IOException 当文件读取过程中发生IO异常时抛出
     */
    public static String readFileToBase64(File file) throws IOException {
        // 一次性读取全部字节，避免 read() 单次读取不足导致 Base64 内容被截断
        byte[] bytes = Files.readAllBytes(file.toPath());
        return Base64.getEncoder().encodeToString(bytes);
    }

}
