/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.jingyue.DocConversion;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.jingyue.DocConversion.common.YuntuDoc;
import com.jingyue.DocConversion.common.YuntuException;
import com.jingyue.DocConversion.internal.HttpUtils;
import com.jingyue.DocConversion.internal.UploadUtil;

/**
 * 调用九云图文档转换服务的入口类。
 * <p>
 * 九云图是一个文档转换的云计算服务，帮助用户把各类文档转成 HTML5(SVG)、长图片、PDF
 * 等格式。在移动端缺少字库的情况下，也能保持字体和版式完全不变，支持高清平滑缩放。转换生成的 HTML 不含任何脚本和外链，下载后可独立使用，最大支持
 * 500M 文档。
 * </p>
 * <p>
 * 以下是样例代码：
 * 
 * <pre>
 * Converter converter = new com.jingyue.DocConversion.Converter();
 * 
 * converter.setConfig(new YuntuConfig(&quot;html&quot;));
 * 
 * YuntuDoc doc = converter.convert(DEMO_DOC_URL);
 * 
 * if (doc.isSuccess()) {
 * 	String pdfURL = converter.getPDF(doc);
 * 	String htmlURL = converter.getWholeHTML(doc);
 * 	String webViewUrl = converter.getWebviewURL(doc);
 * 
 * 	// 输出生成的 PDF 和 HTML 链接。
 * 	System.out.println(&quot;转换生成的 PDF: &quot; + pdfURL);
 * 	System.out.println(&quot;转换生成的 HTML: &quot; + htmlURL);
 * 
 * 	// 打开浏览器，以网页形式展现转换后的文档。
 * 	Desktop.getDesktop().browse(new URI(webViewUrl));
 * 	System.out.println(&quot;九云图转换完成。&quot;);
 * } else {
 * 	System.out.println(&quot;转换失败: &quot; + doc.getMessage());
 * }
 * </pre>
 * 
 * </p>
 * 
 * @version 1.0
 */
public class Converter {

	public final static int PLATFORM_YUNTU = -1;

	public final static int PLATFORM_ALI = 0;

	public final static int PLATFORM_BAIDU = 1;

	public final static int PLATFORM_HUAWEI = 2;

	/** 九云图服务器网址。 */
	protected String host = "http://server.9yuntu.cn";

	/** 九云图授权码。该授权码需要在构造这个类的实例时指定。如果缺少授权码，转换的结果带有 “九云图 DEMO” 水印。 */
	private String appCode = null;

	private String appKey = null;

	private String appSecret = null;

	/** 文档转换的参数配置。 */
	private YuntuConfig config = YuntuConfig.DEFAULT;

	private int platform = PLATFORM_YUNTU;

	/**
	 * 构建九云图文档转换类。该方法未提供授权码，转换的结果会带有 “九云图 DEMO” 水印。
	 */
	public Converter() {
		this(null, PLATFORM_YUNTU);
	}

	/**
	 * 构建九云图文档转换类，需提供授权码。
	 * 
	 * @param appCode 授权码。如果该授权码无效，转换的结果会被添加 “九云图 DEMO” 水印。
	 */
	public Converter(String appCode) {
		this(appCode, PLATFORM_ALI);
	}

	/**
	 * 构建九云图文档转换类，需提供授权码。
	 * 
	 * @param appCode  授权码。如果该授权码无效，转换的结果会被添加 “九云图 DEMO” 水印。
	 * @param platform 云平台。0-ALI, 1-BAIDU, 2-HUAWEI.
	 */
	public Converter(String appCode, int platform) {
		this.appCode = appCode;
		this.host = this.getHost(platform);
	}

	/**
	 * 构建九云图文档转换类，需提供授权码。
	 * 
	 * @param appKey    授权码。如果该授权码无效，转换的结果会被添加 “九云图 DEMO” 水印。
	 * @param appSecret 授权码。如果该授权码无效，转换的结果会被添加 “九云图 DEMO” 水印。
	 */
	public Converter(String appKey, String appSecret) {
		this.appKey = appKey;
		this.appSecret = appSecret;
		this.host = this.getHost(platform);
	}

