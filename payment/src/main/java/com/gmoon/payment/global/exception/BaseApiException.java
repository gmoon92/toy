package com.gmoon.payment.global.exception;

public interface BaseApiException {

	String getApiReturnCode();

	String getApiDetailCode();

	String getApiMessage();
}
