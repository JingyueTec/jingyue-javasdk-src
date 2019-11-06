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
package com.jingyue.DocConversion.demo;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import com.jingyue.DocConversion.Converter;
import com.jingyue.DocConversion.YuntuConfig;
import com.jingyue.DocConversion.common.YuntuDoc;
import com.jingyue.DocConversion.common.YuntuException;

/**
 * 这是一个演示九云图 Java SDK 的 Demo 程序，可直接运行。
 * 
 * @version 1.0
 */
public class YuntuDemo {

	/** 用来演示的样例文档。 */
	private final static String DEMO_DOC_URL = "https://image2.9yuntu.cn/resources/api/%e4%b9%9d%e4%ba%91%e5%9b%beAPI%e4%bd%bf%e7%94%a8%e8%af%b4%e6%98%8e.docx";

	/**
	 * 九云图 Demo 主程序。
	 * 
	 * @param args
	 *            命令行参数。
	 * @throws YuntuException
	 *             文档转换异常。
	 * @throws IOException
	 *             文档显示异常。
	 * @throws URISyntaxException
	 *             网络访问异常。
	 */
	public static void main(String[] args) throws YuntuException, IOException,
			URISyntaxException {

		System.out.println("九云图转换开始...");

		Converter converter = new com.jingyue.DocConversion.Converter();

		converter.setConfig(new YuntuConfig("html"));

		YuntuDoc doc = converter.convert(DEMO_DOC_URL);

		if (doc.isSuccess()) {
			String pdfURL = converter.getPDF(doc);
			String htmlURL = converter.getWholeHTML(doc);
			String webViewUrl = converter.getWebviewURL(doc);

			// 输出生成的 PDF 和 HTML 链接。
			System.out.println("转换生成的 PDF: " + pdfURL);
			System.out.println("转换生成的 HTML: " + htmlURL);

			// 打开浏览器，以网页形式展现转换后的文档。
			Desktop.getDesktop().browse(new URI(webViewUrl));
			System.out.println("九云图转换完成。");
		} else {
			System.out.println("转换失败: " + doc.getMessage());
		}
	}
}
