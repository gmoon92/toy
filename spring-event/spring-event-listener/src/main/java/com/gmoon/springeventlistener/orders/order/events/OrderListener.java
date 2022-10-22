package com.gmoon.springeventlistener.orders.order.events;

import javax.persistence.EntityNotFoundException;

import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.gmoon.springeventlistener.orders.cart.application.CartService;
import com.gmoon.springeventlistener.users.user.domain.JpaUserRepository;
import com.gmoon.springeventlistener.users.user.domain.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Async
@Component
@RequiredArgsConstructor
public class OrderListener {

	private final CartService cartService;
	private final JpaUserRepository userRepository;

	@Order(Ordered.LOWEST_PRECEDENCE)
	@EventListener(condition = "#event.orderNo != null && !#event.orderNo.isEmpty()")
	public void syncOrderLines(CompletedOrderEvent event) {
		log.info("Handling synchronized order lines ... {}", event);
	}

	@Order(50)
	@TransactionalEventListener(
		condition = "#event.orderNo != null && !#event.orderNo.isEmpty()",
		phase = TransactionPhase.AFTER_COMMIT,
		fallbackExecution = true
	)
	public void removeOrderedCart(CompletedOrderEvent event) {
		for (String productNo : event.getProductNos()) {
			String userName = event.getUserName();
			String userEmail = event.getUserEmail();
			User user = userRepository.findUserByNameAndEmail(userName, userEmail)
				.orElseThrow(EntityNotFoundException::new);

			String userId = user.getId();
			log.info("remove all ordered cart product: {}, user: {}", productNo, userId);
			cartService.remove(userId, productNo);
		}
	}

	@Order(Ordered.HIGHEST_PRECEDENCE)
	@Async
	@EventListener(condition = "#event.orderNo != null && !#event.orderNo.isEmpty()")
	public void sendMessage(CompletedOrderEvent event) {
		log.info("Handling send message... {}", event);
	}
}
