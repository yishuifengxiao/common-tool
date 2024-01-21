/**
 *
 */
package com.yishuifengxiao.common.tool.collections;


import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * <p>json转换提取工具</p>
 * <p style="color:red">注意这里内部使用的jackson,故进行数据转换时需要按照jackson的规范进行标记</p>
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
 * "font-family: &quot;Courier New&quot;, Courier, monospace; font-size: 14px; line-height:
 * 22px">/store/book/author</code></td>
 * <td style="padding: 0.25em 0.5em; margin: 0; border-right-color: rgba(0, 0,
 * 0, 1); border-left-color: rgba(0, 0, 0, 1)"><code style=
 * "font-family: &quot;Courier New&quot;, Courier, monospace; font-size: 14px; line-height: 22px">$.store.book[*]
 * .author</code></td>
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
 * "font-family: &quot;Courier New&quot;, Courier, monospace; font-size: 14px; line-height:
 * 22px">/store//price</code></td>
 * <td style="padding: 0.25em 0.5em; margin: 0; border-right-color: rgba(0, 0,
 * 0, 1); border-left-color: rgba(0, 0, 0, 1); background-color: rgba(221, 221,
 * 221, 1)"><code style=
 * "font-family: &quot;Courier New&quot;, Courier, monospace; font-size: 14px; line-height: 22px">$.store.
 * .price</code></td>
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
 * "font-family: &quot;Courier New&quot;, Courier, monospace; font-size: 14px; line-height: 22px">//book[last()
 * ]</code></td>
 * <td style="padding: 0.25em 0.5em; margin: 0; border-right-color: rgba(0, 0,
 * 0, 1); border-left-color: rgba(0, 0, 0, 1); background-color: rgba(221, 221,
 * 221, 1)"><code style=
 * "font-family: &quot;Courier New&quot;, Courier, monospace; font-size: 14px; line-height: 22px">$..book[(&#64;
 * .length-1)]</code></td>
 * <td style="padding: 0.25em 0.5em; margin: 0; border-right-color: rgba(0, 0,
 * 0, 1); border-left-color: rgba(0, 0, 0, 1); background-color: rgba(221, 221,
 * 221, 1)">最后一本书</td>
 * </tr>
 * <tr>
 * <td style="padding: 0.25em 0.5em; margin: 0; border-right-color: rgba(0, 0,
 * 0, 1); border-left-color: rgba(0, 0, 0, 1)"><code style=
 * "font-family: &quot;Courier New&quot;, Courier, monospace; font-size: 14px; line-height: 22px">//book[position()
 * &lt;3]</code></td>
 * <td style="padding: 0.25em 0.5em; margin: 0; border-right-color: rgba(0, 0,
 * 0, 1); border-left-color: rgba(0, 0, 0, 1)"><code style=
 * "font-family: &quot;Courier New&quot;, Courier, monospace; font-size: 14px; line-height: 22px">$..book[0,
 * 1]</code><div
 * style="padding: 0; margin: 0"><code style=
 * "font-family: &quot;Courier New&quot;, Courier, monospace; font-size: 14px; line-height: 22px">$.
 * .book[:2]</code></div></td>
 * <td style="padding: 0.25em 0.5em; margin: 0; border-right-color: rgba(0, 0,
 * 0, 1); border-left-color: rgba(0, 0, 0, 1)">前面的两本书。</td>
 * </tr>
 * <tr>
 * <td style="padding: 0.25em 0.5em; margin: 0; border-right-color: rgba(0, 0,
 * 0, 1); border-left-color: rgba(0, 0, 0, 1); background-color: rgba(221, 221,
 * 221, 1)"><code style=
 * "font-family: &quot;Courier New&quot;, Courier, monospace; font-size: 14px; line-height:
 * 22px">//book[isbn]</code></td>
 * <td style="padding: 0.25em 0.5em; margin: 0; border-right-color: rgba(0, 0,
 * 0, 1); border-left-color: rgba(0, 0, 0, 1); background-color: rgba(221, 221,
 * 221, 1)"><code style=
 * "font-family: &quot;Courier New&quot;, Courier, monospace; font-size: 14px; line-height: 22px">$..book[?(&#64;
 * .isbn)]</code></td>
 * <td style="padding: 0.25em 0.5em; margin: 0; border-right-color: rgba(0, 0,
 * 0, 1); border-left-color: rgba(0, 0, 0, 1); background-color: rgba(221, 221,
 * 221, 1)">&nbsp;过滤出所有的包含isbn的书。</td>
 * </tr>
 * <tr>
 * <td style="padding: 0.25em 0.5em; margin: 0; border-right-color: rgba(0, 0,
 * 0, 1); border-left-color: rgba(0, 0, 0, 1)"><code style=
 * "font-family: &quot;Courier New&quot;, Courier, monospace; font-size: 14px; line-height: 22px">//book[price&lt;
 * 10]</code></td>
 * <td style="padding: 0.25em 0.5em; margin: 0; border-right-color: rgba(0, 0,
 * 0, 1); border-left-color: rgba(0, 0, 0, 1)"><code style=
 * "font-family: &quot;Courier New&quot;, Courier, monospace; font-size: 14px; line-height: 22px">$..book[?(&#64;
 * .price&lt;10)]</code></td>
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
 * @author qingteng
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
public final class JsonUtil {

