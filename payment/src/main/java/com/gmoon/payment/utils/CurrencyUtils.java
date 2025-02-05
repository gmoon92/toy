package com.gmoon.payment.utils;

import com.gmoon.payment.vo.Currency;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.text.DecimalFormat;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CurrencyUtils {

	public static String formatCurrencyString(Currency currency, BigDecimal price) {
		var currencyFormatted = newCurrencyDecimalFormat(currency);
		var result = currencyFormatted.format(price);
		log.debug("Converting currency {} with price {} to formatted price: {}", currency, price, result);
		return switch (currency) {
			case CNY, YEN -> result + " " + currency.unit;
			default -> currency.unit + " " + result;
		};
	}

	private static DecimalFormat newCurrencyDecimalFormat(Currency currency) {
		var pattern = new StringBuilder("#,##0");
		if (currency.decimalPlaces > 0) {
			pattern.append(".");
			if (Currency.CNY == currency) {
				pattern.append("0".repeat(currency.decimalPlaces));
			} else {
				pattern.append("#".repeat(currency.decimalPlaces));
			}
		}
		return new DecimalFormat(pattern.toString());
	}
}
