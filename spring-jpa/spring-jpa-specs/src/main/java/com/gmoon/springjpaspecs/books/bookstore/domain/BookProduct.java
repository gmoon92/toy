package com.gmoon.springjpaspecs.books.bookstore.domain;

import com.gmoon.springjpaspecs.global.BaseEntity;
import java.util.UUID;
import javax.persistence.Entity;
import javax.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class BookProduct extends BaseEntity<UUID> {

	@Id
	private UUID id;
}