	private String getHost(int platform) {
		String host;

		this.platform = platform;
		switch (platform) {
		case PLATFORM_ALI:
			host = "http://api.9yuntu.cn";
			break;

		case PLATFORM_BAIDU:
			host = "http://api-baidu.9yuntu.cn";
			break;

		case PLATFORM_HUAWEI:
			host = "http://yuntu.apistore.huaweicloud.com";
			break;

		case PLATFORM_YUNTU:
		default:
			host = "http://server.9yuntu.cn";
			break;
		}
		return host;
	}

	/**
	 * 设置文档转换的参数配置。
	 * 
	 * @param config 文档转换的参数配置。
	 */
	public void setConfig(YuntuConfig config) {
		this.config = config;
	}

	/**
	 * 获取文档转换的参数配置。
	 * 
	 * @return 文档转换的参数配置。
	 */
	public YuntuConfig getConfig() {
		return this.config;
	}

	/**
	 * 转换指定的文档。
	 * 
	 * @param docUrl 被转换的文档 URL。
	 * @return 返回一个 <code>YuntuDoc</code> 实例, 其中包含了文档转换状态等信息。
	 * @throws YuntuException 文档转换异常。
	 */
	public YuntuDoc convert(String docUrl) throws YuntuException {
		if (docUrl != null && docUrl.toLowerCase().startsWith("http")) {
			String path = "/execute/Convert";
			Map<String, String> querys = getQueries();
			Map<String, String> headers = getHeaders();

			querys.put("docURL", docUrl);

			String body = HttpUtils.get(host, path, headers, querys);

			return getYuntuDoc(body);
		} else {
			return convert(new File(docUrl));
		}
	}

	/**
	 * 转换指定的文档。
	 * 
	 * @param file 被转换的文档。
	 * @return 返回一个 <code>YuntuDoc</code> 实例, 其中包含了文档转换状态等信息。
	 * @throws YuntuException 文档转换异常。
	 */
	public YuntuDoc convert(File file) throws YuntuException {
		try {
			return new UploadUtil(this.getConfig()).uploadToOSS(file);
		} catch (MalformedURLException e) {
			throw new YuntuException(e);
		} catch (IOException e) {
			throw new YuntuException(e);
		}
	}

	/**
	 * 转换指定的文档。
	 * 
	 * @param inputStream 被转换的文档数据流。
	 * @param fileName    被转换的文档的fileName（必须包含扩展名）。
	 * @return 返回一个 <code>YuntuDoc</code> 实例, 其中包含了文档转换状态等信息。
	 * @throws YuntuException 文档转换异常。
	 */
	public YuntuDoc convert(InputStream inputStream, String fileName) throws YuntuException {
		try {
			return new UploadUtil(this.getConfig()).uploadToOSS(inputStream, fileName);
		} catch (MalformedURLException e) {
			throw new YuntuException(e);
		} catch (IOException e) {
			throw new YuntuException(e);
		}
	}

	protected Map<String, String> getQueries() {
		Map<String, String> querys = new HashMap<String, String>();

		querys.put("outputType", config.getOutputType());
		querys.put("watermark", config.getWatermark());
		if (this.appCode != null) {
			if (this.appCode.startsWith("jyt")) {
				querys.put("yuntuKey", this.appCode);
			}
		}
		if (this.appKey != null && this.appSecret != null) {
			querys.put("key", this.appKey);
			querys.put("secret", this.appSecret);
		}
		return querys;
	}

	protected Map<String, String> getHeaders() {
		Map<String, String> headers = new HashMap<String, String>();

		if (this.appCode != null) {
			if (this.platform == PLATFORM_ALI) {
				headers.put("Authorization", "APPCODE " + this.appCode);
			} else if (this.platform == PLATFORM_BAIDU) {
				headers.put("X-Bce-Signature", "AppCode/" + this.appCode);
			}
		}
		headers.put("Content-Type", "application/json");
		return headers;
	}

	/**
	 * 根据 JSON 字符串，返回一个 <code>YuntuDoc</code> 实例。
	 * 
	 * @param json 给定的 JSON 字符串。
	 * @return 返回一个 <code>YuntuDoc</code> 实例。
	 */
	protected YuntuDoc getYuntuDoc(String json) {
		YuntuDoc doc = new YuntuDoc();

		try {
			JSONObject jsonObj = new JSONObject(json);
			int code = jsonObj.getInt("retCode");
			String statusPage = jsonObj.getString("docStatusPage");

			doc.setCode(code);
			doc.setStatusPage(statusPage);
			if (code == 0) {
				String docID = jsonObj.getString("docID");

				doc.setID(docID);
			} else if (code == 1) {
				String docID = jsonObj.getString("docID");

				if (docID != null) {
					doc = queryStatus(docID, 3000);
				} else {
					doc.setCode(2);
					doc.setMessage("转换失败！");
				}
			} else if (code == 2) {
				String message = jsonObj.getString("retMsg");

				doc.setMessage(message);
			}
		} catch (JSONException e) {
			doc.setCode(2);
			doc.setMessage("转换失败！");
		}
		return doc;
	}

