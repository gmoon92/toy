package com.gmoon.batchinsert.coupons.infra;

import com.gmoon.batchinsert.coupons.domain.CouponGroup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaCouponGroupRepository extends JpaRepository<CouponGroup, String> {
}
