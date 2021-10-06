package com.moong.springaop.proxy3;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Bean;

@Configurable
public class MessageConfig {

	/**
	 * <bean id="message" class="com.learning.springpractice.aop.factory.MessageFactoryBean">
	 * 	<property name="text" value="Factory Bean" />
	 * </bean>
	 * */
	@Bean
	public MessageFactoryBean message() {
		MessageFactoryBean m = new MessageFactoryBean();
		m.setText("Factory Bean");
		return m;
	}
}
