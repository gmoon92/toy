package com.gmoon.hibernateannotation.payment;

import com.gmoon.hibernateannotation.payment.constants.Currency;
import com.gmoon.hibernateannotation.payment.constants.ProductDiscriminantType;
import com.gmoon.hibernateannotation.payment.constants.ProductType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue(ProductDiscriminantType.Value.COMPANY)
@PrimaryKeyJoinColumn(name = "company_product_id")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class CompanyProduct extends Product {

	public CompanyProduct(ProductType productType) {
		super(productType);
	}

	public static CompanyProduct create(ProductType productType, Double price, Currency currency) {
		return (CompanyProduct) new CompanyProduct(productType)
			.withPrice(price, currency);
	}
}