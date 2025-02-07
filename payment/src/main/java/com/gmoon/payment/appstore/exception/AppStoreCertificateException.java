package com.gmoon.payment.appstore.exception;

import java.io.Serial;

import com.gmoon.payment.global.exception.ErrorCode;

public class AppStoreCertificateException extends AppStoreException {

	@Serial
	private static final long serialVersionUID = 696752210268646925L;

	public AppStoreCertificateException(String message) {
		super(message);
	}

	public AppStoreCertificateException(Throwable cause, String message) {
		super(cause, message);
	}

	@Override
	public String getDetailCode() {
		return ErrorCode.ERROR_CERTIFICATE_FILE;
	}
}
