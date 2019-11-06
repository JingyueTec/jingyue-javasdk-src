package com.jingyue.DocConversion.internal;

import java.util.Map;

import com.jingyue.DocConversion.Converter;
import com.jingyue.DocConversion.YuntuConfig;
import com.jingyue.DocConversion.common.YuntuDoc;
import com.jingyue.DocConversion.common.YuntuException;

public class Sender extends Converter {

	public Sender(YuntuConfig config) {
		this.setConfig(config);
	}

	/**
	 * 重新转换指定的文档。
	 * 
	 * @param docID    文档 ID。
	 * @param fileName 文档名称。
	 * @return 返回一个 <code>YuntuDoc</code> 实例, 其中包含了文档转换状态等信息。
	 * @throws YuntuException 文档转换异常。
	 */
	public YuntuDoc convert(String docID, String fileName) throws YuntuException {
		String path = "/execute/Convert";
		Map<String, String> querys = getQueries();
		Map<String, String> headers = getHeaders();

		querys.put("docID", docID);
		querys.put("creator", "JavaSDK");
		querys.put("fileName", fileName);
		querys.put("from", "JavaSDK");

		headers.put("Content-Type", "application/json");

		String body = HttpUtils.get(host, path, headers, querys);

		return getYuntuDoc(body);
	}
}
