package com.gmoon.springaop.proxy4;

import java.lang.reflect.Proxy;

import org.springframework.beans.factory.FactoryBean;

public class ProfileFactoryBean implements FactoryBean<Object> { // 생성할 오브젝트 타입을 지정할 수도 있지만 범용적으로 사용하기 위해 Object로 정의

	private ProfileHandler profileHandler;
	private Object target;
	private Profile profile;
	private String pattern; // 메소드 패턴
	private Class<?> serviceInterface;  // 다이나믹 프록시를 생성할 때 필요하다. UserService 외의 인터페이스를 가진 타깃에도 적용

	public void setTarget(Object target) {
		this.target = target;
	}

	public void setProfile(Profile profile) {
		this.profile = profile;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	public void setServiceInterface(Class<?> serviceInterface) {
		this.serviceInterface = serviceInterface;
	}

	/***
	 * FactoryBean 인터페이스 구현 메소드
	 */
	@Override
	public Object getObject() throws Exception {
		try {
			profileHandler = new ProfileHandler();
			profileHandler.setTarget(target);
			profileHandler.setProfile(profile);
			profileHandler.setPattern(pattern);
			return Proxy.newProxyInstance(getClass().getClassLoader()
				 , new Class[] {serviceInterface}
				 , profileHandler);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/***
	 * 팩토리 빈이 생성하는 오브젝트의 타입은 DI 받은 인터페이스 타입에 따라 달라진다.
	 * 따라서 다양한 타입의 프록시 오브젝트 생성에 재사용 할 수 있다.
	 */
	@Override
	public Class<?> getObjectType() {
		return serviceInterface;
	}

	/***
	 * 싱글톤 빈이 아니라는 뜻이 아니라 getObject()가
	 * 매번 같은 오브젝트를 리턴하지 않는다는 의미
	 */
	public boolean isSingleton() {
		return false;
	}

}
