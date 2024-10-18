/**
 *
 */
package com.yishuifengxiao.common.tool.bean;


import com.yishuifengxiao.common.tool.collections.JsonUtil;
import com.yishuifengxiao.common.tool.exception.UncheckedException;
import com.yishuifengxiao.common.tool.io.CloseUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 对象转换工具
 * </p>
 * 该工具的主要目标是对java对象进行操作，其具备以下的几项功能
 * <ol>
 * <li>将源对象的属性值根据属性的名字复制到目标对象</li>
 * <li>将java bean 对象转换成二进制数据</li>
 * <li>将二进制数据转换成 java bean</li>
 * </ol>
 *
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
public final class BeanUtil {

    /**
     * 将源对象里属性值复制给目标对象(当前方法是一个线程安全类的方法)
     *
     * @param <S>    源对象的类型
     * @param <T>    目标对象的类型
     * @param source 源对象
     * @param target 目标对象
     * @return 复制后的目标对象
     */
    public static <S, T> T copy(S source, T target) {
        if (null == source || null == target) {
            return null;
        }
        List<Field> targetFields = ClassUtil.fields(target.getClass());
        List<Field> sourceFields = ClassUtil.fields(source.getClass());
        targetFields = targetFields.stream().filter(v -> !(Modifier.isAbstract(v.getModifiers()) || Modifier.isNative(v.getModifiers()) || Modifier.isStatic(v.getModifiers()) || Modifier.isFinal(v.getModifiers()))).filter(v -> sourceFields.stream().anyMatch(s -> s.getName().equals(v.getName()))).collect(Collectors.toList());
        for (Field field : targetFields) {
            field.setAccessible(true);
            try {
                Object val = field.get(source);
                if (null == val) {
                    continue;
                }
                field.set(target, val);
            } catch (IllegalAccessException e) {
                throw new UncheckedException(e.getMessage());
            }
        }

        return target;
    }


    /**
     * 将Java对象序列化为二进制数据
     *
     * @param obj 需要序列化的的对象
     * @return 二进制数据
     */
    public static byte[] objectToByte(Object obj) {
        byte[] bytes = null;
        try {
            // object to bytearray
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            ObjectOutputStream oo = new ObjectOutputStream(bo);
            oo.writeObject(obj);
            bytes = bo.toByteArray();
            bo.close();
            oo.close();
        } catch (Exception e) {
            throw new UncheckedException(e.getMessage());
        }
        return bytes;
    }

    /**
     * 将序列化化后的二进制数据反序列化为对象
     *
     * @param bytes 序列化化后的二进制数据
     * @return 对象
     */
    public static Object byteToObject(byte[] bytes) {
        Object obj = null;
        try {
            // bytearray to object
            ByteArrayInputStream bi = new ByteArrayInputStream(bytes);
            ObjectInputStream oi = new ObjectInputStream(bi);
            obj = oi.readObject();
            bi.close();
            oi.close();
        } catch (Exception e) {
            throw new UncheckedException(e.getMessage());
        }
        return obj;
    }

    /**
     * 将序列化化后的二进制数据反序列化为对象
     *
     * @param <T>   目标对象的类型
     * @param bytes 原始的二进制数据
     * @param clazz 目标对象
     * @return 反序列化之后的对象
     */
    @SuppressWarnings("unchecked")
    public static <T> T byteToObject(byte[] bytes, Class<T> clazz) {
        T t = null;
        try {
            // bytearray to object
            ByteArrayInputStream bi = new ByteArrayInputStream(bytes);
            ObjectInputStream oi = new ObjectInputStream(bi);
            t = (T) oi.readObject();
            bi.close();
            oi.close();
        } catch (Exception e) {
            throw new UncheckedException(e.getMessage());
        }
        return t;
    }

    /**
     * 将Map转成指定的JavaBean对象
     *
     * @param <T>   目标对象的类型
     * @param map   原始的map对象数据
     * @param clazz 目标对象
     * @return 转换后的java对象
     */
    @SuppressWarnings({"rawtypes"})
    public static <T> T mapToBean(Map map, Class<T> clazz) {
        if (null == map) {
            return null;
        }
        return JsonUtil.str2Bean(JsonUtil.toJSONString(map), clazz);
    }

    /**
     * 将java对象转换为Map
     *
     * @param data java对象
     * @return 转换后的map数据
     */
    @SuppressWarnings("rawtypes")
    public static Map beanToMap(Object data) {
        if (null == data) {
            return Collections.EMPTY_MAP;
        }
        return JsonUtil.json2Map(JsonUtil.toJSONString(data));
    }

    /**
     * 对象深克隆
     *
     * @param val 待克隆的的对象
     * @return 克隆后的对象
     */
    public static Object cloneVal(Object val) {
        if (null == val) {
            return null;
        }
        ByteArrayOutputStream bos = null;
        ObjectOutputStream oos = null;
        ByteArrayInputStream bis = null;
        ObjectInputStream ois = null;
        try {
            bos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(bos);
            // 将原始对象写入字节数组
            oos.writeObject(val);
            // 获取字节数组
            byte[] byteArr = bos.toByteArray();
            bis = new ByteArrayInputStream(byteArr);
            ois = new ObjectInputStream(bis);
            // 从字节数组中读取对象
            return ois.readObject();
        } catch (Exception e) {
            throw UncheckedException.of(e.getMessage());
        } finally {
            CloseUtil.close(ois, bis, oos, bos);
        }
    }

}
