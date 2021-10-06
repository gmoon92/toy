package com.gmoon.springaop.proxy3;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Bean;

/**
 * <bean id="message" class="com.learning.springpractice.aop.factory.MessageFactoryBean">
 * 	<property name="text" value="Factory Bean" />
 * </bean>
 * */
@Configurable
public class MessageConfig {
	@Bean
	public MessageFactoryBean message() {
		MessageFactoryBean m = new MessageFactoryBean();
		m.setText("Factory Bean");
		return m;
	}
}
