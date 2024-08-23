package com.gmoon.dbrestore.web.logs.domain;

import java.io.Serializable;

import com.gmoon.dbrestore.web.movies.domain.Coupon;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "tb_coupon_log")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(of = "id")
@ToString(of = "id")
public class CouponLog implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column
	private Long movieId;

	@Column
	private Long couponId;

	public static CouponLog from(Coupon coupon) {
		CouponLog couponLog = new CouponLog();
		couponLog.couponId = coupon.getId();
		couponLog.movieId = coupon.getMovie().getId();
		return couponLog;
	}
}
