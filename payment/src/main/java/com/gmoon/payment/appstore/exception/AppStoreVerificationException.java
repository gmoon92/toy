package com.gmoon.payment.appstore.exception;

import java.io.Serial;

import com.gmoon.payment.global.exception.ErrorCode;

public class AppStoreVerificationException extends AppStoreException {

	@Serial
	private static final long serialVersionUID = -3175909245884044496L;

	public AppStoreVerificationException(Throwable cause) {
		super(cause);
	}

	@Override
	public String getDetailCode() {
		return ErrorCode.APP_STORE_INVALID_SIGNING_DATA;
	}
}
