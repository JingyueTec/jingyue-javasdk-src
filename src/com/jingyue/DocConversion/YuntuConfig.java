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

/**
 * 文档转换的参数配置。
 */
public class YuntuConfig {

	/** 默认设置。 */
	public static final YuntuConfig DEFAULT = new YuntuConfig();

	/**
	 * 文档转换生成的格式，包括："webview", "html", "htmls", "pdf", "longimage", "images",
	 * "svgs"。 默认是 "webview"。
	 * <ul>
	 * <li>"webview" - 转换结果保存在九云图服务器，可通过链接在浏览器中展现，不支持下载；</li>
	 * <li>"html" - 生成一个包括文档所有页面内容的完整 HTML，不含任何脚本和外链，图片均以 svg 或 base64
	 * 形式内嵌，支持下载；</li>
	 * <li>"htmls" - 文档的每个页面转换成一个独立的 HTML，不含任何脚本和外链，图片均以 svg 或 base64
	 * 形式内嵌)，支持下载；</li>
	 * <li>"pdf" - 生成 PDF，支持下载；</li>
	 * <li>"longimage" - 生成长图片，支持下载；</li>
	 * <li>"images" - 文档的每个页面转换成一个单独的图片，支持下载；</li>
	 * <li>"svgs" - 文档的每个页面转换成一个单独的 SVG 文件，支持下载。</li>
	 * </ul>
	 */
	private String outputType = null;

	/** 文字水印。 */
	private String watermark = null;

	/**
	 * 构建文档转换配置类。
	 */
	public YuntuConfig() {
		this(null);
	}

	/**
	 * 构建文档转换配置类，并指定输出格式。
	 * 
	 * @param outputType
	 *            输出格式，包括："webview", "html", "htmls", "pdf", "longimage",
	 *            "images", "svgs"。
	 *            <ul>
	 *            <li>"webview" - 转换结果保存在九云图服务器，可通过链接在浏览器中展现，不支持下载；</li>
	 *            <li>"html" - 生成一个包括文档所有页面内容的完整 HTML，不含任何脚本和外链，图片均以 svg 或
	 *            base64 形式内嵌，支持下载；</li>
	 *            <li>"htmls" - 文档的每个页面转换成一个独立的 HTML，不含任何脚本和外链，图片均以 svg 或 base64
	 *            形式内嵌)，支持下载；</li>
	 *            <li>"pdf" - 生成 PDF，支持下载；</li>
	 *            <li>"longimage" - 生成长图片，支持下载；</li>
	 *            <li>"images" - 文档的每个页面转换成一个单独的图片，支持下载；</li>
	 *            <li>"svgs" - 文档的每个页面转换成一个单独的 SVG 文件，支持下载。</li>
	 *            </ul>
	 */
	public YuntuConfig(String outputType) {
		this.outputType = outputType;
	}

	/**
	 * 获取文档转换的输出格式。
	 * 
	 * @return 输出格式，包括："html", "htmls", "pdf", "longimage", "images", "svgs"。
	 *         <ul>
	 *         <li>"webview" - 转换结果保存在九云图服务器，可通过链接在浏览器中展现，不支持下载；</li>
	 *         <li>"html" - 生成一个包括文档所有页面内容的完整 HTML，不含任何脚本和外链，图片均以 svg 或 base64
	 *         形式内嵌，支持下载；</li>
	 *         <li>"htmls" - 文档的每个页面转换成一个独立的 HTML，不含任何脚本和外链，图片均以 svg 或 base64
	 *         形式内嵌)，支持下载；</li>
	 *         <li>"pdf" - 生成 PDF，支持下载；</li>
	 *         <li>"longimage" - 生成长图片，支持下载；</li>
	 *         <li>"images" - 文档的每个页面转换成一个单独的图片，支持下载；</li>
	 *         <li>"svgs" - 文档的每个页面转换成一个单独的 SVG 文件，支持下载。</li>
	 *         </ul>
	 */
	public String getOutputType() {
		return outputType;
	}

	/**
	 * 设置文档转换的输出格式。
	 * 
	 * @param outputType
	 *            输出格式，包括："html", "htmls", "pdf", "longimage", "images", "svgs"。
	 *            <ul>
	 *            <li>"webview" - 转换结果保存在九云图服务器，可通过链接在浏览器中展现，不支持下载；</li>
	 *            <li>"html" - 生成一个包括文档所有页面内容的完整 HTML，不含任何脚本和外链，图片均以 svg 或
	 *            base64 形式内嵌，支持下载；</li>
	 *            <li>"htmls" - 文档的每个页面转换成一个独立的 HTML，不含任何脚本和外链，图片均以 svg 或 base64
	 *            形式内嵌)，支持下载；</li>
	 *            <li>"pdf" - 生成 PDF，支持下载；</li>
	 *            <li>"longimage" - 生成长图片，支持下载；</li>
	 *            <li>"images" - 文档的每个页面转换成一个单独的图片，支持下载；</li>
	 *            <li>"svgs" - 文档的每个页面转换成一个单独的 SVG 文件，支持下载。</li>
	 *            </ul>
	 */
	public void setOutputType(String outputType) {
		this.outputType = outputType;
	}

	/**
	 * 获取文字水印内容。
	 * 
	 * @return 文字水印内容。
	 */
	public String getWatermark() {
		return watermark;
	}

	/**
	 * 设置文字水印内容。
	 * 
	 * @param watermark
	 *            文字水印内容。
	 */
	public void setWatermark(String watermark) {
		this.watermark = watermark;
	}
}
