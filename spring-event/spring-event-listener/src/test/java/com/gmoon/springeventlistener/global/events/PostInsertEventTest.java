package com.gmoon.springeventlistener.global.events;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.ResolvableType;

import com.gmoon.springeventlistener.orders.Fixtures;
import com.gmoon.springeventlistener.orders.cart.domain.Cart;
import com.gmoon.springeventlistener.orders.cart.domain.vo.ProductNo;
import com.gmoon.springeventlistener.orders.cart.domain.vo.UserId;

class PostInsertEventTest {

	@DisplayName("제네릭 타입 캡슐화")
	@Test
	void resolvableType() {
		UserId userId = new UserId(Fixtures.USER_ID);
		ProductNo productNo = new ProductNo(Fixtures.PRODUCT_NO0);
		Cart cart = new Cart(userId, productNo);
		PostInsertEvent<Cart> persistence = new PostInsertEvent<>(cart);

		ResolvableType type = persistence.getEntityType();

		assertThat(type.resolve()).isInstanceOf(Cart.class.getClass());
	}
}
