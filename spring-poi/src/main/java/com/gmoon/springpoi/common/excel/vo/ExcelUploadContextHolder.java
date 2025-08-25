package com.gmoon.springpoi.common.excel.vo;

import java.util.function.Supplier;

import org.springframework.util.Assert;

import com.gmoon.springpoi.common.utils.SecurityUtil;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ExcelUploadContextHolder {
	private static final ThreadLocal<Supplier<ExcelUploadContext>> context = new ThreadLocal<>();

	public static void clearContext() {
		context.remove();
	}

	public static ExcelUploadContext getContext() {
		return getDeferredContext().get();
	}

	public static void setContext(ExcelUploadContext context) {
		Assert.notNull(context, "Only non-null ExcelUploadContext instances are permitted");
		ExcelUploadContextHolder.context.set(() -> context);
	}

	private static Supplier<ExcelUploadContext> getDeferredContext() {
		Supplier<ExcelUploadContext> result = context.get();
		if (result == null) {
			ExcelUploadContext context = new ExcelUploadContext(SecurityUtil.getCurrentUser());
			result = () -> context;
			ExcelUploadContextHolder.context.set(result);
		}
		return result;
	}
}
