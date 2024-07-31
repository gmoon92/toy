package com.gmoon.dbrecovery.movies.infra;

import com.gmoon.dbrecovery.movies.domain.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaCouponRepository extends JpaRepository<Coupon, Long> {

	List<Coupon> findAllByMovieId(Long movieId);
}
