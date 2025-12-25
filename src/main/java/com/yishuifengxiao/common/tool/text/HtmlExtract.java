/**
 *
 */
package com.yishuifengxiao.common.tool.text;

import com.yishuifengxiao.common.tool.collections.CollUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * html抽取工具
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
        org.dom4j.Document doc = null;
        try {
            SAXReader reader = new SAXReader();
            reader.setValidation(false);
            try {
                reader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            } catch (SAXException e) {
                log.error("配置SAXReader安全特性失败", e);
            }
            //修复属性引号
            String content = xml.replaceAll("<(\\w+)(\\s+\\w+)=(?![\\\"'])([^\\s>]+)", "<$1$2=\"$3\"");
            doc = reader.read((new ByteArrayInputStream(content.trim().getBytes("UTF-8"))));
        } catch (Exception e) {
            if (log.isInfoEnabled()) {
                log.info("There was a problem parsing XML [{}], the problem is {}", xml, e.getMessage());
            }
            doc = parseWithJsoup(xml);

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
    private static org.dom4j.Document parseWithJsoup(String html) {
        // 使用jsoup解析并返回格式良好的XHTML
        org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(html);
        // 使用XML语法
        jsoupDoc.outputSettings().syntax(org.jsoup.nodes.Document.OutputSettings.Syntax.xml);
        // XHTML转义
        jsoupDoc.outputSettings().escapeMode(org.jsoup.nodes.Entities.EscapeMode.xhtml);
        // 将jsoup文档输出为格式良好的XML/XHTML
        jsoupDoc.outputSettings().prettyPrint(false);  // 不美化输出

        String xhtml = jsoupDoc.html();

        try {
            SAXReader reader = new SAXReader();
            reader.setValidation(false);
            return reader.read(new StringReader(xhtml));
        } catch (Exception e) {
            log.error("There was a problem parsing HTML [{}], the problem is ", html, e);
        }
        return null;
    }

}
