package com.yishuifengxiao.common.tool.bean;

import com.yishuifengxiao.common.tool.exception.CustomException;
import com.yishuifengxiao.common.tool.text.RegexUtil;
import com.yishuifengxiao.common.tool.utils.Assert;
import org.apache.commons.lang3.StringUtils;

import javax.tools.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>
 * 源码编译器
 * </p>
 * <p>提供将Java源代码字符串动态编译并加载为Class对象的功能，支持运行时动态生成类。</p>
 *
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
public class CustomStringJavaCompiler {

    /**
     * 类全名
     */
    private String fullClassName;

    /**
     * 源代码
     */
    private String sourceCode;

    /**
     * 存放编译后的字节码（key:类全名, value:编译后输出的字节码）
     */
    private final Map<String, ByteJavaFileObject> javaFileObjectMap = new ConcurrentHashMap<>();

    /**
     * Java编译器实例
     */
    private final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

    /**
     * 存放编译过程中输出的诊断信息
     */
    private final DiagnosticCollector<JavaFileObject> diagnosticsCollector = new DiagnosticCollector<>();

    /**
     * 是否已编译
     */
    private Boolean compile;

    /**
     * 构造函数
     *
     * @param sourceCode Java源代码字符串
     */
    public CustomStringJavaCompiler(String sourceCode) {
        this.sourceCode = sourceCode;
        this.fullClassName = getFullClassName(sourceCode);
    }

    /**
     * 编译字符串源代码
     *
     * @return true:编译成功 false:编译失败（失败信息可通过getCompilerMessage获取）
     */
    public boolean compile() {
        Assert.isNotBlank("不是java代码", this.fullClassName);
        if (null == this.compile) {
            StandardJavaFileManager standardFileManager = compiler.getStandardFileManager(diagnosticsCollector, null, null);
            JavaFileManager javaFileManager = new StringJavaFileManage(standardFileManager);
            JavaFileObject javaFileObject = new StringJavaFileObject(fullClassName, sourceCode);
            JavaCompiler.CompilationTask task = compiler.getTask(null, javaFileManager, diagnosticsCollector, null, null, Arrays.asList(javaFileObject));
            this.compile = task.call();
        }
        return this.compile;
    }

    /**
     * 获取源码中第一个public class对应的Class对象
     *
     * @return 第一个public class对应的Class对象
     * @throws Exception 加载失败时抛出异常
     */
    public Class<?> loadClass() throws Exception {
        return loadClass(fullClassName);
    }

    /**
     * 获取指定全路径的Class对象
     *
     * @param className Class的全路径名称
     * @return 指定路径的Class对象
     * @throws Exception 加载失败时抛出异常
     */
    public Class<?> loadClass(String className) throws Exception {
        boolean res = this.compile();
        if (!res) {
            throw CustomException.of(getCompilerMessage());
        }
        return new StringClassLoader().findClass(className);
    }

    /**
     * 获取编译信息（错误和警告）
     *
     * @return 编译诊断信息字符串
     */
    @SuppressWarnings("rawtypes")
    public String getCompilerMessage() {
        StringBuilder sb = new StringBuilder();
        List<Diagnostic<? extends JavaFileObject>> diagnostics = diagnosticsCollector.getDiagnostics();
        for (Diagnostic diagnostic : diagnostics) {
            sb.append(diagnostic.toString()).append("\r\n");
        }
        return sb.toString();
    }

    /**
     * 从源代码中提取类的全名称
     *
     * @param sourceCode Java源代码
     * @return 类的全名称（包名.类名）
     */
    public static String getFullClassName(String sourceCode) {
        String className = RegexUtil.extract("public\\s+class\\s+\\S+", sourceCode);
        String packageName = RegexUtil.extract("package\\s+\\S+", sourceCode);
        packageName = StringUtils.trim(StringUtils.substringBetween(packageName, "package", ";"));
        className = StringUtils.trim(StringUtils.substringAfterLast(className, "class"));
        return StringUtils.isBlank(packageName) ? className : packageName + "." + className;
    }

    /**
     * 自定义字符串源码对象，用于表示等待编译的源代码
     */
    private class StringJavaFileObject extends SimpleJavaFileObject {

        /**
         * 等待编译的源码内容
         */
        private String contents;

        /**
         * 构造函数
         *
         * @param className 类全名称
         * @param contents  源代码内容
         */
        public StringJavaFileObject(String className, String contents) {
            super(URI.create("string:///" + className.replaceAll("\\.", "/") + Kind.SOURCE.extension), Kind.SOURCE);
            this.contents = contents;
        }

        /**
         * 获取源码内容
         *
         * @param ignoreEncodingErrors 是否忽略编码错误
         * @return 源码内容
         * @throws IOException IO异常
         */
        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
            return contents;
        }
    }

    /**
     * 自定义字节码对象，用于存储编译后的字节码
     */
    private class ByteJavaFileObject extends SimpleJavaFileObject {

        /**
         * 存放编译后的字节码
         */
        private ByteArrayOutputStream outPutStream;

        /**
         * 构造函数
         *
         * @param className 类全名称
         * @param kind      文件类型
         */
        public ByteJavaFileObject(String className, Kind kind) {
            super(URI.create("string:///" + className.replaceAll("\\.", "/") + Kind.SOURCE.extension), kind);
        }

        /**
         * 获取输出流用于写入编译后的字节码
         *
         * @return 字节输出流
         */
        @Override
        public OutputStream openOutputStream() {
            outPutStream = new ByteArrayOutputStream();
            return outPutStream;
        }

        /**
         * 获取编译后的字节数组
         *
         * @return 字节数组
         */
        public byte[] getCompiledBytes() {
            return outPutStream.toByteArray();
        }
    }

    /**
     * 自定义JavaFileManager，控制编译后字节码的输出位置
     */
    @SuppressWarnings("rawtypes")
    private class StringJavaFileManage extends ForwardingJavaFileManager {

        /**
         * 构造函数
         *
         * @param fileManager 原始JavaFileManager
         */
        @SuppressWarnings("unchecked")
        StringJavaFileManage(JavaFileManager fileManager) {
            super(fileManager);
        }

        /**
         * 获取输出的文件对象
         *
         * @param location  包导向的位置
         * @param className 类名称
         * @param kind      文件类型（SOURCE或CLASS）
         * @param sibling   用于放置提示的文件对象，可能为null
         * @return JavaFileObject实例
         * @throws IOException IO异常
         */
        @Override
        public JavaFileObject getJavaFileForOutput(Location location, String className,
                                                   JavaFileObject.Kind kind, FileObject sibling) throws IOException {
            ByteJavaFileObject javaFileObject = new ByteJavaFileObject(className, kind);
            javaFileObjectMap.put(className, javaFileObject);
            return javaFileObject;
        }
    }

    /**
     * 自定义类加载器，用于加载动态编译的字节码
     */
    private class StringClassLoader extends ClassLoader {

        /**
         * 查找并加载指定名称的类
         *
         * @param name 类全名称
         * @return Class对象
         * @throws ClassNotFoundException 类未找到异常
         */
        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            ByteJavaFileObject fileObject = javaFileObjectMap.get(name);
            if (fileObject != null) {
                byte[] bytes = fileObject.getCompiledBytes();
                return defineClass(name, bytes, 0, bytes.length);
            }
            try {
                return CustomStringJavaCompiler.class.getClassLoader().loadClass(name);
            } catch (Exception e) {
                return super.findClass(name);
            }
        }
    }
}