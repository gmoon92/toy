package com.gmoon.springeventlistener.orders.order.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.gmoon.springeventlistener.orders.order.domain.vo.Product;
import com.gmoon.springeventlistener.orders.order.domain.vo.User;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Order implements Serializable {

	@Id
	@GeneratedValue(generator = "system-uuid")
	@GenericGenerator(name = "system-uuid", strategy = "uuid2")
	@Column(name = "order_no", length = 50)
	private String id;

	@Enumerated(value = EnumType.STRING)
	@Column(name = "status", length = 10, nullable = false)
	@ColumnDefault("'WAITE'")
	private OrderStatus status;

	@Embedded
	private User user;

	@OneToMany(
		 mappedBy = "order",
		 cascade = {CascadeType.PERSIST, CascadeType.MERGE},
		 orphanRemoval = true
	)
	@OnDelete(action = OnDeleteAction.CASCADE)
	private List<OrderLineItem> lines = new ArrayList<>();

	public Order(OrderStatus status, User user) {
		this.status = status;
		this.user = user;
	}

	public Order addOrderItems(OrderLineItem... items) {
		for (OrderLineItem item : items) {
			lines.add(item);
			item.setOrder(this);
		}
		return this;
	}

	public void complete() {
		if (status != OrderStatus.ACCEPTED) {
			throw new IllegalStateException();
		}

		status = OrderStatus.COMPLETED;
	}

	public long totalPrice() {
		return lines.stream()
			 .mapToLong(OrderLineItem::getPrice)
			 .sum();
	}

	public String getProductNames() {
		return lines.stream()
			 .map(OrderLineItem::getProductName)
			 .collect(Collectors.joining());
	}

	public List<String> getProductNos() {
		return lines.stream()
			 .map(OrderLineItem::getProduct)
			 .map(Product::getId)
			 .collect(Collectors.toList());
	}
}
