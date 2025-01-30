package com.gmoon.payment.utils;

import com.gmoon.payment.vo.Currency;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class CurrencyUtilsTest {

	@ParameterizedTest
	@EnumSource(value = Currency.class)
	void toMoneyString(Currency currency) {
		log.trace("{}", CurrencyUtils.toMoneyString(currency, BigDecimal.ZERO));
		log.trace("{}", CurrencyUtils.toMoneyString(currency, BigDecimal.valueOf(1_000_000_000.111)));
		log.trace("{}", CurrencyUtils.toMoneyString(currency, BigDecimal.valueOf(100)));
		log.trace("{}", CurrencyUtils.toMoneyString(currency, BigDecimal.valueOf(100.1)));

		assertThat(CurrencyUtils.toMoneyString(Currency.CNY, BigDecimal.ZERO)).isEqualTo("0 元");
		assertThat(CurrencyUtils.toMoneyString(Currency.CNY, BigDecimal.valueOf(0.1))).isEqualTo("0.10 元");
		assertThat(CurrencyUtils.toMoneyString(Currency.CNY, BigDecimal.valueOf(111))).isEqualTo("111.00 元");
		assertThat(CurrencyUtils.toMoneyString(Currency.CNY, BigDecimal.valueOf(111.123))).isEqualTo("111.12 元");
		assertThat(CurrencyUtils.toMoneyString(Currency.CNY, BigDecimal.valueOf(111.1))).isEqualTo("111.10 元");
	}
}
