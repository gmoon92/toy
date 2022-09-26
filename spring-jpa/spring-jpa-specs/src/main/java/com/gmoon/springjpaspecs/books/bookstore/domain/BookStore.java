package com.gmoon.springjpaspecs.books.bookstore.domain;

import com.gmoon.springjpaspecs.global.vo.BaseEntity;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "tb_bookstor")
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class BookStore extends BaseEntity {

	@Id
	private String id;

	private String name;

}
