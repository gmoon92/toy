package com.gmoon.springasync.config;

import javax.annotation.PostConstruct;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.DispatcherServlet;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class FilterConfig {
	private final DispatcherServlet dispatcherServlet;

	@PostConstruct
	public void init() {
		dispatcherServlet.setThreadContextInheritable(true);
	}
}
