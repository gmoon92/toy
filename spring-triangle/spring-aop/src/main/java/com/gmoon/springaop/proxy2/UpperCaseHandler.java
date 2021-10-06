package com.gmoon.springaop.proxy2;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class UpperCaseHandler implements InvocationHandler {
	//private Hello target;
	private Object target; // 어떤 종류의 인터페이스를 구현한 타깃에도 적용 가능하도록 Object 타입으로 수정

	public UpperCaseHandler(Object target) {
		this.target = target;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		/***
		 String ret = (String) method.invoke(target // 실행할 타겟 오브젝트
		 , args // parameter
		 );
		 return ret.toUpperCase(); // 부가기능 제공
		 */
		Object ret = method.invoke(target, args);

		if (ret instanceof String // 메소드 리턴 타입이 String인 경우만 대문자 변경
				&& method.getName().startsWith("say")) { // 메소드 이름이 say로 시작될 경우만
			return ((String)ret).toUpperCase(); // 부가기능 제공
		}

		return ret;
	}
}
