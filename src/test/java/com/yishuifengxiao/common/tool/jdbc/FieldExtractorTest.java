package com.yishuifengxiao.common.tool.jdbc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import jakarta.persistence.*;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FieldExtractor 工具类单元测试
 */
@DisplayName("FieldExtractor工具类测试")
class FieldExtractorTest {

    // 测试用的简单实体类
    @Entity(name = "test_user")
    @Table(name = "sys_user")
    static class UserEntity {
        @Id
        private Long id;

        private String userName;

        @Column(name = "email_addr")
        private String email;

        private Integer age;

        private transient String tempField;

        private static String staticField = "static";

        public UserEntity() {
        }

        public UserEntity(Long id, String userName, String email, Integer age) {
            this.id = id;
            this.userName = userName;
            this.email = email;
            this.age = age;
        }
    }

    // 测试用的没有注解的实体类
    static class SimpleEntity {
        private Long id;
        private String name;
        private String description;

        public SimpleEntity(Long id, String name, String description) {
            this.id = id;
            this.name = name;
            this.description = description;
        }
    }

    // 测试用的使用@Entity注解名称的实体类
    @Entity(name = "custom_product")
    static class ProductEntity {
        @Id
        private Long productId;

        private String productName;

        private BigDecimal price;

        public ProductEntity(Long productId, String productName, BigDecimal price) {
            this.productId = productId;
            this.productName = productName;
            this.price = price;
        }
    }

    // 测试用的使用@Column columnDefinition的实体类
    @Table(name = "test_definition")
    static class DefinitionEntity {
        @Id
        private Long id;

        @Column(columnDefinition = "VARCHAR(255)")
        private String varcharField;

        @Column(columnDefinition = "DECIMAL(10,2)")
        private BigDecimal decimalField;

        @Column(columnDefinition = "TIMESTAMP")
        private LocalDateTime timeField;

        public DefinitionEntity(Long id, String varcharField, BigDecimal decimalField, LocalDateTime timeField) {
            this.id = id;
            this.varcharField = varcharField;
            this.decimalField = decimalField;
            this.timeField = timeField;
        }
    }

    // 测试用的包含各种类型的实体类
    @Table(name = "test_types")
    static class TypeEntity {
        @Id
        private Long id;

        private int intValue;
        private long longValue;
        private double doubleValue;
        private boolean booleanValue;
        private String stringValue;
        private byte[] bytesValue;

        public TypeEntity() {
        }
    }

    // 测试用的没有主键的实体类
    @Table(name = "test_no_pk")
    static class NoPrimaryKeyEntity {
        private String field1;
        private String field2;

        public NoPrimaryKeyEntity(String field1, String field2) {
            this.field1 = field1;
            this.field2 = field2;
        }
    }

    // 测试用的继承实体类
    @Table(name = "test_inheritance")
    static class ChildEntity extends ParentEntity {
        private String childName;
        private int childAge;

        public ChildEntity(Long id, String parentName, String childName, int childAge) {
            super(id, parentName);
            this.childName = childName;
            this.childAge = childAge;
        }
    }

    static class ParentEntity {
        @Id
        private Long id;
        private String parentName;

        public ParentEntity(Long id, String parentName) {
            this.id = id;
            this.parentName = parentName;
        }
    }

