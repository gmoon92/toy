package com.gmoon.payment.global.vo;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Country {
	SOUTH_KOREA("South Korea", Currency.WON),
	JAPAN("Japan", Currency.YEN),
	UNITED_STATES("United States", Currency.USD),
	EUROPE("Eurozone", Currency.EUR),
	UNITED_KINGDOM("United Kingdom", Currency.GBP),
	AUSTRALIA("Australia", Currency.AUD),
	CHINA("China", Currency.CNY),
	INDIA("India", Currency.INR),
	CANADA("Canada", Currency.CAD);

	public final String value;
	public final Currency currency;
}
