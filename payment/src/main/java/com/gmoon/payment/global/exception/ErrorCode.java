package com.gmoon.payment.global.exception;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ErrorCode {

	public static final String BAD_REQUEST = "9002";
	public static final String ERROR_CERTIFICATE_FILE = "9003";

	public static final String APP_STORE_CLIENT_ERROR = "9100";
	public static final String APP_STORE_API_REQUEST_ERROR = "9101";
	public static final String APP_STORE_IO_EXCEPTION = "9101";
	public static final String APP_STORE_INVALID_SIGNING_DATA = "9101";

	public static final String SYSTEM_ERROR = "9999";
}
