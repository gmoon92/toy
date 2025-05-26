package com.gmoon.batchinsert.coupons.infra;

import com.gmoon.batchinsert.coupons.domain.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaCouponRepository extends JpaRepository<Coupon, String> {
}
