package com.yishuifengxiao.common.tool.asn1;

import com.yishuifengxiao.common.tool.exception.UncheckedException;
import com.yishuifengxiao.common.tool.lang.Hex;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ASN.1工具类，提供ASN.1数据的编码和解码功能
 *
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
public class Asn1Util {

    private static final ConcurrentHashMap<Class<?>, Method> READ_PDU_METHOD_CACHE = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Class<?>, Method> WRITE_PDU_METHOD_CACHE = new ConcurrentHashMap<>();

    /**
     * 将十六进制字符串转换为BERReader对象
     *
     * @param hexVal 十六进制字符串
     * @return BERReader对象
     */
    public static BERReader hexToBERReader(String hexVal) {
        byte[] bytes = Hex.hexToBytes(hexVal);
        return new BERReader(new java.io.ByteArrayInputStream(bytes));
    }

    /**
     * 将UTF-8字符串转换为BERReader对象
     * 该方法将输入的UTF-8字符串转换为字节数组，然后创建BERReader对象用于ASN.1数据解析
     *
     * @param utf8Val UTF-8编码的字符串，将作为ASN.1数据源进行解析
     * @return BERReader对象，可用于读取和解析ASN.1 BER编码的数据
     * @example // 示例：将UTF-8字符串转换为BERReader进行解析
     * String utf8String = "示例UTF-8字符串";
     * BERReader reader = Asn1Util.utf8ToBERReader(utf8String);
     * // 使用reader读取ASN.1结构...
     * @see BERReader
     * @see #hexToBERReader(String)
     */
    public static BERReader utf8ToBERReader(String utf8Val) {
        byte[] bytes = utf8Val.getBytes(StandardCharsets.UTF_8);
        return new BERReader(new java.io.ByteArrayInputStream(bytes));
    }

    /**
     * 将指定对象序列化为十六进制字符串。该方法通过反射调用对象的 writePdu 方法，
     * 并使用 BER 编码将对象写入字节流，最后转换为十六进制字符串表示。
     *
     * @param object 要序列化的对象，必须实现 writePdu 方法且不能为 null
     * @return 序列化后的十六进制字符串
     * @throws Exception 如果在序列化过程中出现错误（如方法调用失败、IO 异常等）
     */
    public static String toHexString(Object object) throws Exception {
        if (object == null) {
            throw new UncheckedException("object参数不能为null");
        }

        try (ByteArrayOutputStream bufferOut = new ByteArrayOutputStream()) {
            BERWriter berWriter = new BERWriter(bufferOut);

            Method writePduMethod = WRITE_PDU_METHOD_CACHE.computeIfAbsent(object.getClass(), key -> {
                try {
                    Method method = key.getMethod("writePdu", key, BERWriter.class);
                    method.setAccessible(true);
                    return method;
                } catch (NoSuchMethodException e) {
                    throw new UncheckedException(String.format("类%s中不存在writePdu方法", key.getName()), e);
                }
            });

            writePduMethod.invoke(object, object, berWriter);

            byte[] bytes = bufferOut.toByteArray();

            if (bytes.length == 0) {
                log.warn("对象序列化后生成的数据为空，对象类型: {}", object.getClass());
                return "";
            }

            return Hex.bytesToHex(bytes);
        } catch (UncheckedException e) {
            throw new UncheckedException(String.format("参数验证失败，对象类型: %s", object.getClass()), e);
        } catch (IllegalAccessException e) {
            throw new UncheckedException(String.format("无法访问%s的writePdu方法", object.getClass()), e);
        } catch (InvocationTargetException e) {
            Throwable targetException = e.getTargetException();
            throw new UncheckedException(String.format("调用%s的writePdu方法时发生异常", object.getClass()),
                    targetException != null ? targetException : e);
        } catch (Exception e) {
            throw new UncheckedException(String.format("对象序列化失败，对象类型: %s", object.getClass()), e);
        }
    }

