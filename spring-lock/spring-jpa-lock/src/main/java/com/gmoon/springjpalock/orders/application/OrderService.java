package com.gmoon.springjpalock.orders.application;

import java.util.List;

import javax.persistence.EntityNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.gmoon.springjpalock.menus.domain.Menu;
import com.gmoon.springjpalock.menus.domain.MenuRepository;
import com.gmoon.springjpalock.orders.domain.Order;
import com.gmoon.springjpalock.orders.domain.OrderLineItem;
import com.gmoon.springjpalock.orders.domain.OrderRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {

	private final OrderRepository orderRepository;
	private final MenuRepository menuRepository;

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Order ordering(Order param, String menuId) {
		Menu menu = menuRepository.findById(menuId)
			 .orElseThrow(EntityNotFoundException::new);

		// 주문 생성
		List<OrderLineItem> orderLineItems = param.getOrderLineItems();
		for (OrderLineItem item : orderLineItems) {
			item.setMenu(menu);
		}

		// 2. 메뉴 재고 감소
		menu.decreaseQuantity();
		return orderRepository.saveAndFlush(param);
	}
}
