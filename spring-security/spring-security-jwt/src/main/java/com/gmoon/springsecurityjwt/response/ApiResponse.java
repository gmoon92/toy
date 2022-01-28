package com.gmoon.springsecurityjwt.response;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
	private final T data;

	@Builder(access = AccessLevel.PRIVATE)
	private ApiResponse(HttpStatus httpStatus, String code, String message, T data) {
		this.httpStatus = httpStatus;
		this.code = code;
		this.message = message;
		this.data = data;
	}

	public static ApiResponse of(Throwable e, HttpStatus httpStatus) {
		return ApiResponse.builder()
			.httpStatus(httpStatus)
			.message(e.getMessage())
			.build();
	}
}
