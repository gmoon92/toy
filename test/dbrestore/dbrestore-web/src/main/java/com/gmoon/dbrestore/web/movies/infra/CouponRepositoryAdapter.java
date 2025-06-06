package com.gmoon.dbrestore.web.movies.infra;

import com.gmoon.dbrestore.web.movies.domain.Coupon;
import com.gmoon.dbrestore.web.movies.domain.CouponRepository;
import jakarta.persistence.EntityNotFoundException;
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

	@Override
	public Coupon get(Long couponId) {
		return repository.findById(couponId)
			 .orElseThrow(() -> new EntityNotFoundException("Coupon with id " + couponId + " not found"));
	}
}
