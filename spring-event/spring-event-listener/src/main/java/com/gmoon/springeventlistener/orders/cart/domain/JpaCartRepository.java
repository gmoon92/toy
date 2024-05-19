package com.gmoon.springeventlistener.orders.cart.domain;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gmoon.springeventlistener.orders.cart.domain.vo.ProductNo;
import com.gmoon.springeventlistener.orders.cart.domain.vo.UserId;

public interface JpaCartRepository extends JpaRepository<Cart, String> {

	Optional<Cart> findCartByUserIdAndProductNo(UserId userId, ProductNo productNo);

	void deleteAllByUserIdAndProductNo(UserId userId, ProductNo productNo);
}