    /**
     * 将十六进制字符串解码并转换为指定类型的ASN.1对象
     *
     * <p>该方法通过反射调用目标类的静态readPdu方法，将十六进制编码的BER数据
     * 解析为对应的Java对象。为了提高性能，方法会缓存反射获取的Method对象。</p>
     *
     * @param <T>   目标对象的泛型类型
     * @param clazz 目标类的Class对象，该类必须包含静态方法readPdu(BERReader)，不能为null
     * @param hex   BER编码的十六进制字符串表示，不能为null或空字符串
     * @return 解析后的ASN.1对象实例
     * @throws UncheckedException 当出现以下情况时抛出：
     *                            <ul>
     *                              <li>clazz或hex参数为null或hex为空字符串</li>
     *                              <li>目标类中不存在readPdu(BERReader)静态方法</li>
     *                              <li>readPdu方法不是静态方法</li>
     *                              <li>反射调用readPdu方法时发生异常</li>
     *                              <li>十六进制字符串格式错误或BER数据解析失败</li>
     *                            </ul>
     * @example // 示例：将十六进制字符串转换为ASN1Object对象
     * String hexData = "30820122A003020102";
     * ASN1Object obj = Asn1Util.toObject(ASN1Object.class, hexData);
     * @see BERReader
     * @see #hexToBERReader(String)
     */
    public static <T> T toObject(Class<T> clazz, String hex) {
        if (clazz == null) {
            throw new UncheckedException("类对象不能为null");
        }
        if (hex == null || hex.isEmpty()) {
            throw new UncheckedException("十六进制字符串不能为null或空");
        }

        try {
            BERReader reader = hexToBERReader(hex);

            Method readPduMethod = READ_PDU_METHOD_CACHE.computeIfAbsent(clazz, key -> {
                try {
                    Method method = key.getMethod("readPdu", BERReader.class);
                    if (!java.lang.reflect.Modifier.isStatic(method.getModifiers())) {
                        throw new UncheckedException("readPdu方法必须是静态方法");
                    }
                    method.setAccessible(true);
                    return method;
                } catch (NoSuchMethodException e) {
                    throw new UncheckedException(String.format("类%s中不存在readPdu(BERReader)静态方法", key.getName()),
                            e);
                }
            });

            @SuppressWarnings("unchecked") T obj = (T) readPduMethod.invoke(null, reader);
            return obj;
        } catch (UncheckedException e) {
            throw new UncheckedException(String.format("参数验证失败，类: %s, 十六进制: %s", clazz, hex), e);
        } catch (InvocationTargetException e) {
            Throwable targetException = e.getTargetException();
            throw new UncheckedException(String.format("调用readPdu方法时发生异常，类: %s", clazz), targetException != null ?
                    targetException : e);
        } catch (Exception e) {
            throw new UncheckedException(String.format("无法将十六进制字符串转换为对象，类: %s, 十六进制: %s", clazz, hex), e);
        }
    }

    /**
     * 根据Luhn算法生成校验位
     * <p>
     * Luhn算法是一种简单的校验和公式，常用于验证身份证号码、信用卡号码等数字序列的有效性。
     * 该方法对输入的数字字符串进行计算，返回一个校验位数字（0-9）。
     * </p>
     *
     * @param strdata 输入的数字字符串，不能为null或空字符串，必须全部由数字字符组成
     * @return 计算得到的校验位数字字符串（0-9）
     * @throws UncheckedException 当输入为null、空字符串或包含非数字字符时抛出异常
     */
    public static String generateLuhn(String strdata) {
        if (strdata == null || strdata.isEmpty()) {
            throw new UncheckedException("输入数据不能为空");
        }
        char[] strDataChars = strdata.toCharArray();
        int len = strDataChars.length;
        int total = 0;
        boolean doubleDigit = true;

        for (int i = len - 1; i >= 0; i--) {
            int digit = strDataChars[i] - '0';

            if (doubleDigit) {
                digit *= 2;
                if (digit >= 10) {
                    digit = digit / 10 + digit % 10;
                }
            }

            total += digit;
            doubleDigit = !doubleDigit;
        }

        int checkDigit = (total % 10 == 0) ? 0 : (10 - total % 10);
        return String.format("%d", checkDigit);
    }

    /**
     * 根据ICCID长度计算并补充校验位
     * <p>
     * ICCID（Integrated Circuit Card Identifier）是集成电路卡标识符，该方法根据不同长度的ICCID进行相应处理：
     * <ul>
     *   <li>20位：已是完整格式，直接返回</li>
     *   <li>19位：补充1位Luhn校验位，形成20位ICCID</li>
     *   <li>18位：补充1位Luhn校验位和字母"F"，形成20位ICCID</li>
     * </ul>
     * </p>
     *
     * @param iccid 集成电路卡标识符，长度必须为18、19或20位，可以为null或空字符串
     * @return 处理后的ICCID字符串
     * <ul>
     *   <li>输入为null或空时，返回原值</li>
     *   <li>20位时，返回原值</li>
     *   <li>19位时，返回补充Luhn校验位后的20位ICCID</li>
     *   <li>18位时，返回补充Luhn校验位和"F"后的20位ICCID</li>
     * </ul>
     * @throws UncheckedException 当ICCID长度不是18、19或20位，或包含非数字字符时抛出异常
     */
    public static String validateIccid(String iccid) {
        if (StringUtils.isBlank(iccid)) {
            return iccid;
        }
        iccid = iccid.trim();
        int length = iccid.length();

        if (length == 20) {
            return iccid;
        }

        if (length == 18) {
            try {
                return iccid + generateLuhn(iccid) + "F";
            } catch (UncheckedException e) {
                throw new UncheckedException("ICCID包含非数字字符，无法计算校验位: " + iccid, e);
            }
        }

        if (length == 19) {
            try {
                return iccid + generateLuhn(iccid);
            } catch (UncheckedException e) {
                throw new UncheckedException("ICCID包含非数字字符，无法计算校验位: " + iccid, e);
            }
        }

        throw new UncheckedException("ICCID长度必须为18、19或20位，当前长度: " + length);
    }

}
