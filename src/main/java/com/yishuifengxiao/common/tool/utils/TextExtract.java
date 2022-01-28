package com.yishuifengxiao.common.tool.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * <p>
 * 在线性时间内抽取主题类（新闻、博客等）网页的正文。 采用了<b>基于行块分布函数</b>的方法，为保持通用性没有针对特定网站编写规则。
 * </p>
 *
 * @author Chen Xin(xchen@ir.hit.edu.cn)
 */
public class TextExtract {

	private static int FREQUENT_URL = 30;
	private static Pattern links = Pattern.compile(
			"<[aA]\\s+[Hh][Rr][Ee][Ff]=[\"|\']?([^>\"\' ]+)[\"|\']?\\s*[^>]*>([^>]+)</a>(\\s*.{0," + FREQUENT_URL
					+ "}\\s*<a\\s+href=[\"|\']?([^>\"\' ]+)[\"|\']?\\s*[^>]*>([^>]+)</[aA]>){2,100}",
			Pattern.DOTALL);
	private List<String> lines;
	private final static int BLOCKS_WIDTH = 3;
	private int threshold;
	private String html;
	private int start;
	private int end;
	private StringBuilder text;
	private ArrayList<Integer> indexDistribution;

	public TextExtract() {
		this.lines = new ArrayList<String>();
		this.indexDistribution = new ArrayList<Integer>();
		this.text = new StringBuilder();
		/* 当待抽取的网页正文中遇到成块的新闻标题未剔除时，只要增大此阈值即可。 */
		/* 阈值增大，准确率提升，召回率下降；值变小，噪声会大，但可以保证抽到只有一句话的正文 */
		this.threshold = -1;
	}

	/**
	 * 判断传入HTML，若是主题类网页，则抽取正文；否则输出<b>"unkown"</b>。
	 *
	 * @param html 网页HTML字符串
	 * @return 网页正文string
	 */
	public String parse(String html) {
		this.html = this.preProcess(html);
		return this.getText();
	}

	/**
	 * 去除掉非法字符
	 * 
	 * @param html 原始的html
	 * @return 去除掉干扰字符的html
	 */
	private String preProcess(String html) {

		String source = html.replaceAll("(?is)<!DOCTYPE.*?>", "");
		// remove html comment
		source = source.replaceAll("(?is)<!--.*?-->", "");
		// remove javascript
		source = source.replaceAll("(?is)<script.*?>.*?</script>", "");
		// remove css
		source = source.replaceAll("(?is)<style.*?>.*?</style>", "");
		// remove special char
		source = source.replaceAll("&.{2,5};|&#.{2,5};", " ");

		// 剔除连续成片的超链接文本（认为是，广告或噪音）,超链接多藏于span中
		source = source.replaceAll("<[sS][pP][aA][nN].*?>", "");
		source = source.replaceAll("</[sS][pP][aA][nN]>", "");

		int len = source.length();
		while ((source = links.matcher(source).replaceAll("")).length() != len) {
			len = source.length();
		}

		// 防止html中在<>中包括大于号的判断
		source = source.replaceAll("<[^>'\"]*['\"].*['\"].*?>", "");

		source = source.replaceAll("<.*?>", "");
		source = source.replaceAll("<.*?>", "");
		source = source.replaceAll("\r\n", "\n");

		return source;

	}

	/**
	 * 获取网页内容
	 * 
	 * @return 网页内容
	 */
	private String getText() {
		lines = Arrays.asList(html.split("\n"));
		indexDistribution.clear();
		// 空行的数量
		int empty = 0;
		for (int i = 0; i < lines.size() - BLOCKS_WIDTH; i++) {

			if (lines.get(i).length() == 0) {
				empty++;
			}

			int wordsNum = 0;
			for (int j = i; j < i + BLOCKS_WIDTH; j++) {
				lines.set(j, lines.get(j).replaceAll("\\s+", ""));
				wordsNum += lines.get(j).length();
			}
			indexDistribution.add(wordsNum);
		}
		int sum = 0;

		for (int i = 0; i < indexDistribution.size(); i++) {
			sum += indexDistribution.get(i);
		}

		threshold = Math.min(100, (sum / indexDistribution.size()) << (empty / (lines.size() - empty) >>> 1));
		threshold = Math.max(50, threshold);

		start = -1;
		end = -1;
		boolean boolstart = false, boolend = false;
		// 前面的标题块往往比较小，应该减小与它匹配的阈值
		boolean firstMatch = true;
		text.setLength(0);

		StringBuilder buffer = new StringBuilder();
		for (int i = 0; i < indexDistribution.size() - 1; i++) {

			if (firstMatch && !boolstart) {
				if (indexDistribution.get(i) > (threshold / 2) && !boolstart) {
					if (indexDistribution.get(i + 1).intValue() != 0 || indexDistribution.get(i + 2).intValue() != 0) {
						firstMatch = false;
						boolstart = true;
						start = i;
						continue;
					}
				}

			}
			if (indexDistribution.get(i) > threshold && !boolstart) {
				if (indexDistribution.get(i + 1).intValue() != 0 || indexDistribution.get(i + 2).intValue() != 0
						|| indexDistribution.get(i + 3).intValue() != 0) {
					boolstart = true;
					start = i;
					continue;
				}
			}
			if (boolstart) {
				if (indexDistribution.get(i).intValue() == 0 || indexDistribution.get(i + 1).intValue() == 0) {
					end = i;
					boolend = true;
				}
			}

			if (boolend) {
				buffer.setLength(0);
				for (int ii = start; ii <= end; ii++) {
					if (lines.get(ii).length() < 5) {
						continue;
					}
					buffer.append(lines.get(ii) + "\n");
				}
				String str = buffer.toString();
				if (str.contains("Copyright") || str.contains("版权所有")) {
					continue;
				}
				text.append(str);
				boolstart = boolend = false;
			}
		}

		if (start > end) {
			buffer.setLength(0);
			for (int ii = start; ii <= (lines.size() - 1); ii++) {
				if (lines.get(ii).length() < 5) {
					continue;
				}
				buffer.append(lines.get(ii) + "\n");
			}
			String str = buffer.toString();
			if ((!str.contains("Copyright")) || (!str.contains("版权所有"))) {
				text.append(str);
			}
		}

		return text.toString();
	}
}