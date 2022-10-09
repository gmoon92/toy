package com.gmoon.springeventlistener.orders.order.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.GenericGenerator;

import com.gmoon.springeventlistener.orders.order.domain.vo.Product;

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

	@Embedded
	private Product product;

	@Column(name = "quantity", nullable = false)
	@ColumnDefault("0")
	private long quantity;


	@Builder
	private OrderLineItem(Order order, Product product, long quantity) {
		this.order = order;
		this.product = product;
		this.quantity = quantity;
	}

	protected void setOrder(Order order) {
		this.order = order;
	}

	public String getProductName() {
		return product.getName();
	}

	public long getPrice() {
		return quantity * product.getPrice();
	}
}
