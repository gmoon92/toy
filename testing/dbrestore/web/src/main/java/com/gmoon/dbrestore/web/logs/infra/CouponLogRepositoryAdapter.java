package com.gmoon.dbrestore.web.logs.infra;

import com.gmoon.dbrestore.web.logs.domain.CouponLog;
import com.gmoon.dbrestore.web.logs.domain.CouponLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CouponLogRepositoryAdapter implements CouponLogRepository {

	private final JpaCouponLogRepository repository;

	@Override
	public CouponLog save(CouponLog couponLog) {
		return repository.save(couponLog);
	}
}
