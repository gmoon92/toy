package com.gmoon.springeventlistener.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableJpaRepositories(basePackages = {"com.gmoon.springeventlistener.*"})
@EnableJpaAuditing
@EnableTransactionManagement
@Configuration
public class JpaConfig {
}
