package com.gmoon.dbcleaner.movies.infra;

import com.gmoon.dbcleaner.movies.domain.Coupon;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface JpaCouponRepository extends CrudRepository<Coupon, Long> {

	List<Coupon> findAllByMovieId(Long movieId);
}
