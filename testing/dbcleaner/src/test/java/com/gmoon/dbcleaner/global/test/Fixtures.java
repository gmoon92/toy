package com.gmoon.dbcleaner.global.test;

import com.gmoon.dbcleaner.movies.domain.Coupon;
import com.gmoon.dbcleaner.movies.domain.Movie;

public class Fixtures {

	public static Movie newMovie(Long id) {
		return Movie.builder()
			 .id(id)
			 .build();
	}

	public static Coupon newCoupon(Movie movie) {
		return Coupon.builder()
			 .movie(movie)
			 .build();
	}
}
