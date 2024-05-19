package com.gmoon.springaop.proxy1;

import java.lang.reflect.Method;

public class Reflection {
	public Integer stringLength(String str) throws Exception {
		Method method = String.class.getMethod("length");
		return (Integer)method.invoke(str);
	}

	public Character stringCharAt(String str, int idx) throws Exception {
		Method method = String.class.getMethod("charAt" // method name
			 , int.class // parameter type
		);
		return (Character)method.invoke(str, idx);
	}

	/****
	 *  Spring Bean은 Class 이름과 Property로 정의된다.
	 *  하지만 다이내믹 프록시 오브젝트는 일반적인 Spring의 Bean으로는 등록할 수 없다.
	 *
	 *  Spring은 Class 이름을 기준으로 Reflection을 활용하여 새로운 Object를 생성한다.
	 *
	 *  1) Class의 newInstance() 메소드
	 *  	1-1) 해당 클래스의 파라미터가 없는 생성자를 호출
	 *  		- setMethod DI를 활용해야한다.
	 *  	1-2) 그 결과 생성되는 Object를 반환
	 * @return
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	public Object createObject(String name) throws Exception {
		return Class.forName(name).newInstance();
	}
}
