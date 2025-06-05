package com.gmoon.dbrestore.web.logs.infra;

import com.gmoon.dbrestore.web.logs.domain.CouponLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaCouponLogRepository extends JpaRepository<CouponLog, Long> {
}
