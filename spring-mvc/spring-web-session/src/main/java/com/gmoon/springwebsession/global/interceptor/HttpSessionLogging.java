package com.gmoon.springwebsession.global.interceptor;

import java.util.Enumeration;

import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HttpSessionLogging implements HandlerInterceptor {

	@Override
	public void afterCompletion(
		 HttpServletRequest request,
		 HttpServletResponse response,
		 Object handler,
		 Exception ex
	) {
		loggingForHttpSessionAttributes(request);
	}

	private void loggingForHttpSessionAttributes(HttpServletRequest request) {
		HttpSession session = request.getSession(false);
		log.info("==================HTTP Session Start==================");
		if (session == null) {
			log.info("Empty HttpSession");
		} else {
			Enumeration<String> attributeNames = session.getAttributeNames();
			while (attributeNames.hasMoreElements()) {
				String name = attributeNames.nextElement();
				Object value = session.getAttribute(name);
				log.info("attr: {}={}", name, value);
			}
		}
		log.info("==================HTTP Session End==================");
	}
}
