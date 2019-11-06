package com.jingyue.DocConversion.internal;

import org.json.JSONException;
import org.json.JSONObject;

import com.jingyue.DocConversion.common.YuntuException;

public class JsonUtil {

	public static JSONObject parse(String jsonStr) throws YuntuException {
		if(jsonStr == null){
			return null;
		}

		if (!jsonStr.equals("") && !jsonStr.equals("{}")) {
			try {
				return new JSONObject(jsonStr);
			} catch (JSONException e) {
				// Does nothing
			}
		}
		return new JSONObject();
	}

	public static String getString(JSONObject jsonObj, String key) {
		String result = null;

		if (jsonObj != null && key != null) {
			try {
				result = jsonObj.getString(key);
			} catch (JSONException e) {
				result = null;
			}
		}
		return result;
	}
}
