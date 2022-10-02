package com.gmoon.springwebconverter.model;

import com.gmoon.springwebconverter.config.converter.StringToEnumBinder;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum PaymentType implements StringToEnumBinder {

	KAKAO_BANK("0001"),
	NAVER_PAY("0002"),
	TOSS("0003");

	private final String value;
}
