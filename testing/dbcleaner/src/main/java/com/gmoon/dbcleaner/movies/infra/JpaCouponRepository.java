package com.gmoon.dbcleaner.movies.infra;

import com.gmoon.dbcleaner.movies.domain.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaCouponRepository extends JpaRepository<Coupon, Long> {

	List<Coupon> findAllByMovieId(Long movieId);
}
