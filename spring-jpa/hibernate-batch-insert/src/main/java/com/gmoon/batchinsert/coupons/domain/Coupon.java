package com.gmoon.batchinsert.coupons.domain;

import com.gmoon.javacore.util.TsidUtils;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

@Table(name = "tb_coupon")
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Coupon {

	@Id
	@Column(length = 50)
	private String no;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@OnDelete(action = OnDeleteAction.CASCADE)
	private CouponGroup couponGroup;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(nullable = false)
	private Instant startAt;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(nullable = false)
	private Instant endAt;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(nullable = false)
	private Instant createdAt;

	public Coupon(CouponGroup couponGroup) {
		this.no = TsidUtils.generate(4, 4, "-");
		this.couponGroup = couponGroup;
		this.startAt = couponGroup.getStartAt();
		this.endAt = couponGroup.getEndAt();
		this.createdAt = couponGroup.getCreatedAt();
	}
}
