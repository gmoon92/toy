package com.gmoon.springaop.proxy2;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class UpperCaseHandler implements InvocationHandler {

	private final Object target; // 어떤 종류의 인터페이스를 구현한 타깃에도 적용 가능하도록 Object 타입으로 수정

	/***
	 * <pre>
	 * String ret = (String) method.invoke(
	 * 			target, // 실행할 타겟 오브젝트
	 * 			args // parameter
	 * 		);
	 *
	 * return ret.toUpperCase(); // 부가기능 제공
	 * </pre>
	 *
	 * {@code String ret = (String) method.invoke(target, args)}
	 *
	 * @param target 실행할 타겟 오브젝트
	 * @param args parameter
	 * @return 부가기능 제공
	 */
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		Object result = method.invoke(target, args);
		Class<?> returnType = method.getReturnType();

		String methodName = method.getName();
		log.info("method: {}, returnType: {}, result: {}", methodName, returnType, result);

		if (Void.TYPE.equals(returnType)) {
			// ignore
		} else if (target.equals(returnType)) { // self invoke
			return proxy;
		} else if (String.class.equals(returnType)) {
			boolean sayMethod = methodName.startsWith("say");
			if (sayMethod) {
				return ((String)result).toUpperCase(); // 부가기능(aspect) 제공
			}
		}
		return result;
	}
}
