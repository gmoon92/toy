package com.gmoon.springasync.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.DispatcherServlet;

import jakarta.annotation.PostConstruct;
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
