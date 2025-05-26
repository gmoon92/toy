package com.gmoon.batchinsert.coupons.application.dto;

import java.time.Instant;

public record CouponRequest(
	 String name,
	 int couponCount,
	 Instant startTime,
	 Instant endTime
) {
}
