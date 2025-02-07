package com.gmoon.payment.global.exception;

import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler
	public ResponseEntity<ErrorResponse> handleNoResourceFoundException(NoResourceFoundException e) {
		printWarnLog(ErrorCode.BAD_REQUEST, e.getMessage());
		return new ResponseEntity<>(ErrorResponse.from(ErrorCode.BAD_REQUEST), HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler
	public ResponseEntity<ErrorResponse> handleBindException(BindException e) {
		var message = convertToMessage(e);
		printWarnLog(ErrorCode.BAD_REQUEST, message);
		return new ResponseEntity<>(ErrorResponse.of(ErrorCode.BAD_REQUEST, message), HttpStatus.BAD_REQUEST);
	}

	private String convertToMessage(BindException e) {
		var fieldErrors = e.getBindingResult().getFieldErrors();
		return fieldErrors.stream()
			 .map(it -> String.format("%s: %s", it.getField(), StringUtils.defaultString(it.getDefaultMessage())))
			 .collect(Collectors.joining(", "));
	}

	@ExceptionHandler
	public ResponseEntity<ErrorResponse> handleBaseException(BaseException e) {
		printWarnLog(e.getErrorCode(), e.getDetailCode(), e.getMessage());
		ErrorResponse errorResponse = getErrorResponse(e);
		return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
	}

	private ErrorResponse getErrorResponse(BaseException e) {
		if (e instanceof BaseApiException) {
			return ErrorResponse.of(e.getErrorCode(), e.getDetailCode(), e.getMessage(), (BaseApiException)e);
		}

		return ErrorResponse.of(e.getErrorCode(), e.getDetailCode(), e.getMessage());
	}

	private void printWarnLog(String errorCode, String message) {
		log.warn("errorCode: {}, message: {}", errorCode, message);
	}

	private void printWarnLog(String errorCode, String detailCode, String message) {
		log.warn("errorCode: {}, {} message: {}", errorCode, detailCode, message);
	}

	@ExceptionHandler
	public ResponseEntity<ErrorResponse> handleException(Exception e) {
		log.error(String.format("errorCode: %s, message: %s", ErrorCode.SYSTEM_ERROR, e.getMessage()), e);
		return new ResponseEntity<>(ErrorResponse.from(ErrorCode.SYSTEM_ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
