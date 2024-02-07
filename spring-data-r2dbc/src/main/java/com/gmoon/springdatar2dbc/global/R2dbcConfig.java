package com.gmoon.springdatar2dbc.global;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

@Configuration
@EnableR2dbcRepositories(
	basePackages = {"com.gmoon.*"},
	includeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, value = ReactRepository.class)
)
public class R2dbcConfig {

}
