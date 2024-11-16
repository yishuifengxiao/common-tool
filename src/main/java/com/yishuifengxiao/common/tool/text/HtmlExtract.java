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

import java.io.ByteArrayInputStream;
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
                log.info("There was a problem extracting {} using the CSS rules. The extraction parameters were " +
                                "cssLlector={}, attrName={}, and the problem was {}", html, cssSelector, attrName,
                        e.getMessage());
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
                log.info("There was a problem extracting {} using the CSS Text Rules. The extraction parameter is " +
                        "cssLlector={}, and the problem is {}", html, cssSelector, e.getMessage());
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
        try {
            SAXReader reader = new SAXReader();
            org.dom4j.Document doc = reader.read((new ByteArrayInputStream(xml.trim().getBytes("UTF-8"))));
            org.dom4j.Element root = doc.getRootElement();
            return root;
        } catch (Exception e) {
            if (log.isInfoEnabled()) {
                log.info("There was a problem parsing XML [{}], the problem is {}", xml, e.getMessage());
            }

        }
        return null;
    }

}
