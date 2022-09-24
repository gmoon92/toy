package com.gmoon.springjpaspecs.book.domain;

import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
@Getter
public class BookName implements Serializable {

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
