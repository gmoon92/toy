package com.gmoon.hibernateenvers.global.domain;

import java.io.Serial;
import java.io.Serializable;

import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

@Getter
@MappedSuperclass
public abstract class BaseEntity implements Serializable {

	@Serial
	private static final long serialVersionUID = -1809210303733447907L;

}
