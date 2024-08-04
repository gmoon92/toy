package com.gmoon.dbrestore.web.logs.domain;

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

import java.io.Serializable;

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

	@Column(name = "movie_id")
	private Long movieId;

	@Column(name = "coupon_id")
	private Long couponId;

	public static CouponLog from(Coupon coupon) {
		CouponLog couponLog = new CouponLog();
		couponLog.couponId = coupon.getId();
		couponLog.movieId = coupon.getMovie().getId();
		return couponLog;
	}
}
