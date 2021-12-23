package com.gmoon.springsecuritycsrfaspect.csrf.token;

import org.springframework.security.web.csrf.CsrfToken;

public abstract class BaseCsrfToken implements CsrfToken {
	private static final String HEADER_NAME = "X-CSRF-TOKEN";
	private static final String PARAMETER_NAME = "_csrf";

	@Override
	public String getHeaderName() {
		return HEADER_NAME;
	}

	@Override
	public String getParameterName() {
		return PARAMETER_NAME;
	}

	@Override
	public String getToken() {
		return getValue();
	}

	public abstract String getValue();
}
