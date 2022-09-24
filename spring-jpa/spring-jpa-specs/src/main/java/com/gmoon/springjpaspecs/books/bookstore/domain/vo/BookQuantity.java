package com.gmoon.springjpaspecs.books.bookstore.domain.vo;

import com.gmoon.springjpaspecs.global.vo.BaseValueObject;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode
public class BookQuantity extends BaseValueObject {

	@Column(name = "quantity")
	@ColumnDefault("0")
	private Integer value;

	public BookQuantity(Integer value) {
		validate(value);
		this.value = value;
	}

	private void validate(Integer quantity) {
		if (Objects.isNull(quantity) || quantity < 0) {
			throw new IllegalArgumentException();
		}
	}
}
