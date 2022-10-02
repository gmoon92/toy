package com.gmoon.springwebconverter.config.error.exception;

public class ConversionFailedException extends IllegalArgumentException {

	public ConversionFailedException() {
		super("요청한 문자열을 이넘 클래스로 변환할 수 없습니다.");
	}
}
