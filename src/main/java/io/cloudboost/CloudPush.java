package io.cloudboost;

import io.cloudboost.beans.CBResponse;
import io.cloudboost.json.JSONArray;
import io.cloudboost.json.JSONException;
import io.cloudboost.json.JSONObject;
import io.cloudboost.util.CBParser;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CloudPush {
	public CloudPush() {

	}

	/**
	 * adds new GCM credentials to your app and replaces any pre-existing ones.
	 * Additionally, it deletes any other settings for other mobile app
	 * platforms like windows credentials or iOS certificates. If these are
	 * important to your CloudApp, you can add them from the dashboard
	 * 
	 * @param senderId
	 *            the sender ID or commonly known as project Number from google
	 *            developer console
	 * @param apiKey
	 *            the apikey for request authentication also from Google
	 *            Developer console
	 * @throws CloudException
	 */
	public void addSettings(String senderId, String apiKey,
			ObjectCallback callback) throws CloudException {
		if (CloudApp.getAppId() == null) {
			callback.done(null, new CloudException("CloudApp.appId is null"));
			return;
		}
		String _url = CloudApp.getApiUrl() + "/settings/" + CloudApp.getAppId()
				+ "/push";
		// apple
		Map<String, Object> settings = new HashMap<>();

		Map<String, String> credentials = new HashMap<>();
		Map<String, Object> devices = new HashMap<>();
		settings.put("certificates", new Map[] { credentials });
		devices.put("apple", settings);
		// android
		settings = new HashMap<>();

		credentials = new HashMap<>();
		credentials.put("senderId", senderId);
		credentials.put("apiKey", apiKey);
		settings.put("credentials", new Map[] { credentials });
		devices.put("android", settings);
		// windows
		settings = new HashMap<>();

		credentials = new HashMap<>();
		credentials.put("securityId", "xxxx");
		credentials.put("clientSecret", "xxxx");
		settings.put("credentials", new Map[] { credentials });
		devices.put("windows", settings);
		Map<String, Object> _paramMap = new HashMap<>();
		_paramMap.put("key", CloudApp.getAppKey());
		_paramMap.put("settings", devices);
		JSONObject _params = new JSONObject(_paramMap);
		CBResponse response = CBParser.callJson(_url, "PUT", _params);
		try {
			JSONObject ob = new JSONObject(response.getResponseBody());
			callback.done(ob, null);

		} catch (JSONException e) {
			callback.done(null, new CloudException(e.getMessage()));
		}
	}

	public void addDevice(String token, String timezone, String[] channels,
			String appname, final CloudObjectCallback callback)
			throws CloudException {
		if (CloudApp.getAppId() == null) {
			callback.done(null, new CloudException("CloudApp.appId is null"));
			return;
		}
		CloudObject ob = new CloudObject("Device");

		ob.set("deviceToken", token);
		ob.set("deviceOS", "android");
		ob.set("timezone", timezone);
		ob.set("channels", channels);
		Map<String, String> metadata = new HashMap<>();
		metadata.put("appname", appname);
		ob.set("metadata", metadata);
		ob.save(new CloudObjectCallback() {

			@Override
			public void done(CloudObject x, CloudException t)
					throws CloudException {
				callback.done(x, t);

			}
		});
	}

	public void send(PushData pushData, Object query, CloudPushCallback callback)
			throws CloudException {
		String _tableName = "Device";
		CloudQuery pushQuery = new CloudQuery(_tableName);
		if (CloudApp.getAppId() == null) {
			callback.done(null, new CloudException("CloudApp.appId is null"));
			return;
		}
		if (pushData == null) {
			callback.done(null, new CloudException("pushData object is null"));
			return;
		}
		if (pushData.getMessage() == null) {
			callback.done(null, new CloudException(
					"message is not set in pushData"));
			return;
		}
		if (query instanceof CloudQuery) {
			pushQuery = (CloudQuery) query;
			pushQuery.setTableName(_tableName);
		}
		if (query != null && query instanceof String[]) {
			pushQuery.containedIn("channels", (String[]) query);
		}
		if (query instanceof String) {
			pushQuery.containedIn("channels", new String[] { (String) query });
		}
		Map<String, Object> _params = new HashMap<>();
		_params.put("query", pushQuery.getQuery());
		_params.put("sort", pushQuery.getSort());
		_params.put("limit", pushQuery.getLimit());
		_params.put("skip", pushQuery.getSkip());
		_params.put("data", pushData);
		_params.put("key", CloudApp.getAppKey());
		JSONObject _jsonParams = new JSONObject(_params);
		String _url = CloudApp.getServerUrl() + "/push/" + CloudApp.getAppId()
				+ "/send";
		CBResponse response = CBParser.callJson(_url, "POST", _jsonParams);
		if (response.getStatusCode() == 200) {

			callback.done(response.getResponseBody(), null);
		} else {
			callback.done(null, new CloudException(response.getError()));
		}

	}

	public void subscribe(final String channel, final String deviceToken,
			final CloudObjectCallback callback) throws CloudException {
		String _tableName = "Device";
		CloudQuery query = new CloudQuery(_tableName);
		if (CloudApp.getAppId() == null) {
			callback.done(null, new CloudException("CloudApp.appId is null"));
			return;
		}
		if (deviceToken == null) {
			callback.done(null,
					new CloudException("deviceToken object is null"));
			return;
		}
		query.equalTo("deviceToken", deviceToken);
		query.find(new CloudObjectArrayCallback() {

			@Override
			public void done(CloudObject[] x, CloudException t)
					throws CloudException {
				if (t != null) {
					callback.done(null, t);
				} else {
					if (x.length == 0) {
						callback.done(null, new CloudException(
								"No device found for token" + deviceToken));
					} else if (x.length > 1) {
						callback.done(null, new CloudException(
								"Ambiguity, more than 1 device found for token: "
										+ deviceToken));
					} else if (x.length == 1) {
						CloudObject device = x[1];
						JSONArray channels = device.document
								.getJSONArray("channels");
						for (int i = 0; i < channels.length(); i++) {
							String chann = channels.getString(i);
							if (chann.equalsIgnoreCase(channel)) {
								callback.done(null, new CloudException(
										"This device is already subscribed to similar channel: "
												+ chann));
								return;
							}
						}
						channels.put(channel);
						device.set("channels", channels);
						device.save(new CloudObjectCallback() {

							@Override
							public void done(CloudObject x, CloudException t)
									throws CloudException {
								if (t != null)
									callback.done(null, t);
								else {
									callback.done(x, t);
								}

							}
						});

					}
				}

			}
		});

	}

	public void subscribe(final String[] channelArray,
			final String deviceToken, final CloudObjectCallback callback)
			throws CloudException {
		String _tableName = "Device";
		CloudQuery query = new CloudQuery(_tableName);
		if (CloudApp.getAppId() == null) {
			callback.done(null, new CloudException("CloudApp.appId is null"));
			return;
		}
		if (deviceToken == null) {
			callback.done(null,
					new CloudException("deviceToken object is null"));
			return;
		}
		query.equalTo("deviceToken", deviceToken);
		query.find(new CloudObjectArrayCallback() {

			@Override
			public void done(CloudObject[] x, CloudException t)
					throws CloudException {
				if (t != null) {
					callback.done(null, t);
				} else {
					if (x.length == 0) {
						callback.done(null, new CloudException(
								"No device found for token" + deviceToken));
					} else if (x.length > 1) {
						callback.done(null, new CloudException(
								"Ambiguity, more than 1 device found for token: "
										+ deviceToken));
					} else if (x.length == 1) {
						CloudObject device = x[1];
						JSONArray channels = device.document
								.getJSONArray("channels");
						List<String> channArr = Arrays.asList(channelArray);
						for (int i = 0; i < channels.length(); i++) {
							String chann = channels.getString(i);
							if (channArr.contains(chann)) {
								callback.done(null, new CloudException(
										"This device is already subscribed to similar channel: "
												+ chann));
								return;
							}
						}
						channels.put(channArr);
						device.set("channels", channels);
						device.save(new CloudObjectCallback() {

							@Override
							public void done(CloudObject x, CloudException t)
									throws CloudException {
								if (t != null)
									callback.done(null, t);
								else {
									callback.done(x, t);
								}

							}
						});

					}
				}

			}
		});

	}

	public void unSubscribe(final String channel, final String deviceToken,
			final CloudObjectCallback callback) throws CloudException {
		String _tableName = "Device";
		CloudQuery query = new CloudQuery(_tableName);
		if (CloudApp.getAppId() == null) {
			callback.done(null, new CloudException("CloudApp.appId is null"));
			return;
		}
		if (deviceToken == null) {
			callback.done(null,
					new CloudException("deviceToken object is null"));
			return;
		}
		query.equalTo("deviceToken", deviceToken);
		query.find(new CloudObjectArrayCallback() {

			@Override
			public void done(CloudObject[] x, CloudException t)
					throws CloudException {
				if (t != null) {
					callback.done(null, t);
				} else {
					if (x.length == 0) {
						callback.done(null, new CloudException(
								"No device found for token" + deviceToken));
					} else if (x.length > 1) {
						callback.done(null, new CloudException(
								"Ambiguity, more than 1 device found for token: "
										+ deviceToken));
					} else if (x.length == 1) {
						CloudObject device = x[1];
						JSONArray channels = device.document
								.getJSONArray("channels");
						for (int i = 0; i < channels.length(); i++) {
							String chann = channels.getString(i);
							if (!chann.equalsIgnoreCase(channel)) {
								callback.done(null, new CloudException(
										"This device is not subscribed to given channel: "
												+ chann));
								return;
							} else {
								channels.remove(i);
							}
						}

						device.set("channels", channels);
						device.save(new CloudObjectCallback() {

							@Override
							public void done(CloudObject x, CloudException t)
									throws CloudException {
								if (t != null)
									callback.done(null, t);
								else {
									callback.done(x, t);
								}

							}
						});

					}
				}

			}
		});

	}

	public void unSubscribe(final String[] channelArray,
			final String deviceToken, final CloudObjectCallback callback)
			throws CloudException {
		String _tableName = "Device";
		CloudQuery query = new CloudQuery(_tableName);
		if (CloudApp.getAppId() == null) {
			callback.done(null, new CloudException("CloudApp.appId is null"));
			return;
		}
		if (deviceToken == null) {
			callback.done(null,
					new CloudException("deviceToken object is null"));
			return;
		}
		query.equalTo("deviceToken", deviceToken);
		query.find(new CloudObjectArrayCallback() {

			@Override
			public void done(CloudObject[] x, CloudException t)
					throws CloudException {
				if (t != null) {
					callback.done(null, t);
				} else {
					if (x.length == 0) {
						callback.done(null, new CloudException(
								"No device found for token" + deviceToken));
					} else if (x.length > 1) {
						callback.done(null, new CloudException(
								"Ambiguity, more than 1 device found for token: "
										+ deviceToken));
					} else if (x.length == 1) {
						CloudObject device = x[1];
						JSONArray channels = device.document
								.getJSONArray("channels");
						List<String> channArr = Arrays.asList(channelArray);
						for (int i = 0; i < channels.length(); i++) {
							String chann = channels.getString(i);
							if (!channArr.contains(chann)) {
								callback.done(null, new CloudException(
										"This device is not subscribed to given channel: "
												+ chann));
								return;
							} else {
								channels.remove(i);
							}
						}
						device.set("channels", channels);
						device.save(new CloudObjectCallback() {

							@Override
							public void done(CloudObject x, CloudException t)
									throws CloudException {
								if (t != null)
									callback.done(null, t);
								else {
									callback.done(x, t);
								}

							}
						});

					}
				}

			}
		});

	}
}
