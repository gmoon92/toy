package com.gmoon.timesorteduniqueidentifier.global.utils;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class TsidUtilsTest {

	@DisplayName("동시성 검증")
	@Test
	void generate() {
		int userAgent = 1_000;
		int totalCount = 1_000;

		// 비동기적으로 tsid 채번 후 모든 결과 합침
		List<String> coupons = IntStream.range(0, userAgent)
			 .mapToObj(i -> CompletableFuture.supplyAsync(() -> generateIds(totalCount)))
			 .map(CompletableFuture::join)
			 .flatMap(List::stream)
			 .peek(log::debug)
			 .toList();

		assertThat(coupons)
			 .doesNotHaveDuplicates()
			 .hasSize(userAgent * totalCount)
			 // 포맷(xxxx-xxxx-xxxx-xxxx) 검증
			 .allMatch(coupon -> coupon.matches("^[\\w]{4}(-[\\w]{4}){3}$"));
	}

	private List<String> generateIds(int count) {
		int partDigit = 4;
		int repeat = 4;
		String delimiter = "-";
		return IntStream.range(0, count)
			 .mapToObj(i -> TsidUtils.generate(partDigit, repeat, delimiter))
			 .toList();
	}
}
