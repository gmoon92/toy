package com.gmoon.springeventlistener.orders.cart.domain;

import java.io.Serializable;

import org.hibernate.annotations.UuidGenerator;

import com.gmoon.springeventlistener.orders.cart.domain.vo.ProductNo;
import com.gmoon.springeventlistener.orders.cart.domain.vo.UserId;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Cart implements Serializable {

	@Id
	@UuidGenerator
	@Column(length = 50)
	private String id;

	@Embedded
	@AttributeOverride(name = "value", column = @Column(name = "user_id", length = 50, nullable = false))
	private UserId userId;

	@Embedded
	@AttributeOverride(name = "value", column = @Column(name = "product_no", length = 50, nullable = false))
	private ProductNo productNo;

	public Cart(UserId userId, ProductNo productNo) {
		this.userId = userId;
		this.productNo = productNo;
	}
}
