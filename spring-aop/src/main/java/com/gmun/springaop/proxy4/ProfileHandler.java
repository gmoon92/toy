package com.gmun.springaop.proxy4;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/***
 * target은 실제 Core Cocerns 객체 즉 Aspect 기능이 부착될 객체
 * 
 */
public class ProfileHandler implements InvocationHandler {

	private Object target;
	private String pattern;
	private Profile profile;

	public void setTarget(Object target) {
		this.target = target;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	public void setProfile(Profile profile) {
		this.profile = profile;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if (method.getName().startsWith(pattern)) {
			return invokeInProfile(method, args);

		} else {
			return method.invoke(target, args);
		}
	}

	// 메소드 부가기능 적용
	public Object invokeInProfile(Method method, Object[] args) throws Throwable {
		try {
			profile.getProcessInfo(target, method, args);
			profile.startTime();
			Object ret = method.invoke(target, args);
			profile.endTime();
			return ret;
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
			throw new Exception();
		}
	}

}
