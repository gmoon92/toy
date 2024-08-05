package com.gmoon.dbrestore.web.logs.application;


import com.gmoon.dbrestore.web.logs.domain.CouponLog;
import com.gmoon.dbrestore.web.logs.domain.CouponLogRepository;
import com.gmoon.dbrestore.web.logs.domain.IssueCoupon;
import com.gmoon.dbrestore.web.movies.domain.Coupon;
import com.gmoon.dbrestore.web.movies.domain.CouponRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogService {

	private final CouponRepository couponRepository;
	private final CouponLogRepository couponLogRepository;

	@Async
	@TransactionalEventListener(
		 phase = TransactionPhase.AFTER_COMMIT,
		 fallbackExecution = true,
		 condition = "#issueCoupon.couponId != null "
	)
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void issueCoupon(IssueCoupon issueCoupon) {
		log.info("issue coupon: {}", issueCoupon);

		Long couponId = issueCoupon.getCouponId();
		Coupon coupon = couponRepository.get(couponId);

		// Asynchronous testing of very long processes.
		IntStream.range(0, 1000)
			 .peek(i -> log.info("coupon #{} issued", i))
			 .mapToObj(i -> CouponLog.from(coupon))
			 .forEach(couponLogRepository::save);
	}
}
