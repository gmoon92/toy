package com.rsupport.rv5x.springaccesslog.global.config;

import com.rsupport.rv5x.springaccesslog.global.filter.HttpContentCacheFilter;
import com.rsupport.rv5x.springaccesslog.global.filter.WebAccessLogFilter;
import jakarta.servlet.Filter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.AbstractRequestLoggingFilter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

	@Override
	public void addViewControllers(ViewControllerRegistry registry) {
		WebMvcConfigurer.super.addViewControllers(registry);
	}

	@Bean
	public Filter contentCachingFilter() {
		return new HttpContentCacheFilter();
	}

	/**
	 * @see org.springframework.web.util.ContentCachingRequestWrapper#ContentCachingRequestWrapper(HttpServletRequest, int)
	 */
	@Bean
	public Filter accessLogFilter() {
		WebAccessLogFilter filter = new WebAccessLogFilter(
			 "/user",
			 "/user/view"
		);
		filter.setIncludeClientInfo(false);
		filter.setIncludePayload(true);
		filter.setIncludeHeaders(false);
		filter.setIncludeQueryString(true);
//		filter.setMaxPayloadLength(50);
		filter.setBeforeMessagePrefix(AbstractRequestLoggingFilter.DEFAULT_BEFORE_MESSAGE_PREFIX);
		filter.setBeforeMessageSuffix(AbstractRequestLoggingFilter.DEFAULT_BEFORE_MESSAGE_SUFFIX);
		filter.setAfterMessagePrefix(AbstractRequestLoggingFilter.DEFAULT_AFTER_MESSAGE_PREFIX);
		filter.setAfterMessageSuffix(AbstractRequestLoggingFilter.DEFAULT_AFTER_MESSAGE_SUFFIX);
		return filter;
	}
}
