package com.gmoon.batchinsert.coupons.domain;

import java.util.Collection;

public interface CouponRepository {
	void multiInsert(Collection<Coupon> coupons);
}
