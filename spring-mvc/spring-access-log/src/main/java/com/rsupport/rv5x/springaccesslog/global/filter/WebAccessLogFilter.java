package com.rsupport.rv5x.springaccesslog.global.filter;

import com.rsupport.rv5x.springaccesslog.global.utils.RequestUtils;
import com.rsupport.rv5x.springaccesslog.http.context.HttpRequestPayload;
import com.rsupport.rv5x.springaccesslog.http.context.HttpRequestPayloadContextHolder;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.AbstractRequestLoggingFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class WebAccessLogFilter extends AbstractRequestLoggingFilter {

	@Override
	protected void doFilterInternal(
		 HttpServletRequest request,
		 HttpServletResponse response,
		 FilterChain filterChain
	) throws ServletException, IOException {
		ContentCachingRequestWrapper wrappingRequest = new ContentCachingRequestWrapper(request);
		ContentCachingResponseWrapper wrappingResponse = new ContentCachingResponseWrapper(response);
		try {
			HttpRequestPayloadContextHolder.setContext(new HttpRequestPayload(wrappingRequest));
			super.doFilterInternal(
				 wrappingRequest,
				 wrappingResponse,
				 filterChain
			);
		} finally {
			updateRequestBody(wrappingRequest);
			HttpRequestPayloadContextHolder.clearContext();

			wrappingResponse.copyBodyToResponse();
		}
	}

	private void updateRequestBody(ContentCachingRequestWrapper wrappingRequest) {
		String requestBody = RequestUtils.getRequestBody(wrappingRequest);
		log.info("requestBody: {}", requestBody);
		// todo merged access log entity...
	}


	@Override
	protected void beforeRequest(HttpServletRequest request, String message) {
		log.info("{}", message);
	}

	@Override
	protected void afterRequest(HttpServletRequest request, String message) {
		log.info("{}", message);
	}
}
