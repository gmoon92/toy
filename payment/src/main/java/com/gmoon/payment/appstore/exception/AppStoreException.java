package com.gmoon.payment.appstore.exception;

import com.gmoon.payment.global.exception.BaseException;
import com.gmoon.payment.global.exception.ErrorCode;

@SuppressWarnings("serial")
public abstract class AppStoreException extends BaseException {

	AppStoreException(String message) {
		super(message);
	}

	AppStoreException(Throwable cause) {
		super(cause);
	}

	AppStoreException(Throwable cause, String message) {
		super(cause, message);
	}

	@Override
	public String getErrorCode() {
		return ErrorCode.APP_STORE_CLIENT_ERROR;
	}
}
