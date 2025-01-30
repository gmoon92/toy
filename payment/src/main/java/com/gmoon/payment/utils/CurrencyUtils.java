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

	public static String toMoneyString(Currency currency, BigDecimal amount) {
		String pattern = getPattern(currency);
		String result = toMoney(amount, pattern);
		log.debug("Converting({}) currency {} to {}, {}", pattern, currency, amount, result);
		return switch (currency) {
			case CNY, YEN ->
				 result + " " + currency.unit;
			default -> currency.unit + " " + result;
		};
	}

	private static String toMoney(BigDecimal amount, String pattern) {
		if (amount.equals(BigDecimal.ZERO)) {
			return "0";
		}

		return new DecimalFormat(pattern)
			 .format(amount.doubleValue());
	}

	private static String getPattern(Currency currency) {
		StringBuilder pattern = new StringBuilder("#,##0");
		if (currency.decimalPlaces > 0) {
			pattern.append(".");
			if (Currency.CNY == currency) {
				pattern.append("0".repeat(currency.decimalPlaces));
			} else {
				pattern.append("#".repeat(currency.decimalPlaces));
			}
		}
		return pattern.toString();
	}
}
