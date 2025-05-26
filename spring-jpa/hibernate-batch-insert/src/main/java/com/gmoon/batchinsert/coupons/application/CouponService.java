package com.gmoon.batchinsert.coupons.application;

import com.gmoon.batchinsert.coupons.application.dto.CouponRequest;
import com.gmoon.batchinsert.coupons.domain.Coupon;
import com.gmoon.batchinsert.coupons.domain.CouponGroup;
import com.gmoon.batchinsert.coupons.domain.CouponGroupRepository;
import com.gmoon.batchinsert.coupons.domain.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.IntStream;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class CouponService {

	private final CouponGroupRepository couponGroupRepository;
	private final CouponRepository couponRepository;

	@Transactional
	public void issue(CouponRequest couponRequest) {
		CouponGroup couponGroup = couponGroupRepository.saveAndFlush(new CouponGroup(
			 couponRequest.name(),
			 couponRequest.couponCount(),
			 couponRequest.startTime(),
			 couponRequest.endTime()
		));

		batchInsertCoupons(couponGroup);
	}

	private void batchInsertCoupons(CouponGroup couponGroup) {
		int chunkSize = 50;
		int total = couponGroup.getCouponCount();
		Stream.iterate(
				  0,                             // seed(초기값): 0, 첫번째 청크의 시작 인덱스
				  start -> start < total,      // 종료조건: start < 총 개수일 동안 반복
				  start -> start + chunkSize   // 증분: 청크 사이즈만큼 start 증가 (0 → 1000 → 2000 ...)
			 )
			 .map(start ->
				  IntStream.range(start, Math.min(start + chunkSize, total))
					   .mapToObj(i -> new Coupon(couponGroup))
					   .toList()
			 )
			 .forEach(couponRepository::multiInsert);
	}
}
