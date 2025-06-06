package com.gmoon.batchinsert.coupons.application;

import static org.assertj.core.api.Assertions.*;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.gmoon.batchinsert.coupons.application.dto.CouponRequest;

@SpringBootTest
class CouponServiceTest {

	@Autowired
	private CouponService service;

	@Test
	void issue() {
		CouponRequest couponRequest = new CouponRequest(
			 "new 2025",
			 100,
			 Instant.now(),
			 Instant.now()
		);

		assertThatCode(() -> service.issue(couponRequest))
			 .doesNotThrowAnyException();
	}
}
