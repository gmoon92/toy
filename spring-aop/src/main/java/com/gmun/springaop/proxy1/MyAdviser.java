package com.gmun.springaop.proxy1;

import java.lang.reflect.Method;

import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.Advisor;

public class MyAdviser implements Advisor{
private Advisor advisor;
	
	
	@Override
	public Advice getAdvice() {
		return advisor.getAdvice();
	}

	@Override
	public boolean isPerInstance() {
		return false;
	}
	
	
	private void setConfig(){
	}
	
	private class MyAdvice implements MethodInterceptor{
		@Override
		public Object invoke(MethodInvocation invocation) throws Throwable {
			Method method = invocation.getMethod();
			
			System.out.println("Advice Method Name is " + method.getName());
			
			return method.invoke(invocation.getClass(), invocation.getArguments());
		}
	}
}
