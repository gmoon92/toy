package com.gmoon.springeventlistener.global.events;

import static org.assertj.core.api.Assertions.*;

import java.time.Duration;
import java.util.UUID;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Example;

import com.gmoon.springeventlistener.orders.cart.domain.Cart;
import com.gmoon.springeventlistener.orders.cart.domain.JpaCartRepository;
import com.gmoon.springeventlistener.orders.cart.domain.vo.ProductNo;
import com.gmoon.springeventlistener.orders.cart.domain.vo.UserId;

@SpringBootTest
class PersistenceHandlerTest {

	@Autowired
	private ApplicationEventPublisher publisher;

	@Autowired
	private JpaCartRepository repository;

	@Test
	void save() {
		UserId userId = new UserId(UUID.randomUUID().toString());
		ProductNo productNo = new ProductNo(UUID.randomUUID().toString());
		Cart cart = new Cart(userId, productNo);

		publisher.publishEvent(new PostInsertEvent<>(cart));
		Awaitility.await()
			.pollDelay(Duration.ofSeconds(1))
			.atMost(Duration.ofSeconds(3))
			.untilAsserted(() -> assertThat(repository.exists(Example.of(cart)))
				.isTrue());
	}
}
