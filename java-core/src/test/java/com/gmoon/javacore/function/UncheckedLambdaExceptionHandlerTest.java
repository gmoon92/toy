package com.gmoon.javacore.function;

import static org.assertj.core.api.Assertions.*;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UncheckedLambdaExceptionHandlerTest {

	@DisplayName("wrapper lambda")
	@Test
	void wrapper() {
		Runnable runnable = UncheckedLambdaExceptionHandler.runnable(this::throwsCheckedException);
		assertThatCode(runnable::run)
			 .isInstanceOf(RuntimeException.class);

		Consumer<String> consumer = UncheckedLambdaExceptionHandler.consumer(this::throwsCheckedException);
		assertThatCode(() -> consumer.accept("occur unknown exception."))
			 .isInstanceOf(RuntimeException.class);

		Supplier<String> supplier = UncheckedLambdaExceptionHandler.supplier(() -> throwsCheckedException("occur unknown exception."));
		assertThatCode(supplier::get)
			 .isInstanceOf(RuntimeException.class);

		Function<String, String> function = UncheckedLambdaExceptionHandler.function(this::throwsCheckedException);
		assertThatCode(() -> function.apply("occur unknown exception."))
			 .isInstanceOf(RuntimeException.class);
	}

	private void throwsCheckedException() throws Exception {
		throw new Exception("occur unknown exception.");
	}

	private String throwsCheckedException(String message) throws Exception {
		throw new Exception(message);
	}
}
