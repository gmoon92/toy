package com.gmoon.springjpaspecs.books.bookstore.domain.vo;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;
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
public class BookId implements Serializable {

	@Column(name = "book_id")
	private UUID value;

	public BookId(UUID value) {
		validate(value);
		this.value = value;
	}

	private void validate(UUID bookId) {
		if (Objects.isNull(bookId))
			throw new IllegalArgumentException();
	}
}
