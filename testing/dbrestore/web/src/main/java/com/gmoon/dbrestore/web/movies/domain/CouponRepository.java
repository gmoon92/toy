package com.gmoon.dbrestore.web.movies.domain;

import java.util.List;

public interface CouponRepository {

	List<Coupon> findAllByMovieId(Long movieId);
	Coupon get(Long couponId);
}
