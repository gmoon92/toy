package com.gmoon.springaop.proxy3;

import org.springframework.beans.factory.FactoryBean;

import lombok.Setter;

/***
 * 팩토리 빈
 * 스프링을 대신해서 오브젝트의 생성로직을 담당하도록 만들어진 특별한 빈
 */
@Setter
public class MessageFactoryBean implements FactoryBean<Message> {
	private String text;

	@Override
	public Message getObject() { // 빈 오브젝트 생성 후 반환
		return Message.newMessage(this.text);
	}

	@Override
	public Class<?> getObjectType() {    // 생성되는 오브젝트 타입 반환
		return Message.class;
	}

	@Override
	public boolean isSingleton() { //getObject()가 돌려주는 오브젝트가 항상 같은 싱글톤 오브젝트인지 알려줌
		return FactoryBean.super.isSingleton();
	}
}
