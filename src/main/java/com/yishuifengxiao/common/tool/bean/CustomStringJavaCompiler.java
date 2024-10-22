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
     * 源码
     */
    private String sourceCode;
    /**
     * 存放编译之后的字节码(key:类全名,value:编译之后输出的字节码)
     */
    private final Map<String, ByteJavaFileObject> javaFileObjectMap = new ConcurrentHashMap<>();
    /**
     * 获取java的编译器
     */
    private final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    /**
     * 存放编译过程中输出的信息
     */
    private final DiagnosticCollector<JavaFileObject> diagnosticsCollector = new DiagnosticCollector<>();


    /**
     * 是否编译过
     */
    private Boolean compile;

    /**
     * 默认构造器
     *
     * @param sourceCode 源代码
     */
    public CustomStringJavaCompiler(String sourceCode) {
        this.sourceCode = sourceCode;
        this.fullClassName = getFullClassName(sourceCode);

    }

    /**
     * 编译字符串源代码,编译失败在 diagnosticsCollector 中获取提示信息
     *
     * @return true:编译成功 false:编译失败
     */
    public boolean compiler() {
        Assert.isNotBlank("不是java代码", this.fullClassName);
        if (null == this.compile) {
            //标准的内容管理器,更换成自己的实现，覆盖部分方法
            StandardJavaFileManager standardFileManager = compiler.getStandardFileManager(diagnosticsCollector, null, null);
            JavaFileManager javaFileManager = new StringJavaFileManage(standardFileManager);
            //构造源代码对象
            JavaFileObject javaFileObject = new StringJavaFileObject(fullClassName, sourceCode);
            //获取一个编译任务
            JavaCompiler.CompilationTask task = compiler.getTask(null, javaFileManager, diagnosticsCollector, null, null, Arrays.asList(javaFileObject));
            this.compile = task.call();
        }
        return this.compile;
    }


    /**
     * 获取源码中第一个public class 对应的 Class
     *
     * @return 第一个public class 对应的 Class
     * @throws Exception 加载失败
     */
    public Class<?> result() throws Exception {
        return result(fullClassName);
    }


    /**
     * 获取指定路径的Class
     *
     * @param className Class的全路径
     * @return 指定路径的Class
     * @throws Exception 加载失败
     */
    public Class<?> result(String className) throws Exception {
        boolean res = this.compiler();
        if (!res) {
            throw CustomException.of(getCompilerMessage());
        }
        Class<?> clazz = new StringClassLoader().findClass(className);
        return clazz;
    }

    /**
     * @return 编译信息(错误 警告)
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
     * 获取类的全名称
     *
     * @param sourceCode 源码
     * @return 类的全名称
     */
    public static String getFullClassName(String sourceCode) {

        String className = RegexUtil.extract("public\\s+class\\s+\\S+", sourceCode);
        String packageName = RegexUtil.extract("package\\s+\\S+", sourceCode);
        packageName = StringUtils.trim(StringUtils.substringBetween(packageName, "package", ";"));
        className = StringUtils.trim(StringUtils.substringAfterLast(className, "class"));
        return StringUtils.isBlank(packageName) ? className : packageName + "." + className;
    }

    /**
     * 自定义一个字符串的源码对象
     */
    private class StringJavaFileObject extends SimpleJavaFileObject {
        /**
         * 等待编译的源码字段
         */
        private String contents;

        /**
         * java源代码 => StringJavaFileObject对象 的时候使用
         *
         * @param className
         * @param contents
         */
        public StringJavaFileObject(String className, String contents) {
            super(URI.create("string:///" + className.replaceAll("\\.", "/") + Kind.SOURCE.extension), Kind.SOURCE);
            this.contents = contents;
        }

        /**
         * 字符串源码会调用该方法
         *
         * @param ignoreEncodingErrors ignore encoding errors if true
         * @return
         * @throws IOException
         */
        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
            return contents;
        }

    }

    /**
     * 自定义一个编译之后的字节码对象
     */
    private class ByteJavaFileObject extends SimpleJavaFileObject {
        /**
         * 存放编译后的字节码
         */
        private ByteArrayOutputStream outPutStream;

        public ByteJavaFileObject(String className, Kind kind) {
            super(URI.create("string:///" + className.replaceAll("\\.", "/") + Kind.SOURCE.extension), kind);
        }

        /**
         * StringJavaFileManage 编译之后的字节码输出会调用该方法（把字节码输出到outputStream）
         *
         * @return
         */
        @Override
        public OutputStream openOutputStream() {
            outPutStream = new ByteArrayOutputStream();
            return outPutStream;
        }

        /**
         * 在类加载器加载的时候需要用到
         *
         * @return
         */
        public byte[] getCompiledBytes() {
            return outPutStream.toByteArray();
        }
    }

    /**
     * 自定义一个JavaFileManage来控制编译之后字节码的输出位置
     */
    @SuppressWarnings("rawtypes")
    private class StringJavaFileManage extends ForwardingJavaFileManager {
        @SuppressWarnings("unchecked")
        StringJavaFileManage(JavaFileManager fileManager) {
            super(fileManager);
        }

        /**
         * 获取输出的文件对象，它表示给定位置处指定类型的指定类。
         *
         * @param location  a package-oriented location
         * @param className the name of a class
         * @param kind      the kind of file, must be one of {@link
         *                  JavaFileObject.Kind#SOURCE SOURCE} or {@link
         *                  JavaFileObject.Kind#CLASS CLASS}
         * @param sibling   a file object to be used as hint for placement;
         *                  might be {@code null}
         * @return
         * @throws IOException
         */
        @Override
        public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) throws IOException {
            ByteJavaFileObject javaFileObject = new ByteJavaFileObject(className, kind);
            javaFileObjectMap.put(className, javaFileObject);
            return javaFileObject;
        }
    }

    /**
     * 自定义类加载器, 用来加载动态的字节码
     */
    private class StringClassLoader extends ClassLoader {
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

