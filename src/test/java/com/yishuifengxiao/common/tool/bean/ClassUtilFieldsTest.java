package com.yishuifengxiao.common.tool.bean;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;

import static org.junit.Assert.*;

/**
 * 测试 ClassUtil.fields(Class<T> clazz) 方法
 */
@RunWith(MockitoJUnitRunner.class)
public class ClassUtilFieldsTest {

    // 定义用于测试的基础类
    static class Parent {
        private String parentField = "parent";
    }

    static class Child extends Parent {
        public int childField = 10;
        protected double anotherChildField = 3.14;
    }

    static class GrandChild extends Child {
        private boolean grandChildField = true;
    }

    static class SimpleClass {
        public String simpleField = "simple";
    }

    /**
     * 测试场景 T1: clazz == null
     */
    @Test
    public void testFields_NullClass_ReturnsEmptyList() {
        List<Field> result = ClassUtil.fields(null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * 测试场景 T2: 没有父类的普通类
     */
    @Test
    public void testFields_SimpleClass_ContainsDeclaredFieldsOnly() {
        List<Field> result = ClassUtil.fields(SimpleClass.class);
        assertNotNull(result);
        assertEquals(1, result.size());

        Field field = result.get(0);
        assertEquals("simpleField", field.getName());
        assertEquals(String.class, field.getType());
        assertTrue(Modifier.isPublic(field.getModifiers()));
    }

    /**
     * 测试场景 T3: 有一个父类的子类
     */
    @Test
    public void testFields_ChildClass_IncludesParentAndOwnFields() {
        List<Field> result = ClassUtil.fields(Child.class);
        assertNotNull(result);
        assertEquals(3, result.size()); // parentField, childField, anotherChildField

        boolean hasParentField = result.stream().anyMatch(f -> f.getName().equals("parentField"));
        boolean hasChildField = result.stream().anyMatch(f -> f.getName().equals("childField"));
        boolean hasAnotherChildField = result.stream().anyMatch(f -> f.getName().equals("anotherChildField"));

        assertTrue(hasParentField);
        assertTrue(hasChildField);
        assertTrue(hasAnotherChildField);
    }

    /**
     * 测试场景 T4: 多级继承情况
     */
    @Test
    public void testFields_GrandChildClass_IncludesAllInheritedFields() {
        List<Field> result = ClassUtil.fields(GrandChild.class);
        assertNotNull(result);
        assertEquals(4, result.size());

        boolean hasGrandChildField = result.stream().anyMatch(f -> f.getName().equals("grandChildField"));
        boolean hasParentField = result.stream().anyMatch(f -> f.getName().equals("parentField"));
        boolean hasChildField = result.stream().anyMatch(f -> f.getName().equals("childField"));
        boolean hasAnotherChildField = result.stream().anyMatch(f -> f.getName().equals("anotherChildField"));

        assertTrue(hasGrandChildField);
        assertTrue(hasParentField);
        assertTrue(hasChildField);
        assertTrue(hasAnotherChildField);
    }

    /**
     * 测试场景 T5: 缓存机制是否生效
     * 通过两次调用同一个类来判断缓存是否命中
     */
    @Test
    public void testFields_CacheHit_SecondCallUsesCachedResult() {
        List<Field> firstCall = ClassUtil.fields(SimpleClass.class);
        List<Field> secondCall = ClassUtil.fields(SimpleClass.class);

        assertSame(firstCall, secondCall); // 应该是同一实例（不可变缓存）
    }

    /**
     * 辅助方法：模拟 javax.persistence.Transient 注解是否存在
     * （用于 isSpecialModifier 中逻辑分支覆盖）
     */
    @Test
    public void testFields_WithJavaxPersistenceTransientAnnotation_LoadOnceOnly() throws Exception {
        // 尝试加载 javax.persistence.Transient 类并确认它被缓存
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try {
            Class<?> transientClazz = loader.loadClass("javax.persistence.Transient");

            // 如果成功加载，则应该能获取到这个注解类引用
            Field internalField = ClassUtil.class.getDeclaredField("transientAnnotationClass");
            internalField.setAccessible(true);
            Class<? extends Annotation> cached = (Class<? extends Annotation>) internalField.get(null);

            assertNotNull(cached);
            assertEquals(transientClazz, cached);
        } catch (ClassNotFoundException ignored) {
            // 忽略找不到类的情况
        }
    }
}
