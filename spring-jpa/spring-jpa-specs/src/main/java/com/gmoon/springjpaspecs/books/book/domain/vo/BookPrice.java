package com.gmoon.springjpaspecs.books.book.domain.vo;

import java.math.BigDecimal;
import java.util.Objects;

import com.gmoon.springjpaspecs.global.domain.ValueObject;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class BookPrice extends ValueObject {

	@EqualsAndHashCode.Include
	@Column(name = "price")
	private BigDecimal value;

	public BookPrice(BigDecimal value) {
		validate(value);
		this.value = value;
	}

	private void validate(BigDecimal value) {
		if (Objects.isNull(value)) {
			throw new IllegalArgumentException();
		}

		boolean isLessThenZero = value.compareTo(BigDecimal.ZERO) == -1;
		if (isLessThenZero) {
			throw new IllegalArgumentException();
		}
	}
}
