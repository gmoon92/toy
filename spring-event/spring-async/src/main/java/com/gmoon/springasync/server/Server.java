package com.gmoon.springasync.server;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class Server {
	private static final String GITHUB_BLOG_DOMAIN = "gmoon92.github.io";

	private final String host;
	private final String port1;
	private final String port2;

	public Server(String host, String port1, String port2) {
		this.host = host;
		this.port1 = port1;
		this.port2 = port2;
	}

	public static Server createGithubBlogServer() {
		return new Server(GITHUB_BLOG_DOMAIN, "443", "80");
	}

	public String getWebServerWithoutPortUrl(boolean useSsl) {
		return getWebServerUrl(useSsl, false);
	}

	public String getWebServerUrl(boolean useSsl) {
		return getWebServerUrl(useSsl, true);
	}

	private String getWebServerUrl(boolean useSsl, boolean includePort) {
		Protocol protocol = Protocol.of(useSsl);
		return String.format("%s://%s%s", protocol.getValue(), host, getPort(protocol, includePort));
	}

	private String getPort(Protocol protocol, boolean includePort) {
		if (!includePort) {
			return "";
		}

		String port = Protocol.HTTPS == protocol ? port1 : port2;
		return String.format(":%s", port);
	}
}
