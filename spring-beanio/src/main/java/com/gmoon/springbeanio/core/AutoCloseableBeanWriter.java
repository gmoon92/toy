package com.gmoon.springbeanio.core;

import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import org.beanio.BeanWriter;

@RequiredArgsConstructor
public class AutoCloseableBeanWriter implements BeanWriter, AutoCloseable {

	@Delegate
	private final BeanWriter delegate;
}
