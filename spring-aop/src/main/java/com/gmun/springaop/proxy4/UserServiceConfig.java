package com.gmun.springaop.proxy4;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Bean;

@Configurable
public class UserServiceConfig {

	@Bean
	public ProfileFactoryBean pxuserService() {
		return new ProfileFactoryBean();
	}
}
