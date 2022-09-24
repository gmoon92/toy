package com.gmoon.springjpaspecs.books.domain;

import static com.gmoon.javacore.util.StringUtils.randomAlphabetic;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Embeddable
@Getter
@EqualsAndHashCode
public class IStandardBookNumber implements Serializable {

	@Column(name = "isbn")
	private String value;

	protected IStandardBookNumber() {
		value = randomAlphabetic(16);
	}
}
