package com.gmoon.payment.global.vo;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Currency {
	WON("₩", 0, "원화 (KRW)"),
	YEN("¥", 0, "일본 엔화 (JPY)"),
	YUN("₣", 2, "유로화 (EUR)"),
	USD("$", 2, "미국 달러 (USD)"),
	EUR("€", 2, "유로화 (EUR)"),
	GBP("£", 2, "영국 파운드 (GBP)"),
	AUD("A$", 2, "호주 달러 (AUD)"),
	CNY("元", 2, "중국 위안화 (CNY)"),
	INR("₹", 2, "인도 루피 (INR)"),
	CAD("C$", 2, "캐나다 달러 (CAD)");

	public final String unit;   // 통화 기호
	public final int decimalPlaces;  // 소수점 자릿수
	public final String description;  // 소수점 자릿수
}
