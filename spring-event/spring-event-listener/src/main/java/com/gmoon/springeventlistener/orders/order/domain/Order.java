package com.gmoon.springeventlistener.orders.order.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.UuidGenerator;

import com.gmoon.springeventlistener.orders.order.domain.vo.Product;
import com.gmoon.springeventlistener.orders.order.domain.vo.User;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
	@UuidGenerator
	@Column(length = 50)
	private String no;

	@Enumerated(value = EnumType.STRING)
	@Column(length = 10, nullable = false)
	@ColumnDefault("'WAITE'")
	private OrderStatus status;

	@Embedded
	@AttributeOverrides({
		 @AttributeOverride(name = "id", column = @Column(name = "user_id", length = 50, nullable = false)),
		 @AttributeOverride(name = "name", column = @Column(name = "user_name", nullable = false)),
		 @AttributeOverride(name = "email", column = @Column(name = "user_email", nullable = false))
	})
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
			 .map(Product::getNo)
			 .collect(Collectors.toList());
	}
}
