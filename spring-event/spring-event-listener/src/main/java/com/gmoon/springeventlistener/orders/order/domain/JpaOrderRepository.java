package com.gmoon.springeventlistener.orders.order.domain;

import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaOrderRepository extends JpaRepository<Order, String> {
}
