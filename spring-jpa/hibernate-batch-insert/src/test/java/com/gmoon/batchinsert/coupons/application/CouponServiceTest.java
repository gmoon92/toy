package com.gmoon.batchinsert.coupons.application;

import com.gmoon.batchinsert.coupons.application.dto.CouponRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThatCode;

@SpringBootTest
class CouponServiceTest {

	@Autowired
	private CouponService service;

	@Test
	void issue() {
		CouponRequest couponRequest = new CouponRequest(
			 "new 2025",
			 100_000, // 1s
			 Instant.now(),
			 Instant.now()
		);

		assertThatCode(() -> service.issue(couponRequest))
			 .doesNotThrowAnyException();
	}
}
