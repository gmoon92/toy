package com.gmoon.dbrecovery.global.test;

import com.gmoon.dbrecovery.global.recovery.vo.CaseCadeDeleteTable;
import com.gmoon.dbrecovery.global.recovery.vo.Table;
import com.gmoon.dbrecovery.movies.domain.Coupon;
import com.gmoon.dbrecovery.movies.domain.Movie;

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

	public static Table newTable(String tableName) {
		return Table.builder()
			 .tableName(tableName)
			 .build();
	}

	public static CaseCadeDeleteTable newOnDeleteTable(String tableName) {
		return new CaseCadeDeleteTable(newTable(tableName));
	}
}
