package com.gmoon.springpoi.common.exception;

public class JsonConvertException extends RuntimeException {

	public JsonConvertException(String message) {
		super(message);
	}

	public JsonConvertException(String message, Throwable cause) {
		super(message, cause);
	}

	public JsonConvertException(String message, String jsonStr, Throwable cause) {
		super(formatMessage(message, jsonStr), cause);
	}

	private static String formatMessage(String message, String jsonStr) {
		return message + " | JSON snippet: " + shortJsonString(jsonStr);
	}

	private static String shortJsonString(String json) {
		if (json == null) {
			return "null";
		}

		return json.length() > 100
			 ? json.substring(0, 100) + "..."
			 : json;
	}
}
