package com.gmoon.hibernateannotation.payment;

import com.gmoon.hibernateannotation.payment.constants.Currency;
import com.gmoon.hibernateannotation.payment.constants.ProductType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("U")
@PrimaryKeyJoinColumn(name = "user_product_id")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class UserProduct extends Product {

	@Column
	private String gender;

	public UserProduct(ProductType productType) {
		super(productType);
	}

	public static UserProduct create(ProductType productType, Double price, Currency currency) {
		return (UserProduct) new UserProduct(productType)
			.withPrice(price, currency);
	}
}
