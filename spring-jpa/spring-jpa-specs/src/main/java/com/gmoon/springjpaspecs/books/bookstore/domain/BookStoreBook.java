package com.gmoon.springjpaspecs.books.bookstore.domain;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.gmoon.springjpaspecs.books.bookstore.domain.vo.BookQuantity;
import com.gmoon.springjpaspecs.books.bookstore.domain.vo.BookStatus;
import com.gmoon.springjpaspecs.books.bookstore.domain.vo.BookType;
import com.gmoon.springjpaspecs.global.vo.EntityObject;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@EntityListeners(AuditingEntityListener.class)
@Table(name = "tb_bookstor_book")
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class BookStoreBook extends EntityObject<String> {

	@Id
	@GeneratedValue(generator = "system-uuid")
	@GenericGenerator(name = "system-uuid", strategy = "uuid2")
	private String id;

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

	@CreatedDate
	@Column(name = "created_date")
	private LocalDateTime createdDate;

	@Builder
	private BookStoreBook(String bookId, BookType type, BookStatus status, BookQuantity quantity) {
		this.bookId = bookId;
		this.type = type;
		this.status = status;
		this.quantity = quantity;
	}
}
