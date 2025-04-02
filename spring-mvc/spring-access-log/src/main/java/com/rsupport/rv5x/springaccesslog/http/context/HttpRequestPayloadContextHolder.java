package com.rsupport.rv5x.springaccesslog.http.context;

import java.util.function.Supplier;

import org.springframework.util.Assert;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HttpRequestPayloadContextHolder {

	private static final ThreadLocal<Supplier<HttpRequestPayload>> context = new ThreadLocal<>();

	public static void clearContext() {
		context.remove();
	}

	public static HttpRequestPayload getContext() {
		return getDeferredContext().get();
	}

	public static void setContext(HttpRequestPayload context) {
		Assert.notNull(context, "Only non-null SecurityContext instances are permitted");
		HttpRequestPayloadContextHolder.context.set(() -> context);
	}

	private static Supplier<HttpRequestPayload> getDeferredContext() {
		Supplier<HttpRequestPayload> result = context.get();
		if (result == null) {
			HttpRequestPayload context = new HttpRequestPayload();
			result = () -> context;
			HttpRequestPayloadContextHolder.context.set(result);
		}
		return result;
	}
}
