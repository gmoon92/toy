package com.gmoon.hibernateannotation.payment;

import com.gmoon.hibernateannotation.payment.constants.Currency;
import com.gmoon.hibernateannotation.payment.constants.ProductType;
import java.io.Serializable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Inheritance(strategy = InheritanceType.JOINED) // 상속 구현 전략
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public abstract class Product implements Serializable {

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
