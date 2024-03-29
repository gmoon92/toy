package com.gmoon.springjpaspecs.books.bookstore.domain.vo;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.hibernate.annotations.ColumnDefault;

import com.gmoon.springjpaspecs.global.domain.ValueObject;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class BookQuantity extends ValueObject {

	@EqualsAndHashCode.Include
	@Column(name = "quantity")
	@ColumnDefault("0")
	private Integer value;

	public BookQuantity(Integer value) {
		validate(value);
		this.value = value;
	}

	private void validate(Integer quantity) {
		if (Objects.isNull(quantity) || quantity < 0) {
			throw new IllegalArgumentException();
		}
	}

	public boolean grateThanZero() {
		return value > 0;
	}
}
