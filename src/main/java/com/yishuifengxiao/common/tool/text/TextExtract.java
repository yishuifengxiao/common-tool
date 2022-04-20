package com.yishuifengxiao.common.tool.text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * 在线性时间内抽取主题类（新闻、博客等）网页的正文。 采用了<b>基于行块分布函数</b>的方法，为保持通用性没有针对特定网站编写规则。
 * </p>
 *
 * @author Chen Xin(xchen@ir.hit.edu.cn)
 */
public final class TextExtract {

	private static int FREQUENT_URL = 30;
	private static Pattern links = Pattern.compile(
			"<[aA]\\s+[Hh][Rr][Ee][Ff]=[\"|\']?([^>\"\' ]+)[\"|\']?\\s*[^>]*>([^>]+)</a>(\\s*.{0," + FREQUENT_URL
					+ "}\\s*<a\\s+href=[\"|\']?([^>\"\' ]+)[\"|\']?\\s*[^>]*>([^>]+)</[aA]>){2,100}",
			Pattern.DOTALL);

	private List<String> lines = new ArrayList<String>();

	private StringBuilder text = new StringBuilder();

	private ArrayList<Integer> indexDistribution = new ArrayList<Integer>();

	private final static int BLOCKS_WIDTH = 3;

	/**
	 * 当待抽取的网页正文中遇到成块的新闻标题未剔除时，只要增大此阈值即可。阈值增大，准确率提升，召回率下降；值变小，噪声会大，但可以保证抽到只有一句话的正文
	 */
	private int threshold;

	private int start;

	private int end;

	public TextExtract() {

		this(-1);
	}

	/**
	 * 构造函数
	 * 
	 * @param threshold 当待抽取的网页正文中遇到成块的新闻标题未剔除时，只要增大此阈值即可。阈值增大，准确率提升，召回率下降；值变小，噪声会大，但可以保证抽到只有一句话的正文
	 */
	public TextExtract(int threshold) {
		this.threshold = threshold;
	}

	public String extract(String html) {
		try {
			return this.parse(html);
		} catch (Exception e) {

		}
		return null;
	}

	/**
	 * 判断传入HTML，若是主题类网页，则抽取正文；否则输出<b>"unkown"</b>。
	 *
	 * @param html 网页HTML字符串
	 * @return 网页正文string
	 */
	private String parse(String html) {
		// 预处理，去掉非法字符
		html = this.preProcess(html);
		this.lines = Arrays.asList(html.split("\n"));
		this.indexDistribution.clear();
		// 空行的数量
		int empty = 0;
		for (int i = 0; i < this.lines.size() - BLOCKS_WIDTH; i++) {

			if (this.lines.get(i).length() == 0) {
				empty++;
			}

			int wordsNum = 0;
			for (int j = i; j < i + BLOCKS_WIDTH; j++) {
				this.lines.set(j, this.lines.get(j).replaceAll("\\s+", ""));
				wordsNum += this.lines.get(j).length();
			}
			this.indexDistribution.add(wordsNum);
		}
		int sum = 0;

		for (int i = 0; i < this.indexDistribution.size(); i++) {
			sum += this.indexDistribution.get(i);
		}

		try {
			this.threshold = Math.min(100,
					(sum / this.indexDistribution.size()) << (empty / (this.lines.size() - empty) >>> 1));
			this.threshold = Math.max(50, this.threshold);
		} catch (Exception e) {

		}

		this.start = -1;
		this.end = -1;
		this.text.setLength(0);
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

		boolean boolstart = false, boolend = false;
		// 前面的标题块往往比较小，应该减小与它匹配的阈值
		boolean firstMatch = true;

		StringBuilder buffer = new StringBuilder();
		for (int i = 0; i < this.indexDistribution.size() - 1; i++) {

			if (firstMatch && !boolstart) {
				if (this.indexDistribution.get(i) > (this.threshold / 2) && !boolstart) {
					if (this.indexDistribution.get(i + 1).intValue() != 0
							|| this.indexDistribution.get(i + 2).intValue() != 0) {
						firstMatch = false;
						boolstart = true;
						this.start = i;
						continue;
					}
				}

			}
			if (this.indexDistribution.get(i) > this.threshold && !boolstart) {
				if (this.indexDistribution.get(i + 1).intValue() != 0
						|| this.indexDistribution.get(i + 2).intValue() != 0
						|| this.indexDistribution.get(i + 3).intValue() != 0) {
					boolstart = true;
					this.start = i;
					continue;
				}
			}
			if (boolstart) {
				if (this.indexDistribution.get(i).intValue() == 0
						|| this.indexDistribution.get(i + 1).intValue() == 0) {
					this.end = i;
					boolend = true;
				}
			}

			if (boolend) {
				buffer.setLength(0);
				for (int ii = this.start; ii <= this.end; ii++) {
					if (this.lines.get(ii).length() < 5) {
						continue;
					}
					buffer.append(this.lines.get(ii) + "\n");
				}
				String str = buffer.toString();
				if (containsAnyIgnoreCase(str)) {
					continue;
				}
				this.text.append(str);
				boolstart = boolend = false;
			}
		}

		if (this.start > this.end) {
			buffer.setLength(0);
			for (int ii = this.start; ii <= (this.lines.size() - 1); ii++) {
				if (this.lines.get(ii).length() < 5) {
					continue;
				}
				buffer.append(this.lines.get(ii) + "\n");
			}
			String str = buffer.toString();
			if (!containsAnyIgnoreCase(str)) {
				this.text.append(str);
			}
		}

		return this.text.toString();
	}

	/**
	 * 判断文本是否包含需要过滤掉的字符
	 * 
	 * @param text 待判断的文本
	 * @return 若包含需要过滤掉的字符返回为true,否则为false
	 */
	private boolean containsAnyIgnoreCase(String text) {
		if (StringUtils.isBlank(text)) {
			return false;
		}
		return StringUtils.containsAny(text.toLowerCase(), "Copyright".toLowerCase(), "版权所有");
	}
}