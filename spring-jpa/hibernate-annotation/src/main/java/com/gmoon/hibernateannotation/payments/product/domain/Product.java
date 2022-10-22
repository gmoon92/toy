package com.gmoon.hibernateannotation.payments.product.domain;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToOne;

import org.hibernate.annotations.GenericGenerator;

import com.gmoon.hibernateannotation.payments.price.domain.Currency;
import com.gmoon.hibernateannotation.payments.price.domain.Price;
import com.gmoon.hibernateannotation.payments.product.domain.vo.ProductType;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE) // 상속 구현 전략
@DiscriminatorColumn(name = "discriminator_type")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public abstract class Product implements Serializable {

	@EqualsAndHashCode.Include
	@Id
	@GeneratedValue(generator = "system-uuid")
	@GenericGenerator(name = "system-uuid", strategy = "uuid2")
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
