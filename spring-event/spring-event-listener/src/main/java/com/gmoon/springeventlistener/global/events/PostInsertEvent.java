package com.gmoon.springeventlistener.global.events;

import org.springframework.context.ApplicationEvent;
import org.springframework.core.ResolvableType;
import org.springframework.core.ResolvableTypeProvider;

import lombok.Getter;

@Getter
public class PostInsertEvent<T> extends ApplicationEvent
	implements ResolvableTypeProvider {

	private final ResolvableType entityType;

	public PostInsertEvent(T source) {
		super(source);
		entityType = ResolvableType.forInstance(getSource());
	}

	@Override
	public ResolvableType getResolvableType() {
		return ResolvableType.forClassWithGenerics(
			getClass(),
			entityType);
	}
}
