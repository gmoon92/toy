package com.rsupport.rv5x.springaccesslog.global.config;

import com.rsupport.rv5x.springaccesslog.global.filter.WebAccessLogFilter;
import com.rsupport.rv5x.springaccesslog.global.utils.RequestUtils;
import jakarta.servlet.Filter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.filter.AbstractRequestLoggingFilter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Collections;

@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

	@Override
	public void addViewControllers(ViewControllerRegistry registry) {
		WebMvcConfigurer.super.addViewControllers(registry);
	}

	/**
	 * @see org.springframework.web.util.ContentCachingRequestWrapper#ContentCachingRequestWrapper(HttpServletRequest, int)
	 */
	@Bean
	public FilterRegistrationBean<Filter> webAccessFilter() {
		WebAccessLogFilter webAccessFilter = new WebAccessLogFilter();
		webAccessFilter.setIncludeClientInfo(true);
		webAccessFilter.setIncludePayload(true);
		webAccessFilter.setIncludeHeaders(false);
		webAccessFilter.setIncludeQueryString(true);
		webAccessFilter.setMaxPayloadLength((int) RequestUtils.MAX_BODY_LENGTH.toBytes());
		webAccessFilter.setBeforeMessagePrefix(AbstractRequestLoggingFilter.DEFAULT_BEFORE_MESSAGE_PREFIX);
		webAccessFilter.setBeforeMessageSuffix(AbstractRequestLoggingFilter.DEFAULT_BEFORE_MESSAGE_SUFFIX);
		webAccessFilter.setAfterMessagePrefix(AbstractRequestLoggingFilter.DEFAULT_AFTER_MESSAGE_PREFIX);
		webAccessFilter.setAfterMessageSuffix(AbstractRequestLoggingFilter.DEFAULT_AFTER_MESSAGE_SUFFIX);

		FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>(webAccessFilter);
		registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
		registration.setUrlPatterns(Collections.singleton("*"));
//		registration.setUrlPatterns(Collections.singleton("/user/*"));
		return registration;
	}
}
