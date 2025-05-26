package com.gmoon.batchinsert.coupons.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Table(name = "tb_coupon_group")
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(of = "id")
@ToString
public class CouponGroup {

	@Id
	@Column(length = 50)
	private String id;

	@Column(length = 30, nullable = false)
	private String name;

	@Column
	private int couponCount;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(nullable = false)
	private Instant startAt;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(nullable = false)
	private Instant endAt;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(nullable = false)
	private Instant createdAt;

	public CouponGroup(String name, int couponCount, Instant startAt, Instant endAt) {
		this.id = UUID.randomUUID().toString();
		this.name = name;
		this.couponCount = couponCount;
		this.startAt = startAt;
		this.endAt = endAt;
		this.createdAt = Instant.now();
	}
}
