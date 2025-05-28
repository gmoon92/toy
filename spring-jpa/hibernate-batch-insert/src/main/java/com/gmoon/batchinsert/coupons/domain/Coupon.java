package com.gmoon.batchinsert.coupons.domain;

import java.time.Instant;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.gmoon.batchinsert.coupons.domain.vo.RegisteredBy;
import com.gmoon.batchinsert.global.common.BaseEntity;
import com.gmoon.javacore.util.TsidUtils;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "tb_coupon")
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Coupon extends BaseEntity {

	@Id
	@Column(length = 50)
	private String no;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@OnDelete(action = OnDeleteAction.CASCADE)
	private CouponGroup couponGroup;

	@Embedded
	private RegisteredBy registeredBy = new RegisteredBy();

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
