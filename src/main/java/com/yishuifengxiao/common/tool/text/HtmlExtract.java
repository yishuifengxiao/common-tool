/**
 * 
 */
package com.yishuifengxiao.common.tool.text;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.seimicrawler.xpath.JXDocument;

import com.yishuifengxiao.common.tool.collections.EmptyUtil;

import lombok.extern.slf4j.Slf4j;

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
	public static List<String> extractAllByCss(String cssSelector, String attrName, String html) {
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
			log.info("使用【css规则】 提取 {} 时出现问题，提取参数为 cssSelector= {} ,attrName = {},问题为 {}", html, cssSelector, attrName,
					e.getMessage());
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
	 * @param cssSelector css选择器,必填
	 * @param attrName    元素的属性名
	 * @param html        待提取的html,必填
	 * @return 提取之后的数据，若必填参数为空则返回null
	 */
	public static String extractByCss(String cssSelector, String attrName, String html) {
		List<String> list = extractAllByCss(cssSelector, attrName, html);
		return EmptyUtil.isEmpty(list) ? null : list.get(0);
	}

	/**
	 * <p>
	 * 使用css提取器提取html中的所有符合条件的元素的文本
	 * </p>
	 * 
	 * @param cssSelector css选择器,必填
	 * @param html        待提取的html,必填
	 * @return 提取之后的数据，若必填参数为空则返回null
	 */
	public static List<String> extractAllTextByCss(String cssSelector, String html) {
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
			log.info("使用【css 文本规则】 提取 {} 时出现问题，提取参数为 cssSelector= {} ,问题为 {}", html, cssSelector, e.getMessage());
		}
		return list;
	}

	/**
	 * <p>
	 * 使用css提取器提取html中的一个符合条件的元素的文本
	 * </p>
	 * 
	 * @param cssSelector css选择器,必填
	 * @param html        待提取的html,必填
	 * @return 提取之后的数据，若必填参数为空则返回null
	 */
	public static String extractTextByCss(String cssSelector, String html) {
		List<String> list = extractAllTextByCss(cssSelector, html);
		return EmptyUtil.isEmpty(list) ? null : list.get(0);
	}

	/**
	 * 使用xpath表达式提取所有符合条件的数据
	 * 
	 * @param xpath xpath表达式,必填
	 * @param html  待提取的html,必填
	 * @return 提取之后的数据，若必填参数为空则返回null
	 */
	public static List<String> extractAllByXpath(String xpath, String html) {
		List<String> list = new ArrayList<>();
		if (!StringUtils.isNoneBlank(xpath, html)) {
			return list;
		}
		try {
			JXDocument jxDocument = JXDocument.create(html);
			List<Object> nodes = jxDocument.sel(xpath.trim());
			if (null == nodes) {
				return list;
			}
		} catch (Exception e) {
			log.info("使用【xpath规则】 提取 {} 时出现问题，提取参数为 xpath= {} ,问题为 {}", html, xpath, e.getMessage());
		}
		return list;
	}

	/**
	 * 使用xpath表达式提取一个符合条件的数据
	 * 
	 * @param xpath xpath表达式,必填
	 * @param html  待提取的html,必填
	 * @return 提取之后的数据，若必填参数为空则返回null
	 */
	public static String extracttByXpath(String xpath, String html) {
		List<String> list = extractAllTextByCss(xpath, html);
		return EmptyUtil.isEmpty(list) ? null : list.get(0);
	}

}
