/**
 *
 */
package com.yishuifengxiao.common.tool.text;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.xml.sax.SAXException;

import com.yishuifengxiao.common.tool.collections.CollUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>HTML提取工具类</p>
 * <p>提供HTML内容解析和数据提取功能，支持CSS选择器和XPath两种提取方式。</p>
 * <p>特性：</p>
 * <ul>
 * <li>使用CSS选择器提取元素</li>
 * <li>使用XPath表达式提取数据</li>
 * <li>支持提取元素属性和文本内容</li>
 * <li>HTML预处理转换为XHTML格式</li>
 * </ul>
 *
 * @author qingteng
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
public final class HtmlExtract {

    /**
     * <p>
     * 使用css提取器提取html中的所有符合条件的元素数据
     * </p>
     * <p>
     * 其中 元素的属性名 为选填参数，若该参数存在且不为空则表示提取元素的属性，否则提取整个元素
     * </p>
     *
     * @param cssSelector css选择器,必填
     * @param attrName    元素的属性名
     * @param html        待提取的html,必填
     * @return 提取之后的数据，若必填参数为空则返回null
     */
    public static List<String> extractByCss(String html, String cssSelector, String attrName) {
        List<String> list = new ArrayList<>();
        if (!StringUtils.isNoneBlank(cssSelector, html)) {
            return list;
        }
        try {
            Document document = Jsoup.parse(html.trim());
            Elements elements = document.select(cssSelector.trim());
            if (elements == null) {
                return list;
            }

            elements.forEach(e -> {
                list.add(StringUtils.isBlank(attrName) ? e.outerHtml() : e.attr(attrName.trim()));
            });
        } catch (Exception e) {
            if (log.isInfoEnabled()) {
                log.info("There was a problem extracting {} using the CSS rules. The extraction parameters were " + "cssLlector={}, attrName={}, and the problem was {}", html, cssSelector, attrName, e.getMessage());
            }

        }
        return list;
    }

    /**
     * <p>
     * 使用css提取器提取html中的一个符合条件的元素数据
     * </p>
     * <p>
     * 其中 元素的属性名 为选填参数，若该参数存在且不为空则表示提取元素的属性，否则提取整个元素
     * </p>
     *
     * @param html        待提取的html,必填
     * @param cssSelector css选择器,必填
     * @param attrName    元素的属性名
     * @return 提取之后的数据，若必填参数为空则返回null
     */
    public static String extractAnyByCss(String html, String cssSelector, String attrName) {
        List<String> list = extractByCss(html, cssSelector, attrName);
        return null != list && !list.isEmpty() ? list.get(0) : null;
    }

    /**
     * <p>
     * 使用css提取器提取html中的所有符合条件的元素数据
     * </p>
     *
     * @param cssSelector css选择器,必填
     * @param html        待提取的html,必填
     * @return 提取之后的数据，若必填参数为空则返回null
     */
    public static List<String> extractByCss(String html, String cssSelector) {
        return extractByCss(html, cssSelector, null);
    }

    /**
     * <p>
     * 使用css提取器提取html中的一个符合条件的元素数据
     * </p>
     *
     * @param html        待提取的html,必填
     * @param cssSelector css选择器,必填
     * @return 提取之后的数据，若必填参数为空则返回null
     */
    public static String extractAnyByCss(String html, String cssSelector) {
        return extractAnyByCss(html, cssSelector, null);
    }

    /**
     * <p>
     * 使用css提取器提取html中的所有符合条件的元素的文本
     * </p>
     *
     * @param html        待提取的html,必填
     * @param cssSelector css选择器,必填
     * @return 提取之后的数据，若必填参数为空则返回null
     */
    public static List<String> extractTextByCss(String html, String cssSelector) {
        List<String> list = new ArrayList<>();
        if (!StringUtils.isNoneBlank(cssSelector, html)) {
            return list;
        }
        try {
            Document document = Jsoup.parse(html.trim());
            Elements elements = document.select(cssSelector.trim());
            if (elements == null) {
                return list;
            }
            elements.forEach(e -> {
                list.add(e.ownText());
            });
        } catch (Exception e) {
            if (log.isInfoEnabled()) {
                log.info("There was a problem extracting {} using the CSS Text Rules. The extraction parameter is " + "cssLlector={}, and the problem is {}", html, cssSelector, e.getMessage());
            }

        }
        return list;
    }

