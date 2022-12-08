/**
 *
 */
package com.yishuifengxiao.common.tool.text;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jayway.jsonpath.JsonPath;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>json转换提取工具</p>
 *
 * <p>
 * JSONPath语法元素和对应XPath元素的对比语法如下：
 * </p>
 * <table style="border-spacing: 0; width: 842px; border: 1px solid rgba(0, 0,
 * 0, 1); font-size: 16px">
 * <caption>jsonpath语法</caption> <tbody>
 * <tr>
 * <td style="padding: 0.25em 0.5em; margin: 0; text-align: center;
 * border-right-color: rgba(0, 0, 0, 1); border-left-color: rgba(0, 0, 0, 1);
 * background-color: rgba(221, 221, 221, 1)"><span style="color: rgba(34, 0, 34,
 * 1)"><span style="font-family: verdana, lucida, arial, helvetica,
 * sans-serif"><span style="font-weight: 700">XPath</span></span></span></td>
 * <td style="padding: 0.25em 0.5em; margin: 0; text-align: center;
 * border-right-color: rgba(0, 0, 0, 1); border-left-color: rgba(0, 0, 0, 1);
 * background-color: rgba(221, 221, 221, 1)"><span style="color: rgba(34, 0, 34,
 * 1)"><span style="font-family: verdana, lucida, arial, helvetica,
 * sans-serif"><span style="font-weight: 700">JSONPath</span></span></span></td>
 * <td style="padding: 0.25em 0.5em; margin: 0; text-align: center;
 * border-right-color: rgba(0, 0, 0, 1); border-left-color: rgba(0, 0, 0, 1);
 * background-color: rgba(221, 221, 221, 1)"><span style="color: rgba(34, 0, 34,
 * 1)"><span style="font-family: verdana, lucida, arial, helvetica,
 * sans-serif"><span style="font-weight:
 * 700">Description</span></span></span></td>
 * </tr>
 * <tr>
 * <td style="padding: 0.25em 0.5em; margin: 0; text-align: center;
 * border-right-color: rgba(0, 0, 0, 1); border-left-color: rgba(0, 0, 0,
 * 1)"><span style="color: rgba(34, 0, 34, 1)"><span style="font-family:
 * verdana, lucida, arial, helvetica, sans-serif">/</span></span></td>
 * <td style="padding: 0.25em 0.5em; margin: 0; text-align: center;
 * border-right-color: rgba(0, 0, 0, 1); border-left-color: rgba(0, 0, 0,
 * 1)"><span style="color: rgba(34, 0, 34, 1)"><span style="font-family:
 * verdana, lucida, arial, helvetica, sans-serif">$</span></span></td>
 * <td style="padding: 0.25em 0.5em; margin: 0; border-right-color: rgba(0, 0,
 * 0, 1); border-left-color: rgba(0, 0, 0, 1)"><span style="color: rgba(34, 0,
 * 34, 1)"><span style="font-family: verdana, lucida, arial, helvetica,
 * sans-serif">表示根元素</span></span></td>
 * </tr>
 * <tr>
 * <td style="padding: 0.25em 0.5em; margin: 0; text-align: center;
 * border-right-color: rgba(0, 0, 0, 1); border-left-color: rgba(0, 0, 0, 1);
 * background-color: rgba(221, 221, 221, 1)"><span style="color: rgba(34, 0, 34,
 * 1)"><span style="font-family: verdana, lucida, arial, helvetica,
 * sans-serif">.</span></span></td>
 * <td style="padding: 0.25em 0.5em; margin: 0; text-align: center;
 * border-right-color: rgba(0, 0, 0, 1); border-left-color: rgba(0, 0, 0, 1);
 * background-color: rgba(221, 221, 221, 1)"><span style="color: rgba(34, 0,
 * 34,1)"> <span style="font-family: verdana, lucida, arial, helvetica,
 * sans-serif">&#64;</span></span></td>
 * <td style="padding: 0.25em 0.5em; margin: 0; border-right-color: rgba(0, 0,
 * 0, 1); border-left-color: rgba(0, 0, 0, 1); background-color: rgba(221, 221,
 * 221, 1)"><span style="color: rgba(34, 0, 34, 1)"><span style="font-family:
 * verdana, lucida, arial, helvetica, sans-serif">&nbsp;当前元素</span></span></td>
 * </tr>
 * <tr>
 * <td style="padding: 0.25em 0.5em; margin: 0; text-align: center;
 * border-right-color: rgba(0, 0, 0, 1); border-left-color: rgba(0, 0, 0,
 * 1)"><span style="color: rgba(34, 0, 34, 1)"><span style="font-family:
 * verdana, lucida, arial, helvetica, sans-serif">/</span></span></td>
 * <td style="padding: 0.25em 0.5em; margin: 0; text-align: center;
 * border-right-color: rgba(0, 0, 0, 1); border-left-color: rgba(0, 0, 0,
 * 1)"><span style="color: rgba(34, 0, 34, 1)"><span style="font-family:
 * verdana, lucida, arial, helvetica, sans-serif">. or []</span></span></td>
 * <td style="padding: 0.25em 0.5em; margin: 0; border-right-color: rgba(0, 0,
 * 0, 1); border-left-color: rgba(0, 0, 0, 1)"><span style="color: rgba(34, 0,
 * 34, 1)"><span style="font-family: verdana, lucida, arial, helvetica,
 * sans-serif">子元素</span></span></td>
 * </tr>
 * <tr>
 * <td style="padding: 0.25em 0.5em; margin: 0; text-align: center;
 * border-right-color: rgba(0, 0, 0, 1); border-left-color: rgba(0, 0, 0, 1);
 * background-color: rgba(221, 221, 221, 1)"><span style="color: rgba(34, 0, 34,
 * 1)"><span style="font-family: verdana, lucida, arial, helvetica,
 * sans-serif">..</span></span></td>
 * <td style="padding: 0.25em 0.5em; margin: 0; text-align: center;
 * border-right-color: rgba(0, 0, 0, 1); border-left-color: rgba(0, 0, 0, 1);
 * background-color: rgba(221, 221, 221, 1)"><span style="color: rgba(34, 0, 34,
 * 1)"><span style="font-family: verdana, lucida, arial, helvetica,
 * sans-serif">n/a</span></span></td>
 * <td style="padding: 0.25em 0.5em; margin: 0; border-right-color: rgba(0, 0,
 * 0, 1); border-left-color: rgba(0, 0, 0, 1); background-color: rgba(221, 221,
 * 221, 1)"><span style="color: rgba(34, 0, 34, 1)"><span style="font-family:
 * verdana, lucida, arial, helvetica, sans-serif">父元素</span></span></td>
 * </tr>
 * <tr>
 * <td style="padding: 0.25em 0.5em; margin: 0; text-align: center;
 * border-right-color: rgba(0, 0, 0, 1); border-left-color: rgba(0, 0, 0,
 * 1)"><span style="color: rgba(34, 0, 34, 1)"><span style="font-family:
 * verdana, lucida, arial, helvetica, sans-serif">//</span></span></td>
 * <td style="padding: 0.25em 0.5em; margin: 0; text-align: center;
 * border-right-color: rgba(0, 0, 0, 1); border-left-color: rgba(0, 0, 0,
 * 1)"><span style="color: rgba(34, 0, 34, 1)"><span style="font-family:
 * verdana, lucida, arial, helvetica, sans-serif">..</span></span></td>
 * <td style="padding: 0.25em 0.5em; margin: 0; border-right-color: rgba(0, 0,
 * 0, 1); border-left-color: rgba(0, 0, 0, 1)"><span style="color: rgba(34, 0,
 * 34, 1)"><span style="font-family: verdana, lucida, arial, helvetica,
 * sans-serif">递归下降，JSONPath是从E4X借鉴的。</span></span></td>
 * </tr>
 * <tr>
 * <td style="padding: 0.25em 0.5em; margin: 0; text-align: center;
 * border-right-color: rgba(0, 0, 0, 1); border-left-color: rgba(0, 0, 0, 1);
 * background-color: rgba(221, 221, 221, 1)"><span style="color: rgba(34, 0, 34,
 * 1)"><span style="font-family: verdana, lucida, arial, helvetica,
 * sans-serif">*</span></span></td>
 * <td style="padding: 0.25em 0.5em; margin: 0; text-align: center;
 * border-right-color: rgba(0, 0, 0, 1); border-left-color: rgba(0, 0, 0, 1);
 * background-color: rgba(221, 221, 221, 1)"><span style="color: rgba(34, 0, 34,
 * 1)"><span style="font-family: verdana, lucida, arial, helvetica,
 * sans-serif">*</span></span></td>
 * <td style="padding: 0.25em 0.5em; margin: 0; border-right-color: rgba(0, 0,
 * 0, 1); border-left-color: rgba(0, 0, 0, 1); background-color: rgba(221, 221,
 * 221, 1)"><span style="color: rgba(34, 0, 34, 1)"><span style="font-family:
 * verdana, lucida, arial, helvetica, sans-serif">通配符，表示所有的元素</span></span></td>
 * </tr>
 * <tr>
 * <td style="padding: 0.25em 0.5em; margin: 0; text-align: center;
 * border-right-color: rgba(0, 0, 0, 1); border-left-color: rgba(0, 0, 0,
 * 1)"><span style="color: rgba(34, 0, 34, 1)"><span style="font-family:
 * verdana, lucida, arial, helvetica, sans-serif">&#64;</span></span></td>
 * <td style="padding: 0.25em 0.5em; margin: 0; text-align: center;
 * border-right-color: rgba(0, 0, 0, 1); border-left-color: rgba(0, 0, 0,
 * 1)"><span style="color: rgba(34, 0, 34, 1)"><span style="font-family:
 * verdana, lucida, arial, helvetica, sans-serif">n/a</span></span></td>
 * <td style="padding: 0.25em 0.5em; margin: 0; border-right-color: rgba(0, 0,
 * 0, 1); border-left-color: rgba(0, 0, 0, 1)"><span style="color: rgba(34, 0,
 * 34, 1)"><span style="font-family: verdana, lucida, arial, helvetica,
 * sans-serif">&nbsp;属性访问字符</span></span></td>
 * </tr>
 * <tr>
 * <td style="padding: 0.25em 0.5em; margin: 0; text-align: center;
 * border-right-color: rgba(0, 0, 0, 1); border-left-color: rgba(0, 0, 0, 1);
 * background-color: rgba(221, 221, 221, 1)"><span style="color: rgba(34, 0, 34,
 * 1)"><span style="font-family: verdana, lucida, arial, helvetica,
 * sans-serif">[]</span></span></td>
 * <td style="padding: 0.25em 0.5em; margin: 0; text-align: center;
 * border-right-color: rgba(0, 0, 0, 1); border-left-color: rgba(0, 0, 0, 1);
 * background-color: rgba(221, 221, 221, 1)"><span style="color: rgba(34, 0, 34,
 * 1)"><span style="font-family: verdana, lucida, arial, helvetica,
 * sans-serif">[]</span></span></td>
 * <td style="padding: 0.25em 0.5em; margin: 0; border-right-color: rgba(0, 0,
 * 0, 1); border-left-color: rgba(0, 0, 0, 1); background-color: rgba(221, 221,
 * 221, 1)"><div style="padding: 0; margin: 0">子元素操作符</div></td>
 * </tr>
 * <tr>
 * <td style="padding: 0.25em 0.5em; margin: 0; text-align: center;
 * border-right-color: rgba(0, 0, 0, 1); border-left-color: rgba(0, 0, 0,
 * 1)"><span style="color: rgba(34, 0, 34, 1)"><span style="font-family:
 * verdana, lucida, arial, helvetica, sans-serif">|</span></span></td>
 * <td style="padding: 0.25em 0.5em; margin: 0; text-align: center;
 * border-right-color: rgba(0, 0, 0, 1); border-left-color: rgba(0, 0, 0,
 * 1)"><span style="color: rgba(34, 0, 34, 1)"><span style="font-family:
 * verdana, lucida, arial, helvetica, sans-serif">[,]</span></span></td>
 * <td style="padding: 0.25em 0.5em; margin: 0; border-right-color: rgba(0, 0,
 * 0, 1); border-left-color: rgba(0, 0, 0, 1)"><div style="padding: 0; margin:
 * 0"><span style="color: rgba(34, 0, 34, 1); font-family: verdana, lucida,
 * arial, helvetica, sans-serif">连接操作符在XPath
 * 结果合并其它结点集合。JSONP允许name或者数组索引。</span></div></td>
 * </tr>
 * <tr>
 * <td style="padding: 0.25em 0.5em; margin: 0; text-align: center;
 * border-right-color: rgba(0, 0, 0, 1); border-left-color: rgba(0, 0, 0, 1);
 * background-color: rgba(221, 221, 221, 1)"><span style="color: rgba(34, 0, 34,
 * 1)"><span style="font-family: verdana, lucida, arial, helvetica,
 * sans-serif">n/a</span></span></td>
 * <td style="padding: 0.25em 0.5em; margin: 0; text-align: center;
 * border-right-color: rgba(0, 0, 0, 1); border-left-color: rgba(0, 0, 0, 1);
 * background-color: rgba(221, 221, 221, 1)"><span style="color: rgba(34, 0, 34,
 * 1)"><span style="font-family: verdana, lucida, arial, helvetica,
 * sans-serif">[start:end:step]</span></span></td>
 * <td style="padding: 0.25em 0.5em; margin: 0; border-right-color: rgba(0, 0,
 * 0, 1); border-left-color: rgba(0, 0, 0, 1); background-color: rgba(221, 221,
 * 221, 1)"><div style="padding: 0; margin: 0"><span style="color: rgba(34, 0,
 * 34, 1); font-family: verdana, lucida, arial, helvetica,
 * sans-serif">数组分割操作从ES4借鉴。</span></div></td>
 * </tr>
 * <tr>
 * <td style="padding: 0.25em 0.5em; margin: 0; text-align: center;
 * border-right-color: rgba(0, 0, 0, 1); border-left-color: rgba(0, 0, 0,
 * 1)"><span style="color: rgba(34, 0, 34, 1)"><span style="font-family:
 * verdana, lucida, arial, helvetica, sans-serif">[]</span></span></td>
 * <td style="padding: 0.25em 0.5em; margin: 0; text-align: center;
 * border-right-color: rgba(0, 0, 0, 1); border-left-color: rgba(0, 0, 0,
 * 1)"><span style="color: rgba(34, 0, 34, 1)"><span style="font-family:
 * verdana, lucida, arial, helvetica, sans-serif">?()</span></span></td>
 * <td style="padding: 0.25em 0.5em; margin: 0; border-right-color: rgba(0, 0,
 * 0, 1); border-left-color: rgba(0, 0, 0, 1)"><div style="padding: 0; margin:
 * 0"><span style="color: rgba(34, 0, 34, 1); font-family: verdana, lucida,
 * arial, helvetica, sans-serif">应用过滤表示式</span></div></td>
 * </tr>
 * <tr>
 * <td style="padding: 0.25em 0.5em; margin: 0; text-align: center;
 * border-right-color: rgba(0, 0, 0, 1); border-left-color: rgba(0, 0, 0, 1);
 * background-color: rgba(221, 221, 221, 1)"><span style="color: rgba(34, 0, 34,
 * 1)"><span style="font-family: verdana, lucida, arial, helvetica,
 * sans-serif">n/a</span></span></td>
 * <td style="padding: 0.25em 0.5em; margin: 0; text-align: center;
 * border-right-color: rgba(0, 0, 0, 1); border-left-color: rgba(0, 0, 0, 1);
 * background-color: rgba(221, 221, 221, 1)"><span style="color: rgba(34, 0, 34,
 * 1)"><span style="font-family: verdana, lucida, arial, helvetica,
 * sans-serif">()</span></span></td>
 * <td style="padding: 0.25em 0.5em; margin: 0; border-right-color: rgba(0, 0,
 * 0, 1); border-left-color: rgba(0, 0, 0, 1); background-color: rgba(221, 221,
 * 221, 1)"><div style="padding: 0; margin: 0"><span style="color: rgba(34, 0,
 * 34, 1); font-family: verdana, lucida, arial, helvetica,
 * sans-serif">脚本表达式，使用在脚本引擎下面。</span></div></td>
 * </tr>
 * <tr>
 * <td style="padding: 0.25em 0.5em; margin: 0; text-align: center;
 * border-right-color: rgba(0, 0, 0, 1); border-left-color: rgba(0, 0, 0,
 * 1)"><span style="color: rgba(34, 0, 34, 1)"><span style="font-family:
 * verdana, lucida, arial, helvetica, sans-serif">()</span></span></td>
 * <td style="padding: 0.25em 0.5em; margin: 0; text-align: center;
 * border-right-color: rgba(0, 0, 0, 1); border-left-color: rgba(0, 0, 0,
 * 1)"><span style="color: rgba(34, 0, 34, 1)"><span style="font-family:
 * verdana, lucida, arial, helvetica, sans-serif">n/a</span></span></td>
 * <td style="padding: 0.25em 0.5em; margin: 0; border-right-color: rgba(0, 0,
 * 0, 1); border-left-color: rgba(0, 0, 0, 1)"><span style="color: rgba(34, 0,
 * 34, 1)"><span style="font-family: verdana, lucida, arial, helvetica,
 * sans-serif">Xpath分组</span></span></td>
 * </tr>
 * </tbody>
 * </table>
 *
 * <p>注意点如下：</p>
 * <ul>
 * <li>[]在xpath表达式总是从前面的路径来操作数组，索引是从1开始。</li>
 * <li>使用JOSNPath的[]操作符操作一个对象或者数组，索引是从0开始。</li>
 *
 * </ul>
 *
 * <p>语法示例如下</p>
 * <table style="border-spacing: 0; width: 834px; border: 1px solid rgba(0, 0,
 * 0, 1); font-size: 16px">
 * <caption>jsonpath语法示例</caption> <tbody>
 * <tr>
 * <td style="padding: 0.25em 0.5em; margin: 0; text-align: center;
 * border-right-color: rgba(0, 0, 0, 1); border-left-color: rgba(0, 0, 0, 1);
 * background-color: rgba(221, 221, 221, 1)"><span style="font-weight:
 * 700">XPath</span></td>
 * <td style="padding: 0.25em 0.5em; margin: 0; text-align: center;
 * border-right-color: rgba(0, 0, 0, 1); border-left-color: rgba(0, 0, 0, 1);
 * background-color: rgba(221, 221, 221, 1)"><span style="font-weight:
 * 700">JSONPath</span></td>
 * <td style="padding: 0.25em 0.5em; margin: 0; text-align: center;
 * border-right-color: rgba(0, 0, 0, 1); border-left-color: rgba(0, 0, 0, 1);
 * background-color: rgba(221, 221, 221, 1)"><span style="font-weight:
 * 700">结果</span></td>
 * </tr>
 * <tr>
 * <td style="padding: 0.25em 0.5em; margin: 0; border-right-color: rgba(0, 0,
 * 0, 1); border-left-color: rgba(0, 0, 0, 1)"><code style=
 * "font-family: &quot;Courier New&quot;, Courier, monospace; font-size: 14px; line-height: 22px">/store/book/author</code></td>
 * <td style="padding: 0.25em 0.5em; margin: 0; border-right-color: rgba(0, 0,
 * 0, 1); border-left-color: rgba(0, 0, 0, 1)"><code style=
 * "font-family: &quot;Courier New&quot;, Courier, monospace; font-size: 14px; line-height: 22px">$.store.book[*].author</code></td>
 * <td style="padding: 0.25em 0.5em; margin: 0; border-right-color: rgba(0, 0,
 * 0, 1); border-left-color: rgba(0, 0, 0, 1)"><div style="padding: 0; margin:
 * 0">所有书的作者</div></td>
 * </tr>
 * <tr>
 * <td style="padding: 0.25em 0.5em; margin: 0; border-right-color: rgba(0, 0,
 * 0, 1); border-left-color: rgba(0, 0, 0, 1); background-color: rgba(221, 221,
 * 221, 1)"><code style=
 * "font-family: &quot;Courier New&quot;, Courier, monospace; font-size: 14px; line-height: 22px">//author</code></td>
 * <td style="padding: 0.25em 0.5em; margin: 0; border-right-color: rgba(0, 0,
 * 0, 1); border-left-color: rgba(0, 0, 0, 1); background-color: rgba(221, 221,
 * 221, 1)"><code style=
 * "font-family: &quot;Courier New&quot;, Courier, monospace; font-size: 14px; line-height: 22px">$..author</code></td>
 * <td style="padding: 0.25em 0.5em; margin: 0; border-right-color: rgba(0, 0,
 * 0, 1); border-left-color: rgba(0, 0, 0, 1); background-color: rgba(221, 221,
 * 221, 1)"><div style="padding: 0; margin: 0">所有的作者</div></td>
 * </tr>
 * <tr>
 * <td style="padding: 0.25em 0.5em; margin: 0; border-right-color: rgba(0, 0,
 * 0, 1); border-left-color: rgba(0, 0, 0, 1)"><code style=
 * "font-family: &quot;Courier New&quot;, Courier, monospace; font-size: 14px; line-height: 22px">/store/*</code></td>
 * <td style="padding: 0.25em 0.5em; margin: 0; border-right-color: rgba(0, 0,
 * 0, 1); border-left-color: rgba(0, 0, 0, 1)"><code style=
 * "font-family: &quot;Courier New&quot;, Courier, monospace; font-size: 14px; line-height: 22px">$.store.*</code></td>
 * <td style="padding: 0.25em 0.5em; margin: 0; border-right-color: rgba(0, 0,
 * 0, 1); border-left-color: rgba(0, 0, 0, 1)"><div style="padding: 0; margin:
 * 0">store的所有元素。所有的bookst和bicycle</div></td>
 * </tr>
 * <tr>
 * <td style="padding: 0.25em 0.5em; margin: 0; border-right-color: rgba(0, 0,
 * 0, 1); border-left-color: rgba(0, 0, 0, 1); background-color: rgba(221, 221,
 * 221, 1)"><code style=
 * "font-family: &quot;Courier New&quot;, Courier, monospace; font-size: 14px; line-height: 22px">/store//price</code></td>
 * <td style="padding: 0.25em 0.5em; margin: 0; border-right-color: rgba(0, 0,
 * 0, 1); border-left-color: rgba(0, 0, 0, 1); background-color: rgba(221, 221,
 * 221, 1)"><code style=
 * "font-family: &quot;Courier New&quot;, Courier, monospace; font-size: 14px; line-height: 22px">$.store..price</code></td>
 * <td style="padding: 0.25em 0.5em; margin: 0; border-right-color: rgba(0, 0,
 * 0, 1); border-left-color: rgba(0, 0, 0, 1); background-color: rgba(221, 221,
 * 221, 1)"><div style="padding: 0; margin: 0">store里面所有东西的price</div></td>
 * </tr>
 * <tr>
 * <td style="padding: 0.25em 0.5em; margin: 0; border-right-color: rgba(0, 0,
 * 0, 1); border-left-color: rgba(0, 0, 0, 1)"><code style=
 * "font-family: &quot;Courier New&quot;, Courier, monospace; font-size: 14px; line-height: 22px">//book[3]</code></td>
 * <td style="padding: 0.25em 0.5em; margin: 0; border-right-color: rgba(0, 0,
 * 0, 1); border-left-color: rgba(0, 0, 0, 1)"><code style=
 * "font-family: &quot;Courier New&quot;, Courier, monospace; font-size: 14px; line-height: 22px">$..book[2]</code></td>
 * <td style="padding: 0.25em 0.5em; margin: 0; border-right-color: rgba(0, 0,
 * 0, 1); border-left-color: rgba(0, 0, 0, 1)"><div style="padding: 0; margin:
 * 0">第三个书</div></td>
 * </tr>
 * <tr>
 * <td style="padding: 0.25em 0.5em; margin: 0; border-right-color: rgba(0, 0,
 * 0, 1); border-left-color: rgba(0, 0, 0, 1); background-color: rgba(221, 221,
 * 221, 1)"><code style=
 * "font-family: &quot;Courier New&quot;, Courier, monospace; font-size: 14px; line-height: 22px">//book[last()]</code></td>
 * <td style="padding: 0.25em 0.5em; margin: 0; border-right-color: rgba(0, 0,
 * 0, 1); border-left-color: rgba(0, 0, 0, 1); background-color: rgba(221, 221,
 * 221, 1)"><code style=
 * "font-family: &quot;Courier New&quot;, Courier, monospace; font-size: 14px; line-height: 22px">$..book[(&#64;.length-1)]</code></td>
 * <td style="padding: 0.25em 0.5em; margin: 0; border-right-color: rgba(0, 0,
 * 0, 1); border-left-color: rgba(0, 0, 0, 1); background-color: rgba(221, 221,
 * 221, 1)">最后一本书</td>
 * </tr>
 * <tr>
 * <td style="padding: 0.25em 0.5em; margin: 0; border-right-color: rgba(0, 0,
 * 0, 1); border-left-color: rgba(0, 0, 0, 1)"><code style=
 * "font-family: &quot;Courier New&quot;, Courier, monospace; font-size: 14px; line-height: 22px">//book[position()&lt;3]</code></td>
 * <td style="padding: 0.25em 0.5em; margin: 0; border-right-color: rgba(0, 0,
 * 0, 1); border-left-color: rgba(0, 0, 0, 1)"><code style=
 * "font-family: &quot;Courier New&quot;, Courier, monospace; font-size: 14px; line-height: 22px">$..book[0,1]</code><div
 * style="padding: 0; margin: 0"><code style=
 * "font-family: &quot;Courier New&quot;, Courier, monospace; font-size: 14px; line-height: 22px">$..book[:2]</code></div></td>
 * <td style="padding: 0.25em 0.5em; margin: 0; border-right-color: rgba(0, 0,
 * 0, 1); border-left-color: rgba(0, 0, 0, 1)">前面的两本书。</td>
 * </tr>
 * <tr>
 * <td style="padding: 0.25em 0.5em; margin: 0; border-right-color: rgba(0, 0,
 * 0, 1); border-left-color: rgba(0, 0, 0, 1); background-color: rgba(221, 221,
 * 221, 1)"><code style=
 * "font-family: &quot;Courier New&quot;, Courier, monospace; font-size: 14px; line-height: 22px">//book[isbn]</code></td>
 * <td style="padding: 0.25em 0.5em; margin: 0; border-right-color: rgba(0, 0,
 * 0, 1); border-left-color: rgba(0, 0, 0, 1); background-color: rgba(221, 221,
 * 221, 1)"><code style=
 * "font-family: &quot;Courier New&quot;, Courier, monospace; font-size: 14px; line-height: 22px">$..book[?(&#64;.isbn)]</code></td>
 * <td style="padding: 0.25em 0.5em; margin: 0; border-right-color: rgba(0, 0,
 * 0, 1); border-left-color: rgba(0, 0, 0, 1); background-color: rgba(221, 221,
 * 221, 1)">&nbsp;过滤出所有的包含isbn的书。</td>
 * </tr>
 * <tr>
 * <td style="padding: 0.25em 0.5em; margin: 0; border-right-color: rgba(0, 0,
 * 0, 1); border-left-color: rgba(0, 0, 0, 1)"><code style=
 * "font-family: &quot;Courier New&quot;, Courier, monospace; font-size: 14px; line-height: 22px">//book[price&lt;10]</code></td>
 * <td style="padding: 0.25em 0.5em; margin: 0; border-right-color: rgba(0, 0,
 * 0, 1); border-left-color: rgba(0, 0, 0, 1)"><code style=
 * "font-family: &quot;Courier New&quot;, Courier, monospace; font-size: 14px; line-height: 22px">$..book[?(&#64;.price&lt;10)]</code></td>
 * <td style="padding: 0.25em 0.5em; margin: 0; border-right-color: rgba(0, 0,
 * 0, 1); border-left-color: rgba(0, 0, 0, 1)">过滤出价格低于10的书。</td>
 * </tr>
 * <tr>
 * <td style="padding: 0.25em 0.5em; margin: 0; border-right-color: rgba(0, 0,
 * 0, 1); border-left-color: rgba(0, 0, 0, 1); background-color: rgba(221, 221,
 * 221, 1)"><code style=
 * "font-family: &quot;Courier New&quot;, Courier, monospace; font-size: 14px; line-height: 22px">//*</code></td>
 * <td style="padding: 0.25em 0.5em; margin: 0; border-right-color: rgba(0, 0,
 * 0, 1); border-left-color: rgba(0, 0, 0, 1); background-color: rgba(221, 221,
 * 221, 1)"><code style=
 * "font-family: &quot;Courier New&quot;, Courier, monospace; font-size: 14px; line-height: 22px">$..*</code></td>
 * <td style="padding: 0.25em 0.5em; margin: 0; border-right-color: rgba(0, 0,
 * 0, 1); border-left-color: rgba(0, 0, 0, 1); background-color: rgba(221, 221,
 * 221, 1)"><div style="padding: 0; margin: 0"><span style="color: rgba(34, 0,
 * 34, 1)">所有元素。</span></div></td>
 * </tr>
 * </tbody>
 * </table>
 *
 *
 * @author qingteng
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
public final class JsonUtil {

    /**
     * 将json格式的字符串转为JAVA对象
     *
     * @param <T>   JAVA对象类型
     * @param json  json格式的字符串
     * @param clazz JAVA对象
     * @return 转换后的JAVA对象
     */
    public static <T> T str2Bean(String json, Class<T> clazz) {
        if (StringUtils.isBlank(json)) {
            return null;
        }
        try {
            return JSONObject.parseObject(json.trim(), clazz);
        } catch (Exception e) {
            if (log.isInfoEnabled()) {
                log.info("将字符串 {} 转换成 java对象 {} 时出现问题 {}", json, clazz, e.getMessage());
            }
        }
        return null;
    }

    /**
     * 将json格式的字符串转换为对象集合
     *
     * @param <T>   对象类型
     * @param json  json格式的字符串
     * @param clazz JAVA对象
     * @return 转换后对象集合
     */
    public static <T> List<T> str2List(String json, Class<T> clazz) {
        List<T> t = null;
        try {
            t = JSONObject.parseArray(json.trim(), clazz);
        } catch (Exception e) {
            t = Collections.emptyList();
            if (log.isInfoEnabled()) {
                log.info("将字符串 {} 转换成 {} 集合 时出现问题 {}", json, clazz, e.getMessage());
            }
        }
        return t;
    }

    /**
     * <p>
     * 根据json提取表达式从字符串里提取出内容
     * </p>
     *
     * @param <T>      java对象类型
     * @param json     json格式的字符串
     * @param jsonPath 提取表达式
     * @return 提取出来的字符串
     */
    public static <T> T extract(String json, String jsonPath) {
        if (StringUtils.isBlank(json)) {
            return null;
        }
        try {
            return JsonPath.read(json.trim(), jsonPath);
        } catch (Exception e) {
            if (log.isInfoEnabled()) {
                log.info("根据表达式 {} 从字符串 {} 提取数据 时出现问题 {}", jsonPath, json, e.getMessage());
            }
        }
        return null;
    }

    /**
     * 根据json提取表达式从字符串里提取出内容,并将提取的内容转换为JAVA对象
     *
     *
     * @param <T>      java对象类型
     * @param json     json格式的字符串
     * @param jsonPath json表达式
     * @param clazz    待转换的java对象
     * @return 转换后的java对象
     */
    public static <T> T extract(String json, String jsonPath, Class<T> clazz) {
        Object data = extract(json, jsonPath);
        if (null == data) {
            return null;
        }
        return str2Bean(data.toString(), clazz);
    }

    /**
     * 根据json提取表达式从字符串里提取出内容,并将提取的内容转换为JSONObject对象
     *
     *
     * @param json     json格式的字符串
     * @param jsonPath json表达式
     * @return 转换后的java对象
     */
    public static JSONObject extractJSON(String json, String jsonPath) {
        Object data = extract(json, jsonPath);
        if (null == data) {
            return null;
        }
        try {
            return JSONObject.parseObject(data.toString().trim());
        } catch (Exception e) {
            if (log.isInfoEnabled()) {
                log.info("根据表达式 {} 从字符串 {} 提取JSONObject数据 时出现问题 {}", jsonPath, json, e.getMessage());
            }
        }
        return new JSONObject();
    }

    /**
     * <p>
     * 根据json提取表达式从字符串里提取出内容,并将提取的内容转换为JAVA对象集合
     * </p>
     *
     * @param <T>      java对象类型
     * @param json     json格式的字符串
     * @param jsonPath json表达式
     * @param clazz    待转换的java对象
     * @return 转换后的java对象集合
     */
    public static <T> List<T> extractList(String json, String jsonPath, Class<T> clazz) {
        Object data = extract(json, jsonPath);
        if (null == data) {
            return null;
        }
        return str2List(data.toString(), clazz);
    }

    /**
     * 将java对象转换成json对象
     *
     * @param data 待转换的数据
     * @return 转换后的json对象，若转换失败则返回null
     */
    public static JSONObject toJSON(Object data) {
        if (null == data) {
            return null;
        }
        try {
            return JSONObject.parseObject(JSONObject.toJSONString(data));
        } catch (Exception e) {
            if (log.isInfoEnabled()) {
                log.info("将对象 {} 转换成json对象时出现问题 {} ", data, e.getMessage());
            }
        }
        return null;
    }

    /**
     * 将json格式的字符串转换为json对象
     *
     * @param jsonStr 待转换的数据
     * @return 转换后的json对象，若转换失败则返回null
     */
    public static JSONObject str2JSONObject(String jsonStr) {
        if (StringUtils.isBlank(jsonStr)) {
            return null;
        }
        try {
            return JSONObject.parseObject(jsonStr.trim());
        } catch (Exception e) {
            if (log.isInfoEnabled()) {
                log.info("将字符串 {} 转换成json对象时出现问题 {} ", jsonStr, e.getMessage());
            }
        }
        return null;
    }

    /**
     * 将json格式的字符串转换为json数组
     *
     * @param jsonStr 待转换的数据
     * @return 转换后的json数组，若转换失败则返回null
     */
    public static JSONArray str2JSONArray(String jsonStr) {
        if (StringUtils.isBlank(jsonStr)) {
            return null;
        }
        try {
            return JSONArray.parseArray(jsonStr.trim());
        } catch (Exception e) {
            if (log.isInfoEnabled()) {
                log.info("将字符串 {} 转换成json数组时出现问题 {} ", jsonStr, e.getMessage());
            }
        }
        return null;
    }

    /**
     * 将json格式的字符串转换为map对象
     *
     * @param text 待转换的数据
     * @return 转换后的map对象，若转换失败则返回null
     */
    public static Map<String, Object> json2Map(String text) {
        if (StringUtils.isBlank(text)) {
            return null;
        }
        try {
            return JSONObject.parseObject(text.trim()).getInnerMap();
        } catch (Exception e) {
            if (log.isInfoEnabled()) {
                log.info("将字符串 {} 转换成 map 时出现问题 {} ", text, e.getMessage());
            }
        }
        return null;
    }

    /**
     * 判断字符串是否为json对象格式
     * @param text 字符串
     * @return 字符串是否为json对象格式返回为true, 否则为false
     */
    public synchronized static boolean isJSONObject(String text) {
        if (StringUtils.isBlank(text)) {
            return false;
        }
        try {
            JSONObject.parseObject(text.trim());
        } catch (Throwable e) {
            return false;
        }
        return true;
    }

    /**
     * 判断字符串是否为json数组格式
     * @param text 字符串
     * @return 字符串是否为json数组格式返回为true, 否则为false
     */
    public synchronized static boolean isJSONArray(String text) {
        if (StringUtils.isBlank(text)) {
            return false;
        }
        try {
            JSONArray.parseArray(text.trim());
        } catch (Throwable e) {
            return false;
        }
        return true;
    }
}
