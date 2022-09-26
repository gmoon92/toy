package com.gmoon.springjpaspecs.books.bookstore.domain.vo;

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
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class BookId implements Serializable {

	@EqualsAndHashCode.Include
	@Column(name = "book_id")
	private String value;

	public BookId(String value) {
		validate(value);
		this.value = value;
	}

	private void validate(String bookId) {
		if (Objects.isNull(bookId) || bookId.isEmpty())
			throw new IllegalArgumentException();
	}
}
