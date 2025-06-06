package com.gmoon.junit5.jupiter.argumentssource.provider;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.support.AnnotationConsumer;
import org.junit.platform.commons.util.Preconditions;

import com.gmoon.junit5.jupiter.argumentssource.annotation.NegativeNumbers;

public class NegativeNumbersProvider implements ArgumentsProvider, AnnotationConsumer<NegativeNumbers> {

	private NegativeNumbers negativeNumbers;

	@Override
	public void accept(NegativeNumbers negativeNumbers) {
		this.negativeNumbers = negativeNumbers;
	}

	@Override
	public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
		int startInclusive = negativeNumbers.start();
		int endInclusive = negativeNumbers.end();
		Preconditions.condition(isNegativeNumbers(startInclusive),
			 String.format("The starting(%d) value must be negative.", startInclusive));
		Preconditions.condition(isNegativeNumbers(endInclusive),
			 String.format("The end(%d) value must be negative.", endInclusive));
		Preconditions.condition(startInclusive < endInclusive, "start must be less than end.");

		return IntStream.rangeClosed(startInclusive, endInclusive)
			 .mapToObj(Arguments::of);
	}

	private boolean isNegativeNumbers(int number) {
		return number < 0;
	}
}
