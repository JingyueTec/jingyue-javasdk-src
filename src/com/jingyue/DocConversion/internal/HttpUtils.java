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
package com.jingyue.DocConversion.internal;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.cloud.apigateway.sdk.utils.Client;
import com.cloud.apigateway.sdk.utils.Request;
import com.jingyue.DocConversion.common.YuntuException;

public class HttpUtils {

	/**
	 * Send a HTTP GET request.
	 * 
	 * @param host
	 * @param path
	 * @param method
	 * @param headers
	 * @param querys
	 * @return
	 * @throws YuntuException
	 */
	public static synchronized String get(String host, String path, Map<String, String> headers,
			Map<String, String> querys) throws YuntuException {

		try {
			if (querys != null && querys.get("key") != null && querys.get("secret") != null) {
				return request(host, path, headers, querys);
			} else {
				String url = buildUrl(host, path, querys);
				URL httpUrl = new URL(url);
				HttpURLConnection conn = (HttpURLConnection) httpUrl.openConnection();

				if (headers != null) {
					for (Map.Entry<String, String> header : headers.entrySet()) {
						conn.setRequestProperty(header.getKey(), header.getValue());
					}
				}

				if (conn.getResponseCode() == 200) {
					return getResponseAsString(conn);
				} else {
					throw new YuntuException(getResponseAsString(conn));
				}
			}
		} catch (IOException e) {
			String response = "Please check the AppCode, " + e.getLocalizedMessage();

			throw new YuntuException(response);
		} catch (Exception e) {
			throw new YuntuException(e);
		}
	}

	/**
	 * Send a HTTP POST request.
	 * 
	 * @param host
	 * @param path
	 * @param method
	 * @param headers
	 * @param querys
	 * @return
	 * @throws YuntuException
	 */
	public static synchronized String post(String host, String path, Map<String, String> headers,
			Map<String, String> querys, InputStream inStream, String mimeType) throws YuntuException {

		try {
			String url = buildUrl(host, path, querys);
			URL httpUrl = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) httpUrl.openConnection();

			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", mimeType);
			for (Map.Entry<String, String> header : headers.entrySet()) {
				conn.setRequestProperty(header.getKey(), header.getValue());
			}

			BufferedOutputStream out = new BufferedOutputStream(conn.getOutputStream());

			byte[] bytes = new byte[1024];
			int numReadByte = 0;
			while ((numReadByte = inStream.read(bytes, 0, 1024)) > 0) {

				out.write(bytes, 0, numReadByte);
			}
			out.flush();
			inStream.close();
			if (conn.getResponseCode() == 200) {
				return getResponseAsString(conn);
			} else {
				throw new YuntuException(getResponseAsString(conn));
			}
		} catch (IOException e) {
			String response = "Please check the AppCode, " + e.getLocalizedMessage();

			throw new YuntuException(response);
		}
	}

	private static String request(String host, String path, Map<String, String> headers, Map<String, String> querys)
			throws Exception {

		Request request = new Request();
		String key = querys.remove("key");
		String secret = querys.remove("secret");

		if (key != null && secret != null) {
			request.setKey(key);
			request.setSecret(secret);
		}
		request.setMethod("GET");
		request.setUrl(host + path);
		for (Map.Entry<String, String> query : querys.entrySet()) {
			if (query != null) {
				String k = query.getKey();
				String v = query.getValue();

				if (k != null && v != null) {
					request.addQueryStringParam(k, URLEncoder.encode(v, "utf-8"));
				}
			}
		}

		if (headers != null) {
			for (Map.Entry<String, String> header : headers.entrySet()) {
				String k = header.getKey();
				String v = header.getValue();

				if (k != null && v != null) {
					request.addHeader(k, v);
				}
			}
		}
		request.setBody("yuntu");

		CloseableHttpClient client = null;

		try {
			// Sign the request.
			HttpRequestBase signedRequest = Client.sign(request);

			// Send the request.
			client = HttpClients.custom().build();

			HttpResponse response = client.execute(signedRequest);

			return response != null ? EntityUtils.toString(response.getEntity(), "UTF-8") : "";
		} finally {
			try {
				if (client != null) {
					client.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private static String buildUrl(String host, String path, Map<String, String> querys)
			throws UnsupportedEncodingException {

		StringBuilder sbUrl = new StringBuilder();

		sbUrl.append(host);
		sbUrl.append(path);
		if (null != querys) {
			StringBuilder sbQuery = new StringBuilder();

			for (Map.Entry<String, String> query : querys.entrySet()) {
				if (query != null && query.getValue() != null) {
					if (0 < sbQuery.length()) {
						sbQuery.append("&");
					}
					sbQuery.append(query.getKey());
					sbQuery.append("=");
					sbQuery.append(URLEncoder.encode(query.getValue(), "utf-8"));
				}
			}
			if (0 < sbQuery.length()) {
				sbUrl.append("?").append(sbQuery);
			}
		}
		return sbUrl.toString();
	}

	private static String getResponseAsString(HttpURLConnection conn) throws IOException {

		InputStream es = conn.getErrorStream();

		if (es == null) {
			return getStreamAsString(conn.getInputStream(), "UTF-8");
		} else {
			String msg = getStreamAsString(es, "UTF-8");
			throw new IOException(conn.getResponseCode() + ":" + msg);
		}
	}

	private static String getStreamAsString(InputStream stream, String charset) throws IOException {

		try {
			int count;
			char[] chars = new char[256];
			StringWriter writer = new StringWriter();
			BufferedReader reader = new BufferedReader(new InputStreamReader(stream, charset));

			while ((count = reader.read(chars)) > 0) {
				writer.write(chars, 0, count);
			}
			return writer.toString();
		} finally {
			if (stream != null) {
				stream.close();
			}
		}
	}
}
