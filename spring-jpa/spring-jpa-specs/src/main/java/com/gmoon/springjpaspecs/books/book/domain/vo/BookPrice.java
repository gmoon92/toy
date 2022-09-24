package com.gmoon.springjpaspecs.books.book.domain.vo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode
public class BookPrice implements Serializable {

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
