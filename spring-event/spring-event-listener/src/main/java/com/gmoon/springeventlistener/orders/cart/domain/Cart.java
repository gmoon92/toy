package com.gmoon.springeventlistener.orders.cart.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.hibernate.annotations.GenericGenerator;

import com.gmoon.springeventlistener.orders.cart.domain.vo.ProductNo;
import com.gmoon.springeventlistener.orders.cart.domain.vo.UserId;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Cart implements Serializable {

	@Id
	@GeneratedValue(generator = "system-uuid")
	@GenericGenerator(name = "system-uuid", strategy = "uuid2")
	@Column(length = 50)
	private String id;

	@Embedded
	private UserId userId;

	@Embedded
	private ProductNo productNo;

	public Cart(UserId userId, ProductNo productNo) {
		this.userId = userId;
		this.productNo = productNo;
	}
}