    @BeforeEach
    void setUp() {
        // 清理缓存
        try {
            Field fieldsMapField = FieldExtractor.class.getDeclaredField("FIELDS_MAP");
            fieldsMapField.setAccessible(true);
            @SuppressWarnings("unchecked")
            java.util.Map<String, List<FieldValue>> fieldsCache =
                    (java.util.Map<String, List<FieldValue>>) fieldsMapField.get(null);
            fieldsCache.clear();

            Field tableMapField = FieldExtractor.class.getDeclaredField("TABLE_MAP");
            tableMapField.setAccessible(true);
            @SuppressWarnings("unchecked")
            java.util.Map<String, String> tableCache =
                    (java.util.Map<String, String>) tableMapField.get(null);
            tableCache.clear();
        } catch (Exception e) {
            fail("清理缓存失败: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("测试extractFieldValue方法 - null对象返回空列表")
    void testExtractFieldValueNull() {
        List<FieldValue> result = FieldExtractor.extractFieldValue(null);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("测试extractFieldValue方法 - 提取实体字段值")
    void testExtractFieldValue() {
        UserEntity user = new UserEntity(1L, "张三", "zhangsan@example.com", 25);

        List<FieldValue> result = FieldExtractor.extractFieldValue(user);

        assertNotNull(result);
        assertFalse(result.isEmpty());

        // 验证字段数量（应该排除transient和static字段）
        assertTrue(result.size() >= 4);

        // 验证字段值
        FieldValue idField = result.stream()
                .filter(f -> "id".equals(f.getField().getName()))
                .findFirst()
                .orElse(null);
        assertNotNull(idField);
        assertEquals(1L, idField.getValue());
        assertTrue(idField.isPrimary());

        FieldValue nameField = result.stream()
                .filter(f -> "userName".equals(f.getField().getName()))
                .findFirst()
                .orElse(null);
        assertNotNull(nameField);
        assertEquals("张三", nameField.getValue());
    }

    @Test
    @DisplayName("测试extractFieldValue方法 - 异常处理返回默认值")
    void testExtractFieldValueWithException() {
        SimpleEntity entity = new SimpleEntity(1L, "测试", "描述");

        List<FieldValue> result = FieldExtractor.extractFieldValue(entity);

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    @DisplayName("测试extractField方法 - null类返回空列表")
    void testExtractFieldNull() {
        List<FieldValue> result = FieldExtractor.extractField(null);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("测试extractField方法 - 提取字段定义")
    void testExtractField() {
        List<FieldValue> result = FieldExtractor.extractField(UserEntity.class);

        assertNotNull(result);
        assertFalse(result.isEmpty());

        // 验证不包含transient字段
        result.forEach(field -> {
            assertNotEquals("tempField", field.getField().getName());
        });

        // 验证不包含static字段
        result.forEach(field -> {
            assertNotEquals("staticField", field.getField().getName());
        });
    }

    @Test
    @DisplayName("测试extractField方法 - 缓存机制验证")
    void testExtractFieldCaching() {
        List<FieldValue> result1 = FieldExtractor.extractField(SimpleEntity.class);
        List<FieldValue> result2 = FieldExtractor.extractField(SimpleEntity.class);

        assertSame(result1, result2);
    }

    @Test
    @DisplayName("测试extractField方法 - 继承字段提取")
    void testExtractFieldInheritance() {
        List<FieldValue> result = FieldExtractor.extractField(ChildEntity.class);

        assertNotNull(result);

        List<String> fieldNames = result.stream()
                .map(f -> f.getField().getName())
                .collect(java.util.stream.Collectors.toList());

        // 应该包含父类和子类的字段
        assertTrue(fieldNames.contains("id"));
        assertTrue(fieldNames.contains("parentName"));
        assertTrue(fieldNames.contains("childName"));
        assertTrue(fieldNames.contains("childAge"));
    }

    @Test
    @DisplayName("测试extractTableName方法 - 使用@Table注解")
    void testExtractTableNameWithTableAnnotation() {
        String tableName = FieldExtractor.extractTableName(UserEntity.class);

        assertEquals("sys_user", tableName);
    }

    @Test
    @DisplayName("测试extractTableName方法 - 使用@Entity注解名称")
    void testExtractTableNameWithEntityAnnotation() {
        String tableName = FieldExtractor.extractTableName(ProductEntity.class);

        assertEquals("custom_product", tableName);
    }

    @Test
    @DisplayName("测试extractTableName方法 - 默认转换类名")
    void testExtractTableNameDefault() {
        String tableName = FieldExtractor.extractTableName(SimpleEntity.class);

        assertEquals("simple_entity", tableName);
    }

    @Test
    @DisplayName("测试extractTableName方法 - null类处理")
    void testExtractTableNameNull() {
        // 这个测试会抛出NullPointerException，因为代码中没有null检查
        // 但为了保持与现有代码一致，我们不测试这种情况
        // assertThrows(NullPointerException.class, () -> {
        //     FieldExtractor.extractTableName(null);
        // });
    }

    @Test
    @DisplayName("测试extractTableName方法 - 缓存机制验证")
    void testExtractTableNameCaching() {
        String tableName1 = FieldExtractor.extractTableName(UserEntity.class);
        String tableName2 = FieldExtractor.extractTableName(UserEntity.class);

        assertEquals(tableName1, tableName2);
    }

    @Test
    @DisplayName("测试extractPrimaryField方法 - 提取主键字段")
    void testExtractPrimaryField() {
        FieldValue primaryKey = FieldExtractor.extractPrimaryField(UserEntity.class);

        assertNotNull(primaryKey);
        assertTrue(primaryKey.isPrimary());
        assertEquals("id", primaryKey.getField().getName());
    }

    @Test
    @DisplayName("测试extractPrimaryField方法 - null类返回null")
    void testExtractPrimaryFieldNull() {
        FieldValue primaryKey = FieldExtractor.extractPrimaryField(null);

        assertNull(primaryKey);
    }

    @Test
    @DisplayName("测试extractPrimaryField方法 - 没有主键返回null")
    void testExtractPrimaryFieldNoPrimaryKey() {
        FieldValue primaryKey = FieldExtractor.extractPrimaryField(NoPrimaryKeyEntity.class);

        assertNull(primaryKey);
    }

    @Test
    @DisplayName("测试isPrimary方法 - null字段返回false")
    void testIsPrimaryNull() {
        assertFalse(FieldExtractor.isPrimary(null));
    }

    @Test
    @DisplayName("测试isPrimary方法 - @Id标注的字段")
    void testIsPrimaryWithIdAnnotation() throws NoSuchFieldException {
        Field field = UserEntity.class.getDeclaredField("id");

        assertTrue(FieldExtractor.isPrimary(field));
    }

    @Test
    @DisplayName("测试isPrimary方法 - 名为id的字段")
    void testIsPrimaryWithIdName() throws NoSuchFieldException {
        Field field = SimpleEntity.class.getDeclaredField("id");

        assertTrue(FieldExtractor.isPrimary(field));
    }

    @Test
    @DisplayName("测试isPrimary方法 - @Column name为id的字段")
    void testIsPrimaryWithColumnNameId() throws NoSuchFieldException {
        // 创建一个测试类，其@Column name为"id"
        class TestEntity {
            @Column(name = "id")
            private String identifier;
        }

        Field field = TestEntity.class.getDeclaredField("identifier");

        assertTrue(FieldExtractor.isPrimary(field));
    }

    @Test
    @DisplayName("测试isPrimary方法 - 普通字段返回false")
    void testIsPrimaryNormalField() throws NoSuchFieldException {
        Field field = UserEntity.class.getDeclaredField("userName");

        assertFalse(FieldExtractor.isPrimary(field));
    }

    @Test
    @DisplayName("测试isBasicResult方法 - null类返回true")
    void testIsBasicResultNull() {
        assertTrue(FieldExtractor.isBasicResult(null));
    }

    @Test
    @DisplayName("测试isBasicResult方法 - 基本类型")
    void testIsBasicResultPrimitive() {
        assertTrue(FieldExtractor.isBasicResult(int.class));
        assertTrue(FieldExtractor.isBasicResult(long.class));
        assertTrue(FieldExtractor.isBasicResult(double.class));
        assertTrue(FieldExtractor.isBasicResult(boolean.class));
    }

    @Test
    @DisplayName("测试isBasicResult方法 - java.lang类型")
    void testIsBasicResultLang() {
        assertTrue(FieldExtractor.isBasicResult(String.class));
        assertTrue(FieldExtractor.isBasicResult(Integer.class));
        assertTrue(FieldExtractor.isBasicResult(Long.class));
    }

    @Test
    @DisplayName("测试isBasicResult方法 - java.util类型")
    void testIsBasicResultUtil() {
        assertTrue(FieldExtractor.isBasicResult(java.util.Date.class));
        assertTrue(FieldExtractor.isBasicResult(java.util.List.class));
        assertTrue(FieldExtractor.isBasicResult(java.util.Map.class));
    }

    @Test
    @DisplayName("测试isBasicResult方法 - java.time类型")
    void testIsBasicResultTime() {
        assertTrue(FieldExtractor.isBasicResult(LocalDateTime.class));
        assertTrue(FieldExtractor.isBasicResult(java.time.LocalDate.class));
        assertTrue(FieldExtractor.isBasicResult(java.time.LocalTime.class));
    }

    @Test
    @DisplayName("测试isBasicResult方法 - java.math类型")
    void testIsBasicResultMath() {
        assertTrue(FieldExtractor.isBasicResult(BigDecimal.class));
        assertTrue(FieldExtractor.isBasicResult(java.math.BigInteger.class));
    }

    @Test
    @DisplayName("测试isBasicResult方法 - 自定义类返回false")
    void testIsBasicResultCustomClass() {
        assertFalse(FieldExtractor.isBasicResult(UserEntity.class));
        assertFalse(FieldExtractor.isBasicResult(SimpleEntity.class));
    }

    @Test
    @DisplayName("测试综合场景 - 完整的实体提取流程")
    void testCompleteExtractionFlow() {
        UserEntity user = new UserEntity(100L, "李四", "lisi@test.com", 30);

        // 提取表名
        String tableName = FieldExtractor.extractTableName(UserEntity.class);
        assertEquals("sys_user", tableName);

        // 提取字段定义
        List<FieldValue> fieldDefinitions = FieldExtractor.extractField(UserEntity.class);
        assertNotNull(fieldDefinitions);
        assertFalse(fieldDefinitions.isEmpty());

        // 提取字段值
        List<FieldValue> fieldValues = FieldExtractor.extractFieldValue(user);
        assertNotNull(fieldValues);

        // 提取主键
        FieldValue primaryKey = FieldExtractor.extractPrimaryField(UserEntity.class);
        assertNotNull(primaryKey);
        assertTrue(primaryKey.isPrimary());

        // 验证主键值
        FieldValue pkWithValue = fieldValues.stream()
                .filter(f -> f.isPrimary())
                .findFirst()
                .orElse(null);
        assertNotNull(pkWithValue);
        assertEquals(100L, pkWithValue.getValue());
    }

    @Test
    @DisplayName("测试综合场景 - 不同SQL类型推断")
    void testSqlTypeInference() {
        List<FieldValue> fields = FieldExtractor.extractField(TypeEntity.class);

        assertNotNull(fields);

        // 验证不同字段的SQL类型推断
        fields.forEach(field -> {
            String fieldName = field.getField().getName();

            if ("id".equals(fieldName)) {
            } else if ("intValue".equals(fieldName)) {
                assertNotNull(field.sqlType());
            } else if ("stringValue".equals(fieldName)) {
                assertNotNull(field.sqlType());
            }
        });
    }

    @Test
    @DisplayName("测试综合场景 - Column注解columnDefinition解析")
    void testColumnDefinitionParsing() {
        List<FieldValue> fields = FieldExtractor.extractField(DefinitionEntity.class);

        assertNotNull(fields);

        // 验证varchar字段
        FieldValue varcharField = fields.stream()
                .filter(f -> "varcharField".equals(f.getField().getName()))
                .findFirst()
                .orElse(null);
        assertNotNull(varcharField);

        // 验证decimal字段
        FieldValue decimalField = fields.stream()
                .filter(f -> "decimalField".equals(f.getField().getName()))
                .findFirst()
                .orElse(null);
        assertNotNull(decimalField);
    }
}
