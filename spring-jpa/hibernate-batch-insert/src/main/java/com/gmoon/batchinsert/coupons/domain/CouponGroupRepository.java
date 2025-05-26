package com.gmoon.batchinsert.coupons.domain;

public interface CouponGroupRepository {
	CouponGroup saveAndFlush(CouponGroup couponGroup);
}