    public  static ObjectMapper MAPPER = MAPPER = new ObjectMapper();

    static {

        MAPPER.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        // 反序列化时候遇到不匹配的属性并不抛出异常
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        //确定如果遇到对象Id引用，而该引用没有引用具有该Id的实际对象（“未解析对象Id”）不进行进一步处理
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNRESOLVED_OBJECT_IDS, false);
        //当用com.fasterxml.jackson.annotation.JsonTypeInfo.As.EXTERNAL_property注释的属性丢失，但相关类型id可用时会发生什么的功能则只有当属性标记为“必需”时才会引发异常。
        MAPPER.configure(DeserializationFeature.FAIL_ON_MISSING_EXTERNAL_TYPE_ID_PROPERTY, false);
        // 序列化时候遇到空对象不抛出异常
        MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        // 反序列化的时候如果是无效子类型,不抛出异常
        MAPPER.configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false);
        // 不使用默认的dateTime进行序列化,
        MAPPER.configure(SerializationFeature.WRITE_DATE_KEYS_AS_TIMESTAMPS, false);
        // 使用JSR310提供的序列化类,里面包含了大量的JDK8时间序列化类
        MAPPER.registerModule(new JavaTimeModule());
        //具有不可解析子类型的字段将被反序列化为null,解决报错 com.fasterxml.jackson.databind.exc.InvalidTypeIdException
        MAPPER.disable(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE);
    }

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
            return MAPPER.readValue(json.trim(), clazz);
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
            t = MAPPER.readValue(json.trim(), new TypeReference<List<T>>() {
            });
        } catch (Exception e) {
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
     * <pre>
     *
     * {
     *     "store": {
     *         "book": [
     *             {
     *                 "category": "reference",
     *                 "author": "NigelRees",
     *                 "title": "SayingsoftheCentury",
     *                 "price": 8.95
     *             },
     *             {
     *                 "category": "fiction",
     *                 "author": "EvelynWaugh",
     *                 "title": "SwordofHonour",
     *                 "price": 12.99
     *             }
     *         ],
     *         "bicycle": {
     *             "color": "red",
     *             "price": 19.95
     *         }
     *     }
     * }
     *
     * </pre>
     * A JsonPath can be compiled and used as shown:
     * <pre>
     * JsonPath path = JsonPath.compile("$.store.book[1]");
     * List&lt;Objectt&gt; books = path.read(json);
     * Or:
     * </pre>
     * <pre>
     * List&lt;Objectt&gt; authors = JsonPath.read(json, "$.store.book[*].author")
     * </pre>
     * <pre>
     * If the json path returns a single value (is definite): </pre>
     * <pre>String author = JsonPath.read(json, "$.store.book[1].author")
     * </pre>
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
            return MAPPER.readValue(text.trim(), new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception e) {
            if (log.isInfoEnabled()) {
                log.info("将字符串 {} 转换成 map 时出现问题 {} ", text, e.getMessage());
            }
        }
        return null;
    }

    /**
     * 判断字符串是否为json对象格式
     *
     * @param text 字符串
     * @return 字符串为json对象格式返回为true, 否则为false
     */
    public static boolean isJSONObject(String text) {
        if (StringUtils.isBlank(text)) {
            return false;
        }
        try {
            JsonNode tree = MAPPER.readTree(text.trim());
            return tree.isObject();
        } catch (Throwable e) {
            return false;
        }
    }

    /**
     * 判断字符串是否为json数组格式
     *
     * @param text 字符串
     * @return 字符串为json数组格式返回为true, 否则为false
     */
    public static boolean isJSONArray(String text) {
        if (StringUtils.isBlank(text)) {
            return false;
        }
        try {
            JsonNode tree = MAPPER.readTree(text.trim());
            return tree.isArray();
        } catch (Throwable e) {
            return false;
        }

    }

    /**
     * 判断字符串是否为json格式
     *
     * @param text 字符串
     * @return 字符串为json格式返回为true, 否则为false
     */
    public static boolean isJSON(String text) {
        if (StringUtils.isBlank(text)) {
            return false;
        }
        try {
            MAPPER.readTree(text.trim());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 将对象转换为json格式的字符串
     *
     * @param value 待转换的数据
     * @return
     */
    public static String toJSONString(Object value) {
        try {
            return MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            if (log.isInfoEnabled()) {
                log.info("将数据 {} 转换成 json格式的字符串 时出现问题 {} ", value, e);
            }
        }
        return null;
    }


}
