package com.yishuifengxiao.common.tool.text;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>网页正文抽取工具类</p>
 * <p>在线性时间内抽取主题类（新闻、博客等）网页的正文内容。</p>
 * <p>采用基于行块分布函数的方法，为保持通用性没有针对特定网站编写规则。</p>
 * <p>特性：</p>
 * <ul>
 * <li>自动过滤HTML标签、脚本、样式</li>
 * <li>剔除广告链接和噪声内容</li>
 * <li>基于行块分布算法提取正文</li>
 * <li>支持阈值调整以平衡准确率和召回率</li>
 * </ul>
 *
 * @author Chen Xin(xchen@ir.hit.edu.cn)
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
public final class TextExtract {

    private static final int FREQUENT_URL = 30;
    /**
     * 连续链接块匹配模式：限制字符类范围，避免在超大 HTML 上因 . 与 DOTALL 组合产生严重回溯
     */
    private static final Pattern LINKS = Pattern.compile("<[aA]\\s+[Hh][Rr][Ee][Ff]=[\"|\']?([^>\"\' ]+)[\"|\']?\\s*[^>]*>([^>]+)</a>(\\s*[^<]{0," + FREQUENT_URL + "}\\s*<a\\s+href=[\"|\']?([^>\"\' ]+)[\"|\']?\\s*[^>]*>([^>]+)</[aA]>){2,100}");

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

    /**
     * 构造函数
     */
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

    /**
     * 提取html中的正文
     *
     * @param html 待提取的html内容
     * @return 提取后的内容
     */
    public String extract(String html) {
        String result = null;
        try {
            html = HtmlExtract.preprocessHtml(html);
            result = this.parse(html);
        } catch (Exception e) {
            // 正文抽取失败时返回null，同时记录原因便于排查，避免异常被静默吞掉
            log.warn("网页正文抽取失败: {}", e.getMessage());
        }
        return result;
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
            this.threshold = Math.min(100, (sum / this.indexDistribution.size()) << (empty / (this.lines.size() - empty) >>> 1));
            this.threshold = Math.max(50, this.threshold);
        } catch (Exception e) {
            // 空行占比计算可能因除零而失败，此时保持默认阈值并记录原因
            log.debug("正文阈值计算失败，沿用默认阈值: {}", e.getMessage());
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
        while ((source = LINKS.matcher(source).replaceAll("")).length() != len) {
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
        int size = this.indexDistribution.size();
        for (int i = 0; i < size; i++) {
            // 预读后续块的行数，越界位置按 0 处理，避免尾部下标越界
            int next1 = i + 1 < size ? this.indexDistribution.get(i + 1).intValue() : 0;
            int next2 = i + 2 < size ? this.indexDistribution.get(i + 2).intValue() : 0;
            int next3 = i + 3 < size ? this.indexDistribution.get(i + 3).intValue() : 0;

            if (firstMatch && !boolstart) {
                if (this.indexDistribution.get(i) > (this.threshold / 2) && !boolstart) {
                    if (next1 != 0 || next2 != 0) {
                        firstMatch = false;
                        boolstart = true;
                        this.start = i;
                        continue;
                    }
                }

            }
            if (this.indexDistribution.get(i) > this.threshold && !boolstart) {
                if (next1 != 0 || next2 != 0 || next3 != 0) {
                    boolstart = true;
                    this.start = i;
                    continue;
                }
            }
            if (boolstart) {
                if (this.indexDistribution.get(i).intValue() == 0 || next1 == 0) {
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
     * @return 若包含需要过滤掉的字符返回为true, 否则为false
     */
    private boolean containsAnyIgnoreCase(String text) {
        if (StringUtils.isBlank(text)) {
            return false;
        }
        String lower = text.toLowerCase(Locale.ROOT);
        return lower.contains("copyright") || lower.contains("版权所有");
    }
}