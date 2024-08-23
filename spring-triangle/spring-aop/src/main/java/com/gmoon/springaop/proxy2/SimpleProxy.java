package com.gmoon.springaop.proxy2;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SimpleProxy {
	private final Class<?> serviceInterface;    // 구현할 인터페이스
	private final InvocationHandler handler;    // invocationHandler를 구현한 클래스

	public Object getTarget() {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		return Proxy.newProxyInstance(
			 classLoader, // 동적으로 생성되는 다이내믹 프로식 클래스의 로딩에 사용할 클래스 로더
			 new Class[] {serviceInterface}, // 구현할 인터페이스
			 handler // invocationHandler를 구현한 클래스
		);
	}
}
