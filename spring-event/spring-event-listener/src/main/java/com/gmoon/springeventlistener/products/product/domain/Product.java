package com.gmoon.springeventlistener.products.product.domain;

import java.io.Serializable;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Product implements Serializable {

	@Id
	@UuidGenerator
	@Column(length = 50)
	private String no;

	@Column(nullable = false)
	private String name;

	@ColumnDefault("0")
	@Column(nullable = false)
	private long price;
}
