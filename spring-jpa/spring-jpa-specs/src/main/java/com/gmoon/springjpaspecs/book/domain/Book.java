package com.gmoon.springjpaspecs.book.domain;

import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Entity
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Book {

	@Id
	@Column(name = "id")
	private UUID id;

	@Embedded
	private IStandardBookNumber isbn;

	@Embedded
	private BookName name;

	@Embedded
	private BookPrice price;

	protected Book() {
		id = UUID.randomUUID();
	}

	public static Book create(String name) {
		Book book = new Book();
		book.name = new BookName(name);
		return book;
	}
}
