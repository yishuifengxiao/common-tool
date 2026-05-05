package com.yishuifengxiao.common.tool.bean;

import jakarta.persistence.Transient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ClassUtil 工具类单元测试
 */
@DisplayName("ClassUtil工具类测试")
class ClassUtilTest {

    // 测试用的父类
    static class ParentClass {
        private String parentField = "parent";
        protected int protectedField = 100;
        public String publicField = "public";
        private static String staticField = "static";
        private final String finalField = "final";
        private transient String transientField = "transient";
    }

    // 测试用的子类
    static class ChildClass extends ParentClass {
        private String childField = "child";
        private int childAge = 10;
    }

    // 测试用的简单类
    static class SimpleClass {
        private String name;
        private int age;

        public SimpleClass() {
            this.name = "test";
            this.age = 25;
        }

        public SimpleClass(String name, int age) {
            this.name = name;
            this.age = age;
        }
    }

    // 测试用的嵌套类
    static class OuterClass {
        private String outerName = "outer";
        private InnerClass inner = new InnerClass();

        static class InnerClass {
            private String innerName = "inner";
            private int innerValue = 42;
        }
    }

    // 测试用的带Transient注解的类
    static class TransientTestClass {
        private String normalField = "normal";

        @Transient
        private String transientAnnotatedField = "transient-annotated";
    }

    // 测试用的包含接口类型字段的类
    static class InterfaceFieldClass {
        private Runnable runnableField = () -> {
        };
        private String stringField = "string";
    }

