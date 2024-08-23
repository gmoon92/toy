package com.gmoon.hibernateannotation.payments.product.domain;

import java.io.Serializable;

import org.hibernate.annotations.UuidGenerator;

import com.gmoon.hibernateannotation.payments.price.domain.Currency;
import com.gmoon.hibernateannotation.payments.price.domain.Price;
import com.gmoon.hibernateannotation.payments.product.domain.vo.ProductType;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Inheritance(strategy = InheritanceType.JOINED) // 상속 구현 전략
@DiscriminatorColumn(name = "discriminator_type")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public abstract class Product implements Serializable {

	@Id
	@UuidGenerator
	@EqualsAndHashCode.Include
	private String id;

	@Column(name = "name")
	private String name;

	@Enumerated(EnumType.STRING)
	@Column(name = "type")
	private ProductType type;

	@OneToOne(mappedBy = "product", optional = false, cascade = CascadeType.ALL, orphanRemoval = true)
	protected Price price;

	protected Product(ProductType productType) {
		this.name = productType.name();
		this.type = productType;
	}

	protected Product withPrice(Double price, Currency currency) {
		this.price = Price.builder()
			 .product(this)
			 .price(price)
			 .currency(currency)
			 .build();
		return this;
	}
}
