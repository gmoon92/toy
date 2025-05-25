package com.gmoon.springbeanio.core;

import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import org.beanio.BeanReader;

@RequiredArgsConstructor
public class AutoCloseableBeanReader implements BeanReader, AutoCloseable {

	@Delegate
	private final BeanReader delegate;
}
