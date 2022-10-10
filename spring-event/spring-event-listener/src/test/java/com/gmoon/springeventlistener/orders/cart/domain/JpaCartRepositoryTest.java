package com.gmoon.springeventlistener.orders.cart.domain;

import static com.gmoon.springeventlistener.orders.Fixtures.*;
import static org.assertj.core.api.AssertionsForClassTypes.*;

import java.util.Optional;
import java.util.UUID;

import javax.persistence.EntityNotFoundException;

import org.junit.jupiter.api.Test;
import org.quickperf.sql.annotation.ExpectDelete;
import org.quickperf.sql.annotation.ExpectInsert;
import org.quickperf.sql.annotation.ExpectSelect;
import org.springframework.beans.factory.annotation.Autowired;

import com.gmoon.springeventlistener.global.SupportDataJpaTest;
import com.gmoon.springeventlistener.orders.cart.domain.vo.ProductNo;
import com.gmoon.springeventlistener.orders.cart.domain.vo.UserId;

class JpaCartRepositoryTest extends SupportDataJpaTest {

	@Autowired
	private JpaCartRepository repository;

	@ExpectInsert
	@Test
	void save() {
		UserId userId = userId(UUID.randomUUID().toString());
		ProductNo productNo = productNo(UUID.randomUUID().toString());
		Cart cart = new Cart(userId, productNo);

		Cart savedCart = repository.save(cart);
		flushAndClear();

		assertThat(savedCart.getId()).isNotBlank();
	}

	private UserId userId(String value) {
		return new UserId(value);
	}

	private ProductNo productNo(String value) {
		return new ProductNo(value);
	}

	@ExpectSelect
	@Test
	void findCartByUserIdAndProductNo() {
		UserId userId = userId(USER_ID);
		ProductNo productNo = productNo(PRODUCT_NO0);

		Optional<Cart> cart = repository.findCartByUserIdAndProductNo(userId, productNo);

		assertThatCode(() -> cart.orElseThrow(EntityNotFoundException::new))
			.doesNotThrowAnyException();
	}

	@ExpectDelete
	@Test
	void deleteAllByUserIdAndProductNo() {
		UserId userId = userId(USER_ID);
		ProductNo productNo = productNo(PRODUCT_NO0);

		repository.deleteAllByUserIdAndProductNo(userId, productNo);

		assertThat(repository.findCartByUserIdAndProductNo(userId, productNo).isPresent())
			.isFalse();
	}
}
