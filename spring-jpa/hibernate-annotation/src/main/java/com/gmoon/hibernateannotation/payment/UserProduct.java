package com.gmoon.hibernateannotation.payment;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@PrimaryKeyJoinColumn(name = "user_product_id")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class UserProduct extends Product {

	@Column(nullable = false)
	private String gender;
}
