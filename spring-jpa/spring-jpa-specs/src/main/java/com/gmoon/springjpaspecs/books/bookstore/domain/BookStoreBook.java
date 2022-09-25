package com.gmoon.springjpaspecs.books.bookstore.domain;

import com.gmoon.springjpaspecs.books.bookstore.domain.vo.BookId;
import com.gmoon.springjpaspecs.books.bookstore.domain.vo.BookQuantity;
import com.gmoon.springjpaspecs.books.bookstore.domain.vo.BookStatus;
import com.gmoon.springjpaspecs.books.bookstore.domain.vo.BookType;
import com.gmoon.springjpaspecs.global.vo.BaseEntity;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class BookStoreBook extends BaseEntity<Long> {

	@Id
	@GeneratedValue
	private Long id;

	@Embedded
	private BookId bookId;

	@Enumerated(EnumType.STRING)
	@Column(name = "type")
	private BookType type;

	@Enumerated(EnumType.STRING)
	@Column(name = "status")
	private BookStatus status;

	@Embedded
	private BookQuantity quantity;

	public BookStoreBook(BookId bookId, BookQuantity quantity) {
		this.bookId = bookId;
		this.quantity = quantity;
	}
}