    @BeforeEach
    void setUp() {
        // 每个测试前清理缓存（通过反射）
        try {
            Field fieldsCacheField = ClassUtil.class.getDeclaredField("FIELDS_CACHE");
            fieldsCacheField.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<String, List<Field>> cache = (Map<String, List<Field>>) fieldsCacheField.get(null);
            cache.clear();

            Field fieldLookupCacheField = ClassUtil.class.getDeclaredField("FIELD_LOOKUP_CACHE");
            fieldLookupCacheField.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<String, Field> lookupCache = (Map<String, Field>) fieldLookupCacheField.get(null);
            lookupCache.clear();
        } catch (Exception e) {
            fail("清理缓存失败: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("测试fields方法 - 获取类的所有字段（默认排除特殊修饰符）")
    void testFieldsDefault() {
        List<Field> fields = ClassUtil.fields(SimpleClass.class);

        assertNotNull(fields);
        assertEquals(2, fields.size());

        // 验证字段名称
        List<String> fieldNames = fields.stream()
                .map(Field::getName)
                .collect(java.util.stream.Collectors.toList());
        assertTrue(fieldNames.contains("name"));
        assertTrue(fieldNames.contains("age"));
    }

    @Test
    @DisplayName("测试fields方法 - 包含特殊修饰符字段")
    void testFieldsWithSpecialModifiers() {
        List<Field> allFields = ClassUtil.fields(ParentClass.class, false);

        assertNotNull(allFields);
        assertTrue(allFields.size() >= 6); // 至少包含所有声明的字段

        List<String> fieldNames = allFields.stream()
                .map(Field::getName)
                .collect(java.util.stream.Collectors.toList());
        assertTrue(fieldNames.contains("parentField"));
        assertTrue(fieldNames.contains("staticField"));
        assertTrue(fieldNames.contains("finalField"));
        assertTrue(fieldNames.contains("transientField"));
    }

    @Test
    @DisplayName("测试fields方法 - 排除特殊修饰符字段")
    void testFieldsExcludeSpecialModifiers() {
        List<Field> filteredFields = ClassUtil.fields(ParentClass.class, true);

        assertNotNull(filteredFields);

        List<String> fieldNames = filteredFields.stream()
                .map(Field::getName)
                .collect(java.util.stream.Collectors.toList());
        assertTrue(fieldNames.contains("parentField"));
        assertTrue(fieldNames.contains("protectedField"));
        assertTrue(fieldNames.contains("publicField"));
        assertFalse(fieldNames.contains("staticField"));
        assertFalse(fieldNames.contains("finalField"));
        assertFalse(fieldNames.contains("transientField"));
    }

    @Test
    @DisplayName("测试fields方法 - 继承链字段获取")
    void testFieldsInheritance() {
        List<Field> childFields = ClassUtil.fields(ChildClass.class, true);

        assertNotNull(childFields);

        List<String> fieldNames = childFields.stream()
                .map(Field::getName)
                .collect(java.util.stream.Collectors.toList());

        // 应该包含子类和父类的字段
        assertTrue(fieldNames.contains("childField"));
        assertTrue(fieldNames.contains("childAge"));
        assertTrue(fieldNames.contains("parentField"));
        assertTrue(fieldNames.contains("protectedField"));
        assertTrue(fieldNames.contains("publicField"));

        // 不应该包含特殊修饰符字段
        assertFalse(fieldNames.contains("staticField"));
        assertFalse(fieldNames.contains("finalField"));
    }

    @Test
    @DisplayName("测试fields方法 - null参数抛出异常")
    void testFieldsNullParameter() {
        assertThrows(IllegalArgumentException.class, () -> {
            ClassUtil.fields(null);
        });
    }

    @Test
    @DisplayName("测试fields方法 - Object类返回空列表")
    void testFieldsObjectClass() {
        List<Field> fields = ClassUtil.fields(Object.class);

        assertNotNull(fields);
        assertEquals(0, fields.size());
    }

    @Test
    @DisplayName("测试fields方法 - 缓存机制验证")
    void testFieldsCaching() {
        // 第一次调用
        List<Field> fields1 = ClassUtil.fields(SimpleClass.class);

        // 第二次调用（应该从缓存获取）
        List<Field> fields2 = ClassUtil.fields(SimpleClass.class);

        // 验证两次返回的是同一个对象（缓存生效）
        assertSame(fields1, fields2);
    }

    @Test
    @DisplayName("测试fields方法 - 返回不可修改的列表")
    void testFieldsUnmodifiableList() {
        List<Field> fields = ClassUtil.fields(SimpleClass.class);

        assertThrows(UnsupportedOperationException.class, () -> {
            fields.add(null);
        });
    }

    @Test
    @DisplayName("测试isSpecialModifier方法 - null字段")
    void testIsSpecialModifierNull() {
        assertFalse(ClassUtil.isSpecialModifier(null));
    }

    @Test
    @DisplayName("测试isSpecialModifier方法 - transient字段")
    void testIsSpecialModifierTransient() throws NoSuchFieldException {
        Field field = ParentClass.class.getDeclaredField("transientField");
        assertTrue(ClassUtil.isSpecialModifier(field));
    }

    @Test
    @DisplayName("测试isSpecialModifier方法 - static字段")
    void testIsSpecialModifierStatic() throws NoSuchFieldException {
        Field field = ParentClass.class.getDeclaredField("staticField");
        assertTrue(ClassUtil.isSpecialModifier(field));
    }

    @Test
    @DisplayName("测试isSpecialModifier方法 - final字段")
    void testIsSpecialModifierFinal() throws NoSuchFieldException {
        Field field = ParentClass.class.getDeclaredField("finalField");
        assertTrue(ClassUtil.isSpecialModifier(field));
    }

    @Test
    @DisplayName("测试isSpecialModifier方法 - 普通字段")
    void testIsSpecialModifierNormal() throws NoSuchFieldException {
        Field field = ParentClass.class.getDeclaredField("parentField");
        assertFalse(ClassUtil.isSpecialModifier(field));
    }

    @Test
    @DisplayName("测试isSpecialModifier方法 - Transient注解放置字段")
    void testIsSpecialModifierTransientAnnotation() throws NoSuchFieldException {
        Field field = TransientTestClass.class.getDeclaredField("transientAnnotatedField");
        assertTrue(ClassUtil.isSpecialModifier(field));
    }

    @Test
    @DisplayName("测试isSpecialModifier方法 - 接口类型字段")
    void testIsSpecialModifierInterfaceType() throws NoSuchFieldException {
        Field field = InterfaceFieldClass.class.getDeclaredField("runnableField");
        assertTrue(ClassUtil.isSpecialModifier(field));
    }

    @Test
    @DisplayName("测试hasSpecialModifier方法 - 包含特殊修饰符")
    void testHasSpecialModifierWithSpecial() {
        int modifiers = java.lang.reflect.Modifier.STATIC | java.lang.reflect.Modifier.FINAL;
        assertTrue(ClassUtil.hasSpecialModifier(modifiers));
    }

    @Test
    @DisplayName("测试hasSpecialModifier方法 - 不包含特殊修饰符")
    void testHasSpecialModifierWithoutSpecial() {
        int modifiers = java.lang.reflect.Modifier.PRIVATE;
        assertFalse(ClassUtil.hasSpecialModifier(modifiers));
    }

    @Test
    @DisplayName("测试extractValue方法 - 简单属性提取")
    void testExtractValueSimple() {
        SimpleClass obj = new SimpleClass("John", 30);

        Object nameValue = ClassUtil.extractValue(obj, "name");
        assertEquals("John", nameValue);

        Object ageValue = ClassUtil.extractValue(obj, "age");
        assertEquals(30, ageValue);
    }

    @Test
    @DisplayName("测试extractValue方法 - null对象返回null")
    void testExtractValueNullObject() {
        assertNull(ClassUtil.extractValue(null, "fieldName"));
    }

    @Test
    @DisplayName("测试extractValue方法 - null字段名返回null")
    void testExtractValueNullFieldName() {
        SimpleClass obj = new SimpleClass();
        assertNull(ClassUtil.extractValue(obj, null));
        assertNull(ClassUtil.extractValue(obj, ""));
        assertNull(ClassUtil.extractValue(obj, "   "));
    }

    @Test
    @DisplayName("测试extractValue方法 - Map类型对象")
    void testExtractValueFromMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("key1", "value1");
        map.put("key2", 123);

        assertEquals("value1", ClassUtil.extractValue(map, "key1"));
        assertEquals(123, ClassUtil.extractValue(map, "key2"));
        assertNull(ClassUtil.extractValue(map, "nonExistentKey"));
    }

    @Test
    @DisplayName("测试extractValue方法 - 嵌套属性提取")
    void testExtractValueNested() {
        OuterClass outer = new OuterClass();

        Object value = ClassUtil.extractValue(outer, "inner.innerName");
        assertEquals("inner", value);

        value = ClassUtil.extractValue(outer, "inner.innerValue");
        assertEquals(42, value);
    }

    @Test
    @DisplayName("测试extractValue方法 - 嵌套路径中存在null")
    void testExtractValueNestedWithNull() {
        OuterClass outer = new OuterClass();
        outer.inner = null;

        assertNull(ClassUtil.extractValue(outer, "inner.innerName"));
    }

    @Test
    @DisplayName("测试extractNestedValue方法 - 多层嵌套")
    void testExtractNestedValue() {
        OuterClass outer = new OuterClass();

        Object value = ClassUtil.extractNestedValue(outer, "inner.innerName");
        assertEquals("inner", value);
    }

    @Test
    @DisplayName("测试getValue方法 - 私有字段访问")
    void testGetValuePrivateField() {
        SimpleClass obj = new SimpleClass("Alice", 28);

        Object value = ClassUtil.getValue(obj, "name");
        assertEquals("Alice", value);
    }

    @Test
    @DisplayName("测试getValue方法 - 不存在的字段返回null")
    void testGetValueNonExistentField() {
        SimpleClass obj = new SimpleClass();

        assertNull(ClassUtil.getValue(obj, "nonExistentField"));
    }

    @Test
    @DisplayName("测试getValue方法 - Iterable类型返回null")
    void testGetValueIterable() {
        List<String> list = List.of("a", "b", "c");

        assertNull(ClassUtil.getValue(list, "anyField"));
    }

    @Test
    @DisplayName("测试findField方法 - 查找存在的字段")
    void testFindFieldExisting() throws NoSuchFieldException {
        Field field = ClassUtil.findField(SimpleClass.class, "name");

        assertNotNull(field);
        assertEquals("name", field.getName());
    }

    @Test
    @DisplayName("测试findField方法 - 查找不存在的字段")
    void testFindFieldNonExistent() {
        Field field = ClassUtil.findField(SimpleClass.class, "nonExistent");

        assertNull(field);
    }

    @Test
    @DisplayName("测试findField方法 - 查找继承的字段")
    void testFindFieldInherited() throws NoSuchFieldException {
        Field field = ClassUtil.findField(ChildClass.class, "parentField");

        assertNotNull(field);
        assertEquals("parentField", field.getName());
    }

    @Test
    @DisplayName("测试findField方法 - 缓存机制验证")
    void testFindFieldCaching() {
        Field field1 = ClassUtil.findField(SimpleClass.class, "name");
        Field field2 = ClassUtil.findField(SimpleClass.class, "name");

        assertSame(field1, field2);
    }

    @Test
    @DisplayName("测试综合场景 - 复杂对象的属性提取")
    void testComplexScenario() {
        ChildClass child = new ChildClass();

        // 提取继承的字段
        Object parentFieldValue = ClassUtil.extractValue(child, "parentField");
        assertEquals("parent", parentFieldValue);

        // 提取子类的字段
        Object childFieldValue = ClassUtil.extractValue(child, "childField");
        assertEquals("child", childFieldValue);
    }

    @Test
    @DisplayName("测试综合场景 - 字段过滤与值提取结合")
    void testFieldFilteringWithValueExtraction() {
        // 获取排除特殊修饰符的字段列表
        List<Field> fields = ClassUtil.fields(ParentClass.class, true);

        // 验证只能访问非特殊修饰符字段
        List<String> fieldNames = fields.stream()
                .map(Field::getName)
                .collect(java.util.stream.Collectors.toList());

        assertTrue(fieldNames.contains("parentField"));
        assertFalse(fieldNames.contains("staticField"));

        // 验证可以提取这些字段的值
        ParentClass obj = new ParentClass();
        Object value = ClassUtil.extractValue(obj, "parentField");
        assertNotNull(value);
    }
}
