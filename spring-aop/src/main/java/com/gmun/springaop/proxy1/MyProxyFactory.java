package com.gmun.springaop.proxy1;

import org.springframework.aop.framework.ProxyFactory;

public class MyProxyFactory {
	private ProxyFactory proxyFactory;
	private Object target;
	
	public MyProxyFactory(){
		proxyFactory = new ProxyFactory();
		
	}
	
	public Object getProxyInstance(){
		/*
		proxyFactory.addAdvisor(advisor);
		proxyFactory.setTarget(target);
		*/
		return proxyFactory.getProxy();
	}
	
	public void setTarget(Object target){
		proxyFactory.setTarget(target);
	}
}
