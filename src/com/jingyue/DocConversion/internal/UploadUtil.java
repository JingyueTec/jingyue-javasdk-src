package com.jingyue.DocConversion.internal;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

import com.aliyun.oss.event.ProgressEventType;
import com.aliyun.oss.event.ProgressListener;
import com.jingyue.DocConversion.YuntuConfig;
import com.jingyue.DocConversion.common.YuntuDoc;
import com.jingyue.DocConversion.common.YuntuException;

public class UploadUtil {

	private YuntuDoc doc = null;

	private YuntuConfig config = null;

	public UploadUtil(YuntuConfig config) {
		this.config = config;
		this.doc = new YuntuDoc();
		this.doc.setCode(1);
	}

	public YuntuDoc uploadToOSS(InputStream inputStream, String fileName) throws IOException, YuntuException {
		return uploadToOSS(inputStream, fileName, null);
	}

	public YuntuDoc uploadToOSS(File file) throws IOException, YuntuException {
		return uploadToOSS(file, null);
	}

	private static String generateUUID() {
		return Base62Util.convertTo62(UUID.randomUUID().toString());
	}

	public YuntuDoc uploadToOSS(final File file, final String docID) throws IOException, YuntuException {
		final String token = docID == null ? generateUUID() : docID;

		int length = (int) file.length();
		String name = file.getName();

		String body = HttpUtils.get("https://server.9yuntu.cn", "/execute/UploadFileAction", null, null);
		final String lockID = LockManager.getLockId("LOCK::" + token);

		if (body != null) {
			JSONObject resJsonObj = JsonUtil.parse(body);
			JSONObject jsonObj = null;

			try {
				jsonObj = resJsonObj.getJSONObject("authorizationInfo");
			} catch (JSONException e) {
				throw new YuntuException(e);
			}

			if (jsonObj != null) {
				String accessKeyId = JsonUtil.getString(jsonObj, "AccessKeyId");
				String accessKeySecret = JsonUtil.getString(jsonObj, "AccessKeySecret");
				String securityToken = JsonUtil.getString(jsonObj, "SecurityToken");
				OSSUtil ossUtil = new OSSUtil(accessKeyId, accessKeySecret, securityToken);

				ossUtil.uploadFile(token, name, file, length, new ProgressListener() {

					private long totalBytes = 0;
					private long bytesWritten = 0;

					int last = -1;

					@Override
					public void progressChanged(com.aliyun.oss.event.ProgressEvent progressEvent) {
						long bytes = progressEvent.getBytes();

						ProgressEventType eventType = progressEvent.getEventType();

						switch (eventType) {
						case TRANSFER_STARTED_EVENT:
							break;
						case REQUEST_CONTENT_LENGTH_EVENT:
							this.totalBytes = bytes;
							break;
						case REQUEST_BYTE_TRANSFER_EVENT:
							this.bytesWritten += bytes;
							if (this.totalBytes != -1) {
								int percent = (int) (this.bytesWritten * 100.0 / this.totalBytes);

								if (percent == last) {
									return;
								}
								last = percent;
							} else {
							}
							break;
						case TRANSFER_COMPLETED_EVENT:
							try {
								doc = new Sender(config).convert(token, file.getName());
								if (doc == null || !doc.isSuccess()) {
									System.out.println("error code: " + doc.getCode());
								}
								LockManager.notify(lockID);
							} catch (YuntuException e) {
								e.printStackTrace();
							}
							break;
						case TRANSFER_FAILED_EVENT:
							LockManager.notify(lockID);
							break;
						default:
							break;
						}
					}
				});
				if (doc == null) {
					LockManager.wait(lockID, 10 * 60 * 1000);
				}
			} else {
				String reason = JsonUtil.getString(resJsonObj, "failReason");

				if (reason == null || reason.length() <= 0) {
					reason = "文件上传失败。";
				}
				throw new YuntuException(reason);
			}
		}
		return doc;
	}

	public YuntuDoc uploadToOSS(InputStream inputStream, final String name, final String docID)
			throws IOException, YuntuException {

		final String token = docID == null ? generateUUID() : docID;

		String body = HttpUtils.get("https://server.9yuntu.cn", "/execute/UploadFileAction", null, null);
		final String lockID = LockManager.getLockId("LOCK::" + token);

		if (body != null) {
			JSONObject resJsonObj = JsonUtil.parse(body);
			JSONObject jsonObj = null;

			try {
				jsonObj = resJsonObj.getJSONObject("authorizationInfo");
			} catch (JSONException e) {
				throw new YuntuException(e);
			}

			if (jsonObj != null) {
				String accessKeyId = JsonUtil.getString(jsonObj, "AccessKeyId");
				String accessKeySecret = JsonUtil.getString(jsonObj, "AccessKeySecret");
				String securityToken = JsonUtil.getString(jsonObj, "SecurityToken");
				OSSUtil ossUtil = new OSSUtil(accessKeyId, accessKeySecret, securityToken);

				ossUtil.uploadStream(token, name, inputStream, new ProgressListener() {

					private long totalBytes = 0;
					private long bytesWritten = 0;

					int last = -1;

					@Override
					public void progressChanged(com.aliyun.oss.event.ProgressEvent progressEvent) {
						long bytes = progressEvent.getBytes();

						ProgressEventType eventType = progressEvent.getEventType();

						switch (eventType) {
						case TRANSFER_STARTED_EVENT:
							break;
						case REQUEST_CONTENT_LENGTH_EVENT:
							this.totalBytes = bytes;
							break;
						case REQUEST_BYTE_TRANSFER_EVENT:
							this.bytesWritten += bytes;
							if (this.totalBytes != -1) {
								int percent = (int) (this.bytesWritten * 100.0 / this.totalBytes);

								if (percent == last) {
									return;
								}
								last = percent;
							} else {
							}
							break;
						case TRANSFER_COMPLETED_EVENT:
							try {
								doc = new Sender(config).convert(token, name);
								if (doc == null || !doc.isSuccess()) {
									System.out.println("error code: " + doc.getCode());
								}
								LockManager.notify(lockID);
							} catch (YuntuException e) {
								e.printStackTrace();
							}
							break;
						case TRANSFER_FAILED_EVENT:
							LockManager.notify(lockID);
							break;
						default:
							break;
						}
					}
				});
				if (doc == null) {
					LockManager.wait(lockID, 10 * 60 * 1000);
				}
			} else {
				String reason = JsonUtil.getString(resJsonObj, "failReason");

				if (reason == null || reason.length() <= 0) {
					reason = "数据流上传失败。";
				}
				throw new YuntuException(reason);
			}
		}
		return doc;
	}
}
