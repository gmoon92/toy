package com.gmoon.springjpaspecs.books.bookstore.domain;

import org.hibernate.annotations.UuidGenerator;

import com.gmoon.springjpaspecs.books.bookstore.domain.vo.BookQuantity;
import com.gmoon.springjpaspecs.books.bookstore.domain.vo.BookStatus;
import com.gmoon.springjpaspecs.books.bookstore.domain.vo.BookType;
import com.gmoon.springjpaspecs.global.domain.AuditedEntityObject;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "tb_bookstor_book")
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class BookStoreBook extends AuditedEntityObject<String> {

	@Id
	@UuidGenerator
	private String id;

	@Column(name = "book_id", nullable = false)
	private String bookId;

	@Column(name = "book_name")
	private String bookName;

	@Enumerated(EnumType.STRING)
	@Column(name = "type", nullable = false)
	private BookType type;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false)
	private BookStatus status;

	@Embedded
	private BookQuantity quantity;

	@Builder
	private BookStoreBook(String bookId, String bookName, BookType type, BookStatus status, BookQuantity quantity) {
		this.bookId = bookId;
		this.bookName = bookName;
		this.type = type;
		this.status = status;
		this.quantity = quantity;
	}

	public boolean isDisplay() {
		return BookStatus.DISPLAY == status
			 && availableSale();
	}

	public boolean availableSale() {
		return quantity.grateThanZero();
	}
}
