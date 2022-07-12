package com.gmoon.hibernateannotation.payment.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ProductDiscriminantType {
	COMPANY(Value.COMPANY),
	USER(Value.USER);

	public final String value;

	public static class Value {
		public static final String COMPANY = "null";
		public static final String USER = "U";
	}
}

