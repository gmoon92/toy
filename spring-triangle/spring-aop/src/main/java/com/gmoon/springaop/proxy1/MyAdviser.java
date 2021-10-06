package com.gmoon.springaop.proxy1;

import java.lang.reflect.Method;

import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.Advisor;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MyAdviser implements Advisor {
	private static final String INVOKE_METHOD_NAME_INFO_FORMAT = "Advice Method Name is {}";

	private Advisor advisor;

	@Override
	public Advice getAdvice() {
		return advisor.getAdvice();
	}

	@Override
	public boolean isPerInstance() {
		return false;
	}

	private class MyAdvice implements MethodInterceptor {
		@Override
		public Object invoke(MethodInvocation invocation) throws Throwable {
			Method method = invocation.getMethod();
			String methodName = method.getName();
			log.info(INVOKE_METHOD_NAME_INFO_FORMAT, methodName);
			return method.invoke(invocation.getClass(), invocation.getArguments());
		}
	}
}
