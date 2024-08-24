package com.gmoon.hibernateannotation.payments.price.domain;

import java.io.Serializable;

import com.gmoon.hibernateannotation.payments.product.domain.Product;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Price implements Serializable {

	@EqualsAndHashCode.Include
	@Id
	@Column
	private String productId;

	@ToString.Exclude
	@MapsId
	@OneToOne(optional = false)
	@JoinColumn(insertable = false, updatable = false)
	private Product product;

	@Column(name = "price")
	private Double price;

	@Enumerated(EnumType.STRING)
	@Column
	private Currency currency;

	@Builder
	private Price(Product product, Double price, Currency currency) {
		this.productId = product.getId();
		this.product = product;
		this.price = price;
		this.currency = currency;
	}
}
