package com.gmoon.springjpaspecs.books.bookstore.domain;

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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

@Table(name = "tb_bookstor_book")
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class BookStoreBook extends BaseEntity<String> {

	@Id
	@GeneratedValue(generator = "system-uuid")
	@GenericGenerator(name = "system-uuid", strategy = "uuid2")
	private String id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "book_store_id", referencedColumnName = "id", updatable = false)
	private BookStore bookStore;

	@Column(name = "book_id", nullable = false)
	private String bookId;

	@Enumerated(EnumType.STRING)
	@Column(name = "type", nullable = false)
	private BookType type;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false)
	private BookStatus status;

	@Embedded
	private BookQuantity quantity;

	@Builder
	private BookStoreBook(String bookId, BookStore bookStore, BookType type, BookStatus status, BookQuantity quantity) {
		this.bookId = bookId;
		this.bookStore = bookStore;
		this.type = type;
		this.status = status;
		this.quantity = quantity;
	}
}
