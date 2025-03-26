package com.rsupport.rv5x.springaccesslog.http.context;

import com.rsupport.rv5x.springaccesslog.global.utils.RequestUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@NoArgsConstructor
@Getter
@ToString
public class HttpRequestPayload {

	private String traceId;               // 요청 추적 ID (로그 상관관계 분석)
	private LocalDateTime timestamp;      // 요청 시간
	private String uri;                   // 요청 URI
	private String method;                // HTTP 메서드
	private String queryString;           // 쿼리 스트링
	private String requestBody;           // 요청 본문 (POST, PUT 등에서 사용)
	private Map<String, String> headers;  // 요청 헤더 정보
	private String clientIp;              // 요청을 보낸 클라이언트 IP
	private String userAgent;             // 브라우저/클라이언트 정보
	private String referer;               // 이전 페이지 URL (어디에서 왔는지)
	private String sessionId;             // 세션 ID (로그인 추적 등)

	public HttpRequestPayload(HttpServletRequest request) {
		traceId = UUID.randomUUID().toString(); // 요청 추적 ID 자동 생성
		timestamp = LocalDateTime.now();
		uri = RequestUtils.getRequestUri(request);
		method = RequestUtils.getRequestMethod(request);
		queryString = RequestUtils.getRequestQueryString(request);
		headers = RequestUtils.getRequestHeaders(request);
		clientIp = RequestUtils.getRemoteIp(request);
		userAgent = RequestUtils.getHeader(request, "User-Agent");
		referer = RequestUtils.getHeader(request, "Referer");
		sessionId = RequestUtils.getRequestedSessionId(request);
	}

	public void setRequestBody(ContentCachingRequestWrapper request) {
		boolean multipartRequest = request.getContentType() != null
			 && request.getContentType().startsWith("multipart/");
		if (multipartRequest) {
			requestBody = "[Multipart data omitted]";
		} else {
			requestBody = RequestUtils.getRequestBody(request);
		}
	}
}
