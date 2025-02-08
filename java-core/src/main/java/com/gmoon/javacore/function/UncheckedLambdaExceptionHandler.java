package com.gmoon.javacore.function;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UncheckedLambdaExceptionHandler {

	public static <E extends Exception> Runnable runnable(CheckedRunnable<E> checkedRunnable) {
		return checkedRunnable.asUnchecked(RuntimeException::new);
	}

	public static <T, E extends Exception> Consumer<T> consumer(CheckedConsumer<T, E> checkedConsumer) {
		return checkedConsumer.asUnchecked(RuntimeException::new);
	}

	public static <T, E extends Exception> Supplier<T> supplier(CheckedSupplier<T, E> checkedSupplier) {
		return checkedSupplier.asUnchecked(RuntimeException::new);
	}

	public static <T, R, E extends Exception> Function<T, R> function(CheckedFunction<T, R, E> f) {
		return f.asUnchecked(RuntimeException::new);
	}

	interface UncheckedWrapper<T> {
		T asUnchecked(Function<Exception, RuntimeException> cause);
	}

	@FunctionalInterface
	public interface CheckedSupplier<T, E extends Exception> extends UncheckedWrapper<Supplier<T>> {
		T get() throws E;

		@Override
		default Supplier<T> asUnchecked(Function<Exception, RuntimeException> cause) {
			return () -> {
				try {
					return get();
				} catch (Exception e) {
					throw cause.apply(e);
				}
			};
		}
	}

	@FunctionalInterface
	public interface CheckedRunnable<E extends Exception> extends UncheckedWrapper<Runnable> {
		void run() throws E;

		@Override
		default Runnable asUnchecked(Function<Exception, RuntimeException> cause) {
			return () -> {
				try {
					run();
				} catch (Exception e) {
					throw cause.apply(e);
				}
			};
		}
	}

	@FunctionalInterface
	public interface CheckedConsumer<T, E extends Exception> extends UncheckedWrapper<Consumer<T>> {
		void accept(T t) throws E;

		@Override
		default Consumer<T> asUnchecked(Function<Exception, RuntimeException> cause) {
			return t -> {
				try {
					accept(t);
				} catch (Exception e) {
					throw cause.apply(e);
				}
			};
		}
	}

	@FunctionalInterface
	public interface CheckedFunction<T, R, E extends Exception> extends UncheckedWrapper<Function<T, R>> {
		R apply(T t) throws E;

		@Override
		default Function<T, R> asUnchecked(Function<Exception, RuntimeException> cause) {
			return t -> {
				try {
					return apply(t);
				} catch (Exception e) {
					throw cause.apply(e);
				}
			};
		}
	}
}
