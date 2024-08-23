package com.gmoon.springjpaspecs.books.book.domain;

import static com.gmoon.javacore.util.StringUtils.*;

import java.time.LocalDateTime;
import java.util.UUID;

import com.gmoon.springjpaspecs.books.book.domain.vo.BookName;
import com.gmoon.springjpaspecs.books.book.domain.vo.BookPrice;
import com.gmoon.springjpaspecs.global.domain.EntityObject;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Entity
@Table(name = "tb_book")
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Book extends EntityObject<String> {

	@Id
	@Column
	private String id;

	@EqualsAndHashCode.Include
	@Column(nullable = false, length = 16)
	private String isbn;

	@Embedded
	private BookName name;

	@Embedded
	private BookPrice price;

	@Column
	private LocalDateTime publicationDate;

	protected Book() {
		id = UUID.randomUUID().toString();
		isbn = randomAlphabetic(16);
	}

	@Builder
	private Book(BookName name, BookPrice price, LocalDateTime publicationDate) {
		this();
		this.name = name;
		this.price = price;
		this.publicationDate = publicationDate;
	}
}
