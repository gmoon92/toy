package com.gmoon.payment.appstore.exception;

import java.io.Serial;

import com.gmoon.payment.global.exception.ErrorCode;

public class AppStoreIOException extends AppStoreException {

	@Serial
	private static final long serialVersionUID = 720413702110695774L;

	public AppStoreIOException(Throwable e) {
		super(e, "Thrown while making the AppStore API request.");
	}

	@Override
	public String getDetailCode() {
		return ErrorCode.APP_STORE_IO_EXCEPTION;
	}
}
