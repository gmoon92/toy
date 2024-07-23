package com.gmoon.dbcleaner.movies.infra;

import com.gmoon.dbcleaner.movies.domain.Coupon;
import com.gmoon.dbcleaner.movies.domain.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class CouponRepositoryAdapter implements CouponRepository {

	private final JpaCouponRepository repository;

	@Override
	public List<Coupon> findAllByMovieId(Long movieId) {
		return repository.findAllByMovieId(movieId);
	}
}
