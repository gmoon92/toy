package com.gmoon.dbrestore.web.logs.domain;

import java.time.Instant;
import java.util.UUID;

import org.springframework.context.ApplicationEvent;

import com.gmoon.dbrestore.web.movies.domain.Coupon;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class IssueCoupon extends ApplicationEvent {

	private final String eventId;
	private final Long couponId;
	private final Instant createdAt;

	public IssueCoupon(Coupon coupon) {
		super(coupon);
		this.eventId = UUID.randomUUID().toString();
		this.couponId = coupon.getId();
		this.createdAt = Instant.now();
	}
}
