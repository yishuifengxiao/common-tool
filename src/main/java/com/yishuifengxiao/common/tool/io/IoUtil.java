/**
 *
 */
package com.yishuifengxiao.common.tool.io;

import com.yishuifengxiao.common.tool.exception.UncheckedException;
import com.yishuifengxiao.common.tool.random.IdWorker;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.StreamUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
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
     * 将文件转换为字节数组
     * </p>
     * <strong>线程安全</strong>
     *
     * @param file 待转换的文件
     * @return 转换后的字节数组
     * @throws IOException 转换时出现问题
     */
    public static byte[] file2ByteArray(File file) throws IOException {
        try (FileInputStream inputStream = new FileInputStream(file)) {
            return inputStream2ByteArray(inputStream);
        }
    }

    /**
     * <p>
     * 将输入流转换为字节数组
     * </p>
     * <strong>线程安全</strong>
     *
     * @param inputStream 输入流
     * @return 转换后的字节数组
     * @throws IOException 转换中发生异常
     */
    public static byte[] inputStream2ByteArray(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            return new byte[0];
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream(StreamUtils.BUFFER_SIZE);
        copy(inputStream, out);
        return out.toByteArray();
    }

    /**
     * <p>
     * 使用默认的编码将输入流转为字符串
     * </p>
     * <p>
     * 默认的编码格式由<code>Charset.defaultCharset()</code>确定
     * </p>
     *
     * @param in          输入流
     * @param charsetName 编码格式
     * @return 转换后的字符串
     * @throws IOException 转换中发生异常
     */
    public static String inputStream2String(InputStream in, String charsetName) throws IOException {
        try (InputStreamReader reader = new InputStreamReader(in, charsetName)) {
            try (BufferedReader bw = new BufferedReader(reader)) {
                String result = bw.lines().collect(Collectors.joining(System.lineSeparator()));
                return result;
            }
        } finally {
            CloseUtil.close(in);
        }

    }

    /**
     * <p>
     * 使用默认的编码将输入流转为字符串
     * </p>
     * <p>
     * 默认的编码格式由<code>Charset.defaultCharset()</code>确定
     * </p>
     *
     * @param in 输入流
     * @return 转换后的字符串
     * @throws IOException 转换中发生异常
     */
    public static String inputStream2String(InputStream in) throws IOException {
        return inputStream2String(in, Charset.defaultCharset().name());
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
     * @throws IOException 转换时出现问题
     */
    public static File inputStream2File(@NotNull InputStream inputStream, @NotNull File file) throws IOException {
        copy(inputStream, new FileOutputStream(file));
        return file;
    }

    /**
     * <p>
     * 读取classpath目录下的资源文本
     * </p>
     * <p style="color:yellow">
     * 这里使用了系统默认的编码
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
        return readResourceAsString(clazz, name, Charset.defaultCharset().name());
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
        return inputStream2String(clazz.getClassLoader().getResourceAsStream(name), charsetName);
    }

    /**
     * 将文件复制到输出流中
     *
     * @param file 待复制的文件
     * @param out  输出流
     * @return 复制失败返回为-1
     */
    public static int copy(File file, OutputStream out) {
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
     * Copy the contents of the given InputStream to the given OutputStream. Closes
     * both streams when done.
     * </p>
     * <strong>线程安全</strong>
     *
     * @param in  the stream to copy from
     * @param out the stream to copy to
     * @return the number of bytes copied
     * @throws IOException 转换时出现问题
     */
    public static int copy(InputStream in, OutputStream out) throws IOException {
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
        } finally {
            CloseUtil.close(out, in);
        }
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
     * @throws IOException 转换时出现问题
     */
    public static File string2File(String text, @NotNull File file) throws IOException {
        if (StringUtils.isBlank(text) || null == file) {
            return file;
        }
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            bw.write(text);
            bw.flush();
        }
        return file;
    }

    /**
     * 将文件按照utf-8编码转换为字符串
     *
     * @param file 待转换的文件
     * @return 转换后的字符串
     * @throws IOException 转换时出现问题
     */
    public static String file2String(@NotNull File file) throws IOException {
        return inputStream2String(new FileInputStream(file), StandardCharsets.UTF_8.name());
    }

    /**
     * 将文件按照指定的编码转换为字符串
     *
     * @param file        待转换的文件
     * @param charsetName 编码
     * @return 转换后的字符串
     * @throws IOException 转换时出现问题
     */
    public static String file2String(@NotNull File file, String charsetName) throws IOException {
        return inputStream2String(new FileInputStream(file), charsetName);
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
    public static File base64ToFile(String base64File) {
        File file = new File(IdWorker.uuid());
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
     * @throws IOException 转换时发生异常
     */
    public static String file2Base64(File file) throws IOException {

        try (FileInputStream inputFile = new FileInputStream(file)) {
            byte[] buffer = new byte[(int) file.length()];
            inputFile.read(buffer);
            inputFile.close();
            return Base64.getEncoder().encodeToString(buffer);
        }

    }

}
