package com.gmoon.springjpaspecs.books.book.domain.vo;

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
public class BookName extends ValueObject {

	@EqualsAndHashCode.Include
	@Column(name = "name")
	private String value;

	public BookName(String value) {
		validate(value);
		this.value = value;
	}

	private void validate(String value) {
		if (Objects.isNull(value) || value.isEmpty()) {
			throw new IllegalArgumentException();
		}
	}
}
