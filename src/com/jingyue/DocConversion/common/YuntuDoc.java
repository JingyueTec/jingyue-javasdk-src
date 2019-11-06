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
package com.jingyue.DocConversion.common;

/**
 * 文档状态信息。
 */
public class YuntuDoc {

	/** 文档 ID */
	private String id = null;

	/** 文档状态码 (0--转换成功；1--转换进行中；2--转换失败) */
	private int code;

	/** 文档状态信息。 */
	private String message = "";

	/** 文档状态页面 URL。 */
	private String statusPageURL = null;

	/**
	 * 构造函数。
	 */
	public YuntuDoc() {
	}

	/**
	 * 构造函数。
	 * 
	 * @param docID
	 *            文档 ID。
	 */
	public YuntuDoc(String docID) {
		this.setID(docID);
	}

	/**
	 * 返回文档 ID。
	 * 
	 * @return 文档 ID。
	 */
	public String getID() {
		return id;
	}

	/**
	 * 设置文档 ID。
	 * 
	 * @param docID
	 *            文档 ID。
	 */
	public void setID(String docID) {
		this.id = docID;
	}

	/**
	 * 判断文档转换是否成功。
	 * 
	 * @return <code>true</code> 转换成功；<code>false</code> 转换失败。
	 */
	public boolean isSuccess() {
		return this.getID() != null && this.getCode() == 0;
	}

	/**
	 * 返回文档状态码。
	 * 
	 * @return 文档状态码。0--转换成功；1--转换进行中；2--转换失败。
	 */
	public int getCode() {
		return code;
	}

	/**
	 * 设置文档状态码。
	 * 
	 * @param code
	 *            文档状态码。0--转换成功；1--转换进行中；2--转换失败。
	 */
	public void setCode(int code) {
		this.code = code;
	}

	/**
	 * 返回文档状态信息。
	 * 
	 * @return 文档状态信息。
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * 设置文档状态信息。
	 * 
	 * @param message
	 *            文档状态信息。
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * 返回文档状态页面 URL。
	 * 
	 * @return 文档状态页面 URL。
	 */
	public String getStatusPage() {
		return statusPageURL;
	}

	/**
	 * 设置文档状态页面。
	 * 
	 * @param statusPageURL
	 *            文档状态页面 URL。
	 */
	public void setStatusPage(String statusPageURL) {
		this.statusPageURL = statusPageURL;
	}
}
