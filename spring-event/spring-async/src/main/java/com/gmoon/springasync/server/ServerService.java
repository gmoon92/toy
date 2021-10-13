package com.gmoon.springasync.server;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Service;

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
