package com.gmoon.batchinsert.coupons.infra;

import com.gmoon.batchinsert.coupons.domain.CouponGroup;
import com.gmoon.batchinsert.coupons.domain.CouponGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CouponGroupRepositoryAdapter implements CouponGroupRepository {

	private final JpaCouponGroupRepository jpaRepository;

	@Override
	public CouponGroup saveAndFlush(CouponGroup couponGroup) {
		return jpaRepository.saveAndFlush(couponGroup);
	}
}
