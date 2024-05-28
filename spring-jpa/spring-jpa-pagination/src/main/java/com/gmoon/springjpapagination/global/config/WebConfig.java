package com.gmoon.springjpapagination.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.data.web.config.PageableHandlerMethodArgumentResolverCustomizer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@EnableWebMvc
@EnableSpringDataWebSupport
public class WebConfig {

	@Bean
	public PageableHandlerMethodArgumentResolverCustomizer customizerPageableResolver() {
		return pageableResolver -> {
			pageableResolver.setPageParameterName("page");
			pageableResolver.setSizeParameterName("pageSize");
			pageableResolver.setOneIndexedParameters(false); // zero based page
			pageableResolver.setMaxPageSize(10);

			pageableResolver.setFallbackPageable(PageRequest.of(0, 15, Sort.unsorted()));
		};
	}
}
