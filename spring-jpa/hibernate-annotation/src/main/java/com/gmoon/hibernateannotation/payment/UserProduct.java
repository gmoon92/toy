package com.gmoon.hibernateannotation.payment;

import com.gmoon.hibernateannotation.payment.constants.ProductDiscriminantType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue(ProductDiscriminantType.Value.USER)
@PrimaryKeyJoinColumn(name = "user_product_id")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class UserProduct extends Product {

	@Column
	private String gender;
}
