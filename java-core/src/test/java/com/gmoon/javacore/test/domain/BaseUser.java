package com.gmoon.javacore.test.domain;

import java.util.UUID;

import com.gmoon.javacore.persistence.Id;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public abstract class BaseUser {

	@Id
	@EqualsAndHashCode.Include
	protected UUID id;
}
