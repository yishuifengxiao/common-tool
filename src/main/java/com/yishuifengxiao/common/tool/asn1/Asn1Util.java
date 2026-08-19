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
    private static final ConcurrentHashMap<Class<?>, java.lang.reflect.Constructor<?>> WRITER_CONSTRUCTOR_CACHE = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Class<?>, java.lang.reflect.Constructor<?>> READER_CONSTRUCTOR_CACHE = new ConcurrentHashMap<>();

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
     */
    public static String toHexString(Object object) {
        if (object == null) {
            throw new UncheckedException("object参数不能为null");
        }

        try (ByteArrayOutputStream bufferOut = new ByteArrayOutputStream()) {
            Method writePduMethod = WRITE_PDU_METHOD_CACHE.computeIfAbsent(object.getClass(), key -> {
                // 优先匹配参数类型名为BERWriter的writePdu方法（避免编译期依赖BERWriter类）
                Method fallback = null;
                for (Method method : key.getMethods()) {
                    if (!"writePdu".equals(method.getName())) continue;
                    Class<?>[] paramTypes = method.getParameterTypes();
                    if (paramTypes.length != 2 || !paramTypes[0].equals(key)) continue;
                    try {
                        paramTypes[1].getConstructor(java.io.OutputStream.class);
                    } catch (NoSuchMethodException ignored) {
                        continue;
                    }
                    // 优先选择参数类型名为BERWriter的方法
                    if ("BERWriter".equals(paramTypes[1].getSimpleName())) {
                        method.setAccessible(true);
                        return method;
                    }
                    if (fallback == null) {
                        fallback = method;
                    }
                }
                if (fallback != null) {
                    fallback.setAccessible(true);
                    return fallback;
                }
                throw new UncheckedException(String.format("类%s中不存在writePdu方法", key.getName()));
            });

            // 通过反射获取writePdu方法的第二个参数类型，并通过反射构造实例（缓存Constructor）
            Class<?> writerType = writePduMethod.getParameterTypes()[1];
            java.lang.reflect.Constructor<?> constructor = WRITER_CONSTRUCTOR_CACHE.computeIfAbsent(writerType, type -> {
                try {
                    java.lang.reflect.Constructor<?> ctor = type.getConstructor(java.io.OutputStream.class);
                    ctor.setAccessible(true);
                    return ctor;
                } catch (NoSuchMethodException e) {
                    throw new UncheckedException(String.format("无法找到%s的OutputStream构造方法", type.getName()), e);
                }
            });

            Object writerInstance;
            try {
                writerInstance = constructor.newInstance(bufferOut);
            } catch (InvocationTargetException e) {
                Throwable targetException = e.getTargetException();
                throw new UncheckedException(String.format("创建%s实例时发生异常", writerType.getName()),
                        targetException != null ? targetException : e);
            } catch (InstantiationException e) {
                throw new UncheckedException(String.format("无法实例化%s，可能是抽象类", writerType.getName()), e);
            } catch (IllegalAccessException e) {
                throw new UncheckedException(String.format("无法访问%s的构造方法", writerType.getName()), e);
            }

            writePduMethod.invoke(object, object, writerInstance);

            byte[] bytes = bufferOut.toByteArray();

            if (bytes.length == 0) {
                log.warn("对象序列化后生成的数据为空，对象类型: {}", object.getClass());
                return "";
            }

            return Hex.bytesToHex(bytes);
        } catch (UncheckedException e) {
            // 已经是UncheckedException，直接透传，避免二次包装导致错误信息误导
            throw e;
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
     * 解析为对应的Java对象。为了提高性能，方法会缓存反射获取的Method对象。
     * readPdu方法的参数类型通过反射动态获取，不硬编码为特定Reader类型。</p>
     *
     * @param <T>   目标对象的泛型类型
     * @param clazz 目标类的Class对象，该类必须包含静态方法readPdu，且参数类型支持InputStream构造，不能为null
     * @param hex   BER编码的十六进制字符串表示，不能为null或空字符串
     * @return 解析后的ASN.1对象实例
     * @throws UncheckedException 当出现以下情况时抛出：
     *                            <ul>
     *                              <li>clazz或hex参数为null或hex为空字符串</li>
     *                              <li>目标类中不存在符合条件的readPdu静态方法</li>
     *                              <li>readPdu方法不是静态方法</li>
     *                              <li>反射调用readPdu方法时发生异常</li>
     *                              <li>十六进制字符串格式错误或BER数据解析失败</li>
     *                            </ul>
     * @example // 示例：将十六进制字符串转换为ASN1Object对象
     * String hexData = "30820122A003020102";
     * ASN1Object obj = Asn1Util.toObject(ASN1Object.class, hexData);
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
            // 优先匹配参数类型名为BERReader的readPdu静态方法（避免编译期依赖BERReader类）
            Method readPduMethod = READ_PDU_METHOD_CACHE.computeIfAbsent(clazz, key -> {
                Method fallback = null;
                for (Method method : key.getMethods()) {
                    if (!"readPdu".equals(method.getName())) continue;
                    if (!java.lang.reflect.Modifier.isStatic(method.getModifiers())) continue;
                    Class<?>[] paramTypes = method.getParameterTypes();
                    if (paramTypes.length != 1) continue;
                    try {
                        paramTypes[0].getConstructor(java.io.InputStream.class);
                    } catch (NoSuchMethodException ignored) {
                        continue;
                    }
                    // 优先选择参数类型名为BERReader的方法
                    if ("BERReader".equals(paramTypes[0].getSimpleName())) {
                        method.setAccessible(true);
                        return method;
                    }
                    if (fallback == null) {
                        fallback = method;
                    }
                }
                if (fallback != null) {
                    fallback.setAccessible(true);
                    return fallback;
                }
                throw new UncheckedException(String.format("类%s中不存在readPdu静态方法", key.getName()));
            });

            // 通过反射获取readPdu方法的参数类型，并通过反射构造Reader实例（缓存Constructor）
            Class<?> readerType = readPduMethod.getParameterTypes()[0];
            java.lang.reflect.Constructor<?> constructor = READER_CONSTRUCTOR_CACHE.computeIfAbsent(readerType, type -> {
                try {
                    java.lang.reflect.Constructor<?> ctor = type.getConstructor(java.io.InputStream.class);
                    ctor.setAccessible(true);
                    return ctor;
                } catch (NoSuchMethodException e) {
                    throw new UncheckedException(String.format("无法找到%s的InputStream构造方法", type.getName()), e);
                }
            });

            byte[] bytes = Hex.hexToBytes(hex);
            Object readerInstance;
            try {
                readerInstance = constructor.newInstance(new java.io.ByteArrayInputStream(bytes));
            } catch (InvocationTargetException e) {
                Throwable targetException = e.getTargetException();
                throw new UncheckedException(String.format("创建%s实例时发生异常", readerType.getName()),
                        targetException != null ? targetException : e);
            } catch (InstantiationException e) {
                throw new UncheckedException(String.format("无法实例化%s，可能是抽象类", readerType.getName()), e);
            } catch (IllegalAccessException e) {
                throw new UncheckedException(String.format("无法访问%s的构造方法", readerType.getName()), e);
            }

            @SuppressWarnings("unchecked") T obj = (T) readPduMethod.invoke(null, readerInstance);
            return obj;
        } catch (UncheckedException e) {
            // 已经是UncheckedException，直接透传，避免二次包装导致错误信息误导
            throw e;
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
     * @param strData 输入的数字字符串，不能为null或空字符串，必须全部由数字字符组成
     * @return 计算得到的校验位数字字符串（0-9）
     * @throws UncheckedException 当输入为null、空字符串或包含非数字字符时抛出异常
     */
    public static String generateLuhn(String strData) {
        if (strData == null || strData.isEmpty()) {
            throw new UncheckedException("输入数据不能为空");
        }
        char[] strDataChars = strData.toCharArray();
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

    /**
     * 将MCC和MNC编码为3字节的十六进制字符串（如 "133010"）
     *
     * @param mcc 3位数字，如 "310"
     * @param mnc 2~3位数字，如 "013" 或 "01"
     * @return 6位十六进制字符串（大写）
     * @throws IllegalArgumentException 参数格式错误
     */
    public static String encodeMccMnc(String mcc, String mnc) {
        mcc = StringUtils.trim(mcc);
        mnc = StringUtils.trim(mnc);
        // 1. 校验
        if (mcc == null || !mcc.matches("\\d{3}")) {
            throw new IllegalArgumentException("MCC must be exactly 3 digits");
        }
        if (mnc == null || !mnc.matches("\\d{2,3}")) {
            throw new IllegalArgumentException("MNC must be 2 or 3 digits");
        }

        // 2. 提取数字数组
        int[] mccDigits = mcc.chars().map(c -> c - '0').toArray();
        int[] mncDigits = mnc.chars().map(c -> c - '0').toArray();

        // 3. 补齐MNC到3位（2位时第三位用0xF填充）
        int mnc3 = (mncDigits.length == 3) ? mncDigits[2] : 0xF;

        // 4. 计算三个字节（用int表示，便于直接转十六进制）
        int b1 = (mccDigits[1] << 4) | mccDigits[0];
        int b2 = (mnc3 << 4) | mccDigits[2];
        int b3 = (mncDigits[1] << 4) | mncDigits[0];

        // 5. 格式化为6位十六进制字符串
        return String.format("%02X%02X%02X", b1, b2, b3);
    }

    /**
     * 将6位十六进制字符串解码为MCC和MNC
     *
     * <p>与 {@link #encodeMccMnc(String, String)} 互逆。当第二字节的高半字节为 0xF 时，
     * MNC 视为2位；否则为3位。</p>
     *
     * @param encoded 6位十六进制字符串（大小写均可，如 "133010" 或 "13f010"）
     * @return 长度为2的字符串数组：[0]=MCC（3位数字），[1]=MNC（2~3位数字）
     * @throws IllegalArgumentException 参数格式错误或编码数据无效
     */
    public static String[] decodeMccMnc(String encoded) {
        encoded = StringUtils.trim(encoded);
        if (encoded == null || !encoded.matches("[0-9A-Fa-f]{6}")) {
            throw new IllegalArgumentException("Encoded value must be exactly 6 hex digits");
        }

        // 1. 解析三个字节
        int b1 = Integer.parseInt(encoded.substring(0, 2), 16);
        int b2 = Integer.parseInt(encoded.substring(2, 4), 16);
        int b3 = Integer.parseInt(encoded.substring(4, 6), 16);

        // 2. 提取 MCC 数字
        int mcc0 = b1 & 0x0F;
        int mcc1 = (b1 >> 4) & 0x0F;
        int mcc2 = b2 & 0x0F;

        // 3. 提取 MNC 数字及第三位标记
        int mnc3 = (b2 >> 4) & 0x0F;
        int mnc0 = b3 & 0x0F;
        int mnc1 = (b3 >> 4) & 0x0F;

        // 4. 校验数字有效性（MCC和MNC前两位必须是0-9）
        if (mcc0 > 9 || mcc1 > 9 || mcc2 > 9 || mnc0 > 9 || mnc1 > 9) {
            throw new IllegalArgumentException("Decoded digits must be 0-9, invalid encoded value: " + encoded);
        }

        // 5. 根据 mnc3 判断 MNC 位数并组装结果
        String mcc = String.format("%d%d%d", mcc0, mcc1, mcc2);
        String mnc;
        if (mnc3 == 0xF) {
            // 2位MNC
            mnc = String.format("%d%d", mnc0, mnc1);
        } else if (mnc3 <= 9) {
            // 3位MNC
            mnc = String.format("%d%d%d", mnc0, mnc1, mnc3);
        } else {
            // 0xA~0xE 为无效填充值
            throw new IllegalArgumentException("Invalid MNC filler digit in encoded value: " + encoded);
        }

        return new String[]{mcc, mnc};
    }
}
