package com.gmoon.springjpaspecs.books.bookstore.domain;

import com.gmoon.springjpaspecs.books.bookstore.domain.vo.BookQuantity;
import com.gmoon.springjpaspecs.books.bookstore.domain.vo.BookStatus;
import com.gmoon.springjpaspecs.books.bookstore.domain.vo.BookType;
import com.gmoon.springjpaspecs.global.vo.BaseEntity;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

@Table(name = "tb_bookstor")
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class BookStore extends BaseEntity {

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
//		nullable = false,
//		columnDefinition = "binary(16)",
//		foreignKey = @ForeignKey(name = "fk_menu_product_to_menu"
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
