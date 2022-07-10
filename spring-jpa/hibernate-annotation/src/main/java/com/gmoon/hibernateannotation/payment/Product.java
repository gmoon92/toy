package com.gmoon.hibernateannotation.payment;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;

import com.gmoon.hibernateannotation.payment.constants.Currency;
import com.gmoon.hibernateannotation.payment.constants.ProductType;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class Product implements Serializable {

	@EqualsAndHashCode.Include
	@Id
	@GeneratedValue
	private Long id;

	@Column(name = "name")
	private String name;

	@Enumerated(EnumType.STRING)
	@Column(name = "type")
	private ProductType type;

	@OneToOne(mappedBy = "product", optional = false, cascade = CascadeType.ALL, orphanRemoval = true)
	private Price price;

	private Product(ProductType productType) {
		this.name = productType.name();
		this.type = productType;
	}

	public static Product create(ProductType productType, Double price, Currency currency) {
		return new Product(productType)
			.withPrice(price, currency);
	}

	private Product withPrice(Double price, Currency currency) {
		this.price = Price.builder()
			.product(this)
			.price(price)
			.currency(currency)
			.build();
		return this;
	}
}