	/**
	 * 查询文档转换状态。
	 * 
	 * @param docID      文档 ID。
	 * @param retryTimes 自动轮询的次数。
	 * @return 返回一个 <code>YuntuDoc</code> 实例, 其中包含了文档转换状态等信息。
	 */
	private YuntuDoc queryStatus(String docID, int retryTimes) {
		YuntuDoc doc = new YuntuDoc(docID);
		String path = "/execute/QueryStatus";
		Map<String, String> querys = getQueries();
		Map<String, String> headers = getHeaders();

		querys.put("docID", docID);

		try {
			String body = HttpUtils.get(host, path, headers, querys);

			if (body != null) {
				JSONObject json = new JSONObject(body);
				int code = json.getInt("retCode");
				String statusPage = json.getString("docStatusPage");

				doc.setCode(code);
				doc.setStatusPage(statusPage);
				if (code == 1 || code == 2) {
					String message = json.getString("retMsg");

					doc.setMessage(message);
				}
			}
		} catch (Throwable e) {
			doc.setCode(1);
		}
		if (doc.getCode() == 1) {
			if (retryTimes > 0) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					return doc;
				}
				return queryStatus(docID, --retryTimes);
			} else {
				doc.setCode(2);
				doc.setMessage("转换超时！");
			}
		}
		return doc;
	}

	/**
	 * 根据指定的输出格式，获取文档转换结果。
	 * 
	 * @param doc        指定的 <code>YuntuDoc</code> 实例。
	 * @param outputType 输出格式，包括："webview", "html", "htmls", "pdf", "longimage",
	 *                   "images", "svgs"。
	 *                   <ul>
	 *                   <li>"webview" - 转换结果保存在九云图服务器，可通过链接在浏览器中展现，不支持下载；</li>
	 *                   <li>"html" - 生成一个包括文档所有页面内容的完整 HTML，不含任何脚本和外链，图片均以 svg 或
	 *                   base64 形式内嵌，支持下载；</li>
	 *                   <li>"htmls" - 文档的每个页面转换成一个独立的 HTML，不含任何脚本和外链，图片均以 svg 或
	 *                   base64 形式内嵌)，支持下载；</li>
	 *                   <li>"pdf" - 生成 PDF，支持下载；</li>
	 *                   <li>"longimage" - 生成长图片，支持下载；</li>
	 *                   <li>"images" - 文档的每个页面转换成一个单独的图片，支持下载；</li>
	 *                   <li>"svgs" - 文档的每个页面转换成一个单独的 SVG 文件，支持下载。</li>
	 *                   </ul>
	 * @return 返回文档转换结果。
	 * @throws YuntuException 获取文档转换结果时，出现错误。
	 */
	private List<String> getOutputResult(YuntuDoc doc, String outputType) throws YuntuException {

		if (doc == null || doc.getID() == null || !doc.isSuccess()) {
			throw new YuntuException("doc 状态错误！");
		}

		String path = "/execute/GetOutputResult";
		List<String> outputURLs = new ArrayList<String>();
		Map<String, String> querys = getQueries();
		Map<String, String> headers = getHeaders();

		querys.put("docID", doc.getID());
		querys.put("outputType", outputType);

		String body = null;

		try {
			body = HttpUtils.get(host, path, headers, querys);
			if (body != null) {
				JSONObject json = new JSONObject(body);
				JSONArray urlArray = json.getJSONArray("outputURLs");

				for (int i = 0; i < urlArray.length(); i++) {
					outputURLs.add(urlArray.getString(i));
				}
			}
		} catch (JSONException e) {
			throw new YuntuException("\n请检查 YuntuConfig 是否设置了正确的输出类型。\nbody: [" + body + "], " + e.getMessage() + "");
		}
		return outputURLs;
	}

	/**
	 * 获取文档转换状态页，该页面包含文档信息和转换状态。
	 * 
	 * @param doc 指定的 <code>YuntuDoc</code> 实例。
	 * @return 返回一个用于显示文档转换状态的 URL。
	 */
	public String getStatusPage(YuntuDoc doc) {
		return queryStatus(doc.getID(), 0).getStatusPage();
	}

	/**
	 * 获取用于展现文档的 URL，打开此 URL 需访问九云图服务器。
	 * 
	 * @param doc 指定的 <code>YuntuDoc</code> 实例。
	 * @return 返回一个用于展现文档的 URL。
	 */
	public String getWebviewURL(YuntuDoc doc) {
		String url = null;

		if (doc != null) {
			if (doc.isSuccess()) {
				url = "https://server.9yuntu.cn/doc/" + doc.getID();
			} else {
				url = doc.getStatusPage();
			}
		}
		return url;
	}

	/**
	 * 获取一个包含文档完整内容的 HTML，其中无任何外链和脚本，可下载后脱离九云图独立使用。
	 * 
	 * @param doc 指定的 <code>YuntuDoc</code> 实例。
	 * @return 返回一个 URL，指向完整的 HTML，其中包含文档的所有页面内容。
	 * @throws YuntuException 获取文档转换结果时，出现错误。
	 */
	public String getWholeHTML(YuntuDoc doc) throws YuntuException {
		List<String> outputResult = this.getOutputResult(doc, "html");

		if (outputResult != null && !outputResult.isEmpty()) {
			return outputResult.get(0);
		}
		return null;
	}

	/**
	 * 获取文档分页 HTMLs。每页生成一个独立的 HTML，其中无任何外链和脚本，可下载后脱离九云图独立使用。
	 * 
	 * @param doc 指定的 <code>YuntuDoc</code> 实例。
	 * @return 返回包含一组 URL 的 <code>List</code> 实例，其中每个 URL 指向一个文档页面的 HTML。
	 * @throws YuntuException 获取文档转换结果时，出现错误。
	 */
	public List<String> getPagingHTMLs(YuntuDoc doc) throws YuntuException {
		return this.getOutputResult(doc, "htmls");
	}

	/**
	 * 获取文档转换生成的 PDF。
	 * 
	 * @param doc 指定的 <code>YuntuDoc</code> 实例。
	 * @return 返回一个 URL，指向转换生成的 PDF。
	 * @throws YuntuException 获取文档转换结果时，出现错误。
	 */
	public String getPDF(YuntuDoc doc) throws YuntuException {
		List<String> outputResult = this.getOutputResult(doc, "pdf");

		if (outputResult != null && !outputResult.isEmpty()) {
			return outputResult.get(0);
		}
		return null;
	}

	/**
	 * 获取文档转换生成的长图片。
	 * 
	 * @param doc 指定的 <code>YuntuDoc</code> 实例。
	 * @return 返回一个 URL，指向转换生成的长图片。
	 * @throws YuntuException 获取文档转换结果时，出现错误。
	 */
	public String getLongImage(YuntuDoc doc) throws YuntuException {
		List<String> outputResult = this.getOutputResult(doc, "longimage");

		if (outputResult != null && !outputResult.isEmpty()) {
			return outputResult.get(0);
		}
		return null;
	}

	/**
	 * 获取文档分页图片。每页生成一个单独的图片。
	 * 
	 * @param doc 指定的 <code>YuntuDoc</code> 实例。
	 * @return 返回包含一组 URL 的 <code>List</code> 实例，其中每个 URL 指向转换生成的 一页图片。
	 * @throws YuntuException 获取文档转换结果时，出现错误。
	 */
	public List<String> getPagingImages(YuntuDoc doc) throws YuntuException {
		return this.getOutputResult(doc, "images");
	}

	/**
	 * 获取文档分页 SVG。每页生成一个单独的 SVG 文件。
	 * 
	 * @param doc 指定的 <code>YuntuDoc</code> 实例。
	 * @return 返回包含一组 URL 的 <code>List</code> 实例，其中每个 URL 指向转换生成的 一页 SVG。
	 * @throws YuntuException 获取文档转换结果时，出现错误。
	 */
	public List<String> getPagingSVGs(YuntuDoc doc) throws YuntuException {
		return this.getOutputResult(doc, "svgs");
	}
}
