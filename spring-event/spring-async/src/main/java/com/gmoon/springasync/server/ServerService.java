package com.gmoon.springasync.server;

import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ServerService {
	private final HttpServletRequest request;

	public String getWebServerWithoutPortUrl(Server server) {
		boolean isSecure = request.isSecure();
		return server.getWebServerWithoutPortUrl(isSecure);
	}
}
