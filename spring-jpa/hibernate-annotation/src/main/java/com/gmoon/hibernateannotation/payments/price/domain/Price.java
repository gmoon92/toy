package com.gmoon.hibernateannotation.payments.price.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;

import com.gmoon.hibernateannotation.payments.product.domain.Product;

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
@ToString
public class Price implements Serializable {

	@EqualsAndHashCode.Include
	@Id
	@Column(name = "id")
	private String productId;

	@ToString.Exclude
	@MapsId
	@OneToOne(optional = false)
	@JoinColumn(name = "id", referencedColumnName = "id", insertable = false, updatable = false)
	private Product product;

	@Column(name = "price")
	private Double price;

	@Enumerated(EnumType.STRING)
	@Column(name = "currency")
	private Currency currency;

	@Builder
	private Price(Product product, Double price, Currency currency) {
		this.productId = product.getId();
		this.product = product;
		this.price = price;
		this.currency = currency;
	}
}
