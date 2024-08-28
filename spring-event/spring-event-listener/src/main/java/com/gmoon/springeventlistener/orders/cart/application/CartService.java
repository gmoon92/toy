package com.gmoon.springeventlistener.orders.cart.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gmoon.springeventlistener.orders.cart.domain.JpaCartRepository;
import com.gmoon.springeventlistener.orders.cart.domain.vo.ProductNo;
import com.gmoon.springeventlistener.orders.cart.domain.vo.UserId;

import lombok.RequiredArgsConstructor;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class CartService {

	private final JpaCartRepository cartRepository;

	@Transactional
	public void remove(String userId, String productNo) {
		cartRepository.deleteAllByUserIdAndProductNo(
			 new UserId(userId),
			 new ProductNo(productNo)
		);
	}
}
