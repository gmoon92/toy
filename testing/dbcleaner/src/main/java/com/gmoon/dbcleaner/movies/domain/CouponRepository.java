package com.gmoon.dbcleaner.movies.domain;

import java.util.List;

public interface CouponRepository {

	List<Coupon> findAllByMovieId(Long movieId);
}
