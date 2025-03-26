package com.rsupport.rv5x.springaccesslog.http.context;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;

import java.util.function.Supplier;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HttpRequestPayloadContextHolder {

	private static final ThreadLocal<Supplier<HttpRequestPayload>> contextHolder = new ThreadLocal<>();

	public static void clearContext() {
		contextHolder.remove();
	}

	public static HttpRequestPayload getContext() {
		return getDeferredContext().get();
	}

	public static void setContext(HttpRequestPayload context) {
		Assert.notNull(context, "Only non-null SecurityContext instances are permitted");
		contextHolder.set(() -> context);
	}

	private static Supplier<HttpRequestPayload> getDeferredContext() {
		Supplier<HttpRequestPayload> result = contextHolder.get();
		if (result == null) {
			HttpRequestPayload context = new HttpRequestPayload();
			result = () -> context;
			contextHolder.set(result);
		}
		return result;
	}
}
