package com.yishuifengxiao.common.tool.io;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Base64Utils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Base64;

/**
 * <p>
 * base64与图片转换工具
 * </p>
 * <p>
 * 该工具的主要作用是实现图片与base64字符串之间的互相转换.
 * </p>
 * <p>
 * <strong>该工具是一个线程安全类的工具</strong>
 * </p>
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
     * 图片base64编码之后的前缀正则表达式
     */
    private final static String FORMAT_TEMPLATE = "data:image/[a-zA-Z]+;base64,";

    /**
     * <p>
     * 将 本地图片转换成base64字符串
     * </p>
     * <p>
     * 转换后的base64字符串不抱图片格式信息
     * </p>
     *
     * @param imgPath 本地图片的地址
     * @return base64字符串
     * @throws IOException 转换时发生问题
     */
    public static synchronized String imageToBase64ByLocal(String imgPath) throws IOException {
        // 返回Base64编码过的字节数组字符串
        return imageToBase64ByLocal(new File(imgPath));
    }

    /**
     * <p>
     * 将 本地图片转换成base64字符串
     * </p>
     * <p>
     * 转换后的base64字符串不抱图片格式信息
     * </p>
     *
     * @param image 待转换的图片
     * @return base64字符串
     * @throws IOException 转换时发生问题
     */
    public static synchronized String imageToBase64ByLocal(File image) throws IOException {
        InputStream inputStream = null;
        byte[] data = null;
        // 读取图片字节数组
        try {
            inputStream = new FileInputStream(image);
            data = new byte[inputStream.available()];
            inputStream.read(data);
        } finally {
            CloseUtil.close(inputStream);
        }
        // 返回Base64编码过的字节数组字符串
        return Base64Utils.encodeToString(data);
    }

    /**
     * <p>
     * 将base64字符串转换成图片
     * </p>
     * <p>
     * 该base64字符串里不能包含照片格式信息信息
     * </p>
     *
     * @param imgBase64Str 图片 的base64字符串
     * @param imagePath    输出图片的地址，不能为空
     * @return 输出图片
     * @throws IOException 转换时发生问题
     */
    public static synchronized File base64ToImage(String imgBase64Str, String imagePath) throws IOException {
        File file = new File(imagePath);
        return base64ToImage(imgBase64Str, file);
    }

    /**
     * <p>
     * 将base64字符串转换成图片
     * </p>
     * <p>
     * 该base64字符串里不能包含照片格式信息信息
     * </p>
     *
     * @param imgBase64Str 图片 的base64字符串
     * @param img          输出图片，不能为空
     * @return 输出图片
     * @throws IOException 转换时发生问题
     */
    public static synchronized File base64ToImage(String imgBase64Str, File img) throws IOException {
        OutputStream out = null;
        try {
            imgBase64Str = imgBase64Str.replaceAll(FORMAT_TEMPLATE, "");
            // Base64解码
            byte[] b = Base64Utils.decodeFromString(imgBase64Str);
            for (int i = 0; i < b.length; ++i) {
                if (b[i] < 0) {
                    // 调整异常数据
                    b[i] += 256;
                }
            }
            out = new FileOutputStream(img);
            out.write(b);
            out.flush();

        } finally {
            CloseUtil.close(out);
        }
        return img;
    }

    /**
     * <p>
     * 将图片转换成base64字符串
     * </p>
     * <p>
     * 结果不包含base64信息头
     * </p>
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
     * <p>
     * 将图片转换成base64字符串
     * </p>
     * <p>
     * 结果包含png格式的base64信息头
     * </p>
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

    /**
     * 将base64字符串转换成BufferedImage
     *
     * @param base64 待转换的base64字符串
     * @return 转换后的BufferedImage
     */
    public static BufferedImage base64ToBufferedImage(String base64) {
        if (StringUtils.isBlank(base64)) {
            return null;
        }
        ByteArrayInputStream byteArrayInputStream = null;
        try {
            byte[] b = Base64Utils.decodeFromString(base64);
            for (int i = 0; i < b.length; ++i) {
                if (b[i] < 0) {
                    // 调整异常数据
                    b[i] += 256;
                }
            }
            byteArrayInputStream = new ByteArrayInputStream(b);
            return ImageIO.read(byteArrayInputStream);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            CloseUtil.close(byteArrayInputStream);
        }
        return null;
    }

}