    /**
     * <p>
     * 使用css提取器提取html中的一个符合条件的元素的文本
     * </p>
     *
     * @param html        待提取的html,必填
     * @param cssSelector css选择器,必填
     * @return 提取之后的数据，若必填参数为空则返回null
     */
    public static String extractAnyTextByCss(String html, String cssSelector) {
        List<String> list = extractTextByCss(html, cssSelector);
        return CollUtil.isEmpty(list) ? null : list.get(0);
    }

    /**
     * 使用xpath表达式提取所有符合条件的数据
     *
     * @param html  待提取的html,必填
     * @param xpath xpath表达式,必填
     * @return 提取之后的数据，若必填参数为空则返回null
     */
    public static List<String> extractByXpath(String html, String xpath) {
        List<String> list = new ArrayList<>();
        if (!StringUtils.isNoneBlank(xpath, html)) {
            return list;
        }
        org.dom4j.Element element = element(html);
        if (null == element) {
            return list;
        }
        List<org.dom4j.Node> nodes = element.selectNodes(xpath);
        if (null == nodes) {
            return list;
        }
        return nodes.stream().map(Node::getText).collect(Collectors.toList());
    }

    /**
     * 使用xpath表达式提取一个符合条件的数据
     *
     * @param html  待提取的html,必填
     * @param xpath xpath表达式,必填
     * @return 提取之后的数据，若必填参数为空则返回null
     */
    public static String extractAnyByXpath(String html, String xpath) {
        List<String> list = extractByXpath(html, xpath);
        return CollUtil.isEmpty(list) ? null : list.get(0);
    }

    /**
     * 解析xml片段为Element
     *
     * @param xml 待处理的xml片段
     * @return 解析后的元素
     */
    public static org.dom4j.Element element(String xml) {
        if (StringUtils.isBlank(xml)) {
            return null;
        }
        //修复属性引号
        String content = xml.replaceAll("<(\\w+)(\\s+\\w+)=(?![\\\"'])([^\\s>]+)", "<$1$2=\"$3\"");
        org.dom4j.Document doc = parseXhtml(content);
        if (null == doc) {
            doc = parseXhtml(preprocessHtml(xml));
        }
        if (null == doc) {
            return null;
        }
        org.dom4j.Element root = doc.getRootElement();
        return root;
    }

    /**
     * 使用Jsoup解析HTML并转换为格式良好的XHTML文档
     * 此方法首先使用Jsoup解析HTML，然后将其转换为XHTML格式，最后使用SAXReader解析XHTML
     *
     * @param html 待解析的HTML字符串
     * @return 解析后的org.dom4j.Document对象，如果解析失败则返回null
     */
    private static org.dom4j.Document parseXhtml(String html) {


        try {
            String xhtml = preprocessHtml(html);
            SAXReader reader = new SAXReader();
            reader.setValidation(false);
            try {
                reader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            } catch (SAXException e) {
                log.error("配置SAXReader安全特性失败", e);
            }
            return reader.read(new StringReader(xhtml));
        } catch (Exception e) {
            log.error("There was a problem parsing HTML [{}], the problem is ", html, e);
        }
        return null;
    }

    /**
     * 预处理HTML内容，将其转换为格式良好的XHTML
     * 此方法使用Jsoup解析HTML，然后将其转换为XHTML格式
     *
     * @param html 待预处理的HTML字符串
     * @return 格式良好的XHTML字符串，如果处理失败则返回原HTML
     */
    public static String preprocessHtml(String html) {
        // 处理边界条件：如果输入为null或空字符串，直接返回
        if (html == null || html.isEmpty()) {
            return html;
        }

        try {
            // 使用jsoup解析HTML并创建文档对象
            org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(html);
            // 设置输出语法为XML格式
            jsoupDoc.outputSettings().syntax(org.jsoup.nodes.Document.OutputSettings.Syntax.xml);
            // 设置XHTML转义模式
            jsoupDoc.outputSettings().escapeMode(org.jsoup.nodes.Entities.EscapeMode.xhtml);
            // 设置不美化输出，保持紧凑格式
            jsoupDoc.outputSettings().prettyPrint(false);

            String xhtml = jsoupDoc.html();
            return xhtml;
        } catch (Exception e) {
            // 发生异常时返回原始HTML，保持原有行为
            return html;
        }
    }
}
