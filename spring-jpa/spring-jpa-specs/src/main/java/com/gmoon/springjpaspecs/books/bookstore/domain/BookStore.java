package com.gmoon.springjpaspecs.books.bookstore.domain;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.GenericGenerator;

import com.gmoon.springjpaspecs.books.bookstore.domain.vo.BookQuantity;
import com.gmoon.springjpaspecs.books.bookstore.domain.vo.BookStatus;
import com.gmoon.springjpaspecs.books.bookstore.domain.vo.BookType;
import com.gmoon.springjpaspecs.global.domain.EntityObject;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tb_bookstor")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class BookStore extends EntityObject {

	@Id
	@GeneratedValue(generator = "system-uuid")
	@GenericGenerator(name = "system-uuid", strategy = "uuid2")
	private String id;

	@Column(name = "name", nullable = false)
	private String name;

	@OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
	@JoinColumn(
		 name = "book_store_id",
		 nullable = false
	)
	private List<BookStoreBook> storedBooks = new ArrayList<>();

	public BookStore(String name) {
		this.name = name;
	}

	public void addBook(String bookId, BookQuantity quantity, BookType etc) {
		BookStoreBook storeBook = BookStoreBook.builder()
			 .bookId(bookId)
			 .quantity(quantity)
			 .type(etc)
			 .status(BookStatus.DISPLAY)
			 .build();

		storedBooks.add(storeBook);
	}
}
