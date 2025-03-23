package com.rsupport.rv5x.springaccesslog.global.filter;

import com.rsupport.rv5x.springaccesslog.global.utils.RequestUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.AbstractRequestLoggingFilter;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class WebAccessLogFilter extends AbstractRequestLoggingFilter {

	private final List<String> loggingUris;

	public WebAccessLogFilter(String... uris) {
		this.loggingUris = List.of(uris);
	}

	@Override
	protected void beforeRequest(HttpServletRequest request, String message) {
		if (isLoggableUri(request)) {
			log.info(message);
		}
	}

	@Override
	protected void afterRequest(HttpServletRequest request, String message) {
		if (isLoggableUri(request)) {
			log.info(message);
		}
	}

	private boolean isLoggableUri(HttpServletRequest request) {
		String requestUri = RequestUtils.getRequestUri(request);
		return loggingUris.contains(requestUri);
	}
}
