package com.gmoon.springjpaspecs.books.book.domain.vo;

import static com.gmoon.javacore.util.StringUtils.randomAlphabetic;

import com.gmoon.springjpaspecs.global.vo.BaseValueObject;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Embeddable
@Getter
@EqualsAndHashCode
public class IStandardBookNumber extends BaseValueObject {

	@Column(name = "isbn")
	private String value;

	protected IStandardBookNumber() {
		value = randomAlphabetic(16);
	}
}
