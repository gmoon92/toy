package com.gmoon.payment.global.exception;

public record ErrorResponse(
	 String exceptionCode,
	 String exceptionDetailCode,
	 String message,
	 String apiExceptionCode,
	 String apiExceptionDetailCode,
	 String apiExceptionMessage
) {

	public static ErrorResponse from(String exceptionCode) {
		return new ErrorResponse(exceptionCode, null, null, null, null, null);
	}

	public static ErrorResponse of(String exceptionCode, String message) {
		return new ErrorResponse(exceptionCode, "", message, null, null, null);
	}

	public static ErrorResponse of(String exceptionCode, String exceptionDetailCode, String message) {
		return new ErrorResponse(exceptionCode, exceptionDetailCode, message, null, null, null);
	}

	public static ErrorResponse of(String exceptionCode, String exceptionDetailCode, String message,
		 BaseApiException apiException) {
		return new ErrorResponse(exceptionCode, exceptionDetailCode, message, apiException.getApiReturnCode(),
			 apiException.getApiDetailCode(), apiException.getApiMessage());
	}
}
