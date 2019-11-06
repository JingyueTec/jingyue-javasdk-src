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
 * <p>
 * 表示访问九云图文档转换服务时遇到的异常。包括服务发送请求时出现的错误，以及客户端无法处理返回结果。
 * 例如，在发送请求时网络连接不可用等。异常中包含了错误信息 ，用于让调用者进行特定的处理。
 * </p>
 */
public class YuntuException extends Exception {

	/** 系统自动生成的系列号。 */
	private static final long serialVersionUID = -1173377338525899431L;

	/**
	 * 用给定的异常信息构造新实例。
	 * 
	 * @param message
	 *            异常信息。
	 */
	public YuntuException(String message) {
		super(message);
	}

	/**
	 * 用表示异常原因的对象构造新实例。
	 * 
	 * @param cause
	 *            异常原因。
	 */
	public YuntuException(Throwable cause) {
		super(cause);
	}
}
