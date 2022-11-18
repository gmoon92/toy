package com.gmoon.springcoreutils.parser;

import static org.assertj.core.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.SimpleEvaluationContext;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Spring Expression Language
 * https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#expressions
 * https://www.baeldung.com/spring-expression-language
 */
@Slf4j
class SpringExpressionLanguageTest {

	private ExpressionParser parser;

	@BeforeEach
	void setUp() {
		parser = new SpelExpressionParser();
	}

	@Test
	void getValue() {
		Expression exp = parser.parseExpression("'Hello World'.concat('!')");

		String message = exp.getValue(String.class);

		assertThat(message).isEqualTo("Hello World!");
	}

	@Test
	void logicalOperators() {
		List<Integer> primes = Arrays.asList(2, 3, 5, 7, 11, 13, 17);
		StandardEvaluationContext context = new StandardEvaluationContext();
		context.setVariable("primes", primes);

		Expression expression = parser.parseExpression("#primes.?[#this>10]");
		@SuppressWarnings("unchecked") List<Integer> primesGreaterThanTen
			= (List<Integer>)expression.getValue(context);

		assertThat(primesGreaterThanTen)
			.containsOnly(11, 13, 17);
	}

	@Test
	void usingRegx() {
		String regx = "'100' matches '\\d+'";

		Expression expression = parser.parseExpression(regx);

		assertThat(expression.getValue(Boolean.class)).isTrue();
	}

	@DisplayName("SpEL Operators")
	@Nested
	class OperatorsTest {

		@DisplayName("lt (<)"
			+ "gt (>)"
			+ "le (<=)"
			+ "ge (>=)"
			+ "eq (==)"
			+ "ne (!=)"
			+ "div (/)"
			+ "mod (%)"
			+ "not (!)")
		@Test
		void relationalOperators() {
			assertThat(getValue("2 == 2", Boolean.class)).isTrue();
			assertThat(getValue("2 eq 2", Boolean.class)).isTrue();
			assertThat(getValue("2 != 2", Boolean.class)).isFalse();
			assertThat(getValue("!(2 == 2)", Boolean.class)).isFalse();
			assertThat(getValue("2 ne 2", Boolean.class)).isFalse();
			assertThat(getValue("2 < 3.74", Boolean.class)).isTrue();
			assertThat(getValue("new Integer(0) < new Integer(3)", Boolean.class)).isTrue();

			// is null not eq zero
			assertThat(getValue("0 > null", Boolean.class)).isTrue();
			assertThat(getValue("0 gt null", Boolean.class)).isTrue();
			assertThat(getValue("0 != null", Boolean.class)).isTrue();
			assertThat(getValue("0 ne null", Boolean.class)).isTrue();
			assertThat(getValue("0 == null", Boolean.class)).isFalse();
			assertThat(getValue("0 eq null", Boolean.class)).isFalse();
			assertThat(getValue("0 < null", Boolean.class)).isFalse();
			assertThat(getValue("0 lt null", Boolean.class)).isFalse();

			// evaluates relational operators
			assertThat(getValue("1 instanceof T(Integer)", Boolean.class)).isTrue();
			assertThat(getValue("1 instanceof T(int)", Boolean.class)).isFalse();
			assertThat(getValue("'xyz' instanceof T(Integer)", Boolean.class)).isFalse();
			assertThat(getValue("'5.00' matches '^-?\\d+(\\.\\d{2})?$'", Boolean.class)).isTrue();
			assertThat(getValue("'5.0067' matches '^-?\\d+(\\.\\d{2})?$'", Boolean.class)).isFalse();
		}

		@DisplayName("and (&&)"
			+ "or (||)"
			+ "not (!)")
		@Test
		void logicalOperators() {
			assertThat(getValue("true and false", Boolean.class)).isFalse();
			assertThat(getValue("!true", Boolean.class)).isFalse();
			assertThat(getValue("true and true", Boolean.class)).isTrue();
			assertThat(getValue("true or true", Boolean.class)).isTrue();
			assertThat(getValue("!false", Boolean.class)).isTrue();
		}

		@DisplayName("'+', '-', '*', '/', '%', '^'")
		@Test
		void mathematicalOperators() {
			assertThat(getValue("1 + 1", Integer.class)).isEqualTo(2);
			assertThat(getValue("1 - -3", Integer.class)).isEqualTo(4);
			assertThat(getValue("1000.00 - 1e4", Double.class)).isEqualTo(-9_000d);
			assertThat(getValue("-2 * -3", Integer.class)).isEqualTo(6);
			assertThat(getValue("6 / -3", Integer.class)).isEqualTo(-2);
			assertThat(getValue("7 % 4", Integer.class)).isEqualTo(3);
			assertThat(getValue("1+2-3*8", Integer.class)).isEqualTo(-21);
		}

		@DisplayName("반드시 public setter properties 필요하다.")
		@Test
		void assignmentOperator() {
			Consumer consumer = new Consumer();
			EvaluationContext context = SimpleEvaluationContext.forReadWriteDataBinding().build();

			parser.parseExpression("name").setValue(context, consumer, "gmoon");

			Expression expression = parser.parseExpression("name = 'gmoon'");
			assertThat(expression.getValue(context, consumer, String.class))
				.isEqualTo("gmoon");
		}

		@Setter
		class Consumer {
			private String name;
		}

		@Test
		void arithmeticOperators() {
			assertThat(getValue("10 + 2", Integer.class)).isEqualTo(12);
			assertThat(getValue("10 - 2", Integer.class)).isEqualTo(8);
			assertThat(getValue("2 ^ 9", Integer.class)).isEqualTo(512);
			assertThat(getValue("5 * 2", Integer.class)).isEqualTo(10);
			assertThat(getValue("36 div 2", Integer.class)).isEqualTo(18);
			assertThat(getValue("36 / 2", Integer.class)).isEqualTo(18);
			assertThat(getValue("37 mod 10", Integer.class)).isEqualTo(7);
			assertThat(getValue("37 % 10", Integer.class)).isEqualTo(7);
		}

		private <T> T getValue(String expressionString, Class<T> desiredResultType) {
			return parser.parseExpression(expressionString)
				.getValue(desiredResultType);
		}
	}
}
