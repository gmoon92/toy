package com.gmoon.springeventlistener.orders.order.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.GenericGenerator;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class OrderLineItem implements Serializable {

	@Id
	@GeneratedValue(generator = "system-uuid")
	@GenericGenerator(name = "system-uuid", strategy = "uuid2")
	@Column(length = 50)
	private String id;

	@ManyToOne(optional = false)
	@JoinColumn(
		name = "order_no",
		foreignKey = @ForeignKey(name = "fk_order_to_order_lien_item"),
		updatable = false
	)
	private Order order;

	@Column(name = "product_name", length = 50, nullable = false)
	private String productName;

	@Column(name = "quantity", nullable = false)
	@ColumnDefault("0")
	private long quantity;

	@Column(name = "price", nullable = false)
	@ColumnDefault("0")
	private long price;

	@Builder
	private OrderLineItem(Order order, String productName, long quantity, long price) {
		this.order = order;
		this.productName = productName;
		this.quantity = quantity;
		this.price = price;
	}

	protected void setOrder(Order order) {
		this.order = order;
	}

	public long getTotalPrice() {
		return quantity * price;
	}
}
