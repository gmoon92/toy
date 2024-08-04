package com.gmoon.dbrestore.web.logs.domain;

import com.gmoon.dbrestore.web.movies.domain.Coupon;
import lombok.Getter;
import lombok.ToString;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@ToString
public class IssueCoupon extends ApplicationEvent {

	private final String eventId;
	private final Long couponId;
	private final LocalDateTime createdAt;

	public IssueCoupon(Coupon coupon) {
		super(coupon);
		this.eventId = UUID.randomUUID().toString();
		this.couponId = coupon.getId();
		this.createdAt = LocalDateTime.now();
	}
}
