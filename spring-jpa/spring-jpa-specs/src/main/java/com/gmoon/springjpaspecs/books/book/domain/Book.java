package com.gmoon.springjpaspecs.books.book.domain;

import static com.gmoon.javacore.util.StringUtils.*;

import java.time.LocalDateTime;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.gmoon.springjpaspecs.books.book.domain.vo.BookName;
import com.gmoon.springjpaspecs.books.book.domain.vo.BookPrice;
import com.gmoon.springjpaspecs.global.domain.EntityObject;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Table(name = "tb_book")
@Entity
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Book extends EntityObject<String> {

	@Id
	@Column(name = "id")
	private String id;

	@EqualsAndHashCode.Include
	@Column(name = "isbn", nullable = false, length = 16)
	private String isbn;

	@Embedded
	private BookName name;

	@Embedded
	private BookPrice price;

	@Column(name = "publication_date")
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
