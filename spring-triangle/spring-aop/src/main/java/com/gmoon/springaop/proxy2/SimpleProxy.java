package com.gmoon.springaop.proxy2;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

public class SimpleProxy {
	private Class<?> serviceInterface;    // 구현할 인터페이스
	private InvocationHandler handler;    // invocationHandler를 구현한 클래스

	public SimpleProxy(Class<?> serviceInterface, InvocationHandler handler) {
		this.serviceInterface = serviceInterface;
		this.handler = handler;
	}

	public Object getTarget() {
		return Proxy.newProxyInstance(getClass().getClassLoader(), // 동적으로 생성되는 다이내믹 프로식 클래스의 로딩에 사용할 클래스 로더
			new Class[] {serviceInterface}, // 구현할 인터페이스
			handler // invocationHandler를 구현한 클래스
		);
	}
}
